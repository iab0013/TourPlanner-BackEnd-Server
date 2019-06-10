package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import model.Path;
import model.Poi;
import model.Poi.Category;
import model.Route;
import resource.request.Request;
import resource.response.CityResponse;
import resource.response.HotelsResponse;
import resource.response.PoiDetailsResponse;
import resource.response.UserActivity;
import util.Misc;
import crypt.BCrypt;

/**
 * Clase fachada que interacciona con la base de datos con las diferentes
 * operaciones que pueden realizarse sobre esta.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class DatabaseFacade implements InterfaceDatabase {

	private static DatabaseFacade dbFacade;
	private DataSource ds;
	private String tablePrefix;
	private final String NO_INTEREST = "No se ha recibido interés para ninguna categoría";

	/**
	 * Constructor privado de DatabaseFacade (patrón Singleton).
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 *             en caso de algún problema en la conexión a la base de datos
	 * 
	 */
	private DatabaseFacade() throws ClassNotFoundException, SQLException,
			NamingException {
		Context initialContext = new InitialContext();
		DatabaseProperties dbProperties = new DatabaseProperties(
				"javabase.jndi");
		ds = (DataSource) initialContext.lookup(dbProperties.getProperty("url",
				false));
		tablePrefix = "" + tablePrefix + "";
	}

	/**
	 * Método "getInstance" correspondiente al patrón Singleton, para manejar
	 * únicamente una instancia de la fachada.
	 * 
	 * @throws DatabaseException
	 * 
	 * @return la instancia de la clase
	 * 
	 */
	public static DatabaseFacade getInstance() throws DatabaseException {
		if (dbFacade == null) {
			try {
				dbFacade = new DatabaseFacade();
			} catch (ClassNotFoundException e) {
				throw new DatabaseException(e);
			} catch (SQLException e) {
				throw new DatabaseException(e);
			} catch (NamingException e) {
				throw new DatabaseException(e);
			}
		}
		return dbFacade;
	}

	/**
	 * Método que cambia la variable que actúa como prefijo de las tablas que
	 * contienen las rutas en la BBDD (fo_ : a pie; dr_ : en coche).
	 * 
	 * @param prefix
	 *            prefijo a usar en las consultas de la base de datos
	 * 
	 */
	public void changePrefixTable(String prefix) {
		this.tablePrefix = prefix;
	}

	/**
	 * Método que calcula las coordenadas de la ruta resultante. Para ello habrá
	 * que ir uniendo los diferentes sub-tramos o arcos que la componen.
	 * 
	 * @param path
	 *            ruta final calculada
	 * @param setOfTableIds
	 *            estructura que almacena los ids para extraer posteriormente
	 *            las geometrías
	 * @return string que contiene todas las coordenadas que componen la ruta
	 * 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> obtainResultingCoordinates(Path path,
			ArrayList<ArrayList<Integer>> setOfTableIds) {
		ArrayList<String> resultingCoordinates = new ArrayList<String>();
		ArrayList<String> setOfEdges, previous_setOfEdges = null;
		ArrayList<String> setOfCoordinates;
		double distance, distance2;
		for (int i = 1; i < path.getPath().size(); i++) {
			if (setOfTableIds.get(i - 1).size() > 0) {
				setOfEdges = getSetOfSourceTargetLinestringsFromId(setOfTableIds
						.get(i - 1));
				if (i == 1) {
					distance = calculateDistanceBetweenPois(path.get(i - 1)
							.getCoordinates(), "POINT("
							+ Misc.extractCoordinates(setOfEdges.get(0)).get(0)
							+ ")");
					setOfCoordinates = Misc.extractCoordinates(setOfEdges
							.get(0));
					distance2 = calculateDistanceBetweenPois(
							path.get(i - 1).getCoordinates(),
							"POINT("
									+ setOfCoordinates.get(setOfCoordinates
											.size() - 1) + ")");
					String[] orderedLinestrings;
					ArrayList<String> test;
					if (distance > distance2) {
						test = Misc.extractCoordinates(setOfEdges.get(0));
						orderedLinestrings = test.toArray(new String[test
								.size()]);
						setOfEdges
								.set(0,
										"LINESTRING("
												+ Misc.getReversedEdge(orderedLinestrings)
												+ ")");
					}
				}
				setOfEdges = Misc.orderLineString(setOfEdges);
				if (i != 1 && setOfEdges != null && previous_setOfEdges != null) {
					setOfEdges = Misc.linkSetOfEdges(setOfEdges,
							previous_setOfEdges);
				}

				if (setOfEdges != null) {
					previous_setOfEdges = (ArrayList<String>) setOfEdges
							.clone();
				}
				if (setOfEdges != null) {
					resultingCoordinates.add(Misc
							.formatReturningCoordinates(setOfEdges));
				}
			}
		}
		return resultingCoordinates;
	}

	/**
	 * Método que devuelve las coordenadas de un determinado nodo (a partir de
	 * su id).
	 * 
	 * @param node_id
	 *            id del nodo del que quieren extraerse las coordenadas
	 * @return coordenadas del nodo
	 * @throws DatabaseException
	 * 
	 */
	public String coordinatesFromNodeId(Long node_id) throws DatabaseException {
		String point = "";
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT ST_ASTEXT(geom) as geom from nodes where id=?";
			st = con.prepareStatement(sql);
			st.setLong(1, node_id);
			rs = st.executeQuery();
			if (rs.next()) {
				point = rs.getString("geom");
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return point;
	}

	/**
	 * Método que devuelve una lista de puntos de interés cercanos a una
	 * determinada ubicación, en un radio determinado.
	 * 
	 * @param point
	 *            punto sobre el que se buscan puntos de interés cercanos
	 * @param radius
	 *            radio de búsqueda
	 * @param request
	 *            petici�n recibida.
	 * @return lista de puntos de inter�s cercanos
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<Poi> getNearestNodes(String point, double radius,
			Request request) throws DatabaseException {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		ArrayList<Poi> nodesId = new ArrayList<Poi>();
		try {
			con = ds.getConnection();
			String sql, tagFilter = "";
			if (!(request.getCulture_factor() == 0.0
					&& request.getGastronomy_factor() == 0.0
					&& request.getLeisure_factor() == 0 && request
						.getNature_factor() == 0)) {
				tagFilter = createWhereClause(request);
				if (tagFilter.compareTo("") != 0) {
					tagFilter = "(" + tagFilter + ") AND ";
				} else {
					throw new DatabaseException(NO_INTEREST);
				}
			}
			sql = "SELECT distinct on (poi_id) poi_id, st_astext(the_geom) as the_geom, v, score, type FROM("
					+ "SELECT poi_data.id as poi_id, geom as the_geom, v, poi_data.score as score, type "
					+ "FROM nodes, node_tags, poi_data WHERE node_id=nodes.id "
					+ "AND poi_id=node_id "
					+ " UNION "
					+ "SELECT poi_data.id as poi_id, the_geom, v, score, type "
					+ "FROM ways, way_tags, poi_data "
					+ "WHERE ways.id=way_id AND poi_id=ways.id )T WHERE "
					+ tagFilter
					+ " ST_DWithin(the_geom,'SRID=4326;"
					+ point
					+ "'::geography, ?)";
			st = con.prepareStatement(sql);
			st.setDouble(1, radius);
			rs = st.executeQuery();
			while (rs.next()) {
				Poi poi = new Poi();
				poi.setPoi_id(rs.getLong("poi_id"));
				poi.setCoordinates(rs.getString("the_geom"));
				poi.setScore(rs.getInt("score"));
				poi.setTag(rs.getString("v"));
				poi.setType(rs.getString("type").charAt(0));
				calculateNodeAsociatedScore(poi, request, false);
				nodesId.add(poi);
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return nodesId;
	}

	/**
	 * Método que calcula el score asociado a un determiando POI.
	 * 
	 * @param poi
	 *            punto sobre el que se va a calcular el score.
	 * @param request
	 *            petición del cliente
	 * @param itinearyExpress
	 *            variable que indica si se trata de una ruta recomendada
	 * 
	 */
	public void calculateNodeAsociatedScore(Poi poi, Request request,
			boolean itinearyExpress) {
		int scoreTaggedNode, scorePromotedNode, scorePreferencesNode;
		double currentScore, finalScore, factorCurrentScore, factortScorePromoted;
		if (itinearyExpress) {
			factorCurrentScore = 0.3;
			factortScorePromoted = 0.6;
		} else {
			factorCurrentScore = 0.2;
			factortScorePromoted = 0.5;
		}
		scoreTaggedNode = calculateScoreTaggedNodes(poi);
		scorePromotedNode = getScorePromotedNodes(poi);
		scorePreferencesNode = getScorePreferenceNodes(poi, request);
		currentScore = poi.getScore();
		finalScore = currentScore * factorCurrentScore + scoreTaggedNode * 0.1
				+ scorePreferencesNode * 0.2 + scorePromotedNode
				* factortScorePromoted;
		poi.setScore(finalScore);

	}

	/**
	 * Método que devuelve el valor asociado a las preferencias introducidas por
	 * el usuario.
	 * 
	 * @param poi
	 *            que se evalúa
	 * @param request
	 *            petición del cliente
	 * @return score asociado
	 * 
	 */
	public int getScorePreferenceNodes(Poi poi, Request request) {
		int scorePreferenceNode = -1;
		if (request.getGastronomy_factor() > 0
				&& poi.getCategory().compareTo(Category.GASTRONOMY) == 0) {
			scorePreferenceNode = request.getGastronomy_factor();
		}
		if (request.getGastronomy_factor() > 0
				&& poi.getCategory().compareTo(Category.LEISURE) == 0) {
			scorePreferenceNode = request.getLeisure_factor();
		}
		if (request.getGastronomy_factor() > 0
				&& poi.getCategory().compareTo(Category.CULTURE) == 0) {
			scorePreferenceNode = request.getCulture_factor();
		}
		if (request.getGastronomy_factor() > 0
				&& poi.getCategory().compareTo(Category.NATURE) == 0) {
			scorePreferenceNode = request.getNature_factor();
		}
		return scorePreferenceNode;

	}

	/**
	 * Método que comprueba si un determinado POI está promocionado, y si es así
	 * devuelve el score asociado.
	 * 
	 * @param poi
	 *            que se evalúa
	 * 
	 * @return score asociado
	 */
	public int getScorePromotedNodes(Poi poi) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql;
		try {
			con = ds.getConnection();
			sql = "select score_associated from promoted_places, promoted_level where poi_id=? and promoted_places.level=promoted_level.id";
			st = con.prepareStatement(sql);
			st.setLong(1, poi.getPoi_id());
			rs = st.executeQuery();
			if (rs.next()) {
				poi.setPromoted(true);
				return rs.getInt("score_associated");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return getNoPromotedScore();
	}

	/**
	 * Método que devuelve el valor asociado a los POIs no promocionados.
	 * 
	 * @return score asociado
	 * 
	 */
	public int getNoPromotedScore() {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql;
		try {
			con = ds.getConnection();
			sql = "select score_associated from promoted_level where level='no_promoted'";
			st = con.prepareStatement(sql);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getInt("score_associated");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return -1;
	}

	/**
	 * Método que devuelve el score asociado al tag al que pertenece un
	 * determinado POI.
	 * 
	 * @param poi
	 *            que se evalúa
	 * @return score asociado
	 * 
	 */
	public int calculateScoreTaggedNodes(Poi poi) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql;
		int scoreAsociated = 0;
		Category category;
		try {
			con = ds.getConnection();
			if (poi.getType() == 'N') {
				sql = "select k,v from node_tags, poi_data where poi_data.id=? AND node_id=poi_id";
			} else {
				sql = "select k,v from way_tags, poi_data where poi_data.id=? AND way_id=poi_id";
			}
			st = con.prepareStatement(sql);
			st.setLong(1, poi.getPoi_id());
			rs = st.executeQuery();
			while (rs.next()) {
				if (rs.getString("k").compareTo("name") == 0) {
					poi.setName(rs.getString("v"));
				} else {
					category = getCategoryFromTagName(rs.getString("v"));
					if (category != null) {
						poi.setCategory(category);
					}
					scoreAsociated = getScoreAndTimeToStayAsociatedToATag(
							rs.getString("v"), poi);
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return scoreAsociated;

	}

	/**
	 * Método que devuelve la categoría a la que pertenece un determinado tag o
	 * etiqueta.
	 * 
	 * @param tagName
	 *            etiqueta o tag que se evalúa
	 * @return categoría a la que pertenece el tag
	 */
	public Category getCategoryFromTagName(String tagName) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql;
		Category category = null;
		try {
			con = ds.getConnection();
			tagName = tagName.replace("'", "''");
			sql = "select type from categories, tags where categories.id=category_id and tags.tag=?";
			st = con.prepareStatement(sql);
			st.setString(1, tagName);
			rs = st.executeQuery();
			if (rs.next()) {
				if (rs.getString("type").compareTo("Gastronomy") == 0) {
					category = Category.GASTRONOMY;
				}
				if (rs.getString("type").compareTo("Leisure") == 0) {
					category = Category.LEISURE;
				}
				if (rs.getString("type").compareTo("Culture") == 0) {
					category = Category.CULTURE;
				}
				if (rs.getString("type").compareTo("Nature") == 0) {
					category = Category.NATURE;
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return category;
	}

	/**
	 * Método que devuelve el score predeterminado y coste para un determinado
	 * tag. También establece el tiempo establecido para permanecer en dicho POI
	 * (coste).
	 * 
	 * @param tagName
	 *            etiqueta o tag que se evalúa
	 * @param poi
	 *            punto de interés que se evalua
	 * @return score asociado al tagName
	 */
	public int getScoreAndTimeToStayAsociatedToATag(String tagName, Poi poi) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql;
		int score = -1;
		try {
			con = ds.getConnection();
			if (tagName.contains("'")) {
				tagName = tagName.replace("'", "''");
			}
			sql = "select score,time_to_stay from tags where tag=?";
			st = con.prepareStatement(sql);
			st.setString(1, tagName);
			rs = st.executeQuery();
			while (rs.next()) {
				if (score < rs.getInt("score")) {
					score = rs.getInt("score");
					poi.setTime_to_stay(rs.getInt("time_to_stay"));
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return score;
	}

	/**
	 * Método que devuelve el conjunto de etiquetas o tags para una determinada
	 * categoría.
	 * 
	 * @param category
	 *            categoría
	 * @param excludedTags
	 *            tags a excluir de la ruta final generada
	 * @return conjunto de tags contenidas en esa categoría
	 */
	public String getSetOfTags(int category, List<String> excludedTags) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String tagFilter, sql;
		try {
			con = ds.getConnection();
			sql = "select * from tags where category_id =?";
			tagFilter = "v=";
			st = con.prepareStatement(sql);
			st.setInt(1, category);
			rs = st.executeQuery();
			while (rs.next()) {
				if (!excludedTags.contains(rs.getString("tag"))) {
					tagFilter += "'" + rs.getString("tag") + "'" + " OR v=";
				}
			}
			tagFilter = tagFilter.substring(tagFilter.indexOf("v"),
					tagFilter.lastIndexOf("O"));
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return tagFilter;
	}

	/**
	 * Método que crea el contenido de la claúsula WHERE para la consulta de los
	 * nodos o POIs cercanos.
	 * 
	 * @param request
	 *            petición del usuario
	 * @return contenido de la claúsula WHERE
	 */
	public String createWhereClause(Request request) {
		String tagFilter = "";
		List<String> excludedTags = new ArrayList<String>();
		if (request.getSetOfTags() != null) {
			excludedTags = Arrays.asList(request.getSetOfTags().split(
					"\\s*,\\s*"));
		}
		if (request.getGastronomy_factor() > 0) {
			tagFilter = getSetOfTags(1, excludedTags) + "OR ";
		}
		if (request.getLeisure_factor() > 0) {
			tagFilter += getSetOfTags(2, excludedTags) + "OR ";
		}
		if (request.getCulture_factor() > 0) {
			tagFilter += getSetOfTags(3, excludedTags) + "OR ";
		}
		if (request.getNature_factor() > 0) {
			tagFilter += getSetOfTags(4, excludedTags) + "OR ";
		}
		tagFilter = tagFilter.substring(0, tagFilter.lastIndexOf("O"));
		return tagFilter;
	}

	/**
	 * Método que obtiene el conjunto de LINESTRINGS asociados a una serie de
	 * tramos, a partir de sus ids.
	 * 
	 * @param setOfIds
	 *            conjunto de ids
	 * @return conjunto de LINESTRINGS asociados al conjunto de ids recibidos
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<String> getSetOfSourceTargetLinestringsFromId(
			ArrayList<Integer> setOfIds) {
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement st = null;
		String sql;
		ArrayList<String> linestrings = new ArrayList<String>();
		try {
			con = ds.getConnection();
			for (int id : setOfIds) {
				sql = "select st_astext(the_geom) as linestring from "
						+ tablePrefix + "2po_4pgr where id=?";
				st = con.prepareStatement(sql);
				st.setInt(1, id);
				rs = st.executeQuery();
				if (rs.next()) {
					linestrings.add(rs.getString("linestring"));
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return linestrings;
	}

	/**
	 * Método que calcula la distancia entre dos determinados puntos de interés.
	 * 
	 * @param coordinates_source
	 *            origen del tramo
	 * @param middle_coordinates
	 *            destino del tramo
	 * @return distancia entre los puntos
	 * @throws DatabaseException
	 * 
	 */
	public double calculateDistanceBetweenPois(String coordinates_source,
			String middle_coordinates) {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sql;
		double searchRadius = -1.0;
		try {
			con = ds.getConnection();
			sql = "SELECT ST_Distance(ST_GeographyFromText('SRID=4326;"
					+ coordinates_source
					+ "'),ST_GeographyFromText('SRID=4326;"
					+ middle_coordinates + "')) as radius";
			st = con.prepareStatement(sql);
			rs = st.executeQuery();
			if (rs.next()) {
				searchRadius = rs.getDouble("radius");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return searchRadius;
	}

	/**
	 * Método que devuelve un listado con las ciudades que comienzan por el
	 * prefijo recibido como parámetro.
	 * 
	 * @param prefix
	 *            prefijo
	 * @return listado de ciudades que comienzan por el prefijo
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<String> getCitiesByPrefix(String prefix) {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sql;
		ArrayList<String> citiesList;
		try {
			con = ds.getConnection();
			citiesList = new ArrayList<String>();
			sql = "select city_name from cities where UPPER(city_name) LIKE UPPER('"
					+ prefix + "%')";
			st = con.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				citiesList.add(rs.getString("city_name"));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return citiesList;
	}

	/**
	 * Método que devuelve un conjunto de puntos de interés cercanos a unas
	 * coordenadas determinadas y ordenados por distancia.
	 * 
	 * @param coordinates
	 *            coordenadas a partir de las que se busca
	 * @return listado de puntos de interés cercanos y ordenados por distancia
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<Poi> getNearestPoiFromCoordinates(String coordinates) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		ArrayList<Poi> coordinatesNearestPois;
		try {
			con = ds.getConnection();
			coordinatesNearestPois = new ArrayList<Poi>();
			String sql;
			int sqlQueryLimit = 10;
			do {
				sqlQueryLimit += 10;
				sql = "SELECT POI_ID, ST_ASTEXT(THE_GEOM) AS THE_GEOM FROM POI_DATA ORDER BY the_geom <-> 'SRID=4326;"
						+ coordinates + "'::geometry LIMIT " + sqlQueryLimit;

				st = con.prepareStatement(sql);
				rs = st.executeQuery();
				while (rs.next()) {
					Poi poi = new Poi();
					poi.setCoordinates(rs.getString("the_geom"));
					poi.setPoi_id(rs.getLong("poi_id"));
					coordinatesNearestPois.add(poi);
				}
				Misc.removeUnconnectedPoisFromResultList(
						coordinatesNearestPois, tablePrefix);

			} while (coordinatesNearestPois.size() < 20);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return coordinatesNearestPois;
	}

	/**
	 * Método que comprueba si un determinado nodo para un medio de tranporte
	 * concreto ha presentado alguna inconsistencia (se encuentra almacenado en
	 * la tabla unconnectedPois). En caso de no estar, se inserta en la tabla y
	 * en caso que ese nodo ya haya sido inconexo para el otro medio de
	 * transporte se elimina de la base de datos.
	 * 
	 * @param node_id
	 *            id del nodo que se va a comprobar
	 * @param transportation
	 *            medio de transporte para el que se hace la comprobación
	 * @throws DatabaseException
	 * 
	 */
	public void checkIfUnconnectedPoiExists(Long node_id, String transportation) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT * FROM UNCONNECTED_POIS WHERE poi_ID=? AND transportation=?";
			st = con.prepareStatement(sql);
			st.setLong(1, node_id);
			st.setString(2, transportation);
			rs = st.executeQuery();
			if (rs.next()) {
				if (rs.getString("transportation").compareTo(transportation) != 0) {
					System.err.println("Punto de inter�s conflictivo. Id_"
							+ node_id);
					// removeUnconnectedPoiFromDatabase(node_id);
				}
			} else {
				if (node_id != -1) {
					insertUnconnectedPoi(node_id, transportation);
				}
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
	}

	/**
	 * Método que elimina un punto de interés o nodo de la base de datos.
	 * 
	 * @param node_id
	 *            id del nodo que se va a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void removeUnconnectedPoiFromDatabase(Long node_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		char type = ' ';
		long poi_id = -1;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT * FROM poi_data WHERE ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, node_id);
			rs = st.executeQuery();
			if (rs.next()) {
				type = rs.getString("type").charAt(0);
				poi_id = rs.getLong("poi_id");
			}
			if (type == 'N') {
				deletePoiFromNodesAndNodesTags(poi_id);
			} else {
				deletePoiFromWaysAndWayTags(poi_id);
			}
			deletePoiFromPoiData(node_id);

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}

	}

	/**
	 * Método que registra la votación de un usuario en un determinado punto de
	 * interés.
	 * 
	 * @param id
	 *            id del punto de interés que va a votarse
	 * @param user_rating
	 *            interés asociado al punto de interés
	 * @throws DatabaseException
	 * 
	 */
	public void registerVotation(Long id, Integer user_rating) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		Integer votes_number;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT count(*) as votes_number FROM visited_pois WHERE POI_ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				votes_number = Integer.valueOf(rs.getString("votes_number"));
				registerVote(id, user_rating, votes_number);
			}
			// registrar votación user
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
	}

	/**
	 * Método que marca un punto de interés como "visitado" para un determinado
	 * usuario.
	 * 
	 * @param id
	 *            id del punto de interés que va a registrarse
	 * @param user_rating
	 *            interés asociado al punto de interés
	 * @param email
	 *            email del usuario para el que se registra el punto
	 * @return si el punto ya ha sido visitado anteriormente
	 * @throws DatabaseException
	 * 
	 */
	public boolean registerVisitedPoi(Long id, Integer user_rating,
			String email, String opinion) {
		Connection con = null;
		PreparedStatement st = null;
		boolean visited = false;
		try {
			con = ds.getConnection();
			Integer user_id;
			String sql;
			user_id = getUserIdBy(email);
			if (!checkIfPoiIsAlreadyVisited(user_id, id)) {
				if (opinion.compareTo("") != 0) {
					sql = "INSERT INTO VISITED_POIS(register_user_id, poi_id, score_submitted, opinion) VALUES(?,?,?,?)";
					st = con.prepareStatement(sql);
					st.setInt(1, user_id);
					st.setLong(2, id);
					st.setInt(3, user_rating);
					st.setString(4, opinion);
				} else {
					sql = "INSERT INTO VISITED_POIS(register_user_id, poi_id, score_submitted) VALUES(?,?,?)";
					st = con.prepareStatement(sql);
					st.setInt(1, user_id);
					st.setLong(2, id);
					st.setInt(3, user_rating);
				}
				st.executeUpdate();
			} else {
				visited = true;
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
		return visited;
	}

	/**
	 * Método que comprueba si un punto de interés ya ha sido visitado por un
	 * usuario.
	 * 
	 * @param id
	 *            id del punto de interés que va a comprobarse
	 * @param user_id
	 *            id del usuario
	 * @return si el punto ya ha sido visitado anteriormente
	 * @throws DatabaseException
	 * 
	 */
	public boolean checkIfPoiIsAlreadyVisited(Integer user_id, Long id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		Boolean visited = false;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT * FROM visited_pois WHERE register_user_id=? AND poi_id=?";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			st.setLong(2, id);
			rs = st.executeQuery();
			if (rs.next()) {
				visited = true;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return visited;
	}

	/**
	 * Método que obtiene el id de un usuario a partir del email con el que está
	 * registrado.
	 * 
	 * @param email
	 *            email del usuario
	 * @return id del usuario o -1 si no está registrado
	 * @throws DatabaseException
	 * 
	 */
	public Integer getUserIdBy(String email) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		Integer user_id = -1;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT id FROM registered_users WHERE email=?";
			st = con.prepareStatement(sql);
			st.setString(1, email);
			rs = st.executeQuery();
			if (rs.next()) {
				user_id = rs.getInt("id");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return user_id;
	}

	/**
	 * Método que registra un voto en un determinado punto de interés.
	 * 
	 * @param id
	 *            id del punto de interés
	 * @param user_rating
	 *            interés o rating para ese punto
	 * @param votes_number
	 *            número de votos que tiene registrado ese punto
	 * @throws DatabaseException
	 * 
	 */
	public void registerVote(Long id, Integer user_rating, Integer votes_number) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		Double score;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT score FROM poi_data WHERE id=?";
			st = con.prepareStatement(sql);
			st.setLong(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				score = Double.valueOf(rs.getString("score"));
				score = ((score * (votes_number + 1)) + user_rating)
						/ (votes_number + 2);
				updateScore(id, score);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
	}

	/**
	 * Método que actualiza el score ascociado a un determinado punto de
	 * interés.
	 * 
	 * @param id
	 *            id del punto de interés
	 * @param score
	 *            interés o score para ese punto
	 * @throws DatabaseException
	 * 
	 */
	public void updateScore(Long id, Double score) {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "UPDATE POI_DATA SET SCORE=? WHERE ID=?";
			st = con.prepareStatement(sql);
			st.setDouble(1, score);
			st.setLong(2, id);
			st.executeUpdate();

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
	}

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id(tabla poi_data).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromPoiData(Long node_id) {
		Connection con = null;
		PreparedStatement st = null;

		try {
			con = ds.getConnection();
			String sql;
			sql = "DELETE FROM POI_DATA WHERE ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, node_id);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
	}

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id (tablas ways y way_tags).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromWaysAndWayTags(Long poi_id) {
		Connection con = null;
		PreparedStatement st = null;

		try {
			con = ds.getConnection();
			String sql;
			sql = "DELETE FROM WAYS WHERE ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			st.executeUpdate();
			sql = "DELETE FROM WAY_TAGS WHERE WAY_ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}

	}

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id (tablas nodes y node_tags).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromNodesAndNodesTags(Long poi_id) {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "DELETE FROM NODES WHERE ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			st.executeUpdate();
			sql = "DELETE FROM NODE_TAGS WHERE NODE_ID=?";
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}

	}

	/**
	 * Método que registra un nodo inconexo en la base de datos.
	 * 
	 * @param node_id
	 *            id del nodo que se va a insertar
	 * @param transportation
	 *            medio de transporte para el que ha presentado una
	 *            inconsistencia
	 * @throws DatabaseException
	 * 
	 */
	public void insertUnconnectedPoi(Long node_id, String transportation) {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "INSERT INTO UNCONNECTED_POIS VALUES(?,?)";
			st = con.prepareStatement(sql);
			st.setLong(1, node_id);
			st.setString(2, transportation);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
	}

	/**
	 * Método que comprueba si una ciudad está registrada en la BBDD.
	 * 
	 * @param city
	 *            nombre de la ciudad
	 * @return los datos de la ciudad. Null si no existe
	 * @throws DatabaseException
	 * 
	 */
	public CityResponse checkIfCityExists(String city) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		CityResponse city_params = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT ST_ASTEXT(THE_GEOM) as the_geom, radius,city_name "
					+ "FROM cities WHERE city_name=?";
			st = con.prepareStatement(sql);
			st.setString(1, city);
			rs = st.executeQuery();
			if (rs.next()) {
				city_params = new CityResponse();
				city_params.setCoordinates(rs.getString("the_geom"));
				city_params.setRadius(rs.getDouble("radius"));
				city_params.setCity_name(rs.getString("city_name"));
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return city_params;
	}

	/**
	 * Método que comprueba si un hotel está registrado en la BBDD.
	 * 
	 * @param hotel_name
	 *            nombre del hotel
	 * @param city_params
	 *            datos de la ciudad
	 * @param equal
	 *            si se ha de buscar la coincidencia completa o no.
	 * @return los datos del hotel. Null si no existe
	 * @throws DatabaseException
	 * 
	 */
	public HotelsResponse checkIfHotelExists(String hotel_name,
			CityResponse city_params, boolean equal) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		HotelsResponse response = null;
		ArrayList<String> hotelList = null;
		String likeOrEqual = "";
		if (equal) {
			likeOrEqual = "= UPPER('" + hotel_name + "')";
		} else {
			likeOrEqual = "LIKE UPPER('%" + hotel_name + "%')";
		}
		try {
			con = ds.getConnection();
			hotelList = new ArrayList<String>();
			response = new HotelsResponse();
			String sql;
			if (city_params != null) {
				sql = "select v, geom from( select node_id, st_astext(geom) as geom "
						+ "from node_tags,nodes where "
						+ "id=node_id and (v='hotel' or v='motel' or v='hostel')"
						+ " AND ST_DWithin(geom,'SRID=4326;"
						+ city_params.getCoordinates()
						+ "'"
						+ "::geography,?)"
						+ ")k, node_tags where k='name' and "
						+ "k.node_id=node_tags.node_id and UPPER(v)"
						+ likeOrEqual;
				st = con.prepareStatement(sql);
				st.setDouble(1, city_params.getRadius());
				rs = st.executeQuery();
				while (rs.next()) {
					hotelList.add(rs.getString("v"));
					if (equal) {
						response.setCoordinates(rs.getString("geom"));
					}
				}
				response.setHotelList(hotelList);
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		if (hotelList.size() == 0) {
			response.getHotelList().add("");
		}
		return response;
	}

	/**
	 * Método que comprueba si un usuario está registrado en la BBDD.
	 * 
	 * @param email
	 *            email del usuario
	 * @return contraseña del usuario
	 * @throws DatabaseException
	 * 
	 */
	public String checkIfUserIsRegistered(String email, String password) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "select * from registered_users where email=?";
			st = con.prepareStatement(sql);
			st.setString(1, email);
			rs = st.executeQuery();
			if (rs.next()) {
				if (BCrypt.checkpw(password, rs.getString("password"))) {
					return rs.getString("name");
				} else {
					return "wrong_password";
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return "";
	}

	/**
	 * Método que registra un nuevo usuario en la base de datos.
	 * 
	 * @param email
	 *            email del usuario
	 * @param hashedPassword
	 *            contraseña hasheada del usuario
	 * @throws DatabaseException
	 * 
	 */
	public void registerUser(String email, String hashedPassword, String name) {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			hashedPassword = hashedPassword.replace("'", "''");
			sql = "INSERT INTO registered_users(email, password, name) VALUES(?,?,?)";
			st = con.prepareStatement(sql);
			st.setString(1, email);
			st.setString(2, hashedPassword);
			st.setString(3, name);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}

	}

	/**
	 * Método obtiene todos los puntos no conectados para un determinado punto
	 * de transporte.
	 * 
	 * @param transportation
	 *            medio de transporte
	 * @return lista con los ids de los puntos de interés inconexos
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<Long> getUnconnectedPoiIds(String transportation) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		ArrayList<Long> poiIds = null;
		try {
			con = ds.getConnection();
			poiIds = new ArrayList<Long>();
			String sql;
			sql = "SELECT poi_id FROM UNCONNECTED_POIS WHERE transportation=?";
			st = con.prepareStatement(sql);
			st.setString(1, transportation);
			rs = st.executeQuery();
			while (rs.next()) {
				poiIds.add(rs.getLong("poi_id"));
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return poiIds;
	}

	/**
	 * Método que obtiene el número de puntos de interés visitados por un
	 * usuario.
	 * 
	 * @param user_id
	 *            id del usuario
	 * @return número de puntos de interés visitados
	 * @throws DatabaseException
	 * 
	 */
	public int getVisitedPoisCount(Integer user_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		int count = 0;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT count(id) as count FROM visited_POIS WHERE register_user_id=?";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt("count");
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return count;
	}

	/**
	 * Método que obtiene los puntos de interés visitados por un usuario
	 * ordenados por interés.
	 * 
	 * @param user_id
	 *            id del usuario
	 * @return lista de los puntos de interés priorizados
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<Poi> getVisitedPoisPriorized(Integer user_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		ArrayList<Poi> pois = null;
		try {
			con = ds.getConnection();
			pois = new ArrayList<Poi>();
			String sql;
			sql = "select poi_data.poi_id as poi_id, score_submitted, type from visited_pois, poi_data where register_user_id=? AND visited_pois.poi_id=poi_data.id order by score_submitted desc";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			rs = st.executeQuery();
			while (rs.next()) {
				Poi poi = new Poi();
				poi.setPoi_id(rs.getLong("poi_id"));
				poi.setScore(rs.getInt("score_submitted"));
				poi.setTag(rs.getString("type"));
				pois.add(poi);
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return pois;
	}

	/**
	 * Método que obtiene el nombre de un lugar.
	 * 
	 * @param poi_id
	 *            id del punto de interés
	 * @param user_activity
	 *            estructura para almacenar la información del punto de interés
	 * @param type
	 *            tipo de lugar (Node o Way)
	 * @throws DatabaseException
	 * 
	 */
	public void getPlaceNameAccordingToType(Long poi_id,
			UserActivity user_activity, String type) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		Category category;
		try {
			con = ds.getConnection();
			String sql;
			if (type.compareTo("W") == 0) {
				sql = " select * from ways, way_tags where id=way_id AND id=?";
			} else {
				sql = " select * from nodes, node_tags where id=node_id AND id=?";
			}
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			rs = st.executeQuery();
			while (rs.next()) {
				if (rs.getString("k").compareTo("name") == 0) {
					user_activity.setPlace_name(rs.getString("v"));
				} else {
					category = getCategoryFromTagName(rs.getString("v"));
					if (category != null) {
						user_activity.setCategory(category.toString());
						user_activity.setTag(rs.getString("v"));
					}
				}
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
	}

	/**
	 * Método que devuelva las experiencias de un determinado punto de interés.
	 * 
	 * @param poi_id
	 *            id del punto de interés
	 * @return listado de experiencias para ese punto de interés
	 * @throws DatabaseException
	 * 
	 */
	public List<PoiDetailsResponse> getPoiExperiences(Long poi_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		ArrayList<PoiDetailsResponse> poiDetails = null;
		try {
			poiDetails = new ArrayList<PoiDetailsResponse>();
			con = ds.getConnection();
			String sql;
			sql = "select score_submitted, opinion, register_user_id from visited_pois where poi_id=?";
			st = con.prepareStatement(sql);
			st.setLong(1, poi_id);
			rs = st.executeQuery();
			while (rs.next()) {
				if (rs.getString("opinion") != null) {
					PoiDetailsResponse detail = new PoiDetailsResponse();
					detail.setOpinion(rs.getString("opinion"));
					detail.setScore_submitted(rs.getInt("score_submitted"));
					detail.setUser_name(getRegisteredUserNameById(rs
							.getInt("register_user_id")));
					poiDetails.add(detail);
				}
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return poiDetails;
	}

	/**
	 * Método devuelve el nombre de un determinado usuario registrado.
	 * 
	 * @param user_id
	 *            id del user
	 * @return nombre del usuario
	 * @throws DatabaseException
	 * 
	 */
	public String getRegisteredUserNameById(int user_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String sql, name = "";
		try {
			con = ds.getConnection();
			sql = "select name from registered_users where id=?";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			rs = st.executeQuery();
			if (rs.next()) {
				name = rs.getString("name");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return name;
	}
	
	/**
	 * M�todo que devuelve las coordenadas de una ruta.
	 * 
	 * @param routeName 
	 * 		Nombre de la ruta.
	 * @param user_id 
	 * 		ID del usuario.
	 * @return Coordenadas de la ruta.
	 * @throws DatabaseException.
	 * 
	 * */
	public String getRouteCoordinates(String routeName, int user_id){
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sql;
		String coordinates = "";
		try {
			con = ds.getConnection();
			sql = "SELECT coordinates FROM routes WHERE register_user_id=? AND name=?";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			st.setString(2, routeName);
			rs = st.executeQuery();
			if (rs.next()) {
				coordinates = rs.getString("coordinates");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return coordinates;
		
	}
	
	/**
	 * M�todo que devuelve todas las rutas almacenadas por un usuario.
	 * 
	 * @param user_id 
	 * 		ID del usuario.
	 * @return Conjunto de rutas del usuario.
	 * @throws DatabaseException.
	 * 
	 * */
	public ArrayList<Route> getRoutesByUserId(int user_id) {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sql;
		ArrayList<Route> routesList;
		try {
			con = ds.getConnection();
			routesList = new ArrayList<Route>();
			sql = "SELECT name, city_id, score, date FROM routes WHERE register_user_id=?";
			st = con.prepareStatement(sql);
			st.setInt(1, user_id);
			rs = st.executeQuery();
			while (rs.next()) {
				Route route = new Route();
				route.setName(rs.getString("name"));
				route.setCity(getCityById(rs.getInt("city_id")));
				route.setRating(rs.getDouble("score"));
				route.setDate(rs.getString("date"));
				routesList.add(route);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return routesList;
	}
	
	/**
	 * M�todo que devuelve el nombre de una ciudad mediante su id.
	 * 
	 * @param routeName 
	 * 		Nombre de la ruta.
	 * @param city_id 
	 * 		ID de la ciudad.
	 * @return Nombre de la ciudad.
	 * @throws DatabaseException.
	 * 
	 * */
	public String getCityById(int city_id) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		String city = "";
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT city_name FROM cities WHERE id=?";
			st = con.prepareStatement(sql);
			st.setInt(1, city_id);
			rs = st.executeQuery();
			if (rs.next()) {
				city = rs.getString("city_name");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st, rs);
		}
		return city;
	}

	/**
	 * M�todo que permite introducir una ruta en la base de datos.
	 * 
	 * @param coordinates 
	 * 		Coordenadas de la ruta.
	 * @param user_id 
	 * 		ID del usuario.
	 * @param city_id 
	 * 		ID de la ciudad.
	 * @param routeName 
	 * 		Nombre de la ruta.
	 * @param score 
	 * 		Puntuaci�n media de los puntos de inter�s de la ruta.
	 * @param date 
	 * 		Fecha en la que se gener� la ruta.
	 * @throws DatabaseException.
	 * 
	 * */
	public void insertRoute(String coordinates, int user_id, int city_id,
			String routeName, double rating, String date){
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "INSERT INTO routes(coordinates, register_user_id, city_id, name, score, date) " +
					"VALUES(?,?,?,?,?,?)";
			st = con.prepareStatement(sql);
			st.setString(1, coordinates);
			st.setInt(2, user_id);
			st.setInt(3, city_id);
			st.setString(4, routeName);
			st.setDouble(5, rating);
			st.setString(6, date);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
	}
	
	/**
	 * M�todo que permite eliminar una ruta de la base de datos.
	 * 
	 * @param routeName
	 * 		Nombre de la ruta.
	 * @throws DatabaseException.
	 * 
	 * */
	public void deleteRoute(String routeName){
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "DELETE FROM routes WHERE name=?";
			st = con.prepareStatement(sql);
			st.setString(1, routeName);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
	}
	
	/**
	 * M�todo que devuelve la ciudad en la que se encuentra un punto determinado 
	 * pasado c�mo par�metro.
	 * 
	 * @param point
	 * 		Punto a localizar.
	 * @return CityResponse con el id y nombre de la ciudad d�nde se encuentra el punto.
	 * @throws DatabaseException.
	 * 
	 * */
	public CityResponse getCityByLocation(String point){
		CityResponse cityResponse = new CityResponse();
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = ds.getConnection();
			String sql;
			sql = "SELECT ST_AsText(THE_GEOM) AS the_geom, city_name, id, radius "
					+ "FROM cities WHERE ST_DWithin(the_geom,'SRID=4326;"
					+ point
					+ "'"
					+ "::geography,radius)";
			st = con.prepareStatement(sql);
			rs = st.executeQuery();
			if (rs.next()) {
				cityResponse.setCity_name(rs.getString("city_name"));
				cityResponse.setId(rs.getInt("id"));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DatabaseUtil.close(con, st);
		}
		
		return cityResponse;
	}
	

}

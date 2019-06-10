package database;

import java.util.ArrayList;
import java.util.List;

import resource.request.Request;
import resource.response.CityResponse;
import resource.response.HotelsResponse;
import resource.response.PoiDetailsResponse;
import resource.response.UserActivity;
import model.Path;
import model.Poi;
import model.Poi.Category;

/**
 * Interfaz que contiene todos los métodos que pueden ser aplicados sobre la
 * base de datos en caso de implementarla.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public interface InterfaceDatabase {

	/**
	 * Metodo que devuelve las coordenadas de un determinado nodo (a partir de
	 * su id).
	 * 
	 * @return coordenadas del nodo
	 * @throws DatabaseException
	 * 
	 */
	public String coordinatesFromNodeId(Long node_id) throws DatabaseException;

	/**
	 * Metodo que devuelve una lista de puntos de interes cercanos a una
	 * determinada ubicación, en un radio determinado.
	 * 
	 * @param point
	 *            punto sobre el que se buscan puntos de interes cercanos
	 * @param radius
	 *            radio de busqueda
	 * @return lista de puntos de interés cercanos
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<Poi> getNearestNodes(String point, double radius,
			Request request) throws DatabaseException;

	/**
	 * Método que cambia la variable que actúa como prefijo de las tablas que
	 * contienen las rutas en la BBDD (fo_ : a pie; dr_ : en coche).
	 * 
	 * 
	 */
	public void changePrefixTable(String prefix);

	/**
	 * Método que calcula las coordenadas de la ruta resultante. Para ello habrá
	 * que ir uniendo los diferentes sub-tramos o arcos que la componen.
	 * 
	 * @param path
	 *            ruta final calculada
	 * @return string que contiene todas las coordenadas que componen la ruta
	 * 
	 */
	public ArrayList<String> obtainResultingCoordinates(Path path,
			ArrayList<ArrayList<Integer>> setOfTableIds);

	/**
	 * Método que calcula el score asociado a un determiando POI.
	 * 
	 * @param poi
	 *            punto sobre el que se va a calcular el score
	 * @param request
	 *            petición del cliente
	 * 
	 */
	public void calculateNodeAsociatedScore(Poi poi, Request request,
			boolean itineraryExpress);

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
	public int getScorePreferenceNodes(Poi poi, Request request);

	/**
	 * Método que comprueba si un determinado POI está promocionado, y si es así
	 * devuelve el score asociado.
	 * 
	 * @param poi
	 *            que se evalúa
	 * 
	 * @return score asociado
	 */
	public int getScorePromotedNodes(Poi poi);

	/**
	 * Método que devuelve el valor asociado a los POIs no promocionados.
	 * 
	 * @param poi
	 *            que se evalúa
	 * @return score asociado
	 * 
	 */
	public int getNoPromotedScore();

	/**
	 * Método que devuelve el score asociado al tag al que pertenece un
	 * determinado POI.
	 * 
	 * @param poi
	 *            que se evalúa
	 * @return score asociado
	 * 
	 */
	public int calculateScoreTaggedNodes(Poi poi);

	/**
	 * Método que devuelve la categoría a la que pertenece un determinado tag o
	 * etiqueta.
	 * 
	 * @param tagName
	 *            etiqueta o tag que se evalúa
	 * @return categoría a la que pertenece el tag
	 */
	public Category getCategoryFromTagName(String tagName);

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
	public int getScoreAndTimeToStayAsociatedToATag(String tagName, Poi poi);

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
	public String getSetOfTags(int category, List<String> excludedTags);

	/**
	 * Método que crea el contenido de la claúsula WHERE para la consulta de los
	 * nodos o POIs cercanos.
	 * 
	 * @param request
	 *            petición del usuario
	 * @return contenido de la claúsula WHERE
	 */
	public String createWhereClause(Request request);

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
			ArrayList<Integer> setOfIds);


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
			String middle_coordinates);

	/**
	 * Método que devuelve un listado con las ciudades que cominezan por el
	 * prefijo recibido como parámetro.
	 * 
	 * @param prefix
	 *            prefijo
	 * @return listado de ciudades que comienzan por el prefijo
	 * @throws DatabaseException
	 * 
	 */
	public ArrayList<String> getCitiesByPrefix(String prefix);

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
	public ArrayList<Poi> getNearestPoiFromCoordinates(String coordinates);

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
	public void checkIfUnconnectedPoiExists(Long node_id, String transportation);

	/**
	 * Método que elimina un punto de interés o nodo de la base de datos.
	 * 
	 * @param node_id
	 *            id del nodo que se va a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void removeUnconnectedPoiFromDatabase(Long node_id);

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
	public void insertUnconnectedPoi(Long node_id, String transportation);

	/**
	 * Método que comprueba si una ciudad está registrada en la BBDD.
	 * 
	 * @param city
	 *            nombre de la ciudad
	 * @return los datos de la ciudad. Null si no existe
	 * @throws DatabaseException
	 * 
	 */
	public CityResponse checkIfCityExists(String city);

	/**
	 * Método que comprueba si un hotel está registrado en la BBDD.
	 * 
	 * @param hotel_name
	 *            nombre del hotel
	 * @param city_params
	 *            datos de la ciudad
	 * @param equal
	 *            si se ha de buscar la coincidencia completa o no
	 * @return los datos del hotel. Null si no existe
	 * @throws DatabaseException
	 * 
	 */
	public HotelsResponse checkIfHotelExists(String hotel_name,
			resource.response.CityResponse city_params, boolean equal);

	/**
	 * Método que comprueba si un usuario está registrado en la BBDD.
	 * 
	 * @param email
	 *            email del usuario
	 * @return contraseña del usuario
	 * @throws DatabaseException
	 * 
	 */
	public String checkIfUserIsRegistered(String email, String password);

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
	public void registerUser(String email, String hashedPassword, String name);

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
	public void registerVotation(Long id, Integer user_rating);

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
			String email, String opinion);

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
	public boolean checkIfPoiIsAlreadyVisited(Integer user_id, Long id);

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
	public Integer getUserIdBy(String email);

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
	public void registerVote(Long id, Integer user_rating, Integer votes_number);

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
	public void updateScore(Long id, Double score);

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id(tabla poi_data).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromPoiData(Long node_id);

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id (tablas ways y way_tags).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromWaysAndWayTags(Long poi_id);

	/**
	 * Método que elimina un punto de interés de la base de datos a partir de su
	 * id (tablas nodes y node_tags).
	 * 
	 * @param id
	 *            id del punto de interés a eliminar
	 * @throws DatabaseException
	 * 
	 */
	public void deletePoiFromNodesAndNodesTags(Long poi_id);

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
	public ArrayList<Long> getUnconnectedPoiIds(String transportation);

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
	public int getVisitedPoisCount(Integer user_id);

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
	public ArrayList<Poi> getVisitedPoisPriorized(Integer user_id);

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
			UserActivity user_activity, String type);

	/**
	 * Método que devuelva las experiencias de un determinado punto de interés.
	 * 
	 * @param poi_id
	 *            id del punto de interés
	 * @return listado de experiencias para ese punto de interés
	 * @throws DatabaseException
	 * 
	 */
	public List<PoiDetailsResponse> getPoiExperiences(Long poi_id);

	/**
	 * Método devuelve el nombre de un determinado usuario registrado.
	 * 
	 * @param user_id
	 *            id del user
	 * @return nombre del usuario
	 * @throws DatabaseException
	 * 
	 */
	public String getRegisteredUserNameById(int user_id);

}

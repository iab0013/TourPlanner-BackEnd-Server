package router;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import model.Matrix;
import model.Path;
import model.Poi;
import util.Misc;
import database.DatabaseException;
import database.DatabaseFacade;
import de.cm.osm2po.routing.DefaultRouter;
import de.cm.osm2po.routing.Graph;
import de.cm.osm2po.routing.MultiTargetRouter;
import de.cm.osm2po.routing.PoiRouter;
import de.cm.osm2po.routing.RoutingResultSegment;
import de.cm.osm2po.routing.SinglePathRouter;

/**
 * Clase RouteProvider que contiene métodos y atributos relativos al cálculo de
 * rutas.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class RouteProvider {

	private static RouteProvider routeProvider;
	private Graph graphPedestrian;
	private Graph graphCar;
	private Properties paramsPedestrian;
	private Properties paramsCar;
	private final String MSG_ERROR_UNCONNECTED_POIS = "Existen algunos puntos de interés inconexos. No ha podido generarse correctamente la ruta";
	private ArrayList<Poi> problematicalPois;

	/**
	 * Constructor privado de la clase RouteProvider(patrón Singleton).
	 * 
	 */
	private RouteProvider() {
		File graphFilePedestrian = null;
		File graphFileCar = null;
		String path = System.getProperty("user.dir") + "/../applications/osm_server/graphs";
		graphFilePedestrian = new File(path + "/fo_2po.gph");
		paramsPedestrian = getRouteParams("fo_");
		graphFileCar = new File(path + "/dr_2po.gph");
		paramsCar = getRouteParams("dr_");
		graphPedestrian = new Graph(graphFilePedestrian);
		graphCar = new Graph(graphFileCar);
		problematicalPois = new ArrayList<Poi>();
	}

	/**
	 * Método para obtener el archivo .graph en función del medio de transporte.
	 * 
	 * @param transportation medio de transporte
	 * @return archivo graph.
	 * 
	 */
	public Graph getGraph(String transportation) {
		if (transportation.compareTo("fo_") == 0) {
			return graphPedestrian;
		} else {
			return graphCar;
		}
	}

	/**
	 * Método "getInstance" correspondiente al patrón Singleton, para manejar
	 * únicamente una instancia de la clase.
	 * 
	 * @return la instancia de la clase
	 */
	public static RouteProvider getInstance() {
		if (routeProvider == null) {
			routeProvider = new RouteProvider();
		}
		return routeProvider;
	}

	/**
	 * Método que cálcula la matriz de distancias para un conjunto de puntos de
	 * interés determinado.
	 * 
	 * @param setOfPois
	 *            conjunto de puntos de interés sobre los que se calcula la
	 *            matriz de distancias
	 * @param transportation
	 *            medio de transporte
	 * 
	 * @return matriz de distancias
	 * @throws DatabaseException
	 * @throws RouterProviderException
	 */
	public Matrix getDistanceMatrix(ArrayList<Poi> setOfPois,
			String transportation, double routeTime)
			throws RouterProviderException, DatabaseException {
		DatabaseFacade db;
		boolean sourceUnconnected;
		Graph graph;
		boolean unconnectedSourceOrTarget = false;
		boolean nearestPoisExtracted = false;
		Matrix matrix = new Matrix();
		ArrayList<Poi> coordinatesNearestPoisArrayList = null;
		if (transportation.compareTo("fo_") == 0) {
			graph = graphPedestrian;
		} else {
			graph = graphCar;
		}
		getVertices(setOfPois, graph);
		matrix.setSource(setOfPois.get(0));
		matrix.setTarget(setOfPois.get(1));
		for (int j = 0; j < setOfPois.size(); j++) {
			ArrayList<Poi> otherPOIs = getOtherPOIs(j, setOfPois);
			sourceUnconnected = checkIfExistsCostFromOneVerticeToOthers(
					otherPOIs, setOfPois.get(j), matrix, transportation,
					routeTime);
			if (sourceUnconnected && (j == 0 || j == 1)) {

				db = DatabaseFacade.getInstance();

				if (j == 1 && !nearestPoisExtracted) {
					unconnectedSourceOrTarget = false;
					nearestPoisExtracted = true;
				}
				if (!unconnectedSourceOrTarget) {
					coordinatesNearestPoisArrayList = db
							.getNearestPoiFromCoordinates(setOfPois.get(j)
									.getCoordinates());
					unconnectedSourceOrTarget = true;
				}
				if (coordinatesNearestPoisArrayList.size() > 0) {
					setOfPois.get(j).setCoordinates(
							coordinatesNearestPoisArrayList.get(0)
									.getCoordinates());
				} else {
					throw new RouterProviderException(
							MSG_ERROR_UNCONNECTED_POIS);
				}
				String[] coordinateArray = Misc
						.extractCoordinates(setOfPois.get(j).getCoordinates())
						.get(0).split(" ");
				setOfPois.get(j).setVertex(
						graph.findClosestVertexId(
								Float.parseFloat(coordinateArray[1]),
								Float.parseFloat(coordinateArray[0])));
				if (coordinatesNearestPoisArrayList.size() > 0) {
					coordinatesNearestPoisArrayList.remove(0);
				}
				j = -1;
			}
		}
		RouteProvider.getInstance().repairInconsistenciesMatrix(matrix,
				transportation);
		return matrix;

	}

	/**
	 * Método que comprueba si existe coste de un vertice a todos los demás, y
	 * lo añade a la matriz en caso afirmativo.
	 * 
	 * @param otherPOIs
	 *            array que contiene todos todos los POIs excepto el sourcePoi
	 * @param sourcePoi
	 *            POI de origen
	 * @param matrix
	 *            matriz de distancias
	 * @param graph
	 *            previamente generado con la herramienta os2mpo que contiene la
	 *            información geográfica correspondiente
	 * 
	 * @return si el proceso ha sido satisfactorio
	 */
	public boolean checkIfExistsCostFromOneVerticeToOthers(
			ArrayList<Poi> otherPOIs, Poi sourcePoi, Matrix matrix,
			String transportation, double radius) {
		MultiTargetRouter router = new PoiRouter();
		ArrayList<Poi> unconnectedPois = null;
		Graph graph = null;
		boolean existsCost;
		Properties params;
		int[] otherVerticesArray = getVertexAsArray(otherPOIs);
		if (transportation.compareTo("fo_") == 0) {
			graph = graphPedestrian;
			params = paramsPedestrian;
		} else {
			params = paramsCar;
			graph = graphCar;
		}
		router.traverse(graph, sourcePoi.getVertex(), otherVerticesArray,
				(float) (radius), params);
		int numTimesnotInserted = 0;
		for (Poi poi : otherPOIs) {
			if (router.isVisited(poi.getVertex())) {
				matrix.addPoi(sourcePoi, poi, router.getCost(poi.getVertex())
						+ ((double) sourcePoi.getTime_to_stay() / 60) / 2
						+ ((double) poi.getTime_to_stay() / 60) / 2);
			} else {
				if (unconnectedPois == null) {
					unconnectedPois = new ArrayList<Poi>();
				}
				unconnectedPois.add(poi);
				numTimesnotInserted++;
			}
		}
		if (numTimesnotInserted > (otherPOIs.size() * 0.5)) {
			if (!problematicalPois.contains(sourcePoi)
					&& sourcePoi.getPoi_id() != -1) {
				problematicalPois.add(sourcePoi);
			}
			existsCost = true;
		} else {
			if (unconnectedPois != null) {
				for (Poi unconnectedPoi : unconnectedPois) {
					if (!problematicalPois.contains(unconnectedPoi)
							&& unconnectedPoi.getPoi_id() != -1) {
						problematicalPois.add(unconnectedPoi);
					}
				}
			}
			existsCost = false;
		}
		router.reset();
		return existsCost;
	}

	/**
	 * Método que obtiene los ids asociados a los tramos correspondientes.
	 * 
	 * @param path
	 *            camino resultante de la ejecución del algoritmo de rutas
	 * @param costs
	 *            array de costes para cada segmento de la ruta (se rellena
	 *            dentro del método)
	 * @return listado con los ids asociados a cada tramo
	 * @throws RouterProviderException
	 */
	public ArrayList<ArrayList<Integer>> getArrayOfTableIds(Path path,
			ArrayList<Float> costs, String transportation)
			throws RouterProviderException {
		SinglePathRouter router = new DefaultRouter();
		Graph graph = null;
		Properties params;
		ArrayList<ArrayList<Integer>> setOfTableIds = new ArrayList<ArrayList<Integer>>();
		if (transportation.compareTo("fo_") == 0) {
			graph = graphPedestrian;
			params = getRouteParamsForConstructPath("fo_");
		} else {
			params = getRouteParamsForConstructPath("dr_");
			graph = graphCar;
		}
		float edgeCost = 0F;
		costs.add(edgeCost);
		for (int i = 1; i < path.size(); i++) {
			int[] setOfpaths = router.findPath(graph, path.get(i - 1)
					.getVertex(), path.get(i).getVertex(), Float.MAX_VALUE,
					params);
			edgeCost = 0F;
			if (setOfpaths != null) { // Found!
				ArrayList<Integer> segmentIds = new ArrayList<Integer>();
				for (int j = 0; j < setOfpaths.length; j++) {
					RoutingResultSegment rrs = graph
							.lookupSegment(setOfpaths[j]);
					int segId = rrs.getId();
					segmentIds.add(segId);
					float cost = rrs.getH();
					edgeCost += cost;
				}
				costs.add(edgeCost
						+ ((float) path.get(i - 1).getTime_to_stay() / 60));
				setOfTableIds.add(segmentIds);
			} else {
				throw new RouterProviderException(
						"Origen y destino no conectan");
			}
			router.reset();
		}
		return setOfTableIds;

	}

	/**
	 * Método que repara las inconsistencias que aparezcan en la generación la
	 * matriz.
	 * 
	 * @param matrix
	 *            estructura que almacena la matriz de distancias
	 * @param transport
	 *            transporte para el que se van a reparar las inconsistencias
	 * 
	 * @return lista con todos los valores que estaban almacenados en el
	 *         MultiKeyMap
	 * 
	 */
	public void repairInconsistenciesMatrix(Matrix matrix, String transport)
			throws DatabaseException {
		DatabaseFacade db;
		db = DatabaseFacade.getInstance();

		for (Poi poi : problematicalPois) {
			db.checkIfUnconnectedPoiExists(poi.getPoi_id(), transport);
			matrix.removeAllOcurrences(poi);
			matrix.repairInconsistencies(poi);
		}
	}

	/**
	 * Método que convierte un ArrayList de POIs en un array.
	 * 
	 * @param otherPOIs
	 *            conjunto de puntos de interés
	 * 
	 * @return array de puntos de interés
	 */
	private int[] getVertexAsArray(ArrayList<Poi> otherPOIs) {
		int[] vertices = new int[otherPOIs.size()];
		int i = 0;
		for (Poi poi : otherPOIs) {
			vertices[i] = poi.getVertex();
			i++;
		}
		return vertices;
	}

	/**
	 * Método que obtiene todos los puntos de interés, excepto el que se está
	 * evaluando actualmente.
	 * 
	 * @param currentIndexPoi
	 *            indice que se está evaluando
	 * @param setOfPois
	 *            listado de puntos de interés
	 * 
	 * @return listado con los ids asociados a cada tramo
	 */
	private ArrayList<Poi> getOtherPOIs(int currentIndexPoi,
			ArrayList<Poi> setOfPois) {
		ArrayList<Poi> otherPOIs = new ArrayList<Poi>(setOfPois.size() - 1);
		for (int i = 0; i < setOfPois.size(); i++) {
			if (currentIndexPoi != i) {
				otherPOIs.add(setOfPois.get(i));
			}
		}
		return otherPOIs;
	}

	/**
	 * Método que obtiene y establece los vértices más cercanos de un conjunto
	 * de puntos de interés.
	 * 
	 * @param setOfPois
	 *            conjunto de puntos de interés
	 * @param graph
	 *            previamente generado con la herramienta os2mpo que contiene la
	 *            información geográfica correspondiente
	 * 
	 * @return listado con los ids asociados a cada tramo
	 */
	public void getVertices(ArrayList<Poi> setOfPois, Graph graph) {
		int[] vertices = new int[setOfPois.size()];
		int cont = 0;
		for (Poi poi : setOfPois) {
			ArrayList<String> coordinates = Misc.extractCoordinates(poi
					.getCoordinates());
			String[] coordinateArray = coordinates.get(0).split(" ");
			vertices[cont] = graph.findClosestVertexId(
					Float.parseFloat(coordinateArray[1]),
					Float.parseFloat(coordinateArray[0]));
			poi.setVertex(vertices[cont]);
			cont++;
		}
	}

	/**
	 * Método que devuelve las propiedades que posteriormente se utilizarán en
	 * la reconstrucción de los caminos.
	 * 
	 * @param transport
	 *            medio de transporte
	 * 
	 * @return propiedades a utilizar
	 */
	private Properties getRouteParamsForConstructPath(String transport) {
		Properties params = null;
		if (transport.compareTo("fo_") == 0) {
			params = new Properties();
			params.setProperty("findShortestPath", "true");
			params.setProperty("ignoreRestrictions", "true");
			params.setProperty("ignoreOneWays", "true");
			params.setProperty("heuristicFactor", "1.0"); // 0.0 Dijkstra, 1.0
															// good
															// A*
		} else {
			params = new Properties();
			params.setProperty("findShortestPath", "true");
			params.setProperty("ignoreRestrictions", "false");
			params.setProperty("ignoreOneWays", "false");
			params.setProperty("heuristicFactor", "1.0"); // 0.0 Dijkstra, 1.0
															// good
															// A*
		}
		return params;
	}

	/**
	 * Método que devuelve las propiedades que posteriormente se utilizarán en
	 * la construcción de la matriz de distancias.
	 * 
	 * @param transport
	 *            medio de transporte
	 * 
	 * @return propiedades a utilizar
	 */
	private Properties getRouteParams(String transport) {
		Properties params = null;
		if (transport.compareTo("fo_") == 0) {
			params = new Properties();
			params.setProperty("findShortestPath", "false");
			params.setProperty("ignoreRestrictions", "true");
			params.setProperty("ignoreOneWays", "true");
			params.setProperty("heuristicFactor", "1.0"); 
		} else {
			params = new Properties();
			params.setProperty("findShortestPath", "false");
			params.setProperty("ignoreRestrictions", "false");
			params.setProperty("ignoreOneWays", "false");
			params.setProperty("heuristicFactor", "1.0"); 
		}
		return params;
	}

}

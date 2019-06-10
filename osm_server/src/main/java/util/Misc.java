package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import database.DatabaseException;
import database.DatabaseFacade;
import resource.request.Request;
import model.Poi;
import model.Poi.Category;

/**
 * Clase Misc que contiene métodos de diversa índole.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Misc {

	private final static double DEFAULT_SPEED_FOOT = 5.0;
	private final static double DEFAULT_SPEED_CAR = 40.0;
	private final static double MAX_RADIUS = 10000;

	/**
	 * Método que extrae una serie de coordenadas a partir de una cadena
	 * (String).
	 * 
	 * @param coordinates
	 *            cadena que contiene internamente las coordenadas a extraer
	 * @return lista de coordenadas en un ArrayList
	 * 
	 */
	public static ArrayList<String> extractCoordinates(String coordinates) {
		int endIndex = 0;
		ArrayList<String> ls = new ArrayList<String>();
		coordinates = coordinates.substring(coordinates.indexOf("(") + 1,
				coordinates.indexOf(")"));
		while (coordinates.indexOf(",") != -1) {
			endIndex = coordinates.indexOf(",");
			ls.add(coordinates.substring(0, endIndex));
			coordinates = coordinates.substring(endIndex + 1,
					coordinates.length());
		}
		ls.add(coordinates.substring(0, coordinates.length()));
		return ls;
	}

	/**
	 * Método que "formatea" las coordenadas de un determinado conjunto de arcos
	 * para su posterior envío al dispositivo.
	 * 
	 * @param setOfEdges
	 *            conjunto de tramos sobre los que quiere formatearse las
	 *            coordenadas
	 * @return string que contiene las coordenadas formateadas
	 */
	public static String formatReturningCoordinates(ArrayList<String> setOfEdges) {
		String routeCoordinates = "";
		for (String edgeCoordinates : setOfEdges) {
			for (String coordinates : Misc.extractCoordinates(edgeCoordinates)) {
				routeCoordinates += coordinates + " ";
			}
		}
		return routeCoordinates;
	}

	/**
	 * Método que hace que dos tramos consecutivos entre sí, tenga las
	 * coordenadas en el orden correcto, de modo que se eviten los problemas en
	 * la posterior visualización de la ruta en el dispositivo.
	 * 
	 * @param setOfEdges
	 *            conjunto de tramos posterior a los tramos contenidos en el
	 *            segundo parámetro de esta función
	 * @param previous_setOfEdges
	 *            conjunto de tramos anterior a los tramos contenidos en el
	 *            primer parámetro de esta función
	 * @return conjunto de tramos ordenado de tal forma que se eviten los
	 *         problemas a la hora de visualizar la ruta
	 */
	public static ArrayList<String> linkSetOfEdges(
			ArrayList<String> setOfEdges, ArrayList<String> previous_setOfEdges) {
		String lastLineString, lastCoordinatesPreviousEdge, firstLineString, firstCoordinatesCurrentEdge;
		ArrayList<String> setOfCoordinates, currentSetOfCoordinates;
		lastLineString = previous_setOfEdges
				.get(previous_setOfEdges.size() - 1);
		setOfCoordinates = extractCoordinates(lastLineString);
		lastCoordinatesPreviousEdge = setOfCoordinates.get(setOfCoordinates
				.size() - 1);
		firstLineString = setOfEdges.get(0);
		currentSetOfCoordinates = extractCoordinates(firstLineString);
		firstCoordinatesCurrentEdge = currentSetOfCoordinates.get(0);
		if (firstCoordinatesCurrentEdge.compareTo(lastCoordinatesPreviousEdge) != 0) {
			setOfEdges.add(0, lastLineString);
			setOfEdges = orderLineString(setOfEdges);
			setOfEdges.remove(lastLineString);
		}
		return setOfEdges;
	}

	/**
	 * Método que ordena un determinado conjunto de linestrings.
	 * 
	 * @param lineString
	 *            conjunto de tramos a ordenar
	 * @return arraylist con el conjunto de tramos ordenados
	 * 
	 */
	public static ArrayList<String> orderLineString(ArrayList<String> lineString) {
		ArrayList<String> currentEdge;
		ArrayList<String> previousEdge;
		ArrayList<String> orderedLinestring;
		orderedLinestring = new ArrayList<String>();
		String reversedLineString = "";
		orderedLinestring.add(lineString.get(0));
		String[] orderedLinestrings;
		for (int i = 1; i < lineString.size(); i++) {
			currentEdge = extractCoordinates(lineString.get(i));
			previousEdge = extractCoordinates(lineString.get(i - 1));
			if (currentEdge.get(0).compareTo(
					previousEdge.get(previousEdge.size() - 1)) != 0) {
				orderedLinestrings = currentEdge.toArray(new String[currentEdge
						.size()]);
				reversedLineString = getReversedEdge(orderedLinestrings);
				reversedLineString = "LINESTRING(" + reversedLineString + ")";
				orderedLinestring.add(reversedLineString);
				lineString.set(i, reversedLineString);
			} else {
				orderedLinestring.add(lineString.get(i));
			}
		}
		return orderedLinestring;
	}

	/**
	 * Método que invierte las coordenadas asociadas a un determinado tramo.
	 * 
	 * @param coordinates
	 *            array de coordenadas que quieren invertirse
	 * @return string que contiene las coordenadas invertidas
	 * 
	 */
	public static String getReversedEdge(String[] coordinates) {
		String reversedCoordinates = "";
		for (int i = coordinates.length - 1; i >= 0; i--) {
			if (i != 0) {
				reversedCoordinates += coordinates[i] + ",";
			} else {
				reversedCoordinates += coordinates[i];
			}
		}
		return reversedCoordinates;
	}

	/**
	 * Método que a partir de un par de coordenadas dadas, devuelve las
	 * coordenadas correspondientes al punto medio entre ambas.
	 * 
	 * @param coordinates_source
	 *            coordenadas origen
	 * @param coordinates_target
	 *            coordenadas destino
	 * @return string que contiene las coordenadas del punto medio
	 * 
	 */
	public static String obtainMiddleCoordinates(String coordinates_source,
			String coordinates_target) {
		String[] middle_coordinates = new String[2];
		String[] source_coordinates, target_coordinates;
		double offset_lat, offset_lon;
		source_coordinates = coordinates_source.split(" ");
		target_coordinates = coordinates_target.split(" ");
		offset_lat = (Double.valueOf(source_coordinates[0]) - Double
				.valueOf(target_coordinates[0])) / 2;
		middle_coordinates[0] = String.valueOf(Double
				.valueOf(source_coordinates[0]) - offset_lat);
		offset_lon = (Double.valueOf(source_coordinates[1]) - Double
				.valueOf(target_coordinates[1])) / 2;
		middle_coordinates[1] = String.valueOf(Double
				.valueOf(source_coordinates[1]) - offset_lon);
		return middle_coordinates[0] + " " + middle_coordinates[1];
	}

	/**
	 * Método que filtra los nodos en función de su score, de modo que se
	 * obtengan los POIs de mayor score. Además se filtrará también de acuerdo a
	 * una cota superio pre-establecida.
	 * 
	 * @param nearestNodesList
	 *            lista de nodos a filtrar
	 * 
	 */
	public static void filterPoiList(ArrayList<Poi> nearestNodesList) {
		PoiComparator<Poi> comparator = new PoiComparator<Poi>();
		Collections.sort(nearestNodesList, comparator);
		int size = nearestNodesList.size();
		for (int i = 0; i < (size - 100); i++) {
			nearestNodesList.remove(nearestNodesList.size() - 1);
		}

	}

	/**
	 * Método para generar números enteros aleatorios en un rango dado.
	 * 
	 * @param max
	 *            límite superior para el número entero generado
	 * @param min
	 *            límite inferior para el número entero generado
	 */
	public static int generateRandomNumber(int min, int max) {
		return (int) (min + Math.random() * (max - min));
	}

	/**
	 * Método que selecciona un punto de interés aleatorio de una lista y lo
	 * elimina.
	 * 
	 * @param max
	 *            límite superior para el número entero generado
	 * @param min
	 *            límite inferior para el número entero generado
	 */
	public static Poi selectRandomPoi(ArrayList<Poi> nearestPois) {
		return nearestPois.remove(generateRandomNumber(0,
				nearestPois.size() - 1));
	}

	/**
	 * Método que calcula el radio de búsqueda de puntos de interés cercanos en
	 * concordancia al tiempo disponible.
	 * 
	 * @param routeTime
	 *            tiempo disponible
	 * @param transport
	 *            medio de transporte para la ruta
	 * @return radio de búsqueda
	 * 
	 */
	public static double calculateSearchRadiusAccordingToTime(double routeTime,
			String transport) {
		double radius;
		if (transport.compareTo("fo_") == 0) {
			radius = DEFAULT_SPEED_FOOT * routeTime * 1000; // in meters
		} else {
			radius = DEFAULT_SPEED_CAR * routeTime * 1000;
		}
		if (radius > MAX_RADIUS) {
			radius = MAX_RADIUS;
		}
		return radius;
	}

	/**
	 * Método que obtiene un punto de interés de una categoría determinada.
	 * 
	 * @param nearestNodesList
	 *            listado de puntos de interés cercanos
	 * @param selected_category
	 *            categoría que debe tener el punto obtenido
	 * @return punto de interés de la categoría seleccionada
	 * 
	 */
	public static Poi obtainRandomPoiByCategory(
			ArrayList<Poi> nearestNodesList, String selected_category) {
		Category category;
		category = getCategoryFromString(selected_category);
		if (category != null) {
			for (Poi poi : nearestNodesList)
				if (poi.getCategory() != null) {
					if (poi.getCategory().compareTo(category) == 0) {
						return poi;
					}
				}
		}
		return null;
	}

	/**
	 * Devuelve un enumerado de tipo Category a partir de una cadena.
	 * 
	 * @param selected_category
	 *            categoría seleccionada
	 * @return enumerado de tipo Category
	 * 
	 */
	public static Category getCategoryFromString(String selected_category) {
		Category category = null;
		if (selected_category.compareTo("culture") == 0) {
			category = Category.CULTURE;
		} else if (selected_category.compareTo("gastronomy") == 0) {
			category = Category.GASTRONOMY;
		} else if (selected_category.compareTo("leisure") == 0) {
			category = Category.LEISURE;
		} else if (selected_category.compareTo("nature") == 0) {
			category = Category.NATURE;
		}
		return category;
	}

	/**
	 * Método que configura el inicio o el destino de una ruta determinada.
	 * 
	 * @param coordinates
	 *            coordenadas del punto
	 * @param nearestNodesList
	 *            lista de puntos de interés cercanos
	 * @param index
	 *            posición para el punto a añadir
	 * 
	 */
	public static void configureSourceOrTargetPoi(String coordinates,
			ArrayList<Poi> nearestNodesList, int index) {
		Poi poi = new Poi();
		poi.setCoordinates("POINT(" + coordinates + ")");
		poi.setPoi_id(-1L);
		nearestNodesList.add(index, poi);

	}

	/**
	 * Método que obtiene el punto medio entre un par de coordenadas.
	 * 
	 * @param request
	 *            objeto que contiene las coordenadas de inicio y de fin
	 * @return string que contiene las coordenadas del punto medio
	 * 
	 */
	public static String calculateMiddleCoordinates(Request request) {
		String middle_coordinates;
		middle_coordinates = Misc.obtainMiddleCoordinates(
				request.getSource_coordinates(),
				request.getTarget_coordinates());
		return middle_coordinates;
	}

	/**
	 * Método que convierte un ArrayList en un HashMap.
	 * 
	 * @param nearestNodesList
	 *            ArrayList a partir del cual va a generarse un HashMap
	 * @return HashMap<Long, Poi>
	 * 
	 */
	public static HashMap<Long, Poi> convertArrayListIntoHashMap(
			ArrayList<Poi> nearestNodesList) {
		HashMap<Long, Poi> poiMap = new HashMap<Long, Poi>();
		for (Poi poi : nearestNodesList) {
			poiMap.put(poi.getPoi_id(), poi);
		}
		return poiMap;
	}

	/**
	 * Método que elimina los puntos conflictivos de una lista.
	 * 
	 * @param nearestNodesList
	 *            listado de puntos cercanos
	 * @param transport
	 *            medio de transporte
	 * 
	 */
	public static void removeUnconnectedPoisFromResultList(
			ArrayList<Poi> nearestNodesList, String transport)
			throws DatabaseException {
		DatabaseFacade db = DatabaseFacade.getInstance();
		ArrayList<Long> poiIds;
		HashMap<Long, Poi> poiMap;
		poiIds = db.getUnconnectedPoiIds(transport);
		poiMap = Misc.convertArrayListIntoHashMap(nearestNodesList);
		for (Long poi_id : poiIds) {
			Poi poi = poiMap.get(poi_id);
			if (poi != null) {
				nearestNodesList.remove(poi);
			}
		}
	}

}

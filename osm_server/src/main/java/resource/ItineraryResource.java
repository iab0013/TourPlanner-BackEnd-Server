package resource;

import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import resource.request.Request;
import resource.response.RouteResponse;
import router.RouteProvider;
import router.RouterProviderException;
import util.Misc;
import algorithms.AlgorithmException;
import algorithms.ChaoMingHeuristic;
import algorithms.Grasp;
import algorithms.IAlgorithm;
import model.Matrix;
import model.Poi;
import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que se comunicará con el cliente recibiendo su localización y
 * preferencias y después mandándole a este la ruta generada para dicha
 * localización y preferencias.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/itinerary")
public class ItineraryResource {

	private final double DEFAULT_SEARCH_RADIUS_EXPRESS_ITINERARY = 2000;
	private final int ITERATION_LIMIT_SEARCHING_POIS = 10;
	private final int POI_COUNT_LOWER_THRESOLD = 15;
	private final int POI_COUNT_UPPER_THRESOLD = 100;
	private final double INCREMENT_RADIUS_FACTOR = 1.20;
	private final double DEFAULT_TIME_EXPRESS_ITINERARY = 4.0;
	private final int ERROR_CODE_NOT_TARGET_POI_CATEGORY_SELECTED = 600;
	private final int ERROR_CODE_UNCONNECTED_POIS = 601;
	private final int ERROR_CODE_NOT_FEASIBLE_ROUTE = 602;
	private final int ERROR_CODE_INTERNAL_ERROR = 603;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método GET de la aplicacion, atiende las peticiones de los clientes.
	 * 
	 * @return ruta recomendada
	 */
	public RouteResponse respondAsReady() {
		Request request = new Request();
		request.setSource_coordinates("-3.6971132 42.3436882");
		request.setTarget_coordinates("-3.7065066 42.3370885");
		request.setGastronomy_factor(30);
		request.setCulture_factor(50);
		request.setLeisure_factor(100);
		request.setRouteTime(2.0);
		request.setNature_factor(30);
		request.setTransport("dr_");
		request.setSetOfTags("");
		return execute(request);
	}

	/**
	 * Método que recibe una petición y calcula un itinerario recomendado.
	 * 
	 * @return ruta recomendada
	 */
	private RouteResponse execute(Request request) {
		ArrayList<Poi> nearestNodesList;
		DatabaseFacade db;
		Matrix matrix = null;
		IAlgorithm algorithm;
		model.Path path;
		RouteResponse r = new RouteResponse();
		String middle_coordinates;
		double radius;

		try {
			db = DatabaseFacade.getInstance();
			if (request.getTarget_options() == null) {
				middle_coordinates = Misc.calculateMiddleCoordinates(request);
			} else {
				middle_coordinates = request.getSource_coordinates();
			}
			radius = Misc.calculateSearchRadiusAccordingToTime(
					request.getRouteTime(), request.getTransport());
			db.changePrefixTable(request.getTransport());
			nearestNodesList = getNearestPoisAndFilterThem(request,
					middle_coordinates, radius);
			Misc.configureSourceOrTargetPoi(request.getSource_coordinates(),
					nearestNodesList, 0);
			if (request.getTarget_options() == null) {
				Misc.configureSourceOrTargetPoi(
						request.getTarget_coordinates(), nearestNodesList, 1);
			} else {
				Poi target_poi;
				target_poi = Misc.obtainRandomPoiByCategory(nearestNodesList,
						request.getTarget_options());
				if (target_poi != null) {
					nearestNodesList.add(1, target_poi);
				} else {
					System.err
							.println(ERROR_CODE_NOT_TARGET_POI_CATEGORY_SELECTED);
					r.setStatus(ERROR_CODE_NOT_TARGET_POI_CATEGORY_SELECTED);
					return r;
				}
			}
			matrix = RouteProvider.getInstance().getDistanceMatrix(
					nearestNodesList, request.getTransport(),
					request.getRouteTime());
			if (request.getRoute_mode().compareTo("fast") == 0) {
				algorithm = new Grasp(matrix, request.getRouteTime());
			} else {
				algorithm = new ChaoMingHeuristic(matrix,
						request.getRouteTime());
			}
			path = algorithm.execute();
			if (path != null && path.size() > 2) {
				matrix.getMatrix().clear();
				r = constructSetOfCoordinates(path, request.getTransport());
			} else {
				throw new AlgorithmException();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			r.setStatus(ERROR_CODE_INTERNAL_ERROR);
		} catch (RouterProviderException e) {
			e.printStackTrace();
			r.setStatus(ERROR_CODE_UNCONNECTED_POIS);
		} catch (AlgorithmException e) {
			e.printStackTrace();
			r.setStatus(ERROR_CODE_NOT_FEASIBLE_ROUTE);
		}
		return r;
	}

	/**
	 * Método que obtiene un listado de puntos cercanos y los filtra.
	 * 
	 * @return listado de nodos cercanos
	 */
	protected ArrayList<Poi> getNearestPoisAndFilterThem(Request request,
			String middle_coordinates, double radius) throws DatabaseException {
		ArrayList<Poi> nearestNodesList;
		int iterationLimit = 0;
		DatabaseFacade db = DatabaseFacade.getInstance();
		do {
			nearestNodesList = db.getNearestNodes("POINT(" + middle_coordinates
					+ ")", radius, request);
			if (nearestNodesList.size() > POI_COUNT_UPPER_THRESOLD) {
				Misc.removeUnconnectedPoisFromResultList(nearestNodesList,
						request.getTransport());
				Misc.filterPoiList(nearestNodesList);
			}
			iterationLimit++;
			radius = radius * INCREMENT_RADIUS_FACTOR;
		} while (nearestNodesList.size() < POI_COUNT_LOWER_THRESOLD
				&& iterationLimit < ITERATION_LIMIT_SEARCHING_POIS);
		return nearestNodesList;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que atiende las peticiones recibidas.
	 * 
	 * @return itinerario generado
	 */
	public RouteResponse listenRequest(
			MultivaluedMap<String, String> requestParams) {
		String lat_source, lon_source, lat_target, lon_target, setOfTags, target_options;
		Request request = new Request();
		lon_source = requestParams.getFirst("lon_source");
		lat_source = requestParams.getFirst("lat_source");
		request.setSource_coordinates(lon_source + " " + lat_source);
		target_options = requestParams.getFirst("target_options");
		if (target_options == null) {
			lon_target = requestParams.getFirst("lon_target");
			lat_target = requestParams.getFirst("lat_target");
		} else {
			lon_target = "";
			lat_target = "";
			request.setTarget_options(target_options);
		}
		request.setTarget_coordinates(lon_target + " " + lat_target);
		request.setTransport(requestParams.getFirst("transport"));
		request.setGastronomy_factor(Integer.parseInt(requestParams
				.getFirst("gastronomy")));
		request.setLeisure_factor(Integer.parseInt(requestParams
				.getFirst("leisure")));
		request.setCulture_factor(Integer.parseInt(requestParams
				.getFirst("culture")));
		request.setNature_factor(Integer.parseInt(requestParams
				.getFirst("nature")));
		request.setRouteTime(Double.parseDouble(requestParams.getFirst("time")));
		setOfTags = requestParams.getFirst("setOfTags");
		request.setRoute_mode(requestParams.getFirst("route_mode"));
		request.setSetOfTags(setOfTags);
		return execute(request);
	}

	@POST
	@Path("express")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que genera un itinerario recomendado.
	 * 
	 * @param requestParams parámetros de la petición
	 * @return  itinerario generado
	 */
	public RouteResponse performExpressItinerary(
			MultivaluedMap<String, String> requestParams) {
		String lat_source, lon_source;
		Request request = new Request();
		lon_source = requestParams.getFirst("lon_source");
		lat_source = requestParams.getFirst("lat_source");
		request.setSource_coordinates(lon_source + " " + lat_source);
		request.setTransport(requestParams.getFirst("transport"));
		request.setRouteTime(DEFAULT_TIME_EXPRESS_ITINERARY);
		request.setGastronomy_factor(50);
		request.setLeisure_factor(50);
		request.setNature_factor(50);
		request.setCulture_factor(50);
		request.setRoute_mode(requestParams.getFirst("route_mode"));
		return expressItinerary(request);

	}

	/**
	 * Método que genera un itinerario recomendado.
	 * 
	 * @param requestParams
	 *            parámetros de la petición
	 * @return itinerario generado
	 */
	private RouteResponse expressItinerary(Request request) {
		DatabaseFacade db;
		ArrayList<Poi> nearestNodesList;
		Matrix matrix = null;
		IAlgorithm algorithm;
		model.Path path;
		RouteResponse routeResponse = new RouteResponse();
		try {
			db = DatabaseFacade.getInstance();
			double radius = DEFAULT_SEARCH_RADIUS_EXPRESS_ITINERARY;
			db.changePrefixTable(request.getTransport());
			nearestNodesList = getNearestPoisAndFilterThem(request,
					request.getSource_coordinates(), radius);
			Misc.configureSourceOrTargetPoi(request.getSource_coordinates(),
					nearestNodesList, 0);
			Misc.configureSourceOrTargetPoi(request.getSource_coordinates(),
					nearestNodesList, 1);
			matrix = RouteProvider.getInstance().getDistanceMatrix(
					nearestNodesList, request.getTransport(),
					request.getRouteTime());
			if (request.getRoute_mode().compareTo("fast") == 0) {
				algorithm = new Grasp(matrix, DEFAULT_TIME_EXPRESS_ITINERARY);
			} else {
				algorithm = new ChaoMingHeuristic(matrix,
						DEFAULT_TIME_EXPRESS_ITINERARY);
			}
			path = algorithm.execute();
			if (path != null && path.size() > 2) {
				matrix.getMatrix().clear();
				routeResponse = constructSetOfCoordinates(path,
						request.getTransport());
			} else {
				throw new AlgorithmException();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			routeResponse.setStatus(ERROR_CODE_INTERNAL_ERROR);
		} catch (AlgorithmException e) {
			e.printStackTrace();
			routeResponse.setStatus(ERROR_CODE_NOT_FEASIBLE_ROUTE);
		} catch (RouterProviderException e) {
			e.printStackTrace();
			routeResponse.setStatus(ERROR_CODE_UNCONNECTED_POIS);
		}
		return routeResponse;
	}

	@POST
	@Path("customroute")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que calcula una ruta personalizada.
	 * 
	 * @param requestParams parámetros de la petición
	 * @return  itinerario generado
	 */
	public RouteResponse customRoute(
			MultivaluedMap<String, String> requestParams) {
		String source_lat = "", source_lon = "", target_lat = "", target_lon = "", transportation = "", poi_coordinates = "", count = "", poi_id = "", score = "";
		ArrayList<Poi> pois = new ArrayList<Poi>();
		count = requestParams.getFirst("count");
		Matrix matrix = null;
		IAlgorithm algorithm;
		transportation = requestParams.getFirst("transport");
		source_lat = requestParams.getFirst("lat_source");
		source_lon = requestParams.getFirst("lon_source");
		target_lat = requestParams.getFirst("lat_target");
		target_lon = requestParams.getFirst("lon_target");
		score = requestParams.getFirst("score");
		int poi_count = Integer.valueOf(count);
		for (int i = 0; i < poi_count; i++) {
			poi_coordinates = requestParams.getFirst("coordinates" + i);
			poi_id = requestParams.getFirst("poi_id" + i);
			score = requestParams.getFirst("score" + i);
			Poi poi = new Poi();
			poi.setCoordinates("POINT(" + poi_coordinates + ")");
			poi.setPoi_id(Long.valueOf(poi_id));
			poi.setScore(Double.valueOf(score));
			pois.add(poi);
		}
		RouteResponse response = null;
		DatabaseFacade db = null;
		try {
			db = DatabaseFacade.getInstance();
			db.changePrefixTable(transportation);
			response = new RouteResponse();
			ArrayList<Poi> listOfPois = new ArrayList<Poi>();
			Poi poi_src = new Poi();
			poi_src.setCoordinates("POINT(" + source_lon + " " + source_lat
					+ ")");
			poi_src.setPoi_id(-1L);
			Poi poi_tgt = new Poi();
			poi_tgt.setCoordinates("POINT(" + target_lon + " " + target_lat
					+ ")");
			poi_tgt.setPoi_id(-1L);
			listOfPois.add(poi_src);
			listOfPois.add(poi_tgt);
			listOfPois.addAll(pois);
			RouteProvider router = RouteProvider.getInstance();
			router.getVertices(listOfPois, router.getGraph(transportation));
			model.Path path = null;
			matrix = RouteProvider.getInstance().getDistanceMatrix(listOfPois,
					transportation, DEFAULT_TIME_EXPRESS_ITINERARY);
			algorithm = new Grasp(matrix, Double.MAX_VALUE);
			path = algorithm.execute();
			if (path != null && path.size() > 2) {
				matrix.getMatrix().clear();
				response = constructSetOfCoordinates(path, transportation);
			} else {
				throw new AlgorithmException();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			response.setStatus(ERROR_CODE_INTERNAL_ERROR);
		} catch (RouterProviderException e) {
			e.printStackTrace();
			response.setStatus(ERROR_CODE_UNCONNECTED_POIS);
		} catch (AlgorithmException e) {
			e.printStackTrace();
			response.setStatus(ERROR_CODE_NOT_FEASIBLE_ROUTE);
		}
		return response;
	}

	/**
	 * Método que reconstruye las coordenadas.
	 * 
	 * @param path
	 *            camino con los puntos de interés a visitar
	 * @param transportation
	 *            medio de transporte con el que se ha realizado la ruta
	 * @return itinerario generado
	 */
	private RouteResponse constructSetOfCoordinates(model.Path path,
			String transportation) throws DatabaseException,
			RouterProviderException {
		RouteResponse routeResponse = new RouteResponse();
		DatabaseFacade db;
		ArrayList<Float> costs;
		db = DatabaseFacade.getInstance();
		costs = new ArrayList<Float>();
		ArrayList<ArrayList<Integer>> setOfTableIds = RouteProvider
				.getInstance().getArrayOfTableIds(path, costs, transportation);
		ArrayList<String> coordinates;
		coordinates = db.obtainResultingCoordinates(path, setOfTableIds);
		routeResponse = new RouteResponse();
		routeResponse.setEncodedCoordinates(coordinates);
		routeResponse.setPoi_list(path.getPath());
		routeResponse.setCost_list(costs);
		return routeResponse;
	}

	public String printMatrix(Matrix matrix) {
		return matrix.toString();
	}

}

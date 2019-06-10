package resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import model.Poi;
import resource.request.Request;
import resource.response.PoiDetailsResponse;
import resource.response.RecommendedPoiListResponse;
import util.Misc;
import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso
 * RecommendedPois del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/poi")
public class RecommendedPoisResource {

	private final double DEFAULT_SEARCH_RADIUS_RECOMMENDED_POI_LIST = 5000;
	private final String MSG_ERROR_INTERNAL = "603";

	@POST
	@Path("recommendedlist")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que se encarga de la obtención de los puntos de interés recomendados.
	 * 
	 * @param requestParams parámetros de la petición
	 * @return listado de puntos de interés
	 */
	public RecommendedPoiListResponse getRecommendedPoiList(
			MultivaluedMap<String, String> requestParams) {
		String lat = "", lon = "";
		RecommendedPoiListResponse response = new RecommendedPoiListResponse();
		ArrayList<Poi> poiList = null;
		lat = requestParams.getFirst("lat");
		lon = requestParams.getFirst("lon");
		Request request = new Request();
		request.setCulture_factor(100);
		request.setGastronomy_factor(100);
		request.setNature_factor(100);
		request.setLeisure_factor(100);
		DatabaseFacade db;
		try {
			db = DatabaseFacade.getInstance();
			poiList = db.getNearestNodes("POINT(" + lon + " " + lat + ")",
					DEFAULT_SEARCH_RADIUS_RECOMMENDED_POI_LIST, request);
			Misc.removeUnconnectedPoisFromResultList(poiList,
					requestParams.getFirst("transport"));
			Misc.filterPoiList(poiList);
			response = new RecommendedPoiListResponse();
			for (Poi poi : poiList) {
				poi.setCoordinates(Misc
						.extractCoordinates(poi.getCoordinates()).get(0));
			}
			response.setPoi_list(poiList);
		} catch (DatabaseException e) {
			e.printStackTrace();
			response.setStatus(MSG_ERROR_INTERNAL);
		}
		return response;
	}

	@POST
	@Path("opinions")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que obiene la información detallada de un punto de interés.
	 * 
	 * @param requestParams parámetros de la petición
	 * @return listado de opiniones
	 */
	public List<PoiDetailsResponse> poiDetails(
			MultivaluedMap<String, String> requestParams) {
		String poi_id = "";
		poi_id = requestParams.getFirst("poi_id");
		return getPoiDetails(poi_id);
	}

	
	/**
	 * Método que obiene la información detallada de un punto de interés.
	 * 
	 * @param poi_id id del punto de interés
	 * @return listado de opiniones
	 */
	private List<PoiDetailsResponse> getPoiDetails(String poi_id) {
		DatabaseFacade db;
		List<PoiDetailsResponse> opinionsResponse = new ArrayList<PoiDetailsResponse>();
		try {
			db = DatabaseFacade.getInstance();
			opinionsResponse = db.getPoiExperiences(Long.valueOf(poi_id));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return opinionsResponse;
	}

}

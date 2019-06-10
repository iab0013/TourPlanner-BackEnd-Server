package resource;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import model.Route;
import resource.response.CityResponse;
import resource.response.MyRoutesResponse;
import database.DatabaseConfigurationException;
import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que contiene la lÃ³gica necesaria para interactuar con el recurso
 * ruta del servidor.
 * 
 * @author Alejandro Cuevas Álvarez - aca0073@alu.ubu.es
 */
@Path("/route")
public class MyRoutesResource {

	private final String OK_SAVE = "OK_SAVE";
	private final String OK_DELETE = "OK_DELETE";
	private final String MSG_ERROR_INTERNAL = "603";	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que obtiene las coordenadas de una ruta.
	 * 
	 * @params requestParams
	 * 		Parametros de la petición.
	 * @return Ruta obtenida.
	 * 
	 */
	public MyRoutesResponse getRouteCoordinates(
			MultivaluedMap<String, String> requestParams) {
		String route_name = "", email = "";
		route_name = requestParams.getFirst("route_name");
		email = requestParams.getFirst("email");
		return execute(route_name, email);

	}

	/**
	 * Método que obtiene las coordenadas de una ruta.
	 * 
	 * @params route_name
	 * 		Nombre de la ruta.
	 * * @params email
	 * 		E-mail del usuario.
	 * @return Ruta obtenida.
	 * 
	 */
	public MyRoutesResponse execute(String route_name, String email) {
		DatabaseFacade db;
		String coordinates = "";
		int user_id = -1;
		MyRoutesResponse routeResponse = new MyRoutesResponse();
		try {
			db = DatabaseFacade.getInstance();
			user_id = db.getUserIdBy(email); 
			coordinates = db.getRouteCoordinates(route_name, user_id);
			routeResponse.setCoordinates(coordinates);
		} catch (DatabaseException e) {
			e.printStackTrace();
			routeResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		return routeResponse;
	}
	
	@POST
	@Path("all")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	
	/**
	 * MÃ©todo que devuelve todas las rutas alamacenadas por un usuario.
	 * 
	 * @param requestParams
	 * 		Parametros de la petición.
	 * @return lista de rutas.
	 * 
	 */
	public MyRoutesResponse getAllRoutes(MultivaluedMap<String, String> requestParams) {
		DatabaseFacade db;
		ArrayList<Route> routes = null;
		int user_id = 0;
		MyRoutesResponse routeResponse = new MyRoutesResponse();
		
		try {
			db = DatabaseFacade.getInstance();
			user_id = db.getUserIdBy(requestParams.getFirst("email"));
			routes = db.getRoutesByUserId(user_id);
			routeResponse.setRoutesList(routes);
		} catch (DatabaseConfigurationException e) {
			e.printStackTrace();
			routeResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		
		return routeResponse;
	}
	

	@POST
	@Path("save")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * MÃ©todo que guarda una ruta en la base de datos.
	 * 
	 * @param requestParams
	 * 		Parametros de la petición.
	 * @return información sobre la operación realizada.
	 * 
	 */
	public MyRoutesResponse save(MultivaluedMap<String, String> requestParams) {
		DatabaseFacade db;
		String latitude, longitude, point, route_name, date;
		int user_id;
		double rating;
		CityResponse city;
		MyRoutesResponse routeResponse = new MyRoutesResponse();
		
		try {
			db = DatabaseFacade.getInstance();
			route_name = requestParams.getFirst("route_name");
			user_id = db.getUserIdBy(requestParams.getFirst("email")); 
			latitude = requestParams.getFirst("latitude");
			longitude = requestParams.getFirst("longitude");
			point = "POINT(" + longitude + " " + latitude + ")";
			city = db.getCityByLocation(point);
			rating = Double.parseDouble(requestParams.getFirst("rating"));
			date = requestParams.getFirst("date");
			db.insertRoute(requestParams.getFirst("coordinates"), user_id, city.getId(), 
			route_name, rating, date);
			routeResponse.setStatus(OK_SAVE);
			Route route = new Route();
			route.setName(route_name);
			route.setCity(db.getCityById(city.getId()));
			route.setRating(rating);
			route.setDate(date);
			routeResponse.setRoute(route);
		} catch (DatabaseConfigurationException e) {
			e.printStackTrace();
			routeResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		
		return routeResponse;
		
	}
	
	@POST
	@Path("delete")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * MÃ©todo que elimina una ruta en la base de datos.
	 * 
	 * @param requestParams
	 * 		Parametros de la petición.
	 * @return información sobre la operación realizada.
	 * 
	 */
	public MyRoutesResponse delete(MultivaluedMap<String, String> requestParams) {
		DatabaseFacade db;
		MyRoutesResponse routeResponse = new MyRoutesResponse();
		
		try {
			db = DatabaseFacade.getInstance();
			db.deleteRoute(requestParams.getFirst("route_name"));
			routeResponse.setStatus(OK_DELETE);
		} catch (DatabaseConfigurationException e) {
			e.printStackTrace();
			routeResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		
		return routeResponse;
		
	}

}


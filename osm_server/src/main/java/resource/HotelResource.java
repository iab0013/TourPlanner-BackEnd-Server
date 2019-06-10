package resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import resource.response.CityResponse;
import resource.response.HotelsResponse;
import database.DatabaseConfigurationException;
import database.DatabaseException;
import database.DatabaseFacade;


/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso
 * hotel del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/hotels")
public class HotelResource {

	private final String MSG_ERROR_INTERNAL = "603";	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que recibe una petición con un prefijo y ciudad, y devuelve una lista de
	 * hoteles que empizan por dicho prefijo.
	 * 
	 * @return lista de hoteles
	 */
	public HotelsResponse getHotelsFromDatabaseByPrefix(
			MultivaluedMap<String, String> requestParams) {
		String prefix = "";
		String city_name = "";
		prefix = requestParams.getFirst("prefix");
		city_name = requestParams.getFirst("city_name");
		return execute(prefix, city_name);

	}

	/**
	 * Método que recibe una petición con un prefijo y ciudad, y devuelve una lista de
	 * hoteles que empizan por dicho prefijo.
	 * 
	 * @return lista de hoteles
	 */
	public HotelsResponse execute(String prefix, String city_name) {
		DatabaseFacade db;
		HotelsResponse hotelsResponse = new HotelsResponse();
		CityResponse city;
		try {
			db = DatabaseFacade.getInstance();
			city = db.checkIfCityExists(city_name);
			hotelsResponse = db.checkIfHotelExists(prefix, city, false);
		} catch (DatabaseException e) {
			e.printStackTrace();
			hotelsResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		return hotelsResponse;
	}

	@POST
	@Path("exists")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que comprueba si un determinado hotel existe.
	 * 
	 * @return parámetros del hotel
	 */
	public HotelsResponse exists(MultivaluedMap<String, String> requestParams) {
		DatabaseFacade db;
		String hotel_name, city_name;
		CityResponse city_params = null;
		HotelsResponse hotel_params = new HotelsResponse();
		try {
			db = DatabaseFacade.getInstance();
			hotel_name = requestParams.getFirst("hotel_name");
			city_name = requestParams.getFirst("city_name");
			city_params = db.checkIfCityExists(city_name);
			hotel_params = db.checkIfHotelExists(hotel_name, city_params, true);
			return hotel_params;
		} catch (DatabaseConfigurationException e) {
			e.printStackTrace();
			hotel_params.setStatus(MSG_ERROR_INTERNAL);
		} 
		return hotel_params;
	}

}

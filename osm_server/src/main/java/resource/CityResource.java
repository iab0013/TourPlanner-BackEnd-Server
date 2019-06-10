package resource;

import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import resource.response.CitiesResponse;
import resource.response.CityResponse;
import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso ciudad
 * del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/cities")
public class CityResource {

	private final String MSG_ERROR_INTERNAL = "603";

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que recibe una petición con un prefijo y devuelve una lista de
	 * ciudades que empizan por dicho prefijo.
	 * 
	 * @return lista de ciudades
	 */
	public CitiesResponse getCitiesFromDatabaseByPrefix(
			MultivaluedMap<String, String> requestParams) {
		String prefix = "";
		prefix = requestParams.getFirst("prefix");
		return execute(prefix);

	}

	/**
	 * Método que recibe una petición con un prefijo y devuelve una lista de
	 * ciudades que empizan por dicho prefijo.
	 * 
	 * @return lista de ciudades
	 */
	public CitiesResponse execute(String prefix) {
		DatabaseFacade db;
		ArrayList<String> citiesList = null;
		CitiesResponse response = new CitiesResponse();
		try {
			db = DatabaseFacade.getInstance();
			citiesList = db.getCitiesByPrefix(prefix);
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
			response.setStatus(MSG_ERROR_INTERNAL);
		}
		response.setCitiesList(citiesList);
		return response;
	}

	@POST
	@Path("exists")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que comprueba si una determinada ciudad está registrada en la base de datos.
	 * 
	 * @return ciudad con los datos correspondientes o null si no existe
	 */
	public CityResponse exists(MultivaluedMap<String, String> requestParams) {
		DatabaseFacade db;
		String city_name;
		CityResponse city_params = new CityResponse();
		try {
			db = DatabaseFacade.getInstance();
			city_name = requestParams.getFirst("city_name");
			city_params = db.checkIfCityExists(city_name);
			if (city_params != null && city_params.getCoordinates() != "") {
				return city_params;
			}
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
			city_params.setStatus(MSG_ERROR_INTERNAL);
		}
		return city_params;
	}

}

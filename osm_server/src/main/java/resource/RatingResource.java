package resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso rating
 * del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/rating")
public class RatingResource {

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que se encarga del proceso de rating
	 * 
	 * @param requestParams parámetros de la petición
	 */
	public void registerVotingFromUser(
			MultivaluedMap<String, String> requestParams) {
		String poi_id = "", rating = "", count = "", email = "", opinion = "";
		count = requestParams.getFirst("count");
		email = requestParams.getFirst("email");
		int poi_count = Integer.valueOf(count);
		long id;
		int user_rating;
		for (int i = 0; i < poi_count; i++) {
			poi_id = requestParams.getFirst("poi_id" + i);
			rating = requestParams.getFirst("rating" + i);
			opinion = requestParams.getFirst("opinion" + i);
			id = Long.valueOf(poi_id);
			user_rating = Double.valueOf(rating).intValue();
			registerVotation(id, user_rating, email, opinion);
		}

	}
	
	/**
	 * Método que se registra una votación en el sistema
	 * 
	 * @param id del punto de interés
	 * @param user_rating rating para ese punto de interés
	 * @param email del usuario
	 * @param opinion del usuario para ese punto de interés
	 */
	private void registerVotation(long id, int user_rating, String email,
			String opinion) {
		DatabaseFacade db;
		try {
			db = DatabaseFacade.getInstance();
			if (!db.registerVisitedPoi(id, user_rating, email, opinion)) {
				db.registerVotation(id, user_rating);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} 

	}

}

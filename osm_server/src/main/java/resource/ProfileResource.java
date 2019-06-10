package resource;

import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import model.Poi;
import database.DatabaseException;
import database.DatabaseFacade;
import resource.response.ProfileResponse;
import resource.response.UserActivity;

/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso
 * profile del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/profile")
public class ProfileResource {
	
	private final String MSG_ERROR_INTERNAL = "603";

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que se encarga de gestionar el perfil del usuario.
	 * 
	 * @param requestParams parámetros de la petición
	 * @return información del perfil
	 */
	public ProfileResponse getProfileInfo(
			MultivaluedMap<String, String> requestParams) {
		String email = requestParams.getFirst("email");
		return execute(email);
	}

	/**
	 * Método que se encarga de gestionar el perfil del usuario.
	 * 
	 * @param email email del usuario
	 * @return información del perfil
	 */
	public ProfileResponse execute(String email) {
		DatabaseFacade db;
		ProfileResponse profileResponse = new ProfileResponse();
		int user_id, count;
		ArrayList<Poi> pois = null;
		ArrayList<UserActivity> activity = null;
		try {
			db = DatabaseFacade.getInstance();
			activity = new ArrayList<UserActivity>();
			user_id = db.getUserIdBy(email);
			count = db.getVisitedPoisCount(user_id);
			pois = db.getVisitedPoisPriorized(user_id);
			for (Poi poi : pois) {
				UserActivity user_activity = new UserActivity();
				user_activity.setScore_submitted((int) poi.getScore());
				db.getPlaceNameAccordingToType(
						poi.getPoi_id(), user_activity, poi.getTag());
				activity.add(user_activity);
			}
			profileResponse.setUser_activity(activity);
			profileResponse.setVisited_pois_count(count);
		} catch (DatabaseException e) {
			e.printStackTrace();
			profileResponse.setStatus(MSG_ERROR_INTERNAL);
		} 

		return profileResponse;

	}

}

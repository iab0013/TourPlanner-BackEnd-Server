package resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import crypt.BCrypt;
import database.DatabaseException;
import database.DatabaseFacade;
import resource.response.LoginResponse;

/**
 * Clase que contiene la lógica necesaria para interactuar con el recurso login
 * del servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@Path("/authentication")
public class LoginResource {

	private final String AUTHENTICATED = "Authenticated";
	private final String REGISTERED = "Registered";
	private final String WRONG_PASSWORD = "Wrong Password";
	private final String NOT_REGISTERED = "Not Registered";
	private final String MSG_ERROR_INTERNAL = "603";

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que se encarga del proceso de autentificación en el sistema
	 * 
	 * @param requestParams parámetros de la petición
	 * @return ruta recomendada
	 */
	public LoginResponse authentication(
			MultivaluedMap<String, String> requestParams) {
		String email = "", password = "", name = "";
		email = requestParams.getFirst("email");
		password = requestParams.getFirst("password");
		name = requestParams.getFirst("name");
		return execute(email, password, name);
	}

	/**
	 * Método que se encarga del proceso de autentificación en el sistema
	 * 
	 * @param email email del usuario
	 * @param password contraseña del usuario
	 * @param name nombre del usuario
	 * @return resultado de la autentificación
	 */
	public LoginResponse execute(String email, String password, String name) {
		DatabaseFacade db;
		LoginResponse loginResponse = new LoginResponse();
		String userName;
		try {
			db = DatabaseFacade.getInstance();
			userName = db.checkIfUserIsRegistered(email, password);
			if (userName.compareTo("") != 0) {
				loginResponse.setStatus(AUTHENTICATED);
				loginResponse.setUser_name(userName);
			} else if (userName.compareTo("wrong_password") == 0) {
				loginResponse.setStatus(WRONG_PASSWORD);
			} else if (name != null) {
				String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
				db.registerUser(email, hashed, name);
				loginResponse.setStatus(REGISTERED);
			} else {
				loginResponse.setStatus(NOT_REGISTERED);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			loginResponse.setStatus(MSG_ERROR_INTERNAL);
		}
		return loginResponse;

	}
}

package resource.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Clase que define la estructura de las respuestas generadas al invocar al
 * recurso ProfileResource.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@XmlRootElement
public class ProfileResponse {

	private int visited_pois_count;
	private List<UserActivity> user_activity;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getVisited_pois_count() {
		return visited_pois_count;
	}

	public void setVisited_pois_count(int visited_pois_count) {
		this.visited_pois_count = visited_pois_count;
	}

	public List<UserActivity> getUser_activity() {
		return user_activity;
	}

	public void setUser_activity(List<UserActivity> user_activity) {
		this.user_activity = user_activity;
	}


}

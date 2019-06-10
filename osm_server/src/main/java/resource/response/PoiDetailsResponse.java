package resource.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clase que define la estructura de las respuestas generadas al invocar al
 * recurso RecommendedPoisResource.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@XmlRootElement
public class PoiDetailsResponse {

	private String opinion;
	private String user_name;
	private int score_submitted;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public int getScore_submitted() {
		return score_submitted;
	}

	public void setScore_submitted(int score_submitted) {
		this.score_submitted = score_submitted;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}


	
	
}

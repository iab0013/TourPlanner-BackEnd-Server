package resource.response;

/**
 * Clase que gestiona la actividad del usuario en la aplicaci�n.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class UserActivity {

	private int score_submitted;
	private String place_name;
	private String tag;
	private String category;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getScore_submitted() {
		return score_submitted;
	}

	public void setScore_submitted(int score_submitted) {
		this.score_submitted = score_submitted;
	}

	public String getPlace_name() {
		return place_name;
	}

	public void setPlace_name(String place_name) {
		this.place_name = place_name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
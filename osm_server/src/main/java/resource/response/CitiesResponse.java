package resource.response;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clase que define la estructura de las respuestas generadas al invocar al
 * recurso CityResource.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@XmlRootElement
public class CitiesResponse {

	private List<String> citiesList;

	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getCitiesList() {
		return citiesList;
	}

	public void setCitiesList(List<String> citiesList) {
		this.citiesList = citiesList;
	}

}

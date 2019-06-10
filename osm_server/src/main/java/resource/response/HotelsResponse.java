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
public class HotelsResponse {

	private List<String>  hotelList;
	private String coordinates;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public List<String> getHotelList() {
		return hotelList;
	}

	public void setHotelList(List<String> hotelList) {
		this.hotelList = hotelList;
	}
	
}

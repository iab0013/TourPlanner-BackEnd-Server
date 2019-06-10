package resource.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import model.Poi;

/**
 * Clase que define la estructura de las respuestas generadas al invocar al
 * recurso ItineraryResource.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@XmlRootElement
public class RouteResponse {
	
	private List<String> encodedCoordinates;

	private List<Poi> poi_list;
	
	private List<Float> cost_list;
	
	private Integer status;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<Float> getCost_list() {
		return cost_list;
	}

	public void setCost_list(List<Float> cost_list) {
		this.cost_list = cost_list;
	}

	public List<Poi> getPoi_list() {
		return poi_list;
	}

	public void setPoi_list(List<Poi> poi_list) {
		this.poi_list = poi_list;
	}

	
	public List<String> getEncodedCoordinates() {
		return encodedCoordinates;
	}

	public void setEncodedCoordinates(List<String> encodedCoordinates) {
		this.encodedCoordinates = encodedCoordinates;
	}

	public void addEncodedCoordinates(String encodedCoordinates) {
		this.encodedCoordinates.add(encodedCoordinates);
	}
	
	
	
}

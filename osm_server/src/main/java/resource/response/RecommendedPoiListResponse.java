package resource.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import model.Poi;

/**
 * Clase que define la estructura de las respuestas generadas al invocar al
 * recurso RecommendedPoisResource.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
@XmlRootElement
public class RecommendedPoiListResponse {
	
	private List<Poi> poi_list;
	
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Poi> getPoi_list() {
		return poi_list;
	}

	public void setPoi_list(List<Poi> poi_list) {
		this.poi_list = poi_list;
	}




}

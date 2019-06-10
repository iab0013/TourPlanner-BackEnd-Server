package resource.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import model.Route;

/**
* Clase que define la estructura de las respuestas generadas al invocar al
* recurso MyRouteResponse.
* 
* @author Alejandro Cuevas Álvarez - aca0073@alu.ubu.es
*/
@XmlRootElement
public class MyRoutesResponse {
	
	private String status;
	private String coordinates;
	private Route route;
	private List<Route> routesList;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public List<Route> getRoutesList() {
		return routesList;
	}

	public void setRoutesList(List<Route> routesList) {
		this.routesList = routesList;
	}
	
	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	
}

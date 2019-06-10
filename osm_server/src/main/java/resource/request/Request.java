package resource.request;

/**
 * Clase que define la estructura de las peticiones recibidas en el servidor.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Request {

	private String source_coordinates;
	private String target_coordinates;
	private String transport;
	private String target_options;
	private String route_mode;
	private int gastronomy_factor;
	private int leisure_factor;
	private int culture_factor;
	private int nature_factor;
	private double routeTime;
	private String setOfTags;

	public String getSetOfTags() {
		return setOfTags;
	}

	public void setSetOfTags(String setOfTags) {
		this.setOfTags = setOfTags;
	}

	public double getRouteTime() {
		return routeTime;
	}

	public void setRouteTime(double routeTime) {
		this.routeTime = routeTime;
	}

	public String getSource_coordinates() {
		return source_coordinates;
	}

	public void setSource_coordinates(String source_coordinates) {
		this.source_coordinates = source_coordinates;
	}

	public String getTarget_coordinates() {
		return target_coordinates;
	}

	public void setTarget_coordinates(String target_coordinates) {
		this.target_coordinates = target_coordinates;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public int getGastronomy_factor() {
		return gastronomy_factor;
	}

	public void setGastronomy_factor(int gastronomy_factor) {
		this.gastronomy_factor = gastronomy_factor;
	}

	public int getLeisure_factor() {
		return leisure_factor;
	}

	public void setLeisure_factor(int leisure_factor) {
		this.leisure_factor = leisure_factor;
	}

	public int getCulture_factor() {
		return culture_factor;
	}

	public void setCulture_factor(int culture_factor) {
		this.culture_factor = culture_factor;
	}

	public int getNature_factor() {
		return nature_factor;
	}

	public void setNature_factor(int nature_factor) {
		this.nature_factor = nature_factor;
	}

	public String getTarget_options() {
		return target_options;
	}

	public void setTarget_options(String target_options) {
		this.target_options = target_options;
	}

	public String getRoute_mode() {
		return route_mode;
	}

	public void setRoute_mode(String route_mode) {
		this.route_mode = route_mode;
	}

}

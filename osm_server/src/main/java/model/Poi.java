package model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Clase para gestionar todo lo referente acerca de los puntos de interés que
 * aparecerán en las rutas.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Poi implements Comparable<Poi> {

	private Long poi_id;
	/**
	 * Vértice más cercano.
	 */
	private int vertex;
	private double score;
	private String coordinates;
	private String name;
	private String tag;
	private Category category;
	
	private int serviceTime;
	private int openingTime;
	private int closingTime;
	
	private int arrival;
	private int wait;
	private int startTime;
	private int shift;
	private int maxShift;
	
	/**
	 * Tiempo establecido para permanecer en dicho punto de interás.
	 */
	private int time_to_stay;
	/**
	 * Variable que indica si es promocionado.
	 */
	private boolean promoted;
	/**
	 * Tipo de punto de interés ('N' nodo o 'W' way).
	 */
	private char type;

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	/**
	 * Constructor de la clase Poi.
	 * 
	 */
	public Poi() {
		promoted = false;
		time_to_stay = 0;
	}

	public boolean isPromoted() {
		return promoted;
	}

	public void setPromoted(boolean promoted) {
		this.promoted = promoted;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getTime_to_stay() {
		return time_to_stay;
	}

	public void setTime_to_stay(int time_to_stay) {
		this.time_to_stay = time_to_stay;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public Long getPoi_id() {
		return poi_id;
	}

	public void setPoi_id(Long poi_id) {
		this.poi_id = poi_id;
	}

	public int getVertex() {
		return vertex;
	}

	public void setVertex(int vertex) {
		this.vertex = vertex;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public int getWait(){
		return wait;
	}
	
	public void setWait(int wait){
		this.wait=wait;
	}
	
	public int getMaxShift(){
		return maxShift;
	}
	
	public void setMaxShift(int maxShift){
		this.maxShift=maxShift;
	}
	
	public int getArrival(){
		return arrival;
	}
	
	public void setArrival(int arrival){
		this.arrival=arrival;
	}
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public void setShift(int shift) {
		this.shift = shift;
	}
	
	public int getShift() {
		return shift;
	}

	@Override
	public int compareTo(Poi o) {
		if (this.getPoi_id().compareTo(o.getPoi_id()) == 0
				&& this.getVertex() == o.getVertex()) {
			return 0;
		}
		return -1;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(this.poi_id)
				.append(this.vertex).toHashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;

		Poi rhs = (Poi) obj;
		return new EqualsBuilder()
				.
				// if deriving: appendSuper(super.equals(obj)).
				append(this.poi_id, rhs.poi_id).append(vertex, rhs.vertex)
				.isEquals();
	}
	
	public int calculateWait(){
		int wait = 0;
		//System.out.println("Calculating wait time -- poi: "+poi_id+" opening: "+openingTime+" arrival: "+arrival);
		wait = Math.max(0,(openingTime-arrival));
		return wait;
	}
	
	public int calculateStart(){
		return this.arrival+this.wait;
	}

	public enum Category {
		GASTRONOMY, LEISURE, CULTURE, NATURE
	}

	public int getServiceTime() {
		return serviceTime;
	}

	public int getOpeningTime() {
		return openingTime;
	}

	public int getClosingTime() {
		return closingTime;
	}

	public void setServiceTime(int serviceTime) {
		this.serviceTime=serviceTime;
		
	}

	public void setOpeningTime(int openingTime) {
		this.openingTime=openingTime;
		
	}

	public void setClosingTime(int closingTime) {
		this.closingTime=closingTime;
		
	}

}

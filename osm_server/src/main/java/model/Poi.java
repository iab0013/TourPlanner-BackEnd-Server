package model;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.rits.cloning.Cloner;

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
		//wait = Math.max(0,(openingTime-arrival));
		wait = calculateWaitArgs(arrival);
		return wait;
	}
	
	public int calculateWaitArgs(int arrival){
		int wait = 0;
		wait = Math.max(0, openingTime-arrival);
		return wait;
	}
	
	public int calculateStart(){
		return this.arrival+this.wait;
	}
	
	public int calculateStartArgs(int arrival, int wait){
		return arrival+wait;
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
	
	@Override
	public Object clone() {
		Cloner cloner = new Cloner();
		return cloner.deepClone(this);
	}
	
	public static boolean isNumeric(String str) {
		boolean numeric = true;
        try {
            Double num = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            numeric = false;
        }
        return numeric;
	}
	
	public static long getFullHour(String hoursSplit, String minutesSplit) {
		long hours = TimeUnit.HOURS.toMillis(Integer.parseInt(hoursSplit));
		long minutes = TimeUnit.MINUTES.toMillis(Integer.parseInt(minutesSplit));
		return hours+minutes;
	}
	
	public static void transformStringContent(String strInput) {
		long opening=-1,closing=-1;
		boolean wrongInputDetected = false;
		String date = strInput;
		//es un caso de 13:00 o 13:00+ o 12:30-22:30
		//los + se trataran como de 13:00 a 23:00
		//cuidado con Jun-Sep
		//casos de Mar 15-Nov 15 11:00-24:00
		//casos de Mar-Oct 06:00-22:00 o We 10:00-14:00
		String [] noComas = date.split(";");
		String [] tokens = noComas[0].split(" ");
		String [] detectWrongInputs = noComas[0].split("\\s\\-\\s|\\s|\\-");
		if(detectWrongInputs.length==2) {
			if(isNumeric(detectWrongInputs[0].substring(0,1))&&isNumeric(detectWrongInputs[1].substring(0,1))) {
				wrongInputDetected=true;
				String[] separeDotsOp = detectWrongInputs[0].split(":|\\+|-| ");
				String[] separeDotsCl = detectWrongInputs[1].split(":|\\+|-| ");
				opening=getFullHour(separeDotsOp[0], separeDotsOp[1]);
				closing = getFullHour(separeDotsCl[0], separeDotsCl[1]);
			}
		}
		
		String lastValue = tokens[tokens.length-1];
		System.out.println("lastValue: "+lastValue);
		if(isNumeric(lastValue.substring(0,1))&&wrongInputDetected==false) {
			String[] separeDots = lastValue.split(":|\\+|-| ");
			if(separeDots.length==2) {
				//13:00
				opening = getFullHour(separeDots[0], separeDots[1]);
				closing = TimeUnit.HOURS.toMillis(24);
			}else if(separeDots.length==4) {
				//13:00-23:00
				opening = getFullHour(separeDots[0], separeDots[1]);
				closing = getFullHour(separeDots[2], separeDots[3]);
			}
		}
		System.out.println("opening: "+opening+" closing: "+closing);
		if(opening == -1 || closing == -1) {
			System.out.println("Aplicando valores base 9:00-21:00");
			opening = TimeUnit.HOURS.toMillis(9);
			closing = TimeUnit.HOURS.toMillis(21);
		}
	}

}

package model;

import java.util.LinkedList;

import com.rits.cloning.Cloner;

/**
 * Clase para gestionar los paths o caminos (conjunto de puntos de interés
 * ordenados).
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Path {

	private LinkedList<Poi> path;
	private double score;
	private double cost;
	private double timeLimit;

	/**
	 * Constructor de la clase Path.
	 * 
	 * @param timeLimit
	 *            límite de tiempo para este itinerario o path
	 */
	public Path(double timeLimit) {
		path = new LinkedList<Poi>();
		score = 0;
		setTimeLimit(timeLimit);
	}

	public LinkedList<Poi> getPath() {
		return path;
	}

	public void setPath(LinkedList<Poi> path) {
		this.path = path;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * Método que añade un POI determinado en una posición determinada.
	 * 
	 * @param poi
	 *            POI a insertar
	 * @param index
	 *            posición en la que se insertará el POI
	 * @return si el último POI se ha insertado de modo factible
	 */
	public void add(Poi poi, int index) {
		this.getPath().add(index, poi);
		this.recalculateScore();
	}

	public Poi get(int index) {
		return this.getPath().get(index);
	}

	/**
	 * Método que comprueba si el último POI insertado es factible (no rebasa el
	 * tiempo límite).
	 * 
	 * @param poi
	 *            último POI insertado
	 * @return si el último POI se ha insertado de modo factible
	 */
	public boolean isInsertionFeasible(Poi lastPoiInserted) {
		if (this.getCost() <= getTimeLimit()) {
			recalculateScore();
			return true;
		}
		this.remove(lastPoiInserted);
		return false;

	}

	/**
	 * Método que recalcula el score total asociado al itinerio o path.
	 * 
	 */
	public void recalculateScore() {
		int score = 0;
		for (Poi poi : this.getPath()) {
			score += poi.getScore();
		}
		this.setScore(score);
	}

	public String toString() {
		String str = "";
		int order = 1;
		for (Poi poi : path) {
			str += order + ": " + poi.getPoi_id() + " - " + poi.getVertex()
					+ " - " + poi.getScore() + "\n";
			order++;
		}
		str += "COSTE TOTAL RUTA: " + this.cost + "\n";
		str += "SCORE TOTAL RUTA: " + this.score + "\n";
		return str;
	}

	public void remove(Poi poi) {
		this.getPath().remove(poi);
		this.recalculateScore();
	}

	@Override
	public Object clone() {
		Cloner cloner = new Cloner();
		return cloner.deepClone(this);
	}

	/**
	 * Método que comprueba si un determinado POI está contenido en el path.
	 * 
	 * @param poi
	 *            a comprobar
	 * @return si el POI se encuentra en el path
	 */
	public boolean contains(Poi poi) {
		return this.getPath().contains(poi);
	}

	@Override
	/**
	 * Método que comprueba si dos paths son iguales.
	 * 
	 * @param otherPath path a comprobar
	 * @return si los paths son iguales
	 */
	public boolean equals(Object otherPath) {
		if (((Path) otherPath).size() != this.size()) {
			return false;
		}
		for (int i = 0; i < ((Path) otherPath).size(); i++) {
			if (!this.get(i).equals(((Path) otherPath).get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Método que devuelve el tamaño del path.
	 * 
	 * @return tamaño del path
	 */
	public int size() {
		return path.size();
	}
	
	public Poi getPrevious(Poi currentPoi){
		return path.get(path.indexOf(currentPoi)-1);
	}
	
	public Poi getNext(Poi currentPoi){
		return path.get(path.indexOf(currentPoi)+1);
	}

	
	
	public int calculateMaxShift(Poi currentPoi){
		int maxShift=0;
		int firstValue, secondValue;
		int currentIndex;
		
		firstValue=currentPoi.getClosingTime()-(int)currentPoi.getStartTime();
		
		if(path.getLast().equals(currentPoi)){
			return firstValue;
		}else{
			currentIndex = path.indexOf(currentPoi);
			//(wait i+1)+(maxshift i+1)
			secondValue=(path.get(currentIndex+1).getWait())+(path.get(currentIndex+1).getMaxShift());
			maxShift=Math.min(firstValue, secondValue);
			return maxShift;
		}
	}
	
	public void setMaxShiftFromLast(){
		for(int i=path.size()-1;i>=0;i--){
			//System.out.println("ENTRAMOS MARCHA ATRAS CON i: "+i);
			int newVertexMaxShift = calculateMaxShift(path.get(i));
			path.get(i).setMaxShift(newVertexMaxShift);
		}
	}
	

}

package algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import model.Matrix;
import model.Path;
import model.Poi;

/**
 * Clase que contiene las operaciones y variables comunes a los algoritmos
 * de creaci贸n de rutas.
 * 
 * @author Inigo V谩zquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public abstract class Algorithm {
	
	protected int startTime;

	protected double timeMax;

	protected Matrix matrix;

	protected LinkedList<Poi> candidatePoiList;

	/**
	 * Path 贸ptimo (mayor score).
	 */
	protected Path pathOp;

	protected Poi source_poi;

	protected Poi target_poi;
	
	/**
	 * Conjunto de paths candidatos.
	 */
	protected ArrayList<Path> paths;

	/**
	 * Constructor de la clase Algorithm.
	 * 
	 * @param matrix
	 *            matriz de distancias
	 */
	public Algorithm(Matrix matrix, Double routeTime) {
		this.matrix = matrix;
		this.timeMax = routeTime;
		this.startTime = 21600000; //ESTA PUESTO FIJO A LAS 6 DE LA MA袮NA
		paths = new ArrayList<Path>();
		initilization();
	}

	/**
	 * M茅todo que inicializa las variables y estructuras necesarias para el
	 * proceso de creaci贸n de rutas.
	 * 
	 */
	protected void initilization() {
		candidatePoiList = matrix.multikeymapToLinkedList();
		source_poi = matrix.getSource();
		source_poi.setArrival(startTime);
		source_poi.setWait(source_poi.calculateWait());
		source_poi.setStartTime(source_poi.calculateStart());
		candidatePoiList.remove(matrix.getSource());
		
		target_poi = matrix.getTarget();
		target_poi.setArrival(calculateArrival(source_poi, target_poi));
		target_poi.setWait(target_poi.calculateWait());
		target_poi.setStartTime(target_poi.calculateStart());
		candidatePoiList.remove(matrix.getTarget());
		
		pathOp = new Path(timeMax);
	}



	/**
	 * M茅todo que comprueba e inserta (en caso de ser posible), si un punto
	 * puede ser insertado en un determinado camino, para ello debe reducir el
	 * coste total del camino.
	 * 
	 * @param path
	 *            camino sobre el que se efect煤a la comprobaci贸n.
	 * @param poiToExchange
	 *            punto a intercambiar
	 * @return si puede efectuarse el intercambio
	 */
	protected boolean isExchangeFeasible(Path path, Poi poiToExchange) {
		double min_cost = 0.0;
		boolean inserted = false;
		int indexToExchange = -1;
		min_cost = timeMax;
		int index;
		for (index = 1; index < path.getPath().size() ; index++) {
			path.add(poiToExchange, index);
			matrix.recalculateCostOfAPath(path);
			inserted = path.isInsertionFeasible(poiToExchange);
			matrix.recalculateCostOfAPath(path);
			if (inserted) {
				if (path.getCost() < min_cost) {
					min_cost = path.getCost();
					indexToExchange = index;
				}
				path.remove(poiToExchange);
				matrix.recalculateCostOfAPath(path);
			}
		}
		if (indexToExchange != -1.0) {
			path.add(poiToExchange, indexToExchange);
			matrix.recalculateCostOfAPath(path);
			return true;
		}
		return false;
	}
	
	public int calculateArrival(Poi sourcePoi, Poi currentPoi){
		//tiempo de llegada a current sera source.arribal + c(s,c)
		int arrival;
		//arrival = sourcePoi.getArrival()+sourcePoi.getServiceTime()+(int)matrix.getMoveCost(sourcePoi, currentPoi);
		//System.out.println("");
		arrival = sourcePoi.getStartTime()+sourcePoi.getServiceTime()+(int)matrix.getMoveCost(sourcePoi, currentPoi);
		return arrival;
	}
	

}
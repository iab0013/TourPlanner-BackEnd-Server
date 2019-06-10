package model;

import java.util.LinkedList;
import java.util.Set;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 * Clase para gestionar la matriz de orígenes/destinos, necesaria para luego
 * poder ejecutar el algoritmo que devolverá la ruta más adecuada.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Matrix {

	private MultiKeyMap matrix;

	private Poi source;

	private Poi target;

	public Poi getSource() {
		return source;
	}

	public void setSource(Poi source) {
		this.source = source;
	}

	public Poi getTarget() {
		return target;
	}

	public void setTarget(Poi target) {
		this.target = target;
	}

	public MultiKeyMap getMatrix() {
		return matrix;
	}

	/**
	 * Constructor de la clase Matrix.
	 * 
	 */
	public Matrix() {
		matrix = new MultiKeyMap();
	}

	/**
	 * Método que añade un poi de origen y un poi destino y el coste asosiado.
	 * 
	 * @param poi_source
	 *            punto de inicio
	 * @param poi_target
	 *            punto de fin
	 * @return coste asociado a dicho par
	 */
	public void addPoi(Poi poi_source, Poi poi_target, double cost) {
		matrix.put(poi_source, poi_target, cost);
	}

	/**
	 * Método que devuelve el coste para un determinado par de POIs
	 * (origen-destino).
	 * 
	 * @param poi_source
	 *            punto de inicio
	 * @param poi_target
	 *            punto de fin
	 * @return coste asociado a dicho par. NULL en caso de no estar almacenado
	 */
	public Double get(Poi poi_source, Poi poi_target) {
		Double cost;
		cost = (Double) matrix.get(poi_source, poi_target);
		return cost;
	}

	@Override
	public String toString() {
		String str_matrix = "";
		MapIterator it = matrix.mapIterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = it.getValue();
			str_matrix += "(POI1-Key1-POI2-Key2-Value): "
					+ ((Poi) ((MultiKey) key).getKey(0)).getPoi_id() + " && "
					+ ((Poi) ((MultiKey) key).getKey(0)).getVertex() + " && "
					+ ((Poi) ((MultiKey) key).getKey(1)).getPoi_id() + " && "
					+ ((Poi) ((MultiKey) key).getKey(1)).getVertex() + " && "
					+ " Value: " + value + "\n";
		}
		return str_matrix;
	}

	/**
	 * Método que recalcula el coste total de un path determinado.
	 * 
	 * @param path
	 *            sobre el que quiere recalcularse el coste
	 */
	public void recalculateCostOfAPath(Path path) {
		double cost = 0.0;
		for (int i = 1; i < path.size(); i++) {
			if (this.get(path.get(i - 1), path.get(i)) != null) {
				cost += this.get(path.get(i - 1), path.get(i));
			}
		}
		path.setCost(cost);
	}

	/**
	 * Método que copia los valores almacenados en la estructura utilizada para
	 * almacenar la matriz de distancias (MultyKeyMap) a un LinkedList.
	 * 
	 * @return lista con todos los valores que estaban almacenados en el
	 *         MultiKeyMap
	 * 
	 */
	public LinkedList<Poi> multikeymapToLinkedList() {
		Set<?> set;
		set = this.getMatrix().keySet();
		LinkedList<Poi> candidatePoiList = new LinkedList<Poi>();
		for (Object keyMap : set.toArray()) {
			if (!candidatePoiList.contains((Poi) ((MultiKey) keyMap).getKey(0))) {
				candidatePoiList.add((Poi) ((MultiKey) keyMap).getKey(0));
			}
			if (!candidatePoiList.contains((Poi) ((MultiKey) keyMap).getKey(1))) {
				candidatePoiList.add((Poi) ((MultiKey) keyMap).getKey(1));
			}
		}
		return candidatePoiList;
	}

	/**
	 * Método que repara las inconsistencias que aparezcan en la generación la
	 * matriz.
	 * 
	 * @param poi
	 *            punto a eliminar de la matrix en orden que la matriz sea
	 *            consistente
	 * 
	 * @return lista con todos los valores que estaban almacenados en el
	 *         MultiKeyMap
	 * 
	 */
	public void repairInconsistencies(Poi poi) {
		Set<?> set;
		set = this.getMatrix().keySet();
		for (Object keyMap : set.toArray()) {
			matrix.remove((Poi) ((MultiKey) keyMap).getKey(0), poi);
		}
	}

	/**
	 * Método que elimina todos los POIs de la matriz. Se eliminarán todos los
	 * POIs que aparezcan como primer término en la matriz.
	 * 
	 * @param poi
	 *            POI que va a eliminarse de la matriz
	 */
	public void removeAllOcurrences(Poi poi) {
		matrix.removeAll(poi);
	}
}

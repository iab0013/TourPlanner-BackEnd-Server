package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import util.Misc;
import util.PoiComparator;
import model.Matrix;
import model.Path;
import model.Poi;

/**
 * Clase que contiene las operaciones necesarias para la creación de rutas,
 * basado en la metodología GRASP y de "Path relinking". Heurística propuesta
 * por Vicente Campos y Rafael Martí.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class Grasp extends Algorithm implements IAlgorithm {

	private LinkedList<Poi> restrictedCandidateList;
	private double factor;
	private final int ITERATION_NUMBER = 10;

	@Override
	public Path execute() {
		return executeAlgorithm();
	}

	/**
	 * Constructor de la clase Grasp.
	 * 
	 * @param matrix
	 *            matriz de distancias
	 */
	public Grasp(Matrix matrix, Double routeTime) {
		super(matrix, routeTime);
		restrictedCandidateList = new LinkedList<Poi>();
	}

	/**
	 * Método que realiza las llamadas a los métodos necesarios para la
	 * inicialización del proceso.
	 * 
	 */
	public void initialize() {
		createCandidateList();
		createRestrictedCandidateList();
		selectAndInsertRandomly();
	}

	/**
	 * Método que ejecuta o incia el algoritmo.
	 * 
	 * @return camino o path resultante de la ejecución del algoritmo
	 */
	public Path executeAlgorithm() {
		Path previousPathOp;
		for (int i = 0; i < ITERATION_NUMBER; i++) {
			setRandomFactor();
			initialize();
			improve();
			if (!paths.contains(pathOp)) {
				paths.add(pathOp);
			}
			cleanVariables();
			initilization();
		}
		setBestRelinkedPath(paths);
		previousPathOp = (Path) pathOp.clone();
		pathRelinking();
		if (previousPathOp.getScore() < pathOp.getScore()) {
			return pathOp;
		} else {
			return previousPathOp;
		}
	}

	/**
	 * Método que genera un "factor" aleatorio entre un par de valores
	 * pre-establecidos.
	 * 
	 */
	private void setRandomFactor() {
		factor = (double) Misc.generateRandomNumber(7, 10) / 10;
	}

	/**
	 * Método que reinicia las variables, de modo que puedan realizarse nuevas
	 * iteraciones.
	 * 
	 */
	private void cleanVariables() {
		pathOp = new Path(timeMax);
		restrictedCandidateList = new LinkedList<Poi>();
	}

	/**
	 * Método que intenta mejorar los paths o itinerarios generados durante la
	 * fase de mejora (improve).
	 * 
	 */
	private void pathRelinking() {
		Path currentPath, previousPath;
		ArrayList<Path> relinkedPaths = new ArrayList<Path>();
		for (int i = 1; i < paths.size(); i++) {
			previousPath = paths.get(i - 1);
			currentPath = paths.get(i);
			relinkedPaths.add(relinkPaths(currentPath, previousPath));
			relinkedPaths.add(relinkPaths(previousPath, currentPath));
		}
		setBestRelinkedPath(relinkedPaths);
	}

	/**
	 * Método que selecciona el path con mayor score a partir de un conjunto de
	 * paths.
	 * 
	 * @param relinkedPaths
	 *            conjunto de paths
	 */
	private void setBestRelinkedPath(ArrayList<Path> relinkedPaths) {
		double bestScore = 0;
		for (Path path : relinkedPaths) {
			if (path.getScore() > bestScore) {
				bestScore = path.getScore();
				pathOp = path;
			}
		}

	}

	/**
	 * Método que contiene toda la mecánica del proceso de "relinkado".
	 * Intentamos mejorar un path con los POIs presentes en otro path.
	 * 
	 * @param currentPath
	 *            path de partida
	 * @param previousPath
	 *            path que va a potenciarse o mejorar
	 */
	private Path relinkPaths(Path currentPath, Path previousPath) {
		Path copyOfpreviousPath, pathPQ, pathQP, copyOfPathPQ, intermediateSolution;
		copyOfpreviousPath = (Path) previousPath.clone();
		intermediateSolution = (Path) copyOfpreviousPath.clone();
		pathPQ = removeVertexFromPath(previousPath, currentPath);
		pathQP = removeVertexFromPath(currentPath, previousPath);
		Collections.sort(pathPQ.getPath(), new PoiComparator<Poi>());
		Collections.sort(pathQP.getPath(),
				Collections.reverseOrder(new PoiComparator<Poi>()));
		copyOfPathPQ = (Path) pathPQ.clone();
		for (Poi poi : pathQP.getPath()) {
			while (!isExchangeFeasible(copyOfpreviousPath, poi)
					&& pathPQ.size() > 0) {
				Poi currentPoi = pathPQ.get(0);
				pathPQ.getPath().remove(0);
				copyOfpreviousPath.remove(currentPoi);
				matrix.recalculateCostOfAPath(copyOfpreviousPath);
			}
			if (!copyOfpreviousPath.getPath().contains(poi)) {
				pathPQ = (Path) copyOfPathPQ.clone();
				copyOfpreviousPath = (Path) intermediateSolution.clone();
			} else {
				copyOfPathPQ = (Path) pathPQ.clone();
				intermediateSolution = (Path) copyOfpreviousPath.clone();
			}

		}
		return copyOfpreviousPath;
	}

	/**
	 * Método que elimina todos los puntos de un path que están contenidos en el
	 * otro path.
	 * 
	 * @param currentPath
	 *            path del que van a eliminarse puntos que ya están en
	 *            previousPath
	 * @param previousPath
	 *            path que contiene los puntos a eliminar de currentPath
	 */
	private Path removeVertexFromPath(Path currentPath, Path previousPath) {
		Path copyOfcurrentPath, copyOfPreviousPath;
		copyOfcurrentPath = (Path) currentPath.clone();
		copyOfPreviousPath = (Path) previousPath.clone();
		for (Poi poi : copyOfPreviousPath.getPath()) {
			copyOfcurrentPath.remove(poi);
		}
		return copyOfcurrentPath;
	}

	/**
	 * Método que trata de mejorar la solución (pathOp).
	 * 
	 */
	private void improve() {
		exchangeBetweenVertex();
		oneToOneExchange();
	}

	/**
	 * Método que recorre todos los candidatos y busca la mejor posición para
	 * realizar el intercambio.
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void exchangeBetweenVertex() {
		LinkedList<Poi> copyOfcandidatePoiList = (LinkedList<Poi>) candidatePoiList
				.clone();
		for (int i = 0; i < candidatePoiList.size(); i++) {
			PoiExchange poiExchange = new PoiExchange();
			poiExchange.setPoiToExchange(candidatePoiList.get(i));
			obtainBestExchange(poiExchange);
			if (poiExchange.getBestPoiToExchange() != null) {
				pathOp.add(
						poiExchange.poiToExchange,
						pathOp.getPath().indexOf(
								poiExchange.getBestPoiToExchange()));
				pathOp.remove(poiExchange.getBestPoiToExchange());
				matrix.recalculateCostOfAPath(pathOp);
				copyOfcandidatePoiList.remove(poiExchange.poiToExchange);
				copyOfcandidatePoiList.add(poiExchange.getBestPoiToExchange());
			}
		}
		candidatePoiList = copyOfcandidatePoiList;
	}

	/**
	 * Método que, para un determinado POI, busca y devuelve por referencia los
	 * datos relativos a los datos de dicho intercambio (POI con el que
	 * intercambia, coste, etc.).
	 * 
	 * @param poiExchange almacena los datos del intercambio.
	 * 
	 */
	private void obtainBestExchange(PoiExchange poiExchange) {
		double currentMoveValue = 0;
		poiExchange.setSmallerCost(pathOp.getCost());
		boolean scoreImproved = false, poiRemoved = false;
		for (int i = 1; i < pathOp.size() - 1; i++) {
			Poi currentPoi = pathOp.get(i);
			currentMoveValue = poiExchange.poiToExchange.getScore()
					- currentPoi.getScore();
			if (currentMoveValue >= poiExchange.getBestMoveValue()) {
				pathOp.remove(currentPoi);
				pathOp.add(poiExchange.getPoiToExchange(), i);
				matrix.recalculateCostOfAPath(pathOp);
				if (pathOp.isInsertionFeasible(poiExchange.getPoiToExchange())) {
					if (currentPoi.getScore() == poiExchange.getPoiToExchange()
							.getScore() && !scoreImproved) {
						if (pathOp.getCost() < poiExchange.getSmallerCost()) {
							poiExchange.setSmallerCost(pathOp.getCost());
							poiRemoved = true;
						}
					} else {
						scoreImproved = true;
						poiRemoved = true;
					}
				}
				if (poiRemoved) {
					poiExchange.setBestMoveValue(currentMoveValue);
					poiExchange.setBestPoiToExchange(currentPoi);
					pathOp.remove(currentPoi);
					poiRemoved = false;
				}
				pathOp.add(currentPoi, i);
				pathOp.remove(poiExchange.getPoiToExchange());
				matrix.recalculateCostOfAPath(pathOp);
			}
		}

	}

	/**
	 * Método que intenta insertar puntos en pathOp a partir de la lista de
	 * candidatos.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void oneToOneExchange() {
		LinkedList<Poi> copyOfcandidatePoiList = (LinkedList<Poi>) candidatePoiList
				.clone();
		for (Poi poi : copyOfcandidatePoiList) {
			if (isExchangeFeasible(pathOp, poi)) {
				candidatePoiList.remove(poi);
			}
		}

	}

	/**
	 * Método que selecciona (aleatoriamente) de la lista de candidatos
	 * restringida e intenta insertar en el pathOp.
	 * 
	 */
	private void selectAndInsertRandomly() {
		int insertIndex, consideredPois = 0, size;
		size = restrictedCandidateList.size();
		while (restrictedCandidateList.size() > 0 && consideredPois < size) {
			insertIndex = Misc.generateRandomNumber(0,
					restrictedCandidateList.size());
			if (consideredPois == 0) {
				pathOp.add(restrictedCandidateList.get(insertIndex), 1);
				candidatePoiList.remove(restrictedCandidateList
						.get(insertIndex));
				restrictedCandidateList.remove(insertIndex);
				matrix.recalculateCostOfAPath(pathOp);
			} else {
				if (isExchangeFeasible(pathOp,
						restrictedCandidateList.get(insertIndex))) {
					candidatePoiList.remove(restrictedCandidateList
							.get(insertIndex));
					restrictedCandidateList.remove(insertIndex);
				}
			}
			consideredPois++;
		}

	}

	/**
	 * Método que crea la lista de candidatos restringida. Contendrá aquellos
	 * puntos mayores que un determinado umbral.
	 * 
	 */
	private void createRestrictedCandidateList() {
		double maxScore;
		double threshold;
		maxScore = getMaxScoreFromCandidateList();
		threshold = maxScore * factor;
		for (Poi poi : candidatePoiList) {
			if (poi.getScore() >= threshold) {
				restrictedCandidateList.add(poi);
			}
		}
	}

	/**
	 * Método que extrae el mayor score almacenado en la lista de candidatos.
	 * 
	 * @return mayor score de la lista de candidatos
	 */
	private double getMaxScoreFromCandidateList() {
		double maxScore = 0;
		for (Poi poi : candidatePoiList) {
			if (poi.getScore() > maxScore) {
				maxScore = poi.getScore();
			}
		}
		return maxScore;
	}

	/**
	 * Método que crea la lista de candidatos. En esta estarán todos los puntos
	 * que sean factibles para el tiempo dado.
	 * 
	 */
	private void createCandidateList() {
		pathOp.getPath().addFirst(source_poi);
		pathOp.getPath().addLast(target_poi);
		LinkedList<Poi> candidateList = new LinkedList<Poi>();
		for (Poi poi : candidatePoiList) {
			pathOp.add(poi, 1);
			matrix.recalculateCostOfAPath(pathOp);
			if (pathOp.isInsertionFeasible(poi)) {
				pathOp.remove(poi);
				candidateList.add(poi);
			} 
		}
		candidatePoiList = candidateList;
	}

	/**
	 * Clase interna para almacenar la información a los puntos de interés en el
	 * momento de realizar intercambios.
	 * 
	 * @author Inigo Vázquez - Roberto Villuela
	 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
	 */
	private class PoiExchange {

		private Poi poiToExchange;
		private double bestMoveValue;
		private Poi bestPoiToExchange;
		private double smallerCost;

		public double getSmallerCost() {
			return smallerCost;
		}

		public void setSmallerCost(double smallerCost) {
			this.smallerCost = smallerCost;
		}

		public PoiExchange() {
			bestMoveValue = 0;
		}

		public Poi getPoiToExchange() {
			return poiToExchange;
		}

		public void setPoiToExchange(Poi poiToExchange) {
			this.poiToExchange = poiToExchange;
		}

		public double getBestMoveValue() {
			return bestMoveValue;
		}

		public void setBestMoveValue(double currentMoveValue) {
			this.bestMoveValue = currentMoveValue;
		}

		public Poi getBestPoiToExchange() {
			return bestPoiToExchange;
		}

		public void setBestPoiToExchange(Poi bestPoiToExchange) {
			this.bestPoiToExchange = bestPoiToExchange;
		}

	}

}

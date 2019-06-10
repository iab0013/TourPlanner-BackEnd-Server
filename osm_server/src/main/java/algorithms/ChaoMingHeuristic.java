package algorithms;

import java.util.ArrayList;
import java.util.LinkedList;

import util.Misc;
import model.Matrix;
import model.Path;
import model.Poi;

/**
 * Clase que contiene las operaciones necesarias para la creación de rutas,
 * basado en el algoritmo propuesto por I-Ming Chao, Bruce L. Golden y Edward *
 * A. Wasil.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class ChaoMingHeuristic extends Algorithm implements IAlgorithm {

	private int deviation;

	/**
	 * Mejor score registrado.
	 */
	private double record;

	/**
	 * Objeto para almacenar el mejor path.
	 */
	private Path bestPath;

	/**
	 * Variable para controlar el número de iteraciones que realizará el
	 * algoritmo.
	 */
	private final int ITERATIONNUMBER = 2;

	@Override
	public Path execute() {
		return executeAlgorithm();
	}

	/**
	 * Constructor de la clase ChaoMingHeuristic.
	 * 
	 * @param matrix
	 *            matriz de distancias
	 */
	public ChaoMingHeuristic(Matrix matrix, Double routeTime) {
		super(matrix, routeTime);
		record = 0;
		deviation = 0;
	}

	/**
	 * Método que ejecuta o incia el algoritmo.
	 * 
	 * @return camino o path resultante de la ejecución del algoritmo
	 */
	public Path executeAlgorithm() {
		if (initialize()) {
			updatePathOp();
			bestPath = (Path) pathOp.clone();
			record = pathOp.getScore();
			deviation = 10;
			improvement();
			if (bestPath.getScore() > pathOp.getScore()) {
				return bestPath;
			} else {
				return pathOp;
			}
		} else {
			return null;
		}
	}

	/**
	 * Método que tiene como objetivo intentar acortar la longitud del Path OP
	 * con la esperanza que quepan más puntos que puedan incrementar el score
	 * total.
	 * 
	 */
	private void cleanUp() {
		Path copy_pathOp = null;
		boolean isExchanged = false;
		copy_pathOp = (Path) pathOp.clone();
		for (int index = 1; index < pathOp.getPath().size() - 1; index++) {
			Poi currentPoi = pathOp.get(index);
			copy_pathOp.remove(currentPoi);
			matrix.recalculateCostOfAPath(copy_pathOp);
			isExchanged = isExchangeFeasible(copy_pathOp, currentPoi);
			if (!isExchanged) {
				copy_pathOp.add(currentPoi, index);
			}
			if (copy_pathOp.getCost() > pathOp.getCost()) {
				copy_pathOp.remove(currentPoi);
				copy_pathOp.add(currentPoi, index);
			}
			matrix.recalculateCostOfAPath(copy_pathOp);
		}
		if (copy_pathOp.getCost() < pathOp.getCost()) {
			pathOp = (Path) copy_pathOp.clone();
		}
	}

	/**
	 * Método que tiene como objetivo mejorar en la medida de lo posible la
	 * solución. Desde este método se realizarán las llamadas a los diferentes
	 * métodos que intentan mejorar la solución.
	 * 
	 */
	private void improvement() {
		boolean isMovedTwo = false, isMovedOne = false;
		int poiExtract = ((int) (pathOp.getPath().size() * 0.1));
		int poisToRemove = poiExtract > 0 ? poiExtract : 1;
		int thresold = pathOp.getPath().size() * 2;
		int initialThresold = thresold;
		for (int k = 1; k <= ITERATIONNUMBER; k++) {
			for (int i = 0; i < poisToRemove; i++) {
				if (thresold == 0) {
					return;
				}
				thresold--;
				isMovedTwo = twoPointsExchange();
				updatePathOp();
				isMovedOne = onePointExchange();
				updatePathOp();
				cleanUp();
				if (!isMovedOne && !isMovedTwo) {
					break;
				}
				if (pathOp.getScore() > record) {
					record = pathOp.getScore();
				}
				checkIfPathWasImproved(initialThresold, thresold);
			}
			if (k != ITERATIONNUMBER) {
				reinicialization(k);
				isMovedTwo = false;
				isMovedOne = false;
			}
			checkIfPathWasImproved(initialThresold, thresold);
		}
		if (deviation == 10) {
			deviation = 5;
			improvement();
		}
		checkIfPathWasImproved(initialThresold, thresold);
	}

	/**
	 * Método que comprueba si el pathOp mejora el score más alto obtenido en
	 * los paths candidatos.
	 * 
	 * @param initialThresold umbral inicial
	 * @param thresold umbral
	 * 
	 */
	protected void checkIfPathWasImproved(int initialThresold, int thresold) {
		if (pathOp.getScore() > bestPath.getScore()) {
			bestPath = (Path) pathOp.clone();
			thresold = initialThresold;
		}
	}

	/**
	 * Método para buscar incrementar el score total, para ello se eliminan una
	 * serie de puntos del path OP, concretamente aquellos con menor ratio
	 * (score/cost).
	 * 
	 * @param k
	 *            número de POIs a eliminar del pathOp
	 */
	private void reinicialization(int k) {
		LinkedList<PoiRatio> listPoiRatio = new LinkedList<PoiRatio>();
		for (int index = 1; index < pathOp.getPath().size() - 1; index++) {
			Poi currentPoi = pathOp.get(index);
			PoiRatio currentPoiRatio = new PoiRatio();
			currentPoiRatio.setPoi(currentPoi);
			setPoiRatio(currentPoiRatio, index);
			if (listPoiRatio.size() == 0) {
				listPoiRatio.add(currentPoiRatio);
			} else {
				insertOrderedPoiRatio(listPoiRatio, currentPoiRatio);
			}
		}
		createNewPath(listPoiRatio.get(0).getPoi());
		pathOp.remove(listPoiRatio.get(0).getPoi());
		matrix.recalculateCostOfAPath(pathOp);
		for (int i = 1; i < k; i++) {
			pathOp.remove(listPoiRatio.get(i).getPoi());
			matrix.recalculateCostOfAPath(pathOp);
			isExchangeFeasible(paths.get(paths.size() - 1), listPoiRatio.get(i)
					.getPoi());
		}

	}

	/**
	 * Método que inserta un determinado elemento PoiRatio, en una lista de
	 * forma ordenada, en función de su ratio.
	 * 
	 * @param listPoiRatio
	 *            lista de POIs ordenados de acuerdo a su ratio
	 * @param currentPoiRatio
	 *            POI que quiere insertarse
	 */
	private void insertOrderedPoiRatio(LinkedList<PoiRatio> listPoiRatio,
			PoiRatio currentPoiRatio) {
		boolean isInserted = false;
		for (int i = 0; i < listPoiRatio.size() && !isInserted; i++) {
			if (currentPoiRatio.getRatio() < listPoiRatio.get(i).getRatio()) {
				listPoiRatio.add(i, currentPoiRatio);
				isInserted = true;
			}
		}
		if (!isInserted) {
			listPoiRatio.addLast(currentPoiRatio);
		}

	}

	/**
	 * Método que calcula el ratio de un determinado POI.
	 * 
	 * @param currentPoiRatio
	 *            POI sobre el que quiere calcularse el ratio
	 * @param index
	 *            posición en la que se encuentra el POI
	 */
	private void setPoiRatio(PoiRatio currentPoiRatio, int index) {
		Poi previousPoi, nextPoi;
		double ratio = 0.0, costInsertion;
		previousPoi = pathOp.get(index - 1);
		nextPoi = pathOp.get(index + 1);
		costInsertion = (matrix.get(previousPoi, currentPoiRatio.getPoi())
				+ matrix.get(currentPoiRatio.getPoi(), nextPoi) - matrix.get(
				previousPoi, nextPoi));
		ratio = currentPoiRatio.getPoi().getScore() / costInsertion;
		currentPoiRatio.setRatio(ratio);
	}

	/**
	 * Método que efectúa el proceso relativo al intercambio de dos puntos entre
	 * el camino óptimo y los no óptimos. En el artículo que describe dicho
	 * procedimiento, este proceso se denomina "two points exchange".
	 * 
	 * @return booleano que indica si se ha realizado algún intercambio
	 */
	private boolean twoPointsExchange() {
		Path copy_pathOp = null, copy_pathOp2 = null;
		boolean isNopToOpOk = false, scoreIncreased = false, moved = false;
		double scoreOp = 0;
		double bestExchange = 0;
		int indexBestExchangePoi = -1, indexBestExchangePath = -1;
		double costBestExchange = 0.0;
		copy_pathOp = (Path) pathOp.clone();
		int size = paths.size();
		for (int poiOp = 1; poiOp < pathOp.getPath().size() - 1; poiOp++) {
			Poi currentPoiOp;
			bestExchange = 0;
			copy_pathOp2 = (Path) copy_pathOp.clone();
			scoreOp = copy_pathOp.getScore();
			currentPoiOp = pathOp.get(poiOp);
			scoreIncreased = false;
			isNopToOpOk = false;
			for (int pathIndex = 0; pathIndex < size && !scoreIncreased; pathIndex++) {
				copy_pathOp.remove(currentPoiOp);
				matrix.recalculateCostOfAPath(copy_pathOp);
				Path pathNop = paths.get(pathIndex);
				for (int poiIndex = 1; poiIndex < pathNop.getPath().size() - 1
						&& !scoreIncreased; poiIndex++) {
					copy_pathOp.remove(currentPoiOp);
					matrix.recalculateCostOfAPath(copy_pathOp);
					Poi poiNop = pathNop.get(poiIndex);
					isNopToOpOk = isExchangeFeasible(copy_pathOp, poiNop);
					if (isNopToOpOk) {
						if (copy_pathOp.getScore() > scoreOp) {
							scoreIncreased = true;
							moved = true;
							pathNop.remove(poiNop);
							matrix.recalculateCostOfAPath(pathNop);
							insertPoiOrCreateNewPath(currentPoiOp, pathNop);
						} else {
							if (copy_pathOp.getScore() > bestExchange) {
								indexBestExchangePath = pathIndex;
								indexBestExchangePoi = poiIndex;
								bestExchange = copy_pathOp.getScore();
								costBestExchange = copy_pathOp.getCost();
							}
							copy_pathOp = (Path) copy_pathOp2.clone();
						}
					}
				}
			}
			if (!scoreIncreased) {
				if ((record * (100 - deviation) / 100 < bestExchange)
						&& (costBestExchange < copy_pathOp.getCost())) {
					copy_pathOp.remove(currentPoiOp);
					matrix.recalculateCostOfAPath(copy_pathOp);
					isExchangeFeasible(
							copy_pathOp,
							paths.get(indexBestExchangePath).get(
									indexBestExchangePoi));
					paths.get(indexBestExchangePath).getPath()
							.remove(indexBestExchangePoi);
					insertPoiOrCreateNewPath(currentPoiOp,
							paths.get(indexBestExchangePath));
					moved = true;
				} else {
					copy_pathOp = (Path) copy_pathOp2.clone();
				}
			}
		}
		paths.add(copy_pathOp);
		return moved;
	}

	/**
	 * Método que dados un poi y un path, inserta dicho poi en el path dado o en
	 * caso de no ser factible, crea un nuevo path con ese punto.
	 * 
	 * @param poi
	 *            poi a insertar
	 * @param path
	 *            path sobre el que se va a insertar (en caso de ser factible)
	 */
	private void insertPoiOrCreateNewPath(Poi poi, Path path) {
		boolean isOpToNopOk;
		isOpToNopOk = isExchangeFeasible(path, poi);
		if (!isOpToNopOk) {
			createNewPath(poi);
			if (path.getPath().size() == 2) {
				paths.remove(path);
			}
		}
	}

	/**
	 * Método que efectúa el proceso relativo al movimienteo de un punto de un
	 * determinado camino a otro. En el artículo que describe dicho
	 * procedimiento, este proceso se denomina "one point exchange".
	 * 
	 * @return booleano que indica si se ha realizado alguna inserción
	 */
	@SuppressWarnings("unchecked")
	private boolean onePointExchange() {
		LinkedList<Poi> copyOfcandidatePoiList;
		ArrayList<Path> copyOfPaths;
		boolean moved = false;
		double costBestExchange = 0.0;
		copyOfPaths = (ArrayList<Path>) paths.clone();
		copyOfPaths.add(pathOp);
		double bestExchange = 0;
		int indexBestExchangePath = 0;
		boolean scoreIncreased = false;
		copyOfcandidatePoiList = obtainCandidatesFromSetOfPaths(copyOfPaths);
		for (int candidateIndex = 0; candidateIndex < copyOfcandidatePoiList
				.size(); candidateIndex++) {
			Poi currentPoi;
			bestExchange = 0;
			scoreIncreased = false;
			currentPoi = copyOfcandidatePoiList.get(candidateIndex);
			for (int pathIndex = 0; pathIndex < copyOfPaths.size()
					&& !scoreIncreased; pathIndex++) {
				Path currentPath = copyOfPaths.get(pathIndex);
				Path copyOfCurrentPath = (Path) currentPath.clone();
				if (!currentPath.contains(currentPoi)) {
					if (isExchangeFeasible(currentPath, currentPoi)) {
						copyOfPaths.remove(currentPath);
						if (currentPath.getScore() > obtainPathPoi(copyOfPaths,
								currentPoi).getScore()) {
							moved = true;
							int indexPathRemoved;
							int addIndexPath = pathIndex;
							indexPathRemoved = removePathPoi(copyOfPaths,
									currentPoi);
							if (indexPathRemoved != -1) {
								if (indexPathRemoved < pathIndex) {
									addIndexPath = addIndexPath - 1;
								}
							}
							copyOfPaths.add(addIndexPath, currentPath);
							scoreIncreased = true;
						} else {
							double scoreBeforeRemove = currentPath.getScore();
							costBestExchange = currentPath.getCost();
							currentPath.remove(currentPoi);
							matrix.recalculateCostOfAPath(currentPath);
							copyOfPaths.add(pathIndex, currentPath);
							if (scoreBeforeRemove > bestExchange) {
								indexBestExchangePath = pathIndex;
								bestExchange = scoreBeforeRemove;
							}
							currentPath = (Path) copyOfCurrentPath.clone();
						}

					}
				}
			}
			if (!scoreIncreased) {
				if ((record * (100 - deviation) / 100 < bestExchange)
						&& (costBestExchange < copyOfPaths.get(
								indexBestExchangePath).getCost())) {
					isExchangeFeasible(copyOfPaths.get(indexBestExchangePath),
							currentPoi);
					moved = true;
					removePathPoi(copyOfPaths, currentPoi);
				}
			}
		}
		paths = (ArrayList<Path>) copyOfPaths.clone();
		return moved;
	}

	/**
	 * Método que a partir de una copia de los paths almacenados, genera un
	 * LinkedList con los POIs candidatos.
	 * 
	 * @return LinkedList con los POIs candidatos
	 */
	private LinkedList<Poi> obtainCandidatesFromSetOfPaths(
			ArrayList<Path> copyOfPaths) {
		LinkedList<Poi> candidatePois = new LinkedList<Poi>();
		for (Path path : copyOfPaths) {
			for (int i = 1; i < path.getPath().size() - 1; i++) {
				candidatePois.add(path.get(i));
			}
		}
		return candidatePois;
	}

	/**
	 * Método que dados un poi y un conjunto de caminos, nos devuelve el camino
	 * al que pertenece dicho poi.
	 * 
	 * @param copyOfPaths
	 *            conjunto de caminos
	 * @param poi
	 *            poi del que quiere obtenerse el camino
	 * @return el camino al que pertenece el poi pasado como parámetro
	 */
	private Path obtainPathPoi(ArrayList<Path> copyOfPaths, Poi poiToObtain) {
		for (Path path : copyOfPaths) {
			if (path.contains(poiToObtain)) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Método que elimina un punto de un determinado path, para ello ha de
	 * buscar en una estructura que contiene varios paths.
	 * 
	 * @param copyOfPaths
	 *            conjunto de paths
	 * @param poiToRemove
	 *            poi a eliminar
	 * @return índice del Poi eliminado del Path
	 */
	private int removePathPoi(ArrayList<Path> copyOfPaths, Poi poiToRemove) {
		int index = 0;
		for (Path path : copyOfPaths) {
			if (path.contains(poiToRemove)) {
				path.remove(poiToRemove);
				matrix.recalculateCostOfAPath(path);
				// si se quedan solo los puntos de inicio-fin, elminamos el path
				if (path.getPath().size() == 2) {
					copyOfPaths.remove(path);
					return index;
				}
				break;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Método que crea un nuevo camino, utilizando para ello un nodo dado y los
	 * nodos de inicio-fin.
	 * 
	 * @param poi
	 *            punto comprendido entre el inicio y fin de la ruta
	 */
	private void createNewPath(Poi poi) {
		Path path = new Path(timeMax);
		path.add(target_poi, 0);
		path.add(poi, 0);
		path.add(source_poi, 0);
		matrix.recalculateCostOfAPath(path);
		path.recalculateScore();
		paths.add(path);
	}

	/**
	 * Método que inicializa un conjunto de caminos a partir de los puntos de
	 * interés candidatos (POIs).
	 * 
	 * @return si la inicialización se ha realizado correctamente
	 * 
	 */
	private boolean initialize() {
		boolean furtherPoiInserted = false, furtherPoiEverInserted = false;
		while (candidatePoiList.size() > 0) {
			Path path = new Path(timeMax);
			path.add(source_poi, 0);
			path.add(target_poi, path.getPath().size());
			matrix.recalculateCostOfAPath(path);
			if (path.isInsertionFeasible(target_poi)) {
				furtherPoiInserted = insertFurtherPoi(path);
				if (furtherPoiInserted) {
					furtherPoiEverInserted = true;
					greedyInsertionsIntoPath(path);
					paths.add(path);
				} else {
					break;
				}
			} else {
				return false;
			}
		}
		if (!furtherPoiEverInserted) {
			return false;
		}
		return true;
	}

	/**
	 * Establece o actualiza el PathOp (camino con mayor "score").
	 * 
	 */
	private void updatePathOp() {
		double score = 0;
		double max_score = 0;
		for (Path candidatePath : paths) {
			score = candidatePath.getScore();
			if (score > max_score) {
				pathOp = candidatePath;
				max_score = score;
			}
		}
		paths.remove(pathOp);
	}

	/**
	 * Inserta puntos de la "forma más sencilla" dentro de un camino (siempre
	 * que sea factible).
	 * 
	 * @param path
	 *            camino sobre el que se insertan los puntos
	 */
	@SuppressWarnings("unchecked")
	private void greedyInsertionsIntoPath(Path path) {
		LinkedList<Poi> candidatePoiListCopy = new LinkedList<Poi>();
		candidatePoiListCopy = ((LinkedList<Poi>) candidatePoiList.clone());
		boolean inserted = false;
		int poiToInsert = -1;
		while (candidatePoiListCopy.size() > 0 && !inserted) {
			poiToInsert = Misc.generateRandomNumber(0,
					candidatePoiListCopy.size() - 1);
			Poi poi = candidatePoiListCopy.get(poiToInsert);
			path.add(poi, path.getPath().size() - 1);
			matrix.recalculateCostOfAPath(path);
			inserted = path.isInsertionFeasible(poi);
			if (inserted) {
				candidatePoiListCopy.remove(poi);
			} else {
				candidatePoiListCopy.remove(poi);
			}
			matrix.recalculateCostOfAPath(path);

		}
		candidatePoiList.removeAll(path.getPath());
	}

	/**
	 * Método que busca el punto más lejano a los puntos de inicio y fin y le
	 * inserta.
	 * 
	 * @param path
	 *            camino sobre el que se inserta el punto
	 * @return booleano que indica si se ha insertado el punto más lejano
	 */
	@SuppressWarnings("unchecked")
	private boolean insertFurtherPoi(Path path) {
		Poi fartherPoi = null;
		double cost = 0.0, max_cost = 0.0;
		boolean inserted = false;
		LinkedList<Poi> candidatePoiListCopy = new LinkedList<Poi>();
		candidatePoiListCopy = (LinkedList<Poi>) candidatePoiList.clone();
		while (!inserted && candidatePoiListCopy.size() > 0) {
			for (Poi poi : candidatePoiListCopy) {
				cost = matrix.get(poi, source_poi)
						+ matrix.get(poi, target_poi);
				if (cost > max_cost) {
					max_cost = cost;
					fartherPoi = poi;
				}
			}
			max_cost = 0.0;
			path.add(fartherPoi, path.size() - 1);
			matrix.recalculateCostOfAPath(path);
			inserted = path.isInsertionFeasible(fartherPoi);
			candidatePoiListCopy.remove(fartherPoi);
		}
		if (inserted) {
			candidatePoiList.remove(fartherPoi);
			return true;
		}
		return false;
	}

	/**
	 * Clase interna para almacenar los ratios asociados a un determinado POI.
	 * 
	 * @author Inigo Vázquez - Roberto Villuela
	 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
	 */
	private class PoiRatio {

		private Poi poi;
		private double ratio;

		public Poi getPoi() {
			return poi;
		}

		public void setPoi(Poi poi) {
			this.poi = poi;
		}

		public double getRatio() {
			return ratio;
		}

		public void setRatio(double ratio) {
			this.ratio = ratio;
		}

	}

}

package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import model.Matrix;
import model.Path;
import model.Poi;

public class antColonySystem extends Algorithm implements IAlgorithm{
	
	private static final int NB_MAX = 10;
	private static final int PHEROMONE_INIT = 1;
	private double alpha = 1;
	private double beta = 5;
	private double evaporation = 0.5;
	private double antFactor = 0.8;
	private int maxAnts;
	private int nNodes;
	
	private Matrix pheromoneMatrix = new Matrix();
	private Map<Long, ArrayList<Poi>> neighbourMap;
	
	
	public Path execute(){
		return executeAlgorithm();
	}
	
	public Path executeAlgorithm(){
		
		initialize();
		
		return pathOp;
	}
	
	public antColonySystem(Matrix matrix, Double routeTime){
		super(matrix,routeTime);
	}
	
	public void initialize(){
		maxAnts = (int) (nNodes*antFactor);
		neighbourMap = new HashMap<Long, ArrayList<Poi>>();
		loadPheromoneMatrix();
		loadNeighbourMap();
	}
	
	public void cleanVariables(){
	}
	
	public void constructSolution(){
		
	}
	
	public void localPheromoneUpdate(){
		
	}
	
	public void swap(){
		
	}
	
	public void insert(){
		
	}
	
	public void replace(){
		
	}
	
	public void globalPheromoneUpdate(){
		
	}
	
	/**
	 * Calcula un set ordenado por ratio de posibles vecinos para cada nodo. <currentNodeId, neighbourList>
	 */
	public void loadNeighbourMap(){
		LinkedList<Poi> poiList= matrix.multikeymapToLinkedList();
		ArrayList<Double> sortedRewardRatio;
		ArrayList<Poi> newSet;
		int position;
		double rewardRatio;
		int nbCont;
		
		poiList.remove(matrix.getTarget());
		
		for(Poi poi:poiList){
			newSet= new ArrayList<Poi>();
			sortedRewardRatio = new ArrayList<Double>();
			nbCont = 0;
			for(Poi neighbour:candidatePoiList){
				if(!poi.equals(neighbour) && nbCont<NB_MAX){
					if(poi.getOpeningTime()+poi.getServiceTime()+matrix.getMoveCost(poi, neighbour)<=neighbour.getClosingTime()){
						rewardRatio = calculateRewardRatio(poi, neighbour);
						position = calculatePositionRewardRatio(sortedRewardRatio, rewardRatio);
						if(position == -1){
							newSet.add(neighbour);
						}else{
							newSet.add(position, neighbour);
						}
						nbCont++;
					}
				}
			}
			neighbourMap.put(poi.getPoi_id(), newSet);
		}
	}
	
	public double calculateRewardRatio(Poi currentPoi, Poi neighbour){
		double ratio=0;
		ratio = neighbour.getScore()/(neighbour.getServiceTime()+matrix.getMoveCost(currentPoi, neighbour));
		return ratio;
	}
	
	/**
	 * Calcula la mejor posicion para un vecino en funcion de su ratio de recompensa.
	 * @param list
	 * @param ratio
	 * @return
	 */
	public int calculatePositionRewardRatio(ArrayList<Double> list, double ratio){
		int i;
		boolean fBreak = false;
		
		if(list.size()==0){
			list.add(ratio);
			i = -1;
		}else{
			for(i = 0; i<list.size() && fBreak == false;i++){
				if(list.get(i)<=ratio){
					list.add(i,ratio);
					fBreak = true;
				}
			}
			if(fBreak==false && (i == list.size()-1)){
				list.add(ratio);
				i=-1;
			}
		}
		return i;
	}
	
	/**
	 * Carga matrizes de feromonas.
	 */
	public void loadPheromoneMatrix(){
		LinkedList<Poi> poiList= matrix.multikeymapToLinkedList();
		int greedValue;
		for(int i = 0;i<poiList.size();i++){
			for(int j = 0; j<poiList.size();j++){
				pheromoneMatrix.addPoi(poiList.get(i), poiList.get(j), PHEROMONE_INIT);
			}
		}	
	}
	
	/**
	 * Calcula greedy information de un arco.
	 * @param currentPoi
	 * @param targetPoi
	 * @return
	 */
	public double calculateGreed(Poi currentPoi, Poi targetPoi){
		double greed;
		greed = targetPoi.getScore()/matrix.getMoveCost(currentPoi, targetPoi);
		return greed;
	}
	
	private void printCandidate(){
		System.out.println();
		System.out.println("Candidate List: ");
		for(int i = 0;i<candidatePoiList.size();i++){
			System.out.print(candidatePoiList.get(i).getPoi_id()+" ");
		}
	}
	
	private void printMap(){
		int i;
		System.out.println();
		for(Entry<Long, ArrayList<Poi>> poi:neighbourMap.entrySet()){
			i=0;
			long id = poi.getKey();
			System.out.println("ID: "+id);
			
			for(Poi neighbour:poi.getValue()){
				System.out.print(" n"+i+" id: "+neighbour.getPoi_id()+",");
				i++;
			}
			System.out.println();
		}
	}
	
}

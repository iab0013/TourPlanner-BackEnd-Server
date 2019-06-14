package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import colony.Ant;

import java.util.Map.Entry;

import model.Matrix;
import model.Path;
import model.Poi;

public class antColonySystem extends Algorithm implements IAlgorithm{
	
	private static final int NB_MAX = 45;
	private static final int ITERATION_MAX = 500;
	private static final int NO_IMPRO_MAX = 10;
	private static final int MAX_ANTS = 10;
	private static final int PHEROMONE_INIT = 1;
	private double alpha = 1;
	private double beta = 5;
	private double evaporation = 0.5;
	private double antFactor = 0.8;
	private int nNodes;
	
	private Matrix pheromones = new Matrix();
	private Map<Poi, ArrayList<Poi>> neighbourMap;
	
	private ArrayList<Poi> elegibleNeighbours;
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	
	
	public Path execute(){
		return executeAlgorithm();
	}
	
	public Path executeAlgorithm(){
		int iteration = 0;
		int noImpro = 0;
		double globalBestScore = 0;
		Path globalBestAnt;
		initialize();
		
		//Create maxAnts
		IntStream.range(0, MAX_ANTS)
        .forEach(i -> ants.add(new Ant(timeMax,matrix)));
		
		globalBestAnt = ants.get(0).getAntPath();
		
		while(iteration<ITERATION_MAX){
			System.out.println("Iteration "+iteration+" / "+ITERATION_MAX);
			//Construct solutions for ants
			for(Ant ant:ants){
				ant.clear();
				//System.out.println("hormiga");
				constructSolution(ant);
				ant.getAntPath().setMaxShiftFromLast();
			}
			//Local moves
			System.out.println("+Local moves");
			for(Ant ant:ants){
				//printPathDatesM(ant.getAntPath());
				ant.totalSwap();
				ant.totalInsert();
				ant.totalReplace();
			}
			System.out.println("-Local moves");
			//get ant with best score
			Path localBestAnt = getBestAnt();
			
			if(localBestAnt.getScore()>globalBestScore){
				globalBestScore = localBestAnt.getScore();
				globalBestAnt = localBestAnt;
				noImpro = 0;
			}else{
				noImpro++;
			}
			
			//Global pheromone update
			if(noImpro<NO_IMPRO_MAX){
				globalPheromoneUpdate(globalBestAnt);
			}else{
				loadPheromoneMatrix();
			}
			
			iteration++;
		}
		
		System.out.println("Iteration "+iteration+" / "+ITERATION_MAX);
		printPathDatesM(globalBestAnt);
		System.out.println("Score "+globalBestAnt.getScore());
		return pathOp;
	}
	
	public antColonySystem(Matrix matrix, Double routeTime){
		super(matrix,routeTime);
	}
	
	private void printCandidate(){
		System.out.println();
		System.out.println("Candidate List: ");
		for(int i = 0;i<candidatePoiList.size();i++){
			System.out.print(candidatePoiList.get(i).getPoi_id()+" ");
		}
	}
	
	private void printNeiMap(){
		int i;
		System.out.println();
		for(Entry<Poi, ArrayList<Poi>> poi:neighbourMap.entrySet()){
			i=0;
			System.out.println("ID: "+poi.getKey().getPoi_id());
			
			for(Poi neighbour:poi.getValue()){
				System.out.print(" n"+i+" id: "+neighbour.getPoi_id()+",");
				i++;
			}
			System.out.println();
		}
	}
	
	public void printPathDatesM(Path pathOp){
		System.out.println();
		for(Poi poi:pathOp.getPath()){
			System.out.println("Id: "+poi.getPoi_id()+" arrival: "+poi.getArrival()+" start: "+poi.getStartTime()+" opening: "+poi.getOpeningTime()+" closing: "+poi.getClosingTime()+" wait: "+poi.getWait()+" shift: "+poi.getShift()+" maxShift: "+poi.getMaxShift());
		}
		System.out.println();
		System.out.println();
	}
	
	public void initialize(){
		//maxAnts = (int) (nNodes*antFactor);
		neighbourMap = new HashMap<Poi, ArrayList<Poi>>();
		loadPheromoneMatrix();
		loadNeighbourMap();
	}
	
	public double calculateTMax(Poi poi){
		//total time - movecost current to end poi
		//departure time + movecost(poi,end)
		double costToEnd = matrix.getMoveCost(poi, matrix.getTarget())+matrix.getTarget().getServiceTime()+poi.getServiceTime();
		//System.out.println("Calculating new tMax for vertex "+poi.getPoi_id()+" : "+timeMax+" - "+matrix.getMoveCost(poi, matrix.getTarget())+" - "+matrix.getTarget().getServiceTime());
		return timeMax+startTime-costToEnd;
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
			neighbourMap.put(poi, newSet);
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
		for(int i = 0;i<poiList.size();i++){
			for(int j = 0; j<poiList.size();j++){
				pheromones.addPoi(poiList.get(i), poiList.get(j), PHEROMONE_INIT);
			}
		}	
	}
	
	public void constructSolution(Ant ant){
		ant.setNeighbourMap(neighbourMap);
		generateElegibleNeighbours(ant);
		//System.out.println("Constructing solution");
		while(!elegibleNeighbours.isEmpty()){
			//System.out.println("Elegible has something");
			//add new vertex
			calculateProb(ant);
			Poi selectedPoi = selectRoulette(ant);
			//System.out.println("SelectedPoi: "+selectedPoi.getPoi_id());
			
			localPheromoneUpdate(ant, selectedPoi);
			ant.getAntPath().add((Poi)selectedPoi.clone(), ant.getAntPath().size());
			
			//calcular tiempos del nuevo poi IMPORTANTE
			ant.calculateNewTimes(ant.getAntPath().get(ant.getAntPath().size()-2), selectedPoi);
			
			elegibleNeighbours.remove(selectedPoi);
			generateElegibleNeighbours(ant);
		}
		addEndVertex(ant);
	}
	
	public void generateElegibleNeighbours(Ant ant){
		elegibleNeighbours=new ArrayList<Poi>();
		ArrayList<Poi> possibleNeighbours;
		Poi currentPoi = ant.getAntPath().get(ant.getAntPath().size()-1);
		possibleNeighbours = neighbourMap.get(currentPoi);
		//System.out.println("PossibleN size: "+possibleNeighbours.size());
		for(Poi possiblePoi : possibleNeighbours){
			if(!ant.getAntPath().contains(possiblePoi)){
				//System.out.println("Calculating elegible from possible");
				double departureTime = currentPoi.getStartTime()+currentPoi.getServiceTime();
				double arrivalTime = departureTime+matrix.getMoveCost(currentPoi, possiblePoi);
				//double tMax = timeMax-matrix.getMoveCost(currentPoi, possiblePoi);
				double tMax = calculateTMax(possiblePoi);
				//System.out.println("arrival: "+arrivalTime+" <= closing: "+possiblePoi.getClosingTime()+" && arr<=tmax"+tMax);
				//System.out.println("other arrival: "+calculateArrival(currentPoi, possiblePoi));
				if(arrivalTime<=possiblePoi.getClosingTime() && arrivalTime<=tMax){
					//System.out.println("ADDING ELEGIBLE+++");
					elegibleNeighbours.add(possiblePoi);
				}
			}
		}
	}
	
	public void calculateProb(Ant ant){
		initializeProbability(ant);
		Poi currentPoi = getCurrent(ant);
		
		//if arrival before opening probability reduces
		for(Poi poi:elegibleNeighbours){
			double departureTime = currentPoi.getStartTime()+currentPoi.getServiceTime();
			double arrivalTime = departureTime+matrix.getMoveCost(currentPoi, poi);
			double tMax = calculateTMax(poi);
			if(arrivalTime<poi.getOpeningTime()){
				double newProb = 1-((poi.getOpeningTime()-arrivalTime)/tMax);
				ant.getProbabilities().addPoi(currentPoi, poi, newProb);
			}
		}
		updateProb(ant);
	}
	
	/**
	 * Inicializa todas las probabilidades a 1.
	 */
	public void initializeProbability(Ant ant){
		Poi currentPoi = getCurrent(ant);
		for(Poi poi:elegibleNeighbours){
			ant.getProbabilities().addPoi(currentPoi, poi, 1);
		}
	}
	
	public void updateProb(Ant ant){
		Poi currentPoi = getCurrent(ant);
		for(Poi poi:elegibleNeighbours){
			double newProb = calculateUpdateValue(ant, poi)/sumProb(ant);
			ant.getProbabilities().addPoi(currentPoi, poi, newProb);
		}
	}
	
	public double sumProb(Ant ant){
		double sumProb = 0;
		for(Poi poi:elegibleNeighbours){
			sumProb+=calculateUpdateValue(ant, poi);
		}
		return sumProb;
	}
	
	public double calculateUpdateValue(Ant ant, Poi poi){
		Poi currentPoi = getCurrent(ant);
		double pheroAlpha;
		double greedBeta;
		//li*phero^alpha*greedy^B
		pheroAlpha = Math.pow(pheromones.get(currentPoi, poi), alpha);
		greedBeta = Math.pow(calculateGreed(ant, poi), beta);
		return pheroAlpha*greedBeta*ant.getProbabilities().get(currentPoi, poi);
	}
	
	public double calculateGreed(Ant ant, Poi poi){
		return poi.getScore()/matrix.getMoveCost(getCurrent(ant), poi);
		
	}
	
	public Poi getCurrent(Ant ant){
		return ant.getAntPath().get(ant.getAntPath().size()-1);
	}
	
	public Poi selectRoulette(Ant ant){
		Poi current = getCurrent(ant);
		Poi selected = elegibleNeighbours.get(0);
		double total = ant.getProbabilities().get(current, selected);
		Random rnd = new Random();
		
		
		for(int i = 1; i<elegibleNeighbours.size();i++){
			total += ant.getProbabilities().get(current, elegibleNeighbours.get(i));
			if(rnd.nextDouble() <= (ant.getProbabilities().get(current, elegibleNeighbours.get(i))/total)){
				selected = elegibleNeighbours.get(i);
			}
		}
		return selected;
	}
	
	public void localPheromoneUpdate(Ant ant, Poi poi){
		Poi currentPoi = getCurrent(ant);
		double newValue = pheromones.get(currentPoi, poi)*(1-evaporation);
		pheromones.addPoi(currentPoi, poi, newValue);
	}
	
	public void addEndVertex(Ant ant){
		ant.getAntPath().add((Poi)matrix.getTarget().clone(), ant.getAntPath().size());
		Poi beforeLast = ant.getAntPath().get(ant.getAntPath().size()-2);
		Poi lastVertex = ant.getAntPath().get(ant.getAntPath().size()-1);
		ant.calculateNewTimes(beforeLast, lastVertex);
	}
	
	public Path getBestAnt(){
		Ant best = ants.get(0);
		double bestScore = 0;
		for(Ant ant:ants){
			double score = ant.getAntPath().getScore();
			if(score>bestScore){
				best=ant;
				bestScore=ant.getAntPath().getScore();
			}
		}
		return best.getAntPath();
	}
	
	public void globalPheromoneUpdate(Path path){
		for(int i = 0; i<path.size()-1;i++){
			Poi source = path.get(i);
			Poi target = path.get(i+1);
			double pheromone = pheromones.get(source, target);
			pheromones.addPoi(source, target, pheromone+PHEROMONE_INIT);
		}
	}
	
	
	
}

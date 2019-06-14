package colony;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import model.Matrix;
import model.Path;
import model.Poi;

public class Ant {
	
	private double timeLimit;
	private Path antPath;
	private Matrix probabilities;
	private Matrix matrix;
	private Map<Poi, ArrayList<Poi>> neighbourMap;
	
	
	public Ant(double timeLimit, Matrix matrix){
		this.timeLimit=timeLimit;
		this.matrix=matrix;
		initialize();
	}
	
	public void initialize(){
		probabilities = new Matrix();
		clear();
	}
	
	public void clear(){
		antPath = new Path(timeLimit);
		antPath.add(matrix.getSource(), 0);
	}
	
	public void setAntPath(Path antPath) {
		this.antPath = antPath;
	}
	
	public Path getAntPath() {
		return antPath;
	}
	
	public void setNeighbourMap(Map<Poi, ArrayList<Poi>> neighbourMap) {
		this.neighbourMap = neighbourMap;
	}
	
	public Map<Poi, ArrayList<Poi>> getNeighbourMap() {
		return neighbourMap;
	}
	
	public Matrix getProbabilities() {
		return probabilities;
	}
	
	
	
	public void calculateNewTimes(Poi source, Poi current){
		calculateNewTimesPath(antPath, source, current);	
	}
	
	public void calculateNewTimesPath(Path path, Poi source, Poi current){
		
		int indexCurrent = path.getPath().indexOf(current);
		
		int arrival = calculateArrival(path.get(path.getPath().indexOf(source)), path.get(indexCurrent));
		path.get(indexCurrent).setArrival(arrival);
		
		int wait = path.get(indexCurrent).calculateWait();
		path.get(indexCurrent).setWait(wait);
		
		int start = path.get(indexCurrent).calculateStart();
		path.get(indexCurrent).setStartTime(start);
		//System.out.println("arrival "+arrival+" wait "+wait+" start "+start);
	}
	
	public int calculateArrival(Poi sourcePoi, Poi currentPoi){
		//tiempo de llegada a current sera source.arribal + c(s,c)
		int arrival;
		//arrival = sourcePoi.getStartTime()+sourcePoi.getServiceTime()+(int)matrix.getMoveCost(sourcePoi, currentPoi);
		arrival = calculateArrivalArgs(sourcePoi,currentPoi,sourcePoi.getStartTime());
		return arrival;
	}
	
	public int calculateArrivalArgs(Poi sourcePoi, Poi currentPoi, int start){
		int arrival;
		arrival = start+sourcePoi.getServiceTime()+(int)matrix.getMoveCost(sourcePoi, currentPoi);
		return arrival;
	}
	
	
	
	public boolean swapVertex(Poi firstPoi, Poi secondPoi){
		//comprobar que los vecinos de first son de second y los de second son de first.
		
		System.out.println("**SwapVertex "+firstPoi.getPoi_id()+" "+secondPoi.getPoi_id());
		
		Poi fLeftNei,fRightNei,sRightNei,sLeftNei;
		ArrayList<Integer> startTwFlag;
		boolean swapped = false;
		
		if(!firstPoi.equals(matrix.getSource()) && !secondPoi.equals(matrix.getTarget())){
			//System.out.println("Not first or last OK");
			fLeftNei = antPath.getPrevious(firstPoi);
			fRightNei = antPath.getNext(firstPoi);
			sLeftNei = antPath.getPrevious(secondPoi);
			sRightNei = antPath.getNext(secondPoi);
			if(isNeighbour(secondPoi, fLeftNei)&&isNeighbour(secondPoi, fRightNei)
					&&isNeighbour(firstPoi, sLeftNei)&&isNeighbour(firstPoi, sRightNei)
					|| (fLeftNei.equals(matrix.getSource()) && sRightNei.equals(matrix.getTarget())
							&& isNeighbour(firstPoi, sLeftNei) && isNeighbour(secondPoi, fRightNei))){
				
				//System.out.println("Neis Ok");
				//temporal exchange vertex else, pair discarded
				Path tempPath = (Path) antPath.clone();
				int fPoiIndex = tempPath.getPath().indexOf(firstPoi);
				int sPoiIndex = tempPath.getPath().indexOf(secondPoi);
				
				//System.out.println("Antpath node: "+antPath.get(sPoiIndex).getPoi_id()+" start: "+antPath.get(sPoiIndex).getStartTime());
				
				tempPath.remove(firstPoi);
				tempPath.remove(secondPoi);
				tempPath.add(secondPoi, fPoiIndex);
				tempPath.add(firstPoi, sPoiIndex);
				
				//calculate arrivals between swapped vertex. Care about time windows
				//get(0) da start, get(1) da 1 si todo ok, 0 si no respeta tiempos
				startTwFlag = temporalBetweenVertex(tempPath, tempPath.get(fPoiIndex), tempPath.get(sPoiIndex));
				int twFlag = startTwFlag.get(1);
				int lastStart = startTwFlag.get(0);
				int lastPoiStart = startTwFlag.get(2);
				
				//Calculate time travel diference
				//System.out.println("tempPath: "+lastStart+" antPath: "+antPath.get(sPoiIndex).getStartTime()+" flag: "+twFlag);
				
				if(twFlag==1 && lastStart<antPath.get(sPoiIndex).getStartTime() 
						&& lastPoiStart<antPath.get(antPath.size()-1).getStartTime()){
					
					System.out.println("swap OK. lastStart: "+lastStart+" currentStart: "+antPath.get(sPoiIndex).getStartTime());
					for(int i = 0;i<antPath.size();i++){
						System.out.print(antPath.get(i).getPoi_id()+" ");
					}
					
					antPath=tempPath;
					
					System.out.println();
					for(int i = 0;i<antPath.size();i++){
						System.out.print(antPath.get(i).getPoi_id()+" ");
					}
					//Update arrivals from firstSwap to end
					for(int i = fPoiIndex;i<antPath.size();i++){
						//System.out.println("Updating node "+antPath.get(i).getPoi_id());
						calculateNewTimesPath(antPath, antPath.get(i-1), antPath.get(i));
					}
					
					//Recalculate maxshift from y to start
					antPath.setMaxShiftFromLast();
					swapped=true;
				}
			}
		}
		return swapped;
	}
	
	public boolean isNeighbour(Poi source, Poi target){
		if(neighbourMap.get(source).contains(target)){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean checkTimeWindows(Poi poi,int startTime){
		if(startTime+poi.getServiceTime()<=poi.getClosingTime()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Genera valores arrival, start, wait para el camino X--Z y devuelve el start en Z
	 * @param path
	 * @param source
	 * @param target
	 * @return
	 */
	public ArrayList<Integer> temporalBetweenVertex(Path path, Poi source, Poi target){
		ArrayList<Integer> newValues;
		int fPoiIndex = path.getPath().indexOf(source);
		int sPoiIndex = path.getPath().indexOf(target);
		int start = path.get(fPoiIndex-1).getStartTime();
		int targetStart = path.get(fPoiIndex-1).getStartTime();
		int lastPoiStart = path.get(path.size()-1).getStartTime();
		int flagTw = 1;
		//System.out.println("Calculando temporals");
		//System.out.println("Start en nodo "+path.get(fPoiIndex-1).getPoi_id()+" : "+start);
		//for(int i = fPoiIndex;i<=sPoiIndex&&flagTw==1;i++){
		for(int i = fPoiIndex;i<path.size()&&flagTw==1;i++){
			newValues = temporalTimeValues(path,path.get(i-1),path.get(i),start);
			start = newValues.get(2);
			flagTw = newValues.get(3);
			if(i==sPoiIndex){
				targetStart = start;
			}
			if(i==path.size()-1){
				lastPoiStart = start;
			}
			//System.out.println("Nuevo start en nodo "+path.get(i).getPoi_id()+" : "+start);
		}
		return new ArrayList<Integer>(Arrays.asList(targetStart,flagTw,lastPoiStart));
	}
	
	/**
	 * Genera valores arrival, start, wait y devuelve arrayList con ellos.
	 * @param path
	 * @param source
	 * @param current
	 * @param startTime
	 * @return
	 */
	public ArrayList<Integer> temporalTimeValues(Path path, Poi source, Poi current, int startTime){
		int indexCurrent = path.getPath().indexOf(current);
		int flagTw = 0;
		int arrival = calculateArrivalArgs(path.get(path.getPath().indexOf(source)), path.get(indexCurrent),startTime);
		int wait = path.get(indexCurrent).calculateWaitArgs(arrival);
		int start = path.get(indexCurrent).calculateStartArgs(arrival, wait);
		
		if(checkTimeWindows(current, start)){
			flagTw = 1;
		}
		//System.out.println("--temp arri: "+arrival);
		//System.out.println("--temp wait: "+wait);
		//System.out.println("--temp start: "+start);
		return new ArrayList<Integer>(Arrays.asList(arrival,wait,start,flagTw));
	}
	
	/**
	 * Try swapping all vertex combination until no more are feasible
	 */
	public void totalSwap(){
		System.out.println("***totalSwap");
		boolean swapped = false;
		//recorremos sin tener en cuenta el primero y el ultimo
		for(int i = 1;i<antPath.size()-1;i++){
			for(int j = 1; j<antPath.size()-1;j++){
				//System.out.println("TrySwap "+i+" -- "+j);
				if(i!=j){
					//System.out.println("**Swapping "+i+"--"+j);
					swapped = swapVertex(antPath.get(i), antPath.get(j));
					if(swapped){
						i=1;
						j=1;
					}
				}
			}
		}
	}
	
	
	public void totalInsert(){
		boolean keepInserting = true;
		while(keepInserting){
			keepInserting=insert();
		}
	}
	
	public boolean insert(){
		boolean inserted = false;
		ArrayList<Object> insertVertexList = calculateBestInsert();
		if(!insertVertexList.isEmpty()){
			inserted = true;
			//System.out.println("+INSERTING");
			Poi insertingVertex = (Poi)insertVertexList.get(0);
			int bestSourceIndex = (int)insertVertexList.get(1);
			boolean shiftFlag = false;
			antPath.add(insertingVertex, bestSourceIndex+1);
			//recalculamos valores
			for(int i =bestSourceIndex+1;i<antPath.size();i++){
				calculateNewTimesPath(antPath, antPath.get(i-1), antPath.get(i));
			}
			antPath.setMaxShiftFromLast();
		}
		return inserted;
	}
	
	public ArrayList<Object> calculateBestInsert(){
		//System.out.println("STARTING BEST INSERT");
		//intenta meter vecinos no incluidos en el path
		Poi bestVertex;
		int bestSourceIndex;
		double bestRatio=0;
		
		ArrayList<Object> returnList = new ArrayList<Object>();
		for(int i = 0;i<antPath.size()-1;i++){
			double bestPositionShift = Double.POSITIVE_INFINITY;
			Poi bestClone = (Poi)neighbourMap.get(antPath.get(i)).get(0);
			boolean flag = false;
			for(Poi testingPoi:neighbourMap.get(antPath.get(i))){
				if(!antPath.contains(testingPoi)){
					Poi testingClone = (Poi)testingPoi.clone();
					testingClone.setArrival(calculateArrival(antPath.get(i), testingClone));
					testingClone.setWait(testingClone.calculateWait());
					
					if(isInsertionFeasible(antPath.get(i), antPath.get(i+1), testingClone)){
						//COMPROBAR, NO ESTA ENTRANDO
						int shift = calculateShift(antPath.get(i), antPath.get(i+1), testingClone);
						if(shift<bestPositionShift){
							bestPositionShift=shift;
							bestClone = testingClone;
							flag=true;
//							//System.out.println("New localBestSource: "+localBestPositionSource);
						}
					}
				}
			}
			if(flag==true){
				double auxRatio = getInsertRatio(antPath.get(i), antPath.get(i+1), bestClone);
				if(auxRatio>bestRatio){
					bestRatio=auxRatio;
					bestVertex=bestClone;
					bestSourceIndex=i;
					returnList = new ArrayList<Object>(Arrays.asList(bestVertex,bestSourceIndex));
					
					////System.out.println("CalculateBestIndex bestShift: "+" del vertice "+bestVertex.getPoi_id()+" detras de "+antPath.getPath().get(bestSourceIndex).getPoi_id());
				}
			}
		}
		return returnList;
	}
	
	public boolean isInsertionFeasible(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
//		//System.out.println("InsertionFeasible --> arrival: "+calculateArrival(sourcePoi, currentPoi)+" opening: "+currentPoi.getOpeningTime() + " closing: "+currentPoi.getClosingTime());
		int auxNewShift = calculateShift(sourcePoi,targetPoi,currentPoi);
		if(auxNewShift<=(targetPoi.getWait()+targetPoi.getMaxShift())){
			if(calculateArrival(sourcePoi,currentPoi)+currentPoi.getServiceTime()<=currentPoi.getClosingTime() && targetPoi.getMaxShift()-auxNewShift>=0){
				return true;
			}else{
//				//System.out.println("***not feasible");
				return false;
			}
		}else{
//			//System.out.println("***not feasible");
			return false;
		}
	}
	
	
	public int calculateShift(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
		int shift = 0;
		int originalCost, newToTargetCost, newFromSourceCost;
		
		newFromSourceCost = (int) matrix.getMoveCost(sourcePoi, currentPoi);
		newToTargetCost = (int) matrix.getMoveCost(currentPoi, targetPoi);
		originalCost = ((int) matrix.getMoveCost(sourcePoi, targetPoi));
		
		////System.out.println("Shift "+currentPoi.getPoi_id()+" = "+toHours(newFromSourceCost)+" + "+toHours(currentPoi.getWait())+" + "+toHours(currentPoi.getServiceTime())+" + "+toHours(newToTargetCost)+" - "+toHours(originalCost));
		
		shift = newFromSourceCost+currentPoi.getWait()+currentPoi.getServiceTime()+newToTargetCost-originalCost;
		//shift = originalCost+targetPoi.getWait()+targetPoi.getServiceTime()+newToTargetCost-newFromSourceCost;
		
		return shift;
	}
	
	public double getInsertRatio(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
		return Math.pow(currentPoi.getScore(), 2)/calculateShift(sourcePoi, targetPoi, currentPoi);
	}
	
	public boolean canReplace(Poi current, Poi possible){
		////System.out.println("TESTINIG CANREPLACE");
		int currentIndex = antPath.getPath().indexOf(current);
		////System.out.println("currentIndex "+currentIndex);
		if(neighbourMap.get(possible).contains(antPath.get(currentIndex-1)) 
				&& neighbourMap.get(possible).contains(antPath.get(currentIndex+1))
				&& !antPath.contains(possible)){
			////System.out.println("cumple");
			return true;
		}else{
			////System.out.println("no cumple");
			return false;
		}
	}
	
	public void totalReplace(){
		////System.out.println("STARTING TOTAL REPLACE");
		//for(Poi current:antPath.getPath()){
		for(int i = 1;i<antPath.size()-1;i++){
			if(antPath.get(i)!=antPath.get(antPath.size()-1)&&antPath.get(i)!=antPath.get(0)){
				for(Poi possible:neighbourMap.get(antPath.get(i))){
					////System.out.println("testing "+antPath.get(i).getPoi_id()+" -- "+possible.getPoi_id());
					
					if(possible.getScore()>antPath.get(i).getScore() 
							&& canReplace(antPath.get(i), possible)){
						////System.out.println("CAN REPLACE");
						replace(antPath.get(i),possible);
					}
				}	
			}
		}
	}
	
	public void replace(Poi current, Poi possible){
		Poi leftNei = antPath.get(antPath.getPath().indexOf(current)-1);
		Poi rightNei = antPath.get(antPath.getPath().indexOf(current)+1);
		double oldTime;
		double newTime;
		//oldTime = matrix.getMoveCost(leftNei, current)+matrix.getMoveCost(current, rightNei);
		//newTime = matrix.getMoveCost(leftNei, possible)+matrix.getMoveCost(possible, rightNei);
		int shiftNew = calculateShift(leftNei, rightNei, possible);
		//maxshift tiene que aumentar en el shift de meter el nodo que vamos a quitar
		int shiftOld = calculateShift(leftNei, rightNei,current);
		
		Path testingRemove = (Path)antPath.clone();
		testingRemove.remove(current);
		testingRemove.setMaxShiftFromLast();
		
		
		//if(shiftNew<=rightNei.getMaxShift()+shiftOld){
		if(shiftNew<=testingRemove.get(testingRemove.getPath().indexOf(rightNei)).getMaxShift()){
			int replaceIndex = antPath.getPath().indexOf(current);
			antPath.remove(current);
			antPath.add((Poi)possible.clone(), replaceIndex);
			
			//update
			for(int i =replaceIndex;i<antPath.size();i++){
				calculateNewTimesPath(antPath, antPath.get(i-1), antPath.get(i));
			}
			antPath.setMaxShiftFromLast();
		}
	}
	
	
	
	

	
	
	
}

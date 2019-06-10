package algorithms;

import java.util.ArrayList;
import java.util.Arrays;

import model.Matrix;
import model.Path;
import model.Poi;

public class Hybrid extends Algorithm implements IAlgorithm{
	
	/**
	 * 
	 * @return
	 */
	public Path execute(){
		return executeAlgorithm();
	}
	
	/**
	 * 
	 * @return
	 */
	private Path executeAlgorithm() {

		Path previousPathOp;
		int R,S,numberOfTimeImprovements,n,m;//n = number of locations   m=number of routes
		m=1;
		R=1;
		S=1;
		numberOfTimeImprovements=0;
		previousPathOp = (Path) pathOp.clone();
		initialize();
		n = candidatePoiList.size();
		n=18;
		
		while(numberOfTimeImprovements<150){
			printPathDatesM(pathOp);
			System.out.println();
			System.out.println("                                      ^^ CALCULANDO NUEVO PATH");
			calculateLocalOptimum();
			pathOp.recalculateScore();
			
			if(pathOp.getScore()>previousPathOp.getScore()){
				System.out.println("Nuevo path es mejor +++");
				previousPathOp = (Path) pathOp.clone();
				R=1;
				numberOfTimeImprovements=0;
			}else{
				System.out.println("Path anterior es mejor ---");
				numberOfTimeImprovements++;
			}
			shakePath(R, S);
			S = S+R;
			R++;
			System.out.println("///// COMPROBANDO VALORES S ---- S: "+S+" path.s :"+pathOp.size()+" prepath.s: "+previousPathOp.size());
			
			if(pathOp.size()<previousPathOp.size()){
				if(S>=pathOp.size()){
					S = S-pathOp.size();
				}
			}else{
				if(S>=previousPathOp.size()){
					S = S-previousPathOp.size();
				}
			}
			System.out.println("////// Nuevo valor de S: "+S);
			if(S==0){
				S++;
			}
			if(R==(n/(3*m))){
				R=1;
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("                    MEJOR PATH");
		printPathDates(previousPathOp);
		printPathDatesM(previousPathOp);
		return previousPathOp;
		
		
//		initialize();
//		calculateLocalOptimum();
//		
//		shakePath(2, 3);
//		System.out.println("arrival nodo 19: "+toHours(pathOp.getPath().getLast().getArrival()));
//
//		printPathDatesM(pathOp);
//		printPath(pathOp);
//		printCandidate();
//		//System.out.println("Path copia size: "+previousPathOp.size());
//		return pathOp;
	}
	
	public void initialize(){
		createCandidateList();
	}
	
	/**
	 * Constructor de la clase Hybrid
	 */
	public Hybrid(Matrix matrix ,Double routeTime){
		super(matrix,routeTime);
	}
	
	private void cleanVariables(){
		pathOp = new Path(timeMax);
	}
	
	private void createCandidateList(){
		//todos los nodos menos el inicial, que se mete directamente al mejor path
		//en candidatePoiList
		pathOp.getPath().addFirst(source_poi);
		pathOp.getPath().addLast(target_poi);
		setMaxShift(target_poi);
		setMaxShift(source_poi);
	}
	
	private void calculateLocalOptimum(){
		boolean flag =false;
		int lastCandidatesSize=candidatePoiList.size();
		printPathDatesM(pathOp);
		while(flag == false){
			insert();
			if(lastCandidatesSize==candidatePoiList.size() || candidatePoiList.size()==0){
				flag=true;
			}else{
				lastCandidatesSize=candidatePoiList.size();
			}
		}
	}
	
	private Poi shortestDistance(Poi current){
		Poi shortest;
		shortest = candidatePoiList.getFirst();
		for(Poi neighbor : candidatePoiList){
			if(!(neighbor.equals(current))){
				if(matrix.get(current, neighbor)<(matrix.get(current, shortest))){
					shortest=neighbor;
				}
			}
		}
		return shortest;
	}
	
	private void printPath(Path pathOp){
		System.out.println();
		System.out.println("PATH Target: "+matrix.getTarget().getPoi_id());
		for(int i = 0;i<pathOp.size();i++){
			System.out.print(pathOp.get(i).getPoi_id()+" ");
		}
	}
	
	private void printCandidate(){
		System.out.println();
		System.out.println("Candidate List: ");
		for(int i = 0;i<candidatePoiList.size();i++){
			System.out.print(candidatePoiList.get(i).getPoi_id()+" ");
		}
	}
	
	public void setMaxShift(Poi poi){
		if(pathOp.contains(poi)){
			for(int i = 0; i<pathOp.size();i++){
				if(pathOp.get(i).equals(poi)){
					pathOp.get(i).setMaxShift(pathOp.calculateMaxShift(poi));
				}
			}	
		}
	}
	
	public int calculateShift(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
		int shift = 0;
		int originalCost, newToTargetCost, newFromSourceCost;
		
		newFromSourceCost = (int) matrix.getMoveCost(sourcePoi, currentPoi);
		newToTargetCost = (int) matrix.getMoveCost(currentPoi, targetPoi);
		originalCost = ((int) matrix.getMoveCost(sourcePoi, targetPoi));
		
		//System.out.println("Shift "+currentPoi.getPoi_id()+" = "+toHours(newFromSourceCost)+" + "+toHours(currentPoi.getWait())+" + "+toHours(currentPoi.getServiceTime())+" + "+toHours(newToTargetCost)+" - "+toHours(originalCost));
		
		shift = newFromSourceCost+currentPoi.getWait()+currentPoi.getServiceTime()+newToTargetCost-originalCost;
		//shift = originalCost+targetPoi.getWait()+targetPoi.getServiceTime()+newToTargetCost-newFromSourceCost;
		
		return shift;
	}
	
	/**
	 * Actualiza el valor wait de un vertice.
	 * @param currentPoi
	 */
	public void updateWait(Poi currentPoi){
		//System.out.println("Dentro updateWait");//calcular shift de j(estamos en k) por tanto j es previous.previous
		int previousPoiShift = calculateShift(pathOp.getPrevious(pathOp.getPrevious(currentPoi)), currentPoi, pathOp.getPrevious(currentPoi));
		//System.out.println("previousPoiShift calculado");
		int newWait = Math.max(0, (currentPoi.getWait()-previousPoiShift));
		//System.out.println("newWait calculado");
		currentPoi.setWait(newWait);
	}
	
	/**
	 * Actualiza el arrival de un vertice.
	 * @param currentPoi
	 */
	public void updateArrival(Poi currentPoi){
		int newArrival =  currentPoi.getArrival() + pathOp.getPrevious(currentPoi).getShift();
		currentPoi.setArrival(newArrival);
	}
	
	/**
	 * Actualiza shift de un vertice.
	 * Usar despues de insertar un vertice para actualizar a los que esten detras.
	 * @param currentPoi
	 */
	public void updateShift(Poi currentPoi, int oldWait){
//		int newShift = Math.max(0, (pathOp.getPrevious(currentPoi).getShift()-currentPoi.getWait()));
//		System.out.println("PreviousShift = "+pathOp.getPrevious(currentPoi).getShift());
//		System.out.println("currentWait = "+currentPoi.getWait());
//		System.out.println("newShift = "+(pathOp.getPrevious(currentPoi).getShift()-currentPoi.getWait()));
//		System.out.println("Updating Shift "+currentPoi.getPoi_id()+" : max[0, "+pathOp.getPrevious(currentPoi).getShift()+" - "+currentPoi.getPoi_id()+" = "+newShift);
		int newShift = Math.max(0, (pathOp.getPrevious(currentPoi).getShift()-oldWait));
		currentPoi.setShift(newShift);
	}
	
	/**
	 * Actualiza el startTime de un vertice.
	 * @param currentPoi
	 */
	public void updateStartTime(Poi currentPoi){
		int newStartTime = currentPoi.getStartTime()+currentPoi.getShift();
		currentPoi.setStartTime(newStartTime);
	}
	
	/**
	 * Actualiza el maxShift de un vertice.
	 * @param currentPoi
	 */
	public void updateMaxShift(Poi currentPoi){
		int newMaxShift = currentPoi.getMaxShift()-currentPoi.getShift();
		currentPoi.setMaxShift(newMaxShift);
	}
	
	public boolean isInsertionFeasible(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
//		System.out.println("InsertionFeasible --> arrival: "+calculateArrival(sourcePoi, currentPoi)+" opening: "+currentPoi.getOpeningTime() + " closing: "+currentPoi.getClosingTime());
		int auxNewShift = calculateShift(sourcePoi,targetPoi,currentPoi);
		if(auxNewShift<=(targetPoi.getWait()+targetPoi.getMaxShift())){
			if(calculateArrival(sourcePoi,currentPoi)+currentPoi.getServiceTime()<=currentPoi.getClosingTime() && targetPoi.getMaxShift()-auxNewShift>=0){
				return true;
			}else{
//				System.out.println("***not feasible");
				return false;
			}
		}else{
//			System.out.println("***not feasible");
			return false;
		}
	}
	
	public double getInsertRatio(Poi sourcePoi, Poi targetPoi, Poi currentPoi){
		return Math.pow(currentPoi.getScore(), 2)/calculateShift(sourcePoi, targetPoi, currentPoi);
	}
	
	
	public void insert(){
		ArrayList<Object> insertVertexList = calculateBestInsert();
		
		if(!insertVertexList.isEmpty()){
			Poi insertingVertex = (Poi)insertVertexList.get(0);
			int bestSourceIndex = (int)insertVertexList.get(1);
			boolean shiftFlag = false;
			
			//Insert best visit && calculate arrive start wait shift.
			
			pathOp.add(insertingVertex, bestSourceIndex+1);
			candidatePoiList.remove(insertingVertex);
			
			System.out.println("Modificando valores de "+pathOp.getPath().get(bestSourceIndex+1).getPoi_id());
			
			calculateNewTimeValues(bestSourceIndex+1);
//			int arrival = calculateArrival(pathOp.get(bestSourceIndex), pathOp.get(bestSourceIndex+1));
//			pathOp.get(bestSourceIndex+1).setArrival(arrival);
//			
//			int wait = pathOp.get(bestSourceIndex+1).calculateWait();
//			pathOp.get(bestSourceIndex+1).setWait(wait);
//			
//			int start = pathOp.get(bestSourceIndex+1).calculateStart();
//			pathOp.get(bestSourceIndex+1).setStartTime(start);
//			
//			int shift = calculateShift(pathOp.get(bestSourceIndex), pathOp.get(bestSourceIndex+2), pathOp.get(bestSourceIndex+1));
//			pathOp.get(bestSourceIndex+1).setShift(shift);
			
			
			System.out.println("+Insertando vertice "+insertingVertex.getPoi_id()+" antes de "+pathOp.getNext(insertingVertex).getPoi_id());
			System.out.println("++Shift de "+insertingVertex.getPoi_id()+" : "+pathOp.get(bestSourceIndex+1).getShift());
			System.out.println("++Wait de "+pathOp.getNext(insertingVertex).getPoi_id()+" : "+pathOp.getNext(insertingVertex).getWait());
			System.out.println("++maxShift de "+pathOp.getNext(insertingVertex).getPoi_id()+" : "+pathOp.getNext(insertingVertex).getMaxShift());
			
			if(isInsertionFeasible(pathOp.getPrevious(insertingVertex), pathOp.getNext(insertingVertex), insertingVertex)){
				System.out.println("+++Insertion es correcta");
			}else{
				System.out.println("---Insertion es falsa");
			}
			
			
			
			//For each visit after new visit (until shift = 0) update arrive start wait maxshift shift
			for(int i = bestSourceIndex+2;i<pathOp.size() && shiftFlag==false;i++){
				System.out.println("TEST i EN UPDATE, updating node: "+pathOp.getPath().get(i).getPoi_id());
				shiftFlag = updateAfterInsert(pathOp.get(i));//COMPROBAR SI AFECTA UNTIL SHIFT==0
			}
			
			//Update maxShift from newVertex to start
			for(int i = bestSourceIndex+1; i>=0; i--){
				int newVertexMaxShift = pathOp.calculateMaxShift(pathOp.get(i));
				pathOp.get(i).setMaxShift(newVertexMaxShift);
			}
			
			printPathDatesM(pathOp);
		}
		
	}
	
	public boolean updateAfterInsert(Poi currentPoi){
		//order: arrive wait shift start maxshift
		boolean shiftFlag = false;
		int oldWait = currentPoi.getWait();
		updateWait(currentPoi);
		updateArrival(currentPoi);
		updateShift(currentPoi,oldWait);
		
		if(currentPoi.getShift()==0){
			System.out.println("EL SHIFT EN EL UPDATE ES 0 -- FLAG TRUE");
			shiftFlag=true;
		}else{
			System.out.println("UPDATING START Y MAXSHIFT");
			if(!currentPoi.equals(pathOp.getPath().getLast())){
				System.out.println("SHIFT NORMAL : "+calculateShift(pathOp.getPrevious(currentPoi), pathOp.getNext(currentPoi), currentPoi));
			}
			System.out.println("wait: "+currentPoi.getWait()+" arrival: "+currentPoi.getArrival()+" start: "+currentPoi.getStartTime());
			System.out.println("calculando nuevo start time: "+currentPoi.getStartTime()+" + "+currentPoi.getShift()+" = "+(currentPoi.getStartTime()+currentPoi.getShift()));
			updateStartTime(currentPoi);
			updateMaxShift(currentPoi);
		}
		return shiftFlag;
	}
	
	public void calculateNewTimeValues(int poiIndex){
		int arrival = calculateArrival(pathOp.get(poiIndex-1), pathOp.get(poiIndex));
		System.out.println("** NEW TIME VALUES NODE "+pathOp.get(poiIndex).getPoi_id());
		System.out.println("** arrival: "+arrival);
		pathOp.get(poiIndex).setArrival(arrival);
		
		System.out.println("Calculating wait time -- poi: "+pathOp.get(poiIndex).getPoi_id()+" opening: "+pathOp.get(poiIndex).getOpeningTime()+" arrival: "+pathOp.get(poiIndex).getArrival());
		int wait = pathOp.get(poiIndex).calculateWait();
		System.out.println("** wait: "+wait);
		pathOp.get(poiIndex).setWait(wait);
		
		
		System.out.println("Calculating start time -- poi: "+pathOp.get(poiIndex).getPoi_id()+" arrival: "+pathOp.get(poiIndex).getArrival()+" wait: "+pathOp.get(poiIndex).getWait());
		int start = pathOp.get(poiIndex).calculateStart();
		System.out.println("** start: "+start);
		pathOp.get(poiIndex).setStartTime(start);
		
		if(poiIndex!=pathOp.size()-1){
			int shift = calculateShift(pathOp.get(poiIndex-1), pathOp.get(poiIndex+1), pathOp.get(poiIndex));
			System.out.println("Calculatine shift from scratch- anterior nodo:"+pathOp.get(poiIndex-1).getPoi_id()+" siguiente: "+pathOp.get(poiIndex+1).getPoi_id()+" actual: "+pathOp.get(poiIndex).getPoi_id());
			System.out.println("   SHIFT = "+matrix.getMoveCost(pathOp.get(poiIndex-1), pathOp.get(poiIndex))+" + "+pathOp.get(poiIndex).getWait()+" + "+matrix.getMoveCost(pathOp.get(poiIndex), pathOp.get(poiIndex+1))+" - "+matrix.getMoveCost(pathOp.get(poiIndex-1), pathOp.get(poiIndex+1)));
			System.out.println("** shift: "+shift);
			pathOp.get(poiIndex).setShift(shift);
		}else{
			int shift = Math.max(0, (pathOp.getPrevious(pathOp.get(poiIndex)).getShift()-pathOp.get(poiIndex).getWait()));
			pathOp.get(poiIndex).setShift(shift);
		}
	}
	
	
	/**
	 * Calcula el mejor Poi para insertar y la mejor posicion en la que insertarlo
	 * @return ArrayList<Object> [bestPoi,bestSourceIndex]
	 */
	public ArrayList<Object> calculateBestInsert(){
		Poi bestVertex;
		int bestSourceIndex;
		double bestRatio=0;
		
		int auxBestShift=0;
		ArrayList<Object> returnList = new ArrayList<Object>();
		//for each not included visit
			//determine best posible insert position and shift (lowest shift)
			//calculate ratio
		//insert visit with > ratio
		for(Poi currentPoi : candidatePoiList){
			double bestPositionShift = Double.POSITIVE_INFINITY;
			int localBestPositionSource=0;
			boolean flag = false;
			for(int i=0; i<pathOp.size()-1;i++){//pathOp.get(i) es sourcePoi y i+1 es targetPoi
				
				currentPoi.setArrival(calculateArrival(pathOp.get(i), currentPoi));
				currentPoi.setWait(currentPoi.calculateWait());
				
				if(isInsertionFeasible(pathOp.get(i), pathOp.get(i+1), currentPoi)){
					//COMPROBAR, NO ESTA ENTRANDO
					int shift = calculateShift(pathOp.get(i), pathOp.get(i+1), currentPoi);
					if(shift<bestPositionShift){
						bestPositionShift=shift;
						localBestPositionSource=i;
						flag=true;
						
						auxBestShift=shift;//AUXILIAR BORRAR
						
//						System.out.println("New localBestSource: "+localBestPositionSource);
					}
				}
			}
			if(flag==true){
				double auxRatio = getInsertRatio(pathOp.get(localBestPositionSource), pathOp.get(localBestPositionSource+1), currentPoi);
				if(auxRatio>bestRatio){
					bestRatio=auxRatio;
					bestVertex=currentPoi;
					bestSourceIndex=localBestPositionSource;
					returnList = new ArrayList<Object>(Arrays.asList(bestVertex,bestSourceIndex));
					
					//System.out.println("CalculateBestIndex bestShift: "+auxBestShift+" del vertice "+bestVertex.getPoi_id()+" detras de "+pathOp.getPath().get(bestSourceIndex).getPoi_id());
				}
			}
		}
		return returnList;
	}
	
	
	public void shakePath(int R, int S){
		boolean goToStartFlag = shakeRemoveStep(R, S);
		shakeUpdateAfter(R, S, goToStartFlag);
		shakeUpdatePrevious(R, S, goToStartFlag);
	}
	
	public boolean shakeRemoveStep(int R, int S){
		//delete sets of visits  i-->j (delete R starting from point S)
		int deleteIndex = S;
		boolean goToStartFlag = false;
		//Path copyOfPath = (Path) path.clone();
		System.out.println("--SHAKE STEP    R: "+R+"  S: "+S);
		//Si se llega a la ultima posicion se pasa al segundo vertice
		for(int i = 0;i<R && pathOp.size()>2;i++){
			if(deleteIndex==pathOp.size()-1){
				deleteIndex=1;
				goToStartFlag = true;
			}
			candidatePoiList.add(pathOp.get(deleteIndex));
			pathOp.getPath().remove(deleteIndex);
		}
		return goToStartFlag;
	}
	
	
	public void shakeUpdateAfter(int R, int S, boolean goToStartFlag){
		int startUpdateIndex;
		boolean shiftFlag = false;
		//calculate start update index
		if(goToStartFlag==false){
			System.out.println("Update Cumple primera condicion");
			startUpdateIndex = S;
		}else{
			System.out.println("Update cumple segunda condicion");
			startUpdateIndex = 1;
		}
		
		System.out.println("UPDATE AFTER SHAKE");
		System.out.println("** startUpdateIndex = "+startUpdateIndex);
		
		for(int i=startUpdateIndex;i<pathOp.size() && shiftFlag == false;i++){
			System.out.println("Modificando valores after. Nodo: "+pathOp.get(i).getPoi_id());
			System.out.println("arrival before: "+pathOp.get(i).getArrival());
			calculateNewTimeValues(i);
			//updateShift(pathOp.get(i));
			//shiftFlag = updateAfterInsert(pathOp.get(i));
			System.out.println("arrival despues: "+pathOp.get(i).getArrival());
		}
		for(int i=pathOp.size()-1;i>=0;i--){
			System.out.println("ENTRAMOS MARCHA ATRAS CON i: "+i);
			int newVertexMaxShift = pathOp.calculateMaxShift(pathOp.get(i));
			pathOp.get(i).setMaxShift(newVertexMaxShift);
		}
	}
	
	public void shakeUpdatePrevious(int R, int S, boolean goToStartFlag){
		if(goToStartFlag){
			//update solo vertize start
			int maxShift = pathOp.calculateMaxShift(pathOp.getPath().getFirst());
			pathOp.getPath().getFirst().setMaxShift(maxShift);
		}else{
			//update desde S hacia atras
			for(int i=S-1;i>=0;i--){
				int maxShift = pathOp.calculateMaxShift(pathOp.get(i));
				pathOp.get(i).setMaxShift(maxShift);
			}
		}
	}
	
	
	public void printPathDates(Path pathOp){
		System.out.println();
		for(Poi poi:pathOp.getPath()){
			System.out.println("Id: "+poi.getPoi_id()+" arrival: "+toHours(poi.getArrival())+" start: "+toHours(poi.getStartTime())+" opening: "+toHours(poi.getOpeningTime())+" closing: "+toHours(poi.getClosingTime())+" wait: "+toHours(poi.getWait())+" shift: "+toHours(poi.getShift())+" maxShift: "+toHours(poi.getMaxShift()));
		}
		System.out.println();
		System.out.println();
	}
	
	public void printPathDatesM(Path pathOp){
		System.out.println();
		for(Poi poi:pathOp.getPath()){
			System.out.println("Id: "+poi.getPoi_id()+" arrival: "+poi.getArrival()+" start: "+poi.getStartTime()+" opening: "+poi.getOpeningTime()+" closing: "+poi.getClosingTime()+" wait: "+poi.getWait()+" shift: "+poi.getShift()+" maxShift: "+poi.getMaxShift());
		}
		System.out.println();
		System.out.println();
	}
	
	public int toHours(int timeMilis){
		return timeMilis/1000/60/60;
	}
	
}

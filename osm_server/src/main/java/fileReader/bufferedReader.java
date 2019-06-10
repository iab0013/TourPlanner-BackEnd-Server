package fileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import algorithms.Hybrid;
import algorithms.antColonySystem;
import model.Matrix;
import model.Path;
import model.Poi;

public class bufferedReader {
	
	private static Matrix matrix;
	private static ArrayList<DataSetPOI> setOfDataPOIs;
	private static Integer[][] ttiMatrix;
	private static int id;
	private static int nVertex;
	private static int tMax;
	
	private final static String DATA_SET = "20.1.1.TXT";
	private final static String TTI_DATA_SET = "titt20.TXT";
	
	public static void main(String[] args){
		initialization();
		//Path path = new Hybrid(matrix,(double)tMax).execute();
		
		Path path = new antColonySystem(matrix, (double)tMax).execute();
		
//		System.out.println();
//		System.out.println("id: "+path.getPath().getFirst().getPoi_id()+"  opening: "+path.getPath().getFirst().toHours(path.getPath().getFirst().getOpeningTime()));
//		System.out.println("wait time: "+path.getPath().getFirst().toHours(path.getPath().getFirst().calculateWait(25200000)));
		
		
	}
	
	/**
	 * Metodo que inicia el proceso
	 */
	
	public static void initialization(){
		id = 0;
		matrix = new Matrix();
		setOfDataPOIs = new ArrayList<DataSetPOI>();
		loadDataSet(DATA_SET);
		loadTTiMatrix(TTI_DATA_SET);
		calculateCosts();
	}
	
	/**
	 * Metodo encargado de cargar el DataSet de vertices
	 * @param filename
	 */
	public static void loadDataSet(String filename){
		BufferedReader br = null;
		String line;
		int cont = 0;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			while((line = br.readLine()) != null){
				readVertex(line, cont);
				cont++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Metodo encargado de leer y almacenar los POI del fichero de DataSets
	 * El primer vertice es el origen y el ultimo el destino
	 * @param line
	 * @param cont
	 */
	public static void readVertex(String line,int cont){
		String[] data;
		if(cont == 0){
			data = line.split(" ");
			nVertex = Integer.parseInt(data[0]);
		}else if(cont == 1){
			data = line.split(" ");
			tMax = Integer.parseInt(data[0]);
		}else{
			data = line.split(";");
			DataSetPOI setPOI = new DataSetPOI();
			setPOI.setId(id);
			setPOI.setScore(Integer.parseInt(data[1]));
			setPOI.setServiceTime(Integer.parseInt(data[3]));
			setPOI.setOpeningTime(Integer.parseInt(data[4]));
			setPOI.setClosingTime(Integer.parseInt(data[5]));
			setOfDataPOIs.add(setPOI);
			id++;
		}
	}
	
	/**
	 * Metodo encargado de cargar el fichero que contiene la matriz de tiempos (independiente)
	 * @param filename
	 */
	public static void loadTTiMatrix(String filename){
		BufferedReader br = null;
		String line;
		int row = 0;
		
		ttiMatrix = new Integer[nVertex][nVertex];
		
		try {
			br = new BufferedReader(new FileReader(filename));
			while((line = br.readLine()) != null){
				readTTiLine(line, row);
				row++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo encargado de almacenar los distintos TTI del fichero de tiempos independientes
	 * @param line
	 * @param row
	 */
	public static void readTTiLine(String line, int row){
		String[] data;
		data = line.split(";");
		for(int i = 0; i < data.length; i++){
			ttiMatrix[row][i]=Integer.parseInt(data[i]);	
		}
	}
	
	/**
	 * Metodo test para comprobar que la matriz de tiempos se guarde correctamente. BORRAR
	 */
	public static void printTTiMatrix(){
		for(int i = 0; i < nVertex; i++){
			for (int j = 0; j< nVertex; j++){
				System.out.print(ttiMatrix[i][j]+" ");
			}
			System.out.println("");
		}
	}
	
	
	/**
	 * Metodo que rellena una matriz de los puntos de interes y los costes de tiempo en viajar entre ellos
	 */
	public static void calculateCosts(){
		
		int cont = 0;
		
		for (DataSetPOI currentSetPoi : setOfDataPOIs){
			for (int i = 0; i < setOfDataPOIs.size(); i++){
				Poi source = new Poi();
				Poi target = new Poi();
				int cost;
				
				cost = ttiMatrix[(int) currentSetPoi.getId()][(int) setOfDataPOIs.get(i).getId()];
				source.setPoi_id(currentSetPoi.getId());
				source.setScore(currentSetPoi.getScore());
				source.setServiceTime(currentSetPoi.getServiceTime());
				source.setOpeningTime(currentSetPoi.getOpeningTime());
				source.setClosingTime(currentSetPoi.getClosingTime());
				
				target.setPoi_id(setOfDataPOIs.get(i).getId());
				target.setScore(setOfDataPOIs.get(i).getScore());
				target.setServiceTime(setOfDataPOIs.get(i).getServiceTime());
				target.setOpeningTime(setOfDataPOIs.get(i).getOpeningTime());
				target.setClosingTime(setOfDataPOIs.get(i).getClosingTime());
				
				matrix.addPoi(source, target, cost);
				
				if(cont==0 && i == setOfDataPOIs.size()-1){
					matrix.setSource(source);
					matrix.setTarget(target);
				}
			}
			cont++;
		}
	}
	
	/**
	 * Clase interna. Define el diseño de los POI del DataSet
	 * @author ignap
	 *
	 */
	private static class DataSetPOI{
		private long id;
		private int score;
		private int serviceTime;
		private int openingTime;
		private int closingTime;
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public int getScore() {
			return score;
		}
		public void setScore(int score) {
			this.score = score;
		}
		public int getServiceTime() {
			return serviceTime;
		}
		public void setServiceTime(int serviceTime) {
			this.serviceTime = serviceTime;
		}
		public int getOpeningTime() {
			return openingTime;
		}
		public void setOpeningTime(int openingTime) {
			this.openingTime = openingTime;
		}
		public int getClosingTime() {
			return closingTime;
		}
		public void setClosingTime(int closingTime) {
			this.closingTime = closingTime;
		}
	}
	
}

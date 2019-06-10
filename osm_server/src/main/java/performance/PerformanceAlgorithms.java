package performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import model.Matrix;
import model.Path;
import model.Poi;
import algorithms.Grasp;

/**
 * Clase que permite ejecutar los diferentes datasets testeados a la
 * hora de validar los algoritmos de rutas.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class PerformanceAlgorithms {

	private static Matrix matrix;
	private static ArrayList<DataSetPOI> setOfDataPOIs;
	private static int id = 0;
	/**
	 * Nombre del data set.
	 */
	private final static String DATA_SET = "set_66_1_130.txt";
	private final static int ITERATIONS = 50;

	/**
	 * Método main.
	 * 
	 * @param args
	 *            argumentos de entrada
	 * 
	 */
	public static void main(String[] args) {
		double total = 0.0;
		double max = 0.0;
		double time_limit = 0.0;
		initialization();
		time_limit = extractTimeLimitFromDataSetName();
		for (int i = 0; i < ITERATIONS; i++) {
			long start = System.currentTimeMillis();
			Path path = new Grasp(matrix, time_limit).executeAlgorithm();
			long stop = System.currentTimeMillis();
			long elapsed = stop - start;
			total += path.getScore();
			if (path.getScore() > max) {
				max = path.getScore();
			}
			System.out.println("Solución " + i + ":" + path.getScore()
					+ " T_ejecución: " + elapsed);
		}
		System.out.println("Average: " + total / ITERATIONS + " Best: " + max);
	}

	/**
	 * Método que extrae el tiempo límite para el dataset seleccionado.
	 * 
	 * @return tiempo límite
	 * 
	 */
	private static double extractTimeLimitFromDataSetName() {
		int index_initial = DATA_SET.lastIndexOf("_");
		int index_final = DATA_SET.indexOf(".");
		String timeLimitAsString = DATA_SET.substring(index_initial + 1,
				index_final);
		return Double.valueOf(timeLimitAsString);
	}

	/**
	 * Método que realiza la iniciación del proceso.
	 * 
	 */
	public static void initialization() {
		id = 0;
		matrix = new Matrix();
		setOfDataPOIs = new ArrayList<DataSetPOI>();
		loadDataSet(DATA_SET);
		calculateEcuclideanDistances();
	}

	/**
	 * Método que calcula las distancias euclídeas entre los puntos presentes en
	 * el dataset.
	 * 
	 */
	public static void calculateEcuclideanDistances() {
		double cost;
		int cont = 0;
		for (DataSetPOI currentDsPOI : setOfDataPOIs) {
			for (int i = 0; i < setOfDataPOIs.size(); i++) {
				if (currentDsPOI != setOfDataPOIs.get(i)) {
					cost = calculateCost(currentDsPOI, setOfDataPOIs.get(i));
					Poi source = new Poi();
					Poi target = new Poi();
					source.setCoordinates(String.valueOf(currentDsPOI
							.getLatitude())
							+ " "
							+ String.valueOf(currentDsPOI.getLongitude()));
					source.setScore(currentDsPOI.getScore());
					source.setVertex(currentDsPOI.getVertex());
					source.setPoi_id(currentDsPOI.getPoi());
					target.setCoordinates(String.valueOf(setOfDataPOIs.get(i)
							.getLatitude())
							+ " "
							+ String.valueOf(setOfDataPOIs.get(i)
									.getLongitude()));
					target.setScore(setOfDataPOIs.get(i).getScore());
					target.setVertex(setOfDataPOIs.get(i).getVertex());
					target.setPoi_id(setOfDataPOIs.get(i).getPoi());
					matrix.addPoi(source, target, cost);

					if (cont == 0 && i == 1) {
						matrix.setSource(source);
						matrix.setTarget(target);
					}
				}
			}
			cont++;
		}
	}

	/**
	 * Método que calcula el coste entre un par de vértices determinado.
	 * 
	 * @param currentDsPOI
	 *            vértice origen
	 * @param dataSetPOI
	 *            vértice destino
	 * 
	 */
	private static double calculateCost(DataSetPOI currentDsPOI,
			DataSetPOI dataSetPOI) {
		double total = 0;
		total = Math
				.sqrt(Math.pow(
						currentDsPOI.getLatitude() - dataSetPOI.getLatitude(),
						2)
						+ Math.pow(
								currentDsPOI.getLongitude()
										- dataSetPOI.getLongitude(), 2));
		return total;

	}

	/**
	 * Método que carga un dataset a partir de un nombre dado.
	 * 
	 * @param fileName
	 *            nombre del dataset
	 * 
	 */
	public static void loadDataSet(String fileName) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		int cont = 0;
		File dir = new File(System.getProperty("user.dir") + "//dataSets");
		try {
			archivo = new File(dir + "//" + fileName);
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				if (cont != 0) {
					readLineToArrayOfPOIs(line, false);
				} else {
					readLineToArrayOfPOIs(line, true);
				}
				cont++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * Método lee una línea del fichero y lo almacena en una estructura de tipo
	 * DataSetPOI.
	 * 
	 * @param line
	 *            línea del dataset
	 * @param firstLine
	 *            parámetro que indica si se trata de la primera línea del
	 *            fichero
	 * 
	 */
	public static void readLineToArrayOfPOIs(String line, boolean firstLine) {
		String[] data;
		data = line.split("\t");
		if (!firstLine) {
			DataSetPOI dataSetPOI = new DataSetPOI();
			dataSetPOI.setLatitude(Double.parseDouble(data[0]));
			dataSetPOI.setLongitude(Double.parseDouble(data[1]));
			dataSetPOI.setVertex(id);
			dataSetPOI.setPoi(id);
			dataSetPOI.setScore(Integer.parseInt(data[2]));
			setOfDataPOIs.add(dataSetPOI);
			id++;
		}
	}

	/**
	 * Clase interna que define la estructura de los puntos leídos de los
	 * datasets.
	 * 
	 * @author Inigo Vázquez - Roberto Villuela
	 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
	 */
	private static class DataSetPOI {

		private double latitude;
		private double longitude;
		private int score;
		private int vertex;
		private long poi;

		public int getVertex() {
			return vertex;
		}

		public void setVertex(int vertex) {
			this.vertex = vertex;
		}

		public long getPoi() {
			return poi;
		}

		public void setPoi(long poi) {
			this.poi = poi;
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

	}
}

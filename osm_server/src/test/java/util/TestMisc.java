package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import junit.framework.Assert;
import model.Poi;
import model.Poi.Category;
import org.junit.Test;
import resource.request.Request;
import util.Misc;

/**
 * Clase que contienen los tests que validan la lógica del paquete util.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class TestMisc {

	@Test
	/**
	 * Test que valida el funcionamiento del método extractCoordinates.
	 * 
	 */
	public void extractCoordinatesTest() {
		// lo que se espera obtener tras la llamada a la función
		String coordinatesAfterExtract = "-3.7040397 42.342381";
		// formato en el que se obtienen coordenadas cuando se consulta a la
		// BBDD
		String coordinatesFromDatabase = "POINT(" + coordinatesAfterExtract
				+ ")";
		ArrayList<String> response = Misc
				.extractCoordinates(coordinatesFromDatabase);
		Assert.assertTrue("extractCoordinates ERROR", response.get(0)
				.compareTo(coordinatesAfterExtract) == 0);
		String lineStringFromDatabase = "LINESTRING(-3.559878 42.3432508,-3.5600435 42.3433847,-3.5603821 42.3438097)";
		String firstPairOfCoordinates = "-3.559878 42.3432508";
		String secondPairOfCoordinates = "-3.5600435 42.3433847";
		String thirdPairOfCoordinates = "-3.5603821 42.3438097";
		ArrayList<String> arrayOfExpectedResponses = new ArrayList<String>();
		arrayOfExpectedResponses.add(firstPairOfCoordinates);
		arrayOfExpectedResponses.add(secondPairOfCoordinates);
		arrayOfExpectedResponses.add(thirdPairOfCoordinates);
		ArrayList<String> arrayResponse = Misc
				.extractCoordinates(lineStringFromDatabase);
		for (int i = 0; i < arrayResponse.size(); i++) {
			Assert.assertTrue(arrayResponse.get(i).compareTo(
					arrayOfExpectedResponses.get(i)) == 0);
		}
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método formatReturningCoordinates.
	 * 
	 */
	public void formatReturningCoordinatesTest() {
		String responseFromDatabase = "LINESTRING(-3.559878 42.3432508,-3.5600435 42.3433847,-3.5603821 42.3438097)";
		String anotherResponseFromDatabase = "LINESTRING(-3.5609177 42.3445898,-3.5610438 42.3449158)";
		ArrayList<String> setOfResponses = new ArrayList<String>();
		setOfResponses.add(responseFromDatabase);
		setOfResponses.add(anotherResponseFromDatabase);
		String expectedRestult = "-3.559878 42.3432508 -3.5600435 42.3433847 -3.5603821 42.3438097 -3.5609177 42.3445898 -3.5610438 42.3449158 ";
		String result = Misc.formatReturningCoordinates(setOfResponses);
		Assert.assertTrue("formatReturningCoordinates ERROR",
				result.compareTo(expectedRestult) == 0);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método linkSetOfEdges.
	 * 
	 */
	public void linkSetOfEdgesTest() {
		ArrayList<String> setOfEdges = new ArrayList<String>();
		String lineString = "LINESTRING(-3.6937112 42.3440796,-3.6937542 42.3440528)";
		String anotherLineString = "LINESTRING(-3.6936183 42.3436829,-3.6937542 42.3440528)";
		setOfEdges.add(lineString);
		setOfEdges.add(anotherLineString);
		ArrayList<String> nonContiguousSetOfEdges = new ArrayList<String>();
		String nonContiguouslineString = "LINESTRING(-3.6936183 42.3436829,-3.6937542 42.3440528)";
		String nonContiguousanotherLineString = "LINESTRING(-3.6936183 42.3436829,-3.6936935 42.3436677,-3.6937688 42.3436542)";
		nonContiguousSetOfEdges.add(nonContiguouslineString);
		nonContiguousSetOfEdges.add(nonContiguousanotherLineString);
		ArrayList<String> resultingCoordinates = Misc.linkSetOfEdges(
				nonContiguousSetOfEdges, setOfEdges);
		// La éltima coordenada de cada tramo ha de coincidir con la primera del
		// siguiente
		for (int i = 1; i < resultingCoordinates.size(); i++) {
			ArrayList<String> lastCoordinates = Misc
					.extractCoordinates(resultingCoordinates.get(i));
			ArrayList<String> previousCoordinates = Misc
					.extractCoordinates(resultingCoordinates.get(i - 1));
			Assert.assertTrue(lastCoordinates.get(0).compareTo(
					previousCoordinates.get(previousCoordinates.size() - 1)) == 0);
		}

	}

	@Test
	/**
	 * Test que valida el funcionamiento del método orderLineString.
	 * 
	 */
	public void orderLineStringTest() {
		String lineString = "LINESTRING(-3.6937112 42.3440796,-3.6937542 42.3440528)";
		String lineString2 = "LINESTRING(-3.6937542 42.3440528,-3.6936183 42.3436829)";
		String lineString3 = "LINESTRING(-3.6937688 42.3436542,-3.6936935 42.3436677,-3.6936183 42.3436829)";
		String lineString4 = "LINESTRING(-3.6937688 42.3436542,-3.6948729 42.343447)";
		String lineString5 = "LINESTRING(-3.6948002 42.3432637,-3.6948729 42.343447)";
		ArrayList<String> originalLineStrings = new ArrayList<String>();
		originalLineStrings.add(lineString);
		originalLineStrings.add(lineString2);
		originalLineStrings.add(lineString3);
		originalLineStrings.add(lineString4);
		originalLineStrings.add(lineString5);
		ArrayList<String> orderedLineStrings = Misc
				.orderLineString(originalLineStrings);

		for (int i = 1; i < orderedLineStrings.size(); i++) {
			ArrayList<String> edgeCoordinates = Misc
					.extractCoordinates(orderedLineStrings.get(i));
			ArrayList<String> previousEdgeCoordinates = Misc
					.extractCoordinates(orderedLineStrings.get(i - 1));
			Assert.assertTrue(edgeCoordinates.get(0)
					.compareTo(
							previousEdgeCoordinates.get(previousEdgeCoordinates
									.size() - 1)) == 0);
		}

	}

	@Test
	/**
	 * Test que valida el funcionamiento del método getReversedEdge.
	 * 
	 */
	public void getReversedEdgeTest() {
		String lineString = "LINESTRING(-3.6937688 42.3436542,-3.6936935 42.3436677,-3.6936183 42.3436829)";
		String expectedCoordinates = "-3.6936183 42.3436829,-3.6936935 42.3436677,-3.6937688 42.3436542";
		ArrayList<String> coordinates = Misc.extractCoordinates(lineString);
		String[] reversedCoordinates = coordinates
				.toArray(new String[coordinates.size()]);
		String result = Misc.getReversedEdge(reversedCoordinates);
		Assert.assertTrue(result.compareTo(expectedCoordinates) == 0);

	}

	@Test
	/**
	 * Test que valida el funcionamiento del método obtainMiddleCoordinates.
	 * 
	 */
	public void obtainMiddleCoordinatesTest() {
		String source_coordinates = "-3.6937688 42.3436542";
		String target_coordinates = "-3.6937542 42.3440528";
		// middle_lat = (lat_origen - lat_destino)/2
		// middle_lon = (lon_origen - lon_destino)/2
		// middle_coordinates -> (lat_origen - middle_lat, lon_origen -
		// middle_lon)
		String expected_middle_coordinates = "-3.6937615 42.3438535";
		String middle_coordinates = "";
		middle_coordinates = Misc.obtainMiddleCoordinates(source_coordinates,
				target_coordinates);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "-4.351273 55.865294";
		target_coordinates = "-4.152145 55.866643";
		expected_middle_coordinates = "-4.251709 55.8659685";
		middle_coordinates = Misc.obtainMiddleCoordinates(source_coordinates,
				target_coordinates);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "102.940521 17.679353";
		target_coordinates = "102.917175 18.11975";
		expected_middle_coordinates = "102.928848 17.8995515";
		middle_coordinates = Misc.obtainMiddleCoordinates(source_coordinates,
				target_coordinates);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "-3.549442 40.437933";
		target_coordinates = "-3.584118 40.41428";
		expected_middle_coordinates = "-3.56678 40.4261065";
		middle_coordinates = Misc.obtainMiddleCoordinates(source_coordinates,
				target_coordinates);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método filterPoiList.
	 * 
	 */
	public void filterPoiListTest() {
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		// insertamos 100 POIs con un score aleatorio
		int numIterations = 100;
		for (int i = 0; i < numIterations; i++) {
			Poi poi = new Poi();
			poi.setScore(Misc.generateRandomNumber(0, 100));
			poiList.add(poi);
		}
		Misc.filterPoiList(poiList);
		// ahora si lo recorremos han de estar ordenados de modo ascendente, de
		// modo que en esta lista siempre estén los POIs de mayor score
		for (int j = 1; j < numIterations; j++) {
			Assert.assertTrue(poiList.get(j - 1).getScore() >= poiList.get(j)
					.getScore());
		}
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método generateRandomNumber.
	 * 
	 */
	public void generateRandomNumberTest() {
		int randomNumber = Misc.generateRandomNumber(0, 10);
		Assert.assertTrue(randomNumber >= 0 && randomNumber <= 10);

		randomNumber = Misc.generateRandomNumber(5, 500);
		Assert.assertTrue(randomNumber >= 5 && randomNumber <= 500);

		randomNumber = Misc.generateRandomNumber(9, 18);
		Assert.assertTrue(randomNumber >= 9 && randomNumber <= 18);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método selectRandomPoi.
	 * 
	 */
	public void selectRandomPoiTest() {
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		// insertamos 100 POIs con un score aleatorio
		int numIterations = 100;
		for (int i = 0; i < numIterations; i++) {
			Poi poi = new Poi();
			poi.setPoi_id((long) i);
			poiList.add(poi);
		}
		Poi removedPoi = Misc.selectRandomPoi(poiList);
		// comprobamos ahora que se haya eliminado de la lista
		Assert.assertTrue(!poiList.contains(removedPoi));
		Poi anotherPoiRemoved = Misc.selectRandomPoi(poiList);
		Assert.assertTrue(!poiList.contains(anotherPoiRemoved));
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método calculateSearchRadiusAccordingToTime.
	 * 
	 */
	public void calculateSearchRadiusAccordingToTimeTest() {
		int speed_by_foot = 5;
		double radius = 0;
		double expectedRadius = 0;
		double routeTime = 2.0;
		radius = Misc.calculateSearchRadiusAccordingToTime(routeTime, "fo_");
		// e = v x t (km) -> x 1000 (m)
		expectedRadius = speed_by_foot * routeTime * 1000;
		Assert.assertTrue(radius == expectedRadius);
		routeTime = 5.0;
		// car
		radius = Misc.calculateSearchRadiusAccordingToTime(routeTime, "dr_");
		expectedRadius = 10000; // calculateSearchRadiusAccordingToTime max
								// radius setted to 10000
		Assert.assertTrue(radius == expectedRadius);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método obtainRandomPoiByCategory.
	 * 
	 */
	public void obtainRandomPoiByCategoryTest() {
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		int numIterations = 100;
		for (int i = 0; i < numIterations; i++) {
			Poi poi = new Poi();
			poi.setPoi_id((long) i);
			if (i < 25) {
				poi.setCategory(Category.GASTRONOMY);
			} else if (i >= 25 && i < 50) {
				poi.setCategory(Category.LEISURE);
			} else if (i >= 50 && i < 75) {
				poi.setCategory(Category.CULTURE);
			} else {
				poi.setCategory(Category.NATURE);
			}
			poiList.add(poi);
		}
		Poi poi = Misc.obtainRandomPoiByCategory(poiList, "culture");
		Assert.assertTrue(poi.getCategory().compareTo(Category.CULTURE) == 0);
		poi = Misc.obtainRandomPoiByCategory(poiList, "nature");
		Assert.assertTrue(poi.getCategory().compareTo(Category.NATURE) == 0);
		poi = Misc.obtainRandomPoiByCategory(poiList, "leisure");
		Assert.assertTrue(poi.getCategory().compareTo(Category.LEISURE) == 0);
		poi = Misc.obtainRandomPoiByCategory(poiList, "gastronomy");
		Assert.assertTrue(poi.getCategory().compareTo(Category.GASTRONOMY) == 0);
		poi = Misc.obtainRandomPoiByCategory(poiList, "wrongcategory");
		Assert.assertTrue(poi == null);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método getCategoryFromString.
	 * 
	 */
	public void getCategoryFromStringTest() {
		String requestedCategory = "culture";
		Category category = Misc.getCategoryFromString(requestedCategory);
		Assert.assertTrue(category.compareTo(Category.CULTURE) == 0);
		requestedCategory = "nature";
		category = Misc.getCategoryFromString(requestedCategory);
		Assert.assertTrue(category.compareTo(Category.NATURE) == 0);
		requestedCategory = "leisure";
		category = Misc.getCategoryFromString(requestedCategory);
		Assert.assertTrue(category.compareTo(Category.LEISURE) == 0);
		requestedCategory = "gastronomy";
		category = Misc.getCategoryFromString(requestedCategory);
		Assert.assertTrue(category.compareTo(Category.GASTRONOMY) == 0);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método configureSourceOrTargetPoi.
	 * 
	 */
	public void configureSourceOrTargetPoiTest() {
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		int numIterations = 50;
		for (int i = 0; i < numIterations; i++) {
			Poi poi = new Poi();
			poi.setPoi_id((long) i);
			poiList.add(poi);
		}
		String source_coordinates = "-3.7040397 42.342381";
		String target_coordinates = "-3.7256897 42.368974";
		Misc.configureSourceOrTargetPoi(source_coordinates, poiList, 0);
		Misc.configureSourceOrTargetPoi(target_coordinates, poiList, 1);
		// source
		Assert.assertTrue(poiList.get(0).getCoordinates()
				.compareTo("POINT(" + source_coordinates + ")") == 0);
		// target
		Assert.assertTrue(poiList.get(1).getCoordinates()
				.compareTo("POINT(" + target_coordinates + ")") == 0);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método calculateMiddleCoordinates.
	 * 
	 */
	public void calculateMiddleCoordinatesTest() {
		String source_coordinates = "-3.6937688 42.3436542";
		String target_coordinates = "-3.6937542 42.3440528";
		Request request = new Request();
		request.setSource_coordinates(source_coordinates);
		request.setTarget_coordinates(target_coordinates);
		/*
		 * middle_lat = (lat_origen - lat_destino)/2 middle_lon = (lon_origen -
		 * lon_destino)/2 middle_coordinates -> (lat_origen - middle_lat,
		 * lon_origen - middle_lon)
		 */
		String expected_middle_coordinates = "-3.6937615 42.3438535";
		String middle_coordinates = "";
		middle_coordinates = Misc.calculateMiddleCoordinates(request);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "-4.351273 55.865294";
		target_coordinates = "-4.152145 55.866643";
		expected_middle_coordinates = "-4.251709 55.8659685";
		request.setSource_coordinates(source_coordinates);
		request.setTarget_coordinates(target_coordinates);
		middle_coordinates = Misc.calculateMiddleCoordinates(request);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "102.940521 17.679353";
		target_coordinates = "102.917175 18.11975";
		request.setSource_coordinates(source_coordinates);
		request.setTarget_coordinates(target_coordinates);
		expected_middle_coordinates = "102.928848 17.8995515";
		middle_coordinates = Misc.calculateMiddleCoordinates(request);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);

		source_coordinates = "-3.549442 40.437933";
		target_coordinates = "-3.584118 40.41428";
		request.setSource_coordinates(source_coordinates);
		request.setTarget_coordinates(target_coordinates);
		expected_middle_coordinates = "-3.56678 40.4261065";
		middle_coordinates = Misc.calculateMiddleCoordinates(request);
		Assert.assertTrue(middle_coordinates
				.compareTo(expected_middle_coordinates) == 0);
	}

	@Test
	/**
	 * Test que valida el funcionamiento del método convertArrayListIntoHashMap.
	 * 
	 */
	public void convertArrayListIntoHashMapTest() {
		HashMap<Long, Poi> poiMap = null;
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		int numIterations = 50;
		for (int i = 0; i < numIterations; i++) {
			Poi poi = new Poi();
			poi.setPoi_id((long) i);
			poiList.add(poi);
		}
		poiMap = Misc.convertArrayListIntoHashMap(poiList);
		Iterator<?> it = poiMap.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry) it.next();
			// comprobar que todos los puntos del Hash estén en el List
			Assert.assertTrue(poiList.contains(pairs.getValue()));
			// comprobar la clave de hash coincide con la del punto (clave =
			// poi_id)
			Assert.assertTrue(poiList.get(poiList.indexOf(pairs.getValue()))
					.getPoi_id() == pairs.getKey());
		}
	}

}

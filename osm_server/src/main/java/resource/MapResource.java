package resource;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import resource.response.CityResponse;
import database.DatabaseException;
import database.DatabaseFacade;

/**
 * Clase que contiene la lÃ³gica necesaria para realizar la descarga de mapas de ciudades
 * desde el servidor.
 * 
 * @author Alejandro Cuevas Álvarez - aca0073@alu.ubu.es
 */
@Path("map")
public class MapResource {
	
	private final String MSG_ERROR_INTERNAL = "603";
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/x-navimap")
	/**
	 * Método que realiza la descarga del mapa de una ciudad.
	 * 
	 * @param requestParams
	 * 		Parametros de la petición en los que se encuentra el nombre del mapa a descargar.
	 * @return Mapa de la ciudad correspondiente.
	 * @throws IOException.
	 * 
	 * */
	public File getMap(MultivaluedMap<String, String> requestParams) throws IOException{
		String mapName = requestParams.getFirst("map_name");
		File map = new File(System.getProperty("user.dir") + "/../applications/osm_server/maps/"
				+ mapName + ".map");
		
		// Comprobamos si existe en el servidor el mapa que queremos descargar. 
		if (map.exists()){
			return map;
		}
		
		return null;
	}
	
	@POST
	@Path("exists")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Método que determina la ciudad en la que se encuentra un punto determinado.
	 * 		
	 * @params requestParams
	 * 		Parametros de la petición en los que se encuentra las coordenadas del punto.
	 * @return CityResponse con el id y el nombre de la ciudad en la que se encuentra el punto.
	 * 		
	 * */
	public CityResponse getCityByLocation(MultivaluedMap<String, String> requestParams){
		CityResponse cityResponse = new CityResponse();
		String latitude = "", longitude = "", point = "";
		DatabaseFacade db;
		try {
			db = DatabaseFacade.getInstance();
			latitude = requestParams.getFirst("latitude");
			longitude = requestParams.getFirst("longitude");
			point = "POINT(" + longitude + " " + latitude + ")";
			cityResponse = db.getCityByLocation(point);
		} catch (DatabaseException e) {
			e.printStackTrace();
			cityResponse.setStatus(MSG_ERROR_INTERNAL);
		} 
		
		return cityResponse;
		
	}

}

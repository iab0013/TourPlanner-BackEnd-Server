package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Clase que carga el fichero .properties que gestiona el acceso a la base de
 * datos.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class DatabaseProperties {

	private static final String PROPERTIES_FILE = "Database.properties";
	private static final Properties PROPERTIES = new Properties();

	static {
		URL url = DatabaseProperties.class.getResource("Database.properties");
		InputStream propertiesFile = null;
		try {
			propertiesFile = new FileInputStream(new File(url.getFile()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (propertiesFile == null) {
			throw new DatabaseConfigurationException("Properties file '"
					+ PROPERTIES_FILE + "' is missing in classpath.");
		}
		try {
			PROPERTIES.load(propertiesFile);
		} catch (IOException e) {
			throw new DatabaseConfigurationException(
					"Cannot load properties file '" + PROPERTIES_FILE + "'.", e);
		}
	}
	private String specificKey;


	
	/**
	 * Crea una instancia DAOProperties para la key dada que será utilizada como prefijo
	 * de key del fichero properties DAO.
	 * 
	 * @param specificKey
	 *            key que será utilizada como prefijo
	 * @throws DatabaseConfigurationException
	 *             durante la inicialización en caso que no se encuentre el properties o
	 *             no pueda ser cargado
	 */
	public DatabaseProperties(String specificKey)
			throws DatabaseConfigurationException {
		this.specificKey = specificKey;
	}


	/**
	 * Devuelve una instancia DAOProperties específica, asociada a la key dada
	 * con la opción de indicar si la propiedad es obligatoria o no
	 * 
	 * @param key
	 *            key asociada a la instancia DAOProperties
	 * @param mandatory
	 *            establece si el valor de la propiedad devuelta debería ser null
	 *            o vacía
	 * @return la instancia DAOProperties asociada a la key dada
	 * @throws DAOConfigurationException
	 *             si el valor de la propiedad devuelta es null o vacío mientras sea
	 *             obligatorio
	 */
	public String getProperty(String key, boolean mandatory)
			throws DatabaseConfigurationException {
		String fullKey = specificKey + "." + key;
		String property = PROPERTIES.getProperty(fullKey);

		if (property == null || property.trim().length() == 0) {
			if (mandatory) {
				throw new DatabaseConfigurationException("Required property '"
						+ fullKey + "'" + " is missing in properties file '"
						+ PROPERTIES_FILE + "'.");
			} else {
				property = null;
			}
		}
		return property;
	}

}

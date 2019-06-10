package database;

/**
 * Clase que representa una excepción en la configuración de acceso a la base de
 * datos, que no puede ser resuelta en tiempo de ejecución, como por ejemplo un
 * recurso perdido en el classpath, ausencia de una propiedad en el archivo
 * .properties, etc.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class DatabaseConfigurationException extends RuntimeException {


	private static final long serialVersionUID = 1L;


	/**
	 * Constructor de DatabaseConfigurationException con un mensaje específico.
	 * 
	 * @param message
	 *            mensaje detallado de la excepción
	 * 
	 */
	public DatabaseConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructor de DatabaseConfigurationException con la causa que originó la
	 * excepción.
	 * 
	 * @param cause
	 *            causa que originó la excepción
	 * 
	 */
	public DatabaseConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor de DatabaseConfigurationException con un mensaje específico y
	 * la causa que originó la expeción.
	 * 
	 * @param message
	 *            mensaje detallado de la excepción
	 * @param cause
	 *            causa que originó la excepción
	 */
	public DatabaseConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}

package database;

/**
 * Clase que representa una excepción genérica en la configuración de acceso a
 * la base de datos. Capturará excepciones que pudiera ocasionar el código,
 * tales como SQLExceptions.
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class DatabaseException extends RuntimeException {


	private static final long serialVersionUID = 1L;


	/**
	 * Constructor de DatabaseException con un mensaje específico.
	 * 
	 * @param message
	 *            mensaje detallado de la excepción
	 */
	public DatabaseException(String message) {
		super(message);
	}

	/**
	 * Constructor de DatabaseException con la causa que originó la excepción.
	 * 
	 * @param cause
	 *            causa que originó la excepción
	 * 
	 */
	public DatabaseException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor de DatabaseException con un mensaje especifico y la causa que
	 * origino la expecion.
	 * 
	 * @param message
	 *            mensaje detallado de la excepcion
	 * @param cause
	 *            causa que originó la excepcion
	 */
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}

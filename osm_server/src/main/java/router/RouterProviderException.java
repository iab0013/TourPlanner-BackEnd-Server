package router;

/**
 * Clase que representa una excepción genérica en la generación de la matriz de
 * distancias o en la reconstrucción de los caminos. Dicha excepción aparecerá
 * cuando aparezcan inconsistencias irreparables en la construcción de la matriz
 * (puntos inconexos).
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class RouterProviderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7067934774152008229L;

	public RouterProviderException(String message) {
		super(message);
	}

	public RouterProviderException() {
		super();
	}

	public RouterProviderException(Exception e) {
		super();
	}

}

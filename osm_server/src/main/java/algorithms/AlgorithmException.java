package algorithms;


/**
 * Clase que representa una excepción genérica en la ejecución de los algoritmos
 * de rutas. Dicha excepción aparecerá cuando el algoritmo no sea capaz de generar
 * una solución (tiempo insuficiente).
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public class AlgorithmException extends Exception {

	private static final long serialVersionUID = -6220729930410877712L;

	public AlgorithmException(String message) {
		super(message);
	}
	
	public AlgorithmException() {
		super();
	}

}

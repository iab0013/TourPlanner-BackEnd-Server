package algorithms;

import model.Path;

/**
 * Interfaz que contiene el método común a ejecutar por los diferentes
 * algoritmos que la implementen (Patrón estrategia).
 * 
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 */
public interface IAlgorithm {

	public abstract Path execute();
}

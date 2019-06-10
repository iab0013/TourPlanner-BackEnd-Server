package util;

import java.util.Comparator;

import model.*;

/**
 * Contiene los métodos correspondientes a la clase PoiComparator. 
 *
 * @author Inigo Vázquez - Roberto Villuela
 * @author ivg0007@alu.ubu.es - rvu0003@alu.ubu.es
 *
 */
public class PoiComparator<G> implements Comparator<G> {

	/**
	 * Método que compara dos Pois y devuelve un entero.
	 * 
	 * @param o1
	 *            Primer elemento para comparar
	 * @param o2
	 *            Segundo elemento para comparar
	 * @return Devuelve positivo, 0 o negativo dependiendo de si el primer
	 *         elemento es mayor igual o menor que el segundo
	 */
	public int compare(G o1, G o2) {
		if(((Poi) o1).getScore() < ((Poi) o2).getScore()){
			return 1;
		}
		if(((Poi) o1).getScore() > ((Poi) o2).getScore()){
			return -1;
		}
		return 0;
	}
}

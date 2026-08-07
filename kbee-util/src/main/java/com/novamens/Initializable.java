/**
 *
 */
package com.novamens;

/**
 * Interfaz común para objetos que necesitan inicialización y limpieza.
 * 
 * @author lleggieri
 * 
 */
public interface Initializable {

	/**
	 * Método para la inicialización interna del objeto.
	 */
	void initialize();

	/**
	 * Metodo para saber si el objeto ha sido inicializado.
	 * 
	 * @return true si el objeto ya fue iniciado.
	 */
	boolean isInitialized();

	/**
	 * Método para la limpieza interna del objeto.
	 */
	void shutdown();

}

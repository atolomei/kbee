package com.novamens.content.service;

/**
 * Data Management:
 * 
 * When the application tries to execute some invalid 
 * Data Management action 
 *
 */
public class DataManagementException extends Exception {

	
	private static final long serialVersionUID = 1L;
	

	/**
	 * 
	 * @param message
	 */
	public DataManagementException(String message) {
		super(message);
	}
	
}

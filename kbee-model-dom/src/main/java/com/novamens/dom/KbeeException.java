package com.novamens.dom;

/**
 * <p>The class {@code KbeeException} and any subclasses that are not also
 * subclasses of {@link RuntimeException} are <em>checked
 * exceptions</em>.  Checked exceptions need to be declared in a
 * method or constructor's {@code throws} clause if they can be thrown
 * by the execution of the method or constructor and propagate outside
 * the method or constructor boundary.
 * 
 */
public class KbeeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public KbeeException(Exception e) {
		super(e.getMessage(), e);
	}
	
	public KbeeException(Throwable e) {
		super(e);
	}
	
	public KbeeException(String message) {
		super(message);
	}

}

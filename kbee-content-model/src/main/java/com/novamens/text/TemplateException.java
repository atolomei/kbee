package com.novamens.text;

import com.novamens.util.KbeeRuntimeException;


/**
 * 
 *  Ideally this Exception should be subclass of Exception,
 *  like the semantically related SQLException.
 * 
 *  The reason why it was made a RuntimeException is that
 *  Spring Framework only roll backs transactions
 *  for methods that terminate with a RuntimeException.
 *  
 *
 */
public class TemplateException extends KbeeRuntimeException {
	
	private static final long serialVersionUID = 1L;

	public TemplateException(String message) {
		super(message);
	}

	public TemplateException(Exception cause) {
		super(cause);
	}
	
	public TemplateException(Throwable cause) {
		super(cause);
	}
}

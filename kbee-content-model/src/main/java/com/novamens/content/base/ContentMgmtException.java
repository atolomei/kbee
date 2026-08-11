package com.novamens.content.base;

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
public class ContentMgmtException extends KbeeRuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ContentMgmtException(String message) {
		super(message);
	}

	public ContentMgmtException(Exception cause) {
		super(cause);
	}
	
	public ContentMgmtException(Throwable cause) {
		super(cause);
	}
}

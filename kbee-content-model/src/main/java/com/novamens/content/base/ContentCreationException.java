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
*/
public class ContentCreationException extends KbeeRuntimeException {
	private static final long serialVersionUID = 1L;

	public ContentCreationException(Exception e) {
		super(e);
	}
	
	public ContentCreationException(String message) {
		super(message);
	}
}

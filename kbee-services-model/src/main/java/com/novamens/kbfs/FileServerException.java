package com.novamens.kbfs;

import com.novamens.dom.KbeeException;


/**
 * 
 */
public class FileServerException extends KbeeException {

	private static final long serialVersionUID = 1L;

	private String message;
	
	public FileServerException(Exception e) {
		super(e);
	}

	public FileServerException(Exception e, String message) {
		super(e);
		this.message=message;
	}

	
	
	public FileServerException(String s) {
		super(s);
	}
	
	public FileServerException(Throwable cause) {
		super(cause);
	}
	
	
	@Override
	public String getMessage() {
		if (super.getMessage()!=null)
			return super.getMessage() + (message!=null?(" | " + this.message):"");
		else
			return message;
	}
}

package com.novamens.portal6.model;

import com.novamens.dom.KbeeException;

public class PortalException extends KbeeException {

	private static final long serialVersionUID = 1L;

	public PortalException(String message) {
		super(message);
	}

	public PortalException(Exception cause) {
		super(cause);
	}
	
	public PortalException(Throwable cause) {
		super(cause);
	}
}

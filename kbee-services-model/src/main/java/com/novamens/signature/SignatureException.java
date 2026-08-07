package com.novamens.signature;

import com.novamens.dom.KbeeException;

public class SignatureException extends KbeeException {
	private static final long serialVersionUID = 1L;

	public SignatureException(String message) {
		super(message);
	}
	
	public SignatureException(Exception e) {
		super(e);
	}
}

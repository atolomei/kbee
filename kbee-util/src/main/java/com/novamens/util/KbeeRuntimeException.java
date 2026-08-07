package com.novamens.util;

import kbee.util.NovamensRuntimeException;

public class KbeeRuntimeException extends NovamensRuntimeException {
	private static final long serialVersionUID = 1L;

	public KbeeRuntimeException(Exception e) {
		super(e);
	}
	
	public KbeeRuntimeException(Throwable e) {
		super(e);
		
	}
	
		
	public KbeeRuntimeException(String message) {
		super(message);
	}
	

	public KbeeRuntimeException(String message, Throwable e) {
		super(message, e);
	}
}

package com.novamens.lock;

public class ObjectLockedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ObjectLockedException(String message) {
		super(message);
	}
}

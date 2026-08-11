package com.novamens.content.base;

public class ConstraintException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ConstraintException(Exception e) {
		super(e);
	}
}

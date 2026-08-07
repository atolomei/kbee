package com.novamens.service;

public class ServiceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ServiceNotFoundException(final Class<?> service) {
		super("ServiceNotFound "+ service.getName()); 
	}
}

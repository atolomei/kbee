package com.novamens.content.service;

public interface GenericDomService<T> extends DomService {
	public T getObject();
	public boolean setObject(Object Object);
}
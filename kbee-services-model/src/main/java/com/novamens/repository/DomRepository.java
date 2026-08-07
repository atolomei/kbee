package com.novamens.repository;

import java.io.Serializable;
import java.util.List;

import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;

public interface DomRepository<T> {
	public void save (T object);
	public void delete(T object);
	public T findById(Serializable id); 
	public T findByExternalId(String id); 
	public List<T> findAll();
	public List<T> findAll(Domain domain);
	public List<T> findAll(ObjectState state);
	public List<T> findAll(Domain domain, String order);
	public List<T> findAll(Domain domain, ObjectState state);
	public List<T> findAll(Domain domain, ObjectState state, String order);
	
	public long getTotal();
	public long getTotal(Domain domain);
}

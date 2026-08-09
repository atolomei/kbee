package com.novamens.wicket.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class ObjectModel<T> implements IModel<T> {
	private static final long serialVersionUID = 1L;
	private T object;
	private Class<?> clazz = null;
	private Serializable id;
	private boolean detached = false;
	
	public ObjectModel(T object) {
		setObject(object);
	}
	
	public ObjectModel(T object, boolean detached) {
		setObject(object);
		if (detached)
			detach();
	}
	
	public ObjectModel(T object, Class<?> clazz) {
		this.clazz = clazz;
		setObject(object);
	}
	
	public ObjectModel(Class<?> clazz, Serializable id) {
		this.id = id;
		this.clazz = clazz;
		detached = true;
	}
	
	public void setObject(T object) {
		this.object = object;
		if (object!=null)
		id = ((com.novamens.security.Identifiable)object).getId();
	}
	
	@SuppressWarnings("unchecked")
	public T getObject() throws ObjectNotFoundException {
		if (detached) {

			if (id==null)
				throw new ObjectNotFoundException(id, clazz!=null ?clazz.getName() : "no class");
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			detached = false;
			object = (T)sf.getCurrentSession().load(clazz, id);
			if (object==null)
				throw new ObjectNotFoundException(id, clazz.getName());
		}	
			
		return object;
	}
	
	
	/**
	 * Depends on the Hibernate version
	 */
	public void detach() {
		try {

			if (detached) 
				return;
			
			if (clazz==null) {
				String classname = object.getClass().getName();
				int i = classname.indexOf("_");
				if (i>0) classname = classname.substring(0, i);
				i = classname.indexOf("$");
				if (i>0) classname = classname.substring(0, i);
				clazz = Class.forName(classname);
			}
		
			id= ((com.novamens.security.Identifiable)object).getId();
			
			detached = true;
			object = null;
		}
		catch (java.lang.NullPointerException e1 ) {
			detached = true;
		}
		catch (ClassNotFoundException  e2 ) {
			throw new RuntimeException(e2);
		}
		catch (org.hibernate.ObjectNotFoundException e3 ) {
			detached = true;
		}

	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectModel<?>) {
			return ((ObjectModel<?>)obj).id.equals(id);
		}
		return false;
	}
	
	public Serializable getId() {
		return id;
	}
	
	@Override
	public int hashCode()    {
		return id.hashCode();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		if (!detached) {
			detach();
		}	
		if (!detached) {
			Assert.isTrue(detached, "!detached");
		}
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}
}

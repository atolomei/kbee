package com.novamens.dom;

import org.hibernate.Hibernate;

public interface Proxy<T> {
	
	public T getObject();
	
	public static String getClassName(java.lang.Object object) {
		// String classname = object.getClass().getSimpleName().toLowerCase();
		String classname = object.getClass().getSimpleName();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		return classname;		
	}
	
	public static String getFullClassName(java.lang.Object object) {
		String classname = object.getClass().getName();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		return classname;		
	}
	
	public static Class<?> getClass(Object object) {
		return Hibernate.getClass(object);
	}
}

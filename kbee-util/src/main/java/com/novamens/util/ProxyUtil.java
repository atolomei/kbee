package com.novamens.util;

// Los proxies Hibernate incluyen en el nombre de la clase un prefijo separado por $ o _

public class ProxyUtil {
	public static String getClassName(Object object) {
		String classname = object.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		return classname;		
	}
}

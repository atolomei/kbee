package com.novamens.dom;

import java.io.Serializable;

import com.novamens.security.Identifiable;

public class ObjectID implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String classname;
	private String objectype;
	
	public ObjectID(String classname, Serializable id, String objectype) {
		this.classname = classname;
		this.id = id.toString();
		this.objectype=objectype;
	}
	
	public ObjectID() {
	}
	
	public ObjectID(String id) {
		
		String arr[] = id.split("-");

		this.objectype=arr[0];

		if (arr.length>1) {
			this.classname = arr[1];
			this.id=arr[2];
		}
	}
	
	/**
	 * IMPORTANT: This method depends on Hibernate Classes
	 * 
	 * @param object
	 */
	public ObjectID(Identifiable object) {
		classname = object.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		this.id = String.valueOf(object.getId());
		this.objectype="Id";
	}
	
	public ObjectID(Object obj, String id, String objectype) {
		classname = obj.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		this.id = id;
		this.objectype=objectype;
	}
	
	public String toString() {
		return objectype+"-"+classname+"-"+id;
	}

	protected void setClassName(String classname) {
		this.classname=classname;
	}
	
	protected void setId(String id) {
		this.id=id;
	}

	protected void setObjectType(String tpy) {
		this.objectype=tpy;
	}

	public String getId() {
		return id;
	}
	
	public String getClassName() {
		return classname;
	}

	public String getObjectType() {
		return objectype;
	}

	@Override
	public boolean equals(java.lang.Object object) {
		return (object instanceof ObjectID && (((ObjectID)object).getId().equals(id) && ((ObjectID)object).getObjectType().equals(objectype)));
	}
	
	@Override
	public int hashCode() {
		return (objectype+id).hashCode();
	}
}

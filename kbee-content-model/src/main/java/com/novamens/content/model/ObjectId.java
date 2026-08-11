package com.novamens.content.model;

import java.io.Serializable;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.security.Identifiable;

/**
 */
public class ObjectId implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String classname;
	
	public ObjectId(String classname, Serializable id) {
		this.classname = classname;
		this.id = id.toString();
	}
	
	/**
	 * 
	 * This depends on the Hibernate version
	 * 
	 */
	public ObjectId(Object object) {
		if (object instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)object;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			object = initializer.getImplementation();
		}
		if (object instanceof String) {
			String id = (String)object;
			int i = id.indexOf("#");
			if (i>0) {
				classname = id.substring(0,i);
				this.id = id.substring(i+1);
			}
			else {
				i = id.indexOf("-");
				if (i>0) {
					classname = id.substring(0,i);
					this.id = id.substring(i+1);
				}
				else {
					classname = "";
					this.id = id;
				}
			}	
		}
		else {
			classname = object.getClass().getSimpleName().toLowerCase();
			int i = classname.indexOf("_");
			if (i>0) classname = classname.substring(0, i);
			i = classname.indexOf("$");
			if (i>0) classname = classname.substring(0, i);
			id = ((Identifiable)object).getId()==null ? null : ((Identifiable)object).getId().toString();
		}
	}
	
	public ObjectId(Content content) {
		setObjectIdFromContent(content);
	}

	public void setObjectIdFromContent(Content content) {
		classname = content.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		id = content.getId().toString();
	}
	
	public ObjectId(ResourceContainer container) {
		if (container instanceof Content)
			setObjectIdFromContent((Content) container);
		else {
			classname = container.getClass().getSimpleName().toLowerCase();
			int i = classname.indexOf("_");
			if (i>0) classname = classname.substring(0, i);
			i = classname.indexOf("$");
			if (i>0) classname = classname.substring(0, i);
			id = container.getId().toString();
		}
	}
	
	public String toString() {
		return classname + "#" + id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getClassName() {
		return classname;
	}
	
	@Override
	public boolean equals(Object object) {
		return (object instanceof ObjectId && ((ObjectId)object).getId().equals(id));
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}

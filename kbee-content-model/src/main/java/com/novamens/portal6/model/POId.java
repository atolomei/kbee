package com.novamens.portal6.model;

import java.io.Serializable;

import com.novamens.dom.ObjectID;


public class POId extends ObjectID implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final static String TYPE ="POId";
	
	public POId(String classname, Serializable id) {
		super(classname, id, TYPE);
	}

	public POId(String id) {
		setObjectType(TYPE);
		int i = id.indexOf("-");
		if (i>0) {
			super.setClassName(id.substring(0,i));
			super.setId(id.substring(i+1));
		}
		else {
			super.setClassName("");
			super.setId(id);
		}
	}

	public POId(PortalObject po) {
		super(po, po.getId().toString(), TYPE);
	}
}

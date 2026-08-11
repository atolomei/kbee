package com.novamens.content.model;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.dom.ObjectID;

public class ContentId extends ObjectID implements Serializable {

	private static final long serialVersionUID = 1L;
	public final static String TYPE ="CId";
	
	public ContentId(String classname, Serializable id) {
			super(classname, id, TYPE);
	}
	
	public ContentId(String id) {
		setObjectType(TYPE);
		
		// int i = id.indexOf("-");
		
		String arr[] = id.split("-");
		
		if (arr.length>2) {
			// Cid-clazz-id
			//
			super.setClassName(arr[1]);
			super.setId(arr[2]);
			
			//super.setClassName(id.substring(0,i));
			//super.setId(id.substring(i+1));
			
		}
		else if (arr.length>1) {
			super.setClassName(arr[0]);
			super.setId(arr[1]);
		}
		else {
			 super.setClassName("");
			 super.setId(id);
		}	
	}
	
	public ContentId(Content content) {
		super(content, content.getId().toString(),TYPE);
	}
	
	

}

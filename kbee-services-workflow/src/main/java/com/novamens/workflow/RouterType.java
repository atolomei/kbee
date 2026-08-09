package com.novamens.workflow;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 
 * A Workflow Router decides what to do with a Content when a Task ends.
 * There are 5 Types of Routers, Task router 
 * 
 */
public enum RouterType {
	
	TASK (1, "task"), 
	PUBLISH (2, "publish"), 
	RETURN_TO_CALLER (3, "return"), 
	CANCEL (4, "cancel"),
	SCRIPT(5, "script"), 
	THREAD_END(6, "thread_end"); 
	
	private String key;
	//private String label;
	private int id;
	
	private RouterType(int code, String key) {
	
		this.id = code;
		this.key=key;
	}
	
	public String toString() {
		return ("id: " + String.valueOf(getId()) + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return getLabel( Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(this.getClass().getName(), locale);
		return res.getString(this.key);
	}
	
	public int getId() {
		return id;
	}
}
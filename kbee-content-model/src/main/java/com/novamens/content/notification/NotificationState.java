package com.novamens.content.notification;

import com.novamens.security.PersistentEnum;

public enum NotificationState implements PersistentEnum {

	PENDING (1, "PENDING"), 
	READ 	(2, "READ"), 
	ARCHIVED (3, "ARCHIVED");
	
	private String label;
	private int id;

	private  NotificationState(int code, String label) {
		this.label = label;this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return label;
	}
	
	public int getId() {
		return id;
	}
}

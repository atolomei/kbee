package com.novamens.hibernate.event;

import com.novamens.event.AppUpdateEvent;

public class HibernateUpdateEvent extends AppUpdateEvent {
	private Object[] currentState, previousState;
	private String[] propertyNames;
	
	public HibernateUpdateEvent(Object object, Object[] currentState, Object[] previousState, String[] propertyNames) {
		super(object);
		this.currentState = currentState;
		this.previousState = previousState;
		this.propertyNames = propertyNames;
	}
	
	public String[] getPropertyNames() {
		return propertyNames;
	}
	
	public Object[] getCurrentState() {
		return currentState;
	}
	
	public Object[] getPreviousState() {
		return previousState;
	}
}

package com.novamens.event;


public class AppUpdateEvent extends AbstractEvent {

	
	boolean isUpdateUserLists =true;
	
	public boolean isUpdateUserLists() {
		return isUpdateUserLists;
	}
	
	public AppUpdateEvent(Object object,  boolean isUpdateUserLists ) {
		super(object);
		 this.isUpdateUserLists =  isUpdateUserLists;
	}
	
	public AppUpdateEvent(Object object) {
		super(object);
	}

	@Override
	public boolean distributable() {
		return false;
	}
}

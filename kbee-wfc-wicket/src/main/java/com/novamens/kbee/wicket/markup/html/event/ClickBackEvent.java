package com.novamens.kbee.wicket.markup.html.event;

import java.time.Instant;

import org.apache.wicket.model.IModel;

public class ClickBackEvent<T>  implements WicketEvent {
	
	 IModel<T> model;
	 
	 public ClickBackEvent() {
		 
	 }
	 
	 public ClickBackEvent(IModel<T> model) {
		 this.model=model;
	 }
	 
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	@Override
	public Object getObject() {
		return 	model!=null ? model.getObject() : null;	
	}

}

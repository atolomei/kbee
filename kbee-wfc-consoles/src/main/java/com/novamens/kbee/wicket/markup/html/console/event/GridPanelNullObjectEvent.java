package com.novamens.kbee.wicket.markup.html.console.event;


import java.time.Instant;

import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.WicketEvent;

public class GridPanelNullObjectEvent<T> implements WicketEvent {
				
	 IModel<T> model;
	 
	 public GridPanelNullObjectEvent(IModel<T> model) {
			this.model=model;
	}
		
	public  IModel<T> getModel() {
			return this.model;
	}
		
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	@Override
	public Object getObject() {
		return model.getObject();
	}

}

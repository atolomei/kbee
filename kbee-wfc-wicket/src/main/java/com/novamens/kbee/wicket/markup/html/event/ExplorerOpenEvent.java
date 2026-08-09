package com.novamens.kbee.wicket.markup.html.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;

public class ExplorerOpenEvent<T> implements WicketEvent, IDetachable {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Serializable> map;
	private IModel<T> model;
	
	public ExplorerOpenEvent(IModel<T> model) {
		this.model=model;
	}
	
	public ExplorerOpenEvent(IModel<T> model, Map<String, Serializable> map) {
		this.map=map;
		this.model=model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public Map<String, Serializable> getMap() {
		return map == null ? new HashMap<String, Serializable>() : map;
	}
	
	
	public void detach() {
		if (model!=null)
			model.detach();
		
	}
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public Object getObject() {
		return model!=null?model.getObject(): null;
	}

}

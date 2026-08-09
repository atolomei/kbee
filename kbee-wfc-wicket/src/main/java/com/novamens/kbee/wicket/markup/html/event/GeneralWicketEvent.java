package com.novamens.kbee.wicket.markup.html.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class GeneralWicketEvent implements WicketEvent {

	private String name;
	private Map<String, Serializable> map;
	
	
	public GeneralWicketEvent(String name) {
		this.name=name;
	}
	
	public GeneralWicketEvent(String name, Map<String, Serializable> map) {
		this.name=name;
		this.map=map;
	}
	
	public Map<String, Serializable> getMap() {
		return map == null ? new HashMap<String, Serializable>() : map;
	}
	
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public Object getObject() {
		return name;
	}

}

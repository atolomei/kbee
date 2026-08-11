package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.Locale;

public class KbeeProcessPhase implements ProcessPhase  {

	private Long id;
	private String key;
	private String icon;
	
	public KbeeProcessPhase(Long id, String key, String icon) {
		this.id = id;
		this.key = key;
		this.icon = icon;
	}
	 
	public String getKey() {
		return key;
	}
	public Serializable getId() {
		
		return id;
	}
	
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String getLabel(Locale locale) {
		return key;
	}
	
	
}

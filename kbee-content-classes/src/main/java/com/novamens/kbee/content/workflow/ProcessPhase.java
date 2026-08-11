package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.Locale;

public interface ProcessPhase {

	public String getKey();
	public Serializable getId();
	public String getIcon();
	
	public String getLabel();
	public String getLabel(Locale locale);
}

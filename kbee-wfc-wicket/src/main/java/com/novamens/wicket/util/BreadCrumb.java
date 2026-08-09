package com.novamens.wicket.util;

import java.util.ArrayList;
import java.util.List;

/**
 *   BreadCrumb has a List of BCElement, which are Links (WebMarkupContainer) 
 */
public class BreadCrumb {

	private List<BCElement> elements = new ArrayList<BCElement>();
	
	public boolean is_last_active = true;
		
	public BreadCrumb(BCElement... elements) {
		for (int e=0; e<elements.length; e++) {
			this.elements.add(elements[e]);
		}
		
	}
	
	public void setLastActive(boolean is_last_active) {
		this.is_last_active =is_last_active;
	}
	
	public boolean isLastActive() {
		return is_last_active;
	}
	
	public List<BCElement> elements() {
		return elements;
	}
}

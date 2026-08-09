package com.novamens.kbee.wicket.util;

import org.apache.wicket.markup.html.panel.Panel;

public class InvisiblePanel extends Panel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvisiblePanel(String id) {
		super(id);
	}
	
	public boolean isVisible() {
		return false;
	}

}

package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;


public class InvisibleConsoleSidePanel extends ConsoleSidePanel {
			
	
	private static final long serialVersionUID = 1L;

	public InvisibleConsoleSidePanel(String id) {
		super(id);
	}
	
	public boolean isVisible() {
		return false;
	}

	@Override
	public void onClose(AjaxRequestTarget target) {
	}
}

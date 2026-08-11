package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.wicket.markup.html.panel.Panel;
 

public class WrapperPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrapperPanel(String id, Panel panel) {
		super(id);
		add(panel);
	}
}

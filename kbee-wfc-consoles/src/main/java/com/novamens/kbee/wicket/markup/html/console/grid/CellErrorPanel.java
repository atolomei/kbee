package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class CellErrorPanel extends Panel {
	private static final long serialVersionUID = -3181703784123345388L;

	public CellErrorPanel(String id, String label) {
		super(id);
		add(new Label("label", label));
	}

}

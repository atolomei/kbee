package com.novamens.content.web.markup;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.util.KbeeRuntimeException;


public class HitPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	
	public HitPanel() {
		super("hitpanel");
		throw new KbeeRuntimeException ("deprecated in 6.3");
	}
	
	
}

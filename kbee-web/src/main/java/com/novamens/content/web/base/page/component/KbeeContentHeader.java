package com.novamens.content.web.base.page.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;

public class KbeeContentHeader extends Panel {

	private static final long serialVersionUID = 3152181538697834356L;

	public KbeeContentHeader(String id) {
		this(id, null);
	}
	
	public KbeeContentHeader(String id, IModel<? extends Content> model) {
		super(id);
		String type = model!=null?model.getObject().getContentTemplate().getName():"Type N/A";
		add(new Label("title",   type + ". id: " + (model!=null?model.getObject().getId():"N/A")));
	}
}

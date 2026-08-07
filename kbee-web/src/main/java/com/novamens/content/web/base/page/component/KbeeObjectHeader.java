package com.novamens.content.web.base.page.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.Identifiable;

@SuppressWarnings("serial")
public class KbeeObjectHeader<T> extends Panel {
	private static final long serialVersionUID = 1L;
	private IModel<T> model;

	public KbeeObjectHeader(String id) {
		this(id, null);
	}
	
	public KbeeObjectHeader(String id, IModel<T> model) {
		super(id);
		
		setModel(model);
 	
		add(new Label("title", new Model<String>() {
			public String getObject() {
				IModel<T> model = KbeeObjectHeader.this.getModel(); 
				String idstr = model!=null && model.getObject() instanceof Identifiable ? (((Identifiable) model.getObject()).getId()).toString() : "N/A";
				return "Object. id: " + idstr;
			}
		}));
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
}

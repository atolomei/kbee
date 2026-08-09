package com.novamens.kbee.wicket.markup.html;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class LinkCellItem<T> extends Panel {

	private static final long serialVersionUID = 1L;

	IModel<T> model;
	
	public LinkCellItem(String id, IModel<T> model, IModel<String> label) {
		super(id);
	
		this.model=model;
		
		Link<Void> link = new Link<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				LinkCellItem.this.onClick();
			}
		};

		Label zlabel = new Label("label", label);
		
		link.add(zlabel);
		add(link);
		
		if (target()!=null)
			link.add(new AttributeModifier("target", target()));
		
		if (getLinkCss()!=null)
			link.add(new AttributeModifier("class", getLinkCss()));
		
		if (getLabelCss()!=null)
			zlabel.add(new AttributeModifier("class", getLabelCss()));
		
	}

	public void onClick() {
	}
	
	public IModel<T> getModel() {
		return model;
	}
	public T getModelObject() {
		return getModel().getObject();
	}

	public String target() {
		return null;
	}
	
	public String getLinkCss() {
		return null;
	}
	
	public String getLabelCss() {
		return null;
	}
	
	
	
	
}

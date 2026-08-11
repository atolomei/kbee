package com.novamens.content.web.admin.markup;


import org.apache.wicket.ajax.AjaxRequestTarget;
 
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.wicket.util.BCElement;

public class BCAjaxElement extends BCElement {

	private static final long serialVersionUID = 1L;
	

	public BCAjaxElement(IModel<String> model) {
		super(model);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public AbstractLink getLink(String id) {
		return new WorkingAjaxLink(id) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				BCAjaxElement.this.onClick(target);	
			}
		};		
	}
	
	public void onClick(AjaxRequestTarget target) {
		
	}

}

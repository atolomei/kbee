package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.actions.MenuItem;

public abstract class AjaxMenuItem implements MenuItem {
	
	private static final long serialVersionUID = 1L;
	
	private IModel<?> model;
	
	public AjaxMenuItem() {
	}
	
	public AjaxMenuItem(IModel<?> model) {
		this.model = model;
	}

	public void onClick() throws Exception {
	}

	public String getTarget() {
		return null;
	}
	
	public IModel<?> getModel() {
		return model;
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public boolean isVisible() {
		return true;
	}
	
	public String getCssClass() {
		return null;
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}
	
	public void detach() {
		if (model!=null) 
			model.detach();
		
	}
	
	public abstract void onClick(AjaxRequestTarget target) throws Exception;
}

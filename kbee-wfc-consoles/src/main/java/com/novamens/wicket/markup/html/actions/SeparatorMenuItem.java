package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.actions.MenuItem;


public class SeparatorMenuItem implements MenuItem {
	private static final long serialVersionUID = -1161360861677620245L;

	private IModel<String> label  =null;
	

	public SeparatorMenuItem() {}
	
	public SeparatorMenuItem(IModel<String> label) {
		this.label=label;
	}
	
	@Override
	public void onClick() throws Exception {
	}

	@Override
	public String getLabel() {
		if (label==null)
			return null;
		return label.getObject();
	}

	@Override
	public boolean isEnabled() {
		return false;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public String getTarget() {
		return null;
	}

	@Override
	public String getCssClass() {
		return label!=null ? "label-separator" : "separator";
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}

}

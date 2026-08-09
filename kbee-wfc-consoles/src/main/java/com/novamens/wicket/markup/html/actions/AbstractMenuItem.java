package com.novamens.wicket.markup.html.actions;

import com.novamens.wicket.markup.html.actions.MenuItem;

public abstract class AbstractMenuItem implements MenuItem {
	private static final long serialVersionUID = 1L;

	@Override
	public void onClick() throws Exception {
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	public String getTarget() {
		return null;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	public String getBeforeClick() {
		return null;
	}
}

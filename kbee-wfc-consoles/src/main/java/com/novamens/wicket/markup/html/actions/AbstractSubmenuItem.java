package com.novamens.wicket.markup.html.actions;

import com.novamens.wicket.markup.html.actions.SubmenuItem;

public abstract class AbstractSubmenuItem implements SubmenuItem {
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
	public String getBeforeClick() {
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

	@Override
	public int getLeft() {
		return 148;
	}
}

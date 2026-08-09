package com.novamens.wicket.markup.html.actions;



public class SeparatorMenuItemPanelV5<T> extends AbstractMenuItemPanelV5<T> implements MenuItem {

	 
	private static final long serialVersionUID = 6782339527228627508L;

	public SeparatorMenuItemPanelV5(String id) {
		super(id);
	}

	@Override
	public void onClick() throws Exception {
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getTarget() {
		return null;
	}

	@Override
	public String getCssClass() {
		return "divider";
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}

}

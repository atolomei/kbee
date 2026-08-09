package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;


public class AjaxCheckMenuItemPanelV5<T> extends AjaxMenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;

	private long time = 0;
	
	public AjaxCheckMenuItemPanelV5(String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	

	public AjaxCheckMenuItemPanelV5(String id, IModel<T> model) {
		super(id, model);
		this.setOutputMarkupId(true);
	}

	
	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
		
		long now = System.currentTimeMillis();
		
		if ((now-time)<700) 
			return;
		
		time = now;
		
		onCheckClick(target);
	}
	
	public boolean isIconVisible() {
		return false;
	}
	
	@Override
	public String getCssClass() {
		if (isIconVisible())
			return "label-selected";
		else
			return "label-no-selected";
	}

	@Override
	public String getIconCssClass() {
		return isIconVisible() ? (CHECK + " toright fa-fw"): "";
	}
	
	public void onCheckClick(AjaxRequestTarget target) throws Exception {
	}
}

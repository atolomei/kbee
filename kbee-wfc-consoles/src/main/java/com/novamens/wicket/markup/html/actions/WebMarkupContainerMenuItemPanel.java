package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class WebMarkupContainerMenuItemPanel<T> extends AbstractMenuItemPanelV5<T> {

	
	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer wb;
	
	public WebMarkupContainerMenuItemPanel(String id, IModel<T> model, WebMarkupContainer panel) {
		super(id, model);
		if (!panel.getId().equals("panel"))
			throw(new IllegalArgumentException("panel must have id='panel' "));
		this.wb=panel;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (wb==null)
			wb= new InvisiblePanel("panel");
		addOrReplace(wb);
	}
	
	
	
	@Override
	public void onClick() throws Exception {
	
	}

	@Override
	public String getLabel() {
		return "label";
	}

	@Override
	public String getBeforeClick() {
		return null;
	}

}

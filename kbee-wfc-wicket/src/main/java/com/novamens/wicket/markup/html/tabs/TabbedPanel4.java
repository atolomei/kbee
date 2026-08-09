package com.novamens.wicket.markup.html.tabs;


import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;


public class TabbedPanel4  extends AjaxTabbedPanel<ITab> {
	private static final long serialVersionUID = 1L;

	public TabbedPanel4(final String id, final List<ITab> tabs)	{
		super(id, tabs, null);
	}
	

	@Override
	protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index)
	{
		Component c= super.newTitle(titleId, titleModel, index);
		c.setEscapeModelStrings(false);
		return c;
	}
}

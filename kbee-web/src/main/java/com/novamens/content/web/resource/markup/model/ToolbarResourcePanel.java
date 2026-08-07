package com.novamens.content.web.resource.markup.model;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.TextField;

public class ToolbarResourcePanel extends Panel {
					
	public ToolbarResourcePanel() {
		super("s");
		setOutputMarkupId(true);
	}
	

}

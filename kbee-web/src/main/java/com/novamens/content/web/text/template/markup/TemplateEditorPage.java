package com.novamens.content.web.text.template.markup;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.kbee.content.text.template.TemplateData;

import kbee.web.nav.TabNavigationBar;
import kbee.web.page.BootstrapApplicationPage;

public class TemplateEditorPage<T extends Content> extends BootstrapApplicationPage<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<TemplateData> datamodel = new Model<TemplateData>(new TemplateData());
	
	public TemplateEditorPage(IModel<T> model, ContentTextTemplate template) {
		super(model, new TabNavigationBar<T>("navigation"));
		setPageTitle(new Model<String>(template.getTitle()));
		add(new TemplateEditorMainPanel<T>(model, datamodel, template));
	}
	
	 
}
package com.novamens.content.web.content.markup;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.SubsectionTemplate;

@SuppressWarnings("serial")
public class SubsectionPanel extends Panel  {

	private IModel<SubsectionTemplate> templatemodel;

	public SubsectionPanel(String id, IModel<SubsectionTemplate> model) {
		super(id);
		setOutputMarkupId(true);
		this.templatemodel = model;
	}
	
	public SubsectionTemplate getTemplate() {
		return this.templatemodel.getObject();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		Label label = new Label("subsection-name", new Model<String>() {
			public String getObject() {
				return getTemplate().getElement().getName(); 
			}
		});
		label.setEscapeModelStrings(false);
		add(label);
	}
}

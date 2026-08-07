package com.novamens.content.web.text.template.markup;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.wicket.model.ModelPanel;

@SuppressWarnings("serial")
public class PreviewPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	public PreviewPanel(IModel<T> model, IModel<TemplateData> dataModel, ContentTextTemplate template) {
		super("preview", model);
		setOutputMarkupId(true);
		IModel<String> textmodel = new Model<String>() {
			public String getObject() {
				return getTemplateText(); 
			}
		};
		Label textlabel = new Label("text", textmodel);
		textlabel.setEscapeModelStrings(false);
		add(textlabel);
	}
	
	protected String getTemplateText() {
		return "";
	}
}

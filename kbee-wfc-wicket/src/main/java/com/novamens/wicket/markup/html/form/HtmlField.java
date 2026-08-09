package com.novamens.wicket.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.model.IModel;

public abstract class HtmlField extends TextAreaField<String> {
	private static final long serialVersionUID = 1L;

	public HtmlField(String id) {
		super(id);
	}
	
	public HtmlField(String id, IModel<String> model) {
		super(id, model);
	}	
	
	public abstract String getFocusScript();
	
	public abstract String getCloseScript();
	
	public abstract void updateAjaxCloseAttributes(AjaxRequestAttributes attributes);
	
	public void onClose(AjaxRequestTarget target) {
	}
}

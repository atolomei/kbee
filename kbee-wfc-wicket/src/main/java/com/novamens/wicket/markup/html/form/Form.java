package com.novamens.wicket.markup.html.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;

public class Form<T> extends org.apache.wicket.markup.html.form.Form<T> {
	
    private static final long serialVersionUID = 1L;

	
	public static final String SPINNING ="far fa-sync fa-spin fa-fw spinning";
	public static final String FONTAWESOME = "fontawesome-pro-6.2.1-web/css/all.min.css";
	
	
	public static final String BOOTSTRAP = "bootstrap.css";
	public static final String BOOTSTRAP_JS = "bootstrap.js"; 
	
	public Disposition disposition = Disposition.VERTICAL;
	
	public enum Disposition {
		HORIZONTAL, 
		VERTICAL
	};
	
	public Form(String id) { 
		this(id, null, null);
	}
	
	public Form(String id, Disposition disposition) { 
		this(id, null, disposition);
	}
	
	public Form(String id, IModel<T> model, Disposition disposition) {
		super(id, model);
		setDisposition(disposition);
	}
	
	public void setDisposition(Disposition disposition) {
		this.disposition = disposition;
	}
	
	public Disposition getDisposition() {
		return disposition;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
	}
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		IValueMap attributes = tag.getAttributes();
		if (getDisposition()==Disposition.HORIZONTAL) {
			String css = attributes.getString("class");
			if (css == null) css = "";
			attributes.put("class", "form-horizontal "+css);
		}
		super.onComponentTag(tag);
	}
}

package com.novamens.wicket.markup.html.form;

import org.apache.wicket.model.IModel;



public class TextEmailField<T> extends TextField<T> {

	
	private static final long serialVersionUID = 1L;

	public TextEmailField(String id) {
		this(id, null, false, Width.W12);
	}
	
	public TextEmailField(String id, Width width) {
		this(id, null, false, width);
	}
	
	public TextEmailField(String id, boolean required) {
		this(id, null, required, Width.W12);
	}
	
	public TextEmailField(String id, IModel<T> model) {
		this(id, model, false, Width.W12);
	}
	
	public TextEmailField(String id, IModel<T> model, boolean required) {
		this(id, model, required, Width.W12);
	}
	
	public TextEmailField(String id, IModel<T> model, boolean required, Width width) {
		super( id, model, required, width, null);
	}
	
	@Override
	protected String getInputType() {
		return "email";
	}

}

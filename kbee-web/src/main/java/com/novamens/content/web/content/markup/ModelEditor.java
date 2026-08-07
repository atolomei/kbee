package com.novamens.content.web.content.markup;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

public abstract class ModelEditor <T extends Content> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private String error;

	public ModelEditor(String id) {
		super(id);
	}
	
	public void setError(String message) {
		this.error = message;
	}
	
	public String getError() {
		return error;
	}
}

package com.novamens.content.web.editor.markup;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.editor.Editor;


@SuppressWarnings("serial")
public class ObjectEditorPanel<T> extends Panel implements IFormModelUpdateListener {
	
	private Editor<T> editor2;
	
	private boolean readonly = false;
	
	public ObjectEditorPanel(String id) {
		super(id);
	}
	
	public void updateModel() {
	}
	
	public void cancel() {
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	public void setEditor2(Editor<T> editor) {
		this.editor2 = editor;
	}
	
	public T getModelObject2() {
		return getEditor2().getModelObject();
	}
	
	public IModel<T> getModel2() {
		return getEditor2().getModel();
	}
	
	@SuppressWarnings("unchecked")
	public Editor<T> getEditor2() {
 		if (editor2==null) {
			MarkupContainer parent = getParent();
			Editor<T> editor = null;
			while (editor==null && parent!=null) {
				if (parent instanceof Editor) {
					editor = (Editor<T>)parent;
					setEditor2(editor);
				}
				else
					parent = parent.getParent();
			}
		}
		return editor2;
	}
	
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	public void setReadOnly(boolean re) {
		this.readonly=re;
	}
	
	protected void setUpdatedPart(String part) {
		if (getEditor2()!=null)
			getEditor2().setUpdatedPart(part);
	}
}

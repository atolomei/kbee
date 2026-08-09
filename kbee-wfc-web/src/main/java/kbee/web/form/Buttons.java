package kbee.web.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

@SuppressWarnings("serial")
public class Buttons<T> extends Panel {
	private static final long serialVersionUID = 1L;
	private Editor<T> editor; 

	public Buttons(Editor<T> editor) {
		super("buttons");
		
		setEditor(editor);
		
		add(((ObjectEditor<T>)editor).new SubmitButton(editor.getForm()) {
			@Override 
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				((ObjectEditor<T>)getEditor()).setEditionEnabled(false);
				target.add((ObjectEditor<T>)getEditor());
			}
			@Override
			public String getBeforeHandler() {
				return getBeforeSubmitHandler();
			}
		});
		
		add(new AjaxLink<Void>("cancel") {
			public void onClick(AjaxRequestTarget target) {
				Buttons.this.onCancel(target);
			}
		});
		
		if (getCss()!=null || getSubmitCss()!=null) 
			get("submit").add(new AttributeModifier("class", getSubmitCss()!=null? getSubmitCss() : getCss()));
			
		if (getCss()!=null)
			get("cancel").add(new AttributeModifier("class", getCss()));
	}
	
	public void onCancel(AjaxRequestTarget target) {
		((ObjectEditor<T>)getEditor()).cancel(target);
		target.add((ObjectEditor<T>)getEditor());
	}
	
	@Override
	public boolean isVisible() {
		return getEditor().isEditionEnabled();
	}
	
	public Editor<T> getEditor() {
		return editor;
	}
	
	public void setEditor(Editor<T> editor) {
		this.editor = editor;
	}
	
	protected String getBeforeSubmitHandler() {
		return "";
	}
	
	protected String getCss() {
		return null;
	}

	protected String getSubmitCss() {
		return null;
	}
}

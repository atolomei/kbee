package kbee.web.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.editor.ObjectEditor.SubmitButton;

@SuppressWarnings("serial")
public class EditAndDeleteButtons<T> extends Panel {
	private static final long serialVersionUID = 1L;
	private Editor<T> editor; 

	
	public EditAndDeleteButtons(Editor<T> editor) {
			this(editor, false);
	}
	
	@SuppressWarnings("rawtypes")
	public EditAndDeleteButtons(Editor<T> editor, final boolean submit_only) {
		super("buttons");
		
		setEditor(editor);
		
		add( (new AjaxLink<Void>("edit") {
			public void onClick(AjaxRequestTarget target) {
				getEditor().edit(target);
				target.add((Component)getEditor());
			}
			@Override
			public boolean isVisible() {
				return !getEditor().isEditionEnabled();
			}
		}).add(new AttributeModifier("class",  getEditClass())));
		
		add( (new AjaxLink<Void>("delete") {
			public void onClick(AjaxRequestTarget target) {
				//getEditor().edit(target);
				target.add((Component)getEditor());
			}
			@Override
			public boolean isVisible() {
				return !getEditor().isEditionEnabled();
			}
		}));
		
		SubmitButton sb = ((ObjectEditor<T>)editor).new SubmitButton(editor.getForm()) {
			@Override 
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				((ObjectEditor<T>)getEditor()).setEditionEnabled(false);
				target.add((ObjectEditor<T>)getEditor());
			}
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled();
			}
			@Override
			public IModel<String> getLabel() {
				return getSubmitLabel();
			}
			@Override
			protected IModel<String> getWorkingLabel() {
				return new StringResourceModel("button.submiting", EditAndDeleteButtons.this, null);
			}
		};
		
		sb.add(new AttributeModifier("class",  getSubmitClass()));
		add(sb);
		
		
		AjaxLink<Void> cancel =	new AjaxLink<Void>("cancel") {
			public void onClick(AjaxRequestTarget target) {
 				((ObjectEditor<T>)getEditor()).cancel(target);
				target.add((ObjectEditor<T>)getEditor());
			}
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled() && !submit_only;
			}
		};
		
		cancel.add(new AttributeModifier("class",  getCancelClass()));
		add(cancel);
		
		
	}
	
	protected IModel<String> getSubmitLabel() {
		return new StringResourceModel("button.submit", EditAndDeleteButtons.this, null);
	}

	protected String getSubmitClass() {
		return "btn btn-primary btn-sm";
	}
	
	protected String getEditClass() {
		return "btn btn-primary btn-sm";
	}

	protected String getCancelClass() {
		return "btn btn-default btn-sm";
	}
	
	public Editor<T> getEditor() {
		return editor;
	}
	
	public void setEditor(Editor<T> editor) {
		this.editor = editor;
	}
}

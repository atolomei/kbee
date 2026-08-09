package kbee.web.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.editor.ObjectEditor.SubmitButton;

@SuppressWarnings("serial")
public class EditButtonsV5<T> extends Panel {
	private static final long serialVersionUID = 1L;
	private Editor<T> editor; 

	private boolean disable_after_submit = true;
		
	public EditButtonsV5(Editor<T> editor) {
		this(editor, false);
	}
	
	public void onAfterButtonClick(AjaxRequestTarget target) {
		
	}
	
	public void onEditClick(AjaxRequestTarget target) {
		getEditor().edit(target);
		target.add((Component)getEditor());
		onAfterButtonClick(target);
	}
					
	public void onCancelClick(AjaxRequestTarget target) {
		((ObjectEditor<T>)getEditor()).cancel(target);
		target.add((ObjectEditor<T>)getEditor());
		onAfterButtonClick(target);
	}

				
	public void onSubmitClick(AjaxRequestTarget target) {
		((ObjectEditor<T>)getEditor()).setEditionEnabled(!getDisableAfterSubmit());
		target.add((ObjectEditor<T>)getEditor());
		onAfterButtonClick(target);
	}
	
	
	@SuppressWarnings("rawtypes")
	public EditButtonsV5(Editor<T> editor, final boolean submit_only) {
		super("buttons");
		
		
		setEditor(editor);
		
		if (getStrStyle()!=null)
			add(new AttributeModifier ("style", getStrStyle()));
		
		
		add( (new AjaxLink<Void>("edit") {
			public void onClick(AjaxRequestTarget target) {
				EditButtonsV5.this.onEditClick(target);
			}
			@Override
			public boolean isVisible() {
				return !getEditor().isEditionEnabled();
			}
		}).add(new AttributeModifier("class",  getEditClass())));
		
		SubmitButton sb = ((ObjectEditor<T>)editor).new SubmitButton(editor.getForm()) {
			@Override 
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				onSubmitClick(target);
			}
			
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled();
			}
			@Override
			public IModel<String> getLabel() {
				return EditButtonsV5.this.getSubmitLabel();
			}
			@Override
			protected IModel<String> getWorkingLabel() {
				 return new StringResourceModel("button.submiting", EditButtonsV5.this, null);
			}
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				//super.updateAjaxAttributes(attributes);
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
					public CharSequence getBeforeHandler(Component component) { 
						return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave();";
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		};
		
		
		Label sbl = new Label("save", new Model<String>() {
			public String getObject() {
				return getSubmitLabel().getObject();
			}
		});
		
		sb.add(sbl);
		
		sb.add(new AttributeModifier("class",  getSubmitClass()));
		add(sb);
		
		
		AjaxLink<Void> cancel =	new AjaxLink<Void>("cancel") {
			public void onClick(AjaxRequestTarget target) {
				onCancelClick(target);
			}
			
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled() && !submit_only;
			}
		};
		
		cancel.add(new AttributeModifier("class",  getCancelClass()));
		add(cancel);
		
		
	}
	
	
	String style;
	
	public String getStrStyle() {
		return style;
	}

	
	public void setStrStyle( String s) {
		style=s;
	}

	public boolean getDisableAfterSubmit() {
		return this.disable_after_submit;
	}
	
	public void setDisableAfterSubmit(boolean b) {
		this.disable_after_submit=b;
	}
	
	protected IModel<String> getSubmitLabel() {
		return new StringResourceModel("button.submit", EditButtonsV5.this, null);
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

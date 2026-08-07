package com.novamens.content.web.security.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form.Disposition;


@SuppressWarnings("serial")
public class MonitorConditionEditor<T> extends com.novamens.wicket.markup.html.editor.ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;

	private Boolean enabled = true;
	
	public MonitorConditionEditor(String id, Editor<T> editor) {
		super(id);
		
		setOutputMarkupId(true);
		setEditor(editor);
		
		setEnabled((Boolean)(getCondition()!=null && !getCondition().contains("isHead")));
		
		add(new Label("label", "Monitor"));
		
		WebMarkupContainer selectorpanel = new WebMarkupContainer("selector-panel") {
			@Override
			public boolean isVisible() {
				return getEditor().isEditionEnabled(); 
			}
		};
		
		BooleanField selector = new BooleanField("value", new PropertyModel<Boolean>(this, "enabled")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				MonitorConditionEditor.this.setEnabled(getValue());
				MonitorConditionEditor.this.onUpdate(target);
				target.focusComponent(getInput());
				target.add(MonitorConditionEditor.this);
			}	
			@Override
			public Disposition getDisposition() {
				return Disposition.VERTICAL;
			}
			@Override 
			public boolean isVisible() {
				return getEditor().isEditionEnabled(); 
			}
		};
		
		selectorpanel.add(selector);
		selectorpanel.add(new AttributeModifier("class", "selector "));
		add(selectorpanel);
	}
	
	public void setEnabled(Boolean value) {
		this.enabled = value;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	@Override
	public boolean isVisible() {
		return getEditor().isEditionEnabled();
	}
	
	@Override
	public void updateModel() {
	}
	
	public String getDescription() {
		StringBuffer condition = new StringBuffer();
		
		if (!getEnabled()) {
			condition.append("<span class= \"predicate\" >isHead</span>");
			condition.append("<span class= \"iql-group-start\"> ( </span> ");
			condition.append("<span class= \"iql-value\" >true</span> ");
			condition.append("<span class= \"iql-group-end\"> ) </span> ");
		}
		else {
			condition.append("");
		}
		
		return condition.toString();
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		target.add(MonitorConditionEditor.this);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected boolean editionEnabled() {
		return getEditor().isEditionEnabled();
	}
	
	protected String getCondition() {
		return getEnabled() ? "" : "isHead(true)";
	}
}

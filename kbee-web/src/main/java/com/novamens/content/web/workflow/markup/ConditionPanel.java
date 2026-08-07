package com.novamens.content.web.workflow.markup;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.EditorEvent;

@SuppressWarnings("serial")
public class ConditionPanel<T extends Content> extends ModelPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;
	
	private ManualEndCondition condition;

	public ConditionPanel(String id, IModel<WorkflowContext> model, ManualEndCondition condition) {
		super(id, model);

		setCondition(condition);
	}

	public ManualEndCondition getCondition() {
		return condition;
	}
	
	public void setCondition(ManualEndCondition condition) {
		this.condition = condition;
	}
		
	
	public Component getFocusField() {
		onInitialize();
		return ((ConditionEditor<?>)get("condition-editor")).getFocusField();
	}
	
	public void onInitialize() {
		super.onInitialize();
		if (get("condition-editor")==null) {
			addFields();
		}
	}

	protected void addFields() {
		
		add(new Label("condition", new Model<String>() {
			public String getObject() {
				return getCondition().getLabel();
			}
		}));
		
		add(new AjaxLink<Void>("close-link") {
			public void onClick(AjaxRequestTarget target) {
				fire(new EditorEvent(target, "CLOSE"));
				ConditionPanel.this.onClose(target);
			}
		});
		
		add(new ConditionEditor<T>("condition-editor", getModel(), getCondition()) {
			public void onCancel(AjaxRequestTarget target) {
				onClose(target);
			}
			public boolean validateContent() {
				return ConditionPanel.this.validate();
			}
			public boolean reValidateContent() {
				return ConditionPanel.this.reValidate();
			}
			public void onDetach() {
				super.onDetach();
				ConditionPanel.this.onDetach();
			}
		});
	}

	public void onClose(AjaxRequestTarget target) {
	}
	
	public void onSubmit(AjaxRequestTarget target) {
	}
	
	protected boolean validate() {
		return true;
	}
	
	protected boolean reValidate() {
		return true;
	}
}

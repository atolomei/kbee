package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.model.Classifier;
import com.novamens.kbee.content.workflow.ManualEndCondition;

public interface TaskEditor {
	@Deprecated
	public void showEndConditionPanel(AjaxRequestTarget target, ManualEndCondition condition);
	@Deprecated
	public void showAttributes(AjaxRequestTarget target, Classifier classifier);
	public boolean validate(ManualEndCondition action);
}
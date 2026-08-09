package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.workflow.Task;

public class ActionEvent extends AbstractWicketAjaxEvent {
	
	Task task;
	EndCondition action;
								
	public ActionEvent(AjaxRequestTarget target, Task task, EndCondition action) {
		super(target);
		this.task = task;
		this.action= action;
	}
	
	public Task getTask() {
		return this.task;
	}
	
	public EndCondition getAction() {
		return this.action;
	}

}
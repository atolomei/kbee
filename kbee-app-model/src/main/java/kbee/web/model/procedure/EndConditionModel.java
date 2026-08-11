package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;

import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.workflow.Task;

public class EndConditionModel<T extends EndCondition> implements IModel<T> {
	private static final long serialVersionUID = 1L;
	private String event;
	private IModel<Task> taskmodel;
	private T condition;
	
	public EndConditionModel(IModel<Task> model, T condition) {
		this.condition = condition;
		event = condition.getEvent();
		taskmodel = model;
		detach();
	}
	
	@SuppressWarnings("unchecked")
	public T getObject() {
		if (this.condition==null) {
			for (EndCondition condition : ((UserTask)getTask()).getEndConditions()) {
				if (condition.getEvent().equals(this.event)) {
					this.condition = (T)condition;
					break;
				}
			}
		}
		return this.condition;
	}
	
	public void setObject(EndCondition task) {
	}
	
	public void detach() {
		taskmodel.detach();
		if (condition!=null)
		event = condition.getEvent();
		condition = null;
	}
	
	public Task getTask() {
		return taskmodel.getObject();
	}
}
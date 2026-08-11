package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

public class TaskModel implements IModel<Task> {
	private static final long serialVersionUID = 1L;
	
	private String taskid;
	private IModel<Procedure> proceduremodel;
	private Task task;
	
	public TaskModel(IModel<Procedure> model, Task task) {
		setObject(task);
		proceduremodel = model;
	}
	
	public TaskModel(Task task) {
		setObject(task);
		proceduremodel = new ObjectModel<Procedure>(task.getProcedure().getMaster());
	}
	
	public Task getObject() {
		if (task==null) {
			task = getProcedure().getTask(taskid);
//			for (Task task : getProcedure().getTasks()) {
//				if (task.getId().equals(this.taskid)) {
//					this.task = task;
//					break;
//				}
//			}
		}
		return task;
	}
	
	public void setObject(Task task) {
		this.task = task;
		taskid = task.getId();
	}
	
	@Override
	public void detach() {
		if (proceduremodel!=null)
			proceduremodel.detach();
		task = null;
	}
	
	public Procedure getProcedure() {
		return proceduremodel.getObject();
	}
	
	public IModel<Procedure> getProcedureModel() {
		return proceduremodel;
	}
}
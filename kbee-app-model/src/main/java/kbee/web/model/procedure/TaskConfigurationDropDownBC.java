package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.web.nav.DropDownMenuBC;

public class TaskConfigurationDropDownBC extends DropDownMenuBC<Procedure> {
	private static final long serialVersionUID = 1L;

	private IModel<Task> taskmodel;
	
	public TaskConfigurationDropDownBC(IModel<Procedure> model, IModel<Task> taskmodel) {
		super(model);
		this.taskmodel = taskmodel;
		addElement(new BCElement(new Model<String>(this.taskmodel.getObject().getName()+" <span class=\"ago\">("+new StringResourceModel("task", this, null).getObject()+")</span>")), true);

		for (Task ta: model.getObject().getTasks() ) {
			addElement(
				new HREFBCElement(getServerUrl()+"/model/task/"+ 
						String.valueOf(getModel().getObject().getId()) +"/"+
						String.valueOf(getTaskModel().getObject().getId()) +"/"+
						String.valueOf(ta.getId()),
						new Model<String>( ta.getDisplayName()))
			);
		}
	}

	public IModel<Task> getTaskModel() {
		return this.taskmodel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
			if (this.taskmodel!=null)
				this.taskmodel.detach();
	}

}

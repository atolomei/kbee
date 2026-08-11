package kbee.web.model.procedure;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.wicket.util.BCElement;
import com.novamens.workflow.Task;

/**
 * 
 *
 */
public class TaskConfigurationBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	IModel<Task> model;
    //IModel<Procedure> proc_model;
	//IModel<ProcessLauncher> launcher_model; 
	//IModel<ContentTemplate> tem_model;

	
	public TaskConfigurationBC(IModel<Task> model) {
		super();
		
		this.model=model;
		//this.proc_model=proc_model;
		//this.tem_model=tem_model;
		//this.launcher_model=launcher_model;

	}
	
	@Override
	public IModel<String> getLabel() {
		return new Model<String>(model.getObject().getName());
	}
	
//	@Override
//	public void onDetach() {
//		this.model.detach();
//		this.proc_model.detach();
//		this.tem_model.detach();
//		this.launcher_model.detach();
//		super.onDetach();
//	}
	
	@Override
	public void onClick() {
		setResponsePage(new TaskConfigurationPage(this.model));
	}
}

package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowThread;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowThread;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class ThreadsEditor extends RelationEditor<ForkJoinTask, WorkflowThread> {	
	private static final long serialVersionUID = 1L;
	
	public class ThreadModel implements IModel<WorkflowThread> {
		private IModel<Task> taskmodel;
		private transient WorkflowThread thread;
		public ThreadModel(WorkflowThread thread) {
			this.thread = thread;
		}
		public WorkflowThread getObject() {
			if (thread==null) {
				thread = new KbeeWorkflowThread();
				if (taskmodel!=null)
				((KbeeWorkflowThread)thread).setTask(taskmodel.getObject());
			}
			return thread;
		}
		public void setObject(WorkflowThread thread) {
			this.thread = thread;
		}
		public void detach() {
			if (thread!=null) {
			IModel<Procedure> model = new ObjectModel<Procedure>(getProcedure());
			if (thread.getTask()!=null) {
				taskmodel = new TaskModel(model, thread.getTask());
				taskmodel.detach();
			}
			}
			thread=null;
		}
	}
			
//	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ThreadsEditor.class.getName());

//	private IModel<ContentTemplate> templatemodel;
	private IModel<Task> taskmodel;

	public ThreadsEditor(String id, IModel<Task> taskmodel) {
		super(id);
		this.taskmodel = taskmodel;
	}
	
//	public ContentTemplate getTemplate() {
//		return templatemodel.getObject();
//	}
	
	@Override
	public String getTarget() {
		return null;
	}
	
	@Override
	public String getProperty() {
		return "threads"; 
	}
	
	public Task getTask() {
		return taskmodel.getObject();
	}
	
	public Procedure getProcedure() {
		return ((KbeeTask)getTask()).getProcedure();
	}
	
	public void onDetach() {
		super.onDetach();
		taskmodel.detach();
	}
	
	protected void onValueClick(IModel<WorkflowThread> model) {
		
//		try {
//			if (model.getObject() instanceof KbeeTaskForm) {
//				setResponsePage(new RedirectPage( getServerUrl()+"/eform/"+getTemplate().getId().toString()+"/"+ ((KbeeTaskForm)model.getObject()).getId()));				
//			}
//		} 
//		catch (Exception e) {
//			logger.error(e);
//			setResponsePage(new ApplicationErrorPage<>(e));
//		}
	}


	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<String>() {
			@Override
 			public String getName() {
				return "name";
			}
			@Override
			public boolean getTitle() {
				return true;
			}
		});
//		properties.add(new Property<Task>() {
//			@Override
//			public String getName() {
//				return "task";
//			}
//			@Override
//			public List<Task> getChoices() {
//				return getTasks();
//			}
//			public IModel<Task> getModel(Task task) {
//				if (task==null) return null;
//				IModel<Procedure> model = new ObjectModel<Procedure>(getProcedure());
//				return new TaskModel(model, task);
//			}
//		});
		properties.add(new Property<Procedure>() {
			@Override
			public String getName() {
				return "procedure";
			}
			@Override
			public List<Procedure> getChoices() {
				return getProcedures();
			}
			public IModel<Procedure> getModel(Procedure procedure) {
				if (procedure==null) return null;
				return new SubprocedureModel(procedure);
			}
		});	
		return properties;
	}
	
	public List<Task> getTasks() {
		return getProcedure().getTasks();
	}
	
	public List<Procedure> getProcedures() {
		return getProcedure().getSubprocedures();
	}
	
	protected Property<?> getKey() {
		return null;
	}
	
	@Override
	public IModel<WorkflowThread> getModel(WorkflowThread thread) {
		return new ThreadModel(thread);
	}
	
	@Override
	protected String getTitle(WorkflowThread value) {
		return value.getProcedure()!=null ? value.getProcedure().getDisplayName() : "No Procedure";
	}

	@Override
	protected WorkflowThread getNewValue() {
		return new KbeeWorkflowThread();
	}
}
package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.TaskPageFactory;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;

public class TaskBackupEditor extends ObjectEditor<Task> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskBackupEditor.class.getName());

	private TaskPageFactory taskPageFactory;
	private String taskName; 

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	/**
	 * @param id
	 * @param model
	 */
	public TaskBackupEditor(String id, IModel<Task> model) {
		super(id, model);
		setOutputMarkupId(true);
		setEditionEnabled(false);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		
		form.add(new TextAreaField<String>("precondition", 4, 40) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to express a Condition"; }, getPredicatesHelp());
			};
		});
		
		form.add(new ChoiceField<Task>("taskOnPreconditionFail", new PropertyModel<List<Task>>(this, "tasks")) {
			private static final long serialVersionUID = 1L;
			protected IModel<Task> getModel(Task value) {
				IModel<Procedure> proceduremodel = new ObjectModel<Procedure>(((KbeeTask)value).getProcedure());
				return new TaskModel(proceduremodel, value);
			}	
		});
	
		add(form);
		
		add(new EditButtonsV5<Task>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return role_admin || role_model;
			}
		});
		
		add(new InfoDialog("help-modal"));
		
		
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeTask task = (KbeeTask)getTask();
				getModel().setObject(task);
				KbeeProcedure procedure = (KbeeProcedure)(task.getProcedure());
				onUpdate(target);
				procedure.getDomain().getService(WorkflowDomainService.class).update(procedure, getUpdateDescription());
				reset();
			}
		} 
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	public List<TaskPageFactory> getFactories() {
		List<TaskPageFactory> factories = new ArrayList<TaskPageFactory>();
		Map<String, TaskPageFactory> factoriesmap = ServiceLocator.getService(BeansService.class).getBeansOfType(TaskPageFactory.class);
		for (String bean : factoriesmap.keySet()) {
			TaskPageFactory factory = (TaskPageFactory)ServiceLocator.getService(BeansService.class).getBean(bean);
			factories.add(factory);
		}
		return factories;
	}
	
	public void setTaskPage(TaskPageFactory factory) {
		this.taskPageFactory = factory;
	}
	
	public TaskPageFactory getTaskPage() {
		return this.taskPageFactory;
	}

	public List<Task> getTasks() {
		return ((KbeeProcedure)((KbeeTask)getTask()).getProcedure()).getTasks();
	}
	
	
	public Domain getDomain() {
		KbeeProcedure procedure = (KbeeProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getDomain();
	}

	public Task getTask() {
		return getModel().getObject();
	}
	
	public Procedure getProcedure() {
		return ((KbeeTask)getTask()).getProcedure();
	}

	protected void setTaskPage(String factoryName) {
 		if (factoryName!=null)
		for (TaskPageFactory factory : getFactories()) {
			if (factory.getName().equals(factoryName)) {
				taskPageFactory = factory;
				break;
			}
		}
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	private String getUpdateDescription() {
		String text = getLabel("task.auditname", taskName).getObject();
		String parts = "";
		for (String part : getUpdatedParts()) {
			parts += "".equals(parts) ? " " : ", ";
			parts += part; 
		}
		text += parts;
		return text;
	}
	
	

		
	
	
}

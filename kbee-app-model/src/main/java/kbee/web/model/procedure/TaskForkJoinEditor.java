package kbee.web.model.procedure;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.JsEvaluator;
import com.novamens.kbee.content.workflow.KbeeContentProcedure;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeScriptRouter;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Task;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class TaskForkJoinEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskForkJoinEditor.class.getName());

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

//	private TaskPageFactory taskPageFactory;
	private String taskName; // nombre inicial para auditoria

	
	class IdValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String id = validatable.getValue();
			int founds = 0;
			for (Task task : getProcedure().getTasks()) {
				if (id!=null && id.toLowerCase().equals(task.getId().toLowerCase())) {
					founds++;
				}
			}
			if (founds>1) {
				validatable.error(new ValidationError(this));
			}	
		}
	}
	
//	class IqlValidator implements IValidator<String> {
//		@Override
//		public void validate(final IValidatable<String> validatable) {
//			String statement = validatable.getValue();
//			try {
//				if ((statement==null || "".equals(statement)))
//					return;
//				IqlService iqlservice = getDomain().getService(IqlService.class);
//				ResultSet set = iqlservice.execute(statement);
//				set.hasNext();
//			} 
//			catch (RuntimeException e) {
//				logger.error(e);
//				validatable.error(new ValidationError(this));
//			}
//		}
//	}
	
	class ScriptValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String script = validatable.getValue();
			KbeeScriptRouter router = new KbeeScriptRouter();
			router.setScript(script);
			String message = router.validate(getContentTemplate());
			if (message!=null) {
				validatable.error(new ValidationError(message));
			}
		}
	}
	
	
	
	
	/** ------------------------
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	public TaskForkJoinEditor(String id, IModel<Task> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setEditionEnabled(false);
		
		//setTaskPage(((WebTask)getModelObject()).getPage());
		setTaskName(getModelObject().getName());

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("id", true, new IdValidator()) {
			@Override
			public boolean isEnabled() {
				
				if (!isEditable())
					return false;
				
				if (getProcedure().getVersion()<=1)
					return false;
				
				if (hasProcesses(getProcedure())) 
						return false;
				
				return true;
				
			}
		});
		
		form.add(new TextField<String>("name", true));
		
		form.add(new TextAreaField<String>("description"));
		
		form.add(new ChoiceField<ProcedurePhase>("phase", new PropertyModel<List<ProcedurePhase>>(this, "phases")) {
			@Override
			public boolean isEnabled() {
				return super.isEnabled() && (getProcedure().getVersion()>1 || isRoot());
			}
			@Override
			public boolean isVisible() {
				return !getProcedure().getPhases().isEmpty();
			}
			@Override
			public boolean isNullValid() {
				return true;
			}
		});
		
		form.add(new TextAreaField<String>("routerScript",  new ScriptValidator()) {
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
			}
		});
		
		form.add(new TextAreaField<String>("onStart",  new ScriptValidator()) {
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<Task>(this) {
			@Override
			public boolean isEnabled() {
				return role_admin || role_model;
			}
		});
		
		add(new InfoDialog("help-modal"));
	}
	
	protected boolean hasProcesses(Procedure procedure) {
		return false;
	}

	public Task getTask() {
		return getModel().getObject();
	}
	
	public Procedure getProcedure() {
		return ((KbeeTask)getTask()).getProcedure();
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeTask task = (KbeeTask)getTask();
				//if (getTaskPage()!=null && task instanceof WebTask) {
				//	((WebTask)task).setPage(getTaskPage().getName());
				//}
				KbeeProcedure procedure = (KbeeProcedure)(task.getProcedure());
				List<Task> tasks = procedure.getTasks();
				//if (task.isInitial()) {
				//	tasks.forEach(t -> ((KbeeTask)t).setInitial(false));
				//	task.setInitial(true);
				//}
				procedure.setTasks(tasks);
				getModel().setObject(task);
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
	
	public ContentTemplate getContentTemplate() {
		KbeeContentProcedure procedure = (KbeeContentProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getContentTemplate();
	}
	
	public List<ProcedurePhase> getPhases() {
		KbeeProcedure procedure = (KbeeProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getPhases();
	}
	
//	public List<RoleInProcess> getRoles() {
//		KbeeProcedure procedure = (KbeeProcedure)((KbeeTask)getTask()).getProcedure();
//		return procedure.getRoles();
//	}
	
	public List<Task> getTasks() {
		return ((KbeeProcedure)((KbeeTask)getTask()).getProcedure()).getTasks();
	}
	
	public Domain getDomain() {
		KbeeProcedure procedure = (KbeeProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getDomain();
	}
	
//	public List<TaskPageFactory> getFactories() {
//		List<TaskPageFactory> factories = new ArrayList<TaskPageFactory>();
//		Map<String, TaskPageFactory> factoriesmap = ServiceLocator.getService(BeansService.class).getBeansOfType(TaskPageFactory.class);
//		for (String bean : factoriesmap.keySet()) {
//			TaskPageFactory factory = (TaskPageFactory)ServiceLocator.getService(BeansService.class).getBean(bean);
//			factories.add(factory);
//		}
//		return factories;
//	}
//	
//	public void setTaskPage(TaskPageFactory factory) {
//		this.taskPageFactory = factory;
//	}
//	
//	public TaskPageFactory getTaskPage() {
//		return this.taskPageFactory;
//	}
//	
//	protected void setTaskPage(String factoryName) {
// 		if (factoryName!=null)
//		for (TaskPageFactory factory : getFactories()) {
//			if (factory.getName().equals(factoryName)) {
//				taskPageFactory = factory;
//				break;
//			}
//		}
//	}
	
	protected void onSelect(AjaxRequestTarget target, IModel<EndCondition> conditionmodel) {
		
	}
	
	protected void onDelete(AjaxRequestTarget target, EndCondition condition) {
		
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	private void setTaskName(String name) {
		this.taskName = name;
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
	
	private boolean isEditable() {
		return isRoot();
	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	private IModel<String> getScriptHelp() {
		return new Model<String>(JsEvaluator.GetHelpText(getContentTemplate()));
	}
}

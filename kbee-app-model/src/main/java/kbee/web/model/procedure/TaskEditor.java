package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.KbeeProcedure;
import com.novamens.content.workflow.Validator;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.JsEvaluator;
import com.novamens.kbee.content.workflow.KbeeScriptRouter;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.TaskPageFactory;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.SortableBehavior;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;

@SuppressWarnings("serial")
public class TaskEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskEditor.class.getName());

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private TaskPageFactory taskPageFactory;
	private String taskName; // nombre inicial para auditoria

	class IdValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String id = validatable.getValue();
			int founds = 0;
			for (Task task : getProcedure().getTasks()) {
				if (id!=null && id.toLowerCase().equals(task.getId().toLowerCase())) {
					if (task!=getTask()) {
						founds++;
					}
				}
			}
			if (getProcedure().getSubprocedures()!=null)
			for (Procedure procedure : getProcedure().getSubprocedures()) {
				for (Task task : procedure.getTasks()) {
					if (id!=null && id.toLowerCase().equals(task.getId().toLowerCase())) {
						if (task!=getTask()) {
						founds++;
						}
					}
				}
			}
			if (founds>0) {
				validatable.error(new ValidationError(this));
			}	
		}
	}
	
	class IqlValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String statement = validatable.getValue();
			try {
				if ((statement==null || "".equals(statement)))
					return;
				IqlService iqlservice = getDomain().getService(IqlService.class);
				ResultSet set = iqlservice.execute(statement);
				set.hasNext();
			} 
			catch (RuntimeException e) {
				logger.error(e);
				validatable.error(new ValidationError(this));
			}
		}
	}
	
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
	
	
	/** -------------------------------------
	 * 
	 */
	public class ConditionsTable extends Fragment {
		private boolean orderUpdated = false;
		List<String> orderedIds = null;
		public ConditionsTable(String id) {
			super(id, "conditions-table-fragment", TaskEditor.this);
			
			setOutputMarkupId(true); 
			
			WebMarkupContainer body = new WebMarkupContainer("body");
			
			body.add(new ListView<IModel<EndCondition>>("condition", () -> getEndConditions()) {
				public void populateItem(final ListItem<IModel<EndCondition>> item) {
					
					ManualEndCondition condition = (ManualEndCondition)item.getModelObject().getObject();
					AjaxLink<?> conditionlink = new AjaxLink<Void>("condition-link") {
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, item.getModelObject());
						}
					};
					
					// LABEL
					Label conditionlabel = new Label("condition-label", condition.getLabel());
					
					conditionlink.add(conditionlabel);
					item.add(conditionlink);
					
					// ENABLED
					Label enabledlabel = new Label("condition-enabled", condition.isEnabled() ? "<span class=\""+ new StringResourceModel("yes", TaskEditor.this, null).getObject()+"\">Yes</span>" : "<span class=\"no\">"+ new StringResourceModel("no", TaskEditor.this, null).getObject()+"</span>");
					enabledlabel.setEscapeModelStrings(false);
					item.add(enabledlabel);
	
					
					
					// ROUTER AND TRIGGER
					
					
					StringBuilder str = new StringBuilder();
					str.append( getRouterAndTrigger((ManualEndCondition)condition) );
			
					logger.debug(str.toString());

					Label routerlabel = new Label("condition-router",  new Model<String>(str.toString()));
					routerlabel.setEscapeModelStrings(false);
					item.add(routerlabel);
					
					
					// IQL
					String preconditionstatement = "";
					for (Validator validator : condition.getPrecondition()) {
						preconditionstatement += validator.toString();
					}
					Label preconditionlabel = new Label("condition-precondition", preconditionstatement);
					preconditionlabel.setEscapeModelStrings(false);
					item.add(preconditionlabel);


					// DELETE
					
					item.add( new AjaxLink<Void>("delete-link") {
						@Override
						public void onClick(AjaxRequestTarget target) {
							ConfirmationDialog dialog = (ConfirmationDialog)ConditionsTable.this.get("confirmation-dialog");
							EndCondition condition = item.getModelObject().getObject();
							dialog.open(target, getLabel("confirmation.DeleteCondition", condition.getLabel()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											onDelete(target, condition);
										} 
										catch (Exception e) {
											logger.error(e);
										}
									}
								}
							});
						}
						@Override
						public boolean isVisible() {
							return isRoot() && isEditionEnabled(); 
						}
					});
					
					item.add(new AttributeModifier("data-id", "value_"+item.getIndex()));
				}
			});
			
			add(new AjaxLink<Void>("addcondition-link") {
				public void onClick(AjaxRequestTarget target) {
					target.add(ConditionsTable.this);
					onCreateCondition(target);
				}
				public boolean isVisible() {
					return isEditionEnabled() && (getProcedure().getVersion()>1 || isRoot());
				}
				public boolean isEnabled() {
					return isEditionEnabled() && (getProcedure().getVersion()>1 || isRoot());
				}
			});
			
			body.add(new SortableBehavior() {
				@Override
				public void onSort(AjaxRequestTarget target, List<String> ids) {
					sort(ids);
				}
				@Override
				public String getItemSelector() {
					return "tr.condition";
				}
			});
			
			add(body);
			
			add(new ConfirmationDialog("confirmation-dialog"));
		}
		public boolean orderUpdated() {
			return orderUpdated;
		}
		public List<EndCondition> getEndConditios() {
			int i = 0;
			List<EndCondition> values = ((UserTask)getTask()).getEndConditions();
			if (orderedIds==null) return values;
			List<EndCondition> values2 = new ArrayList<EndCondition>();
			values2.addAll(values); 
			if (values.size()==orderedIds.size()) {
				i =0;
				for (String id : orderedIds) {
					int index = Integer.valueOf(id);
					values.set(i, values2.get(index));
					i++;
				}
			}
			return values;
		}
		private void sort(List<String> ids) {
			orderUpdated = true;
			orderedIds = ids;
			setUpdatedPart("conditions order");
		}
	}	
	
	
	
	/** ------------------------
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	public TaskEditor(String id, IModel<Task> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setEditionEnabled(false);
		
		setTaskPage(((WebTask)getModelObject()).getPage());
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
		
		form.add(new ConditionsTable("conditions"));
		
		form.add(new BooleanField("initial") {
			@Override
			public boolean isVisible() {
				return isEditable();
			}
			@Override
			public boolean isEnabled() {
				return !getTask().isInitial();
			}
		}); 
		
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
		
		form.add(new BooleanField("cancelEnabled"));
		
		form.add(new BooleanField("enableProgressNotes"));
		
		form.add(new BooleanField("enablePublicLink"));
		
		form.add(new BooleanField("enableLabels"));
		
		form.add(new BooleanField("enableEditingAllResources") {
			protected String getFalseStr() {
				return new StringResourceModel("just-this-task", TaskEditor.this, null).getString();
			}
			protected String getTrueStr() {
				return new StringResourceModel("all", TaskEditor.this, null).getString();
			}
		});
		
		form.add(new BooleanField("editableTitle"));

		form.add(new TriggerEditor<Task>("trigger", getTask().getTrigger(), getModel()) {
			public void updateModel() {
				((KbeeTask)getTask()).setTrigger(getTrigger());
			}
		});  

		form.add(new ChoiceField<RoleInProcess>("role", new PropertyModel<List<RoleInProcess>>(this, "roles")));
		
		form.add(new EnabledRolesEditor());
		
		form.add(new ChoiceField<ResolutionAction>("resolutionAction", new PropertyModel<List<ResolutionAction>>(this, "resolutionActions")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(form);
			}
		});
		
		form.add(new ChoiceField<DueDateAction>("duedateAction", new PropertyModel<List<DueDateAction>>(this, "dueDateActions")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(TaskEditor.this.get("form"));
			}
		});
		
		form.add(new TextAreaField<String>("duedateExpression",  new ScriptValidator()) {
			public boolean isEnabled() {
				return getAction()==DueDateAction.CALCULATE_ON_UPDATE ||
					getAction()==DueDateAction.CALCULATE_ON_START;
			}
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				IModel<String> textmodel = new Model<String>(JsEvaluator.GetHelpText(getContentTemplate())+getLabelString("dueDateJsExamples"));
				getHelpModal().open(target, () -> { return "Due Date Computation"; }, textmodel);
			}
			@SuppressWarnings("unchecked")
			public DueDateAction getAction() {
	 			return ((ChoiceField<DueDateAction>)TaskEditor.this.get("form:duedateAction")).getValue();
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
				if (getTaskPage()!=null && task instanceof WebTask) {
					((WebTask)task).setPage(getTaskPage().getName());
				}
				if (((ConditionsTable)get("form:conditions")).orderUpdated()) {
					task.setEndConditions(((ConditionsTable)get("form:conditions")).getEndConditios());
				}
				Procedure procedure = (Procedure)(task.getProcedure());
				List<Task> tasks = procedure.getTasks();
				if (task.isInitial()) {
					tasks.forEach(t -> ((KbeeTask)t).setInitial(false));
					task.setInitial(true);
				}
				procedure.setTasks(tasks);
				getModel().setObject(task);
				onUpdate(target);
				
				((KbeeProcedure)procedure).update();
				
				getDomain().getService(WorkflowDomainService.class).update(procedure.getMaster(), getUpdateDescription());
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
		ContentProcedure procedure = (ContentProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getContentTemplate();
	}
	
	public List<ProcedurePhase> getPhases() {
		return getProcedure().getPhases();
	}
	
	public List<ResolutionAction> getResolutionActions() {
		List<ResolutionAction> actions = new ArrayList<ResolutionAction>();
		actions.add(ResolutionAction.SETNULL);
		actions.add(ResolutionAction.TRANSFER);
		return actions;
	}
	
	public List<RoleInProcess> getRoles() {
		return getProcedure().getRoles();
	}
	
	public List<Task> getTasks() {
		return getProcedure().getTasks();
	}
	
	public Domain getDomain() {
		return ((DomainObject)getProcedure()).getDomain();
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
	
	public List<IModel<EndCondition>> getEndConditions() {
		List<IModel<EndCondition>> models = new ArrayList<IModel<EndCondition>>();
		for (EndCondition condition : ((KbeeTask)getTask()).getEndConditions()) {
			models.add(new EndConditionModel<EndCondition>(getModel(), condition));
		}
		return models;
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
	
	public List<DueDateAction> getDueDateActions() {
		List<DueDateAction> actions = new ArrayList<DueDateAction>();
		actions.add(DueDateAction.SETNULL);
		actions.add(DueDateAction.INHERIT);
		actions.add(DueDateAction.CALCULATE_ON_START);
		actions.add(DueDateAction.CALCULATE_ON_UPDATE);
		return actions;
	}
	
	protected void onSelect(AjaxRequestTarget target, IModel<EndCondition> conditionmodel) {
		
	}
	
	protected void onDelete(AjaxRequestTarget target, EndCondition condition) {
		
	}
	
	protected void onCreateCondition(AjaxRequestTarget target) {
		
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
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

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private String getRouterAndTrigger(EndCondition condition) {
		
		StringBuilder sconditions = new StringBuilder();
		
		String label = "<div><span>" + condition.getLabel() +"</span>";
						
		if (condition instanceof ManualEndCondition) {
			RouterType router = ((ManualEndCondition)condition).getRouter();
			if (router!=null) {
				if (router.equals(RouterType.TASK)) {
					Task nextTask = ((ManualEndCondition)condition).getNextTask();
					if (nextTask!=null)
						label += "<span class=\"separator\"> > </span>" + TaskEditor.this.getLabelString("task") + " [<span class=\"highlight\">"+nextTask.getDisplayName()+" </span>]";
					if (((ManualEndCondition)condition).getTrigger()!=null) 
						label += " - <span class=\"agso\"> "+ ((ManualEndCondition)condition).getTrigger().getType().getLabel(getSessionUser().getLocale()) + "</span>"; 
				}
				else if (router.equals(RouterType.PUBLISH)) {
					label += "<span class=\"separator\"> > </span><span class=\"adgo\"> " + TaskEditor.this.getLabelString("library") + "</span>";
				}
				else if (router.equals(RouterType.RETURN_TO_CALLER)) {
					label += "<span class=\"separator\"> > </span> <span class=\"highlight\">" +  TaskEditor.this.getLabelString("return-caller") +  "</span>";
				}
				else if (router.equals(RouterType.SCRIPT)) {
					label += "<span class=\"separator\"> > </span> <span class=\"highlight\"> " +  TaskEditor.this.getLabelString("js-script") + "  </span>";
				}
			}
			sconditions.append(label+"<br /></div>");
		}
		
		logger.debug(sconditions.toString());
						
		return  sconditions.toString();
	}
}

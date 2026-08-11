package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.PersonSet;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.KbeeProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.ScriptRule;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.DomainObject;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.iql.KbeeCaseExpression;
import com.novamens.kbee.content.util.ContentList;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.kbee.content.workflow.JsEvaluator;
import com.novamens.kbee.content.workflow.KbeeScriptRouter;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.DueDateExpressionType;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.RelationEditor;
import kbee.web.iql.KbeeIqlHelpService;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

/**
 * <p>Task Action editor</p>
 */

@SuppressWarnings("serial")
public class ManualConditionEditor extends ObjectEditor<ManualEndCondition> {
			
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ManualConditionEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private IModel<Task> taskmodel;
	private RouterType router;
	private IModel<PersonSet> collaborationSet;
	private String conditionLabel;
	private String conditionEvent;
	
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
				validatable.error(new ValidationError(e.getMessage()));
			}
		}
	}
	
	class DueDateExpressionValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			final String expressionstr = validatable.getValue();
			if ("".equals(expressionstr) || expressionstr==null) {
				return;
			}
			if (DueDateExpressionType.IQL.equals(getType())) {
				KbeeCaseExpression expression = new KbeeCaseExpression(getDomain(), expressionstr);
				if (!expression.isValid()) {
					validatable.error(new ValidationError(this));
				}
			}
		}
		@SuppressWarnings("unchecked")
		public DueDateExpressionType getType() {
			return ((ChoiceField<DueDateExpressionType>)ManualConditionEditor.this.get("form:duedateExpressionType")).getValue();
		}
	}
	
	class EventValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String event = validatable.getValue();
			Procedure procedure = ((KbeeTask)getTask()).getProcedure();
			int i = 0;
			for (Task task : procedure.getTasks()) {
				if (((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition condition : ((KbeeTask)task).getEndConditions()) {
					if (!condition.equals(getModelObject()) && event.equals(condition.getEvent())) {
						i++;
					}
				}
			}
			if (i>0) { 
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
	
	public ManualConditionEditor(IModel<Task> taskmodel, IModel<ManualEndCondition> model) {
		this("editor", taskmodel, model);
	}
	
	public ManualConditionEditor(String id, IModel<Task> taskmodel, IModel<ManualEndCondition> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setTask(taskmodel);
		setConditionLabel(model.getObject().getLabel());
		setConditionEvent(model.getObject().getEvent());
		setCollaborationSet(model.getObject().getCollaborationSet());
		
		Label main_title = new Label("main-title", new StringResourceModel("action-editor", this, null).setParameters(new Object [] {model.getObject().getLabel()}));
		add(main_title);
		
		setEditionEnabled(false);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("label", true));
		
		form.add(new TextField<String>("event", true, new EventValidator()) {
			public boolean isEnabled()	{
				return isEditionEnabled() && ((KbeeTask)getTask()).getProcedure().getVersion()>1 || isRoot();
			}
		});
		
		form.add(new TextAreaField<String>("description"));
		
		form.add(new BooleanField("enabled") {
			protected String getFalseStr() {
				return getLabelString("archived");
			}
			protected String getTrueStr() {
				return getLabelString("enabled");
			}
		});
		
		form.add(new BooleanField("collaboration") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
				if (!getValue()) {
					setCollaborationSet(null);
					((ChoiceField<?>)form.get("collaborationSet")).setValue(null);
					getModelObject().setCollaborationGroups(new ArrayList<Group>());
					((RelationEditor<?,?>)form.get("collaborationGroups")).cancel();
				}
				target.add(ManualConditionEditor.this.getForm());
			}
		});
		
		form.add(new CollaborationGroupsEditor() {
			@Override
			public boolean isEnabled() {
				return isEditionEnabled() && getModelObject().getCollaboration();
 		}
		});
		
		form.add(new ChoiceField<PersonSet>("collaborationSet", new PropertyModel<PersonSet>(this, "collaborationSet"), () -> getPersonSets()) {
			@Override
			public boolean isEnabled() {
				return isEditionEnabled() && getModelObject().getCollaboration();
			}
			@Override
			public boolean isNullValid() {
				return true;
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setCollaborationSet(getValue());
				setUpdatedPart("collaborationSet");
				target.add(ManualConditionEditor.this);
			}
		});
		
		form.add(new BooleanField("default"));
		form.add(new BooleanField("enablePriority"));
		//form.add(new BooleanField("requiredResources"));
		form.add(new BooleanField("tokenValidation"));
		form.add(new TextField<String>("perms"));

		form.add(new TextAreaField<String>("condition", new ScriptValidator()) {
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
			}
		});
		
		form.add(new WebMarkupContainer("routing") {
			@Override
			public boolean isVisible() {
				return isRoot();
			}
		});
		
		setRouter(getModelObject().getRouter());
		
		form.add(new ChoiceField<RouterType>("router", new PropertyModel<RouterType>(this, "router"), () -> getRouters()) {
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setRouter(getValue());
				setUpdatedPart("router");
				target.add(ManualConditionEditor.this);
			}
		});
		
		form.add(new Label("router.help", new Model<String>() {
			public String getObject() {
				try {
					return getRouter()!=null ? 
						(new StringResourceModel("router."+getRouter().getId()+".help", ManualConditionEditor.this)).getObject() :
						"";
				}
				catch (Exception e) {
					return "-";
				}
			}
		}));
		
		((Label)form.get("router.help")).setEscapeModelStrings(false);
		
		form.add(new ChoiceField<Task>("nextTask", new PropertyModel<List<Task>>(this, "tasks")) {
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			@Override
			public boolean isVisible() {
				return RouterType.TASK.equals(getRouter());
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				target.add(ManualConditionEditor.this);
			}
			protected IModel<Task> getModel(Task value) {
				IModel<Procedure> proceduremodel = new ObjectModel<Procedure>(((KbeeTask)value).getProcedure());
				return new TaskModel(proceduremodel, value);
			}	
		});
		
		form.add(new TextAreaField<String>("routerScript", new ScriptValidator()) {
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			@Override
			public boolean isVisible() {
				return RouterType.SCRIPT.equals(getRouter());
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
			}
		});
		
		IModel<Task> nexttaskmodel = new IModel<Task>() {
			public Task getObject() {
				return getModelObject().getNextTask();
			}
		};
		form.add(new TriggerEditor<ManualEndCondition>("trigger", getModelObject().getTrigger(), nexttaskmodel) {
			public void updateModel() {
				getModelObject().setTrigger(getTrigger());
			}
			public boolean isVisible() {
				return RouterType.TASK.equals(getRouter()) || RouterType.SCRIPT.equals(getRouter());
			}
		});  
		
		form.add(new PreconditionEditor<ManualEndCondition>("precondition"));
			
		form.add(new ClassifiersRulesEditor<ManualEndCondition>("templates") {
			@Override
			public List<ClassificationRule> getRules() {
				return ManualConditionEditor.this.getTemplatesRules();
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				ManualConditionEditor.this.setTemplatesRules(rules);
			}
		});
		
		form.add(new BooleanField("mandatoryLetter"));
		
		form.add(new ClassifiersRulesEditor<ProcessLauncher>("classifiersrules") {
			@Override
			public List<ClassificationRule> getRules() {
				return getRule().getRules(ClassificationRule.class);
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				ManualConditionEditor.this.setRules(rules);
			}
		});
		
		form.add(new AttributesRulesEditor<ProcessLauncher>("attributesrules") {
			@Override
			public List<AttributeRule> getRules() {
				return getRule().getRules(AttributeRule.class);
			}
			@Override
			public void setRules(List<AttributeRule> rules) {
				ManualConditionEditor.this.setRules(rules);
			}
		});
		
		form.add(new ScriptRuleEditor<ProcessLauncher>("scriptrule") {
			@Override
			public ScriptRule getRule() {
				return ManualConditionEditor.this.getRule().getRule(ScriptRule.class);
			}
			@Override
			public void setRule(ScriptRule rule) {
				ManualConditionEditor.this.setRules(Collections.singletonList(rule));
			}
			@Override
			public ContentTemplate getTemplate() {
				return getContentTemplate();
			}
		});
		
		//form.add(new ScriptRuleConditionEditor(taskmodel, getTemplateModel()));
		
		form.add(new NotificationEditor(taskmodel));
		form.add(new LetterEditor(taskmodel));
		
		form.add(new ChoiceField<DueDateAction>("duedateAction", new PropertyModel<List<DueDateAction>>(this, "dueDateActions")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(ManualConditionEditor.this.get("form"));
			}
		});
		
		form.add(new ChoiceField<DueDateExpressionType>("duedateExpressionType", new PropertyModel<List<DueDateExpressionType>>(this, "dueDateExpressionTypes")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(ManualConditionEditor.this.get("form"));
			}
		});
		
		form.add(new TextAreaField<String>("duedateExpression",  new DueDateExpressionValidator()) {
			public boolean isEnabled() {
				return getAction()==DueDateAction.CALCULATE;
			}
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				IModel<String> textmodel;
				if (DueDateExpressionType.JS.equals(getType())) {
					textmodel = new Model<String>(JsEvaluator.GetHelpText(getContentTemplate()));
				}
				else {
					textmodel = new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getDueDateHelp());
				}
				getHelpModal().open(target, () -> { return "Due Date Computation"; }, textmodel);
			}
			@SuppressWarnings("unchecked")
			public DueDateAction getAction() {
				return ((ChoiceField<DueDateAction>)ManualConditionEditor.this.get("form:duedateAction")).getValue();
			}
			@SuppressWarnings("unchecked")
			public DueDateExpressionType getType() {
				return ((ChoiceField<DueDateExpressionType>)ManualConditionEditor.this.get("form:duedateExpressionType")).getValue();
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>() {
					public String getObject() {
						return DueDateExpressionType.JS.equals(getType()) 
							? getLabelString("jSDuedateExpression.help") 
							: getLabelString(getProperty()+".help");
					}
				};
			}
		});
		
		
		form.add(new NumberField<Long>("autoRunAfter"));

				
		form.add(new ReasonsEditor<ManualEndCondition>());
	
		add(form);
		
		add(new EditButtonsV5<ManualEndCondition>(this) {
			@Override
			public boolean isEnabled() {
				return role_admin | role_model;
			}
		});
		
		add(new InfoDialog("help-modal"));
	}
	
	public Task getTask() {
		return taskmodel.getObject();
	}
	
	public void setTask(IModel<Task> model) {
		this.taskmodel = model;
	}
	
	public ContentTemplate getContentTemplate() {
		ContentProcedure procedure = (ContentProcedure)((KbeeTask)getTask()).getProcedure();
		return procedure.getContentTemplate();
	}
	
	public void setConditionLabel(String label) {
		this.conditionLabel = label;
	}
	
	public String getConditionLabel() {
		return conditionLabel; 
	}
	
	public void setConditionEvent(String event) {
		this.conditionEvent = event;
	}
	
	public String getConditionEvent() {
		return conditionEvent; 
	}
	
	public RouterType getRouter() {
		return router;
	}
	
	public void setRouter(RouterType value) {
		this.router = value;
	}
	
	public PersonSet getCollaborationSet() {
		return collaborationSet!=null ? collaborationSet.getObject() : null;
	}

	public void setCollaborationSet(PersonSet collaborationSet) {
		this.collaborationSet = collaborationSet!=null ? new ObjectModel<PersonSet>(collaborationSet) : null;
	}

	public void update(AjaxRequestTarget target) {
		if (!getUpdatedParts().isEmpty()) {
			getModelObject().setRouter(getRouter());
			getModelObject().setCollaborationSet(getCollaborationSet());
			Procedure procedure = getTask().getProcedure();
			
			((KbeeProcedure)procedure).update();
			//procedure.setTasks(procedure.getTasks());
			//procedure.getMaster().setSubprocedures(procedure.getMaster().getSubprocedures());
			
			onUpdate(target);
			((DomainObject)procedure).getDomain().getService(WorkflowDomainService.class).update(procedure.getMaster(), getUpdateDescription());
			reset();
		}
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public List<DueDateAction> getDueDateActions() {
		List<DueDateAction> actions = new ArrayList<DueDateAction>();
		actions.add(DueDateAction.SETNULL);
		actions.add(DueDateAction.INHERIT);
		actions.add(DueDateAction.CALCULATE);
		return actions;
	}
	
	public List<DueDateExpressionType> getDueDateExpressionTypes() {
		List<DueDateExpressionType> types = new ArrayList<DueDateExpressionType>();
		types.add(DueDateExpressionType.IQL);
		types.add(DueDateExpressionType.JS);
		return types;
	}
	
	public List<RouterType> getRouters() {
		List<RouterType> routers = new ArrayList<RouterType>();
		routers.add(RouterType.TASK);
		routers.add(RouterType.PUBLISH);
		routers.add(RouterType.CANCEL);
		routers.add(RouterType.RETURN_TO_CALLER);
		routers.add(RouterType.SCRIPT);
		routers.add(RouterType.THREAD_END);
		return routers;
	}
	
	public List<ResolutionAction> getResolutionActions() {
		List<ResolutionAction> actions = new ArrayList<ResolutionAction>();
		actions.add(ResolutionAction.SETNULL);
		actions.add(ResolutionAction.TRANSFER);
		return actions;
	}
	
	public List<PersonSet> getPersonSets() {
		List<PersonSet> datasets = new ArrayList<PersonSet>();
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof PersonSet) {
				datasets.add((PersonSet)dataset);
			}
		}
		return datasets;
	}
	
	public List<Task> getTasks() {
		return getTask().getProcedure().getTasks();
	}
	
	private MultipleRule getRule() {
		MultipleRule rule = (MultipleRule)getModelObject().getRule();
		if (rule == null) rule = new MultipleRule();
		return rule;
	}
	
	public <T extends WorkflowRule> void setRules(List<T> rules) {
		MultipleRule rule = getRule();
		rule.setRules(rules);
		getModelObject().setRule(rule);
	}
	
	
	private List<ClassificationRule> getTemplatesRules() {
		List<ClassificationRule> rules = new ArrayList<ClassificationRule>();
		List<Content> templates = getModelObject().getLetterTemplates();
		if (templates instanceof ContentList) {
			rules.addAll(((ContentList)templates).getCriteria());
		}
		return rules;
	}
	
	private void setTemplatesRules(List<ClassificationRule> templatesrules) {
		ContentList templates = new ContentList(templatesrules);
		getModelObject().setLetterTemplates(templates);
	}
	
	private String getUpdateDescription() {
		String text = getLabel("condition.auditlabel", getConditionLabel(), getTask().getName()).getObject();
		String parts = "";
		for (String part : getUpdatedParts()) {
			parts += "".equals(parts) ? " " : ", ";
			parts += part; 
		}
		text += parts;
		return text;
	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	private IModel<String> getScriptHelp() {
		return new Model<String>(KbeeScriptRouter.GetHelpText(getContentTemplate()));
	}
	
	private InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
}
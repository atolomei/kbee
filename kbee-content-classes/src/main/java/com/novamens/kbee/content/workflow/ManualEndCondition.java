package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.calendar.CalendarService;
import com.novamens.content.base.Content;
import com.novamens.content.model.PersonSet;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.content.workflow.Validator;
import com.novamens.kbee.calendar.KbeeCalendarService;
import com.novamens.kbee.content.iql.KbeeCaseExpression;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.DueDateExpressionType;
import com.novamens.workflow.Reason;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.WorkflowContext;

public class ManualEndCondition implements EndCondition, Serializable {
	
	private static final long serialVersionUID = 1L;
	private String label;
	private String event;
	private String perm;
	private String css;
	private String description;
	private boolean collaboration;
	private boolean batch = false;
	private boolean infrequent = false;
	private boolean optionalCollaboration = false;
	private boolean enabled = true;
	private boolean setPriority = false;
	//private boolean requiredResources = false;
	private boolean mandatoryLetter = false;
	private boolean tokenValidation = false; 
	private List<Group> collaborationGroups = new ArrayList<Group>();
	private PersonSet collaborationSet = null;
	private boolean isDefault = false;
	private WorkflowRule rule;
	private List<Validator> precondition = new ArrayList<Validator>();
	private List<Content> templates = new ArrayList<Content>();
	private ResolutionAction resolutionAction;
	
	private long autoRunAfter = 0;
	
	private DueDateAction duedateAction;
	private String duedateExpression;
	private DueDateExpressionType duedateExpressionType;
	
	private String nextTaskId;
	private RouterType router;
	private Trigger trigger;
	private transient Task nextTask;
	private String routerScript = null;
	private String condition = null;
	private List<Reason> reasons = new ArrayList<Reason>();
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( ManualEndCondition.class.getName());

	private static kbee.util.logging.Logger calendar_logger = kbee.util.logging.Logger.getLogger(KbeeCalendarService.class.getName());
	
	
	public interface DueDateCalculator {
		OffsetDateTime getDueDate(WorkflowContext context);
	}
	
	/**
	 * 
	 * 
	 *
	 */
	public class IqlDueDateCalculator implements DueDateCalculator {
		private String expression; 
		public IqlDueDateCalculator (String expression) {
			this.expression = expression;
		}
		public OffsetDateTime getDueDate(WorkflowContext context) {
			Content content = ((KbeeContext)context).getContent();
			KbeeCaseExpression caseexpression = new KbeeCaseExpression(content.getDomain(), expression);
			if (!caseexpression.isValid()) {
				logger.warn("invalid duedate expression " + caseexpression);
				return null;
			}
			String value = caseexpression.evaluate(content);
			try {
				calendar_logger.debug("Id ->" + content.getId().toString() + " Title -> " + content.getTitle());
				OffsetDateTime duedate = content.getDomain().getService(CalendarService.class).getDueDate(Integer.valueOf(value));
				return duedate;
			}
			catch (Exception e) {
				logger.error(e);
				if ( caseexpression!=null) 
					logger.error("invalid duedate expression " + caseexpression.toString());
				logger.warn("invalid duedate expressions null.");
			}
			return null;	
		}
	}

	
	/**
	 * 
	 * 
	 *
	 */
	public class JsDueDateCalculator implements DueDateCalculator {
		private String script;
		JsDueDateCalculator(String script) {
			setScript(script);
		}
		public OffsetDateTime getDueDate(WorkflowContext context) {
			Object evaluation = (new JsEvaluator(getScript())).evaluate(context);
			return evaluation instanceof OffsetDateTime ? (OffsetDateTime)evaluation : null;	
		}
		public void setScript(String script) {
			this.script = script;
		}
		public String getScript() {
			return this.script;
		}
	}
	
	
	/**
	 * 
	 * @param label
	 * @param event
	 */
	public ManualEndCondition(String label, String event) {
		setLabel(label);
		setEvent(event);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getCollaboration() {
		return collaboration;
	}
	
	public void setCollaboration(boolean value) {
		this.collaboration = value;
	}
	
	public void setCollaborationGroups(List<Group> groups) {
		this.collaborationGroups = groups;
	}

	public List<Group> getCollaborationGroups() {
		return collaborationGroups;
	}
	
	public PersonSet getCollaborationSet() {
		return collaborationSet;
	}

	public void setCollaborationSet(PersonSet collaborationSet) {
		this.collaborationSet = collaborationSet;
	}

	public boolean getBatch() {
		return batch;
	}
	
	public boolean isBatch() {
		return batch;
	}
	
	public void setBatch(boolean value) {
		this.batch = value;
	}
	
	public boolean isTokenValidation() {
		return tokenValidation;
	}

	public void setTokenValidation(boolean tokenValidation) {
		this.tokenValidation = tokenValidation;
	}

	public boolean isInfrequent() {
		return infrequent;
	}
	
	public void setInfrequent(boolean value) {
		this.infrequent = value;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public long getAutoRunAfter() {
		return autoRunAfter;
	}

	public void setAutoRunAfter(long autoRunAfter) {
		this.autoRunAfter = autoRunAfter;
	}

	public List<Validator> getPrecondition() {
		return precondition;
	}
	
	public void setPrecondition(List<Validator> precondition) {
		this.precondition = precondition;
	}
	
	public WorkflowRule getRule() {
		return rule;
	}
	
	public void setRule(WorkflowRule rule) {
		this.rule = rule;
	}
	
	public boolean getOtionalCollaboration() {
		return optionalCollaboration;
	}
	
	public void setOptionalCollaboration(boolean value) {
		this.optionalCollaboration = value;
	}
	
	public boolean isEnablePriority() {
		return setPriority;
	}
	
//	public boolean getRequiredResources() {
//		return requiredResources;
//	}
//	
//	public void setRequiredResources(boolean value) {
//		this.requiredResources = value;
//	}
	
	public void setPerms(String value) {
		this.perm = value;
	}
	
	public String getPerms() {
		return perm;
	}
	
	public void setCondition(String value) {
		this.condition = value;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setRouter(RouterType value) {
		if (!RouterType.TASK.equals(value)) setNextTask(null);
		this.router = value;
	}
	
	public void setRouterType(String name) {
		this.router = RouterType.valueOf(name);
	}
	
	public RouterType getRouter() {
		return router;
	}
	
	public void setRouterScript(String script) {
		this.routerScript = script;
	}
	
	public String getRouterScript() {
		return this.routerScript;
	}
	
	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean value) {
		enabled = value;
	}
	
	public void setEnablePriority(boolean value) {
		this.setPriority = value;
	}
	
	public void setLetterTemplates(List<Content> templates) {
		this.templates = templates;
	}
	
	public List<Content> getLetterTemplates() {
		return templates;
	}
	
	public boolean isMandatoryLetter() {
		return mandatoryLetter;
	}
	
	public void setMandatoryLetter(boolean value) {
		this.mandatoryLetter = value;
	}
	
	public void setCss(String css) {
		this.css = css;
	}
	
	public String getCss() {
		return css;
	}
	
	public DueDateAction getDuedateAction() {
		return duedateAction;
	}
	
	public void setDuedateAction(DueDateAction action) {
		this.duedateAction = action;
	}
	
	public String getDuedateExpression() {
		return duedateExpression;
	}
	
	public void setDuedateExpression(String expression) {
		this.duedateExpression = expression;
	}
	
	public DueDateExpressionType getDuedateExpressionType() {
		return duedateExpressionType==null ? DueDateExpressionType.IQL : duedateExpressionType;
	}

	public void setDuedateExpressionType(DueDateExpressionType duedateExpressionType) {
		this.duedateExpressionType = duedateExpressionType;
	}

	public OffsetDateTime getDueDate(WorkflowContext context) {
		if (getDuedateAction()==null || getDuedateAction()==DueDateAction.SETNULL)
			return null;
		if (getDuedateAction()==DueDateAction.INHERIT)
			return context.getDueDate();
		if (getDuedateExpression()==null)
			return null;
		DueDateCalculator calculator = getDueDateCalculator(getDuedateExpression());
		if (calculator!=null) {
			OffsetDateTime duedate = calculator.getDueDate(context);
			return duedate;
		}
		
		return null;
	}
	
	public ResolutionAction getResolutionAction() {
		return resolutionAction==null ? ResolutionAction.SETNULL : resolutionAction;
	}
	
	public void setResolutionAction(ResolutionAction action) {
		this.resolutionAction = action;
	}
	
	public boolean isTimeout(WorkflowContext context) {
		if (getAutoRunAfter()<=0) return false;
		
		boolean sametask = false;
		for (EndCondition action : ((KbeeTask)context.getTask()).getEndConditions()) {
			if (getEvent().equals(action.getEvent())) {
				sametask = true;
				break;
			}
		}
		if (!sametask) return false;
		
		Activity activity = context.getCurrentActivity();
		
		if (!Activity.Status.RUNNING.equals(activity.getStatus()))
			return false;
		
		
		OffsetDateTime time = activity.getStartTime();
		
		if (OffsetDateTime.now().isAfter(time.plusHours(getAutoRunAfter())))
			return true;	
		
		return false;
	}
	
	public boolean isEnabled(Content content) {
		if (!isEnabled())
			return false;
		
		boolean enabled = true;
		
		if (getPerms()!=null && !"".equals(getPerms())) { 
			String perm = getPerms();
			
			if ("!write".equals(perm)) {
				enabled = !isWriteable(content);
			}
			
			if ("write".equals(perm)) {
				enabled = isWriteable(content);
			}
		}
		
		if (!enabled)
			return false;
		
		
		if (getCondition()!=null && !"".equals(getCondition())) { 
			String condition = getCondition();
			try {
				WorkflowContext context = content.getService(WorkflowService.class).getContext();
				Object value = (new JsEvaluator(condition)).evaluate(context);
				return Boolean.TRUE.equals(value);
			} 
			catch (Exception e) {
				logger.error(e);
				return false;
			}
		}

		
//		if (getCondition()!=null && !"".equals(getCondition())) { 
//			String condition = getCondition();
//			try {
//				Expression expression = content.getDomain().getService(IqlService.class).getExpression(condition);
//				JavaIqlEvaluator evaluator = new JavaIqlEvaluator(expression);
//				enabled = evaluator.evaluate(content);
//			} 
//			catch (Exception e) {
//				logger.error(e);
//				return false;
//			}
//		}
		
		return enabled;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ManualEndCondition)) return false;
		return ((ManualEndCondition)object).getEvent().equals(getEvent()) && 
				((ManualEndCondition)object).getLabel().equals(getLabel());
	}
	
	public boolean isManual() {
		return true;
	}
	
	public List<Reason> getReasons() {
		return reasons;
	}
	
	public void setReasons(List<Reason> reasons) {
		this.reasons = reasons;
	}
	
	public void setNextTask(Task task) {
		nextTask = task;
		nextTaskId = task!=null ? task.getId() : null;
	}
	
	public Task getNextTask() {
		return nextTask;
	}
	
	public void setNextTaskId(String id) {
		nextTaskId = id;
	}
	
	public String getNextTaskId() {
		return nextTaskId;
	}
	
	protected DueDateCalculator getDueDateCalculator(String expression) {
		if (expression==null)
			return null;
		if (DueDateExpressionType.JS.equals(getDuedateExpressionType())) {
			return new JsDueDateCalculator(expression);
		}
		if (DueDateExpressionType.IQL.equals(getDuedateExpressionType())) {
			return new IqlDueDateCalculator(expression);
		}
		return null;
	}
	
	protected boolean isWriteable(Content content) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl(content);
		return acl.checkPermission(getUser(), KbeePermission.WRITE);
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
};
package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;

import com.novamens.calendar.CalendarService;
import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.service.DomainService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.dom.Domain;
import com.novamens.event.EventService;
import com.novamens.kbee.calendar.KbeeCalendarService;
import com.novamens.kbee.timer.KbeeTimer;
import com.novamens.logging.TaskEndEvent;
import com.novamens.logging.TaskPendingEvent;
import com.novamens.logging.TaskStartEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.timer.TimerService;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowEvent;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

/**
 * 
 * Procedure -> Procedure Definition
 * 
 * 
 * 
 */
@Entity
@Table(name = "WF_PROCESS")
public class KbeeProcess implements Process {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeProcess.class.getName());
	
	private static kbee.util.logging.Logger calendar_logger = kbee.util.logging.Logger.getLogger(KbeeCalendarService.class.getName());
	
	/**
    The </b>TxLogger</b> is set up in Log4J to log synchronoulsy with the transaction Thread.
     This is different from all the other logs that work asynchronously.
	 */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	@Id
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "PROCEDURE")
	private String procedureName;
	
	private transient Procedure procedure;
	private transient WorkflowContext context;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeProcedure.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "procedure_id", updatable=false)
	private Procedure procedure2;
	
	// Aggregation
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeWorkflowActivity.class)
	@JoinColumn(name = "process_id", nullable=false)
	@OrderBy(clause= "STARTIME desc")
	List<Activity> activities = new ArrayList<Activity>();
	
	@Column(name = "STARTIME")
	private OffsetDateTime startTime;
	
	@Column(name = "ENDTIME")
	private OffsetDateTime endTime;
	
	@Column(name = "STATUS")
	private String statusValue;
	

	public KbeeProcess() {
	}

	public KbeeProcess(Procedure procedure, WorkflowContext context) {
		setProcedure(procedure);
		setContext(context);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Process start() {
		setStatus(Status.RUNNING);
		getProcedure().initiate(getContext());
		startTime = OffsetDateTime.now();
		return this;
	}

	public void cancel() {
		for (Activity activity : getActivities()) {
			if (activity.isRunning()) {
				activity.cancel();
			}
		}
		setStatus(Status.CANCELED);
		((KbeeProcedure)getProcedure()).cancel(getContext());
		endTime = OffsetDateTime.now();
	}

	public void end() {
		setStatus(Status.TERMINATED);
		endTime = OffsetDateTime.now();
	}

	public boolean isRunning() {
		return getStatus().equals(Status.RUNNING);
	}
		
	public Activity start(Task task, WorkflowContext context, User user) {
		return start(task, context, user, false);
	}
	
	public Activity start(Task task, WorkflowContext context, User user_to_assign_task, boolean restart) {
		Activity activity = task.start(context, user_to_assign_task);
		
		((KbeeContext)context).setTask(task);
		((KbeeContext)context).setUser(user_to_assign_task);
		((KbeeContext)context).setTime(activity.getStartTime());
		//((KbeeContext)context).getParameters().clear();
		((KbeeContext)context).setResolution(null);
		((KbeeContext)context).setNote(null);

		
		if (task.getRole()!=null) {
			((KbeeContext)context).setRole(task.getRole(), user_to_assign_task);
		}
		
		if (task.getPhase()!=null) {
			((KbeeContext)context).setCurrentPhase(task.getPhase());
		}
		
		if (timeOut(activity.getTask())) {
			((KbeeWorkflowActivity)activity).setContext(context);
			setTimeOut(activity);
		}
		
		String onStart = ((KbeeTask)activity.getTask()).getOnStart();
		if (onStart!=null && !"".equals(onStart.trim())) {
			try {
				(new JsEvaluator(onStart)).evaluate(context);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
				
		User reassigned_by_user =  null;
		String inputnote = null;
		
		if (!((KbeeContext)context).getProcess().getActivities().isEmpty()) {
			if (((KbeeContext)context).getProcess().getActivities().get(0).getAssignedBy()!=null) {
				reassigned_by_user  = ((KbeeContext)context).getProcess().getActivities().get(0).getAssignedBy();
			}
			inputnote = ((KbeeContext)context).getProcess().getActivities().get(0).getNote();
		}
		
		txlogger.info(new TaskStartEvent(((KbeeContext)context).getContent(), activity, inputnote, reassigned_by_user, restart));
		
		this.activities.add(0, activity);
		
		ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(((KbeeContext)context).getContent(), activity));

		return activity;
	}

	/** 
	 *  We must use txlogger event 
	 *  The Notification Service puts a ServiceRequest in the Scheduler, and it must be inside a thread 
	 *  that has a Hibernate Session to commit 
	 * 
	 *  We get sure the log is saved only when the transaction commits succesfully
	 * 
	 */

	public Process end(Activity activity, WorkflowEvent event, WorkflowContext context) {
		
		((KbeeWorkflowActivity)activity).setEvent(event.getId());
		((KbeeWorkflowActivity)activity).setNote(context.getNote());
		((KbeeWorkflowActivity)activity).setResolution(context.getResolution());
		((KbeeWorkflowActivity)activity).setResolutionTitle(context.getResolutionTitle());
		
		if (activity.getTask() instanceof UserTask) {
			for (EForm eform : ((UserTask)activity.getTask()).getForms()) {
				String capture = ((KbeeContext)context).getFormCapture(eform);
				EFormData data = ((KbeeContext)context).getContent().getFormData(eform);
				((KbeeWorkflowActivity)activity).setFormData(capture, data);
			}
		}
		
		((KbeeContext)context).setTime(null);

		activity.end();
		
		txlogger.info(new TaskEndEvent(((KbeeContext)context).getContent(), activity, event.getLabel(), event.getForced()));
		
		getProcedure().handle(event.getId(), context);
						
		((KbeeContext)context).setDueDate(getDueDate(activity, event, context));
		
		setContext(context);

		// Despues de Ejecutar el trigger del workflow, se fija si el content paso a Pending.
		// y generar una entrada en el log 
		Domain domain = ((KbeeContext)context).getContent().getDomain();
		if (domain!=null) {
			try {
				Long wid = (Long) domain.getService(DomainService.class).getWorkflowUser().getId();
				if(((KbeeContext)context).getContent().getWorkspace()!=null && ((KbeeContext)context).getContent().getWorkspace().equals(wid)) {
					txlogger.info(new TaskPendingEvent(((KbeeContext)context).getContent(), activity,	event.getLabel()));
				}
			} 
			catch (Exception e) {
				logger.error(e);
				throw e;
			}
		}
		
		setContext(context);
		
		return this;
	}

	public WorkflowContext getContext() {
		return context;
	}

	public void setContext(WorkflowContext context) {
		((KbeeContext)context).setProcess(this);
		this.context = context;
	}

	public Procedure getProcedure() {
		return procedure==null ? procedure2 : procedure;
	}
	
	public void setProcedure(Procedure procedure) {
		this.procedureName = procedure.getId()!=null ? String.valueOf(procedure.getId()) : procedure.getName();
		this.procedure2 = procedure;
	}
	
	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}
	
	public OffsetDateTime getStartTime() {
		return startTime;
	}
	
	public OffsetDateTime getEndTime() {
		return endTime;
	} 
	
	public List<Activity> getActivities() {
		return activities;
	}
	
	public Status getStatus() {
		return Status.valueOf(statusValue);
	}
	
	public void setStatus(Status status) {
		this.statusValue = status.toString();
	}
	
	public String getProcedureName() {
		return procedureName;
	}
	
	public OffsetDateTime getDueDate(Activity activity, WorkflowEvent event, WorkflowContext context) {
		Task task = activity.getTask();
		if (!(task instanceof UserTask))
			return null;
		for (EndCondition condition : ((UserTask)task).getEndConditions()) {
			if (condition.getEvent().equals(event.getId())) {
				return condition.getDueDate(context);
			}
		}
		return null;
	}
	
	protected boolean timeOut(Task task) {
		if (!(task instanceof UserTask)) return false;
		for (EndCondition condition : ((UserTask)task).getEndConditions()) {
			if (condition instanceof TimeOutEndCondition) {
				return true;
			}
		}
		return false;
	}
	
	protected void setTimeOut(Activity activity) {
		for (EndCondition condition : ((UserTask)activity.getTask()).getEndConditions()) {
			if (condition instanceof TimeOutEndCondition) {
				Content content = ((KbeeContext)activity.getContext()).getContent();
				int duration = ((TimeOutEndCondition)condition).getDuration();
				
				calendar_logger.debug("Id ->" + content.getId().toString() + " Title -> " + content.getTitle());
				OffsetDateTime duedate = content.getDomain().getService(CalendarService.class).getDueDate(duration);
				
				KbeeActivityTimerCallBack callBack = new KbeeActivityTimerCallBack(activity);
				ServiceLocator.getService(TimerService.class).setTimer(new KbeeTimer(duedate, callBack));
			}
		}
	}
}

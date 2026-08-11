package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.LabelsService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.EventService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.lock.MemLockService;
import com.novamens.logging.AssignationEvent;
import com.novamens.logging.DueDateAlertEvent;
import com.novamens.logging.ProgressNoteEvent;
import com.novamens.logging.TaskReassignedFormerOwnerEvent;
import com.novamens.logging.UpdateEvent;
import com.novamens.security.Principal;
import com.novamens.security.TokenSubmission;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowEvent;
import com.novamens.workflow.WorkflowException;
import com.novamens.workflow.WorkflowThread;
import com.novamens.workflow.WorkflowThreadStatus;

import kbee.email.EmailBuilderTaskReassignedFormerOwner;

import com.novamens.workflow.Factory;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public class KbeeWorkflowService implements WorkflowService {
	private Content content = null;
	private WorkflowContext context = null;
	private Process process = null;
	private Factory factory = null;
	private WorkflowDao workflowDao;
	private ContentDao dao = null; 
	
	public static String PROPERTY_NOTE = "note";
	public static String PROPERTY_SENDER = "sender";
	public static String PROPERTY_PROCESS = "process";
	public static String PROPERTY_ASSIGNATION_TIME = "assignation-time";
	public static String PROPERTY_ACTIVITY_RESOURCES = "activity-resources";
	public static String PROPERTY_DUEDATE_ALERTS = "duedate-alerts";
	
	private Map<Thread, WorkflowTransactionSynchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, WorkflowTransactionSynchronization>());

	static final String PROPERTY_UNREAD = "unread";
	
	// Logger sincronico en la TRX
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeWorkflowService.class.getName());
	
	public KbeeWorkflowService() {
	}
	
	public KbeeWorkflowService(Content content) {
		 this.content = content;
	}
	
	public String getTaskCommentSessionUser() {
		return "";
	}
	
	@Override
	public String getTaskComment() {
		Activity activity = getPreviousActivity();
		if (activity==null)
			return null;
		String note = activity.getNote();
		if (note!=null) {
			note = note.replace("\r\n\r\n\r\n", "<br /><br />");
			note = note.replace("\r\n", "<br />");
			note = note.replace("\n", "<br />");
		}
		return note;
	}

	
	@Transactional
	public Process startProcess(ProcessLauncher launcher) {
		return startProcess(launcher, false);
	}

	@Transactional
	public Process startProcess(ProcessLauncher launcher, boolean isApi) {
		return startProcess(launcher, null, false);
	}
	
	@Transactional
	public Process startProcess(ProcessLauncher launcher, String note, boolean isapi) {
		Content content = getContent();
		if (content.isHeadVersion()) {
			content = content.getService(ContentService.class).checkout();
		}
		KbeeContext context = new KbeeContext(getFactory());
		context.setApi(isapi);
		context.setContent(content);
		context.setNote(note);
		process = update(launcher.startProcess(context));
		ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(content, getActivity()));
		getContent().getService(PropertyService.class).setProperty(PROPERTY_PROCESS, String.valueOf(context.getProcess().getId()));
		return process;
	}
	
	public Process startProcess(ProcessLauncher launcher, Object initialData, String note, User collbaorator) {
		Content content = getContent();
		if (content.isHeadVersion()) {
			content = content.getService(ContentService.class).checkout();
		}
		KbeeContext context = new KbeeContext(getFactory());
		context.setApi(true);
		context.setContent(content);
		context.setNote(note);
		context.setCollaborator(collbaorator);
		//update(context);
		process = update(launcher.startProcess(context, initialData));
		ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(content, getActivity()));
		getContent().getService(PropertyService.class).setProperty(PROPERTY_PROCESS, String.valueOf(context.getProcess().getId()));
		context = (KbeeContext)process.getContext();
		context.setNote(note);
		update(context);
		return process;
	}

	
	@Transactional
	public Process startProcess(Procedure procedure) {
		return startProcess(procedure, false);
	}
	
	@Transactional
	public Process startProcess(Procedure procedure, boolean isapi) {
		return startProcess(procedure, null, false);
	}
	
	@Transactional
	public Process startProcess(Procedure procedure, String note, boolean isapi) {
		Content content = getContent();
		if (content.isHeadVersion()) {
			content = content.getService(ContentService.class).checkout();
		}
		KbeeContext context = new KbeeContext(getFactory());
		context.setApi(isapi);
		context.setContent(content);
		context.setNote(note);
		process = update(procedure.start(context));
		ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(content, getActivity()));
		getContent().getService(PropertyService.class).setProperty(PROPERTY_PROCESS, String.valueOf(context.getProcess().getId()));
		return process;
	}
	
	@Transactional
	public void update() {
		update(getProcess().getContext());
	}
	
	@Transactional
	public void setPriority(Priority priority) {
		KbeeContext context = (KbeeContext)getContext();
		context.setPriority( priority);
		update(context);
	}
	
	public void handle(WorkflowEvent event) {
		((KbeeWorkflowEvent)event).setObject(getContent());
		((KbeeWorkflowEvent)event).setActivity(getActivity());
		ServiceLocator.getService(EventService.class).fire(event);
		Process process = getProcess();
		if (process==null) 
			throw new RuntimeException("no active process for content");
		((KbeeContext)getContext()).setProcess(process);
		process.end(getActivity(), event, getContext());
		update(process);
	}
	
	@Transactional()
	public void handle(WorkflowEvent event, WorkflowContext context) {
		addTransactionSynchronization();
		((KbeeWorkflowEvent)event).setObject(getContent());
		((KbeeWorkflowEvent)event).setActivity(getActivity());
		ServiceLocator.getService(EventService.class).fire(event);
		((KbeeContext)context).setProcess(getProcess());
		getProcess().end(getActivity(), event, context);
		update(getProcess());
	}
	
	@Transactional
	public void startTask() throws WorkflowException {
		addTransactionSynchronization();
		Content content = getContent();
		try {
			content.getService(MemLockService.class).lock();
			Process process = getProcess();
			KbeeContext context = (KbeeContext)process.getContext();
			if (taskStarted(context)) {
				throw new WorkflowException();
			}
			else {
				Task task = getTask();
				User sessionuser = getUser();
				User sender = context.getUser();
				process.start(task, context, sessionuser);
				assign(sender, sessionuser, context.getNote(), null);
				update(process);
				if (DueDateAction.CALCULATE_ON_START.equals(((KbeeTask)task).getDuedateAction()) ||
					DueDateAction.CALCULATE_ON_UPDATE.equals(((KbeeTask)task).getDuedateAction())) {
					updateDueDate();
				}
			}
		}
		finally {
			content.getService(MemLockService.class).unlock();
		}
	}
	
	public Activity getActivity() {
		return getProcess()!=null && !getProcess().getActivities().isEmpty() ? getProcess().getActivities().get(0) : null;
	}  
	
	public Process getLastProcess() {
		Process process = null;
		try {
			String processId = (String)getContent().getService(PropertyService.class).getProperty(PROPERTY_PROCESS);
			if (processId!=null) {
				process = getWorkflowDao().findProcessById(Long.valueOf(processId));
			}
		}
		catch (Exception e) {
			LogManager.getLogger(KbeeWorkflowService.class.getName()).error(e);
		}
		return process;
	}
	
	public Task getTask() {
		return ((KbeeContext)getContext()).getTask();
	}
	
	public Task reloadTask() {
		WorkflowContext context = getWorkflowDao().reload(getContext());
		return context.getTask();
	}
	
	public Process getProcess() {
		if (this.process==null) 
			this.process = getWorkflowDao().getActiveProcess(getContent());
		return this.process;
	}
	
	@Transactional
	public void cancel() {
		Process process = getProcess();
		if (process!=null) {
			for (Activity activity : process.getActivities()) {
				if (activity.isRunning()) {
					ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(getContent(), activity));
				}
			}
			process.cancel();
		}
		else {
			getContent().getService(ContentService.class).dropCheckout();
		}
	}
	
	@Transactional
	public void restartPrevious() {
		KbeeContext kbcontext = (KbeeContext)getContext();
		if (kbcontext.getPreviousTerminatedActivity()==null)
			return;
		if (!isPending()) 
			return;
		Activity previous = kbcontext.getPreviousTerminatedActivity();
		Task task = previous.getTask();
		kbcontext.setTask(task);
		User user = previous.getUser();
		getProcess().start(task, kbcontext, user, true);
		assign(user, user, null, null);
		update(getProcess());
	}
	
	public boolean isPending() {
		return getWorkflowUser().getId().equals(getContent().getWorkspace());
	}
	
	public boolean isBatchEnabled() {
		Task task = getTask();
		if (!(task instanceof UserTask))
			return false;
		for (EndCondition condition : ((UserTask)task).getEndConditions()) {
			if (condition instanceof ManualEndCondition && ((ManualEndCondition)condition).isBatch()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean updatedResources() {
		PropertyService properties = getContent().getService(PropertyService.class);
		String resources = (String)properties.getProperty(PROPERTY_ACTIVITY_RESOURCES);
		return resources!=null && !"".equals(resources);
	}
	
	@Transactional
	public void assign(User user, String note)  {
		assign(user, note, null);
	}
	
	@Transactional
	public void assign(User user, String note, String resolution)  {
		assign(getOwner(), user, note, null);
	}
	
	
	/**
	 * {@link TaskReassignedFormerOwnerEvent} 

	 * is processed by the 
	 * 
	 * {@link NotificationService}
	 * 
	 * who schedules a
	 *  
	 * {@link TaskReassignFormerOnwerTaskServiceRequest}
	 * 
	 * who sends a
	 *   
	 * {@link EmailBuilderTaskReassignedFormerOwner}
	 * 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void reassign(User user_to_reassign, String note)  {
		
		User ressigned_by = getUser();
		User current_owner = getOwner();
		
		KbeeContext context = (KbeeContext)getContext();
		Process process = context.getProcess();
		
		if (context.getUser().equals(current_owner))
			context.setUser(user_to_reassign);
		
		for (Activity activity : process.getActivities()) {

			if (activity.isRunning()) {
				
				Assert.isTrue(activity.getUser().equals(current_owner), "not owner");
					
				((KbeeWorkflowActivity)activity).setAssignedBy(ressigned_by); //  
				((KbeeWorkflowActivity)activity).setStatus(Activity.Status.REASSIGNED); 
				((KbeeWorkflowActivity)activity).setEndTime(OffsetDateTime.now()); 
				((KbeeWorkflowActivity)activity).setNote(note);
				
				ServiceLocator.getService(EventService.class).fire(new KbeeWorkflowEvent(getContent(), activity));
		
				logger.debug("ReAssign former user event | content -> " + ((KbeeContext)context).getContent().getDisplayName() + " | " + "user -> " + activity.getUser().getDisplayName() );
				

				// activity is the one terminated by the reassign
				txlogger.info(new TaskReassignedFormerOwnerEvent(((KbeeContext)context).getContent(), activity, note, ressigned_by));
				
				Task task = activity.getTask();
				process.start(task, context, user_to_reassign);
				break;
			}
		}
		
		assign(ressigned_by, user_to_reassign, note, null);
		txlogger.info(new AssignationEvent(getContent(), user_to_reassign, note));
		
		update(process);
	}

	// EmailBuilderTaskReassignedFormerOwner builder = new EmailBuilderTaskReassignedFormerOwner( context, getContent(),  current_owner, ressigned_by);
	// ServiceLocator.getService(EmailService.class).send(builder);
	// schedule(new TaskPendingNotificationTaskServiceRequest(event));

	
	/**
		The process has to be running
		There must be no running activities 	 * 
	 */
	@Transactional
	public void setAsPending(Task task, WorkflowContext context)  {
		
		User sender = getUser();
		User user = getWorkflowUser();
		
		//Process process = ((ProcessProxy)context.getProcess()).getProcess();
		
		Process process = context.getProcess();
		
		if (process instanceof ProcessProxy) {
			process = ((ProcessProxy)process).getProcess();
		}	
		
		((KbeeContext)context).setUser(user);
		
		((KbeeContext)context).setTask(task);
		
		assign(sender, user, null, null);
		txlogger.info(new AssignationEvent(getContent(), user, null));
		
		((KbeeProcess)process).setContext(context);
		
		update(process);
	}
	
	@Transactional
	public void setParameter(String name, String value) {
		KbeeContext context = (KbeeContext)getContext();
		Map<String, String> parameters = context.getParameters();
		parameters.put(name, value);
		context.setParameters(parameters);
		update(context);
	}
	
	@Transactional
	public void setParameters(Map<String, String> parameters) {
		KbeeContext context = (KbeeContext)getContext();
		context.setParameters(parameters);
		update(context);
	}
	
	@Transactional
	public void setResolution(String response, String title) {
		KbeeContext context = (KbeeContext)getContext();
		context.setResolution(response);
		context.setResolutionTitle(title);
		update(context);
	}
	
	@Transactional
	public void setNote(String note) {
		KbeeContext context = (KbeeContext)getContext();
		context.setNote(note);
		update(context);
	}
	
	@Transactional
	public void setDueDate(OffsetDateTime duedate) {
		KbeeContext context = (KbeeContext)getContext();
		context.setDueDate(duedate);
		update(context);
		Activity activity = context.getCurrentActivity();
		if (activity!=null) {
			((KbeeWorkflowActivity)activity).setDueDate(duedate);
		}
	}
	
	
	@Transactional
	public void updateDueDate() {
		try {
			KbeeContext context = (KbeeContext)getContext();
			
			Object evaluation = (new JsEvaluator(((KbeeTask)getTask()).getDuedateExpression())).evaluate(context);
			OffsetDateTime duedate = evaluation instanceof OffsetDateTime ? (OffsetDateTime)evaluation : null;
		
			Activity activity = context.getCurrentActivity();
			
			if (duedate!=null && !duedate.equals(activity.getDueDate())) {
				((KbeeWorkflowActivity)activity).setDueDate(duedate);
				context.setDueDate( duedate);
				update(context);
				PropertyService properties = getContent().getService(PropertyService.class);
				properties.removeProperty(PROPERTY_DUEDATE_ALERTS);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public boolean hasDueDateAlert() {
		return getDueDateAlert()>0;
	}
	
	// en dias al vencimiento
	public int getDueDateAlert() {
		KbeeContext context = (KbeeContext)getContext();
		Activity activity = context.getCurrentActivity();
		
		if (activity==null || activity.getDueDate()==null)
			return 0;
		
		
		List<Integer> dueDateAlerts = getDueDateAlerts();
		
		if (dueDateAlerts==null || dueDateAlerts.isEmpty())
			return 0;
		
		OffsetDateTime duedate = activity.getDueDate();
		OffsetDateTime today = OffsetDateTime.now();
		
		if (duedate.isBefore(today))
			return 0;
		
	    long daysToDueDate = ChronoUnit.DAYS.between(today, duedate);
		
		int alertPending = 0;
		for (Integer days : dueDateAlerts) {
			if (daysToDueDate<=days) {
				alertPending = days;
			}
		}
		
		if (alertPending==0)
			return 0;
		
		PropertyService properties = getContent().getService(PropertyService.class);
		String firedAlerts = (String)properties.getProperty(PROPERTY_DUEDATE_ALERTS);
		
		if (firedAlerts!=null && firedAlerts.contains(String.valueOf(alertPending)))
			return 0;
		
		return alertPending;
	}
	
	@Transactional
	public boolean fireDueDateAlert() {
		int daysToDueDate = getDueDateAlert();
		
		if (daysToDueDate<=0)
			return false;
		
		Content content = getContent();
		
		txlogger.info(new DueDateAlertEvent(content, getActivity(), daysToDueDate));
		
		PropertyService properties = getContent().getService(PropertyService.class);
		String firedAlerts = (String)properties.getProperty(PROPERTY_DUEDATE_ALERTS);
		firedAlerts = firedAlerts==null ? "" : firedAlerts + ";"; 
		firedAlerts += 	daysToDueDate;
		properties.setProperty(PROPERTY_DUEDATE_ALERTS, firedAlerts);
		
		return true;
	}


	/**
	 * Errors are propagated as RuntimeException
	 * 
	 * @param sender
	 * @param user
	 * @param note
	 * @param resolution
	 */
	public void assign(User sender, User user_to_assign, String note, String resolution)  {
		
		getContent().setWorkspace((Long) user_to_assign.getId());
		
		try {
			getContentDao().save(getContent());
			getContent().getService(LabelsService.class).setLabelForAssign();
			PropertyService properties = getContent().getService(PropertyService.class);
			if (note!=null) 
				properties.setProperty(PROPERTY_NOTE, note);
			
			properties.setProperty(PROPERTY_SENDER, sender.getFirstLastName());
			properties.setProperty(PROPERTY_ASSIGNATION_TIME, Long.valueOf((OffsetDateTime.now().toInstant().toEpochMilli())));
			properties.removeProperty(PROPERTY_ACTIVITY_RESOURCES);
			properties.setProperty(PROPERTY_UNREAD, "yes");
		
		} 
		catch (ContentMgmtException e) {
			logger.error(e);
			throw ( new KbeeRuntimeException(e));
		}
	}
	
	@Transactional
	public ActivityProgressNote createProgressNote() {
		KbeeActivityProgressNote note = new KbeeActivityProgressNote();
		note.setCreationOffsetDateTime(OffsetDateTime.now());
		note.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		note.setDomain(getContent().getDomain());
		note.setState(ObjectState.DRAFT);
		note.setLastModifiedUser(getUser());
		((KbeeWorkflowActivity)getActivity()).addProgressNote(note);
		return note;
	}
	
	@Transactional
	public void deleteProgressNote(ActivityProgressNote note) {
		((KbeeWorkflowActivity)getActivity()).deleteProgressNote(note);
	}
	
	@Transactional
	public void publish(ActivityProgressNote note) {
		((KbeeActivityProgressNote)note).setLastModifiedUser(getUser());
		((KbeeActivityProgressNote)note).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeActivityProgressNote)note).setState(ObjectState.ENABLED);
		txlogger.info(new ProgressNoteEvent(getContent(), note));
	}
	
	public TokenSubmission sendToken(WorkflowContext context, Person person) {
		Content content = getContent();
		
        TokenSubmission submission = person.getService(PersonService.class).sendToken(content);

        Map<String, String> parameters = ((KbeeContext)context).getParameters();
        parameters.put("delivery-validation", "token");
        parameters.put("delivery-token", submission.getTokenValue());
        parameters.put("delivery-email", submission.getEmail());
        parameters.put("delivery-phone", submission.getPhone());
        parameters.put("delivery-error", String.valueOf(submission.hasError()));
        parameters.put("delivery-feedback", submission.getFeedback());
        
        content.getService(WorkflowService.class).setParameters(parameters);
        
		return submission;
	}
	
	public TokenSubmission resendToken() {
		try {
			KbeeContext context = ((KbeeContext)getContext());
			String personId = context.getParameter("delivery-person");
			if (personId!=null) {
				DataSetMember personmember = getContentDao().findMemberById(Long.valueOf(personId));
				if (personmember!=null && personmember instanceof PersonMember) {
					TokenSubmission submission = sendToken((PersonMember)personmember);
					return submission;
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	
	@Transactional
	public TokenSubmission sendToken(Person person) {
		return sendToken((KbeeContext)getContext(), person);
	} 
	
	public User getOwner() {
		User user = null;
		Long workspace = ((KbeeContent)getContent()).getWorkspace();
		if (workspace!=null) {
			user = ServiceLocator.getService(SecurityService.class).findUserById(workspace);
		}
		return user;
	}
	
	public Procedure getProcedure() {
		return ((KbeeContext)getContext()).getProcedure();
	}
	
	public WorkflowContext getContext() {
		if (context==null) 
			context = getProcess()==null ? getNewContext() : getProcess().getContext();
		return context;
	}
	
	public WorkflowContext getNewContext() {
		KbeeContext context = new KbeeContext(getFactory());
		context.setContent(getContent());
		return context;
	}
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return getProcedure().getRoles(context);
	}
	
	public boolean active() {
		return getProcess()!=null; 
	}
	
	public Content getContent() {
		return content;
	}
	
	public List<WorkflowThreadStatus> getThreads() {
		return ((KbeeContext)getContext()).getThreads();
	}
	
	public WorkflowDao getWorkflowDao() {
		return workflowDao;
	}
	
	public void setWorkflowDao(WorkflowDao dao) {
		workflowDao = dao;
	}
	
	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	public Factory getFactory() {
		return this.factory;
	}
	
	@Transactional
	public void fix() {
		boolean fixed = false;
		if (getActivity()!=null && 
			getActivity().getUser()!=null &&
			getContent().getWorkspace()>0 &&
			!getActivity().getUser().getId().equals(getContent().getWorkspace())) {
			fixed = true;
			getContent().setWorkspace((Long)(getActivity().getUser().getId()));
		}
		if (getActivity()!=null &&
				getActivity().isRunning() &&
				!getContext().getTask().equals(getActivity().getTask())) {
			((KbeeContext)getContext()).setTask(getActivity().getTask());
			fixed = true;
			update();
		}
		if (fixed) {
			txlogger.info(new UpdateEvent(getContent(), "fix workflow error"));
		}
	}
	
	public boolean isBroken() {
		if (getActivity()!=null && 
			getActivity().isRunning() &&
			getActivity().getUser()!=null &&
			getContent().getWorkspace()>0 &&
			!getActivity().getUser().getId().equals(getContent().getWorkspace())) {
			return true;
		}
		if (getActivity()!=null &&
			getActivity().isRunning() &&
			!getContext().getTask().equals(getActivity().getTask())) {
			return true;
		}
		return false;
	}
	
	private boolean taskStarted(WorkflowContext context) {
		return !getContent().getWorkspace().equals(getWorkflowUserId());
	}
	
	private long getWorkflowUserId() {
		User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+getDomain().getName());
		Assert.isTrue(user!=null, "no workflow user!");
		return (long)user.getId();
	}
	
	private Process update(Process process) {
		getWorkflowDao().update(process);
		return process;
	}
	
	private void update(WorkflowContext context) {
		getWorkflowDao().update(context);
	}
	
	private List<Integer> getDueDateAlerts() {
		
		KbeeTask task = (KbeeTask)getTask();
		
		if (task.getDueDateAlerts()==null)
			return null;
		
		String values[] = task.getDueDateAlerts().split(";");
		
		if (values.length==0)
			return null;
		
		List<Integer> alerts = new ArrayList<>();
		for (int v=0; v<values.length; v++) {
			try {
				alerts.add(Integer.valueOf(values[v].trim()));
			}
			catch (Exception e) {
				logger.error(e);
				return null;
			}
		}
		
		Collections.sort(alerts, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return b.compareTo(a);
			}
		}); 
		
		return alerts;
	}
	
	private User getUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private User getWorkflowUser() {
		User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+getDomain().getName());
		Assert.isTrue(user!=null, "no user");
		return user;
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private ContentDao getContentDao() {
		if (dao==null)	 {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			dao = (ContentDao) beans.getBean("contentDao");
		}
		return dao;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private Activity getPreviousActivity() {
		if (getContext()==null)
			return null;
		return ((KbeeContext) getContext()).getPreviousActivity();
	}
	
	private void addTransactionSynchronization() {
		if (transactions.get(Thread.currentThread()) == null) {
			transactions.put(Thread.currentThread(), new WorkflowTransactionSynchronization((Long)getContent().getId()) {
				public void afterCompletion(int status) {
					try {
						if (status == STATUS_ROLLED_BACK) {
							getWorkflowDao().refresh(getId());
						}
					}
					finally {
						transactions.remove(Thread.currentThread());
					}
				}
			});
		}
	}
}
	
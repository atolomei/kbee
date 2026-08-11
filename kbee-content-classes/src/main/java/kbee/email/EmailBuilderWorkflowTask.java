package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

/**
 * 
 * For 
 * . Task Assigned
 * . Task Pending
 * . Task Past Due Date
 *
 */
public abstract class EmailBuilderWorkflowTask extends EmailBuilderBase {

	private static Logger logger = Logger.getLogger(EmailBuilderBase.class.getName());
	private static Logger emaillogger = Logger.getLogger("email");

	private Content content;
	private WorkflowContext context;
	
	public EmailBuilderWorkflowTask() {
	}
	
	public EmailBuilderWorkflowTask(WorkflowContext context, Content content) {
		this.content=content;
		this.context=context;
	}
	
	public EmailBuilderWorkflowTask(Map<String, Object> parameters) {
		super(parameters);
	}
	
	public WorkflowContext getContext() {
		return context;
	}

	public void setContext(WorkflowContext context) {
		this.context = context;
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
	public Domain getDomain() {
		return content.getDomain();
	}
	
	public WorkflowContext getWorkflowContext() {
		return context;
	}
	
	public Content getContent() {
		return content;
	}
	
	protected boolean isEmailTaskNotifications(Person receiver) {
		if (!receiver.getProfile(UserProfile.class).isEmailNotifications()) {
			logger.debug("User -> " + receiver.getFirstLastName() + " has disabled Email Workflow Notifications.");
			emaillogger.debug("User -> " + receiver.getFirstLastName() + " has disabled Email Rule/Workflow Notifications.");
			return false;
		}
		return true;
	}
	
	public String getArea() {
		return com.novamens.email.EmailBuilder.WORKFLOW;
	}
	
	public Map<String, Object> getBuilderObjects() {
		return new HashMap<String, Object> ();
	}
}
package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.workflow.WorkflowContext;


/**
 * 
 * <p>Task Past Due Date</p>
 * 
 * @see KbeeActivityTimerCallBack
 *
 */
public class EmailBuilderWorkflowTaskPastDueDate extends EmailBuilderWorkflowTask implements EmailBuilder {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	//private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private Person notification_receiver;
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	

	
	public EmailBuilderWorkflowTaskPastDueDate(Map<String, Object> parameters) {
		super(parameters);
		setParameters(parameters);
		setMacroAreas(areas);
	}
	
	public EmailBuilderWorkflowTaskPastDueDate(WorkflowContext context, Content content, Person notification_receiver) {
		super(context, content);
		
		setMacroAreas(areas);
		
		this.notification_receiver=notification_receiver;
		
		if( this.notification_receiver !=null)
			setLanguage(this.notification_receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		
	}

	
	public EmailBuilderWorkflowTaskPastDueDate() {
		setMacroAreas(areas);
	}

	
	@Override
	public String getKey() {
			return EmailTemplate.TASK_TIMEOUT;
	}

	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			
			if (map.containsKey("content"))
				setContent((getContentDao().findContentById( Long.valueOf((String) map.get("content")))));
			
			if (getContent()!=null) 
				setContext(getContent().getService(WorkflowService.class).getContext());
			
			this.notification_receiver= map.containsKey("notificationreceiver") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("notificationreceiver")))) :null;
			
			if( this.notification_receiver !=null)
				setLanguage(this.notification_receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	@Override
	public EmailData build() {
		
		if (notification_receiver==null)
			throw new IllegalArgumentException("notification_receiver is null");

		if (getContent()==null)
			throw new IllegalArgumentException("content is null");
		
		if (getWorkflowContext()==null)
			throw new IllegalArgumentException("context is null");

		
		EmailTemplate template = getEmailTemplate(this.notification_receiver.getDomain(), 
				getLanguage(), 
				getKey());

		String key = "task-timeout-"+ getWorkflowContext().getTask().getId();
		Map<String, Object> map = new HashMap<String, Object>();

		addGeneralMacros(this.notification_receiver.getDomain(), map);
		addContentMacros(getContent(), map);
		addWorkflowMacros(getWorkflowContext(), map);
		addAppContextMacros(null, this.notification_receiver, key, map);
		
		return parse(template, this.notification_receiver.getEmail(), map, key);

	}

	
	
	@Override
	public boolean isSendEnabled()  {
		
		if (!isEnabled(notification_receiver))
			return false;
		
		if (!isEmailRuleNotifications(notification_receiver))
			return false;		
		
		return true;
	}
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("notification_receiver",notification_receiver);
		r.put("content", getContent());
		return r;
	}
}

package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.security.User;
import com.novamens.workflow.WorkflowContext;



/**
 * Send To
 * 
 */
public class EmailBuilderWorkflowPostTerminationAlert extends EmailBuilderBase implements EmailBuilder {
				
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderWorkflowPostTerminationAlert.class.getName());
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	
	WorkflowContext context;
	Content content;
	Person task_executer;
	Person receiver;
	String text;
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };

	
	public EmailBuilderWorkflowPostTerminationAlert() {
		setMacroAreas(areas);	
	}
	
	public EmailBuilderWorkflowPostTerminationAlert(WorkflowContext context, Content content, Person task_executer, Person receiver, String text) {
		
		setMacroAreas(areas);
		this.context=context;
		this.content=content;
		this.task_executer=task_executer;
		this.receiver=receiver;
		this.text=text;
		if (this.task_executer!=null)
			setSender(this.task_executer.getProfile(UserProfile.class).getUser());
		
		if (receiver!=null)
			setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

		
	}
	
	
	public EmailBuilderWorkflowPostTerminationAlert(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}

	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.content = map.containsKey("content") ? (getContentDao().findContentById( Long.valueOf((String) map.get("content")))) :null;
			this.receiver= map.containsKey("receiver") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("receiver")))) :null;
			this.task_executer= map.containsKey("taskexecuter") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("taskexecuter")))) :null;
			this.text=map.containsKey("text") ? ((String) map.get("text")) : null;
			if (content!=null) 
				context = content.getService(WorkflowService.class).getContext();
			
			
			
			if (receiver!=null)
				setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	@Override
	public EmailData build() {
	
		if (receiver==null)
			throw new IllegalArgumentException("receiver is null");
			
		if (content==null)
			throw new IllegalArgumentException("content is null");

		
		EmailTemplate template = getEmailTemplate(receiver.getDomain(),  getLanguage(), getKey());
		String key = ((content!=null) ?  ("workflow-alert-contentid-" + content.getId().toString()) : "workflow-alert-testing");
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(receiver.getDomain(), map);
		addContentMacros(content, map);
		addWorkflowMacros(context, map);
		addAppContextMacros(task_executer, receiver, key, map);
		map.put("${text}", text!=null?text:"");
		
		EmailData data =  parse(template, receiver.getEmail(), map, key);
		return data;
	}

	
	@Override
	public String getKey() {
			return "workflow-notification";
	}
	
	@Override
	public String getArea() {
		return WORKFLOW;
	}
	
	public boolean isSendEnabled() {
		
		if (!isEnabled(receiver))
			return false;
		
		if (!this.isEmailRuleNotifications(receiver))
			return false;
		
		return true;
	}


	@Override
	public Domain getDomain() {
		return content.getDomain();
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("content", content);
		r.put("receiver",  receiver);
		r.put("task_executer", task_executer);
		r.put("text",  text);
		return r;
	}

}

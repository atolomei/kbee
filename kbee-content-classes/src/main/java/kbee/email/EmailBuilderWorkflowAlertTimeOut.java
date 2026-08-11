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
import com.novamens.workflow.WorkflowContext;

/**
 * Task executing time out
 * Task Pending time out
 *
 */
public class EmailBuilderWorkflowAlertTimeOut extends EmailBuilderBase implements EmailBuilder {
			
	 
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderWorkflowAlertTimeOut.class.getName());
	
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	WorkflowContext context;
	Content content;
	Person receiver;
	String alertkey; // Task executing time out / Task Pending time out
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	
	public EmailBuilderWorkflowAlertTimeOut() {
		setMacroAreas(areas);
		
	}
	
	public EmailBuilderWorkflowAlertTimeOut(WorkflowContext context, Content content, Person receiver, String alertkey) {
		setMacroAreas(areas);
		this.context=context;
		this.content=content;
		this.receiver=receiver;
		this.alertkey=alertkey;
		setSender(this.receiver.getProfile(UserProfile.class).getUser());
		
		if (receiver!=null)
			setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	public EmailBuilderWorkflowAlertTimeOut(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}

	
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.content = (map.containsKey("content") ? (getContentDao().findContentById( Long.valueOf((String) map.get("content")))) :null);
			this.receiver= (map.containsKey("receiver") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("receiver")))) :null);
			
			this.alertkey= (map.containsKey("alertkey") ? (String) map.get("alertkey") :null);
			if (content!=null) 
				context = content.getService(WorkflowService.class).getContext();
			
			if (receiver!=null)
				setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

			
			
		} catch (Exception e) {
			logger.error(e);
		}
	}


	
			
	@Override
	public String getKey() {
			return "workflow-notification-timeout";
	}
	
	@Override
	public EmailData build() {

		if (this.context==null)
			throw new IllegalArgumentException("context is null");

		if (this.content==null)
			throw new IllegalArgumentException("content is null");

		if (this.receiver==null)
			throw new IllegalArgumentException("receiver is null");

		if (this.alertkey==null)
			throw new IllegalArgumentException("alertkey is null");
		
		
		
		EmailTemplate template = getEmailTemplate(receiver.getDomain(), getLanguage() , getKey());
		String key = (content!=null) ? (alertkey+"-contentid-" + content.getId().toString()) : (alertkey+"-testing");
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(receiver.getDomain(), map);
		addContentMacros(content, map);
		addWorkflowMacros(context, map);
		addAppContextMacros(null, receiver, key, map);
		EmailData data= parse(template, receiver.getEmail(), map, key );
		
		return data;
	}

	@Override
	public Domain getDomain() {
		return content.getDomain();
	}

	public boolean isSendEnabled() {
		
		if (!isEnabled(receiver))
			return false;
		
		if (!this.isEmailRuleNotifications(receiver))
			return false;
		
		return true;
	}
	
	@Override
	public String getArea() {
		return WORKFLOW;
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("content",  content);
		r.put("receiver",  receiver);
		return r;
	}
	

}


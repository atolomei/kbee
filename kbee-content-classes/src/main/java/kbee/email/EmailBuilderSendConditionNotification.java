package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;

public class EmailBuilderSendConditionNotification extends EmailBuilderBase implements EmailBuilder {
		

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	//private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	
	ENotiRule rule;
	Content content;
	Person publisher;
	Person subscriber;
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	
	
	public EmailBuilderSendConditionNotification () {
		setMacroAreas(areas);
	}
	
	public EmailBuilderSendConditionNotification (ENotiRule rule, Content content, Person publisher, Person subscriber) {
		setMacroAreas(areas);
		this.rule = rule;
		this.content = content;
		this.publisher = publisher;
		this.subscriber = subscriber;
		
		if(this.subscriber!=null)
			setLanguage(subscriber.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	 public EmailBuilderSendConditionNotification(Map<String, Object> parameters) {
			super();
			setParameters(parameters);
			setMacroAreas(areas);
	}

	 
	 
	 
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.rule = map.containsKey("rule") ? (getENotiRuleDao().findENotiRuleById(Long.valueOf((String) map.get("rule")))) :null;
			this.content = map.containsKey("content") ? (getContentDao().findContentById( Long.valueOf((String) map.get("content")))) :null;
			this.subscriber= map.containsKey("subscriber") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("subscriber")))) :null;
			this.publisher= map.containsKey("publisher") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("publisher")))) :null;
			
			if(this.subscriber!=null)
				setLanguage(subscriber.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
	
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	

	@Override
	public String getKey() {
		return "notification-by-action-rule";
		
		//if (rule!=null)
		//	return  ActionRule.EMAIL_TEMPLATE_KEY+(rule.getKey()!=null ? ("-"+rule.getKey().trim()):"");
		//return  ActionRule.EMAIL_TEMPLATE_KEY;
	}

	@Override
	public EmailData build() {
	
		if (this.rule==null)
			throw new IllegalArgumentException("ruleis null");
		
		if (this.content==null)
			throw new IllegalArgumentException("content is null");
		
		if (this.publisher==null)
			throw new IllegalArgumentException("publisher is null");

		if (this.subscriber==null)
			throw new IllegalArgumentException("subscriber is null");


		EmailTemplate tem=getEmailTemplate(rule.getDomain(),  getLanguage() , getKey());
		 
		String key="rule-" + rule.getId().toString();
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(rule.getDomain(), map);
		addContentMacros(content, map);
		addAppContextMacros(publisher, subscriber, key, map);
		addRuleMacros(rule, subscriber, map);
		return parse(tem, subscriber.getEmail(), map, key);
		
		
	}

	@Override
	public Domain getDomain() {
		return rule.getDomain();
	}
	
	
	@Override
	public boolean isSendEnabled()  {
		
		if (!isEnabled(subscriber))
			return false;

		if (this.isEmailRuleNotifications(subscriber))
			return false;
		
		return true;
	}
	
	@Override
	public String getArea() {
		return RULE;
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("rule", rule);
		r.put("content",  content);
		r.put("publisher",  publisher);
		r.put("subscriber",  subscriber);
		return r;
	}


}

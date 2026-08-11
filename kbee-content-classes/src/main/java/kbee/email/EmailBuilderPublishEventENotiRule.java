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
import com.novamens.security.User;

public class EmailBuilderPublishEventENotiRule extends EmailBuilderBase implements EmailBuilder {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderPublishEventENotiRule.class.getName());
	
	private ENotiRule rule;
	private Content content;
	private Person publisher;
	private Person subscriber;
	

	public EmailBuilderPublishEventENotiRule() {
	}
	
	public EmailBuilderPublishEventENotiRule(ENotiRule rule, Content content, Person publisher, Person subscriber) {
		this.rule = rule;
		this.content = content;
		this.publisher = publisher;
		this.subscriber = subscriber;
		setSender(this.publisher.getProfile(UserProfile.class).getUser());
		setLanguage(subscriber.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	public EmailBuilderPublishEventENotiRule(Content content, Person publisher, Person subscriber) {
		this.content = content;
		this.publisher = publisher;
		this.subscriber = subscriber;
		setSender(this.publisher.getProfile(UserProfile.class).getUser());
		setLanguage(subscriber.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}

	public EmailBuilderPublishEventENotiRule(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
	}
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.content = (map.containsKey("content") ? (getContentDao().findContentById( Long.valueOf((String) map.get("content")))) :null);
			this.subscriber= (map.containsKey("subscriber") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("subscriber")))) :null);
			this.publisher= (map.containsKey("publisher") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("publisher")))) :null);
			this.rule = (map.containsKey("rule") ? (getENotiRuleDao().findENotiRuleById(Long.valueOf((String) map.get("rule")))) :null);
			if (this.subscriber!=null)
				setLanguage(subscriber.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public User getSender() {
		return publisher.getProfile(UserProfile.class).getUser();
	}
	
	@Override
	public boolean isSendEnabled() {
		return isEnabled(this.subscriber);
	}
	
	@Override
	public EmailData build() {

		if (this.content==null)
			throw new IllegalArgumentException("content is null");

		if (this.publisher==null)
			throw new IllegalArgumentException("publisher is null");

		if (this.subscriber==null)
			throw new IllegalArgumentException("subscriber is null");
		
		EmailTemplate template = getEmailTemplate(getDomain(), getLanguage(), getKey());
		
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(content.getDomain(), map);
		addContentMacros(content, map);
		addAppContextMacros(publisher, subscriber, getKey(), map);
		
		if (rule!=null)
			addRuleMacros(rule, subscriber, map);
		
		EmailData data = parse(template, subscriber.getEmail(), map, getKey());

		return data;
	}

	@Override
	public Domain getDomain() {
		if (rule!=null)
			return rule.getDomain();
		if (content!=null)
			return content.getDomain();
		return null;
	}
	
	@Override
	public String getArea() {
		return RULE;
	}

	@Override
	public String getKey() {
		String suffix = null;
		if (rule==null)
			return "alert-rule-publish-user";
		if (rule.isSystem())
			suffix = "domain";	
		else 
			suffix = "user";
		return "alert-rule-publish-"+suffix;
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

package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;


public class EmailBuilderSendTestEmailRuleNotification extends EmailBuilderBase implements EmailBuilder {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");
	
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	
	ENotiRule rule;
	String rulekey;
	Person receiver;
	

	
	public EmailBuilderSendTestEmailRuleNotification() {
		setMacroAreas(areas);
	}
			
	public EmailBuilderSendTestEmailRuleNotification(ENotiRule rule, String key, Person receiver) {
		setMacroAreas(areas);
		this.rule=rule;
		this.rulekey=key;
		this.receiver=receiver;
		
		if (receiver!=null)
			setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

	}
	
	public EmailBuilderSendTestEmailRuleNotification(Map<String, Object> parameters) {
		
		super();
		setParameters(parameters);
		setMacroAreas(areas);

	}

	@Override
	public String getKey() {
		if (rulekey!=null)
				return rulekey; //"alert-rule-publish";
		return "alert-rule-publish";
	}
	
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			
			this.rulekey = map.containsKey("rulekey") ?   ((String) map.get("rulekey")) : null;
			this.receiver= map.containsKey("receiver") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("receiver")))) :null;
			this.rule = map.containsKey("rule") ? (getENotiRuleDao().findENotiRuleById(Long.valueOf((String) map.get("rule")))) :null;
			
			if (receiver!=null)
				setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public EmailData build() {
		
		if (this.rule==null)
			throw new IllegalArgumentException("rule is null");

		if (this.receiver==null)
			throw new IllegalArgumentException("receiver is null");

		if (this.rulekey==null)
			throw new IllegalArgumentException("rulekey is null");

		
		EmailTemplate tem=getEmailTemplate(rule.getDomain(),  getLanguage(), getKey());
		String key="rule-" + rule.getId().toString();
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(rule.getDomain(), map);
		addContentMacros( null, map);
		addAppContextMacros(receiver, receiver, key, map);
		addRuleMacros(rule, receiver, map);
		return parse(tem, receiver.getEmail(), map, key);

	}
	

	public boolean isSendEnabled() {
		if (!isEnabled(receiver))
			return false;
		return true;
	}

	@Override
	public Domain getDomain() {
		return rule.getDomain();
	}

	@Override
	public String getArea() {
		return RULE;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("rule", rule);
		r.put("receiver",  receiver);
		return r;
	}
	

}

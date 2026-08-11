package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.service.ServiceLocator;

public class EmailBuilderNewDomain extends EmailBuilderBase implements EmailBuilder {
			
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderNewDomain.class.getName());
	
 
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private Domain newdomain;
	private String service_monitor_email;
	private String service_monitor_name;
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	
	
	
	public EmailBuilderNewDomain () {
		setMacroAreas(areas);
	}
			
	/**
	 * 
	 * @param newdomain
	 * @param x_values
	 * @param service_monitor_email
	 * @param service_monitor_name
	 */
	public EmailBuilderNewDomain (Domain newdomain, String service_monitor_email, String service_monitor_name) {

		setMacroAreas(areas);
		this.newdomain = newdomain;
		setLanguage(this.newdomain.getLocale().getLanguage());
		
		//this.x_values = x_values;
		this.service_monitor_email=service_monitor_email;
		this.service_monitor_name=service_monitor_name;
	}

	
	public EmailBuilderNewDomain(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			
			this.newdomain= map.containsKey("newdomain") ? (getContentDao().findDomainByName((String) map.get("newdomain"))) :null;

			if (newdomain==null)
				this.newdomain= map.containsKey("domain") ? (getContentDao().findDomainByName((String) map.get("domain"))) :null;
			
			
			if(this.newdomain!=null)
				setLanguage(this.newdomain.getLocale().getLanguage());
			
			this.service_monitor_email=map.containsKey("service_monitor_email") ? ((String) map.get("service_monitor_email")) : null;
			this.service_monitor_name=map.containsKey("service_monitor_name") ? ((String) map.get("service_monitor_name")) : null;
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	@Override
	public EmailData build() {
		
		if (this.newdomain==null)
			throw new IllegalArgumentException("newdomain is null");
		
		String language = getLanguage();
		
		EmailTemplate template = getEmailTemplate(getDomainKbee(), language, "newdomain");
		
		if (template==null) {  
			logger.error("new Domain template is missing");
			emaillogger.error("new Domain template is missing");
			template = ServiceLocator.getService(EmailService.class).getDefaultTemplates("en").get("newdomain");
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		StringBuilder str = new StringBuilder();
		
	 
		
		addGeneralMacros(newdomain, map);
		
		map.put("${domain-info}", 			str.toString());
		map.put("${domain-url}", 			getServerUrl(newdomain));		
		map.put("${person-displayname}",  	service_monitor_name);
		map.put("${domain-name}",  			newdomain.getOrganization());
		map.put("${server-url}", 			getServerUrl(newdomain));;
		map.put("${user-created}", 			getSessionUser().getUserName());
		map.put("${domain-type}", 			newdomain.getDomainType().getLabel());
		map.put("${domain-organization}", 	newdomain.getOrganization());
		map.put("${domain-website}", 		newdomain.getWebsite());
		map.put("${domain-name}", 			newdomain.getOrganization()!=null?newdomain.getOrganization():newdomain.getName());
		
		return parse(template, service_monitor_email, map,"new-domain-"+newdomain.getName());

	}

	@Override
	public Domain getDomain() {
		return newdomain;
	}

	
	
	@Override
	public String getArea() {
		return DOMAIN;
	}

	

	@Override
	public String getKey() {
		return "new-domain"; //EmailTemplate.NEW_DOMAIN;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("newdomain", newdomain);
		r.put("service_monitor_email",  service_monitor_email);
		r.put("service_monitor_name",  service_monitor_name);
		return r;
	}

}

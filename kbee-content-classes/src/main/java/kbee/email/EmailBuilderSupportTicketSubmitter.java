package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.service.ServiceLocator;

import kbee.content.support.SupportTicket;

public class EmailBuilderSupportTicketSubmitter extends EmailBuilderBase implements EmailBuilder {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSupportTicketSubmitter.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");
	
	
	private Person submitter;
	private SupportTicket ticket;
	private String subject;
	private String text;
	
	
	private String areas [] = { GENERAL, CONTEXT, SUPPORT };
	
	
	
	public EmailBuilderSupportTicketSubmitter() {
		setMacroAreas(areas);
	}
	
	public EmailBuilderSupportTicketSubmitter(SupportTicket ticket) {
		
		try {
				this.ticket=ticket;
				this.submitter = getContentDao().findUserProfileByUser(ticket.getUser()).getPerson();
			
		} catch (Exception e) {
			logger.error(e);
		}
		setMacroAreas(areas);
	}

	
	public EmailBuilderSupportTicketSubmitter(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}


	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			
			if (map.containsKey("support-ticket") )  {
				this.ticket = getContentDao().findSupportTicket( Long.valueOf((String) map.get("support-ticket")));
			}
			
			
			
		} catch (Exception e) {
			logger.error(e);
		}
		
		try {
			if (map.containsKey("support-ticket-submitter") )  {
				this.submitter = getContentDao().findPersonById(Long.valueOf((String) map.get("support-ticket-submitter")));
			}
			else {
				if (this.ticket!=null)
					this.submitter = getContentDao().findUserProfileByUser(ticket.getUser()).getPerson();
			}
				
		} catch (Exception e) {
			logger.error(e);
		}
		
		
		
		try {
			if (map.containsKey("support-ticket-subject") )  {
				this.subject = (String) map.get("support-ticket-subject");
			}
			else {
				this.subject = null;
			}
				
		} catch (Exception e) {
			logger.error(e);
		}

		
		try {
			if (map.containsKey("support-ticket-text") )  {
				this.text = (String) map.get("support-ticket-text");
			}
			else {
				this.text = null;
			}
				
		} catch (Exception e) {
			logger.error(e);
		}

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		if (submitter!=null)
			setLanguage(submitter.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

	}

	
	@Override
	public String getArea() {
		return DOMAIN;
	}

	@Override
	public String getKey() {
		return "support-ticket-submitter";
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("support-ticket-submitter",  (this.submitter!=null?this.submitter.getId().toString():"null"));
		r.put("support-ticket",  (this.ticket!=null?this.ticket.getId().toString():"null"));
		
		r.put("support-ticket-subject", ( subject!=null?subject: "null"));
		r.put("support-ticket-text", ( text!=null?text: "null"));
		
		return r;
	}
	

	@Override
	public EmailData build() {
		
		if (this.ticket==null)
			throw new IllegalArgumentException("ticket is null");
		
		
		if (this.submitter==null)
			throw new IllegalArgumentException("submitter is null");
		
		
		
		
		String language = getLanguage();
		
		EmailTemplate template = getEmailTemplate(ticket.getDomain(), language, getKey());
		
		if (template==null) {  
			logger.error("ticket template is missing");
			emaillogger.error("icket template is missing");
			template = ServiceLocator.getService(EmailService.class).getDefaultTemplates("en").get(getKey());
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// can replace these fields when it comes from testing
		//
		if (subject!=null)
			ticket.setSubject(subject);
		
		if (text!=null)
			ticket.setText(text);

		addAppContextMacros(this.submitter, this.submitter, getKey(), map);
		addSupportMacros(this.ticket,  map);
		
		return parse(template,this.submitter.getEmail(), map, getKey()+"-"+ticket.getName());

	}

	@Override
	public Domain getDomain() {
		return ticket.getDomain();
	}

}

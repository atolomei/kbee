package kbee.email;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;

public class EmailBuilderSendSubscriptionReport extends EmailBuilderBase implements EmailBuilder {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };
	
	private Person person;
	private String to;
	private String reportScheduleName;
	private String reportScheduleDescription;
	private String displayname;
	private Long audit_kbfile_id;
	private String[] attachment;
	
	
	
	public EmailBuilderSendSubscriptionReport() {
		setMacroAreas(areas);
		
	}
	
	public EmailBuilderSendSubscriptionReport(Person person, String to, String reportScheduleName, String reportScheduleDescription, String[] attachment, String displayname, Long audit_kbfile_id) {
		setMacroAreas(areas);
		this.person = person;
		this.to = to;
		this.reportScheduleName = reportScheduleName;
		this.reportScheduleDescription = reportScheduleDescription;
		this.attachment = attachment;
		this.displayname = displayname;
		this.audit_kbfile_id  =audit_kbfile_id;
		
		if (person!=null)
			setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

		
	}
	
	public EmailBuilderSendSubscriptionReport(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);

	}

	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.person= map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
			this.to=map.containsKey("to") ? ((String) map.get("to")) : null;
			this.reportScheduleName=map.containsKey("reportschedulename") ? ((String) map.get("reportschedulename")) : null;
			this.reportScheduleDescription=map.containsKey("reportscheduledescription") ? ((String) map.get("reportscheduledescription")) : null;
			
			
			
			this.displayname=map.containsKey("displayname") ? ((String) map.get("displayname")) : null;
			this.audit_kbfile_id= map.containsKey("audit_kbfile_id") ? Long.valueOf((String) map.get("audit_kbfile_id")) :null;
			
			
			if (person!=null)
				setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());

			
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public String getKey() {
			return "report_subscription";
	}
	
	@Override
	public EmailData build() {
		
		
		if (this.person==null)
			throw new IllegalArgumentException("person is null");


		if (this.to==null)
			throw new IllegalArgumentException("to is null");



		
		
		UserProfile profile = person.getProfile(UserProfile.class);
		
		EmailTemplate template = getEmailTemplate( person.getDomain(),  getLanguage(), getKey());

		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(person.getDomain(), map);
		String durl = getServerUrl(person.getDomain());
		String rsu=  durl + "/reports/subscription"; 
		
		if (reportScheduleDescription!=null)
			reportScheduleDescription=reportScheduleDescription.trim();
			
		map.put("${report-schedule-name}", reportScheduleName);
		map.put("${report-schedule-description}", reportScheduleDescription);
		map.put("${domain-url}", durl);
		map.put("${report-subscription-url}", rsu);
		map.put("${username}", profile.getUser().getUserName());
		map.put("${domain-name}", person.getDomain().getOrganization()!=null? person.getDomain().getOrganization(): person.getDomain().getName());
		map.put("${person-displayname}", displayname);

		EmailData data = parse(template, to, map,attachment, "scheduled-report-"+reportScheduleName);

		if (data!=null)
			data.setAuditKBFileId( audit_kbfile_id);
		
		return data;

	}
	
	
	@Override
	public boolean isSendEnabled()  {
		
		if (!isEnabled(person))
			return false;

		return true;
	}

	@Override
	public Domain getDomain() {
		return person.getDomain();
	}
	
	@Override
	public String getArea() {
		return REPORT;
	}

	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		r.put("to",to);
		r.put("reportScheduleName",reportScheduleName);
		r.put("reportScheduleDescription", reportScheduleDescription);
				
		//r.put("displayname;
		//r.put("audit_kbfile_id;
		//r.put("attachment;
		
		return r;
	}

}

package kbee.email;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;

public class EmailBuilderSendGrid extends EmailBuilderBase implements EmailBuilder {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	//private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private Person person;
	private File file;
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };

	
	public EmailBuilderSendGrid() {
		setMacroAreas(areas);
	}
	
	public EmailBuilderSendGrid(Person person, File file) {
		this.person=person;
		this.file=file;
		setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		setMacroAreas(areas);
	}
	
	public EmailBuilderSendGrid(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}

	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			
			this.person=map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
			
			if (map.containsKey("file")) {
				// Path is absolute
				file = new File((String) map.get("file"));
			}
			
			if (person!=null)
				setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
		} catch (Exception e) {
			logger.error(e);
		}
	}


	/**
	 * 
	 */
	@Override
	public String getKey() {
			return "send-grid";
	}
	
	
	@Override
	public EmailData build() {

		
		if (this.person==null)
			throw new IllegalArgumentException("person is null");
		
		if (this.file==null)
			logger.debug("File is null");
		


		String from = this.getNoReplyEmailAddress();
		String to_email=person.getEmail();
		String subject="Grid Export - " + person.getDomain().getDisplayName();
		String filename  =  file.getName();
		long size = file!=null?file.getTotalSpace():0;
		String msg = msg_eng + filename + "  " + String.valueOf(size);
		String local_file = (file!=null?file.getAbsolutePath():null);
		
		// there is no TEMPLATE
		EmailData data = new EmailData(from, to_email, subject, msg, null, "grid-export", local_file);
		return data;

	}

	String msg_eng = "Your export is attached. It is a .csv file that can be opened with MS Excel and other Spreadsheets.<br />File: ";
	
	@Override
	public Domain getDomain() {
		return person.getDomain();
	}

	
	
	@Override
	public boolean isSendEnabled()  {
		if (!isEnabled(person))
			return false;
		return true;
	}
		
	@Override
	public String getArea() {
		return GRID;
	}

	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		r.put("file",  file);
		return r;
	}
		
	
}

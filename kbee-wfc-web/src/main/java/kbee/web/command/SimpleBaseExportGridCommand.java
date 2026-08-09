package kbee.web.command;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.command.AbstractCommand;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

public abstract class SimpleBaseExportGridCommand extends AbstractCommand {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleExportGridContentsCommand.class.getName());

													
	static public final DateTimeFormatter dateformat = DateTimeFormatter.RFC_1123_DATE_TIME;
	
	static public final String SEPARATOR = ",";
	
	private Query query;
	
	private String working_dir;
	
	private Serializable domainId = null;
	
	@SuppressWarnings("unused")
	private Domain domain = null;
	
	@Override
	public void execute() {
		setState(CommandState.RUNNING);
		initWorkingDir();
		executeExport();
	}
	
	protected abstract void executeExport();
	
		
	public void setQuery(Query query) {
		this.query=query;
	}

	
	public Query getQuery() {
		return this.query;
	}

	public void setDomainId(Serializable id) {
		domainId = id;
	}
	
	public Serializable getDomainId() {
		return domainId;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}
	
	public boolean isRunning() {
	    	return super.getState()==CommandState.RUNNING;
	}


	protected void sendEmail(File file) {
		
		UserProfile up = getContentDao().findUserProfileByUser(getSessionUser());
		String from = up.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY);
		String to_email=up.getPerson().getEmail();
		String subject="Grid Export - " + up.getDomain().getDisplayName();
		
		// TODO spa
		String msg = "Your export is attached. It is a .csv file that you can open with MS Excel and other Spreadsheets.";
		
		String local_file = file.getAbsolutePath();
		EmailData emaildata = new EmailData(from, to_email, subject, msg, null, "Grid List Export", local_file);
 		ServiceLocator.getService(EmailService.class).sendEmail(up.getPerson(), emaildata);
	}

	
	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
	
	
	protected String escape(String str) {
		if (str==null)
			return "";
		return str.replace(SEPARATOR, " ");
	}
	
	protected String getClassification(Content content, Classifier clasi) {
		StringBuilder str = new StringBuilder();
		try {
			List<Classification> list = content.getClassification();

			for (Classification ca: list) {
					if (ca.getClassifier().equals(clasi)) {
						if (str.length()>0)
							str.append(" | ");
						str.append(ca.getStrValue());
					}
			}
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getSimpleName());
		}
		return str.toString();
	}

									
	protected String getClassification(PersonMember person, Classifier clasi) {
		StringBuilder str = new StringBuilder();
		try {
			List<Classification> list = person.getClassification();
			for (Classification ca: list) {
					if (ca.getClassifier().equals(clasi)) {
						if (str.length()>0)
							str.append(" | ");
						str.append(ca.getStrValue());
					}
			}
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getSimpleName());
		}
		return str.toString();
	}
	

							
	protected String getClassification(DataSetMember member, Classifier clasi) {
		StringBuilder str = new StringBuilder();
		try {
			List<Classification> list = member.getClassification();
			for (Classification ca: list) {
					if (ca.getClassifier().equals(clasi)) {
						if (str.length()>0)
							str.append(" | ");
						str.append(ca.getStrValue());
					}
			}
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getSimpleName());
		}
		return str.toString();
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	
	protected void initWorkingDir() {
		setWorkingDir(ServiceLocator.getService(ApplicationServerService.class).getDataExportDir()  + File.separator + "grid");
		try {
			File dir = new File( getWorkingDir());
			if (!dir.exists() || !dir.isDirectory())
				KbeeFileUtils.forceMkdir(new File( getWorkingDir()));
		} catch (IOException e) {
			logger.error(e);
			setState(CommandState.ERROR);
			return;
		}
	}

	protected String getWorkingDir() {
		return this.working_dir;
	}

	protected void setWorkingDir(String s) {
		this.working_dir=s;
	}
	
}

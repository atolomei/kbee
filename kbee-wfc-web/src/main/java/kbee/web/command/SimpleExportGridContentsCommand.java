package kbee.web.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;

import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetType;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.workflow.WorkflowContext;

/**
 *
 */
public class SimpleExportGridContentsCommand extends AbstractCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleExportGridContentsCommand.class.getName());
	
	
	static private final DateTimeFormatter xls_date 	= DateTimeFormatter.ofPattern("yyyy-MM-dd");
	static private final DateTimeFormatter dateformat 	= DateTimeFormatter.RFC_1123_DATE_TIME;
	
	private String SEPARATOR = ",";
	private Query query;
	private int total = 0;
	private String working_dir;
	private Serializable domainId = null;
	private boolean isSendByEmail = false;
	
	@SuppressWarnings("unused")
	private Domain domain = null;
	
	@SuppressWarnings("unused")
	private SessionFactory sf;

	private boolean isWorkflowConsole = true;
	private boolean isContentConsole = false;
	
	private String format;

	private  BufferedWriter out = null;
	private File file;

	
	static final public String INTERNAL_SEPARATOR = " | ";

	
	public SimpleExportGridContentsCommand(Query query) {
		this.query=query;
	}
	
	public boolean isContentBaseConsole() {
		return isContentConsole;
	}

	
	public boolean isWorkflowConsole() {
		return isWorkflowConsole;
	}
	
	
	public void setWorkflowConsole(boolean b) {
		this.isWorkflowConsole=b;
	}

	public void setContentConsole(boolean b) {
		this.isContentConsole=b;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format=format;
	}

	public String getWorkingDir() {
		return this.working_dir;
	}

	public File getFile() {
		return this.file;
	}
	 
	/**
	 * 
	 * 	
	 */
	@Override
	public void execute() {

		this.working_dir = ServiceLocator.getService(ApplicationServerService.class).getDataExportDir()  + File.separator + "grid";
		
		try {
			File dir = new File(this.working_dir);
			if (!dir.exists() || !dir.isDirectory())
				KbeeFileUtils.forceMkdir(new File(this.working_dir));
		} catch (IOException e) {
			logger.error(e);
			setState(CommandState.ERROR);
			return;
		}
				
		setState(CommandState.RUNNING);
		
		this.total = 0;
		
		int errno = 0;
		try {
				if (getQuery()==null) {
					logger.error("query is null.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query is null.");
					return;
				}
				
				startStream();
				
				ResultSet results = getQuery().execute();
				this.total = results.size();
				if (this.total==0) {
					this.setState(CommandState.COMPLETED);
					this.setProgress(100);
					return;
				}
					
				int progress = 0;
				int counter  = 0;
				int n 		 = 0;

				logger.debug("Processing: " + String.valueOf(this.total));
				
				Map<String, Classifier> 	classifiers 		= new HashMap<String, Classifier>();
				Map<String, Attribute> 		attributes 			= new HashMap<String, Attribute>();
				List<Classifier> 			list_classifiers 	= new ArrayList<Classifier>();
				List<Attribute> 			list_attributes	 	= new ArrayList<Attribute>();
 		
				errno=0;
				
				while (results.hasNext() && n<200 && errno < 100) {
					try {
							Content content = (Content) results.next().getObject();
							for (Classification clasi: content.getClassification() ) {
								Classifier c = clasi.getClassifier();
								if (c.getState()==ObjectState.ENABLED) {
									if (!classifiers.containsKey(String.valueOf(c.getId()))) {
										classifiers.put(String.valueOf(c.getId()), c);
										list_classifiers.add(c);
									}
								}
							}
							for (AttributeTemplate att: content.getContentTemplate().getAttributes()) {
									Attribute a = att.getAttribute();
								if (a.getState()==ObjectState.ENABLED) {
									if (!attributes.containsKey(String.valueOf(a.getId()))) {
										attributes.put(String.valueOf(a.getId()), a);
										list_attributes.add(a);
									}
								}
							}
							
						} catch (Exception  e) {
							errno++;
							logger.error(e);
						}
						n++;
				}

				//
				//
				exportHeader(list_classifiers, list_attributes);
				
				results = getQuery().execute();

				errno = 0;
				while (results.hasNext() && errno < 100) {
					try {
						
						exportRow((Content) results.next().getObject(), list_classifiers, list_attributes);
						
					} catch (Exception  e) {
						errno++;
						logger.error(e);
					}
					finally {
						counter++;
						if (this.total>0) 
							progress = 100 * counter/this.total;
						this.setProgress(progress);
					}
				}
		}
		catch (Throwable e) {
				logger.error(e);
				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				return;
		
		} finally {
			close();
		}

		if (errno<100) {
			
			if (isSendByEmail())
				sendEmail();
			
			setState(CommandState.COMPLETED);
			setProgress(100);
		}
		else {
			setState(CommandState.ERROR);
			this.setResult("100 rows with errors");
			this.setResultDetails("");
		}
		setDateTerminated(OffsetDateTime.now());
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * @param b
	 */
 	public void setSendByEmail(boolean b) {
		this.isSendByEmail=b;
		
	}
	public boolean isSendByEmail() {
		return this.isSendByEmail;
	}

	protected void close() {
		if (this.out!=null) {
			try {
				this.out.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
	}
	
	
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

	/***
	 * @param file
	 */
	protected void sendEmail() {
		UserProfile up = getContentDao().findUserProfileByUser(getSessionUser());
		String from = up.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY); 
		String to_email=up.getPerson().getEmail();
		String subject="Grid Export - " + up.getDomain().getDisplayName();
		String msg = "Your export is attached. It is a .csv file that you can open with MS Excel and other Spreadsheets.";
		String local_file = this.file.getAbsolutePath();
		EmailData emaildata = new EmailData(from, to_email, subject, msg, null, "Grid List Export", local_file);
 		ServiceLocator.getService(EmailService.class).sendEmail(up.getPerson(), emaildata);
	}

		
 /**	
  * 
  * 
  * @param list_classifiers
  * @param list_attributes
  * @param out
  * @throws IOException 
  * 
  */
 protected void exportHeader(List<Classifier> list_classifiers, List<Attribute> list_attributes) throws IOException {

		// init export file
		StringBuilder header = new StringBuilder();
		
		header.append("Title");
		header.append(SEPARATOR);
		
		header.append("Oid/Id");
		header.append(SEPARATOR);
		
		if (isWorkflowConsole()) {
			header.append("Procedure");
			header.append(SEPARATOR);

			header.append("Procedure Started");
			header.append(SEPARATOR);

			header.append("Task");
			header.append(SEPARATOR);
			
			header.append("Workspace");
			header.append(SEPARATOR);
			
			header.append("Task Started");
			header.append(SEPARATOR);
			
			header.append("Due Date");
			header.append(SEPARATOR);
		}
		
		header.append("Modified");
		header.append(SEPARATOR);
		
		header.append("Modified by");
		header.append(SEPARATOR);
		
		header.append("Content Class");
		header.append(SEPARATOR);

		header.append("external Id");
		header.append(SEPARATOR);

		
		// Classifiers
		int m=0;
		for (Classifier cl: list_classifiers) {
			if (m>0)
				header.append(SEPARATOR);
			header.append(cl.getName());
			m++;
		}
		
		if(!list_attributes.isEmpty())
			header.append(SEPARATOR);
		
		// Attrtibutes
		int p=0;
		for (Attribute a: list_attributes) {
			if (p>0)
				header.append(SEPARATOR);
			header.append(a.getName());
			p++;
		}
		
		logger.debug(header.toString());
		
		out.write(header.toString()+"\n");
		
	}

 
 /**
  * 	
  * ExportRow
  * 
  */
protected void exportRow(Content content, List<Classifier> list_classifiers, List<Attribute> list_attributes) throws Exception {

		WorkflowService workflowService = content.getService(WorkflowService.class);
	
		StringBuilder str = new StringBuilder();
		
		// Title 
		//
		str.append(escape(content.getTitle()));
		str.append(SEPARATOR);
		

		// Oid / Id 
		//
		str.append(escape(String.valueOf(content.getOId())+" / "+String.valueOf(content.getId())));
		str.append(SEPARATOR);
		

	if (isWorkflowConsole()) {
		
		// Procedure 
		//
		String proc = workflowService==null || workflowService.getContext().getProcedure()==null ? "" : workflowService.getContext().getProcedure().getName();
		str.append(escape(proc));
		str.append(SEPARATOR);
		
		
		// Procedure Started 
		//
		OffsetDateTime startdate = (workflowService!=null && workflowService.getContext()!=null && workflowService.getContext().getProcess()!=null)?workflowService.getContext().getProcess().getStartTime():null;
		if (startdate!=null)
				str.append(escape(dateformat.format(startdate)));
			else
				str.append("");
		str.append(SEPARATOR);
		
		
		// Task 
		//
		String taskname = (workflowService==null || workflowService.getTask()==null) ? "" : workflowService.getTask().getName();
		str.append(escape(taskname));
		str.append(SEPARATOR);
		
		
		// Workspace 
		//
		Long wks = content.getWorkspace();
		if (wks!=null && wks.longValue()>0) {
			User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserById(wks);
			if (user!=null)
				str.append(escape(user.getFirstLastName()));
			else
				str.append("");
		}
		else {
			str.append("");
		}
		str.append(SEPARATOR);

						
		// Task Started 
		//
		if (	content.getService(WorkflowService.class)!=null && 
				content.getService(WorkflowService.class).getContext()!=null && 
				content.getService(WorkflowService.class).getContext().getTime()!=null  ) {
			
				str.append(escape(dateformat.format(content.getService(WorkflowService.class).getContext().getTime())));
		}
		else
			str.append("");
		str.append(SEPARATOR);
		
		
		// Due Date 
		//
		WorkflowContext context = (WorkflowContext)content.getService(WorkflowService.class).getContext();
		OffsetDateTime duedate = context.getDueDate();
		if (duedate!=null)
			str.append(escape(dateformat.format(duedate)));
		else
			str.append("");
		str.append(SEPARATOR);
	
	}
	
		// Modified -----------------------------------------------
		//
		OffsetDateTime modi = content.getLastModifiedOffsetDateTime();
		if (modi!=null)
			str.append(escape(dateformat.format(modi)));
		else
			str.append("");
		str.append(SEPARATOR);

		
		// Modified User -----------------------------------------------
		//
		if (content.getLastModifiedUser()!=null) 
			str.append(content.getLastModifiedUser().getFirstLastName());
		else
			str.append("");
		str.append(SEPARATOR);

		
		// Content Class -----------------------------------------------
		//	
		if (content.getContentTemplate()!=null)
			str.append(escape(content.getContentTemplate().getDisplayName()));
		else
			str.append("");
		str.append(SEPARATOR);
		

		// External Id ---------------------------------------------------
		//	
		if (content.getExternalId()!=null)
			str.append(escape(content.getExternalId()));
		else
			str.append("");
		str.append(SEPARATOR);

		
		// Classifiers -----------------------------------------------
		//
		int r=0;

		for (Classifier cl: list_classifiers) {
			String s = getClassification(content, cl);
			if (r>0)
				str.append(SEPARATOR);
			str.append(s);
			r++;
		}
		
		if(!list_attributes.isEmpty())
			str.append(SEPARATOR);
		
		// Attributes -----------------------------------------------
		//
		int q=0;

		for (Attribute a: list_attributes) {
			String s = getAttribute(content, a);
			if (q>0)
				str.append(SEPARATOR);
			str.append(s);
			q++;
		}
		logger.debug(str.toString());
		out.write(str.toString()+"\n");
}
 
	
/**
 *
 * 	
 */
protected ContentDao getContentDao() {
	 return  (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
}


/**
 * 
 * 	
 */
protected String escape(String str) {
	if (str==null)
		return "";
	return str.replace(SEPARATOR, "").replace(INTERNAL_SEPARATOR, " - ");
}


/**
 * 
 * 	
 */
protected String getAttribute(Content content, Attribute att) {
	List<String> list = content.getAttributeValues(att);
	StringBuilder str = new StringBuilder();
	try {
		for (String s: list) {
					if (str.length()>0)
						str.append(" | ");
					str.append(s);
		}
		
	} catch (Exception e) {
		logger.error(e);
	}
	return str.toString();
}


/**
 * 
 * 	
 */
protected void startStream() throws IOException {
	long start = System.currentTimeMillis();
	String name = (this.isWorkflowConsole()?"tasks-":"content-") + getSessionUser().getUserName().replace("@", "-") + "-" + String.valueOf(start);
	this.file = new File(this.working_dir + File.separator + name + ".csv");
	this.out = new BufferedWriter(new FileWriter(file));
}
 


/**
 * 
 * 	
 */
protected String getClassification(Content content, Classifier clasi) {
	List<Classification> list = content.getClassification();
	StringBuilder str = new StringBuilder();
	try {
		for (Classification ca: list) {
				if (ca.getClassifier().equals(clasi)) {
					if (ca.getDataSetType()==DataSetType.DATE) {
						OffsetDateTime da = ca.getDateValue();
						if (da!=null) {
								str.append(xls_date.format(da));
						}
						else {
							str.append("");
						}
						return str.toString();
					}
					else {
						if (str.length()>0)
							str.append( INTERNAL_SEPARATOR);
						str.append(ca.getStrValue());
					}
				}
			
		}
	} catch (Exception e) {
		logger.error(e);
	}
	return str.toString();
}



/**
 * 	
 */
protected KbeeUser getSessionUser() {
	return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
}


}
 




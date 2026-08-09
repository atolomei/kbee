package kbee.web.command;


import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.indexer.query.Query;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Exports Content grid to MS Excel
 */
public class SimpleExcelExportGridCommand extends SimpleExportGridContentsCommand {
				
	static private Logger logger = LogManager.getLogger(SimpleExcelExportGridCommand.class.getName());
	
	static private final DateTimeFormatter dateformat 	= DateTimeFormatter.RFC_1123_DATE_TIME;
	
	private WritableWorkbook book;
	private WritableSheet excelSheet;
	private String file_name;
	private int current_row = 0;

	
	public SimpleExcelExportGridCommand(Query query) {
		super(query);
	}
	
	/**
	 * 
	 */
	@Override
	protected void startStream() throws IOException {
		long start = System.currentTimeMillis();
		String name = (this.isWorkflowConsole()?"tasks-":"content-") + getSessionUser().getUserName().replace("@", "-") + "-" + String.valueOf(start);
		this.file_name = this.getWorkingDir() + File.separator + name + ".xls";
		this.book =  Workbook.createWorkbook(new File(file_name));
		this.excelSheet =book.createSheet("Data 1", 0);
	}

	/**
	 * 
	 */
	@Override
	protected void exportHeader(List<Classifier> list_classifiers, List<Attribute> list_attributes) throws IOException {

		try {
        
			int col = 0;
			int row = 0;
			
			this.excelSheet.addCell( new Label(col++, row, "Title"));
			this.excelSheet.addCell( new Label(col++, row, "Oid-version-Id"));
        	if (isWorkflowConsole()) {
        		this.excelSheet.addCell( new Label(col++, row, "Procedure"));
        		this.excelSheet.addCell( new Label(col++, row, "Procedure Started"));
        		this.excelSheet.addCell( new Label(col++, row, "Task"));
        		this.excelSheet.addCell( new Label(col++, row, "Workspace"));
        		this.excelSheet.addCell( new Label(col++, row, "Task Started"));
        		this.excelSheet.addCell( new Label(col++, row, "Due Date"));
    		}
        	this.excelSheet.addCell( new Label(col++, row, "Modified"));
        	this.excelSheet.addCell( new Label(col++, row, "Modified by"));
        	this.excelSheet.addCell( new Label(col++, row, "Content Class"));

        	if (isContentBaseConsole()) {
        		this.excelSheet.addCell( new Label(col++, row, "Checkout by"));
        	}
        	
    		for (Classifier cl: list_classifiers) 
    			this.excelSheet.addCell(new Label(col++, row, cl.getName()));
    		
    		for (Attribute a: list_attributes) 
    			this.excelSheet.addCell(new Label(col++, row, a.getName()));

		} catch (WriteException e) {
			logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		}
	}


	/**
	 * 
	 * 
	 */
	@Override
	protected void exportRow(Content content, List<Classifier> list_classifiers, List<Attribute> list_attributes) throws Exception {

		int col = 0;
		
		current_row++;
		 
		WorkflowService workflowService = content.getService(WorkflowService.class);
		
		/** Title */
		this.excelSheet.addCell( new Label(col++, current_row, escape(content.getTitle())));
	
		
		/** Oid-version-Id */ 
		this.excelSheet.addCell( new Label(col++, current_row, escape(String.valueOf(content.getOId())+ "-" + String.valueOf(content.getVersion()).trim()+"-"+String.valueOf(content.getId()))));
		

		if (isWorkflowConsole()) {
			
			// Procedure 
			//
			String proc = workflowService==null || workflowService.getContext().getProcedure()==null ? "" : workflowService.getContext().getProcedure().getName();
			this.excelSheet.addCell( new Label(col++, current_row,escape(proc)));
			
			
			// Procedure Started 
			//
			OffsetDateTime startdate = (workflowService!=null && workflowService.getContext()!=null && workflowService.getContext().getProcess()!=null)?workflowService.getContext().getProcess().getStartTime():null;
			if (startdate!=null)
					this.excelSheet.addCell( new Label(col++, current_row, escape(dateformat.format(startdate))));
			else
					this.excelSheet.addCell( new Label(col++, current_row,""));
			
			// Task 
			//
			String taskname = (workflowService==null || workflowService.getTask()==null) ? "" : workflowService.getTask().getName();
			this.excelSheet.addCell( new Label(col++, current_row, escape(taskname)));
			
			
			// Workspace 
			//
			Long wks = content.getWorkspace();
			if (wks!=null && wks.longValue()>0) {
				User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserById(wks);
				if (user!=null)
					this.excelSheet.addCell( new Label(col++, current_row,escape(user.getLastFirstName())));
				else
					this.excelSheet.addCell( new Label(col++, current_row,""));
			}
			else {
				this.excelSheet.addCell( new Label(col++, current_row,""));
			}
			
	
			// Task Started 
			//
			if (content.getService(WorkflowService.class)!=null && 
				content.getService(WorkflowService.class).getContext()!=null && 
				content.getService(WorkflowService.class).getContext().getTime()!=null) {
				this.excelSheet.addCell( new Label(col++, current_row,escape(dateformat.format(content.getService(WorkflowService.class).getContext().getTime()))));
			}
			else
				this.excelSheet.addCell( new Label(col++, current_row,""));
			
 			// Due Date 
			//
			WorkflowContext context = (WorkflowContext)content.getService(WorkflowService.class).getContext();
			OffsetDateTime duedate = context.getDueDate();
			if (duedate!=null)
				this.excelSheet.addCell( new Label(col++, current_row,escape(dateformat.format(duedate))));
			else
				this.excelSheet.addCell( new Label(col++, current_row,""));
			
		}
	
		
		
		// Modified -----------------------------------------------
		//
		OffsetDateTime modi = content.getLastModifiedOffsetDateTime();
		if (modi!=null)
			this.excelSheet.addCell( new Label(col++, current_row, escape(dateformat.format(modi))));
		else
			this.excelSheet.addCell( new Label(col++, current_row,""));
	
		
		// Modified User -----------------------------------------------
		//
		if (content.getLastModifiedUser()!=null) 
			this.excelSheet.addCell( new Label(col++, current_row, escape(content.getLastModifiedUser().getLastFirstName())));
		else
			this.excelSheet.addCell( new Label(col++, current_row,""));
			
	
		// Content Class -----------------------------------------------
		//	
		if (content.getContentTemplate()!=null)
			this.excelSheet.addCell( new Label(col++, current_row,escape(content.getContentTemplate().getDisplayName())));
		else
			this.excelSheet.addCell( new Label(col++, current_row,""));
	
		
    	if (isContentBaseConsole()) {
    		if (content.isLocked()) {
    			String nam = (content.getLastModifiedUser()!=null? content.getLastModifiedUser().getLastFirstName():"Yes [NA]");
    			this.excelSheet.addCell( new Label(col++, current_row,escape(nam)));
    		}
    		else
    			this.excelSheet.addCell( new Label(col++, current_row,""));
    	}

		// Classifiers -----------------------------------------------
		//
		for (Classifier cl: list_classifiers) {
			String s = getClassification(content, cl);
			this.excelSheet.addCell( new Label(col++, current_row,s));
		}
		
		// Attributes -----------------------------------------------
		//
		for (Attribute a: list_attributes) {
			String s = getAttribute(content, a);
			this.excelSheet.addCell(new Label(col++, current_row,s));
		}
	}

	/**
	 */
	@Override
	protected void close() {
		
        if (this.book != null) {
        	try {
        		
        		this.book.write();
            	this.book.close();
            	
            } catch (IOException e) {
    			logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            } catch (WriteException e) {
    			logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            }
        }
	}

	@Override
	public File getFile() {
		return new File(this.file_name);
	}


	@Override
	protected String escape(String str) {
		if (str==null)
			return str;
		return str.replace(INTERNAL_SEPARATOR, " - ");
	}
	
	
	/**
	 */
	@Override
	protected void sendEmail() {
		UserProfile up = getContentDao().findUserProfileByUser(getSessionUser());
		String from = up.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY); 
		String to_email=up.getPerson().getEmail();
		String subject="Grid xls export - " + up.getDomain().getDisplayName();
		String msg = "Your export is attached. It is a .xls file that you can open with MS Excel.";
	
		File file = getFile();
		String local_file = file.getAbsolutePath();
		
		EmailData emaildata = new EmailData(from, to_email, subject, msg, null, "Grid List Export", local_file);
 		ServiceLocator.getService(EmailService.class).sendEmail(up.getPerson(), emaildata);
	}

}

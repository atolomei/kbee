package com.novamens.kbee.content.command;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailService;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.service.datamanagement.DirectoryZipper;
import com.novamens.kbee.content.service.datamanagement.KbeeHTMLExporter;

import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.util.KbeeFileUtils;

import kbee.email.EmailBuilderDBExport;

/** 
 *  max: max elements
 *  classifier_id: value_id
 *  modified:
 *  modified_user:
 *  domain: 
 *
 */
public class ExportContentsCommand extends AbstractCommand implements Runnable {
			
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ExportContentsCommand.class.getName());
	
	private Thread thread;
	private boolean running;

	private Query query;
	
	private DMExporter exporter;

	private int total = 0;
	
	private String zip_file_path = null;
	
	private Serializable domainId = null;
	
	@SuppressWarnings("unused")
	private Domain domain = null;
	
	@SuppressWarnings("unused")
	private SessionFactory sf;
	
	private int max_to_export = -1;	

	public class Tuple {
		public Classifier clasi;
		public DataSetMember member;
		
		public Tuple(Classifier clasi, DataSetMember member) {
			this.clasi=clasi;
			this.member=member;
		}
	}
	
	
	public ExportContentsCommand() {
		setName("Export Contents Command");
	}

	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		if (getQuery()!=null)
			str.append(" | " + getQuery());

		return str.toString();	
	}
	
	
	public void setExporter( DMExporter exporter) {
		this.exporter=exporter;
	}

	
	public DMExporter getExporter() {
		return this.exporter;
	}

					
	public void setQuery(Query query) {
		this.query=query;
	}

	
	public Query getQuery() {
		return this.query;
	}

	@Override
	public void run() {
		setState(CommandState.RUNNING);
		executeTask();
	}
	
	@Override
	public synchronized void stop() {
		super.stop();
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
	    	return this.running;
	}
	
	
	@Override
	public void execute() {
		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(Thread.NORM_PRIORITY);
    	this.thread.start();
	}

	
	public void setMaxToExpor(int max) {
		this.max_to_export=max;
	}


	public int getMaxToExport() {
		return this.max_to_export;
	}
	
	
	protected void exportData() {
	
		total = 0;
		
		try {
			
				if (getQuery()==null) {
					logger.error("query is null.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query is null.");
					return;
				}
		
				logger.debug(getQuery().toString());
				
				init_demo_exporter();
			
				if (getExporter()==null) {
					logger.error("exporter is null");
					this.setResultComments("exporter is null");
					this.setState(CommandState.ERROR);
					return;
				}
			
				getExporter().start();
				
				ResultSet results = getQuery().execute();
			
				total = results.size();
				
				
				if (total==0) {
					this.setState(CommandState.COMPLETED);
					this.setProgress(100);
					return;
				}
					
				int progress = 0;
				int counter = 0;
				
				logger.debug("Processing: " + String.valueOf(total) + " items");
				
				while (results.hasNext() && (getMaxToExport()==-1 || (counter<getMaxToExport()))) {
					Content content = (Content) results.next().getObject();
					try {
						getExporter().export(content, counter);
					} catch (RuntimeException  e) {
						logger.error(e, content!=null?content.getTitle():"null");
					}
					counter++;
					if (total>0) {
						progress = 50 * counter/total;
					}
					this.setProgress(progress);
					logger.debug("done -> " + content.getTitle());
				}
				
				Thread.sleep(1000);
				this.setProgress(50);
				
		}
		catch (Throwable e) {
				
				logger.error(e);
				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
		
		} finally {
			logger.debug("done");
			if (getExporter()!=null)
				getExporter().close();
		}
	}
	
		

	
	public String getZipFilePath() {
		return this.zip_file_path;
	}


	protected void compressExportedData() {
	
	   String srcdir = getExporter().getExportDir();
	   String desdir = getWorkDir()  + File.separator + "zxp"  + "-" + String.valueOf(System.currentTimeMillis());
	   LocalDateTime now = LocalDateTime.now();
	   
	   String nam = String.format("%4d-%02d-%02d-%02d%02d%02d",
	   now.getYear(),
	   now.getMonth().getValue(),
	   now.getDayOfMonth(),
	   now.getHour(),
	   now.getMinute(),
	   now.getSecond());
	   
	  String zipfile = "dbexport-"+getExporter().getDomain().getName().trim() + "-" + nam +".zip";
		
	  this.zip_file_path = desdir + File.separator + zipfile;
		
	  DirectoryZipper zipper = new DirectoryZipper(new File(srcdir), new File(desdir), zipfile);
		
	  try {
			zipper.execute();
			this.setProgress(100);
			this.setState(CommandState.COMPLETED);
			setDateTerminated(OffsetDateTime.now());
			this.setResult("ok");
			this.setResultComments("Processed " +  String.valueOf(total));
			this.setResultDetails("Export Directory: " + getExporter().getExportDir());
			
			
	  } catch (IOException e) {
			logger.error(e);

			this.setResult(e.getClass().getSimpleName());
			this.setResultDetails(e.getMessage());
			setState(CommandState.ERROR);
			setDateTerminated(OffsetDateTime.now());
	  }
	}


	protected void deleteTempDir() {
		String dir = getExporter().getExportDir();
		try {
			KbeeFileUtils.forceDelete(new File(dir));
		} catch (IOException e) {
			logger.error(e);

		}
	}
	

	protected void sendEmail() {
		UserProfile up = getContentDao().findUserProfileByUser(getExporter().getUserExport());
		
		if (this.zip_file_path!=null) { 
		
			EmailBuilderDBExport builder = new EmailBuilderDBExport(up.getPerson(), this.zip_file_path);
			ServiceLocator.getService(EmailService.class).send(builder);
			
			//ServiceLocator.getService(EmailService.class).sendDBExportLink(up.getPerson(), this.zip_file_path);
			// DBLogger.info(new DownloadEvent((String) getConsole().getDisplayName().getObject(), fileName));
			// LOG EVENT ?
			// 
		}
		else
			logger.error("Zip File is null");
	}

	
	public String getZipPath() {
		return this.zip_file_path;
	}
	
	protected void executeTask() {

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		try {
			
			logger.debug("Starting Export Contents Command");

			sf = com.novamens.hibernate.session.Session.open();
			
			exportData();
			compressExportedData();
			sendEmail();
			
			logger.debug("Zip file. " + this.zip_file_path);
			logger.debug("done. ");
			
		} finally {
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");
			deleteTempDir();
		}
	}


	protected void setRunning(boolean value) {
    	this.running = value;
	}
	
	/**
	 * @throws IOException
	 */ 
	private void init_demo_exporter() throws IOException {
		
		this.exporter=new KbeeHTMLExporter(getUserId());
		
		if (getQuery() instanceof SolrParametersQuery)
				this.exporter.setQueryStr(((SolrParametersQuery) getQuery()).getStatement());
		
		else if (getQuery() instanceof HibernateQuery)
			this.exporter.setQueryStr(((HibernateQuery) getQuery()).getStatement());
		
	}
	
	
	private ContentDao getContentDao() {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 return  (ContentDao) beans.getBean("contentDao");
	}
}

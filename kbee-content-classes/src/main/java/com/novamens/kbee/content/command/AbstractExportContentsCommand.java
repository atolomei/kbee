package com.novamens.kbee.content.command;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.content.user.UserProfile;
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


public class AbstractExportContentsCommand extends AbstractCommand implements Runnable {

	static Logger logger = LogManager.getLogger(ExportContentsCommand.class.getName());
	
	private Thread thread;
	private boolean running;

	private Query query;
	
	private DMExporter exporter;

	private int total = 0;
	
	private String zip_file_path = null;
	
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
	
	
	public AbstractExportContentsCommand() {
		setName("Abstract Export Contents Command");
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
		
				logger.info(getQuery().toString());
				
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
				
				logger.info("Processing: " + String.valueOf(total));

				int errno=0;
				
				while (results.hasNext() && (getMaxToExport()==-1 || (counter<getMaxToExport()))) {
					Content content = (Content) results.next().getObject();
					try {
						getExporter().export(content, counter);
					} catch (Exception  e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						if (errno++>100)
							break;
					}
					counter++;
					if (total>0) {
						progress = 50 * counter/total;
					}
					this.setProgress(progress);
					logger.info(content.getTitle());
				}
				
				Thread.sleep(1000);
				this.setProgress(50);
				
		}
		catch (Throwable e) {
				logger.error(e.getClass().getName(), e);
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());

				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
		
		} finally {
			
			if (getExporter()!=null)
				getExporter().close();
		}
	}
	
	public String getZipFilePath() {
		return this.zip_file_path;
	}


	protected String getExportSubdir() {
		return "zxp";
	}
	
	protected String getZipFileNamePrefix() {
		return "dbexport-";
	}
	
	protected void compressExportedData() {
	
	   String srcdir = getExporter().getExportDir();

	   
	   final String desdir = getDataExportDir()  + File.separator + getExportSubdir()  + "-" + String.valueOf(System.currentTimeMillis());
	   
	   LocalDateTime now = LocalDateTime.now();
	   
	   String nam = String.format("%4d-%s-%02d-%02d%02d%02d",
	   now.getYear(),
	   now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) ,
	   now.getDayOfMonth(),
	   now.getHour(),
	   now.getMinute(),
	   now.getSecond());
	   
	  String zipfile = getZipFileNamePrefix()+getExporter().getDomain().getName().trim() + "-" + nam +".zip";
		
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
			logger.error(e.getStackTrace());
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
			 EmailBuilderDBExport builder=new EmailBuilderDBExport(up.getPerson(), this.zip_file_path);
			 ServiceLocator.getService(EmailService.class).send(builder);
			//ServiceLocator.getService(EmailService.class).sendDBExportLink(up.getPerson(), this.zip_file_path);
		}
		else
			logger.error("Zip File is null");
	}
	

	protected void executeTask() {

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		try {
			sf = com.novamens.hibernate.session.Session.open();
			exportData();
			compressExportedData();
			sendEmail();
			
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
	protected void init_demo_exporter() throws IOException {
		
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

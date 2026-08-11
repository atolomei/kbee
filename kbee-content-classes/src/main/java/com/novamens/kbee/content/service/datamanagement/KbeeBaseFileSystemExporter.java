package com.novamens.kbee.content.service.datamanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.TextContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;

public abstract class KbeeBaseFileSystemExporter implements DMExporter {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFileSystemExporter.class.getName());
	
	static final private int XSECTION_SIZE = 1000;
	
	static private final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss z");
	static public final String FIELD_SEPARATOR  = " | ";	
	static public final String NA 				= " N/A ";
	
	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;

	private String working_dir;
	private String export_dir;
	private String export_log_dir;
	
	private Serializable uid;
	
	private String query_str;
	private int exported = 0;
	private int attachments_exported = 0;
	private int errors = 0;

	private BufferedWriter global_log = null;
	private long start_time;
	private long end_time;
	
	private User user;

	private boolean isInitialized  =false;
	
	
	public static int getSectionSize() {
		return XSECTION_SIZE;
	}

	private boolean is_stand_alone = false;
	
	
	/** 
	 * @param uid
	 * 
	 * There is no SessionUser because the command is called via the Scheduler
	 *  
	 */
	public KbeeBaseFileSystemExporter(Serializable uid) {

		this.uid=uid;
		this.working_dir = getDataExportDir()  + File.separator + "xp" + uid.toString() + "-" + String.valueOf(System.currentTimeMillis());

		// Export dir es absoluto
		setExportDir(this.working_dir + File.separator + "export");

		// Log dir es absoluto
		setLogDir(this.working_dir + File.separator + "export" + File.separator + "log");
	}	
	

	

	/** 
	 * 
	 * 1 directorio para el Content: contentid-contentoid-content-class:
   	 *
   	 * Ej:
	 * 234532-oid345676-iDOC/
	 * 
	 * Los archivos son:
	 * 
	 * 234532-iDOC-info.txt
	 * 234532-iDOC-audit.txt
	 * 
	 * pdf1, 2 etc.
	 * 
	 */
	@Override
	public void export(Content content) {
		export(content, -1);
	}
	

	/**
	 * 
	 */
	@Override
	public void export(Content content, int index) {
		
		if (!isInitialized()) {
			logger.error("not initialized ");
			throw( new KbeeRuntimeException ("Exporter is not started."));
		}
		
		try {

			long start = System.currentTimeMillis();
			
			String content_dir = getExportDir() + File.separator + getContentHomeDir(content, index);
			
			KbeeFileUtils.forceMkdir(new File(content_dir));
			
			if (content instanceof TextContainer) 
				exportText(content, index, content_dir);
			

			exportResourceList(content, index, content_dir);
			
			exportAttributes(content, index, content_dir);
			
			
			if (content.getContentTemplate().isAbstract()) {
				exportNotes(content, index, content_dir);
			}
			
			if (content.getContentTemplate().isCustomAttributes()) {
				if (hasPermissionsCustomAttributes(content))				
					exportCustomTags(content, index, content_dir);
			}
			
			if (content.getContentTemplate().isPrivateNotes()) {
				if (hasPermissionsPrivateNotes(content))
					exportPrivateNotes(content, index, content_dir);
			}
			
			if (hasPermissionsAuditTrail(content))
				exportAuditTrail(content, index, content_dir);

			if (hasPermissionsResources(content))
				exportResources(content, true, index, content_dir);
			
			if (hasPermissionsPrivateNotes(content))
				exportResources(content, false, index, content_dir);
			
			long end = System.currentTimeMillis();
			
			long duration = end - start;
	
			if (!this.isStandAlone())
				logExport(content, duration);
			
			// no hace nada, las clases hijas se ocupan
			//
			onAfterExportElement(content, index);
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	
	
	
	
	
	
	
	@Override
	public void start()  throws IOException {
		init();
	}
	
	

	@Override
	public void setExportDir(String home_dir) {
		this.export_dir=home_dir;
	}

	
	
	@Override
	public void setQueryStr(String str) {
		this.query_str=str;
	}

	

	@Override
	public String getQueryStr() {
		return this.query_str;
	}
	
	

	@Override
	public String getExportDir() {
		return export_dir;
	}

	

	@Override
	public void setLogDir(String dir) {
		this.export_log_dir=dir;
	}
	
	

	@Override
	public String getExportLogDir() {
		return export_log_dir;
	}
	

	
	
	public void export(Content content, RelationTemplate relation, String source_target, int index) {
		
		if (!isInitialized())
			throw( new KbeeRuntimeException ("Exporter is not started."));
		
		try {

			long start = System.currentTimeMillis();
			
			String content_dir = getExportDir() + File.separator + getContentHomeDir(content, index);
			
			KbeeFileUtils.forceMkdir(new File(content_dir));
			
			exportRelationshipResources(content, relation, source_target, index, content_dir);
			
			long end = System.currentTimeMillis();
			
			long duration = end - start;
	
			if (!this.isStandAlone())
				logExport(content, duration);
			
			// no hace nada, las clases hijas se ocupan
			//
			onAfterExportElement(content, index);
			
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	public void exportRelationshipResources(Content content, RelationTemplate relation, String source_target, int index, String home_dir) {
		
		if (source_target==null)
			throw new IllegalArgumentException("source_target is null");
		
		 String name=relation.getName();
		 
		 if (source_target.toLowerCase().trim().equals("source")) {
			 List<Relation> dir=content.getRelations();
			 for (Relation r:dir) {
				 if (name.equals(r.getTemplate().getName())) {
					 Content c_t=getContentDao().findContentById(r.getTarget().getId());
					 exportResources(c_t, true, index, home_dir);
				 }
			 }
		 }
		 else {
			 List<Relation> rev=content.getReverseRelations();
			 for (Relation r:rev) {
				 if (name.equals(r.getTemplate().getName())) {
					 Content c_s=r.getSource();
					 exportResources(c_s, true, index, home_dir);
				 }
			 }
		}
	}



	
	protected boolean hasPermissionsResources(Content content) {
		
		if (isAdminUser())
			return true;

		if (isSupportUser())
			return false;
		
		return true;	
	}

	
	protected boolean hasPermissionsPrivateNotes(Content content) {
		
		if (isAdminUser())
			return true;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(content, this.getUserExport());
	}
	
	
	protected boolean hasPermissionsCustomAttributes(Content content) {
		if (isAdminUser())
			return true;
		return true;
	}
	
	protected boolean hasPermissionsAuditTrail(Content content) {
		return true;
	}

	protected boolean isAdminUser() {
		User  user = getUserExport();
		boolean b=ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId());
		return b;
		
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember( this.getUserExport(), KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isInitialized() {
		return this.isInitialized;
	}


	public long getStartTime() {
		return this.start_time;
	}

	public void setStandAlone(boolean b) {
		this.is_stand_alone = b;
	}

	public boolean isStandAlone() {
		return this.is_stand_alone;
	}

		
	
	@Override
	public void close() {
		
		// es para las subclases
		onAfterExport(this.getExportDir());
		
		if (getGlobalLog()!=null) {
			try {
		
				this.end_time = System.currentTimeMillis();
				getGlobalLog().write("\n");
				getGlobalLog().write("\n");
				
				getGlobalLog().write("Total exported: " + String.valueOf(getExported()));
				getGlobalLog().write("\n");
				
				getGlobalLog().write("Total attachments: " + String.valueOf(getattachmentsExported()));
				getGlobalLog().write("\n");
				
				getGlobalLog().write("Duration: "+  ServiceLocator.getService(DateTimeService.class).formatLapseSeconds(end_time-start_time, Locale.getDefault()) +"\nGenerated: "+ dateformat.format(new Date()));
				
				
				
			}
			 catch (IOException e) {
					logger.error(e);
			}
			finally {
				try {
					
					if (getGlobalLog()!=null)
						getGlobalLog().close();
					
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
		
		
		
		@Override
		public int getErrors() {
		return this.errors;
		}
		
		
		@Override
		public int getExported() {
		return this.exported;
		}
		
		
		@Override
		public int getattachmentsExported() {
		return this.attachments_exported;
		}
		
		
		protected void incAttachmentsExported() {
		this.attachments_exported++;
		}
		
		
		protected void incExported() {
		this.exported++;
		}
		
		public User getUserExport() {
			if (this.user==null) {
				this.user = getUser();
			}
			return this.user;
		}
		
		
		public Domain getDomain() {
		return getContentDao().findUserProfileByUserId(getUser().getId()).getDomain();
		}
		
			
	
	
	protected void logExport(Content content, long duration) {
		
		logger.info(content.getDisplayName() + " Duration: " + String.valueOf(duration) + " ms");
		
		incExported();
		
		try {
			
				if (getGlobalLog()!=null) {
							getGlobalLog().write(String.format("%6d", getattachmentsExported()));
							getGlobalLog().write(FIELD_SEPARATOR);
							
							if (content.getTitle()!=null)
								getGlobalLog().write(content.getTitle().replace(FIELD_SEPARATOR, "_"));
							else
								getGlobalLog().write("n/a");
							getGlobalLog().write(FIELD_SEPARATOR);
							
							if (content.getId()!=null)
								getGlobalLog().write(content.getId().toString());
							else
								getGlobalLog().write("n/a");
							getGlobalLog().write(FIELD_SEPARATOR);
							
							if (content.getOId()!=null)  
								getGlobalLog().write(content.getOId().toString());
							else
								getGlobalLog().write("n/a");
							getGlobalLog().write(FIELD_SEPARATOR);
							
							if (content.getContentTemplate()!=null) 
								getGlobalLog().write(content.getContentTemplate().getName().replace(FIELD_SEPARATOR, "_"));
							else  
								getGlobalLog().write("n/a");
							getGlobalLog().write(FIELD_SEPARATOR);
							
							getGlobalLog().write(String.valueOf(duration)+ " ms");
							
							getGlobalLog().write("\n");
				}
			
		} catch (Exception e) {
			logger.error(e);
		}
	}


	protected void onAfterExportElement(Content content, int index) 	{}
	protected void onAfterExport(String home_dir) 						{}
				
	protected abstract void exportResourceList(Content content, int index, String home_dir);
	protected abstract void exportAttributes(Content content, int index, String home_dir);
	protected abstract void exportAuditTrail(Content content, int index,  String home_dir);
	protected abstract void exportCustomTags(Content content, int index, String content_dir);
	protected abstract void exportNotes(Content content, int index, String content_dir);
	protected abstract void exportPrivateNotes(Content content, int index, String content_dir);
	protected abstract void exportText(Content content, int index, String content_dir);

	
	
	protected String getHomeResourcesDir(String home_dir) {
		return home_dir + File.separator + "files";
		
	}
	
	/** 
	 * Se exportan todos, los publicos y privados
	 * La lista de publicos se exportan en ResourcesList 
	 * y la lista de privados se exporta en PrivateNotes
	 * 
	 * 
	 */
	protected void exportResources(Content xcontent, boolean public_resources,  int index,  String home_dir) {
		
		Content content;
		
		if (xcontent instanceof HibernateProxy)
			content = (Content) Hibernate.unproxy(xcontent);
		else
			content=xcontent;

		content=getContentDao().findContentById(xcontent.getId());
		
		if  (    content.getClassCode().equals(IDoc.CLASS_CODE) 
			  || content.getClassCode().equals(TreeIDoc.CLASS_CODE)
			  || content.getClassCode().equals(OrganizationalText.CLASS_CODE)
			)
		{
			
			List<Resource> list = ((ResourceContainer) content).getResources(public_resources);
			
			for (Resource resource: list) {
				
				logger.debug("starting to process -> " + resource.getTitle());
				
				if (resource instanceof KBFile) {
					
						try {
							
							File file = ((KBFile) resource).getFile();
							
							if (file!=null) {
								
								File dest_file = new File( getHomeResourcesDir(home_dir) + File.separator + file.getName());
								
								if (dest_file.exists()) 
									dest_file = new File( getHomeResourcesDir(home_dir) + File.separator + String.valueOf( ((KBFile) resource).getId()) + "-" +file.getName()); 
								
								logger.debug("Copying -> " + dest_file.getAbsolutePath());
								
								FileUtils.copyFile(file, dest_file);
								
								incAttachmentsExported();
							}
							else {
								try {
									if (getGlobalLog()!=null)
										getGlobalLog().write("File not found id: " +  ((KBFile) resource).getId().toString()+"\n");
									else
										logger.error("File not found id: " +  ((KBFile) resource).getId().toString()+"\n");
									
								} catch (Exception e) {
									logger.error(e);
								}
							}
							
						} catch (Exception e) {
							logger.error(e);
						}
						
						logger.debug(resource.getTitle() + "  | id -> "  + resource.getId().toString());
				}
				else {
					logger.debug("resource is not KBFile" + resource!=null?resource.getTitle():"null");
				}
					
				
			}
		}else {
			logger.debug("content not instanceof ResourceContainer -> " + content!=null?content.getTitle():"null");
		}
	}

	@SuppressWarnings("unchecked")
	protected List<LogEvent> getAuditTrail(Content content) {
		List<LogEvent> auditTrail = (List<LogEvent>)getContentDao().getAuditTrail(content);
			if (auditTrail==null)
 				auditTrail = new ArrayList<LogEvent>();
		return auditTrail;
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	/**
	 * 
	 * ContentHomeDir es relativo (a Exportir)
	 * Si  la cantidad de content a exportar es mayor a SECTION_SIZE, 
	 * partir en n directorios de 2000 idocs cada uno
	 * 
	 * @param content
	 * @return
	 */
	protected String getContentHomeDir(Content content, int index) {
		
		if (this.isStandAlone()) 
			return "";
		
		String sufix = 	content.getId().toString() + "-" + content.getOId().toString() + "-" + content.getContentTemplate().getName().toLowerCase();
		String ret  =null;
		
		String section = getSection(content, index);
		
		if (section!=null)	
			ret =  section + File.separator + sufix;
		else
			ret = sufix;

		return ret;
	}


	protected String getSection(Content content, int index) {
		if (index>-1 || getSectionSize()==0) {
			double bucket = index / (Double.valueOf(getSectionSize()));
				return String.valueOf((Double.valueOf(Math.floor(bucket))).intValue()+1);
		} else		{
			return null;
		}
	}

	
	protected String getWorkingDir() {
		return this.working_dir;
	}


	protected BufferedWriter getGlobalLog() {
		return this.global_log;
	}
	
	
	private void init() throws IOException {

		start_time = System.currentTimeMillis();

		exported = 0;
		errors = 0;
		attachments_exported = 0;
		
		logger.info("Working dir: " + getWorkingDir());
		KbeeFileUtils.forceMkdir(new File(getWorkingDir()));
		
		logger.info("Export dir: " + getExportDir());
		KbeeFileUtils.forceMkdir(new File(getExportDir()));
		
		logger.info("Export Log dir: " + getExportLogDir());
		KbeeFileUtils.forceMkdir(new File(getExportLogDir()));
		
		if (!isStandAlone()) {
			String file_name = "global.log";
	
			File log = new File(getExportLogDir() + File.separator + file_name);
	
			global_log = new BufferedWriter(new FileWriter(log));
					
			getGlobalLog().write("Export Log\n");
			getGlobalLog().write("\n");
			
			getGlobalLog().write("Started\n");
			getGlobalLog().write(dateformat.format(new Date()) + "\n\n");
					
			if (getQueryStr()!=null) {
				getGlobalLog().write("Query\n");
				getGlobalLog().write(getQueryStr() + "\n");
			}
			
			getGlobalLog().write("\n");
			getGlobalLog().write("\n");
					
			getGlobalLog().write("     #");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write("Title");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write("Id");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write("OId");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write("Content Class"); 
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write("Duration");
			getGlobalLog().write("\n");
		}
		
		this.isInitialized = true; 
		
	}
	
	/**
	 * 
	 * @param size
	 * @return
	 */
	protected String formatFileSize(long size) {
		
		if (size==0) return String.format("%6d ", size).trim();
		if (size<KB) return String.format("%6d bytes", size).trim();
		if (size<MB) return String.format("%6.0f KB", (double) size / (double) KB).trim();

		else if (size<GB) {
			if (size<99*MB)	return String.format("%6.2f MB", (double) size / (double) MB).trim();
			else			return String.format("%6.0f MB", (double) size / (double) MB).trim();
		}
		else return String.format("%6.2f GB", (double) size / (double) GB).trim();	
	}


	protected KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).findUserById(this.uid);
	}
	
	protected String getDataExportDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "dataexport";
	}
	
	protected String getWorkDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath();
	}

}

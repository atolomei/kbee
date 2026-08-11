package com.novamens.kbee.content.document;


import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.TreeFileFactoryService;
import com.novamens.content.service.TreeFileService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;


/**
 * dir= local directory, it will include its contents but not the directory itself
 * domain=domain where to import
 */
public class ImportTreeIDocFileSystem extends AsyncCommand {
			
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(ImportTreeIDocFileSystem.class.getName());
	
	private ContentTemplate tidoc_template;

	private String BASE_DIR = PropertiesFactory.getInstance("kbee").getProperties().getProperty("treeidoc.import.dir", "C:\\file_tree_test" /*"C:\\Users\\atolo\\workspace-idoc-snapshot\\temp\\AA\\0594d130-b1eb-11e8-8f46-0050569415cd"*/);
	private String DOMAIN = PropertiesFactory.getInstance("kbee").getProperties().getProperty("treeidoc.import.domain", "windsor");

	/**
	 *  metrics must have the following objects:
	  
	   	put("total_dirs" 				, new AtomicInteger(0));
		put("total_files"	 			, new AtomicInteger(0));
		put("total_disk" 				, new AtomicLong(0));
		put("total_files_to_import" 	, new AtomicInteger(0));
		put("total_dirs_to_import" 		, new AtomicInteger(0));
		put("total_size_to_import" 		, new AtomicLong(0);
	 */
	
	private Map<String, Number> metrics = new ConcurrentHashMap<String, Number>(5, 0.9f, 1);

	private AtomicInteger total_dirs 			= new AtomicInteger(0);
	private AtomicInteger total_files	 		= new AtomicInteger(0);
	private AtomicLong	total_disk 				= new AtomicLong(0);
	private AtomicInteger total_files_to_import = new AtomicInteger(0);
	private AtomicInteger total_dirs_to_import 	= new AtomicInteger(0);
	private AtomicLong total_size_to_import 	= new AtomicLong(0);
	private AtomicBoolean initialized 			= new AtomicBoolean(false);
										
	
	ImportTreeIDocFileSystem() {
		setName("Import TreeIDoc from File System");
		setDescription("Import TreeIDoc and its TreeFile from local File System (dir=local directory, domain=domain name)");
	}
	
	/**
	 * 
	 */
	@Override
	protected void executeAsync() {
		
		try {

			metrics.put("dirs",  total_dirs);
			metrics.put("files", total_files);
			metrics.put("size", total_disk);
			metrics.put("total_files_to_import", total_files_to_import);
			metrics.put("total_dirs_to_import",  total_dirs_to_import);
			metrics.put("total_size_to_import",  total_size_to_import);

			this.initialized.getAndSet(true);
			
			/** open Hibernate Session */
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());
			setDateStarted(OffsetDateTime.now());
			setProgress(0.0);
			List<ContentTemplate> templates = getContentDao().getTemplates(getDomain());
			for(ContentTemplate template: templates) {
				if (template.getContentClass().getName().toLowerCase().startsWith("tree") ) {
					tidoc_template=template;
					break;
				}
			}
			if (this.tidoc_template==null) {
				kblogger.error(getDomain().getName() + " TreeIDoc not found");
				setResult("TreeIDoc not found");
				setResultDetails(getDomain().getName() + " TreeIDoc not found");
				setState(CommandState.ERROR);
				return;
			}
	
			String base=getBaseDirectory();
			importdir(base);
			setProgress(100.0);
			
		}	
		catch (Exception e) {
			kblogger.error(e);

			setState(CommandState.ERROR);
			setResult(e.getClass().getName());
			setResultDetails(e.getMessage());
			
		} finally {
			com.novamens.hibernate.session.Session.close();
			setDateTerminated(OffsetDateTime.now());
		}
	}
	
	@Override
	public double getProgress() {

			if(!initialized.get())
				return 0;
			
			AtomicInteger tf=(AtomicInteger)  metrics.get("total_files_to_import");		
			AtomicInteger td=(AtomicInteger)  metrics.get("total_dirs_to_import"); 
			AtomicInteger cf=(AtomicInteger)  metrics.get("dirs");
			AtomicInteger cd=(AtomicInteger)  metrics.get("files");
			
			if ((tf.get()+td.get())>0) {
				Double p=100.0*Double.valueOf(cf.get()+cd.get()) / Double.valueOf(tf.get()+td.get());
				return p.doubleValue();
			}
			else
				return 0.0;
	}
	
	/**
	 * Create
	 * Load
	 * Delete
	 */
	private void importdir(String basedir) {
		
		TreeFileDir tree_file_root = (TreeFileDir) ServiceLocator.getService(TreeFileFactoryService.class).createTreeFileDir();
		TreeIDoc tree_idoc 		= (TreeIDoc) ServiceLocator.getService(ContentFactoryService.class).create(tidoc_template.getName());
		
		if (tree_file_root==null)
			throw new ContentCreationException("TreeFile is null");
		
		if (tree_idoc==null)
			throw new ContentCreationException("TreeIDoc is null");
		
		File dir = new File(basedir);
		
		
		try {
			tree_file_root.getService(TreeFileService.class).addDirectory(dir, metrics);
			tree_idoc.setTreeFile(tree_file_root);
			tree_file_root.getService(TreeFileService.class).save();
			tree_idoc.getService(ContentService.class).update();
			
		} catch (Exception e) {
			kblogger.error(e);
		}
	}
	
	

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	private String getDomainName() {
		if (!getParameters().containsKey("domain"))
			return DOMAIN;
		return getParameters().get("domain").toString();
			
	}
	
	private String getBaseDirectory() {
		if (!getParameters().containsKey("dir"))
			return BASE_DIR;
		return getParameters().get("dir").toString();
			
	}
	
	private Domain domain = null;
	public Domain getDomain() {
		if (domain!=null)
			return domain;
		domain = getContentDao().findDomainByName(getDomainName());
		return domain;
	}


	
	/*
	private void test() {
		
		@SuppressWarnings("unchecked")
		List<KbeeTreeIDoc> list = (List<KbeeTreeIDoc>) getContentDao().getContent(KbeeTreeIDoc.class, getDomain().getId());
		
		for (TreeIDoc tree_idoc: list) {
			if (tree_idoc.getTreeFile()!=null) {
				kblogger.debug(tree_idoc.getTreeFile().getService(TreeFileService.class).toHTMLString());
			}
			
			
			TreeIDoc test_tree_idoc = getContentDao().findTreeIDocById(Long.valueOf(tree_idoc.getId().toString()));
			if (test_tree_idoc != null) {
				kblogger.debug("findTreeIDocById OK ["+test_tree_idoc.getId().toString()+"]");
			}
			else {
				kblogger.error("findTreeIDocById failed for id: " + tree_idoc.getId().toString());
			}
		
		}
		
	}
		*/

}

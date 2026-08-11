package com.novamens.kbee.content.treeidoc.test;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.TreeFileFactoryService;
import com.novamens.content.service.TreeFileService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.document.KbeeTreeIDoc;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class TreeIDocTestCommand extends AsyncCommand {
			
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(TreeIDocTestCommand.class.getName());
	
	private ContentTemplate tidoc_template;

	private String BASE_DIR = "C:\\file_tree_test";
	
	
	TreeIDocTestCommand() {
		setName("Test. TreeIDoc and TreeFile");
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	protected void executeAsync() {
		
		try {
			
			// open Hibernate Session
			//
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());
			
			setDateStarted(OffsetDateTime.now());
			setProgress(0);
			
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
	
			String base;
			if (getParameters().get("dir")!=null)
				base=getParameters().get("dir").toString();
			else
				base=BASE_DIR;
			
			create(base);
			
			load();
			
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

	
	/**
	 * Create
	 * Load
	 * Delete
	 */
	private void create(String basedir) {
		
		TreeFileDir tree_file_root = (TreeFileDir) ServiceLocator.getService(TreeFileFactoryService.class).createTreeFileDir();
		TreeIDoc 	tree_idoc 		= (TreeIDoc) ServiceLocator.getService(ContentFactoryService.class).create(tidoc_template.getName());
		
		if (tree_file_root==null)
			throw new ContentCreationException("TreeFile is null");
		
		if (tree_idoc==null)
			throw new ContentCreationException("TreeIDoc is null");
		
		
		kblogger.debug("TreeIDoc: "+ tree_idoc.toString());
		kblogger.debug("TreeFile: "+ tree_file_root.toString());
		
		File dir = new File(basedir);
		
		try {

			tree_file_root.getService(TreeFileService.class).addDirectory(dir);

			// print(tree_file_root, 0);
			
			tree_idoc.setTreeFile(tree_file_root);
			
			tree_file_root.getService(TreeFileService.class).update();
			tree_idoc.getService(ContentService.class).update();
			
 			kblogger.debug(tree_file_root.getService(TreeFileService.class).toHTMLString());
			
		} catch (Exception e) {
			kblogger.error(e);
		}
	}
	
	
	/**
	 * 
	 * Add Local dir
	 * All files (up to 20) inside the dir make the TreeFile 
	 */

	private void add() 	{
		
	}

	@SuppressWarnings("unchecked")
	private void load() {

//		List<KbeeTreeIDoc> list = (List<KbeeTreeIDoc>) getContentDao().getContent(KbeeTreeIDoc.class, getDomain().getId());
//		
//		for (TreeIDoc tree_idoc: list) {
//			if (tree_idoc.getTreeFile()!=null) {
//				kblogger.debug(tree_idoc.getTreeFile().getService(TreeFileService.class).toHTMLString());
//			}
//			
//			TreeIDoc test_tree_idoc = getContentDao().findTreeIDocById(Long.valueOf(tree_idoc.getId().toString()));
//			if (test_tree_idoc != null) {
//				kblogger.debug("findTreeIDocById OK ["+test_tree_idoc.getId().toString()+"]");
//			}
//			else {
//				kblogger.error("findTreeIDocById failed for id: " + tree_idoc.getId().toString());
//			}
//		}
	}

	
	/**
	 * @param tree_file
	 * @param son_number

	private void print(TreeFile tree_file, int son_number) {
		
		if (tree_file.getLevel()>0) 
			kblogger.debug("------------");
		
		kblogger.debug("[" + String.valueOf(tree_file.getLevel()) + "." + String.valueOf(son_number)+ "] - " + tree_file.getName());

		if (tree_file.getType().equals(TreeFile.FILE))
			kblogger.debug("File: " +  (tree_file.getFile()!=null?tree_file.getFile().getName():"null"));
		else {
			if (tree_file.getChildren()==null)
				kblogger.debug("Children: null");
			else
				kblogger.debug("Children: "+ String.valueOf(tree_file.getChildren().size()));
			
			int n= 0;
			for (TreeFile son: tree_file.getChildren()) 
				print(son,++n);
		}
	}
	 */
	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	
	private Domain domain = null;

	
	public Domain getDomain() {
		if (domain!=null)
			return domain;
		domain = getContentDao().findDomainByName("windsor");
		return domain;
	}

	
}

package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.novamens.content.base.ContentCreationException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.service.TreeFileFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.document.KbeeTreeFileDir;
import com.novamens.kbee.content.document.KbeeTreeFileKBFile;
import com.novamens.logging.TreeFileCreationEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeTreeFileFactoryService implements TreeFileFactoryService {
			
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTreeFileFactoryService.class.getName());
	

	public KbeeTreeFileFactoryService() {}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TreeFileDir createTreeFileDir() throws ContentCreationException {
		
		TreeFileDir tree_file = KbeeTreeFileDir.createRoot();
		
		tree_file.setCreationOffsetDateTime(OffsetDateTime.now());
		tree_file.setLastModifiedUser(getSessionUser());
		tree_file.setState(ObjectState.ENABLED);
		tree_file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		tree_file.setPosition(0);
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		tree_file.setDomain(userProfile.getDomain());

		try {
					getContentDao().saveTreeFile(tree_file);
					txlogger.info(new TreeFileCreationEvent(tree_file, "create"));
				}
				catch (Exception e) {
					logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():""));
					throw new ContentCreationException(e);
		}
		return tree_file;
	}
	
					
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TreeFileKBFile createTreeFileKBFile() throws ContentCreationException {
		
		TreeFileKBFile tree_file = KbeeTreeFileKBFile.createRoot();
		
		tree_file.setCreationOffsetDateTime(OffsetDateTime.now());
		tree_file.setLastModifiedUser(getSessionUser());
		tree_file.setState(ObjectState.ENABLED);
		tree_file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		tree_file.setPosition(0);
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		tree_file.setDomain(userProfile.getDomain());

		try {
					getContentDao().saveTreeFile(tree_file);
					txlogger.info(new TreeFileCreationEvent(tree_file, "create"));
				}
				catch (Exception e) {
					logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():""));
					throw new ContentCreationException(e);
		}
		return tree_file;
	}
	
	
	
	/*
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public TreeFile create(Class<? extends TreeFile> clazz) throws ContentCreationException {

		TreeFile tree_file = KbeeTreeFile.createRoot();
		tree_file.setCreationOffsetDateTime(OffsetDateTime.now());
		tree_file.setLastModifiedUser(getSessionUser());
		tree_file.setState(ObjectState.ENABLED);
		tree_file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		tree_file.setPosition(0);
		//tree_file.setType(TreeFile.DIRECTORY);
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		tree_file.setDomain(userProfile.getDomain());

		try {
					getContentDao().saveTreeFile(tree_file);
					txlogger.info(new TreeFileCreationEvent(tree_file, "create"));
				}
				catch (Exception e) {
					logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():""));
					throw new ContentCreationException(e);
		}
		return tree_file;
	}
	*/
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
			
		} catch (Exception e) {
			logger.error(e, "getSessionUser() error");
			return null;
		}
	}
	
	// Spring 
	//
	private ContentDao contentDao;
	public void setContentDao(ContentDao dao) 						{contentDao=dao;}
	public ContentDao getContentDao()							 	{return contentDao;} // return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");	

}

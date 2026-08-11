package com.novamens.kbee.content.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.commons.io.FilenameUtils;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.AuditResourceService;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbfs.FileServerException;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;


public class KbeeAuditResourceService implements AuditResourceService {
																				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeAuditResourceService.class.getName());

	private ContentDao contentDao = null; 

	@Override
	public KBFile putFile(File file, Domain domain) throws FileNotFoundException, FileServerException, ServiceNotFoundException {
		return upload(file, domain, getSessionUser());
	}

	@Override
	public KBFile getFile(Serializable id) {
		return (KBFile) getContentDao().findResourceById(KBFile.class, id);
		
	}

	@Override
	public KBFile putFile(File file)  throws FileNotFoundException, FileServerException, ServiceNotFoundException {
		return upload(file);
	}

	@Override
	public KBFile putFile(File file, User user, Domain domain) throws FileNotFoundException, FileServerException, ServiceNotFoundException  {
		return upload(file, domain, user);
	}

	
	public ContentDao getContentDao() {
		return contentDao;
	}
	
	public void setContentDao(ContentDao dao) {
		contentDao=dao;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}	
	
	
	
	
	private KBFile upload(File file) throws FileNotFoundException, FileServerException, ServiceNotFoundException  {							
		Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		return upload(file, domain, getSessionUser());
	}
	
	
	private KBFile upload(File file, Domain domain, User user) throws FileNotFoundException, FileServerException, ServiceNotFoundException  {
		
		
		
		String path = file.getName();
		
		//KB  FileImpl kbfile = new K BFileImpl();
		//kbfile.setName(path);
		// kbfile.set OId(ServiceLocator.getService(ContentFactoryService.class).getResour ceNewOId());

		
		KBFileImpl kbfile = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(path);
		
		kbfile.setDomain(domain);
		
		String title = FilenameUtils.getBaseName(path).replaceAll("(-|_)", " ");
		kbfile.setTitle(title);
		kbfile.setState(ObjectState.ENABLED);
		kbfile.setCreationOffsetDateTime(OffsetDateTime.now());
		kbfile.setLastModifiedUser(user);
		kbfile.setUploadOffsetDateTime(OffsetDateTime.now());

		// KBFS V1, V2 
		KBFSResourceService service = kbfile.getService(KBFSResourceService.class);

		BufferedInputStream stream = null;
		
		try {
			stream = new BufferedInputStream(new FileInputStream(file), 8192);
			service.putObject(file.getName(), stream);
			getContentDao().save(kbfile);
			return kbfile;
		} 
		catch (FileServerException | ServiceNotFoundException e) {
			logger.error(e);
			throw e;
		} 
		finally {
			if (stream!=null) {
				try {
					stream.close();
				}
				catch (IOException e) {
					logger.error(e);
					throw new FileServerException(e);
				}
			}
		} 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

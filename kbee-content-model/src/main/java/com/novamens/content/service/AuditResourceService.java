package com.novamens.content.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.kbfs.FileServerException;
import com.novamens.security.User;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.service.SystemService;

public interface AuditResourceService extends SystemService {

	public KBFile putFile(File file) throws FileNotFoundException, FileServerException, ServiceNotFoundException;
	public KBFile putFile(File file, Domain domain) throws FileNotFoundException, FileServerException, ServiceNotFoundException;
	public KBFile putFile(File file, User user, Domain domain) throws FileNotFoundException, FileServerException, ServiceNotFoundException;
	public KBFile getFile( Serializable id);
	
	
}

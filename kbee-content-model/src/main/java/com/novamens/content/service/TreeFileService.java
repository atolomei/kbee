package com.novamens.content.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.service.BusinessObjectService;

public interface TreeFileService extends BusinessObjectService {
	
	public void delete() 										throws ContentMgmtException;
	public void update() 										throws ContentMgmtException;
	public void save() 											throws ContentMgmtException;
	
	public String toHTMLString();
	
	
	/**
	 * This method adds all <b>contents</b> of local_directory
	 * local_directory is excluded.
	 */
	public void addDirectory(File local_directory, Map<String, Number> metrics) throws IOException, ContentMgmtException;
	public void addDirectory(File local_directory) 								throws ContentMgmtException, IOException;
	

}

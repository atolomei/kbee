package com.novamens.content.service;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.FactoryService;

public interface TreeFileFactoryService extends BusinessSystemService, FactoryService {

	TreeFileDir 	createTreeFileDir() throws ContentCreationException;
	TreeFileKBFile 	createTreeFileKBFile() throws ContentCreationException;
	
}

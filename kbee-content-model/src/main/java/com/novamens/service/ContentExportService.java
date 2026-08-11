package com.novamens.service;

import java.io.File;

import com.novamens.content.model.RelationTemplate;
import com.novamens.service.BusinessObjectService;

public interface ContentExportService extends BusinessObjectService {

	String ALL = "all";
	String SEARCHER = "searcher";
	
	public File getHTMLExport();

	
	/**
	 * ALL
	 * RESOURCES
	 *
	 * @param mode
	 * @return
	 */
	public File getHTMLExport(String mode);


	public File getResourcesExport();


	public File getPublicResourcesExport();
	public File getPrivateResourcesExport();
	
	public File getRelationshipExport(RelationTemplate object, String source_target);
	
}

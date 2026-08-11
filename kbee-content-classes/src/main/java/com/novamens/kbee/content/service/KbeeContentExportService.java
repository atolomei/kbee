package com.novamens.kbee.content.service;


import java.io.File;
import java.io.IOException;

import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.kbee.content.service.datamanagement.DirectoryZipper;
import com.novamens.kbee.content.service.datamanagement.KbeeHTMLExporter;
import com.novamens.kbee.content.service.datamanagement.KbeeRelationshipExporter;
import com.novamens.kbee.content.service.datamanagement.KbeeResourcesExporter;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeContentExportService implements ContentExportService {
			
	
	private Content content  = null;
	private String export_subtitle = "";
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentExportService.class.getName());
	
	private DMExporter exporter;
	
	private String zip_file_path;
	
	
	public KbeeContentExportService() {
	}

	public KbeeContentExportService(Content content) {
		 this.content = content;
	}

	@Override
	public File getHTMLExport() {
		return getHTMLExport(ALL);
	}
	
	
	@Override
	public File getRelationshipExport(RelationTemplate relation, String source_target) {
		
		String id = getSessionUser().getId().toString();
		
		String la= source_target.contentEquals("source") ? relation.getTargetLabel() : relation.getReverseLabel(); 
		
		this.export_subtitle = (la!=null? ("-"+la.toLowerCase().replace(" ", "").trim())  : "");
		
		this.exporter = new KbeeRelationshipExporter(id,relation, source_target);

		boolean isok =true;
		exporter.setStandAlone(true);
		
		try {

			exporter.start();
			exporter.export(getContent());
			
		} catch (IOException e) {
			
			isok = false;
			logger.error(e);
			return null;
		}
		finally {
			exporter.close();
		}
		
		if (isok) {
			compressExportedData();
			return new File(this.zip_file_path);
		}
		else
			return null;
	}


	@Override
	public File getPublicResourcesExport() {
		return getResourcesExport(true);
	}

	@Override
	public File getPrivateResourcesExport() {
		return getResourcesExport(false);
	}
	
	
	public File getResourcesExport(boolean is_public) {
		
		String id = getSessionUser().getId().toString();
		
		this.exporter = new KbeeResourcesExporter(id, is_public);
		
		this.export_subtitle = "-resources-"+(is_public?"public":"private"); 

		boolean isok =true;
		exporter.setStandAlone(true);
		
		try {

			exporter.start();
			exporter.export(getContent());
			
		} catch (IOException e) {
			
			isok = false;
			logger.error(e);
			return null;
		}
		finally {
			exporter.close();
		}
		
		if (isok) {
			compressExportedData();
			return new File(this.zip_file_path);
		}
		else
			return null;
	}
	
	
	/**
	 * "ALL" All files
	 * 
	 * 
	 * @param mode
	 * @return
	 */
	public File getHTMLExport(String mode) {
		
		String id = getSessionUser().getId().toString();
		
		this.exporter = new KbeeHTMLExporter(id);

		boolean isok =true;
		
		exporter.setStandAlone(true);
		
		try {

			exporter.start();
			exporter.export(getContent());
			
		} catch (IOException e) {
			
			isok = false;
			logger.error(e);
			return null;
		}
		finally {
			exporter.close();
		}
		
		if (isok) {
			compressExportedData();
			return new File(this.zip_file_path);
		}
		else
			return null;
	}

	
	
	protected String getExportSubtitle() {
		return this.export_subtitle;
	}
	/**
	 * 
	 * 
	 */
	protected void compressExportedData() {
	
	  String srcdir = getExporter().getExportDir();
	  String desdir = ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + getSessionUser().getUserName();

	  String title = getContent().getTitle()!=null?getContent().getTitle():"";
	  
	  if (title!=null && title.length()>0) {
		
		  	title = title.replaceAll("[\\s|\\r|:|\\/|\"|-]", "-");
			title = title.replace("á", "a");
		  	title = title.replace("á", "a");
			title = title.replace("é", "e");
			title = title.replace("í", "i");
			title = title.replace("ó", "o");
			title = title.replace("ú", "u");
			title = title.replace("ñ", "n");
			title = title.replace("Á", "A");
		  	title = title.replace("É", "E");
			title = title.replace("Í", "I");
			title = title.replace("Ó", "O");
			title = title.replace("Ú", "U");
			title = title.replace("Ñ", "N");
	  }
	  
	  String zipfile = (title.length()>0?(title+"-"):"") +  getContent().getOId().toString() + "-v" + String.valueOf(getContent().getVersion())  +  getExportSubtitle() + ".zip";	   
	  
	  this.zip_file_path = desdir + File.separator + zipfile;
		
	  DirectoryZipper zipper = new DirectoryZipper(new File(srcdir), new File(desdir), zipfile);
		
	  try {

		  zipper.execute();
		
		  logger.info("done");
		  
	  } catch (IOException e) {
		  logger.error(e);
	  }
	}

	

	private DMExporter getExporter() {
		return this.exporter;
	}
	
	
	
	private Content getContent() {
		return content;
	}

	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public File getResourcesExport() {
		return 	getPublicResourcesExport();
	}


	
}

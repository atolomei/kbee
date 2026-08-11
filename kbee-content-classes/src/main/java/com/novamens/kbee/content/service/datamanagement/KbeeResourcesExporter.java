package com.novamens.kbee.content.service.datamanagement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.util.KbeeFileUtils;

public class KbeeResourcesExporter extends KbeeBaseFileSystemExporter {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeResourcesExporter.class.getName());
	

	private boolean is_public = true;
			
	public KbeeResourcesExporter(Serializable uid, boolean is_public) {
		super(uid);
		 this.is_public= is_public;
	}
	
	
	public boolean isPublic() {
		return this.is_public;
	}
	
	public void setIsPublic(boolean b) {
		this.is_public=b;
	}
	
	@Override
	protected void exportResourceList(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportAttributes(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportAuditTrail(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportCustomTags(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportPrivateNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void exportText(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public void export(Content content) {
		export(content, -1);
	}
	
	
	@Override
	protected String getHomeResourcesDir(String home_dir) {
		return home_dir;
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public void export(Content content, int index) {
		
		if (!super.isInitialized())
			throw( new RuntimeException ("Exporter is not started."));
		
		try {

			long start = System.currentTimeMillis();
			
			String content_dir = getExportDir() + File.separator + getContentHomeDir(content, index);
			
			KbeeFileUtils.forceMkdir(new File(content_dir));
			
			if (this.is_public) {
				if (hasPermissionsResources(content))
					exportResources(content, this.is_public, index, content_dir);
			}
			else
				if (this.hasPermissionsPrivateNotes(content))
					exportResources(content, this.is_public, index, content_dir);
			
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

}

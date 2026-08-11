package com.novamens.kbee.content.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ResourceService;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;

public class KbeeResourceService implements ResourceService {
			
	private Resource resource;
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFileService.class.getName());

	public KbeeResourceService() {
	}
	
	public KbeeResourceService(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}
		
	@Override
	public File getThumbnailFile(ThumbnailSize size) throws IOException {
		
		Resource resource = getResource();
		
		String domain_name="";
		
		try {									
			ThumbnailService ths = ServiceLocator.getService(ThumbnailService.class);
			if (resource!=null) {
				domain_name=((AbstractResource)resource).getDomain().getName();
				File file = resource instanceof ExternalResource
					? 	ths.getThumbnailFile(resource.getId().toString(), domain_name, ((ExternalResource)resource).getUrl(), size)
					:	ths.getThumbnailFile(resource.getId().toString(), domain_name, ((KBFile)resource).getFile(), size);	
				return file;
			}
			throw new RuntimeException("File is null");
		} 
		catch (IOException e) {
			logger.debug(e);
			throw e;
		}
	}
	
	@Override
	public InputStream getThumbnail(ThumbnailSize size) throws IOException {
		try {
			return new FileInputStream(getThumbnailFile(size));
		} 
		catch (IOException e) {
			logger.debug(e);
			throw (e);
		}	
	}
}

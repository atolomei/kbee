package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// import test.com.novamens.kbee.content.scheduler.TestServiceRequest;


import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;

/**
 * 
 * @author atolomei
 *
 */
@Deprecated
public class ThumbailGeneratorRequest extends AbstractServiceRequest {

	static private Logger logger = LogManager.getLogger(ThumbailGeneratorRequest.class.getName());
	
	private static final long serialVersionUID = 6141966542615353570L;

	private File file;
	private String id;
	private ThumbnailSize size;
				
	// getThumbnailFile(String id, String domain, File srcfile, ThumbnailSize size)
	
	public ThumbailGeneratorRequest (String id, File file, ThumbnailSize size) {
		this.id=id;
		this.file = file;
		this.size = size;
		setName("Thumbnail " + file.getName());
	}
	
	@Override
	public void execute() {

		ThumbnailService thserver = ServiceLocator.getService(KbeeThumbnailService.class);
 	  	try {
 	  			thserver.getThumbnailFile(id, "test", file, size);
 	  	}
 	  	catch (IOException e) {
 	  		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
 	  	}
	}
}

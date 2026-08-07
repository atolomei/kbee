package com.novamens.kbee.url;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.kbee.thumbnail.KbeeThumbnailService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

/**
 * 
 *
 */
public class MSExcelExporter {
	
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MSExcelExporter.class.getName());
	
	private String working_dir;
	
	public File convertCSVtoExcel(File csv) {
		
		if (csv==null)
			return null;
		
		this.working_dir = ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() +  File.separator + "dataexport" + File.separator + "grid";
		
		String xls_name = FilenameUtils.getBaseName(csv.getName())+".xls";
		
		
		try {
			File dir = new File(working_dir);
			if (!dir.exists() || !dir.isDirectory())
				KbeeFileUtils.forceMkdir(new File(working_dir));
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
		
		
		
		BufferedWriter out = null;
		
		
		try {
			
			return null;
			
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
		}
		
		//File file = new File(private String working_dir+);
		// return file;

	}
}

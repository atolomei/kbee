package com.novamens.kbee.content.service.datamanagement;


import java.io.File;
import java.io.IOException;
 
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.util.KbeeFileUtils;


public class CleanUpExportDirRequest extends AbstractCronJobRequest {
	private static final long serialVersionUID = 1L;
			
	static private final long DURATION = 60 * 60 * 24 * 10 * 1000; // 10 days
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CleanUpExportDirRequest.class.getName());
	
	public CleanUpExportDirRequest() {
		setName("Clean up Export Dir");
	}
		
		@Override
		public void execute() {

			logger.debug("Starting Clean up Export Dir");
			
			String  working_dir = getDataExportDir();

			logger.debug("Dir: " + working_dir);
			
			final File[] dirContents = new File(working_dir).listFiles();
			
			if (dirContents!=null) {
				for (File file : dirContents) {
					try {
						if (file.isDirectory()) {
								long now = Calendar.getInstance().getTime().getTime();
								long thr = now - DURATION;
								Calendar cal =  Calendar.getInstance();
								cal.setTimeInMillis(thr);
								Date date_thr = cal.getTime();
								if (!FileUtils.isFileNewer(file, date_thr)) {
										try {
											logger.debug("Removing: " + file.getAbsolutePath());
											KbeeFileUtils.forceDelete(file);
										} catch (IOException e) {
											logger.error(e);
										}
								}
						}
					} catch (RuntimeException e) {
						logger.error(e);
					}
				}
			}
		
			logger.debug("done.");
		}

		
		public void setCronExpression(String expression) {
			super.setCronExpression(new CronExpressionJ8(expression));
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(super.toString());
			return str.toString();	
		}
}

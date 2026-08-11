package com.novamens.content.web.admin.markup.datamanagement;


import java.io.File;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;

import com.novamens.content.command.CommandState;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

import kbee.util.PropertiesFactory;

/**
 * 
 * 
 *
 */
public class RemoveOldExportsCommand extends AbstractCommand {
		

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RemoveOldExportsCommand.class.getName());
	
	private int days = 10;
	
	
	public void setPreserveDays(int days) {
		this.days=days;
	}
	
	@Override
	public void execute() {

		boolean is_error = false;

		int total = 0;
		int total_size = 0;

		try {
			
			setState(CommandState.RUNNING);
			setProgress(0);
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			File dir = new File(ServiceLocator.getService(ApplicationServerService.class).getDataExportDir());
			
			if (dir.exists() && dir.isDirectory()) {
				
				Instant threshold = Instant.now().minusSeconds(86400 * this.days); // 10 days
				
				
				OffsetDateTime date = OffsetDateTime.ofInstant(threshold,ZoneId.systemDefault());
	
				logger.debug("Removing files older than "+ DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date));
				
				final File[] dirContents = dir.listFiles();

				total_size = dirContents.length;
				
				if (total_size>0) {
					int counter = 0;
					for (File file: dirContents) {
						if (!isStopped()) {
							Instant mod = Instant.ofEpochMilli(file.lastModified());
							if (mod.isBefore(threshold)) {
								try {
									logger.debug("Removing dir: " + file.getAbsolutePath());
									KbeeFileUtils.forceDelete(file);
									total++;
								} catch (Exception e) {
									logger.error(e);
								}
								finally {
									setProgress(++counter/total_size);
								}
							}
						}
						else {
							break;
						}
					}
				}
				else {
					setProgress(100);
				}
			}
			else
				logger.debug("Directory: " + ServiceLocator.getService(ApplicationServerService.class).getDataExportDir() + " does not exist.");
			
			logger.debug("done (removed: " + String.valueOf(total) + ").");
		
		} catch (Exception e) {
			is_error=true;
			logger.error(e);
		}
		finally {
			
			if (isStopped()) {
				setState(CommandState.CANCELED);
			}
			else if (!is_error) {
				setState(CommandState.COMPLETED);
				setProgress(100);
			}
			else
				setState(CommandState.ERROR);
				setResult("Total deleted " + String.valueOf(total) + " / " + String.valueOf(total_size));
		}
	}

}

package com.novamens.kbee.bulkImport;

import com.novamens.content.command.CommandState;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.event.ProgressEvent;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import java.io.InputStream;
import java.time.OffsetDateTime;


/**
 * 
 * 
 *
 */
public class BulkImportCommand extends AsyncCommand {

	private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BulkImportCommand.class.getName());

    private ExcelBulkImporter excelBulkImporter;
    private KBFile fileToImport;
    private long totalItems = 0;
    private long totalItemsProcessed = 0;

    String username;

    public BulkImportCommand(ExcelBulkImporter excelBulkImporter, KBFile fileToImport) {
        setName(this.getClass().getSimpleName());
        setDescription("Bulk import a list of entities from an excel file.");
        this.excelBulkImporter = excelBulkImporter;
        this.fileToImport = fileToImport;
        username = ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser().getName();
    }

    @Override
    protected void executeAsync() {
    	
        setDateStarted(OffsetDateTime.now());
        try {
            com.novamens.hibernate.session.Session.open();
            ServiceLocator.getService(SecurityService.class).authenticate(username);
            try(InputStream inputStream = fileToImport.getInputStream()) {
                KBFile kbFile = excelBulkImporter.processFile(inputStream, new com.novamens.event.ProgressListener() {
					@Override
					public void progressUpdate(ProgressEvent evt) {
						totalItems = evt.getExpected();
						totalItemsProcessed = evt.getProgress();
					}
				});
                this.setResultFile(kbFile);
            }
            setProgress(100.0);
            logger.info("Command ended successfully.");
            setResultComments("Please, download and review the result file.");
            setState(CommandState.COMPLETED);
        }
        catch (Exception e){
            logger.error(e);
            setState(CommandState.ERROR);
            setResultComments(e.getClass().getSimpleName() + "| " + e.getMessage());
        }
        finally {
           setDateTerminated(OffsetDateTime.now());
           com.novamens.hibernate.session.Session.close();
        }
    }
    
    public long getTotalItems() {
        return totalItems == 0 ? excelBulkImporter.getTotalItems() : totalItems;
    }

    public long getTotalItemsProcessed() {
        return totalItemsProcessed;
    }
}

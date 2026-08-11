package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.service.ServiceLocator;

/**
 * 
 *  create index datasetmember_state_idx on datasetmember (state, lastmodifieddate desc);
 * <p> </p> 
 */
public class RemovedDeletedObjectsCommand extends AsyncCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RemovedDeletedObjectsCommand.class.getName());
	

	private int MAX_ITEMS_TO_PROCESS = 10000;
	private int total = 0;
	private int errors = 0;
	OffsetDateTime since = OffsetDateTime.now().minusDays(10);

	
	public RemovedDeletedObjectsCommand() { 
		setDescription("This is a Async Command that tries to wipe objects that were logically deleted from the system (Database)");
	}

	@Override
	protected void executeAsync() {
			
		try  {

			setDateStarted(OffsetDateTime.now());
			setState(CommandState.RUNNING);
			
			total = 0;
			errors = 0;

			processDataSetMembers();
			processUsers();
			
			setState(CommandState.COMPLETED);
			
			
		} catch (Exception e) {
			
			setState(CommandState.ERROR);
			logger.error(e);
			
		}
		finally {
			
			super.setDateTerminated(OffsetDateTime.now());
			
		}
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	
	private void processDataSetMembers() {

		logger.debug("Process DataSetMembers ");
		
		MAX_ITEMS_TO_PROCESS = 10;
		 
		List<DataSetMember> list = getContentDao().getDeletedDataSetMembers(since, MAX_ITEMS_TO_PROCESS);
		
		logger.debug("total -> " + String.valueOf(list));
		
		for (DataSetMember da: list) {
			try {
				
				logger.debug( (da.getStrValue()!=null?   da.getStrValue() :"null") +"  | id: " + String.valueOf(da.getId()));
				da.getService(DOMObjectService.class).delete();
				total++;
				
			} catch (Exception e) {
				errors++;
				logger.error(e);
			}
		}
	}
	
						
	private void processUsers() {
	}

}

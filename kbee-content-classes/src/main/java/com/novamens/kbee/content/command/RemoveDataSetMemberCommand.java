package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;

import com.novamens.content.service.DOMObjectService;
import com.novamens.hibernate.session.Session;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.util.KbeeRuntimeException;

public class RemoveDataSetMemberCommand extends AsyncCommand implements RemoveCommand {

	private DataSetMember modelobject;
	
	public RemoveDataSetMemberCommand(DataSetMember modelobject) {
		this.modelobject=modelobject;
		setDescription("This is a Async Command that tries to wipe ab object that was logically deleted from the system -> " + modelobject.getDisplayName());
	}

	@Override
	protected void executeAsync() {
			
		Transaction transaction = null;
		
		try  {

			Thread.sleep(3000);
			
			Session.open();
			
			setDateStarted(OffsetDateTime.now());
			setState(CommandState.RUNNING);

			if (modelobject==null)
				throw new KbeeRuntimeException("datasetmenber can not be null");

			getLogger().debug("Attempting to remove ->" + modelobject.getDisplayName());
	
			transaction = beginTransaction();
			modelobject.getService(DOMObjectService.class).delete();
			transaction.commit();
			
			setState(CommandState.COMPLETED);
			
		} catch (Exception e) {

			getLogger().error(e);
			
			setState(CommandState.ERROR);
			setResultDetails(e.getClass().getName() + ": " + e.getMessage());
			if(transaction != null)
				transaction.rollback();
			
			this.stop();
			
		}
		finally {
			super.setDateTerminated(OffsetDateTime.now());
			//com.novamens.hibernate.session.Session.close();
		}
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}

package com.novamens.aerolineas.content.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public class CheckValidityCommand extends AsyncCommand {
	
	static private Logger logger = LogManager.getLogger(CheckValidityCommand.class.getName());
	
	public CheckValidityCommand() {
		setName("Check Validity Command");
	}
	
	public void executeAsync() {
		Transaction transaction = null;
		try {
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@aerolineas");

			com.novamens.hibernate.session.Session.open();
			
			transaction = beginTransaction();
			
			IqlService iqlservice = getDomain().getService(IqlService.class);
			
			ResultSet resultSet = iqlservice.execute("ValidUntil_Before(TODAY) and not State(No Vigente) and not InternalState(Draft)");
			
			while (resultSet.hasNext()) {
				SearchResult result = resultSet.next(); 
				if (result.getObject() instanceof Content) {
					//Content content = (Content)result.getObject();
					// setdefeated((Content)result.getObject());
				}
			}
			
			transaction.commit();

			end();
		}	
		catch (Exception e) {
			logger.error("Close Command Error",  e);
//			resultDetails = e.getMessage();
			transaction.rollback();
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	@Override
	public long getTotalItemsProcessed() {
//		return totalevents;
		return 0;
	}
	
	@Override
	public String getResultDetails() {
//		return resultDetails;
		return null;
	}
	
	public boolean getValue() {
		String value = (String)getParameter("value");
		return "false".equals(value) ? false : true; 
	}
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	@Override
	public double getProgress() {
		double progress;
		progress = 0;
		return progress;
	}
	
	
}

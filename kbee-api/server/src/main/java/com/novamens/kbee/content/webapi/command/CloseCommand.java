package com.novamens.kbee.content.webapi.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.webapi.logging.ApiLogDao;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public class CloseCommand extends AsyncCommand {
	
	static private Logger logger = LogManager.getLogger(CloseCommand.class.getName());
	
	private long totalevents = 0;
	private String resultDetails = "";
	
	public CloseCommand() {
		setName("Close API Events Command");
	}
	
	public void executeAsync() {
		Transaction transaction = null;
		try {
			com.novamens.hibernate.session.Session.open();
			
			transaction = beginTransaction();
			
			totalevents = getLogDao().setClose((String)getParameter("criteria"), getValue());
			
			transaction.commit();

			end();
		}	
		catch (Exception e) {
			logger.error("Close Command Error",  e);
			resultDetails = e.getMessage();
			transaction.rollback();
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return totalevents;
	}
	
	@Override
	public String getResultDetails() {
		return resultDetails;
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

	private ApiLogDao getLogDao() {
		return (ApiLogDao) ServiceLocator.getService(BeansService.class).getBean("apiLogDao");	
	}
}

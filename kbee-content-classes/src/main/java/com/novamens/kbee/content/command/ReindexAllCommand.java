package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

/**
 * 
 * Reindex All:  
 * 
 *
 */
public class ReindexAllCommand extends AbstractCommand {
			
	private int total = 0, index = 0;

	private Logger logger = LogManager.getLogger(ReindexAllCommand.class.getName());

	
	public ReindexAllCommand() {
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + (getDescription()!=null ? (" | " + getDescription()) : "");
	}
	
	
	public long getTotalItems() {
		return total;
	}
	
	public long getTotalItemsProcessed() {
		return index;
	}

	
	@Override
	public void execute() {
		
		setDateStarted(OffsetDateTime.now());
		
		List<String> statements = new ArrayList<String>();
		
		//setLogger(getLoggerName());
		
 		try {
					
			// Domain
			//
			statements.add("from KbeeDomain"); 
			
			// Security
			//
			statements.add("from KbeeGroup"); // 1
			statements.add("from KbeeUser");
			statements.add("from KbeeSecurityRule"); // 3
			statements.add("from KbeeENotiRule");
			
			// Templates
			//
			statements.add("from KbeeEmailTemplate");  // 5
			
			// model 
			//
			statements.add("from KbeeDataSet");
			statements.add("from KbeeClassifier"); 
			statements.add("from KbeeAttribute");
			statements.add("from KbeeContentTemplate");
		
			
			// members
			//
			statements.add("from KbeeDataSetMember"); // 10
			statements.add("from KbeePerson"); // 11
			statements.add("from KbeeUserLabel"); 

			// Content
			//								
			statements.add("from KbeeBillboard"); // 12;
			statements.add("from KbeeIDoc");
			statements.add("from KbeeOrganizationalText");
			
			
								
			statements.add("from KbeeSite");
			statements.add("from KbeePage");
			statements.add("from KbeeArea");
			statements.add("from KbeeBlock");
			statements.add("from KbeeViewBK");
			statements.add("from KbeeViewDetailContent");
			

			// Log
			//
			// statements.add("from ObjectEvent");
			// statements.add("from SendEmailEvent"); // 15
				
			
			total = getTotalObjects(statements);
			index = 0;

			if (total<1)
				total=1;
			
			final Double dotal = Double.valueOf(total);
			
			logger.info("total objects to index "+ String.valueOf(total));
			info("total objects to index "+ String.valueOf(total));
			
			for (String stm: statements) {
				try {
			
					logger.info("Starting "+ stm);
					info("Starting "+ stm);
					ReindexCommand reindexcommand = new ReindexCommand(stm, getDomainKbee()) {
						public void onIndex(Object object) {
							ReindexAllCommand.this.setProgress(Double.valueOf(index++) * 100.00 / dotal);
						}
					};
					
					reindexcommand.execute();

				} 
				catch (Exception e) {
					
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					
					
					error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					error(e.getStackTrace());
				}
			}
		} 
		finally {
			setDateTerminated(OffsetDateTime.now());
			
			if (!isStopped()) {
				setProgress(100.00);
				setResult("Ok");
				setState(CommandState.COMPLETED);
			}
			else {
				setResult("Cancelled by user.");
				setState(CommandState.CANCELED);
			}
		}
	}

	private Domain getDomainKbee() {
		return getContentDao().findDomainByName ("kbee");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private int getTotalObjects(List<String> statements) {
		int total = 0;
		for (String stm: statements) {
			try {
				info("Calculating total for "+ stm);
				ReindexCommand reindexcommand = new ReindexCommand(stm, getDomainKbee());
				total += reindexcommand.getNumbersOfObjectsToIndex();
			} 
			catch (Exception e) {
				
				
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				
				
				error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				error(e.getStackTrace());
			}
		}
		return total;
	}
	
//	private String getLoggerName() {
//		String name = "logs/reindex-all-";
//		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
//		name += format.format(new Date());
//		name += "-" + String.valueOf(getId()) + ".log";
//		return name;
//	}
}

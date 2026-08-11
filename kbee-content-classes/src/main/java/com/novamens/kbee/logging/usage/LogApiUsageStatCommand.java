package com.novamens.kbee.logging.usage;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 *
 */			
public class LogApiUsageStatCommand extends AbstractCommand {
			
	static Logger logger = LogManager.getLogger(LogApiUsageStatCommand.class.getName());

	private DateTimeFormatter df = DateTimeFormatter.ofPattern ( "yyyy-MM-dd", 	Locale.ENGLISH);
	
	private boolean is_trx = false;
	
	 public LogApiUsageStatCommand() {
		 setExactlyOneSemantics(true);
	 }

	 
	 public LogApiUsageStatCommand(String from, String to) {
		getParameters().put("from", from);
		getParameters().put("to", to);
	 }
	 
	 
	/**
	 * 
	 * CommandBean -> RequestCommand -> Scheduler TRX
	 * RequestCronJob -> Scheduler TRX
	 * 
	 * From UI -> must include trx here
	 * 
	 * @param b
	 */
	public void setTrx(boolean b) {
		this.is_trx=b;
	}
	
	public boolean isTrx() {
		return this.is_trx;
	}
	
	/**
	 * 
	 * This must be used from a Spring Bean
	 * For Scheduler based log: {@link LogApiUsageServiceRequest}
	 * 
	 */
	@Override
	public void execute() {
			   
		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		try {
					
					UsageStatService service = (UsageStatService) ServiceLocator.getService(BeansService.class).getBean("usageStatService");
					
					String from = null;
					String to = null;
					
					if (getParameters()!=null) {
						from = (String) getParameters().get("from");
						to = (String) getParameters().get("to");
					}
					
					ZoneId zone = null;
					zone=ZoneId.systemDefault();
					
 					if (from==null)
 						from =  df.format(ZonedDateTime.ofInstant(OffsetDateTime.now().minusMonths(12).toInstant(), zone));

 					if (to==null)
 						to =  df.format(ZonedDateTime.ofInstant(OffsetDateTime.now().minusHours(1).toInstant(), zone)); 
 					
 					if (isTrx())
 						service.saveApiUsage(from, to);
 					else
 						service.nonTrxSaveApiUsage(from, to);  // trx is from the scheduler
 					
 					setProgress(100);
 					setResult("OK");
 					setState(CommandState.COMPLETED);
 					
 					
	 	} catch (Exception e) {
	 		
	 				setState(CommandState.ERROR);
	 				setResult(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());

					//  
					// Database transactions that fail must propagate the exception
					// for the Scheduler to rollback and mark the request
					// 
					throw(e);
		}
		finally {
			
				setDateTerminated(OffsetDateTime.now());
		}
 	}		 
}

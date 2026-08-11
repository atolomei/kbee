package com.novamens.kbee.logging.usage;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.scheduler.AbstractCronJobRequest;

/**
 * 
 *  select TS "DateTime", TOTAL "Requests", MEAN_TIME_TOTAL  "Mean time (ms)", 
 *  TOTAL_POST   "POST", MEAN_TIME_POST   "POST  mean time (ms)", 
 *  TOTAL_DELETE "DELETE", MEAN_TIME_DELETE     "DELETE mean time (ms)", 
 *  TOTAL_BOUNCED   "Total Bounced (Err. 429)" from kb_api_usage_stat order by ts;
 * 
 */
public class LogApiUsageServiceRequest extends AbstractCronJobRequest {
	private static final long serialVersionUID = 1L;
											
	static Logger logger = LogManager.getLogger(LogApiUsageServiceRequest.class.getName());

	public LogApiUsageServiceRequest() {
 		setName("Log API daily usage");
 		setDescription("Logs API daily usage");
 		
 	}

 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 * 
 	 * 2018-07-31 13:58:56.618-04
 	 * 
 	 * 7.21
 	 * 
   	 */
	@Override
	public void execute() {
		try {
			String from = null;
			String to = null;
					
			if (getParameters()!=null) {
				from = (String) getParameters().get("from");
				to = (String) getParameters().get("to");
			}
					
			ZoneId zone = null;
			zone=ZoneId.systemDefault();
					
			DateTimeFormatter df = DateTimeFormatter.ofPattern ( "yyyy-MM-dd", 	Locale.ENGLISH);
					
			if (from==null)
				from =  df.format(ZonedDateTime.ofInstant(OffsetDateTime.now().minusHours(24).toInstant(), zone));

			if (to==null)
				to =  df.format(ZonedDateTime.ofInstant(OffsetDateTime.now().minusHours(1).toInstant(), zone));
				LogApiUsageStatCommand command = new LogApiUsageStatCommand(from, to);
 				command.execute(); // trx scheduler
		} 
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
			// Database transactions that fail must propagate the exception
			// for the Scheduler to rollback and mark the request
			throw(e);
		}
 	}
 }

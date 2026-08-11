package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.Map;

import com.novamens.content.command.CommandState;
import com.novamens.kbee.scheduler.TestServiceRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.TransactionService;


/**
 * Parameters:
 * 
   rate  			= number of Request per second to send (50 req/sec) 
   total_to_send 	= total Requests to send (12000)
   prob_hp 			= proportion of HP Requests (0.65) 
 
 */
public class TestSchedulerLoadCommand extends AsyncCommand {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TestSchedulerLoadCommand.class.getName());

	private Double rate = 30.0;  // req/second  

	private long TOTAL_TO_SEND=60000;		
	private Double prob_hp = 0.6;

	long E_START;
	long TOTAL_DURATION = 5*60*1000; // ms 5 minutes
	long actual_duration = 0;
	private long total_sent=0;
	
	
	
	public TestSchedulerLoadCommand() {
		setName("TestSchedulerLoadCommand");
	}

	public TestSchedulerLoadCommand(Map<String, Object> param) {
		super(param);
		setName("TestSchedulerLoadCommand");
		
	}
	
	
	
	
	@Override
	public long getTotalItems() {
		return TOTAL_TO_SEND;
	}

	
	public long getTotalItemsProcessed() {
		return total_sent;
	}
	
	protected com.novamens.transaction.Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}

	
	/**
	 * 30 min is the longest allowed
	 * 
	 * @return
	 */
	private boolean run_too_long() {
		return (System.currentTimeMillis() - E_START) > 1000 * 60 * 30; // 30 min 
	}
	

	
	@Override
	protected void executeAsync() {
		
		
		
		
		E_START = System.currentTimeMillis();
		long start = System.currentTimeMillis();
		
		
		com.novamens.transaction.Transaction transaction = null;
		
		try  {
	
			initTest();
			
			com.novamens.hibernate.session.Session.open();

			// Authenticate
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");  
			
			SchedulerService service = ServiceLocator.getService(SchedulerService.class);

			
			long remaining=0;
			long second_number = 0;
			
			
			actual_duration = 0;
			total_sent=0;
			
			
			while ( (total_sent < TOTAL_TO_SEND) && (actual_duration < TOTAL_DURATION)) {

				try {
				
					if (isStopped()) 
					return;
					
						setProgress(100.0 * new Double(total_sent) / new Double(TOTAL_TO_SEND));
						
						double d_total_this_second = Math.random()*2.0*rate;
						
						long i_total_this_second = Math.round(d_total_this_second) + remaining;
						long sent_this_second = 0;
						boolean is_ok = (d_total_this_second > 0);
						
						if (i_total_this_second>0) {
								
								transaction = beginTransaction();
								
								try {
									long sleep_time = Math.round(new Double(1000.0 / new Double(i_total_this_second).doubleValue()).doubleValue());
									long duration = 0;
									
									start = System.currentTimeMillis();
									
									while (sent_this_second < d_total_this_second && duration <= 1000) {
										try {
												TestServiceRequest t=new TestServiceRequest("Test Scheduler load " + String.valueOf(total_sent));
												t.setPriority( (Math.random()<=prob_hp? SchedulerService.HIGH_PRIORITY: SchedulerService.LOW_PRIORITY));
												service.enqueue(t);
												sent_this_second++;
												total_sent++;
												
												try {Thread.sleep(sleep_time);} catch (InterruptedException e) {}
												
											} catch (SchedulerException e) {
												logger.error(e);
												is_ok = false;
											}
											
											duration = System.currentTimeMillis()-start;
										}
										logger.debug( String.valueOf("sec_"+ String.valueOf(second_number++).trim() + " [" +  String.valueOf(sent_this_second) +  "]  - Total: " + String.valueOf(total_sent)));
										if (sent_this_second<d_total_this_second)
											remaining = i_total_this_second-sent_this_second;
										else {
											sleep_time = 1000 - (System.currentTimeMillis() - start);
											if (sleep_time>0) {
												try {Thread.sleep(sleep_time);} catch (InterruptedException e) {}
											}
										}
			
								} catch (Throwable e) {
									is_ok = false;							
								}
								finally {
									if (transaction!=null) {
										if (is_ok)
											transaction.commit();
										else
											transaction.rollback();
									}
								}
						}
							else {
								try {Thread.sleep(900);} catch (InterruptedException e) {}
							}
						
					
						} finally {
							actual_duration = System.currentTimeMillis() - E_START;
							
							if (run_too_long()) 
								return;
						}
					
					}
				
					logger.debug("Total Sent      -> " + String.valueOf(total_sent));
					logger.debug("Total Duration  -> " + String.valueOf(actual_duration/1000.0) +" secs");
					
					
					if (actual_duration>0) 
						logger.debug("Rate      -> " + String.valueOf(total_sent/ (actual_duration/1000.0)) + " req/sec");
					
					
					setProgress(100);
					setResult("OK");
					setState(CommandState.COMPLETED);
				
				}
				catch (Exception e) {
					setState(CommandState.ERROR);
					setResultComments(e.getClass().getSimpleName() + " | " + e.getMessage());
					logger.error(e);
		}
		finally {

			com.novamens.hibernate.session.Session.close();	
			
			String ss;
			if (actual_duration>0) 
				ss="| Rate      -> " + String.valueOf(total_sent/ (actual_duration/1000.0)) + " req/sec";
			else
				ss= "| Rate n/a";
			
			setResultComments("Total sent "+ String.valueOf(this.total_sent)+" | " + "Total Duration  -> " + String.valueOf(actual_duration/1000.0) +" secs" + ss);
			setDateTerminated(OffsetDateTime.now());
			
			long end = System.currentTimeMillis();
			logger.debug("Duration " + String.valueOf(end-start) + " ms");

		}
	}
	
	


	private void initTest() {


		if (getParameters()==null)
			return;

		
		if (getParameters().get("duration_secs")!=null) {
			String s_dur=getParameters().get("duration_secs").toString();
			try {
					TOTAL_DURATION=Long.valueOf(s_dur).longValue() * 1000;
			} catch (Exception e) {
					TOTAL_DURATION=1000*60*5;
				}
			}
		
		// req/sec
		String srate= (String) getParameter("rate");
		if (srate!=null) {
			try { 
				rate=Double.valueOf(srate.trim().replace("\r", ""));
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		

		// total to send
		String stotal = (String) getParameter("total");
		if (stotal!=null) {
			try { 
				TOTAL_TO_SEND=Long.valueOf(stotal.trim().replace("\r", ""));
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		
				
		// proportion of hp
		String sprob_hp= (String) getParameter("prob_hp");
		if (sprob_hp!=null) {
			try { 
				prob_hp=Double.valueOf(sprob_hp.trim().replace("\r", ""));
			}
			catch (Exception e) {
				logger.error(e);
			}
		}

		
		



		
	}



	
	

	

	
	
	
	
	
	
	


}

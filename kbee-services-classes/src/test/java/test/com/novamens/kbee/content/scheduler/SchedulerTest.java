package test.com.novamens.kbee.content.scheduler;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.novamens.kbee.scheduler.TestServiceRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public class SchedulerTest {
	
	private Logger logger = LogManager.getLogger(this.getClass().getName());
	
	@SuppressWarnings("unused")
	private int total = 0;
	
	
	@BeforeAll
	public void setUp() throws Exception {
		ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
	}
	
	@AfterAll
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() {
		testScheduler();
	}


	public void testScheduler() {
			long start = System.currentTimeMillis();
			long DURATION = 1000 * 60 * 2; // minutos
			long lapse_between_display = 1000 * 2;
			long last_display = start;
			long now;
			boolean bend = false;
			double tasks_per_second = 0.5;
			double wait_time;
			while (!bend) {
				try {
					add(1);
					wait_time = 1000.0 * (1.0 / tasks_per_second);  
					Thread.sleep((Double.valueOf(wait_time)).longValue());
					now  = System.currentTimeMillis();
					if (now-last_display>lapse_between_display) {
						last_display = now;
						showSchedulerStats();
					}
					bend = ((System.currentTimeMillis()-start)>DURATION);
					
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
			
			now=System.currentTimeMillis();
			// System.out.println("DONE: duration: " + (now-start)/1000.0 + " secs.");
	}
	
	/**
	 * 
	 */
	private void showSchedulerStats() {
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);

		try {

			logger.info("QueueSize " + service.getQueueSize());
			logger.info("ErrorQueueSize " + service.getErrorQueueSize());
		
		} catch (SchedulerException e) {
			logger.error(e);
		}
	}

	/**
	 * @param n
	 */
	@SuppressWarnings("unused")
	private void add(int n) {
		try{
			Transaction transaction =  ServiceLocator.getService(TransactionService.class).beginTransaction();
			Random random = new Random();
			long to,t1;
			to=System.currentTimeMillis();
			for (int i=0; i<n; i++) {
				
				TestServiceRequest request = new TestServiceRequest("Request-" + String.valueOf(System.currentTimeMillis()));
				
				int dice = random.nextInt(100);
				int prio = (dice>30?1:2);
				
				// 70% priority 1. Index file 
				// 30% priority 2. send email 
				
				request.setPriority(prio); 
				request.setCost(prio==1? SchedulerService.STANDARD_PROCESSING_COST : SchedulerService.STANDARD_PROCESSING_COST * 5);
				logger.info("Adding request " + request.getName());
				ServiceLocator.getService(SchedulerService.class).enqueue(request);
				total++;
			}
			transaction.commit();
		
		}	catch(SchedulerException e) {
			logger.error(e);
				throw new RuntimeException(e);
		}
	}
	
	
	/**
	 *  Sends 100 photos to the thumbnail server via the Scheduler
	 *   

	public void testThumbnail() {

		String srcdir = "C:\\Users\\atolomei\\Dropbox\\Camera Uploads";
		int max = 100;
		
		File dir = new File(srcdir);
		File file;
		
		SchedulerService scheduler 	= ServiceLocator.getService(SchedulerService.class);
			
  		logger.info("Starting to add Thumbnail Request into Scheduler... ");
 	  		
 	  	int total=0;
 	  	int errors=0;
 	  			
 	  	File[] listOfFiles = dir.listFiles();
 	  			
 	  	for (int n = 0; n<listOfFiles.length && n<max; n++) {
 	  		file = listOfFiles[n];
  			if (!file.isDirectory()) {
  				String id =String.valueOf(Math.abs(file.getAbsolutePath().hashCode()));   				// unique id of the file
			 	try {
			 		ThumbailGeneratorRequest request = new ThumbailGeneratorRequest(id, file, ThumbnailSize.LARGE);
			 		request.setPriority(SchedulerService.HIGH_PRIORITY);
			 		logger.info("enqueue: " + file.getName() + "  [ " + (total+1)+" ]");
			 		scheduler.enqueue(request);
			 		total++;
 				 	  		
			 	} catch (IOException e) {
			 		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			 		errors++;
 				 	}
  				}
 	  	}
 	  	logger.info(" total added: " + total  + "    errors: " + errors);
 	  	
 	  	int n=0;
 	  	while (n++<60) { 
 	  		try {
 	  				logger.info("Working the queue. size: " + scheduler.getQueueSize() +  "  Buffer: " +  scheduler.getBufferSize());
				
 	  		} catch (IOException e) {
 	  			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
 	  		}
 	  		try {
				Thread.sleep(3000);
 	  		} catch (InterruptedException e) {
			}
 	  	}
	}
	
	 */

	
}

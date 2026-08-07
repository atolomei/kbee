package com.novamens.kbee.scheduler;



import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;

public class TestServiceRequest extends AbstractServiceRequest   {
	
	private static final long serialVersionUID = -6374210602342749003L;
										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TestServiceRequest.class.getName());
	
	private int pause;
	private volatile AtomicBoolean is_stopped = new AtomicBoolean(false);
	
	
	public TestServiceRequest() {
		this(TestServiceRequest.class.getSimpleName());
	}
	
	
	public TestServiceRequest(String name) {
		setName(name);
		
		Random random = new Random();
		setCost(SchedulerService.STANDARD_PROCESSING_COST);
		setPriority(random.nextInt(10) > 4 ? SchedulerService.LOW_PRIORITY : SchedulerService.HIGH_PRIORITY);
		int p = getPriority();		
		if (p==1) 
			pause = 100 + random.nextInt(10)*20;
		else
			pause = 200  + random.nextInt(10)*35;
		
		setDescription ( "Sleep " + String.valueOf(pause)+" ms");
	}
	
	
	
	
	@Override
	public void execute() {
		
		synchronized (Thread.currentThread()) {
			try {
				Thread.sleep(pause);
				logger.debug("done -> " + String.valueOf(pause)+" ms ");
			}
				catch (InterruptedException e) {
					this.is_stopped.set(true);
			}
			
			if (isStopped()) 
				return;
		}
	}
	
	@Override
	public void stop() {
		logger.debug("Received order to Stop.");
		this.is_stopped.set(true);
	}

	protected boolean isStopped() {
		return this.is_stopped.get();
	}
	
	

}

package com.novamens.kbee.content.webapi.traffic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.novamens.event.Event;
import com.novamens.event.EventListener;

import org.apache.commons.lang3.StringUtils;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

import kbee.api.service.TimeoutException;

public class KbeeTrafficControlService implements TrafficControlService, EventListener {
			
	private Set<TrafficPass> passes = null;
	private long waittimeout = 10000L; 

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTrafficControlService.class.getName());

    
	public KbeeTrafficControlService() {
	}
	
	public KbeeTrafficControlService(int numberofpasses) {
		passes = Collections.synchronizedSet(new HashSet<TrafficPass>());
		for (int p=0; p<numberofpasses; p++) 
			passes.add(new KbeeTrafficPass());
	}
	
	public TrafficPass getPass() {
		
		TrafficPass pass = null;
		
		if (passes==null) {
			synchronized (this) {
				if (passes==null) {
					createPasses();
				}	
			}	
		}
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		long initialtime = System.currentTimeMillis();
		long wait = 0;
		boolean inqueue = false;
		
		try {
			while(pass==null) {
				
				synchronized (this) {
					
					if (!passes.isEmpty()) {
						pass = passes.iterator().next();
						passes.remove(pass);
					}
				}

				if (pass == null) {
					wait = System.currentTimeMillis() - initialtime;
					if (wait > waittimeout) {
						logger.error("TimeoutException  | passes = "+ String.valueOf(passes));
						throw new TimeoutException();
					}
					
					synchronized (this) {
						try {
							if (!inqueue) {
								metrics_service.getMeterAPITrafficeQueueIn().mark();
								metrics_service.getCounterTrafficQueueSize().inc();
								inqueue = true;
							}
							wait(1000);
						}
						catch(InterruptedException e) {
						}
					}
				}
			}
		}
		finally {
			if (inqueue) {
				wait = System.currentTimeMillis() - initialtime;
				metrics_service.getCounterTrafficQueueSize().dec();
				metrics_service.getMeterAPITrafficeQueueOut().mark();
				metrics_service.getTrafficInQueueEstimator().addValue(wait);
			}
		}
		return pass;
	}
	
	public void release(TrafficPass pass) {
		synchronized (this) {
			passes.add(pass);
			notify();
		}
	}
	
	public void setTimeout(long value) {
		waittimeout = value;
	}
	
	protected synchronized void createPasses() {
		
		int numberofpasses = TrafficControlService.DEFAULT_TOKENS;
		
		String tokens = SystemParameters.get("com.novamens.content.webapi.traffictokens", String.valueOf(numberofpasses));
		logger.debug("com.novamens.content.webapi.traffictokens -> " + tokens);
		if (StringUtils.isNumeric(tokens)) {
			numberofpasses = Integer.valueOf(tokens);
		}
		passes = Collections.synchronizedSet(new HashSet<TrafficPass>());
		for (int p=0; p<numberofpasses; p++) {
			passes.add(new KbeeTrafficPass());
		}
	}
	

    @Override
    public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug("Evict Cache Received");
        createPasses();
    }
    
    
}

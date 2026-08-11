package com.novamens.kbee.content.distribution;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

public class CheckinListener implements EventListener {
																					
	static private Logger logger = new Logger(CheckinListener.class.getName());
	
	public boolean listen(Event event) {
		return ((event instanceof AppCheckinEvent) && event.getObject() instanceof IDoc);
	}
	
	public void onEvent(Event event) {
		distribute((Content)event.getObject());
	}
	
	private void distribute(Content content) {
		
		try {
			if (getDistributionClassifier(content)!=null)
				ServiceLocator.getService(SchedulerService.class).enqueue(new DistributionServiceRequest(content));
			
		}
		catch(Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	public Classifier getDistributionClassifier(Content content) {
		if ( content ==null)
			return null;
		for (Classification clasi:  content.getClassification()) {
			if (clasi!=null && clasi.getClassifier().isDistribution()) 
					return clasi.getClassifier();
		}
		return null;
	}
}
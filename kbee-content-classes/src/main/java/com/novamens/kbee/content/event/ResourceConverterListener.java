package com.novamens.kbee.content.event;


import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.ServiceLocator;


public class ResourceConverterListener implements EventListener {
			
	String beanname = null;
																										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceConverterListener.class.getName());

	
	public boolean listen(Event event) {
		return (event instanceof AppCheckinEvent) && event.getObject() instanceof Content && event.getObject() instanceof ResourceContainer;
	}
	
	public void onEvent(Event event) {
		try {
			if (!((ResourceContainer)event.getObject()).getResources().isEmpty()) {
				ServiceRequest req = getRequest((Content)event.getObject());
				if (req!=null)
					ServiceLocator.getService(SchedulerService.class).enqueue(req);
				else
					logger.error("Request for bean " + getBeanName() + " is null");
			}
		}
		catch (SchedulerException e) {
			logger.error(e);
			throw new RuntimeException();
		}
	}
	
	public void setBeanName(String name) {
		beanname = name;
	}
	
	public String getBeanName() {
		return beanname;
	}
	
	
	/**
	 * 
	 * 
	 * @param content
	 * @return
	 */
	protected ServiceRequest getRequest(Content content) {
		return (ServiceRequest)ServiceLocator.getService(BeansService.class).getBean(getBeanName(), content);
	}
}

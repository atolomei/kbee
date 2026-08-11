package com.novamens.kbee.content.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.LogEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

public class IndexerListener implements EventListener {
	
	protected static final Log logger = LogFactory.getLog(IndexerListener.class);
	
	public boolean listen(Event event) {
		return event.getObject() instanceof Indexable; 
	}
	
	public void onEvent(Event event) {
		try {
			Object object = event.getObject();
			Domain domain = object instanceof DomainObject ? ((DomainObject)object).getDomain() : getDomain();
			if (domain==null) {
				logger.error("null domain for " + object.getClass().getName());
				return;
			}
			if (!(object instanceof LogEvent)) {
				JavaIndexerService indexer = object instanceof LogEvent ? domain.getService(LogIndexerService.class) : domain.getService(JavaIndexerService.class);
				if ((event instanceof AppUpdateEvent || event instanceof AppCreateEvent) && ((Identifiable)event.getObject()).getId()!=null) {
					if (indexer!=null) indexer.index(event.getObject());
				}
				else 
				if (event instanceof AppDeleteEvent) {
					if (indexer!=null) indexer.delete(event.getObject());
				}
			}
		}
		catch (IndexerException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		catch (NullPointerException e) {
			logger.error("null domain for " + event.getObject().toString());
		}
	}
	
	public JavaIndexerService getIndexer() {
		Domain domain = getDomain();
		if (domain!=null)
			return getDomain().getService(JavaIndexerService.class);
		else
			return null;
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (profile!=null) 
			return profile.getDomain();
		else
			return null;
	}
}

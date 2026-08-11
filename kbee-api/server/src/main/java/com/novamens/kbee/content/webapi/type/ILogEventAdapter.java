package com.novamens.kbee.content.webapi.type;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.logging.AbstractObjectEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.ILogEvent;

public class ILogEventAdapter implements Adapter<LogEvent, ILogEvent> {
	
	public ILogEventAdapter() {
	}
	
	public ILogEvent adapt(LogEvent event) {
		ILogEvent ievent = new ILogEvent();
		AbstractObjectEvent oevent = (AbstractObjectEvent)event;
		ievent.setId(String.valueOf(oevent.getId()));
		ievent.setDisplayName(oevent.getTitle());
		ievent.setType(oevent.getEventType());
		if (oevent instanceof ContentEvent) {
			ievent.setVersion(((ContentEvent)oevent).getVersion());
		}
		ievent.setParameters(oevent.getParameters());
		if (oevent.getEventUser()!=null) {
			ievent.setUser(new ApiProxy(oevent.getEventUser().getDisplayName(), UriHelper.getUri(oevent.getEventUser()), "user"));
		}
		ievent.setTime(oevent.getTime());
		ievent.setDescription(event.getDescription());
		ievent.setDomain(getDomain(oevent.getDomainId()).getName());
		return ievent;	
	}
	
	private Domain getDomain(long id) {
		return getContentDao().findDomainById(id);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
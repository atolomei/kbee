package kbee.web.service;

import org.apache.wicket.protocol.http.WebApplication;

import com.novamens.event.EventListener;
import com.novamens.portal6.model.PortalService;
import com.novamens.service.SystemService;
				
public interface PortalUrlMapperService extends SystemService, PortalService, EventListener {

	
	public void map(WebApplication webapp);
	
}

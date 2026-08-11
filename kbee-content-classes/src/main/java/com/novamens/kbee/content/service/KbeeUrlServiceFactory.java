package com.novamens.kbee.content.service;


import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.UrlService;
import com.novamens.dom.Domain;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class KbeeUrlServiceFactory extends AbstractServiceFactory<UrlService> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUrlServiceFactory.class.getName());

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(UrlService.class);
	}
	
	@SuppressWarnings("unchecked")
	public UrlService getService(Object object) {

		if (object instanceof Content)
			return new KbeeContentUrlService((Content)object);
		
		if (object instanceof DataSetMember)
			return new KbeeMemberUrlService((DataSetMember)object);
		
		if (object instanceof Domain)
			return new KbeeDomainUrlService((Domain)object);
		
		if (object instanceof Resource)
			return new KbeeResourceUrlService((Resource)object);
		
		logger.error( this.getClass().getSimpleName() + " - getService() does not support class -> " + object.getClass().getName() );
		
		throw new IllegalArgumentException(this.getClass().getSimpleName() + " - getService() does not support class -> " + object.getClass().getName());
	}
}

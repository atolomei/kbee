package com.novamens.kbee.content.qa;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.qa.QAService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class QAServiceFactory extends AbstractServiceFactory<QAService>  {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(QAService.class);
	}
	
	@SuppressWarnings("unchecked")
	public QAService getService(Object object) {
		Assert.isInstanceOf(Content.class, object);
		return new ContentQAService((Content)object);
	}	
}

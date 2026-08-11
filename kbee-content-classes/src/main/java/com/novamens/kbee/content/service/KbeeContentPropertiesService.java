package com.novamens.kbee.content.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.properties.ContentProperties;
import com.novamens.logging.UpdateEvent;
import com.novamens.service.BusinessObjectService;
import com.novamens.service.ServiceLocator;

public class KbeeContentPropertiesService implements BusinessObjectService {

	private ContentProperties contentProperties = null;
	private ContentDao dao = null; 

	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	
	
	public KbeeContentPropertiesService() {
	}
	
	public KbeeContentPropertiesService(Content content) {
		this. contentProperties = getContentDao().getContentProperties(content); 
	}
	 
	 @Transactional(propagation = Propagation.REQUIRED)
	 public void delete() throws ContentMgmtException {
		getContentDao().delete(getContentProperties());
		List<String> list = new ArrayList<String>();
		list.add("delete Properties");
		trx_logger.info(new UpdateEvent(getContentProperties().getContent(), list)); 
	}
	
	 
	@Transactional(propagation = Propagation.REQUIRED)
	public void update() throws ContentMgmtException   {
		 getContentDao().save(getContentProperties());
		 List<String> list = new ArrayList<String>();
		 list.add("update Properties");
		 trx_logger.info(new UpdateEvent(getContentProperties().getContent(), list));
	}
	
	
	public ContentProperties getContentProperties() {
		return contentProperties;
	}

	
	private ContentDao getContentDao() {
		if (dao==null)	 {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 dao = (ContentDao) beans.getBean("contentDao");
		 }
		return dao;
	}	
}

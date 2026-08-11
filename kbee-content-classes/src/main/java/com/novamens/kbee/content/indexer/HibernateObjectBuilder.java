package com.novamens.kbee.content.indexer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.ObjectBuilder;
import com.novamens.service.ServiceLocator;

public class HibernateObjectBuilder implements ObjectBuilder {
 
	static private Logger logger = LogManager.getLogger(HibernateObjectBuilder.class.getName());
	
	public Object build(Document document) {
		Object object;
		try {
			object = getContentDao().findObjectById(new ObjectId(document.getFieldValue("id")));
			if (object == null) {
				logger.info("null object "+ document.getId());
			}
			return object;
		} 
		catch (ContentMgmtException e) {
			if (logger.isDebugEnabled()) {
				logger.error("buliding object", e);
			}
			else {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
			return null;
		}
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

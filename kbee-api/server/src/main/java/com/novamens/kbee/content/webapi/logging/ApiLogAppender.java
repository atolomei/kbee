package com.novamens.kbee.content.webapi.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

@Plugin(name = "API", category = "Core", elementType = "appender")
public class ApiLogAppender extends AbstractAppender  {
	
	protected static final Log logger = LogFactory.getLog(ApiLogAppender.class);

	private ApiLogAppender(String name, Filter filter) {
		super(name, filter, null);
	}
	
	@PluginFactory
	public static ApiLogAppender createAppender(
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new ApiLogAppender(name, filter);
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();
	}
	
	public void append(LogEvent event) {
		if(event.getMessage().getParameters()!=null) {
			for(Object parameter : event.getMessage().getParameters()){
				if (parameter instanceof ApiLogEvent) {
					append((ApiLogEvent)parameter);
				}
			}
		}
	}
	
	protected ApiLogDao getLogDao() {
		return (ApiLogDao)ServiceLocator.getService(BeansService.class).getBean("apiLogDao");	
	}
	
	protected void append(ApiLogEvent event) {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			getLogDao().append(event);
			transaction.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	
	protected Transaction beginTransaction() {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
}

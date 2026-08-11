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

@Plugin(name = "SyncAPI", category = "Core", elementType = "appender")
public class SyncApiLogAppender extends AbstractAppender  {
	
	protected static final Log logger = LogFactory.getLog(SyncApiLogAppender.class);

	private SyncApiLogAppender(String name, Filter filter) {
		super(name, filter, null);
	}
	
	@PluginFactory
	public static SyncApiLogAppender createAppender(
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new SyncApiLogAppender(name, filter);
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
		try {
			getLogDao().append(event);
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	
}

package com.novamens.logging;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

/**
 * Plug in de Log4j que graba en la tabla de Auditoría los eventos.
 *  
 */
@Plugin(name = "SyncAuditor", category = "Core", elementType = "appender")
public class SyncAuditorAppender extends AbstractAppender  {
	 

	private SyncAuditorAppender(String name, Filter filter) {
		super(name, filter, null);
	}
	
	@PluginFactory
	public static SyncAuditorAppender createAppender(
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new SyncAuditorAppender(name, filter);
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
		try {
			if(event.getMessage().getParameters()!=null) {
				for(Object param : event.getMessage().getParameters()) {
					if ( /*(param instanceof TaskPendingEvent) ||*/ !(param instanceof AbstractLogEvent || param instanceof SendEmailEvent)) 
						return;
				}
			}
			else
				return;

			if(event.getMessage().getParameters()!=null) {
				BeansService beans = ServiceLocator.getService(BeansService.class);
				LogDao logDao = (LogDao)beans.getBean("logDao");
				for(Object param : event.getMessage().getParameters()){
					if (param instanceof AbstractLogEvent) {
						AbstractLogEvent objEvent = (AbstractLogEvent) param;
						logDao.update(objEvent);
					} 
					else if (param instanceof SendEmailEvent) {
						SendEmailEvent objEvent = (SendEmailEvent) param;
						logDao.update(objEvent);
					}
				}
			}
		}
		catch (Throwable e) {
			for(Object param : event.getMessage().getParameters()){
				if (param instanceof AbstractLogEvent || param instanceof SendEmailEvent) {
					e.printStackTrace();
				}
			}	
		}
		finally {
		}
	}
}

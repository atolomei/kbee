package com.novamens.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.novamens.beans.BeansService;
import com.novamens.hibernate.session.Session;
import com.novamens.service.ServiceLocator;

/**
 * Plug in de Log4j que graba en la tabla de Auditoría los eventos.
 * Log4j.xml tiene que tener configurado el logger asincrónico.
 */
@Plugin(name = "Auditor", category = "Core", elementType = "appender")
public class AuditorAppender extends AbstractAppender  {
				
	static Logger logger = LogManager.getLogger(AuditorAppender.class.getName());

	private boolean session = false;
	private long opentime = 0;

	private AuditorAppender(String name, Filter filter) {
		super(name, filter, null);
	}
	
	@PluginFactory
	public static AuditorAppender createAppender(
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new AuditorAppender(name, filter);
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();
		if (this.session)	
			Session.close();
	}
	
	public void append(LogEvent event) {
		try {
			if(event.getMessage().getParameters()!=null) {
				for(Object param : event.getMessage().getParameters()) {
					if (!(param instanceof AbstractLogEvent || param instanceof SendEmailEvent)) 
						return;
				}
			}
			else
				return;

			if (!this.session) {
				Session.open();
				session = true;
				this.opentime = System.currentTimeMillis();
			}
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
			for(Object param : event.getMessage().getParameters()) {
				if (param instanceof AbstractLogEvent || param instanceof SendEmailEvent) {
					logger.error(e.getClass().getName(), e);
				}
			}	
			if (this.session) {
				Session.close();
				this.session = false;
			}	
		}
		finally {
			if (this.session) {
				if (System.currentTimeMillis() - this.opentime>120000L) {
					Session.close();
					this.session = false;
				}
			}	
		}
	}
}

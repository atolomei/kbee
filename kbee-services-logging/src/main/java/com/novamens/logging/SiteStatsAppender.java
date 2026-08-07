package com.novamens.logging;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.novamens.hibernate.session.Session;
import com.novamens.service.ServiceLocator;
import com.novamens.site.logging.PortalStatService;
import com.novamens.site.logging.SiteStatEvent;
import com.novamens.site.logging.SiteStatInEvent;
import com.novamens.site.logging.SiteStatOutEvent;


/**
 * This Log4J appender logs visits into the Site Log Database
 *
 */
@Plugin(name = "SiteStats", category = "Core", elementType = "appender")
public class SiteStatsAppender extends AbstractAppender {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SiteStatsAppender.class.getName());
	
	
	private SiteStatsAppender(String name, Filter filter) {
		super(name, filter, null);
	}

	public SiteStatsAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	
	@PluginFactory
	public static SiteStatsAppender createAppender(
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new SiteStatsAppender(name, filter);
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();
		 
	}

	@Override
	public void append(LogEvent event) {
		try {
 			if(event.getMessage().getParameters()!=null && event.getMessage().getParameters().length>0) {
				for(Object param : event.getMessage().getParameters()) {
					if (!(param instanceof SiteStatEvent)) 
						return;
				}
			}
			else
				return;
 			
 			Session.open();
			
			if(event.getMessage().getParameters()!=null) {
				com.novamens.site.logging.PortalStatService service = ServiceLocator.getService(PortalStatService.class);
				for(Object param : event.getMessage().getParameters()) {
					if (param instanceof SiteStatInEvent) {
						SiteStatInEvent objEvent = (SiteStatInEvent) param;
						service.addInboundLog(objEvent);
					}
					else if (param instanceof SiteStatOutEvent) {
						SiteStatOutEvent objEvent = (SiteStatOutEvent) param;
						service.addOutboundLog(objEvent);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
			 
		}
		finally {
			try {
				Session.close();
			} catch (Exception e) {
				//logger.error(e);
			}
		}
	}
}


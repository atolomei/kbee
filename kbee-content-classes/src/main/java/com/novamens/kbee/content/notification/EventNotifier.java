package com.novamens.kbee.content.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.notification.NotificationService;
import com.novamens.logging.AbstractLogEvent;
import com.novamens.service.ServiceLocator;
			    
/**  
 * 
 * Handler that receives from Log4j
  * the events to be notified.
  *
  * Your task is to pass to the notification service
  * the received event for you to process.
  *
  * Log4j appender that calls process from {@link NotificationService}
  
   ((event instanceof CheckinEvent)
(event instanceof TaskStartEvent)
(event instanceof TaskPendingEvent)
(event instanceof WorkNoteCreateEvent)
(event instanceof WorkNoteDeleteEvent)))
				
 
 */

@Plugin(name = "EventNotifier", category = "Core", elementType = "appender")
public class EventNotifier extends AbstractAppender {
							
	static private Logger logger = LogManager.getLogger(EventNotifier.class.getName());
	
	NotificationService noti = null;
	ContentDao dao = null;
	
	@PluginFactory
	public static EventNotifier createAppender (
		@PluginAttribute("name") String name,
		@PluginElement("filters") Filter filter) {
		return new EventNotifier(name, filter);
	}
	
	
	@Override
	public void start() {
		super.start();
	}

	
	@Override
	public void stop() {
		super.stop();
	}
	
	/**
	 * <p>This method does not open a Hibernate Session
	 * It is assumed that the logger.info(..) that made the call
	 * was in a thread that has a Hibernate Session opened.</p>
	 *  
	 * <p>The idea is that Event Notifications be atomic with the 
	 * Database transaction where the event is created.</p>   
	 * 
	 * see {@link AuditNotifier} for Async append that opens a Hibernate Session
	 *
	 * 
	 */
	public void append(LogEvent event) {
		try {
			 if(event.getMessage().getParameters()!=null) {
				 for(Object param: event.getMessage().getParameters()) {
					 if (!(param instanceof AbstractLogEvent)) 
								return;
					 com.novamens.event.LogEvent objEvent = (com.novamens.event.LogEvent) param;
					 getNotifierService().process(objEvent);					 					 
				}
			}
		}
		catch (Throwable e) {
			logger.error(" {} | {} | {}", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		}
	}
	
	
	private NotificationService getNotifierService() {
		if (noti==null)
			noti = ServiceLocator.getService(NotificationService.class);
		return noti;
	}

	
	private EventNotifier(String name, Filter filter) {
		super(name, filter, null);
	}

}

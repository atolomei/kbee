package com.novamens.kbee.content.security;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.IQLRule;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReindexByCriteriaCommand;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public class RuleUpdateListener implements EventListener {
	
	static private Logger logger = new Logger(LogManager.getLogger(RuleUpdateListener.class.getName()));

	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent || event instanceof AppCreateEvent) && event.getObject() instanceof IQLRule);
	}
	
	public void onEvent(Event event) {
		int i =0;
		
		KbeeSecurityRule rule = ((KbeeSecurityRule)event.getObject());
		
		Domain domain = rule.getDomain();
		
		if (event instanceof HibernateUpdateEvent) {
			HibernateUpdateEvent updateevent = (HibernateUpdateEvent)event; 
			for (String propertyName : updateevent.getPropertyNames()) {
				if ("condition".equals(propertyName)) {
					if (updateevent.getCurrentState()[i]!=null && 
							updateevent.getPreviousState()!=null &&
							!updateevent.getCurrentState()[i].equals(updateevent.getPreviousState()[i])) {
						IqlService iqlservice = domain.getService(IqlService.class);
						String condition = (String)((HibernateUpdateEvent)event).getPreviousState()[i];
						if (condition!=null && !"".equals(condition) && !condition.equals(rule.getCondition())) {
							IqlQuery query = iqlservice.getNewQuery(condition);
							Index index = domain.getService(JavaIndexerService.class).getIndex();
							ReindexByCriteriaCommand command = new ReindexByCriteriaCommand(query, index, "reader", "taker");
							command.setName("Reindex Rule \""+ rule.getName() + "\" Old Criteria");
							logger.debug(command.getName());
							command.setParameter("condition", condition);
							command.setParameter("rule", rule.getId());
							command.setDescription(rule.getDescription());
							ServiceLocator.getService(CommandService.class).add(command);
						}
					}
					break;
				}
				else 
					i++;
			}
		}
			
		IqlService iqlservice = domain.getService(IqlService.class);
		String condition = rule.getCondition();
		if ("".equals(condition)) return;
		IqlQuery query = iqlservice.getNewQuery(condition);
		Index index = domain.getService(JavaIndexerService.class).getIndex();
									
		ReindexByCriteriaCommand command = new ReindexByCriteriaCommand(query, index, "reader", "taker", "writer");
		command.setParameter("rule", rule.getId());
		command.setName("Reindex Rule \""+ rule.getName() + "\"");
		command.setDescription(rule.getDescription());
		ServiceLocator.getService(CommandService.class).add(command);
		
		logger.debug("Reindex Rule :"+rule.getName());
			
		ServiceLocator.getService(ContentSystemSecurityService.class).onUpdate(rule);
	}
}
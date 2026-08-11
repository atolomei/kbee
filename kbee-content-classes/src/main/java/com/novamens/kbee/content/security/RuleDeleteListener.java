package com.novamens.kbee.content.security;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.security.IQLRule;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReindexByCriteriaCommand;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

public class RuleDeleteListener implements EventListener {
	
	static private Logger logger = new Logger(LogManager.getLogger(RuleDeleteListener.class.getName()));

	public boolean listen(Event event) {
		return ((event instanceof AppDeleteEvent) && event.getObject() instanceof IQLRule);
	}
	
	public void onEvent(Event event) {
		try {
			Domain domain = ((KbeeSecurityRule)event.getObject()).getDomain();
			IqlService iqlservice = domain.getService(IqlService.class);
			String condition = ((KbeeSecurityRule)event.getObject()).getCondition();
			if ("".equals(condition)) return;
			IqlQuery query = iqlservice.getNewQuery(condition);
			Index index = domain.getService(JavaIndexerService.class).getIndex();
			
			SecurityRule rule = (KbeeSecurityRule)event.getObject();
			
			ReindexByCriteriaCommand command = new ReindexByCriteriaCommand(query, index, "reader", "taker");
			command.setName("Reindex Rule (On Delete) \""+ rule.getName() + "\" Criteria");
			logger.debug(command.getName());
			command.setParameter("condition", condition);
			command.setParameter("rule", rule.getId());
			command.setDescription(rule.getDescription());
			ServiceLocator.getService(CommandService.class).add(command);
		}
		catch (Exception e) {
			throw new KbeeRuntimeException(e);
		}
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
}

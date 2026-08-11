package com.novamens.kbee.content.rule;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.rule.ActionRule;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

public class KbeeRulesCommand extends AsyncCommand {
															
	private static kbee.util.logging.Logger logger 				= kbee.util.logging.Logger.getLogger(KbeeRulesCommand.class.getName());
	private static kbee.util.logging.Logger actionrulesLogger 	= kbee.util.logging.Logger.getLogger("actionrules");

			
	private int total_rules = 0;
	private int evaulated_rules = 0;
	private String commandMode = ActionRule.PRODUCTION;

	
	public KbeeRulesCommand() {
		setName("Rules Command");
		setDescription("mode= " + ActionRule.PRODUCTION +" | " + ActionRule.TEST_CONDITION);
	}
	
	public void executeAsync() {
		try {
			
			total_rules = 0;
			evaulated_rules = 0;
			
			super.setState(CommandState.RUNNING);
			super.setDateStarted(OffsetDateTime.now());
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			if (getParameters().containsKey("mode"))
				setMode(getParameters().get("mode").toString());
			
			com.novamens.hibernate.session.Session.open();
			
			for (Domain domain : getContentDao().getDomains()) {
				for (ActionRule rule : getRepository(ActionRule.class).findAll(domain)) {
					if (rule.getState()==ObjectState.ENABLED)
						total_rules++;
				}
			}
											
			setDescription("Total Rules to Evaluate: " + String.valueOf(total_rules));

			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			
			setProgress(0.0);
			
			if (total_rules>0) {
	 			for (Domain domain : getContentDao().getDomains()) {
	 				logger.debug("Process > " + domain.getName());
	 				process(domain);
	 			}
			}
			
 			setProgress(100.0);
			end();
			super.setState(CommandState.COMPLETED);
		}
		catch (Exception e) {
			
			logger.error(e);
			actionrulesLogger.error(e);
			
			super.setState(CommandState.ERROR);
			super.setResult(e.getClass().getSimpleName());
			super.setResultComments(e.getMessage());
			stop();
		}
		finally {
			
			super.setDateTerminated(OffsetDateTime.now());
			com.novamens.hibernate.session.Session.close();
			logger.debug("done  " + OffsetDateTime.now().toString());
		}
	}
	
	// -----------------
	// mode
	//
	// production
	// test-condition
	// 
	public void setMode( String mode) {
		this.commandMode=mode;
	}
	
	public String getMode() {
		return this.commandMode;
	}
	
	private void process(Domain domain) {
		
		try {
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+domain.getName());
			
			List<ActionRule> rules = getRepository(ActionRule.class).findAll(domain);
													
			super.setStatusInfo(domain.getName() + " (" + String.valueOf(rules.size())+" rules)");
			
			logger.debug(domain.getName() + " (" + String.valueOf(rules.size())+" rules)");

			for (ActionRule rule : rules) {
				if (rule.getState()==ObjectState.ENABLED) {
					setProgress( Double.valueOf((this.evaulated_rules++) * 100.0 / this.total_rules).doubleValue());
					logger.debug("Before evaluating: " + rule.getDisplayName() + " (id: " + rule.getId().toString()+")");
					evaluate(rule);
					
				}
			}
			
		} 
		catch (Exception e) {
			
			logger.error(e);
			actionrulesLogger.error(e);
			
			super.setStatusInfo(e.getClass().getSimpleName());
		}
	}
	
	private void evaluate(ActionRule rule) {
		Transaction transaction = null;
		try {
			logger.debug(rule.getDisplayName() + " | " + rule.getDisplayCondition() + " | id: " + rule.getId().toString());
			transaction = beginTransaction();
			rule.evaluate(getMode());
			transaction.commit();
		}
		catch (Exception e) {
			logger.error(e);
			actionrulesLogger.error(e);
			super.setStatusInfo(e.getClass().getSimpleName());
			transaction.rollback();
		}
	}
	
    public long getTotalItems() {
        return total_rules;
    }

    public long getTotalItemsProcessed() {
        return evaulated_rules;
    }
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
  
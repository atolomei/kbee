package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.DomService;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.dao.KbeeContentDao;
import com.novamens.kbee.content.notification.KbeeNotification;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

import kbee.content.support.SupportTicketsSubmitterCommand;
				
public class CleanUpContentRulesCommand extends AsyncCommand {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CleanUpContentRulesCommand.class.getName());
	
	public CleanUpContentRulesCommand() {
		super();
		setName("Clean up rules for deleted contents");
	}
		
	/**
	 * delete from kb_action_rule where contentOid!=null and contentOid not in (select oid from content where oid=contentOid)
	 */
	
	@Override
	protected void executeAsync() {
		super.setState(CommandState.RUNNING);		
		super.setDateStarted(OffsetDateTime.now());
		
		int index = 0;
		
		try {

			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			
			List<ActionRule> candidates = new ArrayList<ActionRule>();
			List<ActionRule> list = (List<ActionRule>) getContentDao().getResultSet("from " + KbeeIqlActionRule.class.getSimpleName() + " R where R.isContentRule=true and R.contentOId != null order by lastModifiedDate");
			
			super.setProgress(0);
			
			for (ActionRule rule: list) {
				Long cid = ((KbeeIqlActionRule) rule).getContentOId();
				Content c  = getContentDao().findContentByOId(cid);
				if (c==null)
					candidates.add(rule);
			}
			
			for (ActionRule a:candidates) {
				logger.debug("Deleting  -> " + a.getDisplayName() + " | id: " +  a.getId().toString());
				delete(a);
				index++;
				super.setProgress( (double) index / (double) candidates.size());
			}
			
			setProgress(100.0);
			super.setState(CommandState.COMPLETED);
			
		} catch (Exception e) {
			logger.error(e);
			super.setState(CommandState.ERROR);
			super.setResult(e.getClass().getSimpleName());
			super.setResultComments(e.getMessage());
			stop();
		}
		finally {
			
			logger.debug("removed -> " + String.valueOf(index));
			setDateTerminated(OffsetDateTime.now());
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	
	
	private void delete(ActionRule rule) {
		
		Transaction transaction = null;
		boolean is_ok = false;
		
		try {

			transaction = beginTransaction();
			rule.getService(DomService.class).delete();
			is_ok = true;
			
		} catch (Exception e) {
			logger.error(e);
			super.setState(CommandState.ERROR);
			super.setResult(e.getClass().getSimpleName());
			super.setResultComments(e.getMessage());
			stop();
		}
		finally {

			try {
			if (transaction!=null) {
				if (is_ok) {
					transaction.commit();
				}
				else {
					transaction.rollback();
					return;
				}
			}
			} catch (Exception e) {
				logger.error(e);
			}
			
			setDateTerminated(OffsetDateTime.now());
			com.novamens.hibernate.session.Session.close();
		}
	
	}
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
	}

}

package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;

public class KbeeActionTimeoutCommand extends AsyncCommand {
															
	private static Logger logger 	= Logger.getLogger(KbeeActionTimeoutCommand.class.getName());

			
	private int total_domains = 0;
	private int evaluated_domains = 0;

	
	public KbeeActionTimeoutCommand() {
		setName("Workflow Timeouts");
		setDescription("mode= " + ActionRule.PRODUCTION +" | " + ActionRule.TEST_CONDITION);
	}
	
	public void executeAsync() {
		try {
			
			total_domains = 0;
			evaluated_domains = 0;
			

			 super.setState(CommandState.RUNNING);
			super.setDateStarted(OffsetDateTime.now());
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			//if (getParameters().containsKey("mode"))
			//	setMode(getParameters().get("mode").toString());
			
			com.novamens.hibernate.session.Session.open();
			
			List<Domain> domains =  getContentDao().getDomains();
			
			total_domains = domains.size();
			
			setDescription("Total Domains to Evaluate: " + total_domains);
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			setProgress(0.0);
			
			for (Domain domain : domains) {
				String d = domain.getDisplayName();
				try {
					for (Procedure procedure : getWorkflowDao().getProcedures(domain)) {
						if (hasActionsWithTimeOut(procedure) || hasDueDates(procedure)) {
							logger.debug("Timeouts|Alerts "+domain.getDisplayName());
							ProcessesQuery query = new ProcessesQuery(procedure);
							ResultSet processestSet = query.execute();
							while (processestSet.hasNext()) {
								
								Process process = (Process)processestSet.next().getObject(); 
								EndCondition action = getActionInTimeOut(process);
								if (action!=null) {
									execute(process, action);
								}
								
								Content content = getContent(process);
								if (content!=null) {
									WorkflowService ws = content.getService(WorkflowService.class);
									if (ws.hasDueDateAlert()) {
										ws.fireDueDateAlert(); 
									}
								}
							}
						}
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
				evaluated_domains++;
			}
			
 			setProgress(100.0);
			end();
			super.setState(CommandState.COMPLETED);
		}
		catch (Exception e) {
			
			logger.error(e);
			
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
	
    public long getTotalItems() {
        return total_domains;
    }

    public long getTotalItemsProcessed() {
        return evaluated_domains;
    }
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	private boolean hasActionsWithTimeOut(Procedure procedure) {
		try {
			for (Task task : procedure.getTasks()) {
				if (((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition condition : ((KbeeTask)task).getEndConditions()) {
					if (condition.getAutoRunAfter()>0) {
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
	
	
	private boolean hasDueDates(Procedure procedure) {
		try {
			for (Task task : procedure.getTasks()) {
				if (((KbeeTask)task).getDueDateAlerts()!=null && !"".equals(((KbeeTask)task).getDueDateAlerts())) {
					return true;
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	
	private EndCondition getActionInTimeOut(Process process) {
		Content content = getContent(process);
		if (content==null) return null;
		Task task = content.getService(WorkflowService.class).getTask();
		for (EndCondition condition : ((KbeeTask)task).getEndConditions()) {
			if (condition.getAutoRunAfter()>0) {
				if (condition.isTimeout(content.getService(WorkflowService.class).getContext()) && condition.isEnabled(content)) {
					return condition;
				}
			}
		}
		return null;
	}
	
	private void execute(Process process, EndCondition action) {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			Content content = getContent(process);
			content.getService(WorkflowService.class).handle(new KbeeWorkflowEvent(action.getEvent(), action.getLabel()));
			transaction.commit();
		}
		catch (Exception e) {
			logger.error(e);
			super.setStatusInfo(e.getClass().getSimpleName());
			transaction.rollback();
		}
	}
	
	private Content getContent(Process process) {
		Content content = null;
		for (Activity activity : process.getActivities()) {
			content = ((KbeeWorkflowActivity)activity).getContent();
			break;
		}
		return content;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao) ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
  
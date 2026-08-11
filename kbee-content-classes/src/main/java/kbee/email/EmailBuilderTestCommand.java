package kbee.email;

import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

public class EmailBuilderTestCommand extends AsyncCommand {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderTestCommand.class.getName());
	
	
	Domain domain;
	
	int processed = 0;
	int total_builers = 1;
	
	
	public EmailBuilderTestCommand() {
		setName("EmailBuilderTestCommand");	
	}

	
	@Override
	protected void executeAsync() {

		try {
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			
			super.setState(CommandState.RUNNING);
			super.setDateStarted(OffsetDateTime.now());
			
			com.novamens.hibernate.session.Session.open();
			
			String dname = null;
			if (getParameters()!=null) {
				if (getParameters().containsKey("domain"))
						dname=(String) getParameters().get("domain");
			}
			
			if (dname==null)
				dname = "windsor";
			
			
			
			domain = getContentDao().findDomainByName("windsor");
			
			logger.debug("-------------------------------------------------");
			
			{
				
				
				process();
				setProgress(Double.valueOf((this.processed++) * 100.0 / this.total_builers).doubleValue());
			}
 			
			
 			setProgress(100.0);
			end();
			super.setState(CommandState.COMPLETED);


		} catch (Exception e) {
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
	
	
	
	
	private void process() {

		
		Transaction transaction = null;
		boolean is_ok = false;

		try {
				
			transaction = beginTransaction();		
			is_ok = true;
			
			
			
				
		}	catch (Exception e) {
				logger.error(e);
				super.setStatusInfo(e.getClass().getSimpleName() + " " + e.getMessage());
				
		} finally {
				
			if (transaction!=null) {
				if (is_ok) {
					transaction.commit();
				}
				else {
					transaction.rollback();
				}
			}
		}
		
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}

package kbee.content.support;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DOMObjectService;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderSupportTicketReceiver;
import kbee.email.EmailBuilderSupportTicketSubmitter;
import kbee.util.PropertiesFactory;

public class SupportTicketsSubmitterCommand extends AsyncCommand {
			
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SupportTicketsSubmitterCommand.class.getName());

	static boolean IS_SUPPORT_ENABLED  = true;
	static {
		
		try {
			IS_SUPPORT_ENABLED = PropertiesFactory.getInstance("kbee").getProperties().getProperty("support.enable", "no").equals("yes");
		} catch (Exception e) {
			IS_SUPPORT_ENABLED= false;
		}
	}
	
	
	Integer upper_threshold = Integer.valueOf(3);
	
	String key = "minute";  	 // "minute"  (error_count < 3) 
								 // "daily" (error_count < 6)  
	String emailTo;
	int processed = 0;
	int error = 0;
	int total = 0;
	
	
	public SupportTicketsSubmitterCommand() {
		this("minute");
	}
	
	public SupportTicketsSubmitterCommand(String key) {
		super();
		setName("Submit support tickets by email or API | SystemParameter -> support.email");
		upper_threshold = key.equals("minute") ? Integer.valueOf(3) : Integer.valueOf(6);
}
	

	public Integer getUpperThreshold() {
		return upper_threshold;
	}




	public void setUpperThreshold(Integer upper_threshold) {
		this.upper_threshold = upper_threshold;
	}


	public String getKey() {
		return key;
	}




	public void setKey(String key) {
		this.key = key;
	}



	
	
	@Override
	protected void executeAsync() {
		

				processed = 0;
				error = 0;
				
				
				if (!IS_SUPPORT_ENABLED) {
					logger.debug("Support is not enabled on this Server ('support.enable' != 'yes')");
					setProgress(100.0);
					end();
					super.setState(CommandState.COMPLETED);
					return;
				}
						
				super.setState(CommandState.RUNNING);		
				super.setDateStarted(OffsetDateTime.now());

		try {
				
				
				com.novamens.hibernate.session.Session.open();
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
				
				emailTo = ServiceLocator.getService(BrandingService.class).getSupportTicketEmailAddress();
				logger.debug(emailTo);
				
				List<SupportTicket> list = getContentDao().getPendingSupportTickets(upper_threshold.intValue());
				
				if (list==null)
					list=new ArrayList<SupportTicket>();
				
				this.total  = list.size();
				
				if (total>0) {
					
					for (SupportTicket s: list) {
				
						setProgress(Double.valueOf((this.processed++) * 100.0 / this.total).doubleValue());
						
						if (    s.getDeliveryStatus()!=SupportTicket.DELIVERY_STATUS_SENT && 
								s.getDeliveryStatus()!=SupportTicket.DELIVERY_STATUS_DOMAIN_NOT_ENABLED) {
				
								try {
										if (s.getDomain().isEnabled()) {
												if (s.getDeliveryStatus()==SupportTicket.DELIVERY_STATUS_PENDING) {
													sendTicket(s);
												
												}
												else if (s.getDeliveryStatus()==SupportTicket.DELIVERY_STATUS_ERROR) {
													if (s.getErrorCount()<this.upper_threshold.intValue()) {
														sendTicket(s);
												
													}
												}
										}
										else {
											setAsDomainNotEnabled(s);
											processed++;
										}
									} catch (Exception e) {
										error++;
									}
								}
					}
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
				setDateTerminated(OffsetDateTime.now());
				com.novamens.hibernate.session.Session.close();
				logger.debug("done  " + OffsetDateTime.now().toString());
			}
	}	
	
	
	protected void setAsDomainNotEnabled(SupportTicket ticket) {
		try {
			ticket.setDeliveryStatusMsg("Sent - > " + emailTo);
			ticket.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			ticket.setDeliveryStatus(SupportTicket.DELIVERY_STATUS_DOMAIN_NOT_ENABLED);
			ticket.getService(DOMObjectService.class).update();
			logger.debug("Domain not enabled -> " + ticket.toString());
			
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	
	
	/**
	 * @param ticket
	 */
	protected void sendTicket(SupportTicket t) {

 
		SupportTicket ticket = null;
		try {
			

			ticket = getContentDao().findSupportTicket((Long) t.getId());
			
			
			if (ticket!=null) {

				// IF IS EMAIL
				// send to receiver
				EmailData rec_data= (new EmailBuilderSupportTicketReceiver(ticket, emailTo)).build();
				ServiceLocator.getService(EmailService.class).send(rec_data, ticket.getDomain());


				// send to submitter (the person who created the ticket)
				EmailData data= (new EmailBuilderSupportTicketSubmitter(ticket)).build();
				ServiceLocator.getService(EmailService.class).send(data, ticket.getDomain());

				ticket.setDeliveryStatusMsg("Sent - > " + emailTo);
				
				ticket.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				ticket.setDeliveryStatus(SupportTicket.DELIVERY_STATUS_SENT);
	
				ticket.getService(DOMObjectService.class).update();
				
			}
			
			
		} catch (Exception e) {
			
			logger.error(e);
			if (ticket!=null) {
				ticket.setErrorCount(ticket.getErrorCount()+1);
				ticket.setDeliveryStatusMsg(e.getClass().getSimpleName() + " | " + e.getMessage());
				ticket.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				ticket.setDeliveryStatus(SupportTicket.DELIVERY_STATUS_ERROR);
			}
		}
		
		
 
	}
	
	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return (ContentDao) beans.getBean("contentDao");
   }

}

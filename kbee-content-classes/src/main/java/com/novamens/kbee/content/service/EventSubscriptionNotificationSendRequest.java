package com.novamens.kbee.content.service;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;

import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailData;

// import com.novamens.kbee.wicket.services.BrandingService;

import com.novamens.logging.SendEmailEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailSendServiceRequest;
import kbee.util.PropertiesFactory;

/**
 *  <p>Notificaciones a subscriptores ante eventos sobre un {@link Content}. 
 *  Los eventos que generan notificaciones a suscriptores son:</p>

 *  <ul>
 *	   		<li>add {@link Report} 	desde {@link SocialEventListener}</li>
 *  	 	<li>add {@link Vote} 	desde {@link SocialEventListener}</li>
 *   		
 *   		<li>add {@link Comment} desde {@link KnowledgeSharingEventListener} </li>
 *   		<li>add {@link Answer}  desde {@link KnowledgeSharingEventListener}</li>
 *  </ul>
 */

public class EventSubscriptionNotificationSendRequest extends AbstractServiceRequest {
													
	
	private final String product = ServiceLocator.getService(BrandingService.class).getProductKey();

	static private  final long serialVersionUID = -7597682871672578453L;
	
	static private Logger logger = LogManager.getLogger(EmailSendServiceRequest.class.getName());
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
	
	private Serializable content_id;
	private com.novamens.content.subscription.SubscriptionEvent event;
	private String subject;
	private String msg;

	@SuppressWarnings("unused")
	private String link;

	/**
	 * <p>This object is serialized by the scheduler so
	 * it must be serializable.</p>
	 * 
	 * @param content_id
	 * @param event
	 * @param subject
	 * @param msg
	 * @param link
	 */
	
	public EventSubscriptionNotificationSendRequest(Serializable content_id, 
									    com.novamens.content.subscription.SubscriptionEvent event, 
									    String subject, 
									    String msg, 
									    String link) {
		
		this.content_id=content_id;
		this.event=event;
		this.subject=subject;
		this.msg=msg;
		this.link=link;
		
		
		
		setPriority(SchedulerService.LOW_PRIORITY);
		setCost(SchedulerService.STANDARD_PROCESSING_COST * 50);
		setName("Notification - ContentId: " + content_id + " (" + event.toString()+ ")");
	}

	/**
	 * Envía notificación por email a la lista de suscriptores 
	 */
	@Override
	public void execute() {
		
		Content content = getContentDao().findContentByOId(this.content_id);
				
		if (content!=null) {
			
			String APPLICATION, FROM;
			
			if (content.getDomain()!=null) {
				
				APPLICATION = product;
				
				if (APPLICATION==null)
					APPLICATION	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.application", product);

				FROM = content.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY);
				
				if (FROM==null)
					FROM	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.notification.noreplyemailaddress", "noreply@novamens.com");
			} else {
				APPLICATION	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.application", product);
				FROM	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.notification.noreplyemailaddress", "noreply@novamens.com");
			}

			String from_email =  String.format("%s <%s>", APPLICATION, FROM);
			
			SubscriptionService service = content.getService(SubscriptionService.class);
				try {
					List<UserProfile> list = service.getSubscribers(event);
					int n = 0;
					for (UserProfile userProfile: list) {
						EmailData emaildata = new EmailData(from_email, userProfile.getPerson().getEmail(), this.subject, this.msg, null, this.event.getEventType());
						emaildata.setUserId(new ObjectId(userProfile.getUser()).toString());
						emaildata.setObjectId((new ObjectId(content)).toString());
						
						String err = sendbyemail (emaildata);
						
						DBLogger.info(new SendEmailEvent(emaildata, err, content.getDomain()));
						n++;
					}
					logger.info("Event " + event.toString() +" - Sent: " + String.valueOf(n)+ " emails.");
					
				} catch (IOException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
		} else {
			logger.warn("Content with id: " + this.content_id + " not found");
		}
	}

	/**	
	 * 
	 */
	private String sendbyemail (EmailData emaildata) {
		
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 JavaMailSender mailsender = (JavaMailSender) beans.getBean("mailSender");
		 
		try {
			final MimeMessage msg = mailsender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
			helper.setFrom(emaildata.from);
			helper.setTo(emaildata.to);
			helper.setSubject(emaildata.subject);
			StringBuilder strMensaje = new StringBuilder();
			if (!emaildata.msg.isEmpty()) 
				strMensaje.append(emaildata.msg.replaceAll("\n", "<br/>") + "<br/>");

			helper.setText(strMensaje.toString(), true); // use the true flag to indicate the text included is HTML
			
			Content content = getContentDao().findContentByOId(this.content_id);
			
 			String mode = content!=null? content.getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_STATUS):"enabled";
			
			boolean is_active;
			
			if (mode==null)
				is_active = true;	
			else
				is_active = mode.equals("enabled") || mode.equals("yes");
		
			if (!is_active) {
				logger.debug("Simulating sending email... To disable set property: com.novamens.kbee.service.email = send");
				logger.debug(emaildata.toString());
				return("ok mode:nosend");
			}
			else {
				mailsender.send(msg);
				if (content!=null)
					ServiceLocator.getService(SystemMetricsService.class).mark("email", content.getDomain().getId());
				else
					ServiceLocator.getService(SystemMetricsService.class).getMeterEmails().mark();
				
				return("ok");
			}

		} catch (MessagingException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return(e.getMessage());
		}
		 catch (MailAuthenticationException e) {
			 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return(e.getMessage());
			
		} catch (RuntimeException e1) {
			logger.error(e1.getClass().getName());
			return(e1.getMessage());
		}
	}
	
	private ContentDao getContentDao() {
			 return  (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}



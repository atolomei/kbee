package kbee.email;

import java.io.File;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.logging.SendEmailEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;



/**
 *
 * <p> 
 * ServiceRequest to send email in Asynchronous Mode.
 * Files to send as attachment must be local files, and the absolutepath provided
 * </p>
 * 
 */
public class EmailSendServiceRequest extends AbstractServiceRequest {
	
	private static final long serialVersionUID = 520103921512826079L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailSendServiceRequest.class.getName());
	
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
	
	private EmailData emaildata;
	private Serializable domainid;

	private boolean executed = false;

	/**

		setParameters(map);
		Assert.isTrue(map!=null, "Map is null");
		this.emaildata=new EmailData(
		map.get("from"), 
		map.get("to"), 
		map.get("subject"),
		map.get("msg"),
		null,
		map.get("context_info"),
		null);
		this.domainid=map.get("domainid");
	}
	**/
	
	public EmailSendServiceRequest() {
	}
	
	
	public EmailSendServiceRequest(EmailData emaildata, Domain domain) {
		this.emaildata=emaildata;
		this.domainid=domain.getId();
		super.setId(System.currentTimeMillis()+emaildata.hashCode());
		setPriority(SchedulerService.LOW_PRIORITY);
		setCost(SchedulerService.STANDARD_PROCESSING_COST);
		setName(this.getClass().getSimpleName());
		setDescription("From: " + emaildata.from + " | To: " + emaildata.to + "| Subject: " + emaildata.subject + " | " + (emaildata.context_info!=null?emaildata.context_info:"n/a") );
	}

	
	public void setParameters(Map<String, String> map) {
		super.setParameters(map);
		Assert.isTrue(map!=null, "Map is null");
		
		try {
			this.emaildata=new EmailData(
			map.get("from"), 
			map.get("to"), 
			map.get("subject"),
			map.get("msg"),
			null,
			map.get("context_info"),
			null);
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			if (map.get("domainid")!=null) {
				this.domainid= Long.valueOf(map.get("domainid").replace("\r", "").replace("\n", ""));
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	@Override
	public String toString() {
		return (getName()!=null?getName():"") + " | " + super.toString();
	}
	
	@Override
	public void execute() {

		boolean sucess = false;
		
		try {
			if (isExecuted()) {
				sucess = true;
				logger.error("-----------------------------------------------------------------------------");
				logger.error(getName()+ " | id:" + getId().toString() +". already executed ");
				logger.error("-----------------------------------------------------------------------------");
				return;
			}

			
			if (this.domainid==null)
				return;
			

			if (this.emaildata==null)
				return;

			ServiceLocator.getService(SchedulerService.class).addRequestToken(getId());
			
			Domain domain = getContentDao().findDomainById(domainid);
			String mode = domain.getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_STATUS);
			
			boolean is_active;
			
			if (mode==null)
				is_active = true;	
			else
				is_active = mode.equals("enabled") || mode.equals("yes");
			
			if (is_active) {
				
					 Map<String, Object> params = new HashMap<String, Object>();
					 
					 params.put("from-email", emaildata.getFrom());
					 params.put("to-email", emaildata.getTo());
					 params.put("subject", emaildata.getSubject());

					 if (domain!=null)
						 params.put("domain_id", domain.getId().toString());
					 
					 StringBuilder str = new StringBuilder();
					 
					 
					 if (emaildata.getResources()!=null) { 
						 for( String s: emaildata.getResources()) {
							 if (s!=null && !s.equals("null")) {
								 File file = new File(s);
								 if (file==null || !file.exists() || file.isDirectory()) {
									 str.append("file error: " + s);
								 }
							 }
						 }
						 
						 params.put("attachments", emaildata.getResources());
					 }

					 params.put("texto",emaildata.getMsg()+ (str.length()>0?str.toString():""));
							 
					 if (emaildata.getLocalFileToSend()!=null)  
						 params.put("local_attachments", emaildata.getLocalFileToSend());
					 
					 String sentErr = sendbyemail(params);
					 
					 DBLogger.info(new SendEmailEvent(emaildata, sentErr, domain));
					 sucess=true;
			 }
			 else {
					try {
						
						logger.info("Mode NOSEND: Simulating sending email...");
						DBLogger.info(new SendEmailEvent(emaildata, "OK. Service: Disabled", getContentDao().findDomainById(domainid)));
				 		Thread.sleep(1000*(((int)Math.random())*100%2+1));
				 		sucess=true;
					} 
					catch (InterruptedException e) {
					}
			}
			
		} catch (Exception e) {
			logger.error(e);
			
		} finally {

			try {	
				this.executed=true;
				if (!sucess)
					ServiceLocator.getService(SchedulerService.class).removeRequestToken(getId());
			} catch (Exception e) {
				logger.error(e, " |  finally block ");
			}
		}
	}


	private ContentDao getContentDao() {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 return (ContentDao) beans.getBean("contentDao");
	}
	

	
	/**
	 *  
	 * @param params
	 * @return
	 */
	private String  sendbyemail (Map<String, Object> params) {
		
		 if (params.get("to-email")==null) {
			 logger.error("to-email is null");
			 return "to-email is null";
		 }
				 
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 JavaMailSender mailsender = (JavaMailSender) beans.getBean("mailSender");
		 
		try {

			final MimeMessage msg = mailsender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

			// Sender
			//
			String sen = (String)params.get("from-email");
			String sarr[] = sen.split(";");
			if (sarr.length==1)
				helper.setFrom((String) params.get("from-email"));
			else
				helper.setFrom(sarr[0]);
			
			
			helper.setSubject((String)params.get("subject"));
			
			StringBuilder strMensaje = new StringBuilder();

			if (! ((String) params.get("texto")).isEmpty())
				strMensaje.append( ((String) params.get("texto")).replaceAll("\n", "<br/>") + "<br/>" );
			
			helper.setText(strMensaje.toString(), true); // true flag to indicate the text included is HTML
			

			// File Server's resources
			//
			if (params.get("attachments")!=null) {
				String  resources[] = (String []) params.get("attachments");
				if (resources!=null) {
					for (String str: resources) {
						try {
							File file = getFile(str);
							if (file!=null && file.exists() && !file.isDirectory()) {  
								String name=file.getName();
								helper.addAttachment(name, file);
							}
							else 
								logger.error("local file not found: " + str);
							
						} catch (Exception e) {
							logger.error(" {} | {} | {}",  e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
						}
					}
				}
			}
			
			// Local files
			if (params.get("local_attachments")!=null) {
				 String path = (String) params.get("local_attachments");
				 File file = new File(path);
				 if (file!=null && file.exists() && !file.isDirectory()) {
					 helper.addAttachment(FilenameUtils.getName(path), file);
				 }
				 else {
					 logger.error("local file not found " + path);
				 }
			 }
			
			// Receiver/s
			String rec = (String)params.get("to-email");
			
			if (rec==null)
				rec="";
			
			rec=rec.replace(",", ";");
			
			String arr[] = rec.split(";");
			if (arr.length==1) {
				helper.setTo(rec);
			}
			else {
				helper.setTo(new InternetAddress(arr[0]));
	            int i = 0;
	            for (String address : arr) {
	            	if (i>0) 
	            		helper.addCc(new InternetAddress(address));
	                i++;
	            }
			}

			mailsender.send(msg);
			
			if (params.get("domaid_id")!=null)
				ServiceLocator.getService(SystemMetricsService.class).mark("email", (String) params.get("domaid_id"));
			else
				ServiceLocator.getService(SystemMetricsService.class).getMeterEmails().mark();

			return "OK";
			
		} catch (MessagingException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " |  " + e.getMessage());
			return e.getMessage();
			
		}
		 catch (MailAuthenticationException e) {
			 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " |  " + e.getMessage());
			 logger.error(emaildata.toString());
			 return e.getClass().getSimpleName();
			
		} catch (RuntimeException e1) {
			logger.error(e1.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.debug(e1);
			return e1.getMessage();
		}
	}

	
	/**
	 * 
	 * @param url
	 * @return
	 */
	private File getFile(String absolutepath) {

		if (absolutepath==null || absolutepath.equals("null"))
			return null;

		try {
			return new File(absolutepath);
			
		} catch (Exception e) {
			logger.error(e.getMessage() + " | file does not exist: " + absolutepath);
		}
		return null;
	}

	private boolean isExecuted() {
		if (this.executed)
			return true;
		return ServiceLocator.getService(SchedulerService.class).containsRequestToken(getId());
	}

}

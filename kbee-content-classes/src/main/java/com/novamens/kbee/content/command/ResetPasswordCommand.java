package com.novamens.kbee.content.command;



import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

public class ResetPasswordCommand extends AbstractCommand   {
			
	static private Logger logger = LogManager.getLogger(ResetPasswordCommand.class.getName());
	
//	static {
//		setCommandDescription("<p>Reset password to all users of the caller's domain. Parameters: domain_id, password</p>");
//	}
//	
	private Domain domain;
	private Serializable domainId;
	private String new_password;
		
	public ResetPasswordCommand() {
			this(null);
	}
	
	public ResetPasswordCommand(String pwd) {
		setName("Reset All Domain Passwords");
		setPriority(SchedulerService.HIGH_PRIORITY);
		setPassword(pwd);
	}
	
	@SuppressWarnings("unused")
	@Override
	public void execute() {
		
		setDateStarted(OffsetDateTime.now());
		
		if (getPassword()==null) {
			setProgress(100);
			setResult("ERROR");
			setResultComments("Password is null");
			setState(CommandState.ERROR);
			setDateTerminated(OffsetDateTime.now());
			return;
		}
		
		int counter=0;
		int errCount = 0;
		
		try {
			
			 List<UserProfile> list = getContentDao().findUserProfileByDomain(getDomain());
			 int size = list.size();
			BeansService beans = ServiceLocator.getService(BeansService.class);
			
			SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");

			 StringBuilder str = new StringBuilder();
			 
			 for (UserProfile profile: list) {
				 
				 if (errCount>200)
					 break;
			
				 try {
					 if (profile.getUser()!=null) {
						 
						 if (!profile.getUser().getUserName().startsWith("root@")) {
							 
							  ((KbeeUser) profile.getUser()).setPassword(getPassword());
							    ServiceLocator.getService(SecurityContentMgmtService.class).update(profile.getUser());
							    
							    logger.info(profile.getPersonFirstLastName() + " [ " + String.valueOf(getProgress())+"% ]" );
								 counter++;
								 setProgress(size>0?(int) 100*counter/size:100);
						  }
					 }
				 } 
				 catch (Exception e) {
					 logger.error(e);					 	
				 }
			 }
			
			 if (errCount>200)
					setResult("Error");
			 else {
				setProgress(100);
				setResult("OK");
			 }
			setResultComments("Indexed " +String.valueOf(counter)+" Objects. " + (str.length()>0?(" Error: " +str) :""));
			setState(CommandState.COMPLETED);
			setDateTerminated(OffsetDateTime.now());

		}
		catch (RuntimeException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public void setPassword(String password) {
		this.new_password=password;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
		this.domainId = domain.getId();
	}
	
	public Domain getDomain() {
		if (domain == null) {
			if (domainId == null) {
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else
				domain = getContentDao().findDomainById(domainId);
		}
		return domain;
	}

	public void setDomainId(long longValue) {
		this.domainId=longValue;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		if (getDomain()!=null) {
			str.append(getDomain().getName());
		}
		return str.toString();
				
	}
	
	@Override
	public String getDescription() {
		return "<p>Reset password to all users of the caller's domain. Parameters: domain_id, password</p>";
	}		
	
	private String getPassword() {
		return this.new_password;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

package com.novamens.kbee.content.social;


import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.social.Comment;
import com.novamens.content.social.Report;
import com.novamens.content.social.SocialDao;
import com.novamens.content.social.SocialService;
import com.novamens.content.social.Vote;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.event.EventService;
import com.novamens.kbee.url.UriHelper;


import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailSendServiceRequest;
import kbee.util.PropertiesFactory;

/**-------------------------------------------------------------------
 * 
 * <p>Social service for a {@link Content</p>
 * 
 */
public class KbeeSocialService implements SocialService {

	static private Logger logger = LogManager.getLogger(KbeeSocialService.class.getName());
	
	private final String BRANDING_APP_NAME 		= "brandingAppName";
	private final String EMAIL_SERVICE_NO_REPLY 	= "emailServiceNoReply";
	
	static Map<String, String> spa_labels 				= new HashMap<String, String>();  
	static Map<String, String> eng_labels 				= new HashMap<String, String>();

	private Content content = null;
	private SocialDao dao   = null;
	
	
	
	public KbeeSocialService() {
	}
	
	public KbeeSocialService(Content content) {
		 this.content = content;
	}
	
	
	public int getVotes() {
		try { 
			Long votes = (Long) getContent().getService(PropertyService.class).getProperty("votes");
			if (votes==null) 
				return 0;
			return votes.intValue();
		
		} catch (Exception e) {
		  logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		  return 0;
	  	}
	}

	
	public boolean hasVotedSessionUser() {
		try {
			return !(getSocialDao().findVotesByUser(getContent(), getSessionUser()).isEmpty());
		} catch (Exception e) {
			  logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			  return false;
		  	}
	}
	
	
	@Transactional
	public void addVote() {
		try {
			List<Vote> votes = getSocialDao().findVotesByUser(getContent(), getSessionUser());
			if (votes.isEmpty()) {
				KbeeVote vote = new KbeeVote(getContent(), getSessionUser(),1);
				getSocialDao().save(vote);
				getContent().getService(PropertyService.class).setProperty("votes", getVotes()+1);
				ServiceLocator.getService(EventService.class).fire(new KbeeSocialEvent(getContent(), vote, getSessionUser()));
			}
		} catch (Exception e) {
  		  logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
  	  	}
	}
	

	@Transactional
	public void update(Comment comment) throws ContentMgmtException   {
	
		 		if (!getContent().isHeadVersion()) {
					Content con= getContentDao().findContentByOId(getContent().getOId());
					comment.setReferencedContent(con);
				}
				else {
					comment.setReferencedContent(getContent());
				}
		 		getSocialDao().save(comment);
		 		
	}

	/**
	 * if the comment has responses it is marked as deleted.
	 * 
	 * REVISAR
	 * }
	 */
	@Transactional
	public void delete(Comment comment) throws ContentMgmtException {
		try {
			getContentDao().delete(comment);
		} catch (ContentMgmtException e) {
			  throw(e);
		} catch (Exception e2) {
			  logger.error(e2.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}


	@Transactional
	public Comment addComment(String text, Comment parent) throws ContentMgmtException  {
		return addComment(text, null, null);
	}


	@Transactional
	public Comment addComment(String text, Long site_oid, Comment parent) throws ContentMgmtException {
		try {

			Comment comment = (Comment)ServiceLocator.getService(ContentFactoryService.class).create("Comment", false, true);
			comment.setText(text);
			
			if (parent!=null) {
				comment.setState(ObjectState.DRAFT);
				comment.setParent(parent);
			}
		
		 	if (!getContent().isHeadVersion()) {
				Content con = getContentDao().findContentByOId(getContent().getOId());
				comment.setReferencedContent(con);
			}
			else {
				comment.setReferencedContent(getContent());
			}

			 if (site_oid!=null)
				 comment.setSiteOId(site_oid);
		
			comment.setDomain(getContent().getDomain());
			comment.setDateSubmitted(OffsetDateTime.now());	
			comment.setUser(getSessionUser());
			getSocialDao().save(comment);
			
			// EVENT
		 	//
			ServiceLocator.getService(EventService.class).fire(new KbeeKnowledgeSharingEvent(getContent(), comment));
			
			return comment;
		}
		catch (ContentCreationException e) {
			logger.error("addComment ");	
			throw new ContentMgmtException(e);
		}
	}


	public List<Comment> getComments() {
		try {
			return getSocialDao().findCommentsByContent(getContent());
		} catch (Exception e2) {
			  logger.error(e2.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			  return null;
		}
		
	}
	

	public Content getContent() {
		return content;
	}
	

	public User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	
	@Transactional
	public void report() {
		
		try {
				List<Report> reports = getSocialDao().findReportsByUser(getContent(), getSessionUser());
				
				if ( reports.isEmpty()) {
					BeansService beans = ServiceLocator.getService(BeansService.class);
					ContentDao dao = (ContentDao) beans.getBean("contentDao");
					double level = dao.findUserProfileByUser(getSessionUser()).getConfidenceLevel();			 
					KbeeReport report = new KbeeReport(getContent(), getSessionUser(), (int) level);
					getSocialDao().save(report);
					getContent().getService(PropertyService.class).setProperty("reports", getReports()+(int) level);
		
					String url = UriHelper.getInstance().getUri(getContent());
		
					// EVENT
					//
					//
					ServiceLocator.getService(EventService.class).fire(new KbeeSocialEvent(getContent(), report, getSessionUser(), url));
				}
		} catch (RuntimeException e) {
  		  logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
  	  }
		
	}


	
	public int getReports() {
		Long reports = (Long)getContent().getService(PropertyService.class).getProperty("reports");
		if (reports==null) 
			return 0;
		return reports.intValue();
	}

	@Override
	public List<Comment> getCommentsResponses(Comment comment) {
		return getSocialDao().findCommentsResponses(comment);
	}


	
	@Override
	public int getTotalComments() {
		try {
			return getSocialDao().getTotalComments(getContent());
		} catch (Exception e2) {
			  logger.error(e2.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			  return 0;
		}
	}
	
	
	@Override
    public void notifyReportComment(Comment comment, String to_email) {
										
   	  try{

			     if (to_email!=null) {
			    	 												
		 				String subject =  "Comentario de: "+ comment.getUser().getDisplayName() +" - Reportado en: " + comment.getReferencedContent().getTitle();
		 				UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
						User caller = userProfile.getUser();
												
						String FROM = comment.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
						
						
						String APPLICATION = ServiceLocator.getService(BrandingService.class).getProductKey();
						
						if (FROM==null)
							FROM	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.notification.noreplyemailaddress", "noreply@novamens.com");
				
		 			 	StringBuilder msg = new StringBuilder();
						msg.append(caller.getDisplayName() + " ha reportado el siguiente comentario: \n");
															
		 				msg.append("\nnAutor: " + comment.getUser().getFirstLastName());
		 				msg.append("\nComentario: " + comment.getText());
						msg.append("\nContenido: " + comment.getReferencedContent().getTitle());
		 				msg.append("\n\nReportado por: " + userProfile.getPersonFirstLastName());
		 				msg.append("\n\n");
		 				
		 				
		 				
		 		 		ServiceLocator.getService(EmailService.class).send(new EmailData( FROM + " ("+ APPLICATION +")", to_email,  subject, msg.toString(), null, "Comment Report"), userProfile.getDomain());
		
			     }
    	  } catch (Exception e) {
    		  logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
    	  }
  	}
	
	
	/**
	 *  TODO: Revisar que se notifica correctamente al responder un comentario.
	 * 
	 */
	@Override
	public void notifyCommentResponded(Comment parent, Comment response, User user_response) {
		
		try { 

			SchedulerService scheduler = ServiceLocator.getService(SchedulerService.class);
			
			User parent_user = parent.getUser();
			UserProfile up = getContentDao().findUserProfileByUserId(parent_user.getId());
			
			if (!up.isEmailNotifications()) {
				logger.info(up.getPersonFirstLastName() + " has disabled notifications.");
				return;
			}
			
			UserProfile up_res = getContentDao().findUserProfileByUserId(user_response.getId());
			
			// Map<String, String> labels = (parent_user.getLocale().equals(Locale.ENGLISH)?eng_labels : spa_labels);
			
			String APPLICATION = up.getDomain().getService(DomainSettingsService.class).get(BRANDING_APP_NAME);

			if (APPLICATION==null)
				APPLICATION	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.application", APPLICATION);

			String FROM = up.getDomain().getService(DomainSettingsService.class).get(EMAIL_SERVICE_NO_REPLY);
			
			if (FROM==null)
				FROM	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.notification.noreplyemailaddress", APPLICATION.equals("kbee")?"noreply@novamens.com":"noreply@realpage.com");
			
			String title 		= up_res.getPersonFirstLastName() + " respondió a su comentario "; 
			String to 			= up.getPerson().getEmail();
			String subject 		= title;
			String msg 			= response.getText().toString();
			String from_email 	= FROM;
			
			EmailData edata = new EmailData(from_email, to, subject, msg, "comment-responded-"+parent.getId().toString());
			
			edata.setUserId((new ObjectId(user_response)).toString());
			edata.setObjectId((new ObjectId((Comment) response)).toString());
			
			EmailSendServiceRequest req= new EmailSendServiceRequest(edata, up.getDomain());
			scheduler.enqueue(req);						
			
		} 
		catch (Exception e) {
			 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
		

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	private SocialDao getSocialDao() {
		if (dao==null)	 {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 dao = (SocialDao) beans.getBean("socialDao");
		 }
		return dao;
	}


}

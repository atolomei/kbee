package com.novamens.kbee.content.distribution;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectID;
import com.novamens.email.EmailService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderSendContent;
import kbee.util.logging.Logger;

public class DistributionServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(DistributionServiceRequest.class.getName());

	private Long contentId;
	private transient Content content = null;
	
	public DistributionServiceRequest(Content content) {
		contentId = (Long)content.getId();
		super.setDescription(DistributionServiceRequest.this.getClass().getSimpleName() + " [ " + (contentId!=null? String.valueOf(contentId):"null"));
		try {
			setObjectID(new ObjectID(content).toString());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}


	public void execute() {
		ServiceLocator.getService(SecurityService.class).authenticate("root@"+getContent().getDomain().getName());
		try {
			for (Classification classification :  getContent().getClassification(getDistributionClassifier())) {
				sendByEmailTo( getContent() , classification.getDataSetMember());
			}
		}
		catch(Exception e) {
			logger.error(e);
		}
	}
	
	public Content getContent() {
		if (content==null) {
			content = (Content)getContentDao().findContentById(contentId);
		}
		return content;
	}
	
	public Classifier getDistributionClassifier() {
		if (getContent()==null)
			return null;
		for (Classification classification : getContent().getClassification()) {
			if (classification!=null && classification.getClassifier().isDistribution()) 
				return classification.getClassifier();
		}
		return null;
	}
	
	private void sendByEmailTo(Content content, DataSetMember member) {
		
		if (!(member instanceof PersonMember)) 
			return;
		
		PersonMember person = (PersonMember)member;
		String email = person.getEmail();
		
		if (email==null) 
			return;
		
		EmailBuilderSendContent builder = new EmailBuilderSendContent();
		builder.setSender(getUser());
		builder.setContent(content);
		builder.setTo(email);
		String lang = person.getProfile(UserProfile.class)!=null ?
			person.getProfile(UserProfile.class).getUser().getLocale().getLanguage() :
			content.getDomain().getLocale().getLanguage();
		builder.setLanguage(lang);
		builder.setParameter("public-url", getPublicUrl(content, person));
		builder.setParameter("registration-url", getRegistrationUrl(person));
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	private String getRegistrationUrl(PersonMember person) {
		KbeeJson data = new KbeeJson();
		data.put("id", String.valueOf(person.getId()));
		data.put("date", person.getCreationOffsetDateTime().toString());
		data.put("domain", String.valueOf(person.getDomain().getId()));
		return person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	private String getPublicUrl(Content content, PersonMember person) {
		return content.getService(UrlService.class).getPublicUrl(person);
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
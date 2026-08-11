package com.novamens.kbee.content.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.TextChange;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectID;
import com.novamens.email.EmailService;
import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderSendContent;
import kbee.util.logging.Logger;

public class TextChangeServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(TextChangeServiceRequest.class.getName());

	private Long contentId;
	private transient Content content = null;
	
	public TextChangeServiceRequest(Content content) {
		contentId = (Long)content.getId();
		super.setDescription(TextChangeServiceRequest.this.getClass().getSimpleName() + " [ " + (contentId!=null? String.valueOf(contentId):"null"));
		try {
			setObjectID(new ObjectID(content).toString());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}


	public void execute() {
		ServiceLocator.getService(SecurityService.class).authenticate("root@"+getContent().getDomain().getName());
//		try {
			for (ContentLink link : getLinks()) {
				sendByEmailTo(link.getSource(), link.getTarget(), getOwners(link.getSource()));
			}
				
//		}
//		catch(Exception e) {
//			logger.error(e);
//		}
	}
	
	public Content getContent() {
		if (content==null) {
			content = (Content)getContentDao().findContentById(contentId);
		}
		return content;
	}
	
	public List<ContentLink> getLinks() {
		
//		try {
			List<ContentLink> links = new ArrayList<>();
			Set<Content> linked = new HashSet<>();
			Content content = getContent();
			KbeeText text = (KbeeText)content.getService(ContentService.class).getText();
			if (text!=null) {
				List<TextChange> changes = content.getService(ContentService.class).getTextChanges();
				if (changes!=null && !changes.isEmpty()) {
					for (ContentLink reverselink : content.getReverseLinks()) {
						if (!linked.contains(reverselink.getSource())) {
							linked.add(reverselink.getSource());
							links.add(reverselink);
						}
					}
				}
			}
			return links;
//		}
//		catch(Exception e) {
//			logger.error(e);
//			throw new KbeeRuntimeException(e);
//		}
	}
	
	private void sendByEmailTo(Content content, Content target, List<Person> receivers) {
		
		if (receivers.isEmpty())
			return;
		
		Person person = receivers.get(0);
		
		EmailBuilderSendContent builder = new EmailBuilderSendContent();
		builder.setSender(getUser());
		builder.setContent(content);
		
		String targeturl = target.getService(UrlService.class).getUrl(true);
		//String text = "<span>El contenido vinculado <a href=\""+targeturl+"\">"+target.getTitle()+"</a> fue modificado </span>";
		
		
		String date = ServiceLocator.getService(DateTimeService.class).format(target.getLastModifiedOffsetDateTime());
		String text = "<span>El contenido vinculado <a href=\""+targeturl+"\">"+target.getTitle()+"</a> fue modificado el "+date+ " por " + target.getLastModifiedUser().getDisplayName()+ "</span>";
		
		builder.setText(text);
		
		builder.setReceivers(receivers);
		String lang = person.getProfile(UserProfile.class)!=null ?
			person.getProfile(UserProfile.class).getUser().getLocale().getLanguage() :
			content.getDomain().getLocale().getLanguage();
		builder.setLanguage(lang);
		builder.setParameter("public-url", getPublicUrl(content, person));
		//builder.setParameter("registration-url", getRegistrationUrl(person));
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
//	private String getRegistrationUrl(PersonMember person) {
//		KbeeJson data = new KbeeJson();
//		data.put("id", String.valueOf(person.getId()));
//		data.put("date", person.getCreationOffsetDateTime().toString());
//		data.put("domain", String.valueOf(person.getDomain().getId()));
//		return person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
//	}
	
	private List<Person> getOwners(Content content) {
		List<Person> owners = new ArrayList<>();
		for (User user : getUsers(content)) {
			UserProfile profile = getContentDao().findUserProfileByUser(user);
			owners.add(profile.getPerson());
		}
		return owners;
	}
	
	private List<User> getUsers(Content content) {
		List<User> users = new ArrayList<>();
		for (Principal principal : ServiceLocator.getService(ContentSystemSecurityService.class).getEnabledPrincipals(content, KbeePermission.WRITE)) {
			if (principal instanceof User) {
				users.add((User)principal);
			}	
			else if (principal instanceof Group)	{
				for (Principal member : ((KbeeGroupProxy)principal).getMembers()) {
					if (member instanceof User) {
						users.add((User)member);
					}
				}
			}
		}
		return users;
	}
	
	private String getPublicUrl(Content content, Person person) {
		return content.getService(UrlService.class).getPublicUrl(person);
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
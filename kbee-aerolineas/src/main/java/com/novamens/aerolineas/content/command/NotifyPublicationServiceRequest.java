package com.novamens.aerolineas.content.command;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.Role;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dom.ObjectID;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderPublishEventENotiRule;

public class NotifyPublicationServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(NotifyPublicationServiceRequest.class.getName());

	private Long contentId;
	private transient Content content = null;
	
	public NotifyPublicationServiceRequest(Content content) {
		contentId = (Long)content.getId();
		super.setDescription(NotifyPublicationServiceRequest.this.getClass().getSimpleName() + " [ " + (contentId!=null? String.valueOf(contentId):"null"));
		try {
			setObjectID(new ObjectID(content).toString());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void execute() {
		ServiceLocator.getService(SecurityService.class).authenticate("root@"+getContent().getDomain().getName());
		// No se envia la notificación si el documento no esta vigente
		if (getContent().getService(ContentService.class).isValid()) {
			for (Classification classification : getContent().getClassification()) {
				if (classification.getClassifier().getAlias().equals("acuserecibo")) {
					for (Person user : getUsers(getRole("acuse"), classification.getDataSetMember())) {
						
						if (isReadable(user)) {
							notifyUserWaitingRecepit(user, false);
						}
					}
				}
				if (classification.getClassifier().getAlias().equals("distribucion")) {
					for (Person user : getUsers(getRole("receptor"), classification.getDataSetMember())) {
						if (isReadable(user)) {
							notifyUser(user, true);
						}
					}
				}
			}
		}	
	}
	
	public void notifyUser(Person user, boolean deleteonaccept) {
		notifyByAlert(user, true);
		notifyByEmail(user, "domain");
	}
	
	public void notifyUserWaitingRecepit(Person user, boolean deleteonaccept) {
		notifyByAlert(user, false);
		notifyByEmail(user, "requires-accept");
	}
	
	public Content getContent() {
		if (content==null) {
			content = (Content)getContentDao().findContentById(contentId);
		}
		return content;
	}
	
	
	private Role getRole(String alias) {
		for (Role role : getSecurityDao().getRoles(getContent().getDomain())) {
			if (alias.equals(role.getAlias())) {
				return role;
			}
		}
		return null;
	}
	
	private Set<Person> getUsers(Role role, DataSetMember member) {
		Set<Person> users= new HashSet<Person>();
		if (!(member instanceof EntityMember)) return users;
		EntityMember entity = (EntityMember)member;
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(entity)) {
			if (userRole.getRole().equals(role)) {
				users.add(userRole.getPerson());
			}
		}
		return users;
	}
	
	private void notifyByEmail(Person subscriber, String ruletype) {
		
		
		Person publisher = getContentDao().findUserProfileByUser(getContent().getLastModifiedUser()).getPerson();
		KbeeENotiRule rule = new KbeeENotiRule();
		rule.setDomain(subscriber.getDomain());
		rule.setKey(ruletype);
		rule.setName("name");
		rule.setCondition("condition");
		rule.setDescription("description");
		rule.setOwner(getUser(subscriber));
		rule.setLastModifiedUser(getUser());
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		
		EmailBuilderPublishEventENotiRule builder = new EmailBuilderPublishEventENotiRule(rule, getContent(), publisher, subscriber);
		ServiceLocator.getService(EmailService.class).send(builder);
		
		//ServiceLocator.getService(EmailService.class).sendPublishContentRuleNotification(rule, getContent(), publisher, user);
		
		
	}
	
	private boolean isReadable(Person person) {
		UserProfile userprofile = person.getProfile(UserProfile.class);
		if (userprofile==null) return false;
		User user = userprofile.getUser();
		if (user==null) return false;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(getContent(), user);
	}	
	
	private void notifyByAlert(Person user, boolean deleteonaccept) {
		ServiceLocator.getService(ContentFactoryService.class).createContentPublishNotification(getContent(), getUser(user), deleteonaccept);
	}

	private User getUser(Person person) {
		UserProfile userProfile = person.getProfile(UserProfile.class);
		User user = userProfile.getUser();
		return user;
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
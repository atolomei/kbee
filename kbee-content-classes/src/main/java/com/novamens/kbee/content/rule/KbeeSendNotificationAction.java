package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.rule.SendNotificationAction;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.logging.NotificationEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderSendConditionNotification;

public class KbeeSendNotificationAction extends KbeeAbstractAction implements SendNotificationAction, Serializable {

	private static final long serialVersionUID = 1L;
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSendNotificationAction.class.getName());
	static private Logger txLogger = LogManager.getLogger("TxLogger");
	
	private String text;
	private String subtitle;
	
	private Serializable roleId; 
	private String personIds;
	
	
	/**
	 * <p>requires to be a bean</p>
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Content content) {
		
		logger.debug(this.getClass().getSimpleName() +" -> " + content.getTitle() + " (id:" + content.getId().toString() + ")");
		
		Map<Serializable, Serializable> map = new HashMap<Serializable, Serializable>();
		
		Role role = getRole();

		if (role!=null) {
			Set<Person> users = (role instanceof EntityRole) ? getUsers(role, content) : getUsers(role);
			for (Person p : users) {
				if (!map.containsKey(p.getId())) {
					notifyPerson(content, p);
					map.put(p.getId(), p.getId());
				}
			}
		}
		
		if (personIds!=null) {
			List<Person> noti=getNotifyPersonList();
			for (Person p: noti) {
				if (!map.containsKey(p.getId())) {
					notifyPerson(content, p);
					map.put(p.getId(), p.getId());
				}
			}	
		}
			
		txLogger.info(new NotificationEvent(content, getSubtitle()));
		return content;
	}


	
	public void notifyPerson(Content content, Person p) {
		notifyByEmail(content, p);
		notifyByAlert(content, p);
	}


	
	@Override
	public Role getRole() {
		if (roleId==null) 
			return null;
		
		Role role;
		
		synchronized (this) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			role = (Role)sf.getCurrentSession().load(KbeeAbstractRole.class, this.roleId);
		}
		return role;
	}

	
	@Override
	public void setRole(Role role) {
		this.roleId = role.getId();
	}
	
	
	
	@Override
	public void setText(String template) {
		this.text = template;
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public void setSubtitle(String subject) {
		this.subtitle = subject;
	}
	
	@Override
	public String getSubtitle() {
		return subtitle;
	}
	
	public boolean justOneTime() {
		return true;
	}
	

	
	
	
	@Override
	public List<Person> getNotifyPersonList() {
		List<Person> list = new ArrayList<Person>();
		if (personIds==null)
			return list;
		String ids[]=personIds.split("#");
		for (String s: ids) {
			Person p= getContentDao().findPersonById(Long.valueOf(s));
			if (p!=null)
				list.add(p);
		}
		return list;
	}

	
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (getActionRuleName()!=null)
			str.append(separator(str) + "Name: " + getActionRuleName());
		
		if (this.roleId!=null)
			str.append(separator(str) + "Role Id: " + roleId.toString());
		
		if(this.personIds!=null) 
			str.append(separator(str) + "User Ids:" + personIds.toString());
			
		if(this.subtitle!=null) 
				str.append(separator(str) + "Subtitle:" + subtitle);
				
		return str.toString();
	}

	
	private Set<Person> getUsers(Role role, Content content) {
		Set<Person> users = new HashSet<Person>();
		Classifier classifier = ((EntityRole)role).getClassifier();
		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().equals(classifier)) {
				users.addAll(getUsers(role, classification.getDataSetMember()));
				break;
			}
		}
		return users;
	}

	private Set<Person> getUsers(Role role, DataSetMember member) {
		Set<Person> users= new HashSet<Person>();
		if (!(member instanceof EntityMember)) return users;
		EntityMember entity = (EntityMember)member;
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(entity)) {
			if (role==null || role.equals(userRole.getRole())) {
				users.add(userRole.getPerson());
			}
		}
		return users;
	}
	
	private Set<Person> getUsers(Role role) {
		Set<Person> users = new HashSet<Person>();
		if (!(role instanceof DomainRole)) return users;
		for (UserRole userRole :  getSecurityDao().findUserRolesByRole(role)) {
			if (role==null || role.equals(userRole.getRole())) {
				users.add(userRole.getPerson());
			}
		}
		return users;
	}
	
	
	private void notifyByEmail(Content content, Person receiver) {

		// this is a kind of  "support" ENotiRule
		//
		KbeeENotiRule rule = new KbeeENotiRule();
		rule.setDomain(receiver.getDomain());
		rule.setDescription("Rule generated by " + this.getClass().getSimpleName() + " for ActionRule " + getActionRuleName());
		rule.setRuleSource(ENotiRule.SOURCE_TIME_DEPENDENT_RULE);
		rule.setSubject(getSubtitle());
		rule.setText(getText());
		rule.setName(getActionRuleName());
		rule.setActionRuleId(getActionRuleId());
		rule.setActionRuleName(getActionRuleName());
		rule.setLastModifiedUser(getUser());
		rule.setOwner(getUser());
		
		EmailBuilderSendConditionNotification builder= new EmailBuilderSendConditionNotification (rule, content, getPerson(getUser()), receiver);
		builder.setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		
		ServiceLocator.getService(EmailService.class).send(builder);
		//ServiceLocator.getService(EmailService.class).sendConditionNotification(rule, content, getPerson(getUser()), receiver);
	}
	
	private void notifyByAlert(Content content, Person user) {
		ServiceLocator.getService(NotificationService.class).sendNotification(NotificationType.CONDITION, content, getText(), getUser(user));
	}
	
	private User getUser(Person person) {
		UserProfile userProfile = person.getProfile(UserProfile.class);
		User user = userProfile.getUser();
		return user;
	}
	
	private Person getPerson(User user) {
		Person person = null;
		UserProfile userProfile = getContentDao().findUserProfileByUser(user);
		if (userProfile!=null) {
			person = (Person)userProfile.getEntity();
		}
		return person;
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private String separator(StringBuilder str) {
		if (str.length()>0)
			return " | ";
		return "";
	}

	
	public void setNotifyPersonList(List<Person> list) {
		if (list==null)
			return;
		this.personIds = list.stream().map(s -> s.getId().toString()).collect(Collectors.joining("#"));
	}
	
	public String getNotifyPersonListString() {
		return this.personIds;
	}



	public void setNotifyPersonListString(String personIds2) {
		this.personIds = personIds2;
	}

	

}
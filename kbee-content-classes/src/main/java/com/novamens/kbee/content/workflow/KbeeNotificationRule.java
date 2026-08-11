package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.workflow.NotificationRule;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.security.KbeeAbstractRole;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.email.EmailBuilderWorkflowPostTerminationAlert;

public class KbeeNotificationRule implements NotificationRule, Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<Serializable> roles;
	private String text;
	
	public KbeeNotificationRule() {
	}

	
	public void execute(WorkflowContext context) {
		
		Content content = ((KbeeContext)context).getContent();
		
		 List<Role> list = getReceivers();
		 
		for (Role role : list) {
			for (User user : getUsers(role, content)) {
				// NotifyByAlert(content, user); // alerts are not sent 
				notifyByEmail(context, content, user);
			}
		}
	}
	
	
	public List<Role> getReceivers() {
	
		List<Role> roles = new ArrayList<Role>();
		
		if (this.roles==null) 
			return null;
		
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		
		for (Serializable roleId : this.roles) {
			Role role = (Role)sf.getCurrentSession().load(KbeeAbstractRole.class, Long.valueOf((String)roleId));
			Class<?> clazz = Hibernate.getClass(role);
			role = (Role)sf.getCurrentSession().load(clazz, Long.valueOf((String)roleId));
			roles.add(role);
		}
		return roles;
	}
	
	public void setReceivers(List<Role> roles) {
		this.roles=new ArrayList<Serializable>();
		for (Role role : roles) {
			this.roles.add(String.valueOf(role.getId()));
		}
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getDescription() {
		return "";
	}
	
	private Set<User> getUsers(Role role, Content content) {
		Set<User> users= new HashSet<User>();
		if (role.isEntity()) {
			Classifier classifier = ((EntityRole)role).getClassifier();
			for (Classification classification : content.getClassification(classifier)) {
				if (classification!=null && classification.getDataSetMember()!=null) {
					users.addAll(getUsers(role, classification.getDataSetMember()));
				}
			}
		}
		else {
			for (UserRole userRole : getSecurityDao().findUserRolesByRole(role)) {
				users.add(userRole.getUser());
			}
		}
		return users;
	}
	
	private Set<User> getUsers(Role role, DataSetMember member) {
		Set<User> users= new HashSet<User>();
		if (!(member instanceof EntityMember)) return users;
		EntityMember entity = (EntityMember)member;
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(entity)) {
			if (userRole.getRole().equals(role)) {
				users.add(userRole.getUser());
			}
		}
		return users;
	}
	

	protected void notifyByAlert(Content content, User user) {
		ServiceLocator.getService(NotificationService.class).sendNotification(NotificationType.WORKFLOW, content, getText(), user);
	}
	
	
	protected void notifyByEmail(WorkflowContext context, Content content, User user_receiver) {
		Person task_executer = getContentDao().findUserProfileByUser(content.getLastModifiedUser()).getPerson();
		Person receiver 	 = getContentDao().findUserProfileByUser(user_receiver).getPerson();
		
		EmailBuilderWorkflowPostTerminationAlert builder = new EmailBuilderWorkflowPostTerminationAlert(context, content, task_executer, receiver, getText());
		builder.setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		
		ServiceLocator.getService(EmailService.class).send(builder);

		
		//ServiceLocator.getService(EmailService.class).sendWorkflowNotification(context, content, task_executer, receiver, getText());
	}
	
	
	protected User getSessionUser() {
		return  ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}
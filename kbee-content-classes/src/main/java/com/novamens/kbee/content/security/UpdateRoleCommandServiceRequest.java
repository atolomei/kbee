package com.novamens.kbee.content.security;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserRole;
import com.novamens.dom.ObjectID;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

public class UpdateRoleCommandServiceRequest extends AbstractServiceRequest {
			
	private static final long serialVersionUID = 1L;
	
	private Long roleId;
	private transient KbeeAbstractRole role = null;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UpdateRoleCommandServiceRequest.class.getName());

	
	public UpdateRoleCommandServiceRequest(Role role) {
		roleId = ((KbeeAbstractRole)role).getId();
		try {
			setObjectID(new ObjectID(role).toString());
		} catch (Exception e) {
			logger.error(e);
		}
		
		super.setDescription(UpdateRoleCommandServiceRequest.this.getClass().getSimpleName() + " [ " + (roleId!=null? String.valueOf(roleId):"null"));
	}
	
	public void execute() {
		Assert.isTrue(getRole()!=null, "no role");
		try {
			for (UserRole userrole : getUsers()) {
				updateRole(userrole);
			}
		}
		finally {
			//com.novamens.hibernate.session.Session.close();
		}
	}
	
	public KbeeAbstractRole getRole() {
		if (role==null) {
			role = (KbeeAbstractRole)getSecurityDao().findRoleById(roleId);
			((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().evict(role);
			role = (KbeeAbstractRole)getSecurityDao().findRoleById(roleId);
		}
		return role;
	}
	
	protected void updateRole(UserRole userRole) {
		if (userRole.getPerson()!=null) {
			getRole().setRole(userRole.getPerson(), userRole.getEntity());
		}
		else {
			userRole = (UserRole)getContentDao().reload(userRole);
			if (userRole.getPerson()!=null) {
				getRole().setRole(userRole.getPerson(), userRole.getEntity());
			}
		}
	}
	
	private List<UserRole> getUsers() {
		return getSecurityDao().findUserRolesByRole(getRole());
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return	(ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}

package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.service.ServiceLocator;

public class KbeeRolesService implements RolesService {
			
	static private Logger txlogger = LogManager.getLogger("TxLogger");
																												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeRolesService.class.getName());
	
	private Person person  = null;

	public KbeeRolesService() {
	}

	public KbeeRolesService(Person person) {
		this.person = person;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void update(List<UserRole> roles) {
		List<UserRole> newroles = roles.stream().
			filter((role) ->!getProfile().getRoles().contains(role)).
			collect(Collectors.toList());
		
		List<UserRole> deletedroles = getProfile().getRoles().stream().
				filter((role) ->!roles.contains(role)).
				collect(Collectors.toList());

		if (logger.isDebugEnabled()) {
			roles.forEach( item -> logger.debug(item.toString()));
		}
		
		getProfile().setRoles(roles);
		
		if (!newroles.isEmpty() || !deletedroles.isEmpty()) {
			ServiceLocator.getService(SecurityContentMgmtService.class).update(person, (String)null);
		}
		for (UserRole role : newroles) {
			txlogger.info(new SecurityUpdateEvent(getProfile().getUser(), "Add Role "+role.getDisplayName()));
		}
		for (UserRole role : deletedroles) {
			txlogger.info(new SecurityUpdateEvent(getProfile().getUser(), "Remove Role "+role.getDisplayName()));
		}
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void add(UserRole role) {
		List<UserRole> roles = new ArrayList<>();
		roles.addAll(getProfile().getRoles());
		if (!roles.contains(role)) {
			roles.add(role);
			update(roles);
		}
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(UserRole role) {
		List<UserRole> roles = new ArrayList<>();
		roles.addAll(getProfile().getRoles());
		if (roles.contains(role)) {
			roles.remove(role);
			update(roles);
		}
	}
	
	@Override
	public boolean isAdministrator(EntityMember entity) {
		for (UserRole userRole : getProfile().getRoles()) {
			if (entity.equals(userRole.getEntity()) && userRole.getRole().isAdministrator()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isAdministrator(DataSet dataset) {
		for (UserRole userRole : getProfile().getRoles()) {
			if (userRole.getRole().isEntity() && userRole.getRole().isAdministrator()) {
				return true;
			}
		}
		return false;
	}
	
	public UserProfile getProfile() {
		return person.getProfile(UserProfile.class);
	}
	
	protected String getDisplayName(UserRole role) {
		return "(" + (role.getEntity()!=null ? role.getEntity().getDisplayName():"") + ")";
	}
}

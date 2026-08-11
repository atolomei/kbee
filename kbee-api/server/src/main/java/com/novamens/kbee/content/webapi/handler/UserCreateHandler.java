package com.novamens.kbee.content.webapi.handler;

import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.security.Role;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.ObjectState;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;

public class UserCreateHandler extends  UserUpdateHandler {
	
	@Transactional
	public ITransaction create(ApiUser user) {
		return update(user);
	}
	
	
	@Override
	protected Person getPerson(ApiUser user) throws ContentMgmtException {
		Person person = null;
		person = createUser(user);
		user.setId(String.valueOf(person.getId()));
		getContentDao().save(person);
		return person;
	}
	

	private Person createUser(ApiUser user) throws ContentMgmtException {
		String username = getValidUserName(user);
		Person person = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser(user.getFirstName(),
				user.getLastName(),
				user.getEmail(),
				username,
				ObjectState.ENABLED,
				true, // para que es
				new HashSet<Group>(), // groups
				null,				  // canonical groups (in addition of default) 	 
				new ArrayList<Role>()); // Role 
		return person;
	}
}
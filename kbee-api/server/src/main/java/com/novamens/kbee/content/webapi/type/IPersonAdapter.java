package com.novamens.kbee.content.webapi.type;

import com.novamens.content.entity.Person;

import kbee.api.model.IPerson;

public class IPersonAdapter implements Adapter<Person, IPerson> {
	
	//private static Logger logger = Logger.getLogger(IPersonAdapter.class.getName());
	
	public IPersonAdapter() {
	}

	public IPerson adapt(Person person) {
		
		IPerson iperson = new IPerson();
		
		iperson.setId(String.valueOf(person.getId()));
		iperson.setState(person.getState().name());
		iperson.setDisplayName(person.getDisplayName());
		iperson.setDomain(person.getDomain().getName());
		iperson.setLastModifiedUser(new ApiUserProxy(person.getLastModifiedUser()));
		
		iperson.setFirstName(person.getFirstName());
		iperson.setLastName(person.getLastName());
		iperson.setEmail(person .getEmail());
		
		return iperson;	
	}
	
}

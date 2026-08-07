package com.novamens.content.web.suggestion.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.entity.Profile;
import com.novamens.content.model.ObjectId;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class PersonProxy implements Person, Proxy<Person> {
	private ObjectId id;
	private String displayName;
	
	static private Logger logger = LogManager.getLogger(PersonProxy.class.getName());
	
	public PersonProxy(ObjectId id) {
		this.id = id;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String value) {
		this.displayName = value;
	}
	
	public Serializable getId() {
		return null;
	}
	
	public String getName() {
		return null;
	}
	
	public String getFirstName() {
		return null;
	}
	
	public void setFirstName(String name) {
		
	}
	
	public String getLastName() {
		return null;
		
	}
	
	public void setLastName(String surname) {
		
	}
	
	public String getLastFirstName() {
		return null;
		
	}
	
	public String getEmail() {
		return null;
		
	}
	
	public void setEmail(String email) {
		
	}

	public String getPhone() {
		return null;
		
	}
	
	public void setPhone(String phone) {
		
	}
	
	public String getDescription() {
		return null;
		
	}
	
	public void setDescription(String desc) {
		
	}
	
	public LocalDate getBirthDate() {
		return null;
	}
	
	public void setBirthDate(LocalDate date) {
		
	}
	
	public KBFile getPhoto() {
		return null;
	}
	
	public void setPhoto(KBFile file) {
		
	}
	
	@Override
	public void setAddress(String address) {
	}

	@Override
	public String getAddress() {
		return null;
	}

	public String getFirstLastName() {
		return null;
	}
	
	public Domain getDomain() {
		return null;
	}
	
	public void setDomain(Domain domain) {
	}
	
	
	public <T extends Profile> T getProfile(Class<T> profileclass) {
		return null;
	}
	
	public List<Profile> getProfiles() {
		return null;
	}

	public void setId(Serializable id) {
		
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return null;
	}

 	
	public void setState(ObjectState enabled) {
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		return null;
	}
	
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}
	
	public Date getLastModifiedDate() {
		return null;
	}
	
	public void setLastModifiedDate(Date lastModifiedDate) {
	}
	
	public Date getCreationDate() {
		return null;
	}
	
	public void setCreationDate(Date lastModifiedDate) {
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return null;
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
	}
	
	public User getLastModifiedUser() {
		return null;
	}
	
	public void setLastModifiedUser(User lastModifiedUser) {
		
	}
	
	public Person getObject() {
		Person person = null;
		try {
			person = (Person)getContentDao().findObjectById(id);
		}
		catch (ContentMgmtException e) {
			logger.error(e);
		}
		return person;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public String getWorkPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkPosition(String pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBusinessTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPhotoDomainLogo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPhotoDomainLogo(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEmailValidated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEmailValidated(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIsEmailValidated(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDefaultPhoto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDefaultPhoto(boolean b) {
		// TODO Auto-generated method stub
		
	}


}

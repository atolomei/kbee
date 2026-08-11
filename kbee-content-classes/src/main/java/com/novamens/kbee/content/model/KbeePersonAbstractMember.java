package com.novamens.kbee.content.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;


import javax.persistence.Entity;

import com.novamens.content.entity.Person;
import com.novamens.content.entity.Profile;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.security.User;

@Entity
public abstract class KbeePersonAbstractMember extends KbeeEntityMember implements PersonMember {
	
	public KbeePersonAbstractMember() {
		super();
	}
	
	public KbeePersonAbstractMember(PersonSet dataset) {
		super(dataset);
	}
	
	public KbeePersonAbstractMember(UserSet dataset) {
		super(dataset);
	}
	
	public KbeePersonAbstractMember(Person person, DataSet dataset) {
		super((EntitySet)dataset);
		setValue(person.getDisplayName());
	}
	
	@Override
	public String getDisplayName() {
		return getPerson()!=null ? getPerson().getDisplayName() : "";
	}
	
	public String getFirstName() {
		return getPerson().getFirstName();
	}
	
	public void setFirstName(String name) {
		getPerson().setFirstName(name);
		updateValue();
	}
	
	public String getLastName() {
		return getPerson().getLastName();
	}
	
	public void setLastName(String surname) {
		getPerson().setLastName(surname);
		updateValue();
	}
	
	public String getLastFirstName() {
		return getPerson().getLastFirstName();
	}
	
	public String getEmail() {
		return getPerson().getEmail();
	}
	
	public LocalDate getBirthDate() {
		return getPerson().getBirthDate();
	}
	
	public void setEmail(String email) {
		getPerson().setEmail(email);
	}

	public String getPhone() {
		return getPerson().getPhone();
	}
	
	public void setPhone(String phone) {
		getPerson().setPhone(phone);
	}
	
	public String getDescription() {
		return getPerson().getDescription();
	}
	
	public void setDescription(String desc) {
		getPerson().setDescription(desc);
	}
	
	public KBFile getPhoto() {
		return getPerson().getPhoto();
	}
	
	public void setPhoto(KBFile photo) {
		getPerson().setPhoto(photo);
	}
	
	public <T extends Profile> T getProfile(Class<T> profileclass) {
		return getPerson().getProfile(profileclass);
	}
	
	@Override
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
		if (getPerson()!=null) 
			getPerson().setLastModifiedUser(user);
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		super.setLastModifiedOffsetDateTime(date);
		if (getPerson()!=null) 
			getPerson().setLastModifiedOffsetDateTime(date);
	}
	
	@Override
	public void setDomain(Domain domain) {
		super.setDomain(domain);
		if (getPerson()!=null) 
			getPerson().setDomain(domain);
	}
	
	public void setBirthDate(LocalDate birthDate) {
		getPerson().setBirthDate(birthDate);
	}
	
	@Override
	public String getWorkPosition() {
		if (getPerson()!=null) 
			return getPerson().getWorkPosition();
		return null;
	}

	@Override
	public void setWorkPosition(String pos) {
		if (getPerson()!=null) 
			getPerson().setWorkPosition(pos);
	}
	
	protected void updateValue() {
		setStrValue(getPerson().getFirstLastName());	
	}
	
}

package com.novamens.kbee.content.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.entity.Person;
import com.novamens.content.entity.Profile;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.UserSet;
import com.novamens.kbee.content.entity.KbeeEntity;

@Entity
@DiscriminatorValue(value="3")
public class KbeePersonMember extends KbeePersonAbstractMember {
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity=KbeeEntity.class)
	@JoinColumn(name="ENTITY_ID")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	private com.novamens.content.entity.Entity entity;
	

	public KbeePersonMember() {
		super();
	}

	public KbeePersonMember(PersonSet dataset) {
		super(dataset);
	}
	
	public KbeePersonMember(UserSet dataset) {
		super(dataset);
	}

	public KbeePersonMember(Person person, DataSet dataset) {
		super(person, dataset);
		setPerson(person);
	}
	
	public void setPerson(Person person) {
		this.entity = person;
	}
	
	public Person getPerson() {
		return (Person)this.entity;
	}

	@Override
	public String getFirstLastName() {
		if (getPerson()==null)
			return null;
		return getPerson().getFirstLastName();
	}
	
	@Override
	public String getBusinessTitle() {
		if (getPerson()==null)
			return null;
		return getPerson().getBusinessTitle();
	}
	
	public List<Profile> getProfiles() {
		if (getPerson()==null)
			return null;
		return getPerson().getProfiles();
	}

	@Override
	public boolean isPhotoDomainLogo() {
		if (getPerson()==null)
			return false;
		return getPerson().isPhotoDomainLogo();
	}

	@Override
	public void setPhotoDomainLogo(boolean b) {
		if (getPerson()==null)
			return; 
		getPerson().setPhotoDomainLogo(b);
	}
	
	@Override
	public void setAddress(String address) {
		if (getPerson()==null)
			return;
		getPerson().setAddress(address);
	}

	@Override
	public String getAddress() {
		if (getPerson()==null)
			return null; 
		return getPerson().getAddress();
	}

	@Override
	public boolean isOnlyRootEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmailValidated() {
		if (getPerson()==null)
			return false; 
		return getPerson().isEmailValidated();
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void setEmailValidated(boolean b) {
		if (getPerson()==null)
			return; 
		getPerson().setEmailValidated(b);
		
	}

	@Override
	public void setIsEmailValidated(boolean b) {
		if (getPerson()==null)
			return; 
		getPerson().setEmailValidated(b);
	}

	@Override
	public boolean isDefaultPhoto() {
		return false;
	}

	@Override
	public void setDefaultPhoto(boolean b) {
	}

	
}

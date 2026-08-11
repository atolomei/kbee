package com.novamens.kbee.content.model;

import java.util.List;

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
import com.novamens.content.text.Text;
import com.novamens.kbee.content.entity.KbeeEntity;

@Entity
@DiscriminatorValue(value="4")
public class KbeePersonSubsetMember extends KbeePersonAbstractMember  {
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeEntity.class)
	@JoinColumn(name="ENTITY_ID")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	private com.novamens.content.entity.Entity entity;
	
	public KbeePersonSubsetMember() {
		super();
	}
	
	public KbeePersonSubsetMember(PersonSet dataset) {
		super(dataset);
	}
	
	public KbeePersonSubsetMember(UserSet dataset) {
		super(dataset);
	}
	
	public KbeePersonSubsetMember(Person person, UserSet dataset) {
		super(dataset);
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
	public Text getNotes() {
		return null;
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
	public String getWorkPosition() {
		return null;
	}

	@Override
	public void setWorkPosition(String pos) {
	}

	@Override
	public String getBusinessTitle() {
		if (getPerson()==null)
			return null;
		return getPerson().getBusinessTitle();
	}
	
	@Override
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOnlyRootEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmailValidated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
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

package com.novamens.kbee.content.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.Assert;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSubset;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;

@Entity
@DiscriminatorValue(value="5")
public class KbeeUserSubset extends KbeeDataSet implements UserSubset {
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "GROUP_ID", updatable=false)
	private Group group;
	
	public KbeeUserSubset() {
		super();
	}
	
	public KbeeUserSubset(String name, DataSetType type) {
		super(name, type);
	}

	public DataSetMember createMember() {
		Assert.isTrue(true, "not allowed");
		return null;
	}
	
	public DataSetMember createMember(Person person) {
		KbeePersonSubsetMember member = new KbeePersonSubsetMember(this);
		if (person instanceof PersonMember) 
			person = ((KbeePersonMember)person).getPerson();
		member.setPerson(person);
		return member;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group; 
	}
	
	@Override
	public DataSet clone() {
		KbeeUserSubset clone = new KbeeUserSubset();
		super.clone(clone);
		clone.setGroup(getGroup());
		return clone;
	}
}

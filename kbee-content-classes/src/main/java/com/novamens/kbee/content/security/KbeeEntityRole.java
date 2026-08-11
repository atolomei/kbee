package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.service.DataSetService;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.security.acl.Group;

import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue(value="2") // EntityRole.TYPE
public class KbeeEntityRole extends KbeeAbstractRole implements EntityRole, com.novamens.dom.Indexable {
	
	// TxLogger runs in the same thread as the caller
	static Logger txLogger = LogManager.getLogger("TxLogger");
	
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeClassifier.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="CLASSIFIER_ID")
	private Classifier classifier;
	
	@Getter
	@Setter
	@Column(name = "isadministrator")
	private boolean isAdministrator;
	
	@Column(name = "enable_useradmin")
	private boolean isUserAdmin;
	
	@Getter
	@Setter
	@ManyToMany(fetch = FetchType.EAGER, targetEntity=KbeeDataSet.class)
	@JoinTable(name = "KB_MANAGED_ENTITY", 
		joinColumns = {	@JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "DATASET_ID", nullable = false, updatable = false) })
	List<DataSet> managedEntities = new ArrayList<DataSet>();
	
	public KbeeEntityRole() {
		setType(EntityRole.TYPE);
	}
	
	@Override
	public void setRole(Person person, EntityMember entity) {
		entity.setRole(this, person);
		super.setRole(person, entity);
	}
	
	@Override
	public void removeRole(Person person, EntityMember entity) {
		super.removeRole(person, entity);
		entity.removeRole(this, person);
	}
	

	@Override
	public boolean enableUserAdmin() {
		return isUserAdmin;
	}


	@Override
	public String getRoleType() {
		Locale locale = getSessionUser().getLocale(); 
		ResourceBundle res = ResourceBundle.getBundle(this.getClass().getName(), locale);
		return res.getString("type");
	}
	
	@Override
	public Set<Group> getGroups(EntityMember entity) {
		Set<Group> groups = new HashSet<Group>(); 
		groups.addAll(getGroups());
		
		Group entitygroup = ((KbeeEntityMember)entity).getGroup(this);
		if (entitygroup!=null) groups.add(entitygroup);
		
		if (getGroup()!=null) 
			groups.add(getGroup());
		if (entity.getGroup()!=null) {
			groups.add(entity.getGroup());
		}
		if (entity.getDataSet().isAggregation()) {
			EntityMember aggregator = getAggregator(entity);
			if (aggregator!=null) {
				Group group = aggregator.getGroup();
				if (group!=null) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	@Override
	public String getDisplayName() {
		String name = getName();
		if (getClassifier()!=null) {
			name += " (" + getClassifier().getDisplayName() + ")";
		}
		else {
			name += " (-)";
		}
		return name;
	}
	
	@Override
	public boolean manage(DataSet dataset) {
		return getManagedEntities().contains(dataset);
	}
	
	private EntityMember getAggregator(EntityMember aggregation) {
		for (Classification classification : aggregation.getClassification()) {
			if (classification!=null && classification.getDataSetMember() instanceof EntityMember) {
				EntityMember entity = (EntityMember)classification.getDataSetMember();
				if (includes(entity, aggregation)) {
					return entity;
				}
				
			}
		}
		return null;
	}
	
	private boolean includes(EntityMember entity, EntityMember aggregation) {
		for (DataSet dataset : entity.getDataSet().getService(DataSetService.class).getAggregations()) {
			if (dataset.equals(aggregation.getDataSet())) {
				return true;
			}
		}
		return false;
	}
}

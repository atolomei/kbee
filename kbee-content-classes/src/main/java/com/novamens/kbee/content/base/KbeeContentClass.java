package com.novamens.kbee.content.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
 

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import com.novamens.content.base.ContentClass;
import com.novamens.content.model.Classifier;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeClassifier;
 
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "ContentClass")
@DynamicInsert 
public class KbeeContentClass implements ContentClass {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentClass.class.getName());
	
	@Id  
	@Column(name = "Id")
	private String id;
	
	@Column(name = "name")
	private String name;

	@Column(name = "enabled")
	private boolean isenabled = true;
	
	@Column(name = "indexable")
	private boolean indexable = true;


	
	@Column(name = "selectable")
	private boolean selectable = true;

	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "enabledclasses", targetEntity=KbeeClassifier.class)
	private List<Classifier> enabledclassifiers = new ArrayList<Classifier>();
	
	@Column(name = "javaclass")
	private String javaclass;
	
	@Override
	public List<Classifier> getClassifiers() {
	 	return  enabledclassifiers;
	}
	
	@Override
	public String getId() {
	 	return id;
	}

	@Override
	public void setId(String id) {
	 	this.id= id;
	}

 	@Override
	public String getName() {
	 	return name;
	}

	@Override
	public void setName(String name) {
	 this.name=name;
	}
	
	@Override
	public ObjectState getState() {
 		return isEnabled() ? ObjectState.ENABLED : ObjectState.ARCHIVED;
	}
	
	@Override
	public boolean isEnabled() {
 		return isenabled;
	}
	
	@Override
	public boolean isIndexable() {
 		return indexable;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isenabled=enabled;
	}
	
 	@Override
	public String getJavaClass() {
		return javaclass;
	}

	@Override
	public void setJavaClass(String classname) {
		this.javaclass=classname;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeContentClass)) 
			return false;
		return ((KbeeContentClass)object).getId().equals(getId());
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public boolean isSelectable() {
		return this.selectable;
	}

	@Override
	public void setSelectable(boolean as) {
		this.selectable=as;
		
	}

}

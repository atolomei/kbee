package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.dom.AbstractObject;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_LAUNCHER_GROUP")
public class KbeeLauncherGroup extends AbstractObject implements  LauncherGroup {

	@Id
	@SequenceGenerator(name = "source_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;

	@Column(name = "alias")
	private String alias;
	
	@Column(name = "position")
	private int order;
	
	@Column(name = "visible")
	private boolean visible;

	public List<ProcessLauncher> getLaunchers() {
		return null;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
		
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public int getOrder() {
		return this.order;
	}
	
	@Override
	public String getAlias() {
		return this.alias;
	}
	
	@Override
	public Long getId()	{
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}

	public void setAlias(String a) {
		this.alias=a;
	}
}
package com.novamens.kbee.portal.model;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.dom.Json;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;

import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

/**
 * <p>Abstract class of all Portal objects, it is analogous to {@link Content}.</p>
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entity")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "PO_PORTALOBJECT")
@DynamicInsert
public abstract class KbeePortalObject extends AbstractObject implements PortalObject {
				
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObject.class.getName());
	
	/** Version id */
	@Id
	@SequenceGenerator(name = "content_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_sequencer")
	@Column(name = "id")
	private Long id;

	/** Object Id. Shared by all versions */
	@Column(name = "oid")
	private Long oid = null;

	@Column(name = "name")
	private String name;

	/** display name */
	@Column(name = "title")
	private String title;

	@Column(name = "subtitle")
	private String subtitle;

	@Column(name = "clazz")
	private String clazz;

	/** used by api and iql */
	@Column(name = "key")
	private String key;

	@Column(name = "description")
	private String description;

	@Column(name = "usage_info_key")
	private String usage_info_key;
	
	
	@Column(name = "data_provider_info")
	private String data_provider_info;
	
	@Column(name = "kmode")
	private int mode;

	@Column(name = "version")
	private int version = 0;

	@Column(name = "nextversion")
	private int nextVersion = 0;

	@Column(name = "ishead")
	private boolean ishead = true;

	@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeePortalObject.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "prev_version")
	private KbeePortalObject previousVersion;

	// @ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeePortalObject.class)
	// @Fetch(FetchMode.SELECT)
	// @JoinColumn(name = "parent_id")
	// private PortalObject parent;
	
	@Column(name = "custom_values")
	private String custom_values;

	@Transient
	private String language = Locale.getDefault().getLanguage();


	public KbeePortalObject() {
	}
	
	@Override
	public void setUsageInfoKey(String i) {
		this.usage_info_key = i;
	}

	@Override
	public String getUsageInfoKey() {
		return usage_info_key;
	}

	
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public String getSubtitle() {
		return subtitle;
	}


	@Override
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	

	public void  setCustomValuesJson(Json js) {
		if (js!=null)
			this.custom_values= js.toString();
		else
			this.custom_values= null;
	}
	@Override
	public Json getCustomValuesJson() {
		
		if (this.custom_values==null)
			return new KbeeJson();
		
		try {
			KbeeJson json = new KbeeJson(custom_values);
			return json;
		}
		catch (Exception e) {
			logger.error(e);
			return new KbeeJson();
		}
	}

	
	

	public Map<String, String> getGeneralInfo() {

		return new HashMap<String, String>();
	}

	/**
	 * implemented by subclasses
	 */
	public abstract PortalObject clone();
	
	
	public abstract PortalObject getParent();
	//{
	//	return parent;
	//}

	/** 
	 * ISO 639
	 */
	@Override
	public String getLanguage() {
		return language;
	}

	
	public Site getSite() {
	
		if (getParent() != null)
			return getParent().getSite();
		
		else if (this instanceof Site)
			return (Site) this;
		
		return null;
		
	}

	protected void onClone(PortalObject object) {
		super.onClone((AbstractObject) object);

		((KbeePortalObject) object).setName(getName());
		((KbeePortalObject) object).setTitle(getTitle());

		// Version is Zero for cloned objects
		if (object instanceof KbeePortalObject) {
			((KbeePortalObject) object).setVersion(0);
			((KbeePortalObject) object).setHeadVersion(true);
		}

		// IMPORTANT!: the parent must be changed by subclasses when cloning.
		//
		//((KbeePortalObject) object).setParent(getParent());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public PortalObject getPreviousVersion() {
		return previousVersion;
	}

	@Override
	public void setVersion(int version) {
		this.version = version;
		this.nextVersion = version + 1;
	}

	@Override
	public int getVersion() {
		return version;
	}

	
	public void incVersionCounter() {
		this.nextVersion++;
	}

	@Override
	public int getNextVersion() {
		if (nextVersion == 0)
			nextVersion = version + 1;
		return nextVersion;
	}

	public int getMode() {
		return mode;
	}

	 public void setMode(int mode) {
		this.mode = mode;
	}

	public void setPreviousVersion(Object po) {
		Assert.isInstanceOf(KbeePortalObject.class, po);
		this.previousVersion = (KbeePortalObject) po;
	}

	public boolean isHeadVersion() {
		return ishead;
	}

	public void setHeadVersion(boolean value) {
		ishead = value;
	}

	public void setOId(Long id) {
		oid = id;
	}

	public Long getOId() {
		return oid;
	}

	
	@Override
	public String getDataProviderInfo() {
		return data_provider_info;
	}

	
	/**
	 * must be overriden by subclasses
	 */
	// public abstract KbeePortalObject clone();

	
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return super.toString() + " | Title: " + getTitle() + " |  key: " + (getKey()!=null?getKey():"null");
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = Long.valueOf(id.toString());
	}

	

	@Override
	public boolean isPublished() {
		if (getState() != ObjectState.ENABLED)
			return false;
		PortalObject po = getParent();
		while (po != null) {
			if (po.getState() != ObjectState.ENABLED)
				return false;
			po = po.getParent();
		}
		return true;
	}

	@Override
	public String getDisplayName() {
		return getTitle();
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.PORTAL;
	}
	
	public String getKey() 			{return this.key;}
	public void setKey(String key) 	{this.key=key;}
	
	

	public void setDefaults() {
		
		if (clazz==null)
			this.clazz=this.getClass().getSimpleName();
		
		if (getState()==null)
			setState(ObjectState.ENABLED);
		
		if (super.getCreationOffsetDateTime()==null)
			setCreationOffsetDateTime(OffsetDateTime.now());
		
		if (super.getLastModifiedOffsetDateTime()==null)
			setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (getLastModifiedUser() == null) {
			if (getSessionUser()!=null) 
				setLastModifiedUser(getSessionUser());
		}

		
	}
	
	public PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

}

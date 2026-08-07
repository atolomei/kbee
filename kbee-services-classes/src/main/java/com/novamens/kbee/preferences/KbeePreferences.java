package com.novamens.kbee.preferences;


import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.preferences.Preferences;
import com.novamens.security.User;

/**  
 * 
 * Limitations
 * -----------
 *  ":" can not be present in key
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_preference")
@DynamicInsert
public class KbeePreferences implements Preferences {

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePreferences.class.getName());
	
	@Id 
	@SequenceGenerator(name = "preference_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preference_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "name")
	private String name;
	
	@Column(name = "properties")
	private String properties;
	
	@Transient
	private Properties prop;
	
	
	public KbeePreferences() {
	}

	public KbeePreferences(User user, String name) {
		this.user=user;
		this.name=name;
	}

	@Override
	public Long getId() {
		return id;
	}
	 
	@Override
	public void setId(Long id) {
		this.id=id;
	}
	 
	@Override
	public String getName() {
		return name;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	
	@Override
	public Properties getProperties() {
		if (this.prop==null)
			loadProperties();
		return this.prop;
	}
	
	static final String SEPARATOR = "¡";
	static final String SEPARATOR_ESCAPED = "\\¡";
	
	@Override
	public void setPreference(String keysrc, String valuesrc) {
		
		if (keysrc==null)
			return;
		
		if (this.prop==null)
			loadProperties();
		
		if (valuesrc==null)
			this.prop.remove(keysrc);
		else
			this.prop.put(keysrc, valuesrc);
		
		StringBuilder buil = new StringBuilder();
		for (Entry<Object, Object> entry: this.prop.entrySet()) {
			if (buil.length()>0)
				buil.append(SEPARATOR);
			buil.append( ((String)entry.getKey()) + ":" + ((String)entry.getValue()));
		}
		this.properties=buil.toString();
	}
	

	@Override
	public String getPreference(String key) {
		if (this.prop==null)
			loadProperties();
		return this.prop.getProperty(key);	
	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (id!=null)
			str.append("id " + id.toString() + " | ");
		str.append(" name: " + name);
		if (user!=null)
			str.append(" | user: " + user.getFirstLastName());
		
		if (this.properties!=null)
			str.append(" | properties: " + this.properties);

		return str.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Preferences) 
			return getId().equals(((Preferences) object).getId()); 
		return false;
	}


	@Override
	public int hashCode() {
		int hash= super.hashCode(); 
		hash = this.id!=null?this.id.intValue():0;
		if (this.properties!=null)
			hash += this.properties.hashCode();
		if (this.name!=null)
			hash += this.name.hashCode();
		return hash;
 	}

	
	private void loadProperties() {
		prop=new Properties();
		if (this.properties!=null) {
			String val2 []= this.properties.split(SEPARATOR_ESCAPED);
			for (String str: val2) {
				String el [] = str.split("\\:"); 
				if (el.length>1) {
					this.prop.put(el[0], el[1]);
				}
			}
  		}
	}

	
}

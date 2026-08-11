package com.novamens.kbee.dom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.model.ModelObject;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

/**
 * 
 * 
 * 
 *
 */
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@MappedSuperclass
public abstract class KbeeModelObject extends AbstractObject implements ModelObject {

	@Id 
	@SequenceGenerator(name = "modelobjet_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelobjet_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name="onlyroot")
	private boolean onlyRootEdit;

	@Column(name = "alias")
	private String alias;

	@Column(name = "DESCRIPTION")
	private String description;


	@Override
	public boolean isOnlyRootEdit() {
		return onlyRootEdit;
	}

	public void setOnlyRootEdit(boolean onlyRoot) {
		this.onlyRootEdit = onlyRoot;
	}



	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public void  setDescription(String s) {
		this.description=s;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	public void setAlias(String a) {
		this.alias=a;
	}
	
	@Override
	public String getAlias() {
		return this.alias;
	}
	
	public String getName() {
		return name;
	}
	
	
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
	
	
	protected String parseAlias(String s) {
		if (s==null)
			return null;
		return parsePredicate(s).toLowerCase().trim();
	}
	
	
	protected String parsePredicate(String s) {
		if (s==null)
			return null;
		String a0 = s.toLowerCase().replace("ñ", "enie").replace(" de ", ""); 
		String a1 = StringUtils.stripAccents(ServiceLocator.getService(LanguageService.class).removeStopWords(a0, getDomain().getLocale()));
		String a2=WordUtils.capitalizeFully(a1).replaceAll("[ |\\t|\\s|(|)]", "");
		return a2.trim();
	}
	
	
	
	
	
	
	
}
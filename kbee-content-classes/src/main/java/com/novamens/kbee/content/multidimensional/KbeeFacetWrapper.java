package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.audit.AuditSet;
 
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "Kb_Facet_Wrapper")
@Proxy(lazy=false)
public class KbeeFacetWrapper extends AbstractObject implements FacetWrapper {
	
	@Id 
	@SequenceGenerator(name = "facet_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "facet_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	
	/**
	 *  la implementación multi idioma es incompleta 
	 *  cuando el displayname es distinto de nulo se use sólo ese, sin soportar idiomas 
	 * 
	 */
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "visibility")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json visibility;
	
	@Column(name = "viewmode")
	private int viewmode;
	
	@Column(name = "SUGGESTER")
	private boolean suggester = true;
	
	private transient Facet facet;
	
	public KbeeFacetWrapper() {
	}
	
	public KbeeFacetWrapper(Facet facet) {
		setState(ObjectState.ENABLED);
		setFacet(facet);
	}
	
	@Override
	public Serializable getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	
	// transient String lang_display_name;
	
	
	public String getDisplayName() {
		if (displayName==null) {
			Facet facet = getFacet();
			if (facet!=null) {
				return facet.getDisplayName();
			}
			else {
				return null;
			}
				
		}
		else {
			return displayName;
		}
	}
	
	public String getDisplayName(Locale locale) {
		if (displayName==null) {
			Facet facet = getFacet();
			if (facet!=null) {
				return facet.getDisplayName(locale);
			}
			else {
				return null;
			}
		}
		else {
			return displayName;
		}
	}

	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		if (getViewMode()==3) {
			if (getFacet() instanceof ClassifierHierarchicalFacet) {
				return ((ClassifierHierarchicalFacet)getFacet()).getMembers3(resultSet, maxmembers);
			}
			else 
				return null;
		}
		else {
			return getFacet().getMembers(resultSet, maxmembers);
		}
	}
	
	public List<Member> getMembers(ResultSet resultSet, Member rootMember, int maxmembers) {
		return getFacet().getMembers(resultSet, rootMember, maxmembers);
	}
	
	public List<Member> getMembers(ResultSet resultSet, String filter, int maxmembers) {
		return getFacet().getMembers(resultSet, filter, maxmembers);
	}
	
	public boolean isVisible(ResultSet resultSet) {
		return getFacet().isVisible(resultSet);
	}

	public boolean isNavigable() {
		return getFacet().isNavigable();
	}
	
	public boolean isRangeEnabled() {
		return getFacet().isRangeEnabled();
	}
	
	public boolean isFilterable() {
		return getFacet().isFilterable();
	}
	
	public boolean isHierachical() {
		return getFacet().isHierachical();
	}
	
	public int getOrder() {
		return 0;
	}
	
	public int getViewMode() {
		return viewmode;
	}
	
	public void setVisibility(String context, boolean value) {
		if(visibility==null) 
			visibility = new KbeeJson();
		visibility.put(context, Boolean.valueOf(value).toString());
	}
	
	public boolean isVisible(String context) {
		if (visibility == null || visibility.get(context)==null) return true;
		return "true".equals(visibility.get(context));
	}
	
	public void setVisibility(Json json_visibility) {
		this.visibility=json_visibility;
	}
	
	public Json getVisibility() {
		return visibility;
	}
	
	public boolean isSuggester() {
		return suggester;
	}

	public void setSuggester(boolean suggester) {
		this.suggester = suggester;
	}

	public void setFacet(Facet facet) {
		this.facet = facet;
		this.name  = facet.getName();
	}
	
	public Facet getFacet() {
		return this.facet;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		if (super.getLastModifiedOffsetDateTime()==null) {
			if (getFacet()==null)
				return null;
			if (getFacet() instanceof ClassifierHierarchicalFacet) {
				return ((ClassifierHierarchicalFacet)getFacet()).getClassifier().getLastModifiedOffsetDateTime();
			}
			else 
			if (getFacet() instanceof AttributeFacet) {
				return ((AttributeFacet)getFacet()).getAttribute().getLastModifiedOffsetDateTime();
			}
			else 
			if (getFacet() instanceof DateFacet && ((DateFacet)getFacet()).getAttribute()!=null) {
				return ((DateFacet)getFacet()).getAttribute().getLastModifiedOffsetDateTime();
			}
			else {
				return getDomain().getLastModifiedOffsetDateTime();
			}
		}
		else
			return super.getLastModifiedOffsetDateTime();
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}
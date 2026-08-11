package com.novamens.kbee.content.document;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Resource;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.IDocSection;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "idocsection")
// 
//public class IDocSectionImpl extends AbstractObject implements IDocSection, Serializable {
public class IDocSectionImpl extends AbstractObject implements IDocSection {
	
	
	
	@Column(name = "ID")
	private Long id;
	
	// TODO VER Cascade
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeIDoc.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="idoc_id")
	private IDoc idoc;

	@Column(name = "sectionorder")
	private int order;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "attributejson")
	private String attributejson;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=AbstractResource.class)
	@JoinTable(name = "idocsectionresource",  
				joinColumns 		= @JoinColumn(name = "section_id"), 
				inverseJoinColumns 	= @JoinColumn(name = "resource_id")		
			)
	@OrderColumn(name="position")
	private  List<Resource> resources;
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
		
	public void setDescription(String description) 	 {this.description = description;}
	public String getDescription() 					 {return description;}
					
	public void setOrder(int position) 			 	{this.order = position;}
	public int getOrder() 						 	{return order;}
	
	public void setName(String name) 				 {this.name = name;}
	public String getName() 						 {return name;}

	public void setResources(List<Resource> list) 	 {resources=list;}
	public List<Resource> getResources() 			 {return resources;}
				
	public void setAttributesJSON(String attributes) {this.attributejson = attributes;}
	public String getAttributesJSON() 				 {return attributejson;}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.CONTENT;
	}
}

package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.content.resource.KbeeResourceFolder;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "CONTENTRESOURCE")
public class KbeeContentResource implements ContentResource, Identifiable {
	
	@Id
	@GenericGenerator(
		name = "contentresource_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "contentresourceid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contentresource_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="content_id")
	private Content content;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = AbstractResource.class)
	@JoinColumn(name="resource_id")
	private Resource resource;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeResourceFolder.class)
	@JoinColumn(name="folder_id")
	private ResourceFolder folder;
	
	@Column(name = "ispublic")
	private boolean ispublic = true;
	
	@Column(name = "isindex")
	private boolean isindex = false;
	
	@Column(name = "position", updatable = false)
	private int order;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeResourceTag.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "TAG_ID")
	private ResourceTag tag;
	
	public KbeeContentResource() {
		super();
	};
	
	public KbeeContentResource(Content content, KBFile resource) {
		super();
		setContent(content);
		setResource(resource);
	};
	
	public KbeeContentResource(Content content, Resource resource) {
		super();
		setContent(content);
		setResource(resource);
	}
	
	public KbeeContentResource(Content content, Resource resource, boolean ispublic) {
		super();
		setContent(content);
		setResource(resource);
		setPublic(ispublic);
	}
	
	public KbeeContentResource(Content content, Resource resource, ResourceTag group, boolean ispublic) {
		super();
		setContent(content);
		setResource(resource);
		setTag(group);
		setPublic(ispublic);
	}
	
	public KbeeContentResource(Content content, Resource resource, ResourceTag tag) {
		super();
		setContent(content);
		setResource(resource);
		setTag(tag);
	}
	
	public KbeeContentResource(Content content, Resource resource, ResourceFolder folder, ResourceTag group) {
		super();
		setContent(content);
		setResource(resource);
		setTag(group);
		setFolder(folder);
	}
	
	public KbeeContentResource(Content content, Resource resource, ResourceFolder folder, ResourceTag group, boolean isIndex) {
		super();
		setContent(content);
		setResource(resource);
		setTag(group);
		setFolder(folder);
		setIndex(isIndex);
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	public Content getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content=content;
	}
	
	@Override
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public void setTag(ResourceTag tag) {
		this.tag = tag;
	}
	
	@Override
	public ResourceTag getTag() {
		return this.tag;
	}
	
	@Override
	public ResourceFolder getFolder() {
		return folder;
	}
	
	public void setFolder(ResourceFolder folder) {
		this.folder = folder;
	}
	
	@Override
	public boolean isPublicArea() {
		return this.ispublic;			
	}
	
	public void setPublic(boolean value) {
		this.ispublic = value;			
	}
		
	public boolean isIndex() {
		return isindex;
	}

	public void setIndex(boolean isindex) {
		this.isindex = isindex;
	}

	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
	public String getDisplayName() {
		return (getResource()!=null?getResource().getName():"-");
	}
}

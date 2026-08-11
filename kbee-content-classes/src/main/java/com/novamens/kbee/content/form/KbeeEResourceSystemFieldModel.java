package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EResourceSystemModel;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.service.ServiceLocator;

@JsonTypeName("resourcesystem")
public class KbeeEResourceSystemFieldModel extends KbeeEAbstractFieldModel<ResourceNode> implements EResourceSystemModel<ResourceNode> {
	private static final long serialVersionUID = 1L;
	
	private String tagId;
	
	@JsonProperty("tag")
	private String tagAlias;
	
	public String getTagId() {
		return tagId;
	}
	
	public void setTagId(String id) {
		this.tagId = id;
	}
	
	public String getTagName() {
		return tagAlias;
	}
	
	public void setTag(ResourceTag tag) {
		this.tagId = tag!=null ? String.valueOf(((KbeeResourceTag)tag).getId()) : null;
	}
	
	public void setTagName(String name) {
		this.tagAlias = name;
	}
	
	@JsonIgnore
	public ResourceTag getTag() {
		return getTag(tagId, tagAlias);
	}
	
	public ResourceNode get(Object object) {
		KbeeResourceNode resource = null;
		for (ContentResource contentresource : ((KbeeResourceContainer)object).getContentResources()) {
			if (getTag()==null || getTag().equals(contentresource.getTag())) {
				resource = new KbeeResourceNode(contentresource.getResource(), contentresource.getFolder());
				resource.setIndex(contentresource.isIndex());
				break;
			}
		}
		return resource;
	}
	
	public List<ResourceNode> getValues(Object object) {
		List<ResourceNode> values = new ArrayList<>();
		for (ContentResource contentresource : ((KbeeResourceContainer)object).getContentResources()) {
			if (getTag()==null || getTag().equals(contentresource.getTag())) {
				KbeeResourceNode resource = new KbeeResourceNode(contentresource.getResource(), contentresource.getFolder());
				resource.setIndex(contentresource.isIndex());
				values.add(resource);
			}
		}
		return values;
	}
	
	@Override
	public void set(Object object, Object data) {
		List<Resource> resources = new ArrayList<Resource>();
		if (data!=null)
		resources.add((Resource)data);
		((ResourceContainer)object).setResources(resources, getTag((ResourceContainer)object));
	}
	
	@Override
	public void set(Object object, List<ResourceNode> resources) {
		((ResourceContainer)object).setResourceNodes(resources, getTag((ResourceContainer)object));
	}
	
	public ResourceTag getTag(ResourceContainer content) {
		for (ResourceTag tag : ((Content)content).getContentTemplate().getResourceTags()) {
			if (tag.equals(getTag())) {
				return tag;
			}
		}
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		ResourceTag modeltag = getTag();
		if (modeltag==null && (getTagId()!=null||getTagName()!=null)) {
			String message = "Resource Tag ";
			message += tagAlias!=null ? tagAlias : tagId;
			message += " not found";
			return message;
		}
		if (object instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)object;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			object = initializer.getImplementation();
		}
		if (object!=null && !(object instanceof ResourceContainer)) {
			String message = "Only for resoure container objects"; 
			return message;
		}
		if (object!=null && modeltag!=null) {
			boolean found = false;
			ContentTemplate template = ((Content)object).getContentTemplate();
			for (ResourceTag tag : template.getResourceTags()) {
				if (tag!=null && tag.getName().equals(modeltag.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				String message = "Resource Tag ";
				message += tagAlias!=null ? tagAlias : tagId;
				message += " not found in "+ template.getDisplayName() + " template";
				return message;
			}
		}
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		String description = getModelObjectName(locale) + " ";
		description += getTag()!=null ? getTag().getName() : " not found";
		return description;
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EResourceSystemModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "Tag";
	}
	
	@JsonIgnore
	protected ResourceTag getTag(String tagId, String  tagAlias) {
		if (tagId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			ResourceTag tag = null;
			try {
				tag = (ResourceTag)sf.getCurrentSession().get(KbeeResourceTag.class, Long.valueOf(tagId));
			}
			catch (ObjectNotFoundException e) {
				
			}
			return tag;
		}
		else {
			if (tagAlias!=null) {
				for (ResourceTag tag : getRepository(ResourceTag.class).findAll()) {
					if (tagAlias.equals(tag.getAlias())) {   
						tagId = String.valueOf(((KbeeResourceTag)tag).getId());
						return tag;
					}
				};
			}
		}
		return null;
	}
	
} 
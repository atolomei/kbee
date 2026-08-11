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
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFieldAwareModel;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormResourceEvent;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.model.ContentTemplate;
import com.novamens.event.Event;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.service.ServiceLocator;

@JsonTypeName("resource")
public class KbeeEResourceFieldModel extends KbeeEAbstractFieldModel<Resource> implements EResourceModel<Resource>, EFieldAwareModel {
	private static final long serialVersionUID = 1L;
	
	private String tagId;
	private EFormField<?> field;

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
		if (tagId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			ResourceTag tag = null;
			try {
				tag = (ResourceTag)sf.getCurrentSession().get(KbeeResourceTag.class, Long.valueOf(this.tagId));
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
	
	@JsonIgnore
	public EFormField<?> getField() {
		return field;
	}
	
	@Override
	public void setField(EFormField<?> field) {
		this.field = field;
	}
	
	public Resource get(Object object) {
		List<Resource> resources = getTag()!=null ?
			((ResourceContainer)object).getResources(getTag().getName()) :
			((ResourceContainer)object).getResources();
		Resource resource = resources.isEmpty() ? null : resources.get(0);
		return resource;
	}
	
	public List<Resource> getValues(Object object) {
		List<Resource> values = new ArrayList<Resource>();
		if (getTag()!=null)
			values.addAll(((ResourceContainer)object).getResources(getTag().getName()));
		else
			values.addAll(((ResourceContainer)object).getResources());
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
	public void set(Object object, List<Resource> resources) {
		((ResourceContainer)object).setResources(resources, getTag((ResourceContainer)object));
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
	public boolean handle(Event event) {
		return event instanceof EFormResourceEvent &&
			((EFormResourceEvent)event).getTag().equals(getTag());	
	}
	
	
	public List<Resource> onEvent(Event event) {
		EFormContentData data = (EFormContentData)((EFormResourceEvent)event).getFormData();
		Content content = data.getContent();
		List<Resource> values = new ArrayList<Resource>();
		if (getTag()!=null) {
			//values.addAll(((ResourceContainer)content).getResources(getTag().getName()));
			for (Resource resource :((ResourceContainer)content).getResources(getTag().getName())) {
				values.add(unproxy(resource));
			}
		}	
		else
			values.addAll(((ResourceContainer)content).getResources());
		boolean found = false;
		if (((EFormResourceEvent)event).getResource()!=null) {
			for (Resource resource : values) {
				if (resource.equals(((EFormResourceEvent)event).getResource())) {
					found = true;
					break;
				}
			}
			if (!found) {
				values.add(((EFormResourceEvent)event).getResource());
			}
		}
		return values;
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
		return EResourceModel.GetTypeLabel();
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "Tag";
	}
	
	private Resource unproxy(Resource resource) {
		if (resource instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy)resource;
			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
			resource = (Resource)initializer.getImplementation();
		}
		return resource;
	}
} 
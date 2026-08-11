package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.model.RelationTemplate;
import com.novamens.event.Event;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

@JsonTypeName("relation resource")
public class  KbeeERelationResourceFieldModel extends KbeeEResourceFieldModel {
	private static final long serialVersionUID = 1L;
			
	static private Logger logger = Logger.getLogger(KbeeERelationResourceFieldModel.class.getName());
	
	private String relationId;
	
	@JsonProperty("relation")
	private String relationName;
	
	public String getRelationId() {
		return relationId;
	}
	
	public void setRelationId(String id) {
		this.relationId = id;
	}
	
	public void setRelationName(String name) {
		this.relationName = name;
	}
	
	public String getRelationName() {
		return this.relationName;
	}
	
	@JsonIgnore
	public RelationTemplate getRelation() {
		if (relationId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			RelationTemplate relation = (RelationTemplate)sf.getCurrentSession().load(KbeeRelationTemplate.class, Long.valueOf(this.relationId));
			return relation;
		}
		else {
			if (relationName!=null) {
				for (RelationTemplate relation : getContentDao().getRelations(getContentDao().getDomain())) {
					if (relationName.equals(relation.getName())) {
						relationId = String.valueOf(relation.getId());
						return relation;
					}
				}
			}
		}
		return null;
	}
	
	public Resource get(Object object) {
		List<Relation> relations = ((Content)object).getRelations(getRelation());
		Content related = relations.isEmpty() ? null : reload(relations.get(0).getTarget());
		Resource resource = related!=null ? super.get(related) : null;
		return resource;
	}
	
	@Override
	public void set(Object object, Object data) {
		// read only model
	}
	
	@Override
	public boolean isReadOnly() {
		return true;
	}
	
	@Override
	public boolean handle(Event event) {
		if (!(event instanceof EFormEvent)) 
			return false;
		EFieldModel<?> eventModel = ((EFormEvent)event).getField().getModel();
		if (!(eventModel instanceof KbeeERelationFieldModel))
			return false;
		RelationTemplate relation = ((KbeeERelationFieldModel)eventModel).getRelation();
		if (relation==null)
			return false;
		return relation.equals(getRelation());
	}
	
	@Override
	public List<Resource> onEvent(Event event) {
		List<Resource> values = new ArrayList<Resource>();
		Content content = ((EFormContentData)((EFormEvent)event).getFormData()).getContent();
		Resource resource = get(content);
		if (resource!=null) values.add(resource);
		return values;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		if (getTag()==null && (getTagId()!=null||getTagName()!=null)) {
			String message = "Resource Tag ";
			message += getTagName()!=null ? getTagName() : getTagId();
			message += " not found";
			return message;
		}
		if (object!=null && !(object instanceof ResourceContainer)) {
			String message = "Only for resoure container objects"; 
			return message;
		}
		return null;
	}
	
	protected Content reload(Content content) {
		try {
			Class<?> clazz = Hibernate.getClass(content);
			Serializable id = content.getId();
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			content = (Content)sf.getCurrentSession().load(clazz, id);
			return content;
		}
		catch (Exception e) {
			logger.error(e);
			return content;
		}
	}
	
	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
} 
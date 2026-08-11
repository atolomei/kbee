package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.ERelationModel;
import com.novamens.content.model.RelationTemplate;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.service.ServiceLocator;

@JsonTypeName("relation")
public class  KbeeERelationFieldModel extends KbeeEAbstractFieldModel<Content> implements ERelationModel<Content> {
	private static final long serialVersionUID = 1L;
	
	private String relationId;
	
	@JsonProperty("relation")
	private String relationName;
	
	private boolean reverse;
	
	public void setRelation(String id) {
		this.relationId = id;
	}
	
	public void setRelation(RelationTemplate template) {
		relationId = String.valueOf(template.getId());
	}
	
	public String getRelationId() {
		return this.relationId;
	}
	
	public void setRelationName(String name) {
		this.relationName = name;
	}
	
	public String getRelationName() {
		return this.relationName;
	}
	
	@Override
	@JsonIgnore
	public RelationTemplate getRelation() {
		if (relationId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			RelationTemplate relation = (RelationTemplate)sf.getCurrentSession().get(KbeeRelationTemplate.class, Long.valueOf(this.relationId));
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
		
	public List<Content> getValues(Object object) {
		List<Content> values = new ArrayList<Content>();
		for (Relation relation : ((Content)object).getRelations(getRelation())) {
//			if (relation.getTarget().getOId().equals(((Content)object).getOId())) {
//				values.add(relation.getSource());
//			}
//			else {
//				values.add(relation.getTarget());
//			}
			if (!reverse && relation.getSource().getId().equals(((Content)object).getId())) {
				values.add(relation.getTarget());
			}
			if (reverse && relation.getTarget().getId().equals(((Content)object).getId())) {
				values.add(relation.getSource());
			}

		}
		return values;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void set(Object object, Object data) {
		set(object, (List<Content>)data);
	}
	
	@Override
	public void set(Object object, List<Content> contents) {
		List<Relation> relations = new ArrayList<Relation>();
		if (!isReverse(object)) {
			for (Content content : contents) {
				KbeeRelation relation = null;
				for (Relation r : ((Content)object).getRelations(getRelation())) {
					if (r.getTarget().equals(content)) {
						relation = (KbeeRelation)r;
						break;
					}
				}
				if (relation == null) {
					relation = new KbeeRelation();
					relation.setTemplate(getRelation());
					relation.setSource((Content)object);
					relation.setTarget(content);
				}
				relations.add(relation);
			}
			((Content)object).setRelations(getRelation(), relations);
		}
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		return getModelObjectName(locale) + " " + (getRelation()!=null ? getRelation().getName() : " not found");
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return ERelationModel.GetTypeLabel();
	}
	
	public boolean isReverse(Object object) {
		return object instanceof Content && !getRelation().getSourceTemplate().equals(((Content)object).getContentTemplate());
	}
		
	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	protected ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(this.getClass().getName(), locale);
		return res.getString("relation");
	}
} 
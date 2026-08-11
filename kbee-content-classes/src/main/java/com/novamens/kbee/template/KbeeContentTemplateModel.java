package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class KbeeContentTemplateModel extends KbeeClassificableTemplateModel {
	
	private Content content;
	private String nodename = "content";
	
	public KbeeContentTemplateModel(Content content) {
		setContent(content);
	}
	
	public KbeeContentTemplateModel(Content content, String nodename) {
		setContent(content);
		this.nodename = nodename;
	}
	
	public void setContent(Content content) {
		this.content = content;
	}
	
	public Content getContent() {
		return content;
	}
	
	@Override
	public Object getObject() {
		return content;
	}
	
	@Override
	public Classificable getClassificable() {
		return content;
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = super.get(key);
		if (model==null && isRelation(key)) {
			model = getRelation(key);
		}
		return model;
	}

	@Override
	public List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> templates = new ArrayList<ClassifierTemplate>();
		for (ClassifierTemplate template : getContent().getContentTemplate().getClassifiers()) {
			templates.add(template);
		}
		return templates;
	}
	
	@Override
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
		for (AttributeTemplate template : getContent().getContentTemplate().getAttributes()) {
			templates.add(template);
		}
		return templates;
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return nodename;
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "content";
	}
	
	@Override
	public String getAsString() {
		return getContent().getTitle();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected TemplateModel wrap(KbeeMethod canonical, Object value) {
		TemplateModel model = null;
		if (value!=null && value instanceof ContentTemplate) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("Name", "Name"));
			model = new KbeeObjectWrapperTemplateModel(value, "ContentTemplate", canonicals, this);
		}
		else 
		if (value!=null && value instanceof List<?>) {
			List<Object> values = (List<Object>)value;
			if (!values.isEmpty() && values.get(0) instanceof Resource) {
				Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
				canonicals.add(new KbeeMethod("Name", "Name"));
				canonicals.add(new KbeeMethod("Title", "Title"));
				canonicals.add(new KbeeMethod("Size", "Size"));
				canonicals.add(new KbeeMethod("Url", "Url"));
				model = new KbeeListWrapperTemplateModel(values, "Resource", canonicals, this);
			}
		}
		else {
			model = super.wrap(canonical, value);
		}
		return model;
	}
	
	protected List<TemplateModel> getChilds() {
		List<TemplateModel> childs = super.getChilds();
		for (RelationTemplate relationtemplate :  getContent().getContentTemplate().getRelations()) {
			if (relationtemplate.getSourceTemplate().equals(content.getContentTemplate())) {
				for (Relation relation :  getContent().getRelations(relationtemplate)) {
					TemplateModel model = new KbeeContentTemplateModel(relation.getTarget(), relationtemplate.getName());
					childs.add(model);
				}
			}
		}
		//childs.addAll(getServices().values());
		childs.sort(new Comparator<TemplateModel>() {
			@Override
			public int compare(TemplateModel m1, TemplateModel m2) {
				try {
					if (m1 instanceof TemplateNodeModel && m2 instanceof TemplateNodeModel) {
					return ((TemplateNodeModel)m1).getNodeName().toLowerCase().compareTo(((TemplateNodeModel)m2).getNodeName().toLowerCase());
					}
					else
						return 0;
					
				} 
				catch (Exception e) {
					return 0;	
				}
			}
		});
		return childs;
	}
	
	protected boolean isRelation(String key)  {
		for (RelationTemplate relationtemplate :  getContent().getContentTemplate().getRelations()) {
			if (relationtemplate.getSourceTemplate().equals(content.getContentTemplate()) &&
					key.equals(relationtemplate.getName())) {
				return true;
			}
		}
		return false;
	}
	
	protected TemplateModel getRelation(String key) {
		for (RelationTemplate relationtemplate :  getContent().getContentTemplate().getRelations()) {
			if (relationtemplate.getSourceTemplate().equals(content.getContentTemplate()) &&
					key.equals(relationtemplate.getName())) {
				List<Object> relateds = new ArrayList<>();
				for (Relation relation :  getContent().getRelations(relationtemplate)) {
					relateds.add(relation.getTarget());
				}
				Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
				canonicals.add(new KbeeMethod("Name", "Name"));
				canonicals.add(new KbeeMethod("Title", "Title"));
				canonicals.add(new KbeeMethod("Url", "Url"));
				TemplateModel model = new KbeeListWrapperTemplateModel(relateds, "Resource", canonicals, this);
				return model;

			}
		}
		return null;
	}

	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.add(new KbeeMethod("title", "title"));
		canonicals.add(new KbeeMethod("ContentTemplate", "ContentTemplate"));
		canonicals.add(new KbeeMethod("domain", "Domain"));
		canonicals.add(new KbeeMethod("resource", "Resources"));
		canonicals.add(new KbeeMethod("OId", "OId"));
		canonicals.add(new KbeeMethod("lastModifiedTime", "lastModifiedOffsetDateTime"));
		canonicals.add(new KbeeMethod("publicurl") {
			public Object evaluate(Object object) {
				return getContent().getService(UrlService.class).getPublicUrl();
			}
		});
		canonicals.add(new KbeeMethod("taskurl") {
			public Object evaluate(Object object) {
				return getContent().getService(UrlService.class).getTaskUrl();
			}
		});
		canonicals.add(new KbeeMethod("subtitle") {
			public Object evaluate(Object object) {
				return getContent().getService(ContentService.class).getPortalSubtitle();
			}
		});
		canonicals.add(new KbeeMethod("valid") {
			public Object evaluate(Object object) {
				return getContent().getService(ContentService.class).isValid();
			};
		});
		return canonicals;
	}
	
	@Override
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = super.keysSet();
		return keys;
	}
}

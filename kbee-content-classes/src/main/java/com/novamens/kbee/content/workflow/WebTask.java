package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.kbee.content.model.KbeeModelElementTemplate;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;



@SuppressWarnings("deprecation")
public class WebTask extends KbeeTask implements UserTask {
			
	//private static Logger logger = Logger.getLogger(WebTask.class.getName());

	private String pagebean;
	private boolean readOnly = false;
	private boolean enableEditingAllResources = false;
	private boolean enableLabels;
	private String knowledgeCriteria;
	private String relatedCriteria;
	private String version;
	private boolean includeCallerForms;
	
	private List<ClassifierTemplate> classifiers = new ArrayList<ClassifierTemplate>();
	private List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
	private List<ModelSection> sections = new ArrayList<ModelSection>();
	
	public TaskPage<?> getPage(WorkflowContext context) {
		return getPage(context, false);
	}
	
	public TaskPage<?> getPage(WorkflowContext context, boolean select_preference) {
		TaskPage<?> page = getPageFactory().getPage(this, context);
		return page;
	}
	
	public void setPage(String bean) {
		this.pagebean = bean;
	}
	
	public String getPage() {
		return this.pagebean;
	}

	public void setClassifiers(List<ClassifierTemplate> templates) {
		
		this.classifiers = templates;
	}
	
	public List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> templates = new ArrayList<ClassifierTemplate>();
		if (classifiers!=null) templates.addAll(classifiers);
		for (ModelSection section : getSections()) {
			for (ModelElementTemplate template : section.getStructure()) {
				if (template instanceof ClassifierTemplate) {
					templates.add((ClassifierTemplate)template);
				}
			}
		}
		return templates;
	}
	
	public void setAttributes(List<AttributeTemplate> templates) {
		this.attributes = templates;
	}
	
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
		if (attributes!=null) templates.addAll(attributes);
		for (ModelSection section : getSections()) {
			for (ModelElementTemplate template : section.getStructure()) {
				if (template instanceof AttributeTemplate) {
					templates.add((AttributeTemplate)template);
				}
			}
		}
		return templates;
	}
	
	public void setStructure(List<ModelElementTemplate> structure) {
		int order = 0;
		List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
		List<ClassifierTemplate> classifiers = new ArrayList<ClassifierTemplate>();
		for (ModelElementTemplate template : structure) {
			if (template instanceof AttributeTemplate) {
				((TaskAttributeTemplate)template).setOrder(order);
				attributes.add((TaskAttributeTemplate)template);
			}
			if (template instanceof ClassifierTemplate) {
				((TaskClassifierTemplate)template).setOrder(order);
				classifiers.add((TaskClassifierTemplate)template);
			}
			if (template instanceof KbeeModelElementTemplate) {
				if (template.getElement() instanceof Attribute) {
					TaskAttributeTemplate attributetemplate = new TaskAttributeTemplate();
					attributetemplate.setAttribute((Attribute)template.getElement());
					attributetemplate.setOrder(order);
					attributetemplate.setReadOnly(((KbeeModelElementTemplate)template).isReadOnly());
					attributetemplate.setParent(((KbeeModelElementTemplate)template).getParent());
					attributes.add(attributetemplate);
				}
				if (template.getElement() instanceof Classifier) {
					TaskClassifierTemplate classifiertemplate = new TaskClassifierTemplate();
					classifiertemplate.setClassifier((Classifier)template.getElement());
					classifiertemplate.setOrder(order);
					classifiertemplate.setMultiplicity(((KbeeModelElementTemplate)template).getMultiplicity());
					classifiertemplate.setMetadataSubtitle(((KbeeModelElementTemplate)template).isMetadataSubtitle());
					classifiertemplate.setVisible(((KbeeModelElementTemplate)template).isVisible());
					classifiertemplate.setParent(((KbeeModelElementTemplate)template).getParent());
					classifiers.add(classifiertemplate);
				}
			}
			order++;
		}
		setAttributes(attributes);
		setClassifiers(classifiers);
	}
	
	public List<ModelElementTemplate> getStructure() {
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		structure.addAll(getClassifiers());
		structure.addAll(getAttributes());
		Collections.sort(structure, new Comparator<ModelElementTemplate>() {
			@Override
			public int compare(ModelElementTemplate a, ModelElementTemplate b) {
				return a.getOrder() < b.getOrder() ? -1 : 1;
			}
		});
		return structure;
	}
	
	public List<ModelSection> getSections() {
		return sections;
	}
	
	public void setSections(List<ModelSection> sections) {
		this.sections = sections;
	}
	
	public void setReadOnly(boolean value) {
		this.readOnly = value;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public String getKnowledgeCriteria() {
		return knowledgeCriteria;
	}
	
	public String getRelatedCriteria() {
		return relatedCriteria;
	}
	
	public void setRelatedCriteria(String criteria) {
		this.relatedCriteria = criteria;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
						
	public void setKnowledgeCriteria(String criteria) {
		this.knowledgeCriteria = criteria;
	}
	
	public void setEnableEditingAllResources(boolean value) {
		this.enableEditingAllResources = value;
	}
	
	public boolean getEnableEditingAllResources() {
		return this.enableEditingAllResources;
	}
	
	public boolean isEnableLabels() {
		return enableLabels;
	}

	public void setEnableLabels(boolean enableLabels) {
		this.enableLabels = enableLabels;
	}

	protected TaskPageFactory getPageFactory() {
		String factorybean = pagebean == null ? "taskpage-factory" : pagebean;
		TaskPageFactory factory = (TaskPageFactory)ServiceLocator.getService(BeansService.class).getBean(factorybean);
		return factory;
	}
	
	protected String getPageBean(WorkflowContext context) {
		if (pagebean==null) {
			String pagebean = getContentClass(((KbeeContext)context).getContent()) + "-taskpage";
			if (getVersion()!=null && !"".equals(getVersion())) {
				pagebean += "-" + getVersion();
			}
			return pagebean;
		}
		else {
			return pagebean;
		}
	}
	

	
	public void setIncludeCallerForms(boolean value) {
		this.includeCallerForms = value;
	}
	
	public boolean getIncludeCallerForms() {
		return includeCallerForms;
	}
	
	protected String getContentClass(Content content) {
		Assert.isTrue(content!=null, "no content");
		String classname = content.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		return classname;
	}
}
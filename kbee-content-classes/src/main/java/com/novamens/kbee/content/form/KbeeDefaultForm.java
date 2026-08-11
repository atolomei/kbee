package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.novamens.beans.BeansService;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.service.ServiceLocator;

public class KbeeDefaultForm  implements EForm, Serializable {
	private static final long serialVersionUID = 1L;
	private List<EFormComponent> components;
	private String name;
	
	public KbeeDefaultForm(ContentTemplate template) {
		this.components = getComponents(template);
		this.name = "Attributes";
	}
	
	public List<EFormComponent> getComponents() {
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.components = components;
	}
	
	public List<EFormField<?>> getFields() {
		return getFields(getComponents());
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	public EDisposition getDisposition() {
		return EDisposition.VERTICAL;
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		return EFormAccessLevel.GENERAL;
	}
	
	@Override
	public String getViewer() {
		return null;
	}
	
	@Override
	public boolean isUseInline() {
		return false;
	}

	@Override
	public boolean isFileContainer() {
		return false;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		return true;
	}
	
	@Override
	public List<String> getBehaviors() {
		return new ArrayList<String>();
	}
	
	@Override
	public EFormField<?> getField(String name) {
		for (EFormField<?> field : getFields(getComponents())) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	protected List<EFormComponent> getComponents(ContentTemplate contenttemplate) {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
	
		if (contenttemplate.getStructure().isEmpty()) 
			return components;
		
		for (ModelElementTemplate template : contenttemplate.getStructure()) {
			if (template instanceof ClassifierTemplate) {
				KbeeEClassifierFieldModel model = new KbeeEClassifierFieldModel();
				EFormAbstractField<DataSetMember> field = null;
				Classifier classifier = ((ClassifierTemplate)template).getClassifier();
				long totalmembers = getTotalMembers(classifier.getDataSet());
				Multiplicity multiplicity =  ((ClassifierTemplate)template).getMultiplicity();
				if (Multiplicity.M0N.equals(multiplicity) || Multiplicity.M1N.equals(multiplicity)) {
					field = new KbeeEMembersListField();
				}
				else {
					if (totalmembers>50) {
						field = new KbeeEMemberAutoCompleteField();
					}
					else {
						field = new KbeeEMemberComboField();
					}
				}
				field.setName(classifier.getAlias());
				field.setLabel(classifier.getDisplayName());
				model.setParentClassifier((Classifier)template.getParent());
				model.setReverse(template.isReverse());
				model.setClassifier(classifier);
				field.setRequired(Multiplicity.M1N.equals(multiplicity) || Multiplicity.M11.equals(multiplicity));
				field.setModel(model);
				field.setReadOnly(((ClassifierTemplate) template).isReadOnly());
				components.add(field);
			}
			else
			if (template instanceof AttributeTemplate) {
				Attribute attribute = ((AttributeTemplate)template).getAttribute();
				Multiplicity multiplicity =  ((AttributeTemplate)template).getMultiplicity();
				EFormAbstractField<?> field = null;
				KbeeEAttributeFieldModel<?> model = null;
				if (AttributeType.DATE.equals(attribute.getType()) || AttributeType.VALIDITY_FROM.equals(attribute.getType()) || AttributeType.VALIDITY_TO.equals(attribute.getType()) ) {
					field = new KbeeEDateField();
					model = new KbeeEDateAttributeModel();
					model.setAttribute(attribute);
					((KbeeEDateField)field).setModel((KbeeEDateAttributeModel)model);
				}
				else
				if (AttributeType.STRING.equals(attribute.getType())) {
					field = new KbeeEStringField();
					model = new KbeeEStringAttributeModel();
					model.setAttribute(attribute);
					model.setParentClassifier((Classifier)template.getParent());
					((KbeeEStringField)field).setModel((KbeeEStringAttributeModel)model);
				}
				if (field!=null) {
					field.setName(attribute.getAlias());
					field.setLabel(template.getDisplayName());
					field.setRequired(Multiplicity.M1N.equals(multiplicity) || Multiplicity.M11.equals(multiplicity));
					field.setReadOnly(((AttributeTemplate) template).isReadOnly());
					components.add(field);
				}
			}
		}
			
			List<ResourceTag> groups = contenttemplate.getResourceTags();
			
			if (groups==null || groups.isEmpty()) {
				groups = new ArrayList<ResourceTag>();
				KbeeEResources resources = new KbeeEResources();
				KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
				resources.setModel(model);
				resources.setName("resources");
				components.add(resources);
			}
			for (ResourceTag group : groups) {
				EFormAbstractField<?> field;
				if (group.isMultiple()) {
					KbeeEResources resources = new KbeeEResources();
					KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
					model.setTagName(group.getAlias());
					resources.setModel(model);
					resources.setName(group.getName()==null ? "resources" : "resources"+group.getName().toLowerCase());
					field = resources;
				}
				else {
					KbeeEResource resource = new KbeeEResource();
					KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
					model.setTagName(group.getAlias());
					resource.setModel(model);
					resource.setName(group.getName()==null ? "resources" : "resources"+group.getName().toLowerCase());
					field = resource;
				}
					field.setLabel(group.getName()==null ? "Resources" : group.getName());
					components.add(field);
			}
	
		return components;
	}
	
	private List<EFormField<?>> getFields(List<EFormComponent> components) {
		List<EFormField<?>> fields = new ArrayList<EFormField<?>>();
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				fields.add((EFormField<?>)component);
			}
			if (component instanceof EFormContainer) {
				fields.addAll(getFields(((EFormContainer)component).getComponents()));
			}
		}
		return fields;
	}
	
	private long getTotalMembers(DataSet dataSet) {
		return getContentDao().getTotalElements(dataSet);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public boolean hasToolbar() {
		return false;
	}
}

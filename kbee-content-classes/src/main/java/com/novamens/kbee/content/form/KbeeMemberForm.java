package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormMemberData;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.PropertiesFactory;

@SuppressWarnings("serial")
public class KbeeMemberForm  implements EForm, Serializable {
	private static final long serialVersionUID = 1L;
	private List<EFormComponent> components;
	private String name;
	private String cssClass;
	
	final boolean role_admin =
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_security = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean role_dataset_members	= role_model || role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_members_read = role_dataset_members || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	
	private static String defaultEditor =
		PropertiesFactory
			.getInstance("kbee")
			.getProperties()
			.getProperty("kbee.text.defaulteditor", null);
	
	public KbeeMemberForm(DataSetMember member) {
		this.components = getComponents(member);
		this.name = member.getDataSet().getAlias();
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
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
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
	public String getViewer() {
		return null;
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
	
	@Override
	public boolean hasToolbar() {
		return false;
	}
	
	protected List<EFormComponent> getComponents(DataSetMember member) {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
	
		List<ModelElementTemplate> structure = member.getDataSet().getStructure();
		
		if (editableDisplayName(member)) {
			EFormAbstractField<?> field = new KbeeEStringField();
			field.setName("value");
			field.setRequired(true);
			field.setLabel(getLabel("name"));
			KbeeEStringPropertyModel model = new KbeeEStringPropertyModel();
			model.setProperty("StrValue");
			field.setModel(model);
			components.add(field);
		}
		
		DataSet dataset = member.getDataSet();
		
		boolean hierarchicalSelector = false;
		
		KbeeEClassifierFieldModel hierarchicalmodel = null;
		if (dataset.isHierachical()) {
			hierarchicalmodel = new KbeeEClassifierFieldModel() {
				@Override
				public void set(Object object, List<DataSetMember> data) {
					((KbeeDataSetMember)object).setParents(data);
					super.set(object, data);
				}
				@Override
				public void set(Object object, Object data) {
					List<DataSetMember> parents = new ArrayList<>();
					parents.add((DataSetMember)data);
					((KbeeDataSetMember)object).setParents(parents);
					super.set(object, data);
				}
				@Override
				public DataSetMember get(Object object) {
					List<DataSetMember> parents = ((DataSetMember)object).getParents();
					return parents!=null && !parents.isEmpty() ? parents.get(0) : null; 
				}
				@Override
				public List<DataSetMember> getValues(Object object) {
					return ((DataSetMember)object).getParents();
				}
			};
		}
		
		if (structure.isEmpty()) 
			return components;
		
		for (ModelElementTemplate template : structure) {
			if (template instanceof ClassifierTemplate && ((ClassifierTemplate)template).getClassifier()!=null) {
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
				model.setAccessStrategy(getAccessStrategy(classifier));
				field.setRequired(Multiplicity.M1N.equals(multiplicity) || Multiplicity.M11.equals(multiplicity));
				field.setModel(model);
				field.setReadOnly(((ClassifierTemplate) template).isReadOnly());
				
				if (dataset.isHierachical() && classifier.getDataSet().equals(dataset)) {
					model = hierarchicalmodel;
					model.setClassifier(classifier);
					model.setAccessStrategy(AccessStrategy.ChildsEnabled);
					field.setModel(model);
					hierarchicalSelector = true;
					field.addValidation(new CycleValidation());
				}
				
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
					field = multiplicity.equals(Multiplicity.M0N) ||multiplicity.equals(Multiplicity.M1N) 
						? new KbeeEStringListField() 
						: new KbeeEStringField();
					model = new KbeeEStringAttributeModel();
					model.setAttribute(attribute);
					model.setParentClassifier((Classifier)template.getParent());
					field.setModel((KbeeEStringAttributeModel)model);
					field.setRequired(multiplicity.equals(Multiplicity.M11));
				}
				else
				if (AttributeType.TEXT.equals(attribute.getType())) {
					field = new KbeeEHtmlField();
					((KbeeEHtmlField)field).setEditor(defaultEditor);
					model = new KbeeEStringAttributeModel();
					model.setAttribute(attribute);
					((KbeeEHtmlField)field).setModel((KbeeEStringAttributeModel)model);
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
		
		if (dataset.isHierachical() && !hierarchicalSelector) {
			KbeeEClassifierFieldModel model = hierarchicalmodel;
			EFormAbstractField<DataSetMember> field = null;
			Classifier classifier = getClassifier(dataset);
			model.setClassifier(classifier);
			model.setAccessStrategy(AccessStrategy.All);
			field = new KbeeEMemberAutoCompleteField();
			field.setModel(model);
			field.setName(classifier.getAlias());
			field.setLabel(classifier.getDisplayName());
			//field.addValidation(new CycleValidator());
			components.add(field);
		}

	
		return components;
	}
	
	protected boolean editableDisplayName(DataSetMember member) {
		return (member.getDataSet().isDisplayNameEditable() || member.getDataSet().getStructure().isEmpty());
	}
	
	protected AccessStrategy getAccessStrategy(Classifier classifier) {
		AccessStrategy strategy;
		if (role_dataset_members_read) {
			strategy = AccessStrategy.All;
		}
		else {
			if (classifier.getDataSet().getDataSetType().equals(DataSetType.ENTITY)) {
				strategy = AccessStrategy.Managed;
			}
			else {
				if (classifier.getDataSet().isHierachical()) {
					strategy = AccessStrategy.ChildsEnabled;
				}
				else {
					strategy = AccessStrategy.All;
				}
			}
		}
		return strategy;
	}
	
	private String getLabel(String key) {
		Locale locale = getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault(); 
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), locale);
		return  resources.getString(key);
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
	
	private Classifier getClassifier(DataSet dataset) {
		for (Classifier classifier : getContentDao().getClassifiers(dataset.getDomain())) {
			if (classifier.getDataSet().equals(dataset)) {
				return classifier;
			}
		}
		return null;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	class CycleValidation implements EValidation, Serializable {
		@SuppressWarnings("unchecked")
		@Override
		public void validate(EValidatable validatable) {
			if (validatable.getValue() instanceof List<?>) {
				for (DataSetMember value : (List<DataSetMember>)validatable.getValue()) {
					validate(value, validatable);
				}
			}
			else {
				if (validatable.getValue()!=null) {
					validate((DataSetMember)validatable.getValue(), validatable);
				}
			}
		}
		public boolean isSubmit() {
			return true;
		}
		private void validate(DataSetMember value, EValidatable validatable) {
			value = (DataSetMember)getContentDao().reload(value); 
			if (value.getParents()==null) return;
			DataSetMember member = ((EFormMemberData)validatable.getData()).getMember();
			for (DataSetMember parent : value.getParents()) {
				if (parent.equals(member)) {
					validatable.error("error.cycle", validatable.getField().getLabel(), parent.getDisplayName());
					break;
				}
				else {
					validate(parent, validatable);
				}
			}
		}
	}

}

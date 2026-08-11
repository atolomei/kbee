package com.novamens.kbee.template;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.model.UserSet;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;

public class KbeeTemplateModelInfo implements TemplateModelInfo {
	private static final long serialVersionUID = 1L;

	private String name;
	private ModelType type;
	private String dataSet;
	private String template = null;
	private String description;
	private Multiplicity multiplicity;
	private List<TemplateModelInfo> elements = new ArrayList<TemplateModelInfo>();
	
	public KbeeTemplateModelInfo() {
	}
	
	public KbeeTemplateModelInfo(String name, ModelType type) {
		setName(name);
		setType(type);
	}
	
	public KbeeTemplateModelInfo(String name, ModelType type, String description) {
		setName(name);
		setType(type);
		setDescription(description);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ModelType getType() {
		return type;
	}
	
	public void setType(ModelType type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDataSet() {
		return dataSet;
	}

	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}

	public List<TemplateModelInfo> getElements() {
		return elements;
	}
	
	public void setElements(List<TemplateModelInfo> elements) {
		this.elements = elements;
	}
	
	public void add(TemplateModelInfo element) {
		elements.add(element);
	}
	
	public static KbeeTemplateModelInfo CreateFrom(TemplateModelInfo model) {
		if (ModelType.ACTIVITY.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("user", ModelType.USER));
			newmodel.add(new KbeeTemplateModelInfo("task", ModelType.TASK));
			newmodel.add(new KbeeTemplateModelInfo("starttime", ModelType.DATE));
			newmodel.add(new KbeeTemplateModelInfo("duedate", ModelType.DATE));
			newmodel.add(new KbeeTemplateModelInfo("url", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("note", ModelType.STRING));
			return newmodel;
		}
		if (ModelType.USER.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("username", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("firstname", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("firstname", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("lastname", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("lastfirstname", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("name", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("email", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("domain", ModelType.DOMAIN));
			return newmodel;
		}
		if (ModelType.TASK.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("name", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("displayname", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("procedure", ModelType.PROCEDURE));
			return newmodel;
		}
		if (ModelType.PROCEDURE.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("displayname", ModelType.STRING));
			return newmodel;
		}
		if (ModelType.RESOURCE.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("name", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("title", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("size", ModelType.NUMBER));
			newmodel.add(new KbeeTemplateModelInfo("url", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("publicurl", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("thumbnail980", ModelType.STRING));
			return newmodel;
		}
		if (ModelType.TEMPLATE.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("name", ModelType.STRING));
			return newmodel;
		}
		if (ModelType.SIGNED.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("date", ModelType.DATE));
			newmodel.add(new KbeeTemplateModelInfo("signature", ModelType.SIGNATURE));
			return newmodel;
		}
		if (ModelType.SIGNATURE.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("user", ModelType.USER));
			KbeeTemplateModelInfo personmodel = new KbeeTemplateModelInfo("person", ModelType.PERSON);
			DataSet personset = getPersonSet();
			if (personset!=null) personmodel.setDataSet(personset.getAlias());
			newmodel.add(personmodel);
			newmodel.add(new KbeeTemplateModelInfo("image", ModelType.RESOURCE));
			return newmodel;
		}
		if (ModelType.DOMAIN.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("name", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("organization", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("logo", ModelType.RESOURCE));
			return newmodel;
		}
		if (ModelType.DEVICE.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo();
			newmodel.setName(model.getName());
			newmodel.setType(model.getType());
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("description", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("owner", ModelType.USER));
			newmodel.add(new KbeeTemplateModelInfo("registrationUrl", ModelType.STRING));
			return newmodel;
		}
		if (ModelType.VALUE.equals(model.getType())) {
			ContentDao dao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
			Domain domain = ServiceLocator.getService(UserService.class).getDomain();
			for (Classifier classifier : dao.getClassifiers(domain)) {
				if (classifier.getAlias()!=null && classifier.getAlias().equals(model.getName())) {
					KbeeTemplateModelInfo newmodel = CreateFrom(classifier.getDataSet());
					newmodel.setName(model.getName());
					newmodel.setType(model.getType());
					return newmodel;
				}
			}
		}
		if (ModelType.PERSON.equals(model.getType())) {
			DataSet personset = getPersonSet();
			if (personset!=null) {
				KbeeTemplateModelInfo newmodel = CreateFrom(personset);
				newmodel.setName(model.getName());
				newmodel.setType(model.getType());
				return newmodel;
			}
		}
		if (ModelType.CONTENT.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo(model.getName(), ModelType.CONTENT, "");
			newmodel.setTemplate(model.getTemplate());
			return newmodel;
		}
		if (ModelType.RELATION.equals(model.getType())) {
			KbeeTemplateModelInfo newmodel = new KbeeTemplateModelInfo(model.getName(), ModelType.RELATION, "");
			newmodel.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("title", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("subtitle", ModelType.STRING));
			newmodel.add(new KbeeTemplateModelInfo("url", ModelType.STRING));
			return newmodel;
		}
		return null;
	}
	
	public static List<TemplateModelInfo> getContentModels(String templatename) {
		List<TemplateModelInfo> models = new ArrayList<TemplateModelInfo>();
		ContentDao dao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		for (ContentTemplate template : dao.getTemplates(domain)) {
			if (templatename==null || templatename.equals(template.getName()))
			models.add(CreateFrom(template));
		}
		return models;
	}
	
	public static KbeeTemplateModelInfo CreateFrom(Json json) {
		return null;
	}
	
	public static KbeeTemplateModelInfo CreateFrom(DataSet dataset) {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		model.setName(dataset.getName());
		model.setType(ModelType.VALUE);
		model.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("display-name", ModelType.STRING));
		if (dataset instanceof PersonSet) {
			model.add(new KbeeTemplateModelInfo("firstName", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("lastName", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("email", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("phone", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("workPosition", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("lastFirstName", ModelType.STRING));
			model.add(new KbeeTemplateModelInfo("firstLastName", ModelType.STRING));
		}
		for (ModelElementTemplate elementTemplate :dataset.getStructure()) {
			if (elementTemplate instanceof ClassifierTemplate && ((ClassifierTemplate)elementTemplate).getClassifier()!=null) {
				model.add(getModel(((ClassifierTemplate)elementTemplate).getClassifier(), elementTemplate.getMultiplicity()));
			}
			if (elementTemplate instanceof AttributeTemplate && ((AttributeTemplate)elementTemplate).getAttribute()!=null) {
				model.add(getModel(((AttributeTemplate)elementTemplate).getAttribute(), elementTemplate.getMultiplicity()));
			}
		}
		return model;
	}
	
	public static KbeeTemplateModelInfo GetGlobals() {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		model.setName("Global");
		model.setType(ModelType.COMPOUND);
		model.add(new KbeeTemplateModelInfo("application-name", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("application-fullname", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("my-tasks-url", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("pending-tasks-url", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("library-url", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo(".now", ModelType.DATE, getLabel("now.description")));
		return model;
	}
	
	public static KbeeTemplateModelInfo CreateFrom(ContentTemplate template) {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		model.setName(template.getName());
		model.setType(ModelType.CONTENT);
		model.add(new KbeeTemplateModelInfo("id", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("oid", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("domain", ModelType.DOMAIN));
		model.add(new KbeeTemplateModelInfo("title", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("subtitle", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("publicurl", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("taskurl", ModelType.STRING));
		model.add(new KbeeTemplateModelInfo("contenttemplate", ModelType.TEMPLATE));
		
		KbeeTemplateModelInfo resourcesmodel = new KbeeTemplateModelInfo();
		resourcesmodel.setName("resource");
		resourcesmodel.setMultiplicity(Multiplicity.M0N);
		resourcesmodel.setType(ModelType.RESOURCE);
		model.add(resourcesmodel);
		
		for (ModelElementTemplate elementTemplate :template.getStructure()) {
			if (elementTemplate instanceof ClassifierTemplate) {
				model.add(getModel(((ClassifierTemplate)elementTemplate).getClassifier(), elementTemplate.getMultiplicity()));
			}
			if (elementTemplate instanceof AttributeTemplate) {
				model.add(getModel(((AttributeTemplate)elementTemplate).getAttribute(), elementTemplate.getMultiplicity()));
			}
		}
		
		for (RelationTemplate relation : template.getRelations()) {
			KbeeTemplateModelInfo relationmodel = new KbeeTemplateModelInfo(relation.getName(), ModelType.RELATION);
			relationmodel.setMultiplicity(relation.getMultiplicity());
			model.add(relationmodel);
		}
		
		return model;
	}
	
	private static DataSet getPersonSet() {
		DataSet personset = null, userset = null;
		ContentDao dao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		for (DataSet dataset : dao.getDataSets(domain)) {
			if (dataset instanceof PersonSet) personset = dataset;
			if (dataset instanceof UserSet) userset = dataset;
		}
		personset = personset!=null ? personset : userset;
		return personset;
	}
	
	private static KbeeTemplateModelInfo getModel(Classifier classifier, Multiplicity multiplicity) {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo(classifier.getAlias(), ModelType.VALUE, "");
		model.setDataSet(classifier.getDataSet().getDisplayName());
		model.setMultiplicity(multiplicity);
		return model;
	}
	
	private static KbeeTemplateModelInfo getModel(Attribute attribute, Multiplicity multiplicity) {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		ModelType type =  ModelType.STRING;
		if (AttributeType.DATE.equals(attribute.getType()))
			type = ModelType.DATE;
		model.setName(attribute.getAlias());
		model.setType(type);
		model.setMultiplicity(multiplicity);
		return model;
	}
	
	private static String getLabel(String key) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeTemplateModelInfo.class.getName(), getSessionUser().getLocale());
		return res.getString(key);
	}
	
	private static KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}
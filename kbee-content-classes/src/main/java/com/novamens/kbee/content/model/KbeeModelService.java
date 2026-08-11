package com.novamens.kbee.content.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelReference;
import com.novamens.content.model.ModelService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;

import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;

public class KbeeModelService implements ModelService {
	
	
	
	private final static String TEMPLATE_REFERENCE = "template-reference";
	private final static String ROLE_REFERENCE = "role-reference";
	private final static String DATASET_REFERENCE = "dataset-reference";
	private final static String ALERT_REFERENCE = "alert-reference";
	private final static String LIBRARY_REFERENCE = "library-reference";
	
	private Domain  domain;
	
	
	public KbeeModelService() {
	}
	
	public KbeeModelService(Domain domain) {
		 this.domain = domain;
	}
	
	public Domain getDomain() {
		return domain;
	}

	
	private Locale getLocale() {
		return getDomain()!=null  ?getDomain().getLocale() : getLocale();
	}
	
	@Override
	public List<ModelReference> getReferences(ModelElement element) {
		if (element instanceof Classifier) return getReferences((Classifier)element);
		if (element instanceof Attribute) return getReferences((Attribute)element);
		return null;
	}
	
	public List<ModelReference> getReferences(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		references.addAll(getTemplates(classifier));
		references.addAll(getDataSets(classifier));
		references.addAll(getRoles(classifier));
		references.addAll(getLibraries(classifier));
		references.addAll(getAlerts(classifier));
		return references;
	}	
	
	public List<ModelReference> getReferences(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		references.addAll(getTemplates(attribute));
		references.addAll(getDataSets(attribute));
		references.addAll(getRoles(attribute));
		references.addAll(getLibraries(attribute));
		references.addAll(getAlerts(attribute));
		return references;
	}
	
	
	private String getLabel(String label, Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeModelService.this.getClass().getName(), locale);
		return res.getString(label);
	}
	
	
	
	private List<ModelReference> getTemplates(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (ContentTemplate contenttemplate : getContentDao().getTemplates()) {
			for (ClassifierTemplate template : contenttemplate.getClassifiers()) {
				if (template.getClassifier().equals(classifier)) {
					KbeeModelReference reference = new KbeeModelReference();
					reference.setDescription(getLabel(TEMPLATE_REFERENCE, contenttemplate.getDisplayName()));
					reference.setObject(contenttemplate.getDisplayName());
					
					reference.setGroup( getLabel("template", getLocale()) );
					
					reference.setUrl("/model/contentclass/"+contenttemplate.getId());
					references.add(reference);
				}
			}
		}
		return references;
	}
	
	private List<ModelReference> getTemplates(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (ContentTemplate contenttemplate : getContentDao().getTemplates()) {
			for (AttributeTemplate template : contenttemplate.getAttributes()) {
				if (template.getAttribute().equals(attribute)) {
					KbeeModelReference reference = new KbeeModelReference();
					reference.setDescription(getLabel(TEMPLATE_REFERENCE, contenttemplate.getDisplayName()));
					reference.setObject(contenttemplate.getDisplayName());
					
					reference.setGroup( getLabel("template", getLocale()) );
					
					reference.setUrl("/model/contentclass/"+contenttemplate.getId());
					references.add(reference);
				}
			}
		}
		return references;
	}
	
	private List<ModelReference> getDataSets(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (DataSet dataset : getContentDao().getDataSets(classifier.getDomain())) {
			for (Classifier datasetclassifier : dataset.getClassifiers()) {
				if (datasetclassifier.equals(classifier)) {
					KbeeModelReference reference = new KbeeModelReference();
					reference.setDescription(getLabel(DATASET_REFERENCE, dataset.getDisplayName()));
					reference.setUrl("/model/datasets/"+dataset.getId());
					reference.setObject(dataset.getDisplayName());
					
					reference.setGroup( getLabel("dataset", getLocale()) );
					
					references.add(reference);
				}
			}
		}
		return references;
	}
	
	private List<ModelReference> getDataSets(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (DataSet dataset : getContentDao().getDataSets(attribute.getDomain())) {
			for (AttributeTemplate template : dataset.getAttributes()) {
				if (template.getAttribute().equals(attribute)) {
					KbeeModelReference reference = new KbeeModelReference();
					reference.setDescription(getLabel(DATASET_REFERENCE, dataset.getDisplayName()));
					reference.setUrl("/model/datasets/"+dataset.getId());
					reference.setObject(dataset.getDisplayName());
					reference.setGroup( getLabel("dataset", getLocale()) );
					

					references.add(reference);
				}
			}
		}
		return references;
	}
	
	private List<ModelReference> getRoles(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (Role role : getSecurityDao().getRoles(getDomain())) {
			if ((role instanceof EntityRole && classifier.equals(((EntityRole)role).getClassifier())) || includeClassifier(classifier, role.getCondition())) {
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(ROLE_REFERENCE, role.getDisplayName()));
				reference.setUrl("/security/roles/"+role.getId());
				reference.setObject(role.getDisplayName());
				
				reference.setGroup( getLabel("role", getLocale()) );
				references.add(reference);
			}
		}
		return references;
	}
	
	private List<ModelReference> getRoles(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (Role role : getSecurityDao().getRoles(getDomain())) {
			if (includeAttribute(attribute, role.getCondition())) {
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(ROLE_REFERENCE, role.getDisplayName()));
				reference.setUrl("/security/roles/"+role.getId());
				reference.setObject(role.getDisplayName());
				
				reference.setGroup( getLabel("role", getLocale()) );
				references.add(reference);
			}
		}
		return references;
	}
	
	private List<ModelReference> getAlerts(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		int i =0;
		for (ENotiRule rule : ServiceLocator.getService(ENotiRuleService.class).getEmailRules(getDomain())) {
			if (includeClassifier(classifier, ((KbeeENotiRule)rule).getCondition()) && i<=1000) {
				String displayname = rule.getDisplayName();
				displayname += rule.isSystem() ? " (System)" : " ("+rule.getOwner().getDisplayName()+")";
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(ALERT_REFERENCE, displayname));
				reference.setUrl("/emailnotifications/"+rule.getId());
				reference.setObject(displayname);
				
				reference.setGroup( getLabel("alerts", getLocale()) );
				
				references.add(reference);
				i++;
			}
		}
		return references;
	}
	
	private List<ModelReference> getAlerts(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		int i =0;
		for (ENotiRule rule : ServiceLocator.getService(ENotiRuleService.class).getEmailRules(getDomain())) {
			if (includeAttribute(attribute, ((KbeeENotiRule)rule).getCondition()) && i<=1000) {
				String displayname = rule.getDisplayName();
				displayname += rule.isSystem() ? " (System)" : " ("+rule.getOwner().getDisplayName()+")";
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(ALERT_REFERENCE, displayname));
				reference.setUrl("/emailnotifications/"+rule.getId());
				reference.setObject(displayname);
				
				reference.setGroup( getLabel("alerts", getLocale()) );
				references.add(reference);
				i++;
			}
		}
		return references;
	}
	
	private List<ModelReference> getLibraries(Classifier classifier) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (Library library : getLibraries(classifier.getDomain())) {
			if (includeClassifier(classifier, ((KbeeLibrary)library).getStatement())) {
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(LIBRARY_REFERENCE, library.getDisplayName()));
				reference.setUrl("/libraries/"+library.getId());
				reference.setObject(library.getDisplayName());
				
				reference.setGroup( getLabel("library", getLocale()) );
				references.add(reference);
			}
		}
		return references;
	}
	
	private List<ModelReference> getLibraries(Attribute attribute) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (Library library : getLibraries(attribute.getDomain())) {
			if (includeAttribute(attribute, ((KbeeLibrary)library).getStatement())) {
				KbeeModelReference reference = new KbeeModelReference();
				reference.setDescription(getLabel(LIBRARY_REFERENCE, library.getDisplayName()));
				reference.setUrl("/libraries/"+library.getId());
				reference.setObject(library.getDisplayName());
				
				reference.setGroup( getLabel("library", getLocale()) );
				references.add(reference);
			}
		}
		return references;
	}
	
	private List<Library> getLibraries(Domain domain) {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class).findAll(domain);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	public String getLabel(String key, String... parameter) {
		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), getLocale());
		String label = res.getString(key);
		for (int p=0; p<parameter.length; p++) {
			label = label.replace("{"+String.valueOf(p)+"}", parameter[p]);
		}
		return label;
	}
	
	private boolean includeClassifier(Classifier classifier, String condition) {
		
		if ("".equals(condition) || condition==null) return false;
		
		try {
			Expression iqlexpression = getDomain().getService(IqlService.class).getExpression(condition);
			PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
			Map<String, List<String>> predicates = evaluator.evaluate();
			for (String predicate : predicates.keySet()) {
				if (classifier.getPredicate()!=null && predicate.toLowerCase().equals(classifier.getPredicate().toLowerCase())) {
					return true;
				}
				if (predicate.equals("c"+classifier.getId())) {
					return true;
				}
			}
		}
		catch (Exception e) {
		}
		
		return false;
	}
	
	private boolean includeAttribute(Attribute attribute, String condition) {
		
		if ("".equals(condition) || condition==null) return false;
		
		try {
			Expression iqlexpression = getDomain().getService(IqlService.class).getExpression(condition);
			PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
			Map<String, List<String>> predicates = evaluator.evaluate();
			for (String predicate : predicates.keySet()) {
				if (attribute.getPredicate()!=null && predicate.toLowerCase().equals(attribute.getPredicate().toLowerCase())) {
					return true;
				}
				if (predicate.equals("a"+attribute.getId())) {
					return true;
				}
			}
		}
		catch (Exception e) {
		}
		
		return false;
	}

}

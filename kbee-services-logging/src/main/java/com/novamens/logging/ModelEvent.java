package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;

/** 
 * <ul>
 * 
 * 		Model
 * 		-----	
 * 		<li>{@link DataSet}</li>
 * 		<li>{@link Attribute}</li>
 * 		<li>{@link Classifier}</li>
 * 		<li>{@link ContentTemplate}</li>
 * 
 * 
 * 		<li>{@link EmailTemplate}</li>
 * 
 * 		see {@link	DOMObjectService} 
 * 
 * </ul>
 */
@Entity
@DiscriminatorValue("ModelEvent")
public class ModelEvent extends AbstractObjectEvent {
	
	public ModelEvent() {
		super();
		setAuditSet(AuditSet.MODEL);
	}
	
								
	/** Workflow 
	 * 
	 * ProcessLauncher
	 * LauncherGroup
	 * 
	 * Procedure
	 * 
	 * 
	 * */
	public ModelEvent(LauncherGroup lg, String description) {
		super();
		setLauncherGroup( lg);
		setAuditSet(AuditSet.MODEL);
		setParameters(description);
		
	}
	
	public void setLauncherGroup(LauncherGroup lg) {
		setObjectId((new ObjectId(lg)).toString());
		setKbeeClass(LauncherGroup.class.getSimpleName());
		setDomainId((Long) lg.getDomain().getId());
		setTitle(lg.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public ModelEvent(ProcessLauncher pl, String description) {
		super();
		setLauncher(pl);
		setAuditSet(AuditSet.MODEL);
		setParameters(description);
	}
	
	public void setLauncher(ProcessLauncher pl) {
		setObjectId((new ObjectId(pl)).toString());
		setKbeeClass(ProcessLauncher.class.getSimpleName());
		setDomainId((Long) pl.getDomain().getId());
		setTitle(pl.getDisplayName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public ModelEvent(Procedure proc, String description) {
		super();
		setProcedure(proc);
		setAuditSet(AuditSet.MODEL);
		setParameters(description);
	}
	
	public void setProcedure(Procedure p) {
		setObjectId((new ObjectId(p)).toString());
		setKbeeClass(Procedure.class.getSimpleName());
		// setDomainId((Long) p.getDomain().getId());
		setTitle(p.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	
	
	
	
	
	public ModelEvent(DataSet dataSet, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.MODEL);
		setDataSet(dataSet);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelEvent(Classifier classifier, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.MODEL);
		setClassifier(classifier);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelEvent(DataSet dataSet, String description) {
		super();
		setAuditSet(AuditSet.MODEL);
		setDataSet(dataSet);
		setParameters(description);
	}
					
	public ModelEvent(Classifier classifier, String description) {
		super();
		setAuditSet(AuditSet.MODEL);
		setClassifier(classifier);
		setParameters(description);
	}
	
	public ModelEvent(Attribute attribute, String description) {
		super();
		setAuditSet(AuditSet.MODEL);
		setAttribute(attribute);
		setParameters(description);
	}
	
	public ModelEvent(ContentTemplate template, String description) {
		super();
		setAuditSet(AuditSet.MODEL);
		setTemplate(template);
		setParameters(description);
	}
	
	public ModelEvent(ContentTemplate template, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.MODEL);
		setTemplate(template);
		setParameters(getDescription(updatedParts));
	}
					
	public ModelEvent(RelationTemplate template, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.MODEL);
		setRelationTemplate(template);
		setParameters(getDescription(updatedParts));
	}

	public ModelEvent(RelationTemplate template, String part) {
		super();
		setAuditSet(AuditSet.MODEL);
		setRelationTemplate(template);
		setParameters(part);
	}
	
	public ModelEvent(EmailTemplate template, String description) {
		super();
		setAuditSet(AuditSet.MODEL);
		setEmailTemplate(template);
		setParameters(description);
	}
	
	public void setDataSet(DataSet dataset) {
		setObjectId((new ObjectId(dataset)).toString());
		setKbeeClass(dataset.getName());
		setDomainId((Long) dataset.getDomain().getId());
		setTitle(dataset.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public void setRelationTemplate(RelationTemplate template) {
		setObjectId((new ObjectId(template)).toString());
		setKbeeClass(template.getName());
		setDomainId((Long) template.getDomain().getId());
		setTitle(template.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

	
	public void setTemplate(ContentTemplate template) {
		setObjectId((new ObjectId(template)).toString());
		setKbeeClass(template.getName());
		setDomainId((Long) template.getDomain().getId());
		setTitle(template.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	
	public void setEmailTemplate(EmailTemplate template) {
		setObjectId((new ObjectId(template)).toString());
		setKbeeClass(EmailTemplate.class.getName());
		setDomainId((Long) template.getDomain().getId());
		setTitle(template.getKey()+ ". " + template.getLanguage());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public void setClassifier(Classifier classifier) {
		setObjectId((new ObjectId(classifier)).toString());
		if (classifier.getDataSet()!=null)
		setKbeeClass("Classifier. " + classifier.getDataSet().getName());
		setDomainId((Long) classifier.getDomain().getId());
		setTitle(classifier.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public void setAttribute(Attribute attribute) {
		setObjectId((new ObjectId(attribute)).toString());
		setKbeeClass("Attribute");
		setDomainId((Long) attribute.getDomain().getId());
		setTitle(attribute.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Deprecated
	@Override
	public String getEventType() {
		return "Model";
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}
	
	/***
	 * Replaces getEventType
	 */
	@Override
	public String getType() {
		return "Model";
	}
	
	@Override
	public String getTarget() {
		return getKbeeClass() + " - "  + getObjectId()!=null? getObjectId().toString() : "[null]";
	}
	
	/**
	 * DataSet, Classifier, Content Class
	 */
	@Override
	public String getObjectClass() {
		return getKbeeClass();  // DataSet, Classifier, ContentClass, Attribute
	}
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
}

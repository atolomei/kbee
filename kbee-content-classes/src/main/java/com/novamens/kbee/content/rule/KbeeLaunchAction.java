package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.rule.LaunchAction;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.service.ServiceLocator;

public class KbeeLaunchAction extends KbeeAbstractAction implements LaunchAction, Serializable {
 	private static final long serialVersionUID = 1L;

	private Serializable launcherId;
	private String note;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Classificable entity) {
		ProcessLauncher launcher = getLauncher();
		Content content = ServiceLocator.getService(ContentFactoryService.class).create(launcher.getContentTemplate().getName());
		classify(content, entity);
		content.getService(WorkflowService.class).startProcess(launcher, getNote(), true);
		
		String s = ServiceLocator.getService(DateTimeService.class).format( 
				OffsetDateTime.now(), 
				ZoneId.of(content.getDomain().getTimeZone()).getId(), 
				content.getDomain().getLocale(), DateTimeService.Month_Day_Year);
				
		content.setTitle(getActionRuleName()+ " - " + entity.getDisplayName() + " - " + s);
		
		return content;
	}
	
	public ProcessLauncher getLauncher() {
		if (launcherId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		KbeeProcessLauncher launcher = (KbeeProcessLauncher)sf.getCurrentSession().load(KbeeProcessLauncher.class, this.launcherId);
		return launcher;
	}
	
	public void setLauncher(ProcessLauncher launcher) {
		this.launcherId = launcher.getId();
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	private void classify(Content content, Classificable entity) {
		DataSet dataset = ((EntityMember)entity).getDataSet();
		for (ModelElementTemplate template :  content.getContentTemplate().getStructure()) {
			if (template instanceof ClassifierTemplate) {
				Classifier classifier = ((ClassifierTemplate)template).getClassifier();
				if (classifier!=null && (classifier.getDataSet().equals(dataset) || dataset.equals(classifier.getDataSet2()))) {
					content.setClassification(classifier, (EntityMember)entity);
				}
			}
		};
	}
}
package com.novamens.kbee.content.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.content.model.MemberUpdateServiceRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

/**
 * Listener that listen for the modification of the classification of a datasetmember
 */
public class MemberClassificationUpdateListener implements EventListener {
	
	static private Logger logger = LogManager.getLogger(MemberClassificationUpdateListener.class.getName());
	
	public boolean listen(Event event) {
		return ((event instanceof AppCreateEvent || event instanceof AppUpdateEvent) && event.getObject() instanceof KbeeMemberClassification);
	}
	
	/**
	 * Change in the classification of a member: a new dataset member is selected for a classifier in the member structure
	 */
	public void onEvent(Event event) {
		KbeeMemberClassification classificationupdated = (KbeeMemberClassification)event.getObject(); 
		DataSetMember memberupdated = classificationupdated.getSource();
		for (DataSet dataset : getContentDao().getDataSets(memberupdated.getDomain())) {
			for (ModelElementTemplate template : dataset.getStructure()) {
				// I am looking for a dataset that has in its structure the modified classifier in the member and that the parent is the dataset of the modified member
				if (template!=null && template.getParent()!=null &&
					template.getParent() instanceof Classifier &&
					((Classifier)template.getParent()).getDataSet().equals(memberupdated.getDataSet()) &&
					template.getElement() instanceof Classifier &&
					!((ClassifierTemplate)template).getMultiplicity().isMultiple() &&
					((Classifier)template.getElement()).getDataSet().equals(classificationupdated.getDataSetMember().getDataSet())) {
					try {
						ServiceLocator.getService(SchedulerService.class).enqueue(new MemberUpdateServiceRequest(classificationupdated.getClassifier(), memberupdated));
					}
					catch (SchedulerException e) {
						logger.error(e);
					}
				}	
			}
		}
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
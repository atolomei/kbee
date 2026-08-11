package com.novamens.kbee.content.questionanswer;

import java.util.HashMap;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.questionanswer.Question;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.service.ServiceLocator;

public class QuestionUpdateListener implements EventListener {
	private ContentDao dao = null;
	private String typeClassiferName;
	private String typeMemberName;
	private static final ThreadLocal<Boolean> inprocess = new ThreadLocal<Boolean>();
	private Map<Long, Classifier> typesclassifiers = new HashMap<Long, Classifier>();
	private Map<Long, DataSetMember> typesmembers = new HashMap<Long, DataSetMember>();
	
	public boolean listen(Event event) {
		return event instanceof BeforeUpdateEvent && event.getObject() instanceof Question;
	}
	
	public void onEvent(Event event) {
		try {
			if (inprocess.get()!=null)
				return;
			else
				inprocess.set(true);
			
			Question question = (Question)event.getObject();
			Classifier typeclassifier = getTypeClassifier(question);
			
			if (typeclassifier==null) 
				return;
			
			DataSetMember typemember = getTypeMember(typeclassifier);
			
			if (typemember==null) 
				return;
			
			boolean classified = false;
			for (Classification classification : question.getClassification()) {
				if (classification.getClassifier().getId().equals(typeclassifier.getId())) {
					if (classification.getDataSetMember().getId().equals(typemember.getId())) {
						classified = true;
						break;
					}
				}
			}
			if (!classified) {
				question.addClassification(typeclassifier, typemember);
			}
		}
		finally {
			inprocess.remove();
		}
	}
	
	public Classifier getTypeClassifier(Question question) {
		Classifier typeclassifier = null;
		typeclassifier = typesclassifiers.get(question.getDomain().getId());
		
		if (typeclassifier!=null) 
			return typeclassifier;
		
		for (Classifier classifier : getContentDao().getClassifiers(question.getDomain().getId())) {
			if (classifier.getName().equals(getTypeClassifierName())) {
				typeclassifier = classifier;
				break;
			}
		};
		if (typeclassifier!=null) typesclassifiers.put((Long)question.getDomain().getId(), typeclassifier);
		return typeclassifier;
	}
	
	public DataSetMember getTypeMember(Classifier classifier) {
		DataSetMember typemember = null;
		typemember = typesmembers.get(classifier.getId());
		if (typemember!=null) return typemember;
		for (DataSetMember member : getContentDao().getMembers(classifier.getDataSet(),null)) {
		if (member.getStrValue().equals(getTypeMemberName())) {
				typemember = member;
				break;
			}
		}
		if (typemember!=null) typesmembers.put((Long)classifier.getId(), typemember);
		return typemember;
	}
	
	public String getTypeClassifierName() {
		return typeClassiferName;
	}
	
	public void setTypeClassifierName(String name) {
		this.typeClassiferName = name;
	}
	
	public void setTypeMemberName(String name) {
		this.typeMemberName = name;
	}
	
	public String getTypeMemberName() {
		return typeMemberName;
	}
	
	private ContentDao getContentDao() {
		if (dao==null)	 {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			dao = (ContentDao) beans.getBean("contentDao");
		}
		return dao;
	}
}
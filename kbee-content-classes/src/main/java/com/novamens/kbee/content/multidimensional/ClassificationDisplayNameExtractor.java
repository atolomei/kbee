package com.novamens.kbee.content.multidimensional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.event.EventsDispatcher;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReindexByCriteriaCommand;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

/**
 * evento que reindexa lo que tiene que indexar al cambiar el display name de un objecto  
 * 
 *
 */
public class ClassificationDisplayNameExtractor implements Extractor, EventListener {

	private static Logger logger = kbee.util.logging.Logger.getLogger(ClassificationDisplayNameExtractor.class.getName());
	
	private Classifier classifier;
	private Index index;
	private String idfield, namefield;
	private String type;

	public Object extract(Object object) throws IndexerException  {
		
		if (object==null)
			return null;
		
		if (!(object instanceof Content)) {
			if (object instanceof Classificable) {
				return extractclassification(object);
			}
		}
		
		Assert.isInstanceOf(Content.class, object);
		
		List<String> members = new ArrayList<String>();
		
		Content content = (Content)object;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		if (content.getClassification()!=null) {
			for (Classification classification : content.getClassification()) {
				Classifier classifier = classification!=null ? classification.getClassifier() : null;
				
				if (classifier!=null && classifier.equals(getClassifier())) {
					
					if (classifier.getDataSet()!=null &&  classifier.getDataSet().getDataSetType().equals(DataSetType.DATE)) {
						if (classification.getDateValue()!=null) {
							String datestring = formatter.format(classification.getDateValue());
							members.add(datestring);
							break;
						}
					}
					else {	
						DataSetMember member = classification.getDataSetMember();
						if (member!=null) {
							members.add(String.valueOf(member.getValue()));
						}	
						break;
					}
				}
			}
		}
		return members;
	}
	
	public Object extractclassification(Object object) throws IndexerException  {
		
		if (object==null)
			return null;
		
		Assert.isInstanceOf(Classificable.class, object);
		List<String> members = new ArrayList<String>();
		
		Classificable content = (Classificable)object;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		if (content.getClassification()!=null) {
			for (Classification classification : content.getClassification()) {
				Classifier classifier = classification!=null ? classification.getClassifier() : null;
				if (classifier!=null && classifier.equals(getClassifier())) {
					if (classifier.getDataSet().getDataSetType()!=null && classifier.getDataSet().getDataSetType().equals(DataSetType.DATE)) {
						if (classification.getDateValue()!=null) {
							String datestring = formatter.format(classification.getDateValue());
							members.add(datestring);
							break;
						}
					}
					else {	
						DataSetMember member = classification.getDataSetMember();
						if (member!=null) {
							members.add(String.valueOf(member.getValue()));
						}	
						break;
					}
				}
			}
		}
		return members;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public void setIdFieldName(String name) {
		this.idfield = name;
	}
	
	public String getIdFieldName() {
		return this.idfield;
	}
	
	public void setNameFieldName(String name) {
		this.namefield = name;
	}
	
	public String getNameFieldName() {
		return this.namefield;
	}

	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setEventsDispatcher(EventsDispatcher dispatcher) {
		dispatcher.addListener(this);
	}
	
	public void setIndex(Index index) {
		this.index = index;
	}
	
	public Index getIndex() {
		return index;
	}
	
	public void onEvent(Event event) {
		try {
			if (event instanceof HibernateUpdateEvent) {
				Assert.isInstanceOf(DataSetMember.class, event.getObject());
				HibernateUpdateEvent updateevent = (HibernateUpdateEvent)event;
				int i=0;
				for (String propertyName : updateevent.getPropertyNames()) {
					if ("strvalue".equals(propertyName)) {
						if (updateevent.getCurrentState()[i]!=null && 
							updateevent.getPreviousState()!=null &&
							!updateevent.getCurrentState()[i].equals(updateevent.getPreviousState()[i])) {
								DataSetMember member = (DataSetMember)event.getObject();
								if (getClassifier()!=null && getClassifier().getDataSet()!=null && !member.getDataSet().getId().equals(getClassifier().getDataSet().getId())) 
									return;
								SolrParametersQuery query = new SolrParametersQuery(getIndex());
								query.getParameters().put(getIdFieldName(), String.valueOf(member.getId()));
								if (getType()!=null) {
									query.getParameters().put("type", getType());
								}
								
								ReindexByCriteriaCommand command = new ReindexByCriteriaCommand(query, index, getNameFieldName());
								command.setName("Reindex Value \""+ member.getDisplayName() + "\"");
								command.setParameter("condition", member.getDisplayName());
								command.setDescription("Reindex Value \""+ member.getDisplayName() + "\"");
								ServiceLocator.getService(CommandService.class).add(command);
						}
					}
					else {
						i++;
					}
				}
			}	
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}

	public boolean listen(Event event) {
		return true;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
}

package com.novamens.kbee.content.multidimensional;

import org.springframework.util.Assert;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReindexHierarchyCommand;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

public class HierarchicalNodeUpdateListener implements EventListener {

	private static Logger logger = kbee.util.logging.Logger.getLogger(ClassificationDisplayNameExtractor.class.getName());
	
	private Classifier classifier;
	private Index index;
	
	public HierarchicalNodeUpdateListener(Classifier classifier) {
		this.classifier = classifier;
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
				DataSetMember member = (DataSetMember)event.getObject();
				if (member.getDataSet().equals(getClassifier().getDataSet())) {
					ReindexHierarchyCommand command = new ReindexHierarchyCommand(index);
					command.setName("Reindex Value \""+ member.getDisplayName() + "\"");
					command.setParameter("member", member);
					command.setDescription("Reindex Value \""+ member.getDisplayName() + "\"");
					ServiceLocator.getService(CommandService.class).add(command);
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

package com.novamens.kbee.content.text;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.document.IDoc;
import com.novamens.content.service.ContentService;
import com.novamens.content.text.TextChange;
import com.novamens.content.text.TextPart;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.base.KbeeContentLink;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

public class CheckinListener implements EventListener {
	
	static private Logger logger = new Logger(CheckinListener.class.getName());
																					
	public boolean listen(Event event) {
		return ((event instanceof AppCheckinEvent) && event.getObject() instanceof IDoc);
	}
	
	public void onEvent(Event event) {
		try {
			Content content = (Content)event.getObject();
			KbeeText text = (KbeeText)content.getService(ContentService.class).getText();
			if (text!=null) {
				List<TextChange> changes = content.getService(ContentService.class).getTextChanges();
				if (changes!=null && !changes.isEmpty()) {
					boolean links = false;
					List<TextPart> parts = text.getParts();
					for (TextChange change : changes) {
						for (ContentLink reverselink : content.getReverseLinks()) {
							TextPart part = getPart(parts, reverselink.getAnchor());
							if (part!=null && 
								part.getName().equals(change.getPart().getName()) ||
								isChild(parts, change.getPart(), part)) { 
								((KbeeContentLink)reverselink).setTargetUpdated(true);
								links = true;
							}
						}
					}
					if (links) {
						ServiceLocator.getService(SchedulerService.class).enqueue(new TextChangeServiceRequest(content));
					}
				}
			}
		}
		catch(Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		
	}
	
	private TextPart getPart(List<TextPart> parts, String anchor) {
		for (TextPart part : parts) {
			if (part.getName().equals(anchor))
				return part;
		}
		return null;
	}
	
	private boolean isChild(List<TextPart> parts, TextPart parent, TextPart child) {
		boolean parentfound = false;
		for (TextPart part : parts) {
			if (part.getName().equals(parent.getName())) {
				parentfound = true;
			}
			else {
				if (parentfound) {
					if (part.getLevel()<=parent.getLevel()) {
						return false;
					}
					else {
						if (child!=null && part.getName().equals(child.getName())) {
							return true;
						}
					}
				}
			}	
		}
		return false;
	}

}
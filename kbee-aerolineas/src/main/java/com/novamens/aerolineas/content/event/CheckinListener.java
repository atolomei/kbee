package com.novamens.aerolineas.content.event;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.novamens.aerolineas.content.command.NotifyPublicationServiceRequest;
import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.datetime.DateTimeService;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class CheckinListener implements EventListener {
																					
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(CheckinListener.class.getName());
	
	public boolean listen(Event event) {
		return ((event instanceof AppCheckinEvent) && event.getObject() instanceof IDoc);
	}
	
	public void onEvent(Event event) {
		sendNotifications((Content)event.getObject());
		setExpirationDate((Content)event.getObject());
	}
	
	private void sendNotifications(Content content) {
		try {
			boolean notificaciones = false;
			for (Classification classification : content.getClassification()) {
				if (classification.getClassifier().getAlias().equals("acuserecibo") || classification.getClassifier().getAlias().equals("distribucion")) {
					notificaciones = true;
					break;
				}
			}
			if (notificaciones) {
				ServiceLocator.getService(SchedulerService.class).enqueue(new NotifyPublicationServiceRequest(content));	
			}
		}
		catch(SchedulerException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	private void setExpirationDate(Content content) {
		int months = getReviewFrequency(content);
		if (months<=0) 
			return;
		OffsetDateTime reviewDate = getReviewDate(content);
		if (reviewDate==null) 
			return;
		OffsetDateTime expirationDate = reviewDate.plus(months, ChronoUnit.MONTHS);
		Attribute expirationAttribute = getAttribute(content, "vencimiento");
		if (expirationAttribute==null) 
			return;
		String stringvalue = ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(expirationDate);
		List<String> values = new ArrayList<String>();
		values.add(stringvalue);
		content.setAttributeValues(expirationAttribute, values);
	}
	
	private int getReviewFrequency(Content content) {
		int frecuencyvalue = 0;
		Attribute frecuency = getAttribute(content, "frecuencia_revision");
		if (frecuency==null) return 0;
		List<String> values = content.getAttributeValues(frecuency);
		if (values==null|| values.isEmpty()) return 0;
		try {
			String stringvalue = values.get(0);
			frecuencyvalue = Integer.valueOf(stringvalue);
		}
		catch (NumberFormatException e) {
		}
		return frecuencyvalue;
	}
	
	private OffsetDateTime getReviewDate(Content content) {
		Attribute reviewDate = getAttribute(content, "fecha_revision");
		if (reviewDate==null || !reviewDate.isDate()) 
			return null;
		List<String> values = content.getAttributeValues(reviewDate);
		if (values.isEmpty())
			return null;
		try {
			OffsetDateTime date = ServiceLocator.getService(DateTimeService.class).parseStrDate(values.get(0));
			return date;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private Attribute getAttribute(Content content, String alias) {
		Attribute attribute = null;
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (template.getAttribute().getAlias().equals(alias)) {
				attribute = template.getAttribute();
				break;
			}
		}
		return attribute;
	}

}

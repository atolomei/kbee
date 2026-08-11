package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.service.ServiceLocator;



/**
 * 
 * Parameter -> 
 * 
 * Domain name
 * Classifier name (must be Classifier Date) 
 *
 */
public class ConvertDateClassifierToAttribute extends AsyncCommand {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConvertDateClassifierToAttribute.class.getName());

	private int processed = 0;
	
	public ConvertDateClassifierToAttribute() {
		setName("Convert deprecated Classifier Date -> Attribute Date");
	}
	
	
	private Classifier  dateClassifier;
	private Domain  domain;
	
	
	Attribute date_a;
	
	private Attribute getDateAttribute() {
		return date_a;
	}
	
	
	@Override
	protected void executeAsync() {

		try {
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			super.setState(CommandState.RUNNING);
			super.setDateStarted(OffsetDateTime.now());
			setProgress(0.0);
			
			if (getTargetDomain()==null) {
				logger.error("domain is null");
				return;
			}
				
			
			if (getClassifier()==null) {
				logger.error("dateClassifier is null");
				return;
			}
			
			
			com.novamens.hibernate.session.Session.open();

			createAttributeIfNotExists();
			
			if (date_a==null) {
				logger.error("Date Attribute is null");
				return;
			}
				
			

			
			
			
			
 			setProgress(100.0);
			end();
			super.setState(CommandState.COMPLETED);

		} catch (Exception e) {
				logger.error(e);
				super.setState(CommandState.ERROR);
				super.setResult(e.getClass().getSimpleName());
				super.setResultComments(e.getMessage());
				stop();
		}
		finally {
					
				super.setDateTerminated(OffsetDateTime.now());
				com.novamens.hibernate.session.Session.close();
				logger.debug("done  " + OffsetDateTime.now().toString());
		}
	}
	

	/**
	 * 
	 * 
	 */
	protected void createAttributeIfNotExists() {
		
		String name=getClassifier().getName().toLowerCase().trim();
		
		for (Attribute a: getContentDao().getAttributes(getTargetDomain())) {
			if (a.getName()!=null && a.getType()==AttributeType.DATE && a.getName().contentEquals(name)) {
				date_a=a;
				return;
			}
		}
		
		// ---------------
		// Creates the date attribute associated to this Date Classifier
		// ---------------
		
		Object cla = ServiceLocator.getService(ObjectFactoryService.class).createAttribute();
		((KbeeAttribute) cla).setName(name);
		((KbeeAttribute) cla).setAlias(getClassifier().getAlias());
		((KbeeAttribute) cla).setType(AttributeType.DATE);
		((KbeeAttribute) cla).setMultiplicity(Multiplicity.M01);
		
		date_a = ((KbeeAttribute) cla);
		date_a.getService(DOMObjectService.class).update("Created by Convert Command");
		
	}

	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private Domain getTargetDomain() {
		
		if (domain!=null)
			return domain;
		
		if (!getParameters().containsKey("domain")) { 
			logger.debug("getParameters().containsKey(\"domain\") -> false");
			return null; 
		}

		domain=getContentDao().findDomainByName((String) getParameters().get("domain"));

		if (domain==null)
			logger.debug("getContentDao().findDomainByName " + ((String) getParameters().get("domain")) + " is null");
		
		return domain;
	}

	/**
	 * @return
	 */
	private Classifier getClassifier() {
	
		if (dateClassifier!=null)
			return dateClassifier;
		
		if (!getParameters().containsKey("classifier")) { 
			logger.debug("getParameters().containsKey(\"classifier\") -> false");
			return null; 
		}
		
		String c_name = (String) getParameters().get("classifier");
		
		if ( c_name ==null) {
			logger.debug("parameter classifier name null");
			return null; 
		}
		
		if (getTargetDomain()==null) {
			logger.debug("domain is null");
			return null;
		}
		
		
		for (Classifier c: getContentDao().getClassifiers(getTargetDomain())) {
			if (c.getName()!=null && c.getName().toLowerCase().trim().equals( c_name)) {
				if (c.getDataSet().getDataSetType()==DataSetType.DATE) {
					dateClassifier = c;
					return dateClassifier;
				}
			}
		}
		
		return null;
	}

	
}

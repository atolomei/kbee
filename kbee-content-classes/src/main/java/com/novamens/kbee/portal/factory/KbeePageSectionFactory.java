package com.novamens.kbee.portal.factory;

import java.lang.reflect.InvocationTargetException;

import com.novamens.portal.factory.PageSectionFactory;
import com.novamens.portal.factory.PortalObjectFactory;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalObject;
import com.novamens.service.FactoryService;

public class KbeePageSectionFactory extends KbeePortalObjectFactory<PageSection> implements PageSectionFactory, FactoryService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePageSectionFactory.class.getName());
	
	@Override
	public PageSection create() {
		try {
			
			PageSection block;
			block = (PageSection) Class.forName(className).getDeclaredConstructor().newInstance();
			block.setTitle(super.title);
			block.setKey(super.key);
			block.setUsageInfoKey(getUsageInfoKey());
			return block;
			
		}
		
		catch (InvocationTargetException | NoSuchMethodException e	) {	logger.error(e);} 
		catch (InstantiationException e								) {	logger.error(e);} 
		catch (IllegalAccessException e								) {	logger.error(e);} 
		catch (RuntimeException e									) {	logger.error(e);} 
		catch (ClassNotFoundException e								) {	logger.error(e);}
		
		return null;
	}

	

	

}

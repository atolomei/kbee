package com.novamens.kbee.portal.factory;

import java.lang.reflect.InvocationTargetException;

import com.novamens.portal.factory.PortalObjectFactory;
import com.novamens.portal6.model.Page;


public class KbeePageFactory extends KbeePortalObjectFactory<Page> implements PortalObjectFactory<Page> {

	
private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePageFactory.class.getName());

@Override
public Page create() {
	try {
		
		Page block;
		
		block = (Page) Class.forName(className).getDeclaredConstructor().newInstance();
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

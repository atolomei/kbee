package com.novamens.kbee.portal.factory;

import java.lang.reflect.InvocationTargetException;

import com.novamens.portal.factory.AreaFactory;
import com.novamens.portal.factory.PortalObjectFactory;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.service.FactoryService;

public class KbeeAreaFactory implements AreaFactory, FactoryService {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeBlockFactory.class.getName());
	
	private String id;
	private String title;
	private String key;
	private String className;
	private String usageInfoKey;
	

	public KbeeAreaFactory() {
	}
	
	public KbeeAreaFactory(String id, String title,  String key,  String clazz, String usageinfokey) {
		this.id=id;
		this.className=clazz;
		this.key=key;
		this.id=id;
		this.title=title;
		this.usageInfoKey=usageinfokey;
	}

	@Override
	public Area create() {
		
		try {
			
			Area block;
			block = (Area) Class.forName(className).getDeclaredConstructor().newInstance();
			block.setTitle(this.title);
			block.setKey(this.key);
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
	
	
	
	public String getclassName() {
		return className;
	}

	public void setclassName(String clazz) {
		this.className = clazz;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDisplayName() {
		return title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public String getUsageInfoKey() {
		return usageInfoKey;
	}

	public void setUsageInfoKey(String usageinfokey) {
		this.usageInfoKey = usageinfokey;
	}


}

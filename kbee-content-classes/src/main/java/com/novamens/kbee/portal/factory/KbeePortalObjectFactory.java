package com.novamens.kbee.portal.factory;

import com.novamens.portal.factory.PortalObjectFactory;
import com.novamens.portal6.model.PortalObject;
import com.novamens.service.FactoryService;

public abstract class KbeePortalObjectFactory<T extends PortalObject> implements PortalObjectFactory<T>, FactoryService {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectFactory.class.getName());
	
	protected String id;
	protected String title;
	protected String key;
	protected String className;
	protected String usageInfoKey;
	

	public KbeePortalObjectFactory() {
	}
	
	public KbeePortalObjectFactory(String id, String title,  String key,  String clazz, String usageinfokey) {
		this.id=id;
		this.className=clazz;
		this.key=key;
		this.id=id;
		this.title=title;
		this.usageInfoKey=usageinfokey;
	}

	public abstract T create();
	
	
		
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

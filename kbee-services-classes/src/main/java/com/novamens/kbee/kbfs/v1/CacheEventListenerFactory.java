package com.novamens.kbee.kbfs.v1;

import java.util.Properties;

import net.sf.ehcache.event.CacheEventListener;

public abstract class CacheEventListenerFactory {
	public abstract CacheEventListener createCacheEventListener(Properties properties);
	
}

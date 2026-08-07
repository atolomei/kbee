package com.novamens.kbee.kbfs.v1;

import java.io.File;


import org.apache.logging.log4j.LogManager;

import com.novamens.util.KbeeFileUtils;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

public class FSEventListener implements CacheEventListener {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
		
	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	}

	@Override
	public void dispose() {
	}

	@Override
	public void notifyElementEvicted(Ehcache arg0, Element element) {
		removeFileFromDisk(element);
	}

	@Override
	public void notifyElementExpired(Ehcache ehcache, Element element) {
		removeFileFromDisk(element);
	}

	@Override
	public void notifyElementPut(Ehcache arg0, Element arg1) throws CacheException {
	}

	@Override
	public void notifyElementRemoved(Ehcache arg0, Element element) throws CacheException {
		removeFileFromDisk(element);
	}

	@Override
	public void notifyElementUpdated(Ehcache arg0, Element arg1) throws CacheException {
	}

	@Override
	public void notifyRemoveAll(Ehcache arg0) {
	}

	private void removeFileFromDisk(Element element) {
		if (element.getObjectValue()==null)
			return;
		
		File file = (File) element.getObjectValue();
		try {
				KbeeFileUtils.forceDelete(file);
			}
				catch (java.io.IOException e) {
					logger.error(e);
			}
	}
}

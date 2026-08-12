package com.novamens.indexer.java;


import java.lang.reflect.InvocationTargetException;

import com.novamens.indexer.service.IndexerException;
import com.novamens.util.JXPath;

public class JPathExtractor implements Extractor {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JPathExtractor.class.getName());
	
	private JXPath jpath;
	
	public JPathExtractor(String path) {
		setPath(path);
	}
	
	public void setPath(String path) {
		this.jpath = new JXPath(path);
	}
	
	public Object extract(Object object) throws IndexerException {
		try {
			return jpath.evaluateAll(object);
		}
		catch (InvocationTargetException e) {
			logger.error(e);		
			return null;
		}
		catch (IllegalAccessException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
}

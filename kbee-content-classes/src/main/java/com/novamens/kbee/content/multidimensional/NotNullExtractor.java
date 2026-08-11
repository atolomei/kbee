package com.novamens.kbee.content.multidimensional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.util.JXPath;

public class NotNullExtractor implements Extractor {
	private JXPath jxPath;
	
	public Object extract(Object object) throws IndexerException  {
		Object value = getValue(object);
		if (value==null) return "false";
		return "true";
	}
	
	public void setPath(String path) {
		jxPath = new JXPath(path);
	}
	
	public Object getValue(Object object) throws IndexerException {
		try {
			List<Object> values = jxPath.evaluateAll(object);
			return values!=null ? values.get(0) : null;
		}
		catch (IllegalAccessException e) {
			throw new IndexerException(e);
		}
		catch (InvocationTargetException e) {
			throw new IndexerException(e);
		}
	}
}

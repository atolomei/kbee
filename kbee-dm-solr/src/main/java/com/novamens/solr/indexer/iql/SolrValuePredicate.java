package com.novamens.solr.indexer.iql;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.util.JXPath;

public class SolrValuePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrValuePredicate.class.getName());
	
	private JXPath jpath;
	
	public String getCode(String argument) {
		return getPath() + ":" + argument;
	}
	
	private boolean isLibrary = false;
	private boolean isCanonical = false;
	
	public boolean isCanonical() {
		return isCanonical;
	}
	public void setCanonical(boolean b) {
		this.isCanonical=b;
	}
	
	public void setIsLibrary(boolean b) {
		this.isLibrary=b;
	}

	@Override
	public boolean isLibrary() {
		return this.isLibrary;
	}

	
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Content)) return false;
		
		boolean evaluation = false;
		
		Object value = extractValue(object);
		
		if (value!=null)
			if (value instanceof List) {
				List<?> values = (List<?>)value;
				if (values.size()==1)
					value = String.valueOf(values.get(0));
			}
			else {
				value = String.valueOf(value);
			}
		
		evaluation = value.equals(argument);
	
		return evaluation;
	}
	
	public void setJPath(String path) {
		this.jpath = new JXPath(path);
	}
	
	public Object extractValue(Object object)  {
		try {
			return jpath.evaluateAll(object);
		}
		catch (InvocationTargetException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

}

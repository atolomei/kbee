package com.novamens.solr.indexer.iql;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.util.JXPath;

public class FieldPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	private JXPath jpath;
	private String field;
	private boolean isCanonical = false;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FieldPredicate.class.getName());
	
	public FieldPredicate(String field) {
		this.field = field;
	}
	
	public String getCode(String argument) {
		return field+":" +argument;
	}
	
	public boolean isCanonical() {
		return isCanonical;
	}
	public void setCanonical(boolean b) {
		this.isCanonical=b;
	}
	
	public boolean isInformationModel() {
		return false;
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
		
		evaluation = ((String)value).contains((String)argument);
	
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

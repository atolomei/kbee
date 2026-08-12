package com.novamens.solr.indexer.iql;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.util.JXPath;

public class TitlePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	private JXPath jpath;
	
	public TitlePredicate() {
		setValueTypeDescription("Title String");
	}
	
	public String getCode(String argument) {
		return "(titlephonetic:" + argument + " OR title:" + argument + ")";
	}
	
	public boolean isCanonical() {
		return true;
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
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
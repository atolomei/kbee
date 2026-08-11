package com.novamens.kbee.content.library;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.novamens.indexer.query.Criteria;

public class ParametersCriteria implements Criteria {
	
	private Map<String, Object> parameters;
	
	public ParametersCriteria(String parameters) {
		setParameters(parseParameters(parameters));
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	protected Map<String, Object> parseParameters(String parametersstring) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		StringTokenizer tokenizer = new StringTokenizer(parametersstring, ";");
		while(tokenizer.hasMoreTokens()) {
			String parameterstring = tokenizer.nextToken();
			int i = parameterstring.indexOf("=");
			if (i>0) {
				String name  = parameterstring.substring(0,i);
				String value  = parameterstring.substring(i+1);
				parameters.put(name, value);
			}
		}
		return parameters;
	}

}

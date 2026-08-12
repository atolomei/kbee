package com.novamens.solr.indexer.iql;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.model.Attribute;

public class SolrAttributePredicate extends SolrAbstractPredicate implements AttributePredicate { //implements ClassifierPredicate {
	private Attribute attribute;
	
	public String getCode(String argument) {
		
		StringBuilder code = new StringBuilder();
		code.append(getPath() + ":" + argument + "*");
	
		return code.toString();
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}

	@Override
	public boolean isInformatioModel() {
		return true;
	}
	
	public String getHelpValueTypeDescription() {
		return "String";
	}
	
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Content)) return false;
		
		if (argument==null)
			return false;
		
		Content content = (Content)object;
		
		List<String> values = content.getAttributeValues(getAttribute());
		
		if ("null".equals(argument)) {
			return values.isEmpty();
		}
		
		for (String value : values) {
			if (value.equals(argument.toString())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setAttribute(Attribute attribute) {
		setPath(attribute.getUniqueName()+"name");
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	@Override
	public String getArgument(String value) {
		return value;
	}
}

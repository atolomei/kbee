package com.novamens.solr.indexer.iql;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.indexer.iql.CalculatedPredicate;

public class SolrGreaterThanPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
		
	private Attribute attribute;
	
	public boolean isInformationModel() {
		return true;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	@Override
	public boolean isTimed() {
		return false;
	}
	
	@Override
	public String getHelpValueTypeDescription() {
		return 	"numeric value";
	}
	
	public String getCode(String argument) {
		
		String code = getPath() +":["+ argument + " TO * ]";
		
		return code;
	}
	
	public boolean evaluate(Object object, Object argument) {
		
		if (!(object instanceof Content)) 
			return false;
		
		try {
			Content content = (Content)object;
			List<String> values = content.getAttributeValues(getAttribute());
			if (values.size()!=1) {
				return false;
			}
			else {
				if (Integer.valueOf(values.get(0))>Integer.valueOf((String)argument)) {
					return true;
				}
			}
		}
		catch (Exception e) {
			return false;
		}
		
		return false;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
		setPath(attribute.getUniqueName()+"name");
		setName(attribute.getName());
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
}

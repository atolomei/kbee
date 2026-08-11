package com.novamens.content.query;

import com.novamens.content.model.Attribute;
import com.novamens.indexer.query.ValueFilter;

public class AttributeFilter extends ValueFilter {
	private static final long serialVersionUID = 1L;

	public AttributeFilter(Attribute attribute, String value) {
		super(attribute.getUniqueName()+"name", value);
	}
	
	@Override
	public String getClause() {
		String value = super.value;
		if (value==null) value ="";
		value = value.trim();
		if (!value.endsWith("*")) value += "*";
		return "(" +super.name + ":" + value + "* OR "+
			super.name+":" + super.value +" OR "+
			super.name+":" + value.toUpperCase() +")";
	}
}

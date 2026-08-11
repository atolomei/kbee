package com.novamens.content.query;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.ValueFilter;

public class ClassifierFilter extends ValueFilter {
	private static final long serialVersionUID = 1L;

	public ClassifierFilter(Classifier attribute, DataSetMember value) {
		super(attribute.getUniqueName()+"member", String.valueOf(value.getId()), value.getDisplayName());
	}
	
	@Override
	public String getClause() {
		String value = super.value;
		if (value==null) value ="";
		value = value.trim();
		if (!value.endsWith("*")) value += "*";
		return "(" +super.name + ":" + value + "* OR "+super.name+":" + value.toUpperCase() +")";
	}
}

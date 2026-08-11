package com.novamens.content.iql;

import com.novamens.content.model.Attribute;
import com.novamens.indexer.iql.CalculatedPredicate;

public interface AttributePredicate extends CalculatedPredicate {
	public Attribute getAttribute();
}

package com.novamens.indexer.query;

import java.io.Serializable;

public interface Filter extends Serializable {
	public String getName();
	public String getDisplayName();
	public Serializable getValue();
	public String getDisplayValue();
	public String getClause();
}

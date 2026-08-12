package com.novamens.solr.indexer.iql;

import com.novamens.indexer.iql.Predicate;

public abstract class SolrAbstractPredicate implements Predicate  {
	
	private String path;
	private String name;
	private String valueType = TEXT_TYPE;
	
	public static String DATE_TYPE = "date";
	public static String TEXT_TYPE = "text";
	public static String BOOLEAN_TYPE = "boolean";
	
	private String valueTypeStr = TEXT_TYPE;

	@Override
	public String getValueTypeDescription() {
		return this.valueTypeStr;
	}
	
	public void setValueTypeDescription(String string) {
		valueTypeStr = string;
	}

	@Override
	public String getHelpValueTypeDescription() {
		return getValueTypeDescription();
	}
	
	public boolean isLibrary() {
		return false;
	}
	
	@Override
	public boolean isInformatioModel() {
		return false;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	@Override
	public boolean isTimed() {
		return false;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean validValue(String value) {
		return true;
	}
	
	public void setValueType(String type) {
		this.valueType = type;
	}
	
	public String getValueType() {
		return this.valueType;
	}
	
	public String getDefaultValue() {
		return "";
	};
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getArgument(String value) {
		return null;
	}
}
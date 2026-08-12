package com.novamens.indexer.iql;

public abstract class AbstractCalculatedPredicate implements CalculatedPredicate {
	private String name;
	private String valueType = TEXT_TYPE;
	
	public static String DATE_TYPE = "date";
	public static String TEXT_TYPE = "text";
	
	private String valueTypeStr;

	public String geValueTypeDescription() {
		return this.valueTypeStr;
	}
	
	public void setValueTypeDescription(String string) {
		valueTypeStr = string;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public boolean isCanonical() {
		return false;
	}
	
	
	public String getName() {
		return this.name;
	}
	
	public void setValueType(String type) {
		this.valueType = type;
	}
	
	public String getValueType() {
		return this.valueType;
	}
	
	public boolean validValue(String value) {
		return true;
	}
	
	public String getDefaultValue() {
		return "";
	}
	
	public String getArgument(String value) {
		return null;
	}
	
	public String getPath() {
		return null;
	}
}

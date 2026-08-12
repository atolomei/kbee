package com.novamens.indexer.iql;


/**
 * 
 * 
 * 
 * 
 *
 */
public abstract class AbstractPredicate implements Predicate {
	
	public static String DATE_TYPE = "date";
	public static String TEXT_TYPE = "text";

	private String name;
	
	private String valueType = TEXT_TYPE;
	private String valueTypeStr;
	
	
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getValueTypeDescription() {
		return (this.valueTypeStr!=null ? this.valueTypeStr : valueType);
	}
	
	
	@Override
	public boolean isLibrary() {
		return false;
	}
	
	@Override
	public boolean isInformatioModel() {
		return false;
	}
	
	@Override
	public boolean isTimed() {
		return false;
	}
	
	@Override
	public String getHelpValueTypeDescription() {
		return getValueTypeDescription();
	}
	
	public void setValueTypeDescription(String string) {
		valueTypeStr = string;
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
}

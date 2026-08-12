package com.novamens.indexer.iql;

public interface Predicate {
	
	public String getName();
	public boolean validValue(String value);
	public String getValueType();
	public String getValueTypeDescription();
	
	public String getHelpValueTypeDescription();
	
	
	public String getDefaultValue();
	public String getPath();
	public String getArgument(String value);
	public boolean evaluate(Object object, Object argument);
	
	/**
	 * Canonical 
	 * Model
	 * 
	 * @return
	 */
	public boolean isCanonical();
	public boolean isInformatioModel();
	public boolean isTimed();
	public boolean isLibrary();
	
}

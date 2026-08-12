package com.novamens.indexer.query;

import java.io.Serializable;

public abstract class AbstractFilter implements Filter {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public String value;
	public String displayValue;
	
	public AbstractFilter(String name, String value, String displayValue) {
		this.name = name;
		this.value = value;
		this.displayValue = displayValue;
	}
	
	public AbstractFilter(String name, String value) {
		this.name = name;
		this.value = value;
		this.displayValue = value;
	}
	
	public String getName() {
		return this.name;	
	}
	
	public String getDisplayName() {
		return this.name;
	}
	
	public Serializable getValue() {
		return this.value;	
	}
	
	public void setDisplayValue(String value) {
		this.displayValue = value;
	}
	
	public String getDisplayValue() {
		return this.displayValue;
	}
	
	protected String getAndWords(String value) {
		String stm = "";
		String words[] = value.split(" ");
		if (words.length>1) {
			stm += "(";
			for(int w=0; w<words.length; w++) {
				if (w>0) stm+= " AND ";
				stm+=words[w].trim()+"";
			}
			stm += ")";
		}
		else {
			stm = value.trim()+"";
		}
		return stm;
	}
	
	public String toString() {
		return  name + ":" + value;
	}
	
}

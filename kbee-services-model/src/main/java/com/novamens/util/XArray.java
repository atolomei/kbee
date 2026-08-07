package com.novamens.util;

import java.io.Serializable;

import com.novamens.security.Identifiable;


public class XArray implements Identifiable, Serializable {

	private static final long serialVersionUID = 1L;
	

	public String key;    			// Id
	public String display_label;   	// display label
	public String sort_label;   	// display label for sorting
	public String quantity;   		// quantity
	public String description;
	public String url;

	public XArray(String key, String display_label) {
		this(display_label, display_label, null, null, key, null);
	}
	
	public XArray(String display_label) {
		this(display_label, display_label, null, null, display_label, null);
	}
	
	public XArray(	String display_label, 
					String sort_label,  
					String value, 
					String description, 
					String key, 
					String url) {
		this.display_label = display_label;
		this.sort_label = sort_label;
		this.setValue(value);
		this.setDescription(description);
		this.key=key;
		this.url=url;
		
	}
	
	
	public String getUrl()  { return url;}
	public String getDisplayLabel() { return display_label;}
	
	public String getQuantity() { return quantity;}
	public String getKey() 	 { return key;}
	public String getSortLabel() {return this.sort_label != null ? this.sort_label : getDisplayName();}
	@Override
	public String getDisplayName() {
		return this.display_label;
	}

	
	
	public String toString() {
	
		StringBuilder str = new StringBuilder();
		str.append("key -> " + (key!=null ? key : "null"));
		str.append("| display_label  -> " + (display_label!=null ? display_label : "null"));
		str.append("| sort_label  -> " + (sort_label!=null ? sort_label : "null"));
		str.append("| quantity  -> " + (quantity!=null ? quantity : "null"));
		str.append("| description  -> " + (description!=null ? description : "null"));
		str.append("| url  -> " + (url!=null ? url : "null"));
		return str.toString();
		
				
	}
	public void setValue(String quantity) {
		this.quantity = quantity;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Serializable getId() {
		return key!=null ? key : display_label;
	}
	
	

	

	
}

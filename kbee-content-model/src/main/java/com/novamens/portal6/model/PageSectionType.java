package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum PageSectionType implements PersistentEnum {

	ONE_SECTION	 				(0, "1S"),

	TWO_SECTION_50X50			(10, "2S50x50"),
	
	TWO_SECTION_60X40			(20, "2S60x40"),
	TWO_SECTION_66X33			(30, "2S66x33"),
	TWO_SECTION_75X25			(40, "2S75x25"),
	
	TWO_SECTION_25X75			(50, "2S25x75"),
	TWO_SECTION_33X66			(60, "2S33x66"),
	TWO_SECTION_40X60			(70, "2S40x60");
	
	
	static public final PageSectionType ALL[] = {
	
			ONE_SECTION,
			TWO_SECTION_50X50,
			TWO_SECTION_60X40,
			TWO_SECTION_66X33,
			TWO_SECTION_75X25,
			TWO_SECTION_25X75,
			TWO_SECTION_33X66,
			TWO_SECTION_40X60
	};
	
	
	private String label;
	private int id;

	private  PageSectionType(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel(Locale.getDefault()));} 
	
	
	public String getDisplayName(Locale locale) {
		return getLabel(locale);
	}
	
	public String getDisplayName() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PageSectionType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	public int getId() {return id;}
	
	
	public String getKey() {
		return this.label;
	}

}

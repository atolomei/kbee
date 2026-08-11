package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum PageLayoutType implements PersistentEnum {
	
	PAGE_LAYOUT_1S	 				(0, "1S"),
	PAGE_LAYOUT_2S_50X50			(20, "2S_50x50"),
	
	PAGE_LAYOUT_2S_60X40			(30, "2S_60x40"),
	PAGE_LAYOUT_2S_66X33			(40, "2S_66x33"),
	PAGE_LAYOUT_2S_70X30			(50, "2S_70x30"),
	PAGE_LAYOUT_2S_75X25			(60, "2S_75x25"),
	PAGE_LAYOUT_2S_80X20			(70, "2S_80x20"),
	PAGE_LAYOUT_2S_90X10			(80, "2S_90x10"),
	
	
	PAGE_LAYOUT_2S_40X60			(90, "2S_40x60"),
	PAGE_LAYOUT_2S_33X66			(100, "2S_33x66"),
	PAGE_LAYOUT_2S_30X70			(110, "2S_30x70"),
	PAGE_LAYOUT_2S_25X75			(120, "2S_25x75"),
	PAGE_LAYOUT_2S_20X80			(130, "2S_20x80"),
	PAGE_LAYOUT_2S_10X90			(140, "2S_10x90");
	
	private String label;
	private int id;
											
	private PageLayoutType(int code, String label) {this.label = label;this.id = code;}

	public String toString()	{return ("id: " + getId() + ". label: "+ getLabel());} 
	public String getLabel() 	{return getLabel(Locale.getDefault());}
	public int getId() 			{return id;}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PageLayoutType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}


}

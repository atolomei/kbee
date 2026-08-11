package com.novamens.portal6.model;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

import antlr.collections.List;

public enum AreaType implements PersistentEnum {
			
	AREA_1S	 				(0, "1S", 		true, false, false,	"1S col-lg-12 col-md-12 col-xs-12 block-list-container", "", ""),
	
	
	AREA_3S_3x33 			(10, "3S3X33", 	true, true, true,	"3SL col-lg-4 col-md-6 col-xs-12 block-list-container", 
																"3SC col-lg-4 col-md-6 col-xs-12 block-list-container", 
																"3SR col-lg-4 col-md-6 col-xs-12 block-list-container"),
	
	
	AREA_3S_40x40x20 		(11, "3S40x40x20", 	true, true, true,	"3SL col-lg-5 col-md-6 col-xs-12 block-list-container", 
																	"3SC col-lg-5 col-md-6 col-xs-12 block-list-container", 
																	"3SR col-lg-2 col-md-6 col-xs-12 block-list-container"),

	
	AREA_3S_20x40x40 		(11, "3S20x40x40", 	true, true, true,	"3SL col-lg-2 col-md-6 col-xs-12 block-list-container", 
																	"3SC col-lg-5 col-md-6 col-xs-12 block-list-container", 
																	"3SR col-lg-5 col-md-6 col-xs-12 block-list-container"),

	
	
	
	AREA_2S_50X50			(20, "2S50x50", true, false, true,	"2SL col-lg-6 col-md-6 col-xs-12 block-list-container", 			"", "2SR col-lg-6 col-md-6 col-xs-12 block-list-container"),
	
	AREA_2S_60X40			(30, "2S60x40",  true, false, true,	"2SL col-lg-9 col-md-9 col-xs-12 block-list-container", 			"", "2SR col-lg-4 col-md-3 col-xs-12 block-list-container" ),
	
	
	AREA_2S_66X33			(40, "2S66x33" , true, false, true,	"2SL col-lg-8 col-md-8 col-xs-12 block-list-container", 			"", "2SR col-lg-4 col-md-4 col-xs-12 block-list-container"),
	AREA_2S_75X25			(50, "2S75x25",  true, false, true,	"2SL col-lg-10 col-md-10 col-xs-12 block-list-container",	 	    "", "2SR col-lg-2 col-md-2 col-xs-12 block-list-container"),
	
	AREA_2S_25X75			(60, "2S25x75",  true, false, true, 	"2SL col-lg-2 col-md-2 col-xs-12 block-list-container", 		"", "2SR col-lg-10 col-md-10 col-xs-12 block-list-container"),
	AREA_2S_33X66			(70, "2S33x66",  true, false, true,	"2SL col-lg-4 col-md-4 col-xs-12 block-list-container", 			"", "2SR col-lg-8 col-md-8 col-xs-12 block-list-container"),
	AREA_2S_40X60			(80, "2S40x60",  true, false, true,	"2SL col-lg-3 col-md-3 col-xs-12 block-list-container", 			"", "2SR col-lg-9 col-md-9 col-xs-12 block-list-container");

	
	static public final AreaType ALL[] = {
			AREA_1S, 
			
			AREA_3S_3x33,
			AREA_3S_40x40x20,
			AREA_3S_20x40x40,
			
			AREA_2S_50X50,
			AREA_2S_60X40,
			AREA_2S_66X33,
			AREA_2S_75X25,
			
			AREA_2S_25X75,
			AREA_2S_33X66,
			AREA_2S_40X60
			};
	
	private String label;
	private int id;
	
	private String css_left;
	private String css_right;
	private String css_center;
	
	private boolean has_right 	= false;
	private boolean has_center 	= false;
	private boolean has_left	= true;
	
	
	public boolean hasRight() {return this.has_right;}
	public boolean hasCenter() {return this.has_center;}
	public boolean hasLeft() {return this.has_left;}
	
	
	public String getSubSectionLeftCss () 		{		return this.css_left;	}
	public String getSubSectionRightCss ()	  	{		return this.css_right;	}
	public String getSubSectionCenterCss () 	{		return this.css_center;	}
	
	private AreaType(int code, String label, boolean hasLeft, boolean hasCenter, boolean hasRight, String css_left, String css_center, String css_right) {
	
		this.has_right 	= hasRight;
		this.has_center = hasCenter;
		this.has_left	= hasLeft;
		
		this.label = label;this.id = code; 
		this.css_left=css_left;
		this.css_center=css_center;
		this.css_right=css_right;
	}

	public String toString()	{return ("id: " + getId() + ". label: "+ getLabel());} 
	public String getLabel() 	{return getLabel(Locale.getDefault());}
	public int getId() 			{return id;}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(AreaType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}

}

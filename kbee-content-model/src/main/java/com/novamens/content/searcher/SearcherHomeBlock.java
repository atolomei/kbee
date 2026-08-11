package com.novamens.content.searcher;


import com.novamens.dom.DomainObject;
import com.novamens.dom.Json;

public interface SearcherHomeBlock extends com.novamens.dom.Object, DomainObject {
	
	static public final String CLASS_CODE = "SEHB";
	

	String getTitle();

	String getIQL();

	String getSortStr();

	Json getCustomValuesJson();

	String getAbstract();
	

	
	

}

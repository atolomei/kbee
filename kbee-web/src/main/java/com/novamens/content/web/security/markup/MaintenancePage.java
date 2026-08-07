package com.novamens.content.web.security.markup;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import kbee.util.PropertiesFactory;
import kbee.web.page.FactoryPage;

public class MaintenancePage extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage  implements FactoryPage {
	 
	static final String xcss = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.kbee.css", "/css/kbee.css");
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	 
	
	private static final long serialVersionUID = 8691387363711079330L;

	public MaintenancePage() {
		this(null);
	}
	
	public MaintenancePage(PageParameters parameters) {
		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
	}
	
}

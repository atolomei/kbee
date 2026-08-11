package com.novamens.kbee.portal.model;


import java.io.Serializable;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.PortalMenuItem;


public class KbeePortalMenuItem implements PortalMenuItem, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String label;
	private String displayname;
	private String href;
	private String id;
	private String display_panel_key; 
	private ObjectState state;
	
	
	public KbeePortalMenuItem() {
	}
	
	public KbeePortalMenuItem(String label) {
		this.label=label;
		this.id=label;
		this.displayname=label;
	}
	
	public KbeePortalMenuItem(String id, String label) {
		this.label=label;
		this.id=id;
		this.displayname=label;
	}
	
	public KbeePortalMenuItem(String id, String label, String href) {
		this.label=label;
		this.id=id;
		this.href=href;
		this.displayname=label;
	}
	
	
	public void setLabel(String s) {
		this.label=s;
	}
	
	public void setDisplayName(String s) {
		this.displayname=s;
	}
	
	public void setId(String s) {
		this.id=s;
	}
	
	public void setDisplayPanelKey(String s) {
		this.display_panel_key=s;
	}
	
	public void setHRef(String s) {
		this.href=s;
	}
	

	@Override
	public String getDisplayName() {
		return this.displayname;
	}
	
	@Override
	public String getHRef() {
		return this.href;
	}
	@Override
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String displayPanelKey() {
		return this.display_panel_key;
	}
	
	public String getMenuString() {
		return getLabel();
	}
	
	public String getMenuString(int level) {
		return String.format("%3d",level)+" " + getLabel();
	}

	@Override
	public void setState( ObjectState s) {
		state=s;
	}
	
	public String getTitle() {
		return getLabel();
	}
	
	public void setTitle(String s) {
		setLabel(s);
	}
	
	
	
	@Override
	public ObjectState getState() {
		return state;
	}

	@Override
	public String treeString() {
		return getLabel();
	}
}



/**
		PortalMenu menu 		= new KbeePortalMenuJson("menu");
		PortalMenu submenu 		= new KbeePortalMenuJson("submenu");
		
		PortalMenuItem i1=new KbeePortalMenuItemJson("Item 1");  
		PortalMenuItem i2=new KbeePortalMenuItemJson("Item 2");  
		PortalMenuItem i3=new KbeePortalMenuItemJson("Item 3");  
		PortalMenuItem i4=new KbeePortalMenuItemJson("Item 4");  
		PortalMenuItem i5=new KbeePortalMenuItemJson("Item 5");  
		
		menu.add(i1);  
		menu.add(i2);  
		menu.add(i3);  
		
		submenu.add(i4);  
		submenu.add(i5);  
		menu.add(submenu);  
		
		PortalMenu pm=new KbeePortalMenu();
		pm.setMenu(menu);
		
*/
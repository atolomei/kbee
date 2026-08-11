package com.novamens.portal6.model;

import java.util.List;

public interface PortalPersistentMenu extends PortalObject {
	
	public static final String KEY = "menu";
	
	public String getJsonMenu();
	public void setJSonMenu(String m);
	
	public int getOrder();
	public void setOrder(int n);
	
	public List<PortalMenuItem> getMenuItems();

	public void addMenuItem(PortalMenuItem item);
	public void add(PortalMenuItem item);
	
	
	public PortalMenu getPortalMenu();
	String treeString();
	
	
}

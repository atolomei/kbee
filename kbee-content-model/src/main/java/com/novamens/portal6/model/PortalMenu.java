package com.novamens.portal6.model;

import java.util.List;

public interface PortalMenu extends PortalMenuItem, PortalMenuContainer {

	public void add(PortalMenuItem item);
	public List<PortalMenuItem> getPortalMenuItems();
	
	
	public void seTitle(String s);
	public String getTitle();
	
}

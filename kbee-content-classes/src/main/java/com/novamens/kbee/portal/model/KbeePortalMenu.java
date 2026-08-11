package com.novamens.kbee.portal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;

public class KbeePortalMenu implements PortalMenu, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String label;
	private String id;
	private String displayname;
	private String display_panel_key;
	private String href;
	private ObjectState state;
	
	private List<PortalMenuItem> list = new ArrayList<PortalMenuItem>();
	
	@Override
	public String treeString() {
		StringBuilder str = new StringBuilder();
		str.append("Menu -> " + getLabel());
		for (PortalMenuItem m: this.getPortalMenuItems()) {
			str.append("\n");
			str.append(m.treeString());
			
		}
		return str.toString();
	}
	

/**
		
		PortalMenu menu 		= new KbeePortalMenu("menu");
		PortalMenu submenu 		= new KbeePortalMenu("submenu");
		
		PortalMenuItem i1=new KbeePortalMenuItem("Item 1");  
		PortalMenuItem i2=new KbeePortalMenuItem("Item 2");  
		PortalMenuItem i3=new KbeePortalMenuItem("Item 3");  
		PortalMenuItem i4=new KbeePortalMenuItem("Item 4");  
		PortalMenuItem i5=new KbeePortalMenuItem("Item 5");  
		
		menu.add(i1);  z|
		menu.add(i2);  
		menu.add(i3);  
		
		submenu.add(i4);  
		submenu.add(i5);  
		menu.add(submenu);  
		
		
		logger.debug(menu.getStringMenu());
*/
	
	public KbeePortalMenu() {
	}


	public KbeePortalMenu(String label) {
		this.label=label;
		this.id=label;
		this.displayname=label;
	}

	
	
	public KbeePortalMenu(PortalPersistentMenu source) {
		label = source.getName();
		id = source.getKey();
		displayname = source.getDisplayName();
		display_panel_key = "null";
		list=source.getMenuItems();
	}


	
	public void setLabel(String label) {
		this.label=label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDisplayName() {
		return this.displayname;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String displayPanelKey() {
		return this.display_panel_key;
	}

	@Override
	public void add(PortalMenuItem item) {
		list.add(item);
	}

	
	public void remove(PortalMenuItem item) {
		for(PortalMenuItem i :list) {
			if (item.getId().equals(i.getId())) {
				list.remove(i);
				return;
			}
				
		}
		
	}
	@Override
	public String getHRef() {
		return this.href;
	}
	
	
	
	
	public String getMenuString() {
			return getMenuString(0);
	}
	
	public String getMenuString(int level) {
		
		StringBuilder str = new StringBuilder();
		
		str.append("> "+String.format("%3d",level)+" " +getLabel());
		for (PortalMenuItem item:list) 
			str.append(item.getMenuString(level));
		
		
		return str.toString();
		
	}


	@Override
	public void setDisplayName(String s) {
		displayname=s;
		
	}


	@Override
	public void setId(String s) {
			id=s;
		
	}


	@Override
	public void setDisplayPanelKey(String s) {
			this.display_panel_key=s;
		
	}


	@Override
	public void setHRef(String s) {
			this.href=s;
	}


	@Override
	public ObjectState getState() {
		return this.state;
	}

	
	

	
	@Override
	public void setState(ObjectState s) {
		this.state=s;
	}


	@Override
	public List<PortalMenuItem> getPortalMenuItems() {
		return list;
	}


	@Override
	public void seTitle(String s) {
			setLabel(s);
	}


	@Override
	public String getTitle() {
		return getLabel();
	}


	@Override
	public void setTitle(String s) {
		setLabel(s);
		
	}
}

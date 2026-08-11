package com.novamens.kbee.portal.model;

import com.novamens.portal6.model.IPTab;

public class KbeeIPTab implements IPTab {

	String title;
	String key;
	int id = 0;
	String icon;
	boolean visible = true;
	
	
	public KbeeIPTab(String title, String key) {
		this(title, key, null);
	}
	
	public KbeeIPTab(String title, String key, String icon) {
		this.title=title;
		this.key=key;
		this.icon=icon;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void setVisible(boolean b) {
		this.visible=b;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	
	
}

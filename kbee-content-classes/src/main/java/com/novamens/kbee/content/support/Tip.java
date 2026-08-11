package com.novamens.kbee.content.support;


public interface Tip {

	
	public String FACTORY    = "factory";
	public String GENERAL   = "general";
	public String SECURITY  = "security";
	public String MODEL 	= "model";
	public String PORTAL	= "portal";
	
	public Long getId();
	public void setId(Long id);
	public String getTitle();
	public void setTitle(String title);
	public String getText();
	public void setText(String text);
	public String getLang();
	public void setLang(String lang);
	public String getTexyid();
	public void setTexyid(String texyid);
	
	// public TexyModel getTexy();
	
	public int getIndex();
	public void setIndex(int index);
	
	
	public String getArea();
	public void setArea(String area);
	
	
}

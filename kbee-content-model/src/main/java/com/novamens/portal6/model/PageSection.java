package com.novamens.portal6.model;


import java.util.List;

import com.novamens.dom.ObjectState;

public interface PageSection extends PortalObject,  PortalModel {

	public static final String KEY = "page-section";

	public PageSectionDisposition getPageSectionDisposition();
	public	PageSectionType getPageSectionType();
	
	public List<Area> getAreas();
	public List<Area> getAreas(int tab_index);
	public Area getArea(int orden);

	public List<IPTab> getTabs();
	public int getPTab();
	public void setPTab(int ptab);

	public void setHeader(boolean b);
	public boolean isHeader();

	
	public void add(Area area);
	public void remove(Area area);
	public Area getArea(String area_name);
		
	public void moveUp(Area area);
	public void moveDown(Area area);
	public  Area getPreviousArea(int current_area);
	public  Area getFollowingArea(int current_area);
	
	public int getOrder();
	public int getMaxElements();
	public void onAfterClone();
	public PageSection clone();
	
	public String treeString();
	public List<Area> getAreas(ObjectState state);
	void setOrder(int o);
	void setCss(String css);
	String getCss();
	
	
	
	
	 
}

package com.novamens.portal6.model;

import java.io.Serializable;
import java.util.List;

public interface Block extends PortalObject, PortalModel {
	
	public static final String KEY = "block";
	
	public static final int UNASSIGNED = -1;
	
	public AreaSection getAreaSection();
	public void setAreaSection(AreaSection section);
	
	public int getOrder();
	public void setOrder(int order);
	
	public void onAfterClone();
	public Block clone();

	public String treeString();
	
	public int getPTab();
	public void setPTab(int ptab);

	public List<IPTab> getTabs();
	
	public void setHeader(boolean b);
	public boolean isHeader();
	
	
	
	// public Serializable getOId();
	public void setOId(Long oid);
	public void setDefaults();
	
	public void setCss(String css);
	public String getCss();
	
	
}


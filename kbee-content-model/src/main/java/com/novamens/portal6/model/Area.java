package com.novamens.portal6.model;

import java.util.List;


import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.ObjectState;
 
public interface Area extends PortalObject,  PortalModel {

	public static final String KEY = "area";
	
	public void setHeader(boolean b);
	public boolean isHeader();
	
	public int getPTab();
	public void setPTab(int ptab);
	public List<IPTab> getTabs();
	
	public void add( Block block);
	public void add( Block block, AreaSection section);

	public void remove(Block block) throws ContentMgmtException;
	
	public List<Block> getBlocks(AreaSection section);
	public List<Block> getBlocks();

	public AreaType getAreaType();
	public void setAreaType(AreaType areatype);
	
	public  void moveDown(Block c_block);
	public void moveUp(Block c_block); 

	public boolean canMoveDown(Block c_block);
	public boolean canMoveUp(Block c_block);
	
	public int getOrder();

	public boolean equals(Area a);
	public boolean isInFullWidthCanvas();
	
	public Area clone();
	public void onAfterClone();
	
	public void detach(Block block);
	

	public String getCss();
	public void setCss(String areac);
	
	
	public String treeString();
	public List<Block> getBlocks(int tab_index);
	public void setOId(Long newOId);
	public void setDefaults();
	public List<AreaSection> getAreaSections();
	public List<Block> getBlocks(ObjectState state);
	void setOrder(int o);

	
}



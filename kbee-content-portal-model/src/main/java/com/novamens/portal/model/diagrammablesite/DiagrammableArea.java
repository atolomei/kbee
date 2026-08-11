package com.novamens.portal.model.diagrammablesite;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;

public interface DiagrammableArea extends Area  {

	static final public int AREA_1S 	  	  	= 1;
	static final public int AREA_2S_50x50 	  	= 2;
	static final public int AREA_2S_66x33 	  	= 3;
	static final public int AREA_2S_75x25 	  	= 4;
	static final public int AREA_2S_66x33_L2X1 	= 5;

	static final public int AREA_2S_33x66 	  	= 6;
	
	static final public int AREA_3S_33	 	  	= 12;
	static final public int AREA_FOOTER	  	  	= 8;
	static final public int AREA_HEADER	  	  	= 9;

	public int    getType();
	
	//public void add(DiagrammableBlock block);
	//public void add(DiagrammableBlock block, com.novamens.portal.model.Section section);
	//public void remove(DiagrammableBlock block) throws ContentMgmtException;
	//public List<Block> getBlocks(com.novamens.portal.model.Section section);
	//public List<Block> getBlocks();
	//public  void moveDown(DiagrammableBlock c_block);
	//public void moveUp(DiagrammableBlock c_block); 

	

	public boolean equals(DiagrammableArea a);

	public boolean isInFullWidthCanvas();
	public void setInFullWidthCanvas(boolean fwcanvas);
	
	public String getAreaTypeLabel();
	
	public void setAreaType(int area_type);
	
	public void setStateAll(ObjectState state);
	
	public List<com.novamens.portal6.model.AreaSection> getSectionsEnabled();
	
	public DiagrammableArea clone();
	public void onAfterClone();
	
	public void setMenusVisible(boolean b);
	public boolean isMenusVisible();
	// void detach(DiagrammableBlock block);
	void seetAreaClass(String areac);
	String getAreaClass();
	
	public int getImageFrameWidth(com.novamens.portal6.model.AreaSection section);
	public int getImageFrameHeight(com.novamens.portal6.model.AreaSection section);

	// public ResourceReference getImage();
	
	//public Panel getPanel(String name, boolean show_unpublished, DiagrammableSite callersite);
	//public Panel getPanel(String name);
	//public Panel getPanel(String name, boolean show_unpublished);

	
}

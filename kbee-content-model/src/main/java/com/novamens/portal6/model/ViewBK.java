package com.novamens.portal6.model;


import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;


/**
 * View Block
 * 	// public void setPosition(int position);
	// public int  getPosition();
public String getResponsePageAbsoluteLink();
public Long getOId();
public String getContentTypeAsString();
public String getViewTypeCss();
	public String getGlyphIcon();
	
 */
public interface ViewBK extends View, ResourceContainer {


	public KBFile getFile();
	public void setFile(KBFile file); 

	public Text getText();
	public void setText(Text text);
	
	
	public String getViewType();
	
	
	public void onClick();
	public Block getBlock();
	
	
	
	public Object getObject();
	public void evict();
	
	public String getStyle();
	public void setStyle(String sh);
	
	public ViewBK clone();
	
	
	public void setOpenNewTab(boolean new_tab);
	public boolean isOpenNewTab();
	
	public boolean isKBFile();
	
	public void 	setIconCss(String iconcss);
	public String 	getIconCss();
	String getTagline();
	void setTagline(String tagline);
	String getCss();
	void setCss(String css);
	
		
	
	
	
	
}

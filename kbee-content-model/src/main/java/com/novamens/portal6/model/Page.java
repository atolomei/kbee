package com.novamens.portal6.model;


import java.util.List;

import com.novamens.content.base.Content;

 
public interface Page extends PortalObject,  PortalModel {
	
	public static final String KEY = "page";

	public Content getContent();
	public String treeString();
	
	
	public List<IPTab> getTabs();
	
	public List<PageSection> getPageSections();
	
	public String getName();
	public boolean isAdminPage();
	
	public boolean isSiteSection();
	public boolean isHome();
	
	public int getOrder();
	public void setOrder(int size);
	
	public String getRelativeUrl();
	public PageType  getPageType();
	
	public void add(PageSection page_section);	
	
	public Page clone();
	public void onAfterClone();
	
	public String getContentShortenedId();



	public void setDefaults();
	
	
	/**
	 * IsContent is used to index
	 * @return
	 */
	boolean isContentPage();
	void setIsContentPage(boolean iscontent);
	
	/**
	 * internal page: 
	 * TopPageSection and BottomPageSection
	 * 
	 * @return
	 */
	boolean isRegularPage();
	public void setIsRegularPage(boolean isregular);


	/**
	 * Page is diagrammable or extenal
	 * @param is_sitesection
	 */
	void setSiteSection(boolean is_sitesection);
	
	void setBuildable(boolean b);
	boolean isBuildable();
	void setCss(String css);
	String getCss();
	
	
	
	

}




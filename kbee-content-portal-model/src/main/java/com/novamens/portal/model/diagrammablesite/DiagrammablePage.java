package com.novamens.portal.model.diagrammablesite;

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebPage;

import com.novamens.content.base.Content;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageType;


public interface DiagrammablePage extends Page  {

	
	
	//public void add(DiagrammableArea area);
	//public void remove(DiagrammableArea area);

	//public DiagrammableArea getArea(String area_name);
	//public DiagrammableArea getArea(int orden);

	
	

	public void setIsSection(boolean issection);
	public boolean isSection();

	void setIsAdmin(boolean isadmin);
	public boolean isAdminPage();

	void setIsHome(boolean ishome);
	boolean isHome();

	void setOrder(int orden);
	int getOrder();

	// public WebPage getWebPage();  VA A SERVICIO URL 
	
	//public void moveUp(DiagrammableArea area);
	//public void moveDown(DiagrammableArea area);
	
	public void setRelativeUrl(String url);
	public String getRelativeUrl();
	
	public void setPageType(PageType intValue);
	public PageType  getPageType();
	
	public void setContent(Content referenced_content);
	public Content getContent();
	
	//public DiagrammableArea getPreviousArea(int current_area);
	//public DiagrammableArea getFollowingArea(int current_area);
	//public void setStateAll(ObjectState state);
	

	public List<Integer> getAreaTypes();
	void setIsHeaderContainer(boolean ishc);
	boolean IsHeaderContainer();

	//public String getPageTypeStr(PageType page_type);
	
	public DiagrammablePage clone();
	public void onAfterClone();
	
	public String getContentShortenedId();
	
	public void setMenusVisible(boolean b);
	public boolean isMenusVisible();
	public Map<String, String> getGeneralInfo();
	// public String getPageTypeStr();
	 
	
}

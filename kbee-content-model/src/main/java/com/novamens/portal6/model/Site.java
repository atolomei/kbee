package com.novamens.portal6.model;

import java.util.List;

import com.novamens.content.library.Library;
import com.novamens.security.User;


/**
 *
 */
public interface Site extends PortalObject,  PortalModel {

	public static final String MAIN_DASHBOARD 		= "dashboard";
	
	public static final String LIBRARY 				= "library";
	public static final String PROJECT 				= "project";
	
	public static final String GENERAL_DASHBOARD 	= "general_dashboard";
	public static final String INSTITUTIONAL		= "institutional";
	public static final String MINISITE				= "minisite";
	
	public static final String KEY = "site";

	public void setOwner(User user);
	public User getOwner();

	public String getUrl();
	public void setUrl(String uri);
	

	/** Top Section  --------------------------------------*/
	
	public PageSection getTopBar();
	public void setTopBar( PageSection sec);

			
	/** Menus  --------------------------------------*/
	public List<Page> getPages();
	public List<Page> getSimplePages();
	public void remove(Page page);
	public void add(Page page);
	public Page getHomePage();
	

	
	
	/** Pages  --------------------------------------*/
	public List<PortalPersistentMenu> getMenus();
	public void remove(PortalPersistentMenu menu);
	public void add(PortalPersistentMenu menu);
		
	/** Bottom Section  --------------------------------------*/
	
	public  PageSection getFooter();
	public void setFooter( PageSection sec);
	
	/** Bottom Section  --------------------------------------*/
	
	public SiteType getSiteType();
	
	public void onAfterClone();
	public boolean isHomPage();

	public boolean isPublic();   // accesible on the web
	public boolean isExternal(); // external sites (basically a link)
	
	
	
	/** REVIEW  --------------------------------------*/
	
	public String treeString();
	public boolean isDisplayValidVersion();

	void moveUp(Page page);
	void moveDown(Page page);
	Page getTopBottomSectionPage();
	/**
	 * @param page
	 * towards the end
	 */
	void moveUp(PortalPersistentMenu menu);
	/**
	 * @param page
	 * towards 0
	 */
	void moveDown(PortalPersistentMenu menu);
	boolean isBuildable();
	void setBuildable(boolean b);
	
	// public void setDefaults();
	
	public void setDescription(String desc);
	
	
	public Library getLibrary();
	public void setLibrary(Library library);
	
	
}

 


/**
 * 
public SiteTemplate getSiteType();
public void setDescription(String desc);
public void setURI(String URI);
public void setSubtitle(String subtitle);
public void setEmailContact(String email_contact);
public void setSiteType(SiteType type);
public void setPublicAccess(boolean public_access);
public void setIsExternal(boolean external);
public void setSiteTemplate(SiteTemplate template);

*/

package com.novamens.kbee.portal.model;

/**
 * 
 * ELEMENTOS DE ESTRUCTURA
 * -----------------------
 * 
 * SITE
 * ----
 *  Site topbar
 *  Site footer
 *  Site LateralMenu 
 *  Site Pages 1-N
 *  
 *  PAGE
 *  ----
 *  Page topbar
 *  PageLayout (100 | 90-10 | 80-20 | 75-25 | 70-30 | 60-40 | 50-50)
 *  Page PageSection L
 *  Page PageSection R
 *  Page bottom-panel
 *  
 *   
 *  PAGESECTION
 * ------------
 *  Area 1-N (100 | 90-10 | 80-20 | 75-25 | 70-30 | 60-40 | 50-50)
 *  Area -> 
 *  AreaSection L-R
 *  
 * BLOCK
 * -----
 * IBlockPanel
 * 
 *
 * 
 * ELEMENTOS DE NAVEGACION
 * -----------------------
 * SiteMap
 * LateralMainMenu
 * Menu (horizontal, dropdownmenu, listmenu)
 * Breadcrumb
 *  
 *
 *
 *    
 *  -> 1-NPage -> PageSection  -> Area -> Block -> Item
 * 
 * (1S, 2S66x33, 2S50x50, 2S75x25, 2S33x66, 2S25x75)
 * 
 *  Diagramar TaskPage -> eForm se mete en 1 "Area"
 *  Diagramar Sitio corporativo
 *  Diagramar Help, Manual
 *  
 *  "idoc"
 *  
 *  Page_1, 2, 3, 4, 5
 *  
 *  [_eText_]
 *  [_eForm_]
 *  [_edoc__]
 * 
 */
// NAVIGATION
// ----------
// Site LateralMenu
// SiteMap
// Menu (MenuList, MenuDropdown, LateralMainMenu)
// Tabbar 

// STRUCTURE
// ---------
// SiteTopbar
// List<Page>
// SiteFooter  


import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Type;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.dom.Domain;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@Cacheable 
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entity")
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_SITE")
public class KbeeSite extends KbeePortalObject implements Site, Indexable {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSite.class.getName());

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = KbeePage.class, cascade = {CascadeType.ALL})
	@Fetch(FetchMode.SELECT)
	@OrderBy(clause = "order")
	@JoinColumn(name = "site_id", nullable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "query")
	private List<Page> pages = new ArrayList<Page>();

	@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeePageSection.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="topbar_page_section")
	private PageSection topbar_section;
	
	@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeePageSection.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="footer_page_section")
	private PageSection footer_section;
	
	@Column(name = "site_menu")
	private String site_menu;

	@Column(name = "SITE_TYPE")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.SiteTypeUserType")
	private SiteType site_type;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "owner_id", updatable=false)
	private User owner;

	/** relative url  */ 
	@Column(name = "uri")
	private String uri;

	@Column(name = "isPublic")
	private boolean is_public = false;

	@Column(name = "isbuildable")
	boolean is_buildable = true;

	@Column(name = "ispayloadeditor")
	private boolean isPayloadEditor = false;
	
	@Column(name = "isExternal")
	private boolean is_external = false;

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = KbeePortalPersistentMenu.class, cascade = {CascadeType.ALL})
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "site_id", nullable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private List<PortalPersistentMenu> menus = new ArrayList<PortalPersistentMenu>();

	// ver esto
	@Column(name = "isDisplayValidVersion")
	private boolean isDisplayValidVersion = false;

	
	@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeeLibrary.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "library_id", updatable=true)
	private Library library;

	@Override
	public void setLibrary(Library library) {
		this.library=library;
	}

	@Override
	public Library getLibrary() {
		return this.library;
	}
	

	
	public KbeeSite() {
	}
	
	/**
	 * 
	 */
	@Override
	public void setDefaults() {
		super.setDefaults();
		
		if (getDomain() == null) {
			
			Domain domain = null;
			
			if (getSessionUser()!=null) {
				domain = getContentDao().findUserProfileByUser(getSessionUser()).getDomain();
				if (domain!=null)
					setDomain(domain);
			}
			if (domain==null)
				throw new IllegalArgumentException("site domain can not be null");
		}
		
		if (getLastModifiedUser() == null) {
			if (getSessionUser()!=null) 
				setLastModifiedUser(getSessionUser());
		}

		for (Page p:getPages()) 
			p.setDefaults();
	}
	
	@Override
	public List<Page> getSimplePages() {
		return new ArrayList<Page>();
	}
	
	@Override
	public void setOwner(User user) {
		this.owner=user;
	}
	
	@Override
	public User getOwner() {
		return this.owner;
	}
	 
	@Override
	public void remove(Page page) {
		this.pages.remove(page);
	}
	
	@Override			
	public void add(Page page) {

		if (page.getLastModifiedUser() == null)
			page.setLastModifiedUser(getSessionUser());
		
		if (page.getLastModifiedOffsetDateTime() == null)
			page.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (page.getState() == null)
			page.setState(ObjectState.ENABLED);

		if (page.getOrder() == 0 && this.pages.size() > 0)
				page.setOrder(this.pages.size());
			
		if (page.getTitle() == null) {
			if (this.pages.size() == 0) {
				Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
				ResourceBundle res = ResourceBundle.getBundle(KbeeSite.this.getClass().getName(), locale);
				page.setTitle(res.getString("home") + "  " + getTitle());
			} else
				page.setTitle(page.getName());
		}
		
		page.setOrder(this.pages.size());
		
		this.pages.add(page);

		Collections.sort(this.pages, new Comparator<Page>() {
			public int compare(Page m1, Page m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
	}
	
	@Override
	public Page getHomePage() {
		
		if (getPages()==null)
			return null;
		for (Page p:getPages()) {
			if (p.isHome())
				return p;
		}
		return  getPages().get(0);
	}
	
	

	@Override
	public SiteType getSiteType() {
		return this.site_type;
	}

	public void setSiteType(SiteType type) {
		this.site_type = type;
	}

	@Override
	public String getTitle() {
		if (super.getTitle() != null)
			return super.getTitle();
		return "n/a";
	}
	
	@Override
	public String getDisplayName() {
		return getTitle();
	}

	@Override
	public String getUrl() {
		return uri;
	}

	@Override
	public boolean isHomPage() {
		return getSiteType() == SiteType.HOME;
	}
	
	@Override
	public boolean isPublic() {
		return this.is_public;
	}

	@Override
	public boolean isExternal() {
		return this.is_external;
	}

	public void setIsExternal(boolean external) {
		this.is_external = external;
	}

	public void setPublicAccess(boolean public_visibility) {
		this.is_public = public_visibility;
	}

	@Override
	public String getMetadataAsString() {
		return getSiteType().getLabel(Locale.getDefault()) + ". " + getSubtitle();
	}

	@Transient
	Map<String, String> map = null;

	@Override
	public Map<String, String> getGeneralInfo() {
	
		if (map != null)
			return map;

		map = new HashMap<String, String>();

		try {
		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeSite.class.getName(), locale);
		
		map.put("Class", res.getString(this.getClassKey()));
		map.put(res.getString("title"), getTitle());
		map.put(res.getString("name"), getName());
		map.put(res.getString("status"), (getState()!=null?getState().getLabel(locale):""));
		this.map.put("Id / OId", (getId() != null ? getId().toString() : "")+" / "+(getOId() != null ? getOId().toString() : ""));
		map.put(res.getString("last-modified"), getLastModifiedOffsetDateTimeColloquial()  +"  | " + (getLastModifiedUser()!=null?getLastModifiedUser().getFirstLastName():""));		
	
		if (getKey()!=null)
			this.map.put("key", getKey());
		
		this.map.put("Version", String.valueOf(getVersion()));
		map.put("Description", this.getDescription());
		map.put("Created", getCreationOffsetDateTimeColloquial());
		
		} catch (Exception e) {
			logger.error(e);
			map.put("error", e.getClass().getName()+" | " + e.getMessage());
		}
		return map;
	}

	@Override
	public void onAfterClone() {
		throw new KbeeRuntimeException("not done");
	}

	@Override
	public PortalObject clone() {
		throw new KbeeRuntimeException("not done");
	}

	public void setDisplayValidVersion( boolean b) {
		this.isDisplayValidVersion = b;
	}
	
	@Override
	public boolean isDisplayValidVersion() {
		return this.isDisplayValidVersion;
	}

	@Override
	public List<Page> getPages() {
		return this.pages;
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		return null;
	}

	
	public String getMenuAsString() {
		return site_menu;
	}
	
	public void setMenuAsString(String str) {
		site_menu=str;
	}

	@Override
	public Page getTopBottomSectionPage() {
		for (Page p:getPages()) {
			if (!p.isRegularPage())
				return p;
		}
		return null;
				
	}
	
	@Override
	public PageSection getTopBar() {return topbar_section;}
	
	@Override
	public void setTopBar( PageSection area) {topbar_section=area;}
	
	@Override
	public  PageSection getFooter() {return footer_section;}
	
	@Override
	public void setFooter( PageSection area) {this.footer_section=area;}

	@Override
	public String getClassKey() {
		return Site.KEY;
	}

	@Override
	public PortalObject getParent() {
		return null;
	}
	
	public String treeString() {
			StringBuilder str  = new StringBuilder();
			str.append("\n----------------------------------\n");
			str.append("Site --> " + this.getTitle() +" \n");
			if (this.pages!=null) {
				for (Page p:getPages()) {
					str.append(p.treeString());
				}
			}
			str.append("\n----------------------------------\n");
			return str.toString();
	}

	@Override
	public List<PortalPersistentMenu> getMenus() {
		return this.menus;
	}

	@Override
	public void remove(PortalPersistentMenu menu) {
		if (this.menus!=null)		
			this.menus.remove(menu);
	}


	@Override
	public void add(PortalPersistentMenu menu) {
		if (this.menus==null)
			this.menus=new ArrayList<PortalPersistentMenu>();
		this.menus.add(menu);
	}
	
	
	/**
	 * @param page
	 * towards the end
	 */
	
	@Override
	public void moveUp(Page page) {
		
		if (page.getOrder() >= (this.pages.size()-1))
			return;

		int order=page.getOrder();

		page.setOrder(order+1);
		
		for (Page p: this.pages) {
			if (!page.getId().equals(p.getId()) && (p.getOrder()==order+1) ) {
				p.setOrder(order);
				break;
			}
		}

		Collections.sort(this.pages, new Comparator<Page>() {
			public int compare(Page m1, Page m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		
		if (logger.isDebugEnabled()) 
			pages.forEach(item -> logger.debug(item.getTitle() +  " -> " + String.valueOf(item.getOrder())));

		int n=0;
		for (Page p:pages) 
			p.setOrder(n++);
		
	}
	
					
	/**
	 * @param page
	 * towards 0
	 */
	
	@Override
	public void moveDown(Page page) {
		
		if (page.getOrder()==0)
			return;
		
		int order=page.getOrder();

		page.setOrder(order-1);

		
		for (Page p: this.pages) {
			if (!page.getId().equals(p.getId()) && (p.getOrder()==order-1) ) {
				p.setOrder(order);
				break;
			}
		}

		Collections.sort(this.pages, new Comparator<Page>() {
			public int compare(Page m1, Page m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		
		if (logger.isDebugEnabled()) 
			pages.forEach(item -> logger.debug(item.getTitle() +  " -> " + String.valueOf(item.getOrder())));
		
		int n=0;
		for (Page p:pages) 
			p.setOrder(n++);

	}


	
	
	
	/**
	 * @param page
	 * towards the end
	 */
	
	
	@Override
	public void moveUp(PortalPersistentMenu menu) {
		
		if (menu.getOrder() >= (this.menus.size()-1))
			return;

		int order=menu.getOrder();

		menu.setOrder(order+1);
		
		for (PortalPersistentMenu p: this.menus) {
			if (!menu.getId().equals(p.getId()) && (p.getOrder()==order+1) ) {
				p.setOrder(order);
				break;
			}
		}

		Collections.sort(this.menus, new Comparator<PortalPersistentMenu>() {
			public int compare(PortalPersistentMenu m1, PortalPersistentMenu m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		
		if (logger.isDebugEnabled()) 
			menus.forEach(item -> logger.debug(item.getTitle() +  " -> " + String.valueOf(item.getOrder())));

		int n=0;
		
		for (PortalPersistentMenu p:menus) 
			p.setOrder(n++);
		
	}
	
					
	/**
	 * @param page
	 * towards 0
	 */
	
	
	@Override
	public void moveDown(PortalPersistentMenu menu) {
		
		if (menu.getOrder()==0)
			return;
		
		int order=menu.getOrder();

		menu.setOrder(order-1);

		
		for (PortalPersistentMenu p: this.menus) {
			if (!menu.getId().equals(p.getId()) && (p.getOrder()==order-1) ) {
				p.setOrder(order);
				break;
			}
		}

		Collections.sort(this.menus, new Comparator<PortalPersistentMenu>() {
			public int compare(PortalPersistentMenu m1, PortalPersistentMenu m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		
		if (logger.isDebugEnabled()) 
			menus.forEach(item -> logger.debug(item.getTitle() +  " -> " + String.valueOf(item.getOrder())));
		
		int n=0;
		for (PortalPersistentMenu p:menus) 
			p.setOrder(n++);
	}

	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public void setUrl(String uri) {
		this.uri=uri;		
	}

	@Override	
	public void setBuildable(boolean b) {
		is_buildable=b;
	}
	
	@Override
	public boolean isBuildable() {
		return is_buildable;
	}
	@Override
	public String getDataProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void setPayloadEditor(boolean b) {
		isPayloadEditor=b;
	}
	@Override
	public boolean isPayloadEditor() {
		return isPayloadEditor;
	}


	
}

// @Column(name = "site_template")
// @Enumerated(EnumType.ORDINAL)
// @Type(type = "com.novamens.portal6.model.SiteTemplateUserType")
// private SiteTemplate site_template;

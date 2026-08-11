package com.novamens.kbee.portal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.url.UriHelper;
import com.novamens.portal6.model.IPTab;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageLayoutType;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageType;
import com.novamens.portal6.model.PortalObject;


/**
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_PAGE")
public class KbeePage extends KbeePortalObject implements Page {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePage.class.getName());
	
	@Transient
	List<IPTab> tabs;

	
	@Column(name = "css")
	private String css;
		
	@Override
	public void setCss(String css) {
		this.css=css;
	}

	
	@Override
	public String getCss() {
		return this.css;
	}

	
	@Override	
	public void setBuildable(boolean b) {
		is_buildable=b;
	}
	
	@Override
	public boolean isBuildable() {
		return is_buildable;
	}
	
	@Column(name = "isbuildable")
	boolean is_buildable = true;
	
	@Column(name = "ispayloadeditor")
	private boolean isPayloadEditor = false;
	
	
	@Override
	public boolean isPayloadEditor() {
		return isPayloadEditor;
	}
	
	
	@Override
	public void setPayloadEditor(boolean b) {
		isPayloadEditor=b;
	}

	

	
	
	// ---------------------
	// top 
	// ---------------------
	// left | right 
	// ---------------------
	// bottom
	// ---------------------
	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL }, targetEntity = KbeePageSection.class, orphanRemoval = true)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "page_id", nullable = false)
	@OrderBy(clause = "orden asc")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private List<PageSection> page_sections = new ArrayList<PageSection>();

	/**
	 * this applies to the medium row (left | right ) 
	 */
	@Column(name = "pagelayout_type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.PageLayoutUserType")
	private PageLayoutType pagelayout_type;

	
	
	// Site Sections are the container of the HEADER and FOOTER of all pages
	@Column(name = "issection")
	private boolean is_site_section;

	
	@Column(name = "ishome")
	private boolean is_home;

	@Column(name = "orden")
	private int order;

	@Column(name = "is_admin")
	private boolean is_admin;

	@Column(name = "page_type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.PageTypeUserType")
	private PageType page_type;

	@Column(name = "relative_url")
	private String relative_url;

	@Column(name = "site_id", insertable=false, updatable=false)
	private Long site_id;
	
	// Content id shortened: classCode (2 caracteres) + oid del contenido
	@Column(name = "contentid")
	private String contentshortenedid;

	@Column(name = "menus_visible")
	private boolean page_menus_visible = true;

	
	@Column(name = "iscontent")
	private boolean iscontent;

	@Column(name = "isregular")
	private boolean isregular;


	// Si el link es a un contenido
	//
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_link", nullable = true) // si borran el Content el block queda apuntando a null.
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private Content content_link = null;


	@Transient
	List<Integer> types;

	@Transient
	List<PageSection> page_section_cache = null;

	@Transient
	boolean issorted = false;

	@Transient
	Map<String, String> map = null;

	@Transient
	Map<String, String> map_specific = new HashMap<String, String>();


	
	public KbeePage() {
	}

	public KbeePage(String name) {
		setName(name);
	}

	public KbeePage(String name, boolean is_home) {
		setName(name);
		this.is_home=is_home;
	}
	
	@Override
	public List<PageSection> getPageSections() {
		return this.page_sections;
	}
	
	@Override
	public void add(PageSection page_section) {
		page_sections.add(page_section);
		this.issorted = false;
	}
	
	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		setRelativeUrl(UriHelper.getInstance().getTitleName(title));
	}

	public KbeePage(String name, String title) {
		setName(name);
		setTitle(title);
	}

	public void setIsAdmin(boolean isadmin) {
		this.is_admin = isadmin;
	}

	
	public void setOrder(int order) {
		this.order = order;
	}

	
	public void setRelativeUrl(String url) {
		this.relative_url = url;
	}

	@Override
	public String getRelativeUrl() {
		return this.relative_url;
	}

	
	public void setPageType(PageType pt) {
		this.page_type = pt;
	}

	@Override
	public PageType getPageType() {
		return page_type;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	
	
	@Override
	public boolean isContentPage() {
		return iscontent;
	}


	@Override
	public void setIsContentPage(boolean iscontent) {
		this.iscontent = iscontent;
	}

	
	
	@Override
	public boolean isRegularPage() {
		return isregular;
	}

	@Override
	public void setIsRegularPage(boolean isregular) {
		this.isregular = isregular;
	}

	
	
	
	public void setIsHome(boolean ishome) {
		this.is_home = ishome;
		if (ishome)
			setOrder(0);
	}

	@Override
	public boolean isHome() {
		return is_home;
	}

	
	@Override
	public void setSiteSection(boolean is_sitesection) {
		this.is_site_section = is_sitesection;
	}
	
	@Override
	public ObjectState getState() {
		if (super.getState() == null)
			return ObjectState.ENABLED;
		return super.getState();
	}

	@Override
	public boolean isSiteSection() {
		return is_site_section;
	}

	
	public void setContent(Content c) {
		this.content_link = c;
		this.contentshortenedid = c.getClassCode() + c.getOId().toString();
	}

	@Override
	public Content getContent() {
		return content_link;
	}

	@Override
	public boolean isAdminPage() {
		return is_admin;
	}

	@Override
	public Page clone() {

		KbeePage page = new KbeePage();
		super.onClone((PortalObject) page);

		page.setSiteSection(isSiteSection());
		
		page.setIsHome(isHome());
		
		page.setOrder(getOrder());
		page.setPageType(getPageType());
		page.setIsAdmin(isAdminPage());
		page.setRelativeUrl(getRelativeUrl());
		page.setState(getState());

		if (getContent() != null)
			page.setContent(getContent());

		if (getContentShortenedId() != null)
			page.setContentShortenedId(getContentShortenedId());

		for (PageSection ps : getPageSections()) {
			PageSection clonedSec = ps.clone();
			page.add(clonedSec);
		}

		return page;
	
	}

	

	
	protected void setContentShortenedId(String contentId2) {
		this.contentshortenedid = contentId2;
	}

	@Override
	public String getContentShortenedId() {
		return this.contentshortenedid;
	}

	/**
	 */
	@Override
	public String getMetadataAsString() {

		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeePage.class.getName(), locale);
		StringBuilder str = new StringBuilder();

		str.append(isHome() ? res.getString("home") + ". " : "");
		str.append(isSiteSection() ? res.getString("section") + ". " : "");
		str.append(getState().getLabel(locale));

		return str.toString();
	}

	@Override
	public void setDefaults() {
		super.setDefaults();
		
		if (page_type==null)
			page_type=PageType.STANDARD;
		
		if (pagelayout_type==null)
			pagelayout_type=PageLayoutType.PAGE_LAYOUT_1S;
		
		if (relative_url==null) {
			if (this.getOId()!=null)
				relative_url=this.getOId().toString();
			else
				relative_url=String.valueOf(this.hashCode());
		}
		
		if (page_sections ==null)
			page_sections = new ArrayList<PageSection>();
		
		for (PageSection p: page_sections) 
			if (p instanceof KbeePageSection)
				((KbeePageSection)p).setDefaults();
		
	}



	@Override
	public Map<String, String> getGeneralInfo() {

		if (this.map != null)
			return this.map;

		this.map = new HashMap<String, String>();
		
		try {
			
			Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
			ResourceBundle res = ResourceBundle.getBundle(this.getClass().getName(), locale);
			
			this.map.put("Class", res.getString(this.getClassKey()) + (isHome()? (" (Home)"):""));
			
			this.map.put(res.getString("title"), getTitle());
			this.map.put(res.getString("name"), getName());
			
			this.map.put("Id / OId", (getId() != null ? getId().toString() : "")+" / "+(getOId() != null ? getOId().toString() : ""));
			this.map.put(res.getString("status"), (getState()!=null?getState().getLabel():""));
			
			map.put(res.getString("order"), String.valueOf(getOrder()));
			map.put(res.getString("last-modified"), getLastModifiedOffsetDateTimeColloquial()  +"  | " + (getLastModifiedUser()!=null?getLastModifiedUser().getFirstLastName():""));
			
			if (getKey()!=null)
				this.map.put("key", getKey());

			this.map.put("Sections", String.valueOf(getPageSections().size()));
			this.map.put("Version", String.valueOf(getVersion()));
			
			if (this.getDescription()!=null)
				map.put("Description", this.getDescription());
			
			map.put("Created", getCreationOffsetDateTimeColloquial());
			
			
		} catch (Exception e) {
			logger.error(e);
			map.put("error", e.getClass().getName()+" | " + e.getMessage());
		}

		return this.map;
	}

	public void setStateAll(ObjectState state) {
		for (PageSection ps : getPageSections()) {
				ps.setState(state);
				((KbeePageSection) ps).setStateAll(state);
		}
	}

	@Override
	public void onAfterClone() {
		for (PageSection ps : getPageSections())
			ps.onAfterClone();
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		return null;
	}
	
	
	@Override
	public PortalObject getParent() {
		if (site_id==null)
			return null;
		return getPortalDao().findSiteById(site_id);
	}
	

	@Override
	public String getClassKey() {
		return Page.KEY;
	}


	
	public List<IPTab> getTabs() {
		return tabs; 
	}

	
	
	public String treeString() {
		StringBuilder str  = new StringBuilder();
		str.append("Page --> " + this.getTitle()+" \n");
		for (PageSection p:getPageSections()) {
			str.append(p.treeString());
		}
		return str.toString();
	}

	

}

















// this.map.put(res.getString("page-type"), getPageTypeStr(getPageType()));
//String usagemode;
//if (getPageType() == PageType.LINK) {
//	usagemode = res.getString("usage_info_content_referece");
//	this.map.put(res.getString("reference-content"), "-");
//} else if (getPageType() == PageType.STANDARD)
//	usagemode = res.getString("usage_info_aggregator");
//map.put(res.getString("usage-info"), usagemode);
//map.put(res.getString("page-menus") + ". ",
//		this.isMenusVisible() ? res.getString("yes") : res.getString("no"));
//map.put(res.getString("areas") + ". ", String.valueOf(getAreas().size()));
/**
@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL }, targetEntity = KbeeArea.class, orphanRemoval = true)
@Fetch(FetchMode.SELECT)
@JoinColumn(name = "page_id", nullable = false)
@OrderBy(clause = "orden asc")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
private List<Area> areas = new ArrayList<Area>();
 **/
/**
@Override
public void add(Area area) {
	this.areas.add(area);
	area.setParent(this);
	this.issorted = false;
}

		// str.append(getPageTypeStr(getPageType()) + " ");
		// if (getPageType()==PageType.LINK) {
		//if (this.getRelativeUrl() != null)
		//	str.append("[ " + this.getRelativeUrl() + " ]. ");
		//else
		//	str.append("[ null ]. ");
		//
		// }



**/

/**
@Override
public List<Integer> getAreaTypes() {

	if (this.types != null)
		return this.types;

	this.types = new ArrayList<Integer>();

	List<Integer> types = new ArrayList<Integer>();
	types.add(Integer.valueOf(Area.AREA_1S));

	if (getPageType() == PageType.UGC_PHOTOS)
		return types;

	types.add(Integer.valueOf(Area.AREA_2S_33x66));
	types.add(Integer.valueOf(Area.AREA_2S_50x50));
	types.add(Integer.valueOf(Area.AREA_2S_66x33));
	types.add(Integer.valueOf(Area.AREA_2S_75x25));
	types.add(Integer.valueOf(Area.AREA_2S_66x33_L2X1)); // 3x1 -> 2 arriba 1 abajo
	types.add(Integer.valueOf(Area.AREA_3S_33));
	types.add(Integer.valueOf(Area.AREA_HEADER));

	return types;
}
*/

/**
public static List<PageType> getPageTypes() {

	List<PageType> list = new ArrayList<PageType>();
	list.add(PageType.STANDARD);
	list.add(PageType.LINK);
	return list;
}
*/


//@Override
//public void setParent(PortalObject parent) {
//	super.setParent((Site) parent);
//}

// page.setIsHeaderContainer(IsHeaderContainer());
// page.setMenusVisible(isMenusVisible());


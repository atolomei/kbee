package com.novamens.kbee.portal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.IPTab;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
import com.novamens.portal6.model.PageSectionType;
import com.novamens.portal6.model.PortalObject;
import com.novamens.util.KbeeRuntimeException;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_PAGE_SECTION")
public class KbeePageSection extends KbeePortalObject implements PageSection {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePageSection.class.getName());

	@OneToMany(fetch = FetchType.EAGER,cascade = {CascadeType.ALL}, targetEntity = KbeeArea.class, orphanRemoval = true)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "page_section_id", nullable = false)
	@OrderBy(clause = "orden asc")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private List<Area> areas = new ArrayList<Area>();

	@Column(name = "page_section_type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.PageSectionTypeUserType")
	private PageSectionType page_section_type;

	@Column(name = "page_section_disposition")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.PageSectionDispositionUserType")
	private PageSectionDisposition page_disposition;
	
	@Column(name = "orden")
	private int order;

	@Column(name = "ptab")
	private int ptab;

	@Column(name = "page_id" , insertable=false, updatable=false)
	private Long page_id;

	@Column(name = "headerpanel")
	private boolean headerpanel = false;

	
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
	
	
	@Transient
	List<Area> area_cache = null;

	
	@Transient
	boolean issorted = false;

	@Transient
	Map<String, String> map = null;

	@Transient
	Map<String, String> map_specific = new HashMap<String, String>();

	@Transient
	List<IPTab> tabs;
	
	

	
	public KbeePageSection() {
	}
	
	public KbeePageSection(String name) {
		setName(name);
	}

	
	public void setPTab(int ptab) {
		this.ptab=ptab;
	}
	
	
	public KbeePageSection(PageSectionDisposition dis) {
			this.page_disposition=dis;
	}

	
	@Override
	public PageSectionType getPageSectionType() {
		return page_section_type;
	}


	public void setPageSectionType(PageSectionType page_section_type) {
		this.page_section_type = page_section_type;
	}

	@Override
	public PageSectionDisposition getPageSectionDisposition() {
		return page_disposition;
	}


	public void setPageSectionDisposition(PageSectionDisposition page_disposition) {
		this.page_disposition = page_disposition;
	}


	public List<IPTab> getTabs() {
		return tabs; 
	}
	
	@Override
	public Area getPreviousArea(int current_area) {
		Area area = null;
		for (Area xa : getAreas()) {
			if (xa.getOrder() >= current_area)
				return area;
			else
				area = xa;
		}
		return area;
	}

	@Override
	public Area getFollowingArea(int index) {
		for (Area xa : getAreas()) {
			if (xa.getOrder() > index)
				return xa;
		}
		return null;
	}



	@Override
	public void moveUp( Area area) {
		move(area, -1);
	}

	@Override
	public void moveDown( Area area) {
		move(area, 1);
	}

	@Override
	public void remove(Area area) {
		this.areas.remove(area);
		this.area_cache = null;
		this.issorted = false;
	}

	@Override
	public String getClassKey() {
		return PageSection.KEY;
	}

	@Override
	public List<Area> getAreas(int tab_index) {
		List<Area> list = new ArrayList<Area>();
		for (Area a: getAreas())
			if (a.getPTab()==tab_index)
				list.add(a);
		return list;
	}

	
	@Override
	public List<Area> getAreas(ObjectState state) {
		List<Area> li=getAreas();
		
		List<Area> list = new ArrayList<Area>();
		
		for (Area a:li) {
			if (a.getState()==state) {
				list.add(a);
			}
		}
		return list;
	}
		
	@Override
	public List<Area> getAreas() {

		if (this.issorted && this.area_cache != null)
			return this.area_cache;

		this.area_cache = new ArrayList<Area>();
		this.area_cache.addAll(areas);

		Collections.sort(this.area_cache, new Comparator<Area>() {
			public int compare(Area m1, Area m2) {
				try {
					if (m1.getOrder() < m2.getOrder())
						return -1;
					else if (m1.getOrder() > m2.getOrder())
						return 1;
					else if (m1.getTitle() != null && m2.getTitle() != null)
						return m1.getTitle().compareToIgnoreCase(m2.getTitle());
					return 0;
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});

		this.issorted = true;

		return this.area_cache;
	}

	@Override
	public  Area getArea(String area_name) {
		for (Area area : getAreas()) {
			if (area.getName().equals(area_name))
				return area;
		}
		return null;
	}

	
	@Override
	public Area getArea(int orden) {
		int n = 0;
		for (Area area : getAreas()) {
			if (n++ == orden)
				return area;
		}
		return null;
	}
	 

	/**
	 * @param src_area
	 * @param offset
	 */
	
	private void move(Area src_area, int offset) {

		int src = -1;
		int dest = -1;
		int n = 0;

		for ( Area area : getAreas()) {
			if (area.getId().equals(src_area.getId())) {
				src = n;
				dest = src + offset;
			}
			n++;
		}

		if (src > -1 && src < getAreas().size() && dest > -1 && dest < getAreas().size()) {
			 KbeeArea area_1 = (KbeeArea) getAreas().get(src);
			 KbeeArea area_2 = (KbeeArea) getAreas().get(dest);
			area_1.setOrder(dest);
			area_2.setOrder(src);
		}

		issorted = false;
	}
	


	@Override
	public PortalObject getParent() {
		if (page_id==null)
			return null;
		return getPortalDao().findPageById(page_id);
	}
	
	

	@Override
	public void add(Area area) {
		this.areas.add(area);
		
		if (area instanceof KbeeArea)
			((KbeeArea) area).setOrder(this.areas.size()-1);
		
		this.issorted=false;
	}


	@Override
	public int getMaxElements() {
		return 0;
	}


	@Override
	public String getMetadataAsString() {
		return null;
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		return null;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setOrder(int o) {
		this.order =o;
	}
	
	
	@Override
	public void onAfterClone() {
	}

	@Override
	public PageSection clone() {
		throw new KbeeRuntimeException("not done");
	}

	public void setStateAll(ObjectState state) {
		for (Area area : getAreas()) {
			((KbeeArea) area).setStateAll(state);
		}
	}
	
	@Override
	public void setDefaults() {
		super.setDefaults();
		
		if (page_section_type==null)
			page_section_type=PageSectionType.ONE_SECTION; 
		
		if (page_disposition==null)
			page_disposition=PageSectionDisposition.LEFT;
		
		if (areas!=null)
			areas = new ArrayList<Area>();
		
		for (Area a: areas) { 
			if (a instanceof KbeeArea)
				((KbeeArea) a).setDefaults();
		}
	}

	public String treeString() {
		StringBuilder str  = new StringBuilder();
		str.append("PageSection --> " + this.getTitle()+" \n");
		if (areas!=null) {
			for (Area p:this.areas) {
				str.append( p.treeString());
			}
		}
		return str.toString();
	}
	
	public String toString() {
		
		StringBuilder str = new StringBuilder();

		str.append("Class: "+ this.getClass().getSimpleName()+ " | ");
		
		str.append(super.toString());
		
		if (this.page_section_type!=null)
			str.append(" | type: " + this.page_section_type.getDisplayName());
		
		if (this.page_disposition!=null)
			str.append(" | disposition " + this.page_disposition.getDisplayName());
		
		return str.toString();
	}

	@Override
	public int getPTab() {
		return ptab;
	}

	public void setHeader(boolean b) {
		this.headerpanel=b;
	}
	
	public boolean isHeader() {
		return headerpanel;
	}


	
	public Map<String, String> getGeneralInfo() {

		if (this.map != null)
			return this.map;

		this.map = new HashMap<String, String>();

		try {
			Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
			ResourceBundle res = ResourceBundle.getBundle(KbeePageSection.this.getClass().getName(), locale);
	
			
			map.put("Class. Type / Disposition", res.getString(this.getClassKey())

					+ (getPageSectionType()!=null?(".  " + getPageSectionType().getLabel()) :"n/a"   				) 
					+ (getPageSectionDisposition()!=null?(" / " +getPageSectionDisposition().getLabel()) :"n/a"   ) );
			
			this.map.put(res.getString("title"), getTitle());
			this.map.put(res.getString("name"), getName());
			
			this.map.put("Id / OId", (getId() != null ? getId().toString() : "")+" / "+(getOId() != null ? getOId().toString() : ""));

			map.put(res.getString("status"), (getState() != null ? getState().getLabel(locale) : ""));
			map.put(res.getString("last-modified"), getLastModifiedOffsetDateTimeColloquial()  +"  | " + (getLastModifiedUser()!=null?getLastModifiedUser().getFirstLastName():""));
			
			if (getKey()!=null)
				this.map.put("key", getKey());

			this.map.put("Areas", String.valueOf(getAreas().size()));
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




}

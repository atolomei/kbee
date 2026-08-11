package com.novamens.kbee.portal.model;


import java.time.OffsetDateTime;
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

import org.apache.wicket.request.resource.ResourceReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Type;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.ObjectState;

import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.IPTab;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.AreaType;

/**
* <p>
* Abstract model of Area of a page. 
* </p>
* <p>
* The blocks whose Section is {@link AreaSection.INTERNAL_MULTI_BLOCK} is for
* blocks that belong to the area but are not published in the sections of the Area
* directly but inside a multi_block block
* </p>
*/
@Entity
@PrimaryKeyJoinColumn(name = "po_id")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "PO_AREA")
public class KbeeArea extends KbeePortalObject implements Area {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeArea.class.getName());

	
	// Al borrar un area se borran los blocks que contiene
	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL }, targetEntity = KbeeBlock.class, orphanRemoval = true)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "area_id", nullable = false, updatable = true)
	@OrderBy(clause = "orden asc")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private List<Block> blocks;

	@Column(name = "area_type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.AreaTypeUserType")
	private AreaType area_type;
	
	@Column(name = "orden")
	private int orden;

	@Column(name = "areaclass")
	private String areaclass;

	@Column(name = "full_width_canvas")
	private boolean full_width_canvas = false;


	@Column(name = "headerpanel")
	private boolean headerpanel = false;

	public void setHeader(boolean b) {
		this.headerpanel=b;
	}
	
	public boolean isHeader() {
		return headerpanel;
	}
	
	@Column(name = "ptab")
	private int ptab;

	
	@Column(name = "page_section_id" , insertable=false, updatable=false)
	private Long page_section_id;


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

	
	
	@Transient
	private boolean lists_loaded = false;

	@Transient
	private List<Block> left_section = null;

	@Transient
	private List<Block> center_section = null;

	@Transient
	private List<Block> right_section = null;

	@Transient
	private List<Block> internal_multi_block = null;

	@Transient
	private Boolean isMenusVisible = null;

	@Transient
	private Map<String, String> map;

	@Transient
	private ResourceReference image;

	@Transient
	List<Block> sorted_blocks = null;
	
	@Transient
	List<IPTab> tabs;
	
	
	
	public List<IPTab> getTabs() {
		return tabs; 
	}

	
	public KbeeArea() {
	}

	
	@Override
	public PortalObject getParent() {
		
		if (page_section_id==null)
			return null;
		
		return getPortalDao().findPageSectionById(page_section_id);
		
	}

	public KbeeArea(String name) {
		setName(name);
		setTitle(name);
		this.area_type = AreaType.AREA_1S;
		setLastModifiedOffsetDateTime(OffsetDateTime.now());
	}
	
	public KbeeArea(String name, AreaType area_type) {
		setName(name);
		setTitle(name);
		this.area_type = area_type;
		setLastModifiedOffsetDateTime(OffsetDateTime.now());
	}


	
	public List<Block> getBlocks() {
		if (this.blocks == null)
			this.blocks = new ArrayList<Block>();
		return this.blocks;

	}

	
	@Override
	public List<Block> getBlocks(ObjectState state) {
		
		List<Block> _list=new ArrayList<Block>();
		
		for (Block b:getBlocksSorted()) {
			if (b.getState()==state)
				_list.add(b);
		}
		return _list;
	}
	
	
	public List<Block> getBlocksSorted() {

		if (sorted_blocks != null)
			return sorted_blocks;

		sorted_blocks = getBlocks();
		
		if (sorted_blocks.isEmpty())
			return sorted_blocks;

		Collections.sort(sorted_blocks, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				try {

					int oa = a.getAreaSection().getSortOrder() * 1000 + a.getOrder();
					int ob = b.getAreaSection().getSortOrder() * 1000 + b.getOrder();

					if (oa < ob)
						return -1;
					if (oa > ob)
						return 1;
					return 0;

				} catch (Exception e) {
					return 0;
				}
			}
		});

		return sorted_blocks;
	}

	
	public void setAreaType(AreaType area_type) {
		this.area_type = area_type;
	}

	@Override
	public String getTitle() {
		if (super.getTitle() == null)
			return "Area " + getId().toString();
		return super.getTitle();
	}

	@Override
	public boolean equals(Area a) {
		return getName().equals(a.getName());
	}

	
	@Override
	public void add(Block block) {

		if (block.getOrder() == -1)
			block.setOrder(getBlocks().size());

		if (block.getDomain()==null)
			block.setDomain(this.getDomain());
		
		if (block.getLastModifiedUser()==null)
			block.setLastModifiedUser(getSessionUser());

		if (block.getLastModifiedOffsetDateTime()==null)
			block.setLastModifiedOffsetDateTime(OffsetDateTime.now());

		if (getAreaType() == AreaType.AREA_1S) {
			getBlocks().add(block);
			return;
		}

		if (block.getAreaSection() == null)
			block.setAreaSection(AreaSection.LEFT);

		add(block, block.getAreaSection());

	}

	@Override
	public void add(Block block, AreaSection section) {

		if (!this.lists_loaded)
				populateLists();

		if (section == AreaSection.LEFT) {
			if (block.getOrder() == -1)
				((KbeeBlock)block).setOrder(left_section.size());
			((KbeeBlock)block).setAreaSection(AreaSection.LEFT);
			this.left_section.add(block);
		
		} else if (section == AreaSection.RIGHT) {
			if (block.getOrder() == -1)
				((KbeeBlock)block).setOrder(right_section.size());
			block.setAreaSection(AreaSection.RIGHT);
			this.right_section.add(block);
		
		} else if (section == AreaSection.CENTER) {
			if (block.getOrder() == -1)
				((KbeeBlock)block).setOrder(center_section.size());
			block.setAreaSection(AreaSection.CENTER);
			this.center_section.add(block);
			
		} else if (section == AreaSection.INTERNAL_MULTI_BLOCK) {
			if (block.getOrder() == -1)
				((KbeeBlock)block).setOrder(internal_multi_block.size());
			block.setAreaSection(AreaSection.INTERNAL_MULTI_BLOCK);
			this.internal_multi_block.add(block);
		}
		
		// asume que es Left
		else {
			if (block.getOrder() == -1)
				((KbeeBlock)block).setOrder(left_section.size());
			block.setAreaSection(AreaSection.LEFT);
			this.left_section.add(block);
		}

		getBlocks().add(block);
	}

	@Override
	public List<Block> getBlocks(AreaSection section) {

		if (!this.lists_loaded)
			populateLists();

		if (section == AreaSection.LEFT) {
			return this.left_section;
		} else if (section == AreaSection.RIGHT) {
			return this.right_section;
		} else if (section == AreaSection.CENTER) {
			return this.center_section;
		} else if (section == AreaSection.INTERNAL_MULTI_BLOCK) {
			return this.internal_multi_block;
		}
		return null;
	}

	@Override
	public void detach(Block block) {
		getBlocks().remove(block);
		populateLists();
	}

	@Override
	public void remove(Block block) throws ContentMgmtException {
		getBlocks().remove(block);
		populateLists();
		
		// SiteService service = getSite().getService(SiteService.class);
		// service.delete(block);
		//throw new KbeeRuntimeException("service.delete(block);");
	}

	@Override
	public int getOrder() {
		return orden;
	}

	@Override
	public void setOrder(int o) {
		orden = o;
	}

	@Override
	public AreaType getAreaType() {
		return area_type;
	}


	@Override
	public Area clone() {

		KbeeArea area = new KbeeArea();

		// asigno los valores que correspondan del las superclases
		//
		super.onClone((PortalObject) area);

		area.setAreaType(getAreaType());
		area.setDomain(getDomain());
		area.setName(getName());
		area.setOrder(getOrder());
		area.setState(getState());

		for (Block block : getBlocks()) {
			Block block_cloned = block.clone();
			 
			area.add(block_cloned);
		}
		return area;
	}

	@Override
	public String getMetadataAsString() {
		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeArea.this.getClass().getName(), locale);
		StringBuilder str = new StringBuilder();
		str.append((getState() != null ? (getState().getLabel(locale)) : " n/a"));
		str.append((". "
				+ (this.getLastModifiedUser() != null ? (this.getLastModifiedUser().getFirstLastName()) : " n/a")));
		str.append(". " + res.getString("order") + ": " + String.valueOf(getOrder()));
		return str.toString();
	}


	@Override
	public void setCss(String areac) {
		this.areaclass = areac;
	}

	@Override
	public String getCss() {
		return this.areaclass;
	}

	

	
	public boolean canMoveDown(Block c_block) {
		
		if (!this.lists_loaded)
			populateLists();

		if (c_block.getAreaSection() == AreaSection.RIGHT) 
			return (c_block.getOrder()<(this.right_section.size()-1));
		
		if (getAreaType().hasCenter())
			return true;
				
		if (getAreaType().hasRight())
			return true;

		return true;
	}
	


	
	public boolean canMoveUp(Block c_block) {
		
		if (!this.lists_loaded)
			populateLists();
		
		if (c_block.getAreaSection() == AreaSection.LEFT) { 
			return (c_block.getOrder()>0);
		}
		
		return true;
	}
	
	
	@Override
	public void moveDown(Block c_block) {

		if (!this.lists_loaded)
			populateLists();

		
		if (c_block.getAreaSection() == AreaSection.LEFT) {
			
				int size_left = this.left_section.size();
				
				if (c_block.getOrder()<(size_left-1)) {
						xchange(this.left_section, c_block, 1);
				}
				
				else if (getAreaType().hasCenter()) {
						
				}
					else if (getAreaType().hasRight()) {
						
				}
			
		} else if (c_block.getAreaSection() == AreaSection.RIGHT) {

			int size_right = this.right_section.size();
			
			if (c_block.getOrder()<(size_right-1)) {
				xchange(this.right_section, c_block, 1);
			}
		}
		
		
		else if (c_block.getAreaSection() == AreaSection.CENTER) {
		
			int size_center = this.center_section.size();
			
			if (c_block.getOrder()<(size_center-1)) {
				xchange(this.center_section, c_block, 1);
			}
			else if (getAreaType().hasRight()) {
				
			}
			
		} else if (c_block.getAreaSection() == AreaSection.INTERNAL_MULTI_BLOCK) {
			
			int size_internal = this.internal_multi_block.size();

			if (c_block.getOrder()<(size_internal-1)) {
				xchange(this.internal_multi_block, c_block, 1);
			}
			
			// -------------------------------
			// ver como mover los internal
			// -------------------------------
			
		}
	}

	@Override
	public void moveUp(Block c_block) {

		if (!this.lists_loaded)
			populateLists();

		if (c_block.getAreaSection() == AreaSection.LEFT) {
			
			
			
			xchange(this.left_section, c_block, -1);
		} else if (c_block.getAreaSection() == AreaSection.RIGHT) {
			xchange(this.right_section, c_block, -1);
		}

		else if (c_block.getAreaSection() == AreaSection.CENTER) {
			xchange(this.center_section, c_block, -1);
		} else if (c_block.getAreaSection() == AreaSection.INTERNAL_MULTI_BLOCK) {
			xchange(this.internal_multi_block, c_block, -1);
		}
	}

	
	public void setStateAll(ObjectState state) {
		for (Block block : getBlocks()) {
			block.setState(state);
		}
	}

	
	@Override
	public Map<String, String> getGeneralInfo() {

		if (this.map != null)
			return this.map;

		this.map = new HashMap<String, String>();

		try {
		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeArea.this.getClass().getName(), locale);
		
		map.put("Class - Type", res.getString(this.getClassKey()) +	( (getAreaType()!=null)?  (" - "+getAreaType().getLabel()+" ") :"" ));
		this.map.put(res.getString("title"), getTitle());
		this.map.put(res.getString("name"), getName());
		this.map.put("Id / OId", (getId() != null ? getId().toString() : "")+" / "+(getOId() != null ? getOId().toString() : ""));
		
		map.put(res.getString("status"), getState() != null ? getState().getLabel(locale) : "-");
		map.put(res.getString("last-modified"), getLastModifiedOffsetDateTimeColloquial() );
		map.put("Modified by", (getLastModifiedUser()!=null?getLastModifiedUser().getFirstLastName():""));
		
		map.put("Created", getCreationOffsetDateTimeColloquial());
		
		
		if (getKey()!=null)
			this.map.put("key", getKey());

		this.map.put("Total Blocks", String.valueOf(getBlocks().size()));
		
		this.map.put("Version", String.valueOf(getVersion()));
		
		this.map.put("Order", String.valueOf(getOrder()));
		 if (this.getDescription()!=null)
			 map.put("Description", this.getDescription());

		
		
		
		
		
		
		
		} catch (Exception e) {
			map.put("error", e.getClass().getName()+" | " + e.getMessage());
		}
		
		return this.map;
	}

	@Override
	public boolean isInFullWidthCanvas() {
		return full_width_canvas;
	}

	
	public void setInFullWidthCanvas(boolean fwcanvas) {
		full_width_canvas = fwcanvas;
	}

	@Override
	public void onAfterClone() {
		for (Block block : getBlocks()) {
			block.onAfterClone();
		}
	}


	public void setPTab(int ptab) {
		this.ptab=ptab;
	}
	
	
	private void populateLists() {

		this.left_section = new ArrayList<Block>();
		
		this.internal_multi_block = new ArrayList<Block>();

		this.right_section = new ArrayList<Block>();
		this.center_section = new ArrayList<Block>();

		this.sorted_blocks = null;

		for (Block b : getBlocks()) {
			try {
				if (b.getAreaSection() == AreaSection.LEFT) {
					this.left_section.add(b);
				} else if (b.getAreaSection() == AreaSection.RIGHT) {

					if (getAreaType() != AreaType.AREA_1S)
						this.right_section.add(b);
					else
						this.left_section.add(b);
				} else if (b.getAreaSection() == AreaSection.CENTER) {
					
					if (getAreaType() == AreaType.AREA_3S_3x33)
						this.center_section.add(b);
					else
						this.left_section.add(b);

				} else if (b.getAreaSection() == AreaSection.INTERNAL_MULTI_BLOCK) {
					this.internal_multi_block.add(b);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}

		Collections.sort(this.left_section, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				try {
					if (a.getOrder() < b.getOrder())
						return -1;
					if (a.getOrder() > b.getOrder())
						return 1;
					return (a.getTitle().toLowerCase().compareToIgnoreCase(b.getTitle()));
				} catch (Exception e) {
					return 0;
				}
			}
		});

		Collections.sort(this.center_section, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				try {
					if (a.getOrder() < b.getOrder())
						return -1;
					if (a.getOrder() > b.getOrder())
						return 1;
					return (a.getTitle().toLowerCase().compareToIgnoreCase(b.getTitle()));
				} catch (Exception e) {
					return 0;
				}
			}
		});

		Collections.sort(this.right_section, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				try {
					if (a.getOrder() < b.getOrder())
						return -1;
					if (a.getOrder() > b.getOrder())
						return 1;
					return (a.getTitle().toLowerCase().compareToIgnoreCase(b.getTitle()));
				} catch (Exception e) {
					return 0;
				}
			}
		});

		Collections.sort(this.internal_multi_block, new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				try {
					if (a.getOrder() < b.getOrder())
						return -1;
					if (a.getOrder() > b.getOrder())
						return 1;

					return (a.getTitle().toLowerCase().compareToIgnoreCase(b.getTitle()));
				} catch (Exception e) {
					return 0;
				}

			}
		});

		this.lists_loaded = true;
	}

	
	
	
	public String treeString() {
		StringBuilder str  = new StringBuilder();
		str.append("Area --> " + this.getTitle()+" \n");
		if (this.blocks!=null) {
			for (Block p:this.blocks) {
				str.append(p.treeString());
			}
		}
		return str.toString();
	}



	@Override
	public void setDefaults() {
		super.setDefaults();
		
		if (area_type==null)
			area_type=AreaType.AREA_1S;
		
		if (blocks ==null)
			blocks = new ArrayList<Block>();
		
		for (Block b: blocks) {
			if (b instanceof KbeeBlock)
				((KbeeBlock)b).setDefaults();
		}
		
	}
	

	
	@Override
	public Map<String, String> getSpecificInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getPTab() {
		return ptab;
	}

	@Override
	public String getClassKey() {
		return Area.KEY;
	}


	@Override
	public List<Block> getBlocks(int tab_index) {
		if (tab_index==-1)
			return getBlocks();
		List<Block> ls=new ArrayList<Block>();
		for (Block b:getBlocks()) {
			if (b.getPTab()==tab_index)
				ls.add(b);
		}
		return ls;
	}
	
	
	/**
	 * @param b
	 */
	protected void xchange(List<Block> list, Block src_block, int offset) {
		int src = -1;
		int dest = -1;
		int n = 0;
		for (Block block : list) {
			if (block.equals(src_block)) {
				src = n;
				dest = src + offset;
				break;
			}
			n++;
		}
		if (src > -1 && src < list.size() && dest > -1 && dest < list.size()) {
			Block block_s = list.get(src);
			Block block_d = list.get(dest);
			block_s.setOrder(dest);
			block_d.setOrder(src);
			list.set(src, block_d);
			list.set(dest, block_s);
			for (Block block : getBlocks()) {
				if (block.getId().equals(block_s.getId())) {
					block.setOrder(block_s.getOrder());
				} else if (block.getId().equals(block_d.getId())) {
					block.setOrder(block_d.getOrder());
				}
			}
		}
	}

	@Override
	public List<AreaSection> getAreaSections() {

		List<AreaSection> list = new ArrayList<AreaSection>();
		
		if (getAreaType()==null)
			return list;
		
		list.add(AreaSection.LEFT);
		
		if (getAreaType()==AreaType.AREA_1S) 
			return list;
		
		
		if (    getAreaType()==AreaType.AREA_3S_3x33 ||
				getAreaType()==AreaType.AREA_3S_40x40x20 ||	
				getAreaType()==AreaType.AREA_3S_20x40x40 ) { 
		
			list.add(AreaSection.CENTER);
		}
		
		list.add(AreaSection.RIGHT);
		
		return list;
		
	}




	


}
	
	
	
	
	
	
	
	
	
	
	
	

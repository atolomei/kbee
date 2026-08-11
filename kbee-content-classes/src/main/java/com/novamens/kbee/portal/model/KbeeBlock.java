package com.novamens.kbee.portal.model;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.novamens.content.resource.KBFile;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.IPTab;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.AreaSection;

import javax.persistence.InheritanceType;


/**
 * 
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_BLOCK")
@Inheritance(strategy = InheritanceType.JOINED)
public class KbeeBlock extends KbeePortalObject implements Block {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeBlock.class.getName());
	
	@Column(name = "ptab")
	private int ptab;
	
	@Column(name = "section")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.portal6.model.AreaSectionUserType")
	private AreaSection section;

	@Column(name = "orden")
	private int orden = UNASSIGNED;


	@Column(name = "css")
	private String css;

	
	@Column(name = "block_css")
	private String block_css;

	// Si el link es externo
	@Column(name = "external_link")
	private String external_link = null;

	@Column(name = "area_id", insertable=false, updatable=false)
	private Long area_id;
	
	@Column(name = "headerpanel")
	private boolean headerpanel = false;

	
	@Column(name = "ispayloadeditor")
	private boolean isPayloadEditor = false;
	
	
	

	@Transient
	List<KBFile> xfiles = null;

	@Transient
	Map<String, String> map = null;

	@Transient
	Map<String, String> map_specific = new HashMap<String, String>();

	@Transient
	List<IPTab> tabs;
	
	public List<IPTab> getTabs() {
		return tabs; 
	}
	
	/**
	 */
	public KbeeBlock() {
	}

	public KbeeBlock(String name, String title) {
		setTitle(title);
		setName(name);
	}

	public KbeeBlock(String name) {
		setName(name);
		setTitle(name);
	}

	@Override
	public void setDefaults() {
		super.setDefaults();
		if (section==null)
			section=AreaSection.LEFT;
	}
		
	@Override
	public void setCss(String css) {
		this.css=css;
	}

	@Override
	public String getCss() {
		return this.css;
	}

	@Override
	public AreaSection getAreaSection() {
		return section;
	}

	@Override
	public void setAreaSection(AreaSection section) {
		this.section = section;
	}

	public void setHeader(boolean b) {
		this.headerpanel=b;
	}
	
	public boolean isHeader() {
		return headerpanel;
	}

	
	public void setParentToChildren() {
		// blocks can not have children.
	} 

	@Override
	public int getOrder() {
		return orden;
	}

	@Override
	public void onAfterClone() {

	}

	@Override
	public void setOrder(int order) {
		orden = order;
	}

	@Override
	public String getClassKey() {
		return Block.KEY;
	}

	
	
	protected void onClone(Block block) {

		// asigno los valores que correspondan del las superclases
		super.onClone((PortalObject) block);

		//block.setMaxElements(getMaxElements());
		block.setAreaSection(getAreaSection());
		//block.setSubtitle(getSubtitle());
		//block.setUsageInfo(getUsageInfo());
		block.setOrder(getOrder());
		block.setState(getState());
	}

	@Override
	public Block clone() {
		KbeeBlock block = new KbeeBlock();
		this.onClone((Block) block);
		return block;
	}

	@Override
	public String getMetadataAsString() {
		
		/*
		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeDiagrammablePage.class.getName(), locale);

		StringBuilder str = new StringBuilder();

		str.append(getBlockTypeDisplayName());

		if (getSection() != null)
			str.append(". " + getSection().getLabel(locale));

		int orden = getOrden();
		str.append(". " + res.getString("order") + " " + String.valueOf(orden));
		str.append((getState() != null ? ". " + getState().getLabel(locale) : ". n/a"));
		return str.toString();
		*/
		return "";
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\nOrder: " + String.valueOf(getOrder()));
		
		if (getAreaSection() != null)
			str.append("\nSection: " + getAreaSection().getLabel());
		
		return str.toString();
	}

	
	public void setBlockCss(String css) {
		block_css = css;
	}

	
	@Override
	public Map<String, String> getSpecificInfo() {
		return map_specific;
	}

	public void setPTab(int ptab) {
		this.ptab=ptab;
	}

	
	@Override
	public Map<String, String> getGeneralInfo() {

		if (map != null)
			return map;

		map = new HashMap<String, String>();

		try {
		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeBlock.class.getName(), locale);
		
		map.put("Class", res.getString(this.getClassKey()));
		
		map.put(res.getString("title"), getTitle());
		map.put(res.getString("name"), getName());
		map.put(res.getString("status"), (getState()!=null?getState().getLabel(locale):""));
		
		
		//logger.debug(block.getAreaSection().getLabel() + " " + String.valueOf(block.getOrder()));
		
		map.put("Area Section / Order ", getAreaSection().getLabel() + " / " + String.valueOf(getOrder()) );
		
		this.map.put("Id / OId", (getId() != null ? getId().toString() : "")+" / "+(getOId() != null ? getOId().toString() : ""));
		
		map.put(res.getString("last-modified"), getLastModifiedOffsetDateTimeColloquial() );
		
		map.put("Modified by ",getLastModifiedUser()!=null?getLastModifiedUser().getFirstLastName():"");
		map.put("Created", getCreationOffsetDateTimeColloquial());
		
		//  +"  | " + );		map.put(res.getString("areasection"), (getAreaSection()!=null?getAreaSection().getLabel():"")
		
	
		if (getKey()!=null)
			this.map.put("key", getKey());
		
		this.map.put("Version", String.valueOf(getVersion()));
		
		 if (this.getDescription()!=null)
			 map.put("Description", this.getDescription());
		
		} catch (Exception e) {
			logger.error(e);
			map.put("error", e.getClass().getName()+" | " + e.getMessage());
		}
		return map;
	}


	public String treeString() {
		StringBuilder str  = new StringBuilder();
		str.append("Block --> " + this.getTitle()+" \n");
		return str.toString();
	}


	@Override
	public PortalObject getParent() {
		if (area_id==null)
			return null;
		return getPortalDao().findAreaById(area_id);
	}

	

	@Override
	public int getPTab() {
		return ptab;
	}

	
	
	@Override
	public boolean isPayloadEditor() {
		return isPayloadEditor;
	}

	@Override
	public void setPayloadEditor(boolean b) {
		isPayloadEditor=b;
	}

	
	/**
	@Override
	public void setContentBlockImage(IDoc block_image) {
		this.block_image = block_image;
	}


	@Override
	public boolean isEditSpecificEnabled() {
		return false;
	}

	@Override
	public void setBlockTitleVisible(boolean b) {
		title_visible = b;
	}

	@Override
	public boolean isBlockTitleVisible() {
		return title_visible;
	}

	@Override
	public void setBlockIntroVisible(boolean b) {
		intro_visible = b;
	}
		

	@Override
	public boolean isBlockIntroVisible() {
		return intro_visible;
	}

	@Override
	public void setIntroOnlyImage(boolean intro_only_image) {
		this.intro_only_image = intro_only_image;
	}

	@Override
	public boolean isIntroOnlyImage() {
		return this.intro_only_image;
	}

	@Override
	public void setWebReference(int reference_type, String value) {

		if (reference_type == WebReference.REFERENCE_URL) {
			external_link = value;
			content_link = null;
			page_link = null;
		} else if (reference_type == WebReference.REFERENCE_CONTENT) {
			external_link = null;
			try {
				content_link = getContentDao().findContentByOId(value);
			} catch (Exception e) {
				logger.error(e);
				content_link = null;
			}
			page_link = null;
		} else if (reference_type == WebReference.REFERENCE_PAGE) {
			external_link = null;
			content_link = null;

			Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();

			String arr[] = value.trim().split("/");
			if (arr.length == 1) {
				Site site = getPortalDao().findSiteByURI(arr[0].trim(), domain, null);
				//if (site != null)
					// SERVICE PAGE URL
					// page_link = (DiagrammablePage) site.getHomePage();
			//} else {
			//	Site site = getPortalDao().findSiteByURI(arr[0].trim(), domain, null);
			//	if (site != null) {
					// SERVICE PAGE URL
					// page_link = (DiagrammablePage) site.getPageByUrl(arr[1].trim());
			//	}
			}
		}

		this.webreference = null;
	}
*/
	
	/**
	 * -----------------------------------------------------------------------------
	 * FIXME:
	 * 
	 * ARREGLAR ESTO !!!! Este es el menu del bloque, que por el momento se genera
	 * en el Panel, pero aca sería más simple de manejar.
	 * 
	 * @param model
	 * @return

	@SuppressWarnings({ "serial" })
	public Panel getMenuPanel() {

		IModel<Block> model = new ObjectModel<Block>(this);

		ContextMenuPanelDelete<Block> menu = new ContextMenuPanelDelete<Block>(model);

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new SeparatorMenuItemPanel<Block>(id);
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {

					private static final long serialVersionUID = 1L;

					public void onClick() {
						// Block.this.onEdit();
					}

					public String getLabel() {
						return "Edit";
					}

					@Override
					public boolean isVisible() {
						return false;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// Block.this.onMoveUp();
					}

					public String getLabel() {
						// return Block.this.getLabel("up");
						return "up";
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// Block.this.onMoveDown();
					}

					public String getLabel() {
						return "down"; // Block.this.getLabel("down");
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// Block.this.onShowImage(true);
					}

					public String getLabel() {
						return "Show"; // Block.this.getLabel("showimage");
					}

					@Override
					public boolean isVisible() {
						return isBlockImageVisible();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// Block.this.onShowImage(false);
					}

					public String getLabel() {
						return "hide"; // Block.this.getLabel("hideimage");
					}

					@Override
					public boolean isVisible() {
						return isBlockImageVisible();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new SeparatorMenuItemPanel<Block>(id);
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// Block.this.onUnPublish();
					}

					public String getLabel() {
						return "unpub"; // BlockPanel.this.getLabel("unpublish");
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Block>() {
			@Override
			public MenuItemPanel<Block> getItem(String id) {
				return new EditionMenuItem<Block>(id) {
					public void onClick() {
						// BlockPanel.this.onDelete();
					}

					public String getLabel() {
						return "dele"; // BlockPanel.this.getLabel("delete");
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		return menu;
	}

	 */
	
	/**
	@SuppressWarnings("rawtypes")
	@Override
	public List getElements() {
		return null;
	}

	@Override
	public List<Resource> getResources(String group_name) {
		throw new KbeeRuntimeException("not implemented");
	}

	@Override
	public List<KBFile> getFiles(String group_name) {
		throw new KbeeRuntimeException("not implemented");
	}

	@Override
	public String getCssClass() {
		return getBlockCss();
	}

	
*/
	
	/**
	 * 
	 * 
	 * 
	
	
	@Override
	public String getBlockBodyStyle() {
		return block_body_style;
	}

	@Override
	public void setBlockBodyStyle(String st) {
		this.block_body_style = st;
	}

	@Override
	public boolean isVisibleInlineMenu() {
		return isBlockMenuEnabled();
	}
	

	@Override
	public Panel getSpecificEditorPanel(String id, IModel<Block> model, IModel<Site> sitemodel) {
		return null;
	}
	*/

	/**
	 * <p>
	 * Para indicar si los clicks en los elementos deben ser contabilizados en los
	 * recientemente visitados
	 * </p>
	 */
	/**
	@Override
	public boolean includeInRecentVisited() {
		return true;
	}
	*/

	/**
	@Override
	public boolean isElementsPanelEnabled() {
		return false;
	}*/


	/**
	@Override
	public String getBlockImageCss() {
		return image_css;
	}

	@Override
	public void setBlockImageCss(String css) {
		image_css = css;

	}
	*/

	// Esto es el menu de la Pagina que se propaga
	//
	
	/**
	@Override
	public boolean isMenusVisible() {

		if (this.isMenusVisible == null) {
			this.isMenusVisible = ((com.novamens.portal.model.diagrammablesite.DiagrammableArea) getParent()) != null
					? Boolean.valueOf(((com.novamens.portal.model.diagrammablesite.DiagrammableArea) getParent()).isMenusVisible())
					: Boolean.valueOf(true);
		}
		return this.isMenusVisible;
	}

	@Override
	public void setMenusVisible(boolean b) {
		this.isMenusVisible = b;
	}
*/
	
	/**
	@Override
	public String getSubtitleModeString(Integer mode) {

		if (mode == ListBlock.SUBTITLE_NONE)
			return "Ninguno";

		if (mode == ListBlock.SUBTITLE_DATE_MODIFIED)
			return "Fecha de Modificación";

		if (mode == ListBlock.SUBTITLE_METADATA)
			return "Metadata";

		if (mode == ListBlock.SUBTITLE_USER_DATE_MODIFIED)
			return "Usuarios y Fecha de Modificación";

		if (mode == ListBlock.SUBTITLE_CONTENT_TYPE)
			return "Tipo de Contenido";

		return "N/A";

	}

*/
	
	/**
	 * Para las subclases debe coincidir con "name" de portal-context.xml de Spring
	 * Ej. <property name="id" value="blcok"/>
	 * <bean id="site-contents-searcher" class=
	 * "com.novamens.kbee.portal.model.factory.KbeeBlockXFactory">
	 * <property name="id" value="site-contents-searcher"/>
	 * <property name="name" value="Buscador. Contenidos del Sitio"/>
	 * <property name="className" value=
	 * "com.novamens.kbee.portal.model.KbeeBlockX"/> </bean>
	 * 
	 */

	/**
	@Override
	public String getBlockTypeDisplayName() {
		return getBlockType();
	}
	*/

	/**
	 * Para las subclases debe coincidir con "id" de portal-context.xml de Spring
	 * Ej. <property name="id" value="blcok"/>
	 */

	/**
	@Override
	public String getBlockTypeId() {
		return "block";
	}
	*/

	/**
	@Override
	public void evict() {
		ThumbnailService service = ServiceLocator.getService(ThumbnailService.class);
		if (image != null)
			try {
				service.evict(image.getId().toString(), getDomain().getId().toString());
			} catch (IOException e) {
				e.printStackTra3  ce();
			}
	}
	*/

	/**
	@Override
	public void detach() {
		// webreference = null;
		xfiles = null;
		isMenusVisible = null;
		map = null;
		map_specific = null;
	}
	
	*/
/*
	@Override
	public List<String> getCssClassList() {
		List<String> list = new ArrayList<String>();
		list.add("standard-block");
		list.add("alerta");
		list.add("cajita");
		list.add("destacado");
		list.add("destacado-mini");
		list.add("info");
		list.add("reproductor");
		list.add("social");
		return list;
	}

	*/
/*
	private synchronized void updateRelation() {
		synchronized (this) {
			KbeeIDoc idoc = (KbeeIDoc) getContentDao().findContentByOId(block_image.getOId());
			if (idoc != null && idoc.getFiles() != null & idoc.getFiles().size() > 0) {
				block_image = idoc;
				SiteService service = this.getSite().getService(SiteService.class);
				try {
					service.save();
				} catch (ContentMgmtException e) {
					logger.error(e);

				}
			}
		}
	}
*/
	



	
	
}

















/**
@Override
public boolean isViewMetadataEditor() {
	return false;
}

@Override
public boolean isViewImageEditor() {
	return false;
}

@Override
public boolean isViewRichTextEditor() {
	return false;
}

@Override
public List<String> getBlockSeparatorCssClassList() {
	List<String> list = new ArrayList<String>();
	list.add("standard-separator");
	list.add("banner");
	list.add("after-banner");
	return list;
}
*/

/**


@Override
public List<Resource> getPortalEnabledResources() {
	return getResources();
}
*/


/**
@Override
public void setWebReference(WebReference reference) {

	if (reference == null) {
		external_link = null;
		page_link = null;
		content_link = null;
		this.webreference = null;
		return;
	} else if (reference.getReferenceType() == WebReference.REFERENCE_URL)
		external_link = reference.getUrlReference();

	else if (reference.getReferenceType() == WebReference.REFERENCE_PAGE)
		page_link = reference.getPageReference();

	else if (reference.getReferenceType() == WebReference.REFERENCE_CONTENT)
		content_link = reference.getContentReference();

	this.webreference = null;
}
*/

/**
@Override
public WebReference getWebReference() {

	if (webreference == null)
		loadReference();

	return webreference;
}
*/

/**
// Si el link es a una página
//
@ManyToOne(fetch = FetchType.LAZY, optional = true, targetEntity = KbeePage.class)
@Fetch(FetchMode.SELECT)
@JoinColumn(name = "page_link", nullable = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
private  Page page_link = null;
 */

/**
// Si el link es a un contenido
//
@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
@Fetch(FetchMode.SELECT)
@JoinColumn(name = "content_link", nullable = true) // si borran el Content el block queda apuntando a null.
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
private Content content_link = null;
*/


/**
 *  * <ul>
 * <li>Lista de Contenidos (publicados por una Query)</li>
 * <li>Lista de Sitios (publicados por una Query)</li>
 * <li>Lista de Contenidos y Sitios (publicados manualmente empequetados en una vista {@ViewBK})</li>
 * <li>{@link TextBlock}. Texto con link opcional a Contenido o Sitio</li>
 * <li>{@link XBlock}: bloque "externo". Funciona como una caja negra publicando
 * un Panel que debe ser provisto por la aplicación. Sirve para agregar
 * funcionalidad específica de una implementación, tal como integración de datos
 * de una aplicación externa.</li>
 * </ul>

 */
/**
@Override
public int getMaxElements() {
	return maxelements;
}
@Override
public void setMaxElements(int m) {
	this.maxelements = m;
}
**/

/**
@Column(name = "new_tab")
private boolean new_tab = false;

@Column(name = "textstyle")
private String textstyle;


@Column(name = "maxlements")
private int maxelements;

@Column(name = "quantity_visible")
private boolean quantity_visible = false;

@Column(name = "title_visible")
private boolean title_visible = true;

@Column(name = "image_visible")
private boolean image_visible = true;

@Column(name = "intro_only_image")
private boolean intro_only_image = false;
 
@Column(name = "intro_visible")
private boolean intro_visible = true;
*/



package com.novamens.kbee.portal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.ResourceURI;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.portal.service.PortalDirectoryService;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBK;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;

import com.novamens.util.KbeeRuntimeException;

/**
 * <p>
 * La KbeeView devuelve title, description, etc. que tiene cargados. Las
 * subclases que contienen {@link Content} y {@link DiagrammableSite} puede sobrecargar
 * estos métodos y devolver los valores del Objeto en vez de de la vista (por
 * ejemplo cuando en la vista es null).
 * </p>
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "PO_VIEWBK")
@PrimaryKeyJoinColumn(name = "po_id")
@DynamicInsert
public class KbeeViewBK extends KbeePortalObject implements ViewBK, ResourceContainer {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeViewBK.class.getName());

	public static final String CONTENT_TYPE = "Content";
	public static final String SITE_TYPE 	= "Portal";
	public static final String LINK_TYPE 	= "Link";
	public static final String BLOCK_TYPE 	= "Block";
	public static final String IQL_TYPE 	= "IQL";

	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeBlock.class)
	@JoinColumn(name="block_id", insertable=false, updatable=false, nullable=false)
	private Block block;

	//@Column(name = "description")
	//private String description;

	//@Column(name = "title")
	//private String title;

	@Column(name = "subtitle") 
	private String subtitle;


	@Column(name = "tagline") 
	private String tagline;

	
	@Column(name = "style")
	private String style;

	@Column(name = "css")
	private String css;

	@Column(name = "iconcss")
	private String iconcss;

	@Column(name = "ntab")
	private boolean newtab = false;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KBFileImpl.class, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "image_id", nullable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "resource")
	private KBFile image;


	@Transient
	List<KBFile> xfiles;
	
	public KbeeViewBK() {
	}

	@Override
	public void setFile(KBFile file) {
		this.image = file;
		this.xfiles = null;
	}

	@Override
	public void addFile(KBFile file) {
		this.image = file;
		this.xfiles = null;
	}
	
	@Override
	public void addFile(KBFile file, boolean ispublic) {
		addFile(file);
	}
	
	@Override
	@Deprecated
	public void addFile(KBFile file, ResourceTag group, boolean ispublic) {
		addFile(file);
	}
	
	@Override
	public void addFile(KBFile file, ResourceTag group) {
		addFile(file);
	}
	
	@Override
	public void addResource(Resource file, ResourceTag group) {
		addResource(file);
	}

	@Override
	public void removeFile(KBFile file) {
		if (this.image != null && this.image.equals(file)) {
			this.image = null;
			this.xfiles = null;
		}
	}
	
	public void restoreFile(KBFile file) {
	}
	
	public List<KBFile> getFiles() {
		if (xfiles == null) {
			this.xfiles = new ArrayList<KBFile>();
			if (this.image != null)
				this.xfiles.add(image);
		}
		return this.xfiles;
	}

	public void setFiles(List<KBFile> files) {
		throw new KbeeRuntimeException("not done");
	}

	@Override
	public boolean equals(Object o2) {
		if (o2 instanceof ViewBK) {
			return getId().toString().equals(((ViewBK) o2).getId().toString());
		}
		return false;
	}

	public boolean contains(KBFile file) {
		if (file.equals(this.image))
			return true;
		return false;
	}

	public KBFile getFirstFile() {
		return this.image;
	}

	public void setIconcss(String iconcss) {
		this.iconcss = iconcss;
	}

	public String getIconcss() {
		return this.iconcss;
	}

	public Resource getResource(String name) {
		if (this.image.getName().equals(name)) {
			return this.image;
		}
		return null;
	}
	
	public Resource getResource(ResourceURI uri) {
		return null;
	}
	
	public ResourceURI getURI(Resource uri) {
		return null;
	}
	
	public List<Resource> getResources(ResourceURI uri) {
		return null;
	}

	public void addResource(Resource resource) {
	}
	
	public void addResource(Resource resource, ResourceFolder folder, ResourceTag tag) {
	}

	public List<Resource> getResources() {
		return null;
	}
	
	public List<Resource> getResources(boolean publicarea) {
		return null;
	}
	
	public void setPublic(Resource resource) {
	}
	
	public void setPrivate(Resource resource) {
	}
	
	@Override
	public boolean isPublic(Resource rosurce) {
		return true;
	}
	
	@Override
	public ResourceTag getTag(Resource resource) {
		return null;
	}
	
	@Override
	public void setTag(Resource resource, ResourceTag group) {
	}
	
	@Override
	public void setFolder(Resource resource, ResourceFolder folder) {
		
	}

	@Override
	public void setResources(List<Resource> files) {
	}
	
	@Override
	public void setResources(List<Resource> files, ResourceTag group) {
	}
	
	@Override
	public void setResourceNodes(List<ResourceNode> files, ResourceTag group) {
	}
	
	@Override
	public boolean isKBFile() {
		return image != null;
	}

	@Override
	public KBFile getFile() {
		return image;
	}
	
	@Override
	public String getClassKey() {
		return "viewblock";
	}

	@Override
	public String getMetadataAsString() {
		StringBuilder str = new StringBuilder();
		str.append(getViewType() + ". ");
		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());
		return str.toString();
	}

	@Override
	public void setText(Text text) {
	}

	@Override
	public Text getText() {
		return null;
	}

	public void onClone(KbeeViewBK clone) {
		super.onClone((PortalObject) clone);
		clone.setDescription(this.getDescription());
		clone.setTitle(this.getTitle());
		clone.setText(this.getText());
		clone.setFile(this.getFile());
		clone.setStyle(this.getStyle());
		clone.setOpenNewTab(this.isOpenNewTab());
	}

	/**
	 * Clone debe invocar a onClone para realizar el cloning. onClone es utilizada
	 * por las subclases para llenar de datos los objetos clonados.
	 * 
	 */
	@Override
	public KbeeViewBK clone() {
		KbeeViewBK clone = new KbeeViewBK();
		onClone(clone);
		return clone;
	}

	@Override
	public String getViewType() {
		return null;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		return str.toString();
	}

	@Override
	public void onClick() {
	}

	@Override
	public Object getObject() {
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
	public void evict() {
		ThumbnailService service = ServiceLocator.getService(ThumbnailService.class);
		if (getFile() != null)
			try {
				service.evict(getFile().getId().toString(), getDomain().getName());
			} catch (IOException e) {
				logger.error(e);
			}
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void setStyle(String sh) {
		style = sh;
	}

	@Override
	public void setOpenNewTab(boolean new_tab) {
		this.newtab = new_tab;
	}

	@Override
	public boolean isOpenNewTab() {
		return newtab;
	}
	
	public Block getBlock() {
		return (Block) getParent();
	}

	/**
	 * Global Home Site
	 */
	protected Site getHomeSite() {
		
		PortalDirectoryService service = ServiceLocator.getService(PortalDirectoryService.class);
		Site site = null;
		try {
			site = service.getHomeSite(getDomain());
			if (site == null)
				logger.error("Home Site is null. Please create HomeSite");
			return site;
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

	@Override
	public List<Resource> getPortalEnabledResources() {
		throw new KbeeRuntimeException("not implemented");
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		return null;
	}

	@Override
	public PortalObject getParent() {
		return getBlock();
	}

	@Override
	public void setIconCss(String iconcss) {
		this.iconcss=iconcss;
	}

	@Override
	public String getIconCss() {
		return iconcss;
	}
	
	@Override
	public String getSubtitle() {
		return subtitle;
	}

	@Override
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	 
	@Override
	public String getTagline() {
		return tagline;
	}

	@Override
	public void setTagline(String tagline) {
		this.tagline = tagline;
	}
 
	@Override
	public String getCss() {
		return css;
	}
	 
	@Override
	public void setCss(String css) {
		this.css = css;
	}

	@Override
	public String getDataProviderInfo() {
		return null;
	}
	
	@Deprecated
	public ResourceFolder getFolder(Resource resource) {
		return null;
	}
}

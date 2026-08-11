package com.novamens.kbee.content.resource;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import com.novamens.content.resource.KBGallery;
import com.novamens.content.resource.KBImage;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@Table(name = "Gallery")
@DynamicInsert
public class KBGalleryImpl extends AbstractResource implements KBGallery {

	private static final long serialVersionUID = -4242245578604394711L;

	@Column(name = "gdate")
	private Date date;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = com.novamens.kbee.content.resource.KBImageImpl.class)
	@JoinTable(name = "galleryfile",  
				joinColumns 		= {@JoinColumn(name = "gallery_id") }, 
				inverseJoinColumns 	= {@JoinColumn(name = "file_id") }
			   )
	@OrderColumn(name="gorder")
	private List<KBImage> images = new ArrayList<KBImage>();
	
	/** this field is used by the Searcher Library. It should be replaced by 
	 *  the KbeePortal tools 
	 * */
	@Column(name = "in_portal")
	private boolean in_portal = true;
	
	
	public void setDescription(String description)	{
	}
	
	@Override
	public boolean isBinaryFile() {
		return false;
	}
	
	public String getBaseName() {
		return getName();
	}
	
	@Override
	public String getPath() {
		return getName();
	}
	
	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public String getMetadataAsString() {
		return "not implemented";
	}

	@Override
	public String getMetadataAsString(DateTimeFormatter df) {
		return "not implemented";
	}

	@Override
	public String getGlyphIcon() {
		return  getResourceFAFreeByKey("file");
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return "not implemented";
	}
	
	@Override
	public String getFontAwesomeFreeIcon() {
			return  getResourceFAFreeByKey("file");
	}
	
	@Override
	public boolean isInPortalVersion() {
		return this.in_portal;
	}
	
	@Override
	public void setInPortalVersion(boolean b) {
		this.in_portal=b;
	}
}

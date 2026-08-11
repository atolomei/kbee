package com.novamens.kbee.content.resource;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.dom.Object;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.User;

import javax.persistence.InheritanceType;

/**
 * <p>Resources are binary objects stored and managed by the {@link KBFSService} Object Storage layer.
 * Normally the Object Storage will manage: Amazon S3, Minio, local File System and externally mapped repositories.
 * </p>
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "KRESOURCE")
@DynamicInsert
public abstract class AbstractResource extends AbstractObject implements Resource,  Versionable<Resource>  {
	
	@Id
	@GenericGenerator(
		name = "resource_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "resourceid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resource_sequencer")
	@Column(name = "ID")
	private Long id;

	
	/** 
	 * this field is used by the Searcher Library. 
	 * It should be replaced by the KbeePortal tools
	 *  
	 * */
	@Column(name = "in_portal")
	private boolean in_portal = true;	

	@Column(name = "NAME")
	private String name;
	
	
	
	// Versioning
 	
	@Column(name = "VERSION")
	private int version = 0;
	
	@Column(name = "ISHEAD")
	private boolean ishead = true;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST,  targetEntity = AbstractResource.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="PREV_VERSION")
	private Resource previousVersion;

	@Column(name = "KMODE")
	private int mode;
	
	@Column(name = "SEED")
	private String seed;
	
	@Column(name = "title")
	private String title;
	
	
	@Column(name = "KSIZE")
	private long size = 0;
	
	/** 
	 * OId is the ObjectId (all versions share the same id)
	 *  Id is the version id 
	 **/
	@Column(name = "OID")
	private Long oid = null;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeResourceTag.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "GROUP_ID")
	private ResourceTag group;

 	/** public Area or internal information (ie. secured) */
	@Column(name = "ISPUBLIC")
	private boolean ispublic = true;

	
	@Transient
	Content contentowner = null;
	

	public int getWidth() {return 0;}
	public int getHeight() {return 0;}
	
	
	@Override
	public void setInPortalVersion(boolean b) {
		this.in_portal=b;
	}
	
	@Override
	public boolean isInPortalVersion() {
		return this.in_portal;
	}
	
	@Override
	public OffsetDateTime getUploadOffsetDateTime() {
		return super.getCreationOffsetDateTime();
	}

	
	@Override
	public User getUploadUser() {
		return super.getLastModifiedUser();
	}

	@Override
	public Serializable getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long) id;
	}
	
	public void resetId() {
		this.id=null;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setContentOwner(Content owner) {
		contentowner = owner;
	}
	
	public Content getContentOwner() {
		return contentowner;		
	}
	
	public void setName(String name) {
		this.name = name;		
	}
	
	public void setVersion(int version) {
		this.version = version;	
	}
	
	public String getName() {
		if (this.name==null)
			this.name=getId().toString();
		return name;				
	}
	
	public int getVersion() {
		return version;			
	}
	
	public Resource getPreviousVersion() {
		return previousVersion;	
	}
	
	public int getMode() {
		return mode;				
	}
	public String getSeed()	{
		return seed;				
	}
	
	public void setSeed(String seed) {
		this.seed=seed;			
	}
	
	public void setSize(long size) {
		this.size=size;			
	}
	
	@Override
	public boolean isPublicArea() {
		return this.ispublic;			
	}
	
	public void setPublic(boolean value) {
		this.ispublic = value;			
	}
									
	public void setPreviousVersion(Object resource) {
		previousVersion=(Resource)resource;	
	}
	
	public boolean isHeadVersion() {
		return ishead;				
	}

	public void setHeadVersion(boolean value) {
		ishead = value;				
	}
	
	public void setOId(Long id) {
		this.oid=id;
	}
	
	@Override
	public Long getOId() {
		return oid;
	}
	
	public void setGroup(ResourceTag group) {
		this.group=group;
	}
	
	public ResourceTag getGroup() {
		return group;
	}
	
	/**
	 * must be overriden by subclasses
	 * 
	 */
	public Resource clone() {
		return null;
	}


	/**
	 * <p>toString is used to display info of the Object for the developers</p>
	 */

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append( super.toString()+ " | ");
		str.append("name: " + getName() + " | ");
		str.append("title: " + getTitle() + " | ");
		str.append("version: " + getVersion() + " | ");
		str.append("oid: "+ getOId() + " | ");
		if (getPreviousVersion()!=null) {
			str.append("prev. version: " + getPreviousVersion().getName()+ " | ");
			if (getPreviousVersion() instanceof Versionable)
				str.append(" v" + ( (Versionable<?>) getPreviousVersion()).getVersion()+ " | ");
		}
		str.append("head: "+ (isHeadVersion()?"YES":"NO") + " | ");
		str.append("mode: " + getMode() + " | ");
		if (getSeed()!=null) 
			str.append("seed: " + getSeed() + " | ");
		str.append("size: " + String.valueOf(size) + " bytes | ");
		return str.toString();
	}
	
	public long getSize() {
		return this.size;
	}

	public String getDescription() {
		return null;
	}
	
	@Override
	public int getNextVersion() {
		return version+1;
	}
	
	@Override
	public String getMetadataAsString() {
		return getMetadataAsString(null); 
	}
	
	@Override
	public String getMetadataAsString(DateTimeFormatter df) {
		StringBuilder str = new StringBuilder(); 
		if (getLastModifiedUser()!=null)
			str.append(getLastModifiedUser().getFirstLastName());
		if (getLastModifiedOffsetDateTime()!=null) {
			if (df==null)
				str.append(". " + getLastModifiedOffsetDateTimeColloquial());
			else {
				str.append(". " + df.format(getLastModifiedOffsetDateTime()));
			}
		}
		return str.toString();
	}
	
	static private Map<String, String> resource_glyphicons = new HashMap<String, String>();
	
	static {
		
		resource_glyphicons.put("pdf", "fa-duotone fa-file-pdf");
		resource_glyphicons.put("word", "fa-duotone  fa-file-word");
		resource_glyphicons.put("image", "fa-duotone fa-file-image");
		resource_glyphicons.put("excel", "fa-duotone fa-file-excel");
		resource_glyphicons.put("powerpoint", "fa-duotone fa-file-powerpoint");
		resource_glyphicons.put("link", "fa-duotone fa-chain");
		
		resource_glyphicons.put("zip", "fa-duotone fa-file-zip");
		resource_glyphicons.put("video", "fa-duotone fa-file-video");
		resource_glyphicons.put("audio", "fa-duotone fa-file-audio");
		
		resource_glyphicons.put("msg", "fa-duotone  fa-file-text");
		resource_glyphicons.put("file", "fa-duotone fa-file");
		resource_glyphicons.put("text", "fa-duotone  fa-file-text");

		resource_glyphicons.put("exe", "fa-duotone fa-file");
		
		
		resource_glyphicons.put("java", "fa-duotone fa-file-code");
		resource_glyphicons.put("html", "fa-duotone fa-file-code");
		resource_glyphicons.put("css",  "fa-duotone fa-file-code");
		resource_glyphicons.put("js",   "fa-duotone fa-file-code");
		resource_glyphicons.put("sql",  "fa-duotone fa-file-code");
		resource_glyphicons.put("txt",  "fa-duotone fa-file-alt");
	}
	

											
static private Map<String, String> resource_fa_freeicons = new HashMap<String, String>();
	
	static {
		
		resource_fa_freeicons.put("pdf",        "fa-duotone fa-file-pdf-o");
		resource_fa_freeicons.put("word",       "fa-duotone fa-file-word-o");
		resource_fa_freeicons.put("image",      "fa-duotone fa-file-image-o");
		resource_fa_freeicons.put("excel",      "fa-duotone fa-file-excel-o");
		resource_fa_freeicons.put("powerpoint", "fa-duotone fa-file-powerpoint-o");
		resource_fa_freeicons.put("link",       "fa-duotone fa-chain-o");
		
		resource_fa_freeicons.put("zip",   "fa  fa-file-zip-o");
		resource_fa_freeicons.put("video", "fa fa-file-video-o");
		resource_fa_freeicons.put("audio", "fa fa-file-audio-o");
		
		resource_fa_freeicons.put("msg", "fa  fa-file-text-o");
		resource_fa_freeicons.put("file", "fa fa-file-o");
		resource_fa_freeicons.put("text", "fa fa-file-text-o");
		resource_fa_freeicons.put("exe",  "fa fa-file-o");
		resource_fa_freeicons.put("java", "fa  fa-file-code-o");
		resource_fa_freeicons.put("html", "fa  fa-file-code-o");
		resource_fa_freeicons.put("css",  "fa  fa-file-code-o");
		resource_fa_freeicons.put("js",   "fa  fa-file-code-o");
		resource_fa_freeicons.put("sql",  "fa  fa-file-code-o");
		resource_fa_freeicons.put("txt",  "fa  fa-file-alt-o");
	}
	
	/** 
	 * 
	 * @param key
	 * @return
	 */
	public static String getResourceGlyphIconByKey(String key) {
		return resource_glyphicons.get(key);
	}

	/** 
	 * 
	 * @param key
	 * @return
	 */								
	public static String getResourceFAFreeByKey(String key) {
		return resource_fa_freeicons.get(key);
	}
}

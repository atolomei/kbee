package com.novamens.kbee.content.resource;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.SignedFile;
import com.novamens.content.service.kbfs.KBFSResourceService;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbfs.FileServerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;

/**
 * import com.novamens.kbfs.KBFSStorageType;
 * 
 * V2
 * --
 * bucket
 * objectkey
 *
 * V1
 * --
 * path
 * 
 * 
 * Externo
 * -------
 * path
 * 
 * 
 * StorageType
 * -----------
 * "File System" 	 	 = 1
 * "Minio"  	 = 2
 * "KBFSV3"  	 = 3
 * "KBFSV4"  	 = 4
 * "External" 	 = 20
 * "LocalMapped" = 30
 *   
 * externallystored -> si actualmente esta externo, si el mode es local, entonces debera traerlo en algun momento (desde path)
 * storagemode
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name="RESOURCE_ID")
@Table(name = "KFILE")
public class KBFileImpl extends AbstractResource implements KBFile {
	
	public static final String  FILE_TYPE  = "file";
	public static final String  IMAGE_TYPE = "image";
	public static final String  VIDEO_TYPE = "video";
	public static final String  AUDIO_TYPE = "audio";

	static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KBFileImpl.class.getName());

	
	@Column(name = "exists_in_object_storage")
	private boolean exists_in_object_storage = true;
	 
	@Column(name = "minor")
	private boolean minor = false;

	@Column(name = "PATH")
	private String url = null;
	
	@Column(name = "subtitle")
	private String subtitle;
	
	@Column(name = "description")
	private String description = null;

	@Column(name = "width")
	private int width = 0;
	
	@Deprecated
	@Column(name = "crc32str")
	private String crc32str;

	@Column(name = "sha256")
	private String sha256;
	
	@Column(name = "signed")
	private boolean signed;
	
	@OneToMany(mappedBy = "file", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = KbeeSignedFile.class)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
	private List<SignedFile> signatures = new ArrayList<SignedFile>();
	
	
	@Column(name = "height")
	private int height = 0;

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "uploadedUser")
	private User uploadedUser;
	
	@Column(name = "uploadedDate")
	private OffsetDateTime uploadedDate;

	@Column(name = "externallyStored")
	private boolean externallyStored = false;
	
	@Column(name = "storageMode")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.resource.KBFSStorageTypeUserType")
	private KBFSStorageType storageType;

	@Column(name = "bucketName")
	private String bucketName;

	@Column(name = "objectName")
	private String objectName;
	
	@Column(name = "shard")
	private int shard = 1;
	
	@Column(name = "fsid")
	private String fsid;
	
	@Column(name = "kfsize")
	private long kfsize;

	@Column(name = "isencrypted")
	private boolean isEncrypted;
	
	@Column(name = "external_id")
	private String externalId;
	
	@Override	public void setUploadOffsetDateTime(OffsetDateTime date) 	{this.uploadedDate=date;	}
	@Override	public void setUploadUser(User user) 						{this.uploadedUser = user;	}		
	@Override	public User getUploadUser() 								{return this.uploadedUser;	}
	@Override	public OffsetDateTime getUploadOffsetDateTime() 			{return this.uploadedDate;	}
	
	@Transient 
	private String name_without_extension = null;

	@Transient
	private Content owner = null;

	@Transient
	private File filecache = null;

	@Transient
	private Boolean b_isfile = null;
	
	@Transient
	private String localpath = null;


	public KBFileImpl() {
		super();
	}

	@Override
	public void setWidth(int w) {
		width=w;
	}
	
	@Override
	public void setHeight(int h) {
		height=h;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		name_without_extension=null;
		if (this.getTitle()==null)
			setTitle(name);
	}
	
	@Override
	public String getBaseName() {
		if (name_without_extension==null)
			name_without_extension = FilenameUtils.getBaseName(getName()); 
		return name_without_extension;
	}
	
	@Override
	public String getPath() {
		return getName();
	}
	
	@Override
	public String getLocalPath() {
		return localpath;
	}
	
	public void setLocalPath(String localpath) {
		this.localpath= localpath;
	}
	
	public Content getOwner() {
		return this.owner;
	}
	
	public int getShard() {
		return shard;
	}
	
	public void setShard(int shard) {
		this.shard = shard;
	}
	
	/**
	 * KBFS1. Relative URL inside the File Server
	 * KBFS2. StorageType/shard:bucket:objectname
	 */
	@Override
	public String getUrl() {
		
		if (getStorageType()==KBFSStorageType.KBFS1)
			return url;
		
		else if (getStorageType()==KBFSStorageType.Minio)
			return KBFSStorageType.Minio.getKey()+"/"+String.valueOf(getShard())+":" + getBucketName() + ":" + getObjectName();
		

		else if (getStorageType()==KBFSStorageType.AmazonS3)
			return KBFSStorageType.AmazonS3.getKey()+":" + getBucketName() + ":" + getObjectName();

		
		else if (getStorageType()==KBFSStorageType.MinioArchive)
			return KBFSStorageType.MinioArchive.getKey()+"/"+String.valueOf(getShard())+":"+getBucketName() + ":" + getObjectName();

		
		else if (getStorageType()==KBFSStorageType.Odilon)
			return KBFSStorageType.Odilon.getKey()+"/"+String.valueOf(getShard())+":" + getBucketName() + ":" + getObjectName();
		
		
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url=url;
	}
	
	/**
	 * KBFS2
	 */
	@Override
	public String getBucketName() {
		return bucketName;
	}
	
	@Override
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
	@Override
	public String getObjectName() {
		return objectName;
	}
	
	@Override
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public void setExternalId(String id) {
		this.externalId = id;
	}
	
	@Override
	public String getUploadOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial(this.getUploadOffsetDateTime(), "ago");
	}
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return getLastModifiedOffsetDateTimeColloquial(getLastModifiedOffsetDateTime(), classago);
	}
	
	private String getLastModifiedOffsetDateTimeColloquial(OffsetDateTime date, String classago) {
		if (date==null) 
			return "";
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		try {
			if (b_isfile==null) {
				b_isfile = Boolean.valueOf(this.getService(KBFSResourceService.class).isObject());
			}
			return b_isfile.booleanValue(); 
		} catch (Exception e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean isIndexable() {
		return true;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return getService(KBFSResourceService.class).getObject();
		}
		catch (FileServerException e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	/**
	 * useb by WebResourceResponse
	 */
	@Override
	public String getContentType() {
		
			String s=getFileName();
			
			if (s!=null) {
				String ext = FilenameUtils.getExtension(s);
				if (ext!=null && ext.toLowerCase().equals("png"))
						return "image/png";
				return 	mimeTypesMap.getContentType(s);
			}
			else {
				try {
					if (isBinaryFile()) {
						String ext = FilenameUtils.getExtension(getFileName());
						if (ext!=null && ext.toLowerCase().equals("png"))
							return "image/png";
						return 	mimeTypesMap.getContentType(getFileName());
					}
					else {
						return 	mimeTypesMap.getContentType("file");
					}
					} catch (IOException e) {
						logger.error(e);
						return 	mimeTypesMap.getContentType("file");
					}
				}
	}

	@Override
	public String getSubTitle()	{
		return subtitle;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Si el domain es File Reader. La URL del File es absoluta, sino es relativa al FileServer
	 */
	@Override
	public java.io.File getFile() throws IOException {
		
		if (this.filecache!=null) {
			logger.debug("cache KBFile -> " + filecache.getName());
			return this.filecache;
		}
		
		if (getStorageType()==KBFSStorageType.KBFS1 || getStorageType()==KBFSStorageType.Odilon  || getStorageType()==KBFSStorageType.Odilon || getStorageType()==KBFSStorageType.Minio || getStorageType()==KBFSStorageType.AmazonS3) {
			try {
				this.filecache = this.getService(KBFSResourceService.class).getDownloadedFile();
				this.b_isfile=null;
				
				if (this.filecache==null) {
					logger.error("this.getService(KBFSResourceService.class).getDownloadedFile() -> returned null");
				}
				
				return this.filecache;
				
			} catch (Exception e) {
				if ((e.getMessage()!=null) && e.getMessage().startsWith("File System. File not in disk"))
						logger.debug(e);
				else
					logger.error(e);
				throw new IOException(e);
			}
		}
		logger.error(
			    "getStorageType() not supported " +
			    (getStorageType() != null ? getStorageType().getDisplayName() : "null")
			);
		return null;
	}
	
	

	/**
	 * Name and FileName are the same
	 */
	@Override
	public void setFileName(String name) {
		setName(name);
	}
	
	

	@Override
	public String getFileName() {
		
		if (getName()!=null)
			return getName();
		
		if (getStorageType()==KBFSStorageType.KBFS1)
			return FilenameUtils.getName(getUrl());

		try {
			if (getFile()!=null)
				return getFile().getName();
			
		} catch (IOException e) {
			logger.error(e);
		}
		return "error";
	}
	
	
	/**
	 * true if the resource is always external and must never be
	 * stored by KBEE/RPDD
	 * 
	 * false if the resource must be pulled from the external repository (on demand)
	 */
	@Override
	public boolean isGateway() {
		return externallyStored;
	}
	
	@Override
	public void setGateway(boolean externallyStored) {
		this.externallyStored = externallyStored;
	}
	
	@Override
	public KBFSStorageType getStorageType() {
		return storageType;
	}
	
	@Override
	public void setStorageType(KBFSStorageType storageType) {
		this.storageType = storageType;
	}
	
	@Override
	public String getDescription(int maxchars) {
		if (getDescription()==null)
			return "";
		if (getDescription().length()<=maxchars)
			return getDescription();
		if (maxchars<4)
			return getDescription().substring(0, maxchars);
		return getDescription().substring(0, maxchars-3)+"...";
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
				// df = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
				str.append(". " + df.format(getLastModifiedOffsetDateTime()));
			}
		}

		if (width!=0 || height!=0)
			str.append(". " + String.valueOf(width) +"px x " + String.valueOf(height)+"px");

		return str.toString();
	}
	
	@Deprecated
	@Override
	public String getCRC32() {
		return this.crc32str;
	}
	
	@Override
	public void setSize(long size) {
		this.kfsize=size;
		super.setSize(size);
	}
	
	@Override
	public long getSize() {
		
		if (this.kfsize>0)
			return this.kfsize;
		
		// compatibility
		if (super.getSize()>0 && this.kfsize<=0)
				return super.getSize();
		
		try {
			if (this.kfsize<=0 && getFile()!=null) 
				this.kfsize = getFile().length();
		} catch (IOException e) {
			logger.warn(" getSize(). File error");
			this.kfsize=-1;
		}
		return this.kfsize;
	}
	@Override
	public boolean getIsEncrypted() {
		return isEncrypted;
	}

	@Override
	public void setIsEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	@Override
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		
		if (getLastModifiedUser()!=null)
			str.append("user: " + getLastModifiedUser().getFirstLastName() +" | ");
		
		if (getLastModifiedOffsetDateTime()!=null) 
					str.append("modified: " + (this.getUploadOffsetDateTime()!=null? this.getUploadOffsetDateTime().toString() :"null")  + " | ");

		if (width!=0 || height!=0)
			str.append("image size: " + String.valueOf(width) +"px x " + String.valueOf(height)+"px | ");

		
		if (getBucketName()!=null)
			str.append(" BucketName: " + getBucketName()+" | ");
		
		if (getObjectName()!=null)
			str.append(" ObjectName: " + getObjectName()+" | ");
		
		
		str.append(" SHA256: " + (getSHA256()!=null? (getSHA256()+" | " ): "null | "));
		//str.append(" CRC 32: " + (getCRC32()!=null? (getCRC32()+" | "): "null | "));
		
		if (getUrl()!=null)
			str.append("  url: " + getUrl());
		
		return str.toString();
	}

	
	
	@Override
	public boolean isImage() {
		try {
			return FSUtils.isImage(getFileName());
		} catch (Exception e) {
			logger.warn(e);	
			return false;
		}
	}
	
		
	@Override
	public boolean isAudio() {
		try {
			return FSUtils.isAudio(getFileName());
		} catch (Exception e) {
			logger.warn(e);	
			return false;
		} 
	}
	
 
	@Override
	public boolean isVideo() {
		try {
			return FSUtils.isVideo(getFileName());
		} 
		catch (Exception e) {
			logger.warn(e);
			return false;
		} 
	}

	 
	@Override
	public String getDisplayName() {
		return (getTitle()!=null ? getTitle() : getName());
	}
	
	
	@Override
	public String getKBFS1Path() {
		if (getStorageType()==KBFSStorageType.KBFS1)
			return getUrl();
		return null;
	}
	
	@Override
	public void setKBFS1Path(String path) {
		if (getStorageType()==KBFSStorageType.KBFS1)
			setUrl(path);
	}
	
	@Override
	public String getFSID() {
		return this.fsid;
	}
	
	@Override
	public void setFSID(String fsid) {
		this.fsid=fsid;
		
	}

	@Override
	public String getGlyphIcon() {
			return FSUtils.getGlyphIcon(getFileName());
	}
	
	@Override
	public String getFontAwesomeFreeIcon() {
		try {
			String filename = getFileName();

			if (filename ==null)						return getResourceFAFreeByKey("file");
			if (FSUtils.isPdf(filename))				return getResourceFAFreeByKey("pdf");
			if (FSUtils.isImage(filename))				return getResourceFAFreeByKey("image");
			if (FSUtils.isWord(filename))				return getResourceGlyphIconByKey("word");
			if (FSUtils.isExcel(filename))				return getResourceFAFreeByKey("excel");
			if (FSUtils.isPowerpoint(filename))			return getResourceFAFreeByKey("powerpoint");
			if (FSUtils.isVideo(filename))				return getResourceFAFreeByKey("video");
			if (FSUtils.isAudio(filename))				return getResourceFAFreeByKey("audio");
			if (FSUtils.isZip(filename))				return getResourceFAFreeByKey("zip");
			if (FSUtils.isExe(filename))				return getResourceFAFreeByKey("exe");
			if (FSUtils.isMsg(filename))				return getResourceFAFreeByKey("msg");
			return getResourceFAFreeByKey("file");
		} catch (Exception e) {
			logger.error(e);
			return  getResourceFAFreeByKey("file");
		}
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	@Override
	public void setMinor(boolean b) {
		 minor=b;
	}
	
	@Override
	public boolean isMinor() {
		return minor;
	}
	
	@Override
	public void setSHA256(String base64str) {
		this.sha256=base64str;
	}
	
	@Override
	public String getSHA256() {
		return this.sha256;
	}

	@Override
	public boolean isExistInObjectStorage() {
		return exists_in_object_storage;
	}
	
	@Override
	public void setisExistInObjectStorage(boolean exists) {
		exists_in_object_storage	 = exists;
	}
	
	@Override
	public boolean isSigned() {
		return signed;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	
	public List<SignedFile> getSignatures() {
		return signatures;
	}
	
	public void setSignature(SignedFile signed) {
		((KbeeSignedFile)signed).setFile(this);
		setSigned(true);
		this.signatures.add(signed);
	}
}


package com.novamens.content.resource;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.novamens.content.base.Resource;
import com.novamens.dom.KBFSStorageType;
import com.novamens.security.User;

public interface KBFile extends Resource  {
	
	static final int MAX_DESCRIPTION_SIZE = 2048;
	
	// ------------
	// java.io.File
	// ------------
	public String getFileName();
	public void setFileName(String name);
	public long getSize();
	public boolean isIndexable();
	

	/**
	 * Minor files do not requiere backup
	 * @param b
	 * @return
	 */
	public void setMinor(boolean b);
	public boolean isMinor();
	
	public void resetId();

	
	// Url returns a protocol dependant url
	// for KBFS1 it is the local directory path
	// for KBFS2 it is:   KBFS2/shard:bucket:objectname
	// for KBFS2Archive  it is:   KBFS2Archive/shard:bucket:objectname
	public String getUrl();
	public void setUrl(String url);
	
	public String getKBFS1Path();
	public void setKBFS1Path(String path);

	
	public InputStream getInputStream() throws IOException;
	public String getContentType(); // MiMeType for WebResourceResponse
	
	public String getLocalPath();
	
	public String getTitle();
	public String getSubTitle();
	public String getDescription();
	public String getDescription(int maxchars);
	
	public void setTitle(String title);
	public void setSubtitle(String subtitle);
	public void setDescription(String description);
	
	public java.io.File getFile() throws IOException;
	
	public String getBaseName();
	
	@Deprecated
	public String getCRC32();
	
	
	// Base64 encoded SHA-256 
	public void setSHA256(String crcToHex);
	public String getSHA256();

	
	
	public String getDisplayName();
	
	// Esto debe pasarse a KBImageImpl
	//
	void setWidth(int w);
	void setHeight(int h);
	int getWidth();
	int getHeight();
	
	public boolean isImage();
	boolean isAudio();
	boolean isVideo();
	
	public String getGlyphIcon();
	
	public String getLastModifiedOffsetDateTimeColloquial();
	public String getMetadataAsString(DateTimeFormatter df);
	 
	public OffsetDateTime getUploadOffsetDateTime();
	public void setUploadOffsetDateTime(OffsetDateTime date);
	public void setUploadUser(User user);	
	public User getUploadUser();

	public String getUploadOffsetDateTimeColloquial();
	
	public boolean isGateway();
	public void setGateway(boolean gateway);
	
	public KBFSStorageType getStorageType();
	public void setStorageType(KBFSStorageType storageMode);
	
	/** -------------------------------------------------------- 
	 * KBFS2
	 */
	public String getBucketName();
	public String getObjectName();

	public void setBucketName(String name);
	public void setObjectName(String name);
	
	public int getShard();
	public void setShard(int shard);
	
	public String getFSID();
	public void setFSID(String fsid);

	public boolean getIsEncrypted();
	public void setIsEncrypted(boolean isEncrypted);
	
	default public boolean isExistInObjectStorage() {return true;}
	public void setisExistInObjectStorage(boolean exists);
	
	public boolean isSigned();
	public List<SignedFile> getSignatures();
}
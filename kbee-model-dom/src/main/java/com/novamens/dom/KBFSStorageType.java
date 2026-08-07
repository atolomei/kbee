package com.novamens.dom;


import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum KBFSStorageType implements PersistentEnum, Serializable {

	KBFS1 				(1, "filesystem"), 		// Local FS.       Table: KFile.storagemode=1  | KFile.Path -> legacy(KFile.Bucket KFile.ObjectName)
	Minio 				(2, "minio"),			// Minio 	       Table: KFile.storagemode=2  | KFile.Bucket KFile.ObjectName | shard	 
	MinioArchive		(3, "minioarchive"),	// Minio  	       Table: KFile.storagemode=3  | KFile.Bucket KFile.ObjectName | shard
	
	AmazonS3			(10, "s3"),				// Amazon S3       
	AmazonGlacier		(11, "glacier"),		// Amazon Glacier
	External 			(20, "external"),		// External		   Table: KFile.storagemode=20 | KFile.Bucket | KFile.ObjectName	|  KFile.externally stored=true 
	
	
	
	LocallyMapped 		(30, "locallymapped"), 
	Odilon				(40, "odilon");
	
	 
	private int id;
	private String key;
	
	private KBFSStorageType(int code, String key) {
		this.id=code; 
		this.key=key;
	}
	
	public String toString() {
		return ("id: " + getId() + ". key: "+ getKey());
	}

	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getKey()	{
		return key;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KBFSStorageType.this.getClass().getName(), locale);
		return res.getString(this.key);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KBFSStorageType.this.getClass().getName(), locale);
		return  "<span class=\"" + getKey() + "\">" + res.getString(this.getKey()) + "</span>";
	}

	/***
	 * 
	 * @param key
	 * Even if the key is invalid, this method will return a Storage Type (KBFS1)
	 * @return
	 */
	public static KBFSStorageType getByKey(String key) {
		if (key==null)
			return null;
		
		String xkey=key.toLowerCase().trim();
		
		if (xkey.equals(KBFS1.getKey())) return KBFS1;
		else if (xkey.equals(Minio.getKey())) return Minio;
		else if (xkey.equals(MinioArchive.getKey())) return MinioArchive;
		else if (xkey.equals(AmazonS3.getKey())) return AmazonS3;			 
		else if (xkey.equals(AmazonGlacier.getKey())) return AmazonGlacier;
		else if (xkey.equals(External.getKey())) return External;
		else if (xkey.equals(LocallyMapped.getKey())) return LocallyMapped;
		
		//logger.debug(KBFSStorageType.class.getName() + " | "+key+" is not a valid key, trying to find by id ");
		
		try {
			Integer n=Integer.valueOf(key);
			return getById(n);
		} catch (Exception e) {
			//logger.debug(e.getClass().getName() + " | can not find it by id either");
		}
		
		//logger.debug("returning default KBFS1 ");
		return KBFS1;
	}
	
	public static KBFSStorageType getById(int n) {
			 
		if (n==KBFS1.getId()) 				return KBFS1;
		else if (n==Minio.getId()) 				return Minio;
		else if (n==MinioArchive.getId()) 		return MinioArchive;
		else if (n==AmazonS3.getId()) 		return AmazonS3;			 
		else if (n==AmazonGlacier.getId()) 	return AmazonGlacier;
		else if (n==External.getId()) 			return External;
		else if (n==LocallyMapped.getId()) 		
			return LocallyMapped;
		
		// logger.debug("returning default KBFS1 ");
		return KBFS1;
	}
}

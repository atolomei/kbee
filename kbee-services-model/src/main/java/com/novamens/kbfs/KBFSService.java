package com.novamens.kbfs;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.novamens.dom.KBFSStorageType;
import com.novamens.service.SystemService;


/**
 * <p>
 * 
 * </p>
 */
public interface KBFSService extends SystemService {
	
	
		public String getDisplayName();
		public KBFSStorageType getKBFSStorageType();
		
		public String normalize(String name);
		
		// Put
		public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, long size, String contentType)  throws FileServerException;
		public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, String contentType) 			throws FileServerException;
		public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream)								    throws FileServerException;
							
		// Shard is calculated as function of (bucketName, objectName)
		public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException;
		public void putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType) 			throws FileServerException;
		public void putObject(String bucketName, String objectName, String filename, InputStream stream)								throws FileServerException;
		public void putObject(String bucketName, String objectName, String fileName) 													throws FileServerException;
		
		// Get URL
		// Shard is calculated as function of (bucketName, objectName)
		public String presignedGetObject(String bucketName, String objectName) 						throws FileServerException;
		public String presignedGetObject(String bucketName, String objectName, int expires_seconds) throws FileServerException;
		
		public String presignedGetObject(String fsid, String bucketName, String objectName) 						throws FileServerException;
		public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds) 	throws FileServerException;

		
		// Get Stream
		public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException;
		public InputStream getObject(String bucketName, String objectName) throws FileServerException;

		
		// Get File from a local work directory (that will last 1-2 days)
		public File getDownloadedFile(String bucketName, String objectName, String fileName) throws FileServerException;
		public File getDownloadedFile(String fsid, String bn, String on, String fileName) throws FileServerException;
		
		// Remove
		public void removeObject(String bucketName, String objectName) throws FileServerException;
		public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException;
		
		// Ping 
		public String ping();

		//public long getObjectSize(String bucketName, String objectName) throws FileServerException;
		public boolean isObject(String bucketName, String objectName) throws FileServerException;
		public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException;
							
		
		public Integer getShard(String bucketName, String objectName);
		public String  getFSId(Integer shard);
		public Integer  getShard(String fsid);
		
		public void setMinor(boolean b);
		public boolean isMinor();
		
		public List<String> listBuckets() throws FileServerException;
		public Map<String, String> getInfo();
		
		
}

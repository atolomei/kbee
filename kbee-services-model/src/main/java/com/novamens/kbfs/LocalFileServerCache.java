package com.novamens.kbfs;

import java.io.File;
import java.io.InputStream;

import com.novamens.service.SystemService;

public interface LocalFileServerCache extends SystemService {

	public void put(String bucketName, String objectName, InputStream stream, String fileName);
	public void remove(String bucketName, String objectName);
	public void remove(String key);
	public File get(String bucketName, String objectName);
	public boolean containsKey(String bucketName, String objectName);
	public String getLocalFileServerCacheWorkDir();
	
	public long getTotalDisk();
	public int getTotalItems();
	
	public void setCacheDuration(long miliseconds);
 	public long getCacheDuration();
 	
}



//public void put(String bucketName, String objectName, File file);

package com.novamens.content.service.kbfs;
import java.io.File;
import java.io.InputStream;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.KBFSService;
import com.novamens.service.FactoryService;
import com.novamens.service.ObjectService;

public interface KBFSResourceService extends ObjectService, FactoryService {
 	
	public void putObject(String filename, InputStream stream) throws FileServerException;
	public void putObject(String filename, InputStream stream, String contentType) throws FileServerException;
	
	/**
	 * <p>Removes de binary object from the Storage (minio, S3, etc.)
	 * @throws FileServerException
	 * </p>
	 */
	
	public void removeObject() throws FileServerException;

	public String presignedGetObject() throws FileServerException;
	public InputStream getObject() throws FileServerException;
	public File getDownloadedFile() throws FileServerException;
	public String presignedGetObject(int expires_seconds) throws FileServerException;
	public String normalize(String name);

	public KBFSService getKBFS1();
	public KBFSService getKBFS2();
	public KBFSService getKBFSAmazonS3();
	public KBFSService getOdilon();
	
	public KBFSService getKBFSService();
	public KBFile getKBFile();
	public boolean isObject() throws FileServerException;
	public void update() throws FileServerException;
	public void setDefaultKBFSStorageType(KBFSStorageType kbfsStorageType);
	public void setPreassignedShard(Integer destinationShard);
	

}

package com.novamens.kbfs;

import java.io.File;
import java.io.InputStream;
import java.time.OffsetDateTime;

public interface FileServerS3 extends KBFSService {

	public final String KEY = "s3";
	
	public String getEndPoint();
	public String getAccessKey();
	public String getSecretKey();
	public String getEnvironment(); // dev or prod (replaces FsId)
	
	//public String getFSId();
	//public String getFSId(Integer shard);
	//public Integer getShard(String shardid);
	//public Integer getShard();
	//public double getProbability();
	//public void setProbability(double d);

	
	public String reconnect()  throws FileServerException;
	
	//public String reconnect(String url, String accessKey, String secretKey)  throws FileServerException;

	public boolean isReadOnly();
	
	public String ping(Integer shard);
	public String getEndPoint(Integer shard);
	public String getAccessKey(Integer shard);
	public String getSecretKey(Integer shard);
	public Integer getShard(String bucketName, String objectName);
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, long size,	String contentType) throws FileServerException;
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException;
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream)		throws FileServerException;
	public void putObject(Integer shard, String bucketName, String objectName, String fileName) throws FileServerException;
	public String presignedGetObject(Integer shard, String bucketName, String objectName) throws FileServerException;
	public String presignedGetObject(Integer shard, String bucketName, String objectName, int expires_seconds)	throws FileServerException;
	public InputStream getObject(Integer shard, String bucketName, String objectName) throws FileServerException;
	public File getDownloadedFile(Integer shard, String bucketName, String objectName, String fileName) throws FileServerException;
	public void removeObject(Integer shard, String bucketName, String objectName) throws FileServerException;
	public boolean isObject(Integer shard, String bucketName, String objectName) throws FileServerException;
	// public String reconnect(Integer shard, String url, String accessKey, String secretKey) throws FileServerException;
	
	
	
	public boolean isEnabled();
	public OffsetDateTime getDateConnected();
	

	
}

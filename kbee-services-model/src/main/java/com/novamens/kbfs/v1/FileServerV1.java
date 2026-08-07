package com.novamens.kbfs.v1;

import java.io.File;
import java.io.IOException;

import javax.crypto.spec.SecretKeySpec;

import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.KBFSService;

/**
 *  <p>
 *    THis is a legacy version of the KBFService that should not be used in production.
 *    It is still being used by the {@link ThumbnailService}
 *    Files are stored on the local File System.
 *    
 *    @see {@link KbeeFileServer}

 * </p>
 */
public interface FileServerV1 extends KBFSService {

	
	public final String KEY = "File System";
	
	static  final public String FAST   = "fast";
	static  final public String SLOW   = "slow";

	//------------------------
	//
	public String 			getRelativeURLForFile(String srcfilename, String srcid, String domain)       	throws IOException;
	public FSOutputStream 	getFSOutputStream(String srcfilename, String srcid, String domain) 				throws IOException;
	public FSInputStream 	getFSInputStream(String uniqueurl, long size) 					   				throws IOException;
	
	
	public FSOutputStream getFSOutputStream(String srcfilename, String srcid, String domain, String repo_type) 	throws IOException;
	public String getRelativeURLForFile(String srcfilename, String srcid, String domain, String repo_type) 		throws IOException;
	public void remove(String uniqueurl) 																		throws IOException;

	public void close();

	// general info and config
	//
	public String getName();
	public String toString();
	
	public boolean isEncrypted();
	public boolean isCacheEnabled();
	
	public long getSize();
	public long getTotalFiles();
	public String getRootDirectory();
	
	public void cleanDataDirectory();
	public void calculateMetadata();
	

	public void addSize(long byteswritten); 
	public void addToCache(String url, File file) 												throws IOException;
	public SecretKeySpec getKey();
	public void pause();
	public void resume();
	public long getTotalFilesFast();
	public long getTotalFilesSlow();
	
	public String getLocalCacheDirectory(String uid) throws FileServerException;
	
	
}

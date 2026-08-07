package com.novamens.thumbnail;

import java.io.File;
import java.io.IOException;

import com.novamens.service.SystemService;

/**
 * <p>This Service was designed as a cache of thumbnails. 
 * Files added to the Thumbnail Service must have a <strong>unique id</strong> provided by the client of the service.
 * </p> 
 * 
 * <strong>Add to cache</strong>
 * <p>Clients that want to add a file to the ser ver must provide the id, width and height of the thumbnail in pixels, 
 * and the source file to generate the thumbnail.
 * The server stores the thumbnail in the cache.</p>
 * 
 * <strong>Accesing the thumbnail</strong>
 * <p>Clients that acces the thumbnail need to provide the id, width and height
 * (since it is possible for the system to contain several thumbnails for a single id).</p>
 * 
 * 
 * The Exceptions that this service throws are IOException
 * 
 *
 */
public interface ThumbnailService extends SystemService {

	/**
	 * @param url
	 * @throws IOException
	 */
	public File getThumbnailFile(String id, String domain, File srcfile, ThumbnailSize size) throws IOException;
	
	/**
	 * 				
	 * @param url
	 * @throws IOException
	 */
	public File getThumbnailFile(String id, String domain, String urlYT, ThumbnailSize size) throws IOException;
	
	
	/**
	 * 				
	 * @param url
	 * @throws IOException
	 */
	public void remove(String url) throws IOException;
	
	
	/**
	 * shutdowns the Server
	 */
	public void close();

	
	/**
	 * @return true if the thumbnails are encrypted on disk
	 */
	public boolean isEncrypted();

	
	/**
	 * @return true if it uses cache
	 */
	public boolean usesCache();
	
	public void start() throws IOException;
	public void removeAll()	 throws IOException;
	
	public long getSize();

	public String getDefaultThumbnail(String key);

	void evict(String id, String domain) throws IOException;

	long getCacheMiss();
	long getCacheHits();
	

}

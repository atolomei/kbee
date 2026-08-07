package com.novamens.kbee.kbfs.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import com.novamens.util.KbeeFileUtils;

import kbee.util.PropertiesFactory;
import kbee.util.PropertiesHelper;

/**
 * 
 * <p>File System cache</p>
 * <p>FMCache is not MT Safe</p>
 * 
 */
public class FSCache {

	static private FSCache instance = null;

	static private final Properties props = PropertiesFactory.getInstance("kbee").getProperties();
	static private final String	 root				= props.getProperty("com.novamens.kbee.fileserver.fscache.root", "fscache");
	static private final int  	 cachesize 			= PropertiesHelper.getIntProperty(props, "com.novamens.kbee.fileserver.fscache.size", 4000, 10, 100000);
	static private final int  	 timeToIdleSecs 	= PropertiesHelper.getIntProperty(props,"com.novamens.kbee.fileserver.fscache.time-to-idle-secs", 60*60*24, 60, 60*60*24*100);
	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(FSCache.class.getName());

	private Cache ehcache;
	private CacheManager cm;
	private String name = "kbee file server cache";
  
	static public FSCache getInstance() throws IOException {
		if (instance==null) {
			instance=new FSCache();
			instance.start();
		}
		
		return instance;
	}

	private FSCache() {	 
	}
	
 	/**
	 *
	 * @return
	 */

	@SuppressWarnings("static-access")
	protected String getRoot() { 
		return this.root; 
	}


	public synchronized void close() {
		try {
			logger.info("Shutting down FSCache " + name + " (dir: " + getRoot()+")");
			KbeeFileUtils.forceDelete(new File(getRoot()));
 		} 
		catch (IOException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	/**
	 *  cleans the cache directory.
	 */
	private void start() throws IOException {
		
		logger.info("Starting FSCache " + name);
		logger.info("Directory: " + root);
		
		File cachedir = new File(root);
		if (cachedir.exists()) {
			try {
				KbeeFileUtils.forceDelete(cachedir);
			} 
			catch (FileNotFoundException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		
		KbeeFileUtils.forceMkdir(cachedir);

		Configuration cmconfig = new Configuration();
		cmconfig.setName("FSCache");
   		cm = CacheManager.newInstance(cmconfig);
  		
		CacheConfiguration config =  new CacheConfiguration("fmcache", cachesize)
										.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
										.eternal(false)
										.timeToLiveSeconds(0)
										.timeToIdleSeconds(timeToIdleSecs)
										.diskExpiryThreadIntervalSeconds(360)
										.persistence(new PersistenceConfiguration().strategy(Strategy.NONE));
		
		
		ehcache = new Cache(config);
		cm.addCache(ehcache);
		FSEventListener myListener = new FSEventListener();  
		ehcache.getCacheEventNotificationService().registerListener(myListener);
	}
	
 	/**
	 * adds element. key is the key of the original file. 
	 * The file is the cached version. it must have been created previously

	 * @param key
	 * @param file
	 */
	protected void add(String key, File file) {
		ehcache.put(new Element(key, file));
	}

	/**
	 * Listeners remove files from file system upon expiration and eviction.
	 * 
	 */
	protected void remove(String key) {
		Element element = ehcache.get(key);
		File file = null;
		if (element!=null)
				file = (File) ehcache.get(key).getObjectValue();
		
		if (file!=null) {
			synchronized (this) {
				try {
					KbeeFileUtils.forceDelete(file);
					ehcache.remove(key);
				}
					catch (java.io.IOException e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
				}
		}
	}
	
	protected File get(String key) 	{
		Element element = ehcache.get(key);
		if (element!=null) {
			return (File) element.getObjectValue();
		}
 		return null;
	}

	/**
	 * get file to write. If  the standard file exists. 
	 * 
	 * @param relativeurl
	 * @return
	 * @throws IOException
	 */
	protected File getFileToWrite(String relativeurl) throws IOException {
 		synchronized (this) {
 			createDirsIfNotExist(relativeurl);
 		}
		return new File(getRoot() + File.separator + relativeurl);
	}
 	
	private void createDirsIfNotExist(String relativeUrl) throws IOException {
 		String relpath = getRoot() + File.separator + relativeUrl;
		String dirs[] = relpath.split("\\"+File.separator); 
		int n = dirs[dirs.length-1].length();
		File directory = new File(relpath.substring(0, relpath.length()-n));
		if (!directory.exists()) {
			try {
				KbeeFileUtils.forceMkdir(directory);
			}
			catch (java.io.IOException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw e;
			}
		}
	}
	
	public Map<String, String> getStats() {
		Map<String, String> status = new HashMap<String, String>();
		status.put("Name", ehcache.getName());
		status.put("Size", String.valueOf(ehcache.getSize())); 
		// status.put("Mem size", String.valueOf(ehcache.getMemoryStoreSize()));
		return status;
	}
	
}

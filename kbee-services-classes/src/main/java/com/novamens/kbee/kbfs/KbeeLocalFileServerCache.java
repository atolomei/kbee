package com.novamens.kbee.kbfs;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;

import com.novamens.util.KbeeRuntimeException;



/**
 * <p>
 *  
 *  </p>
 * 
 */			
public class KbeeLocalFileServerCache implements LocalFileServerCache, EventListener {
			
	static private final boolean ENABLED = true;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeLocalFileServerCache.class.getName());
	static private Logger startupLogger = LogManager.getLogger("StartupLogger");
	
	static final private String cacheSubdir = "fscache";
	
	static final int TEN_MINUTES =  1000 * 60 * 10; // 10 minutes
	static final int ONE_DAY =  1000 * 60 * 60 * 24; // 1 day
	static final long _CACHE_DURATION = 1000 * 60 * 60 * 24 * 2; // 2 days
	static final int BUFFER_SIZE = 4096;
	
	private	DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
	private	Map<String, CacheEntry> filemap = new ConcurrentHashMap<String, CacheEntry>(200, 0.9f, 2);
	private	DelayQueue<CacheEntry> delayQueue = new DelayQueue<CacheEntry>();
 	private	AtomicLong disk_size = new AtomicLong(0);
 	
 	
 	private String dir;
 	
 	private AtomicBoolean isTempdir = new AtomicBoolean(false);
 	
 	
 	private	long cacheDuration = _CACHE_DURATION; 

 	private Cleaner cleaner;
 	
 	
 	
 	/**------------------------------------------------ 
 	 * 
  	 * 
 	 */
 	public class CacheEntry implements Delayed {
 		public CacheEntry(String key, File file, long etime) {
 			this.key=key;
 			this.file=file;
 			this.etime=etime;
 			this.size=file.length();
 		}
 		
 		public File file;
 		public String key;
 		public long etime;
 		public long size;
		
 		@Override
		public int compareTo(Delayed a) {
			if (this.etime<((CacheEntry) a).etime)
				return -1;
			else if (this.etime>((CacheEntry) a).etime)
				return 1;
			return  0;
		}
 		
		@Override
		public long getDelay(TimeUnit unit) {
			 long diff = etime - System.currentTimeMillis();
			 return unit.convert(diff, TimeUnit.MILLISECONDS);
		}
 	}

 	

 	/**------------------------------------------------
 	 * 
 	 * <p>Simple Thread that cleans up the Queue</p>
 	 *  
 	 */
 	public class Cleaner implements Runnable {
 	
 		private AtomicBoolean inSiesta = new AtomicBoolean(false);
 		
 		
 		public AtomicBoolean isSiesta() {
 			return inSiesta;
 		}
 		
 		public void run() {
 			
			synchronized (cleaner) {
	 			try {
	 				inSiesta.set(true);
	 				wait(ONE_DAY);
	 				
				} catch (InterruptedException e1) {
					logger.debug("Wake up to work");				
				}
	 			finally {
	 				inSiesta.set(false);	
	 			}
			}


 			while (true) {
 				
 				try {
 		
 					cleanUp();
 					
 				} catch (Exception e) {
 					logger.error(e);
 				}
 				
 						if (getTotalItems()==0) {
 							synchronized (cleaner) {
		 						try {
		 							inSiesta.set(true);
		 							wait(ONE_DAY);
		 					 	} catch (Exception e) {
	 								logger.debug("Again awake -> " + e.getClass().getName());	
	 								logger.debug(e.getMessage());
			 				 	}
		 						finally {
		 							inSiesta.set(false);
		 						}
 							}
	 					}
	 					
 						else {
	 						
	 						try {
	 							inSiesta.set(false);
	 							Thread.sleep(TEN_MINUTES);
	 							
		 						
	 						} catch (Exception e) {
 								logger.error(e);
		 				 	}
	 						finally {
	 							logger.debug("Again awake" );
	 						}
	 					}
 			}
 		}
 		
 		
 		
 	}

 	
 	
 	
 	/**------------------------------------------------
 	 * 
 	 * 
 	 */
 	public KbeeLocalFileServerCache() {
 		
		 if (!ENABLED) {
			 startupLogger.info("-----------------------------------------------------------");
			 startupLogger.info("LocalFileServerCache is not enabled");
			 startupLogger.info("Please set in kbee.properties file-> LocalFileServerCache.enabled = true");
			 startupLogger.info("-----------------------------------------------------------");
			 
		 }
		 
 		cleaner = new Cleaner();
 		Thread thread = new Thread(cleaner);
 		thread.setDaemon(true);
 		thread.setName("LocalFileServerCache Cleaner");
 		thread.start();
 	}


 	/*** -----------------------------------
 	 * 
 	 * @param bucketName
 	 * @param objectName
 	 * @param stream
 	 * @param fileName
 	 */
 	@Override
 	public void put(String bucketName, String objectName, InputStream stream, String fileName) {
 	
 		 if (!ENABLED)
 			 return;
 		 
 		String nf = getTempDir() + File.separator + fileName;
 		
 		byte[] buf = new byte[ BUFFER_SIZE ];
 		int bytesRead;
		BufferedOutputStream out = null;
		/**
		 * save File
		 */
		try {
			 out = new BufferedOutputStream(new FileOutputStream(nf), BUFFER_SIZE);
			 while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
				  out.write(buf, 0, bytesRead);
			  }
		} catch (IOException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);		
			
		} finally {
			
			if (stream!=null) { 
				try {
					stream.close();
				} catch (IOException e) {
					logger.error(e);
				}	
			}
			
			if (out!=null) { 
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}	
			}
			
		}
		
		/** add File to Cache */
  		add(bucketName, objectName, new File(nf));
 	
 	}
 	
 	/***
	 *
	 */
 	@Override
 	public void remove(String bucketName, String objectName) {
 		remove(bucketName + "/" + objectName);
 	}

 	@Override
 	public void remove(String key) {

		 if (!ENABLED)
 			 return;
		 
 		if (filemap.containsKey(key)) {
 			CacheEntry ce=filemap.get(key);
 			synchronized (this) {
	 			if (ce!=null && ce.file!=null && ce.file.exists()) {
	 				disk_size.addAndGet(-1 * ce.size);
	 				logger.debug("remove -> " + ce.file);
	 				FileUtils.deleteQuietly(ce.file);
	 			}
	 			filemap.remove(key);
 			}
 		}
 	}

 	
 	@Override
 	public File get(String bucketName, String objectName) {
 		
		 if (!ENABLED)
 			 return null;

		 
 		try {
	 	
 			CacheEntry ce = this.filemap.get(bucketName + "/" + objectName);

	 		if (ce!=null) {
	 			
	 			if (!ce.file.exists()) {
	 				this.filemap.remove(bucketName + "/" + objectName);
	 				return null;
	 			}

	 			/** Reset the cache duration for this entry */ 
	 			synchronized (this) {
		 			long edate = System.currentTimeMillis() + getCacheDuration();
		 	 		CacheEntry newEntry = ce;
		 	 		this.delayQueue.remove(ce);
		 	 		newEntry.etime = edate; 
		 	 		this.delayQueue.add(ce);
	 	 		}
	 			
	  			return ce.file;
	 		}
	 		return null;
 		} finally {
 			//cleanUp();
 		}
 	}

 	@Override
 	public boolean containsKey(String bucketName, String objectName) {
 		return this.filemap.containsKey(bucketName + "/" + objectName);
 	}

 	
 	@Override
 	public String getLocalFileServerCacheWorkDir() {									
 		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() +  File.separator + cacheSubdir;
 	}

 	/**
 	 * <p>Disk Usage in bytes</p>
 	 */
 	@Override
 	public long getTotalDisk() {
		return this.disk_size.longValue();
	}
	
 	/**
 	 *<p>Total items in Cache</p>
 	 */
 	@Override
 	public int getTotalItems() {
 		return this.filemap.size();
	}

	@Override
	public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
	}

	
 	public void setCacheDuration(long miliseconds) {
 		cacheDuration = miliseconds;
 	}
 	
 	public long getCacheDuration() {
 		return cacheDuration;
 		
 	}
 	

		
	@Override
	public void onEvent(Event event) {
        
		if (!(event instanceof EvictCacheServiceEvent))
			return;
		reset();
	}


	
 	/**
 	 * 
 	 */
 	private void cleanUp() {

 		boolean isElements = false;
 		
 		synchronized (this) {
 			CacheEntry ce = this.delayQueue.poll();
 			
 			int before = getTotalItems();
 			
 			isElements = (ce!=null);
 			
		 	while (isElements) {
					try {
						remove(ce.key);
				 		ce = this.delayQueue.poll();
				 		isElements = (ce!=null);
					
					} catch (Exception e) {
						logger.error(e);
						isElements = false;
					}
		 	}
		 	
		 	logger.debug("Clean up work . before -> " +  String.valueOf(before) + " | after -> " + String.valueOf(getTotalItems()));
 			
 		}
 	}

 	
 	/**
 	 * @param bucketName
 	 * @param objectName
 	 * @param file
 	 * 
 	 */
 	private void add(String bucketName, String objectName, File file) {
 		
 		long edate = System.currentTimeMillis() + getCacheDuration();
 		CacheEntry ce = new CacheEntry(bucketName + "/" + objectName,  file, edate);
 		
 		synchronized (this) {
 			
	 		this.filemap.put(bucketName + "/" + objectName, ce);
	 		this.delayQueue.add(ce);
	 		
	 		if (file==null)
	 			throw new IllegalArgumentException(bucketName + "/" + objectName + " -> File is null");
	 		
	 		this.disk_size.addAndGet(file.length());
	 	}
 		
 		
 		if (cleaner.isSiesta().get()) {
 			synchronized (cleaner) {
 				cleaner.notify();
 			}
 		}

 	}
 	
 	
 	/**
  	 * @return
  	 * 
  	 * File.separator + String.valueOf((Double.valueOf(Math.random()*10).intValue()));
  	 * 
 	 */
 	private String getTempDir() {

 		if (this.isTempdir.get())
 			return this.dir;
 										
 		this.dir = this.getLocalFileServerCacheWorkDir() + 	File.separator + workdf.format(LocalDateTime.now());
 						 
 		File base = new File(dir);
 		
 		if (!base.exists()) {
 			synchronized (this) {
	 			try {
					FileUtils.forceMkdir(base);					 
				} catch (IOException e) {
					logger.error(e);
				}
 			}
 		}
 		else if (!base.isDirectory()) {
 			synchronized (this) {
 				FileUtils.deleteQuietly(base);
	 			try {
	 				FileUtils.forceMkdir(base);
				} catch (IOException e) {
					logger.error(e);
				}
 			}
 		}

 		this.isTempdir.set(true);
 		return this.dir;
 	}

	private void reset() {

		String dir = this.getLocalFileServerCacheWorkDir();
		File base  = new File(dir);
		
		synchronized (this) {
	 			try {
	 				this.isTempdir.set(false);
	 				this.filemap.clear();
	 				this.delayQueue.clear();
	 				FileUtils.deleteQuietly(base);
	 				this.disk_size = new AtomicLong(0);
	 				
				} catch (Exception e) {
					logger.error(e);
				}
 		}		
	}

}

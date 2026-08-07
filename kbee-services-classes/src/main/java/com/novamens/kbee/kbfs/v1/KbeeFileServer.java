package com.novamens.kbee.kbfs.v1;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.kbfs.v1.FSOutputStream;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;

import io.odilon.util.FileNameNormalizer;
import kbee.util.FSUtils;
import kbee.util.NumberFormatter;

/**
 *  
 * <p>dbdir and DBCache must be exclusive for each FileServer. 
 * If another FileServer is using the same dirs the constructor will throw IOException. 
 * 
 * Client asks for a OutputStream (file, domain, FAST / SLOW)
 * Client streams file in OutputStream  |
 *
 * Client closes OutputStream (this is neeeded for cache to work correctly)
 * Client asks for a InputStream (relative url includes fast or slow)
 * </p>
 * 
 */
public class KbeeFileServer implements FileServerV1 {

	static private List<String> dbdirs = Collections.synchronizedList(new ArrayList<String>());
	
 	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFileServer.class.getName());
 	
 	static private Logger startupLogger = LogManager.getLogger("StartupLogger"); 	
 	
 	private boolean minor =  false;
 	
 	private String root; 
 	private String cacheDirectory; 

	private String name 			= null;
	private boolean encrypted 		= false;
	private boolean started			= false;
	private boolean cacheenabled 	= false;
	
	private Long  size 	= Long.valueOf(0);
	private Long total  = Long.valueOf(0);
	
 
	private Map<String, Long> data  = new HashMap<String, Long>();		
	
	private SecretKeySpec secretKey;
	private FSCache	cache;
	private String separator_to_replace;
	Integer shard= Integer.valueOf(1);
	
	private SubDirectoryGenerationStrategy subdirgenenartor;

	private Long total_fast = Long.valueOf(0);
 	private Long size_fast  = Long.valueOf(0);
 	private Long total_slow = Long.valueOf(0);
 	private Long size_slow  = Long.valueOf(0);

	private DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");

	private SystemMetricsService mtrics;
	
	protected String getWorkDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath();
	}
	
	
	//protected String getKBFS1Dir() {
	//	return ServiceLocator.getService(ApplicationServerService.class).getKBFS1Dir();
	//}
	
	
	@Override
	public String normalize(String name) {
		return FileNameNormalizer.normalize(name);
	}
	
	/**
 	 * @throws IOException
	 */
	public KbeeFileServer() throws IOException {
		this("KbeeFileServer");
	}

	/**
	 * 
	 */
	public KbeeFileServer(String name) throws IOException {
		this.name = name;
  		start();
	}

	
	/**
	 * @param name
	 * @param dbdir
	 * @throws IOException
	 */
	public KbeeFileServer(String name, String dbdir) throws IOException {
		this.name = name;
		this.root = dbdir;
  		start();
 	}
	
	/**
	 * 
	 */
	@Override
	public String getDisplayName() {
 		return this.getName();
 	}

	/**
	 * 
	 */
	public KbeeFileServer(String name, String dbdir, boolean encrypted) throws IOException {
		this(name, dbdir, new StandardDirectoryStrategy(dbdir), encrypted, encrypted);  
  	}
		
	/**
	 * 
	 */
	public KbeeFileServer(String name, String dbdir, SubDirectoryGenerationStrategy subdirgenstrategy) throws IOException {
		this(name, dbdir, subdirgenstrategy, false, false);
	}
	
	/**
	 * 
	 */
	public KbeeFileServer(String name, String dbdir, SubDirectoryGenerationStrategy subdirgenstrategy, boolean cacheenabled, boolean encrypted) throws IOException {
		this.name = name;
		this.root = dbdir;
		this.cacheenabled = cacheenabled;
		this.encrypted = encrypted;
		this.subdirgenenartor = subdirgenstrategy;
		start();
 	}	
	
	
	@Override
	public KBFSStorageType getKBFSStorageType() {
		return KBFSStorageType.KBFS1;
	}

	
	@Override
	public void setMinor(boolean b) {
		 minor=b;
	}
	
	@Override
	public boolean isMinor() {
		return minor;
	}
	
	/**
	 * 
	 * Get File from a local directory
	 * bucketname is ignored
	 * 
	 */
	@Override
	public File getDownloadedFile(String bucketName, String objectName, String fileName) throws FileServerException {
		String url=objectName;

		if (url==null)
			throw new FileServerException("url is null");
		
		try {
			try {
				getSystemMetricsService().getMeterV1GetObject(bucketName).mark();
				getSystemMetricsService().getMeterV1GetObject().mark();
			} catch (Exception e) {
				logger.error(e);
			}

			return getFileToRead(url);
			
		} catch (IOException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}
	
	/**
	 * 
	 * 
	 */
	// Get Stream
	public InputStream getObject(String bucketName, String objectName) throws FileServerException {
		String url=objectName;
		try {
			
			File file = getFileToRead(url);
			if (file==null || !file.exists())
				throw new FileServerException("File System. File not in disk -> " + bucketName + " - " + objectName);
			try {
				getSystemMetricsService().getMeterV1GetObject(bucketName).mark();
				getSystemMetricsService().getMeterV1GetObject().mark();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + " |  getSystemMetricsService()");
			}
				
			 
			return new BufferedInputStream(new FileInputStream(file));
		} 
		catch (IOException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}

	
	/**
	 */
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException {
		throw new FileServerException("Not Implemented in FileServer V1. putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType)");
	}
	
	/**
	 */
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {
		throw new FileServerException("Not Implemented in FileServer V1. putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType)");
	}
	
	/**
	 */
	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
		throw new FileServerException("Not Implemented in FileServer V1. putObject(String bucketName, String objectName, String filename, InputStream stream)");
	}

	/**
	 */
	@Override
	public boolean isObject(String bucketName, String objectName) throws FileServerException {
		if (objectName==null)
			throw new FileServerException("objectname is null. isObject(..)");
		try {
			File file = getFileToRead(objectName);
			if (file!=null && file.exists())
				return true;
			return false;
		} catch (IOException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
		
	}

	
	/**
	 * 
	 * 
	 */
	public long getObjectSize(String bucketName, String objectName) throws FileServerException {
		try {
			return this.getFileToRead(objectName).length();
		} catch (IOException e) {
			throw new FileServerException(e);
		}
	}

	/**
	 * 
	 * 
	 */
	@Override					
	public void putObject(String bucketName, String objectName, String fileName) throws FileServerException {

		throw new FileServerException("Not Implemented in FileServer V1. putObject(String bucketName, String objectName, String fileName)");
		
		// file.setStorageType(KBFSStorageType.KBFS1); // esto determina a que File Server va el pdf
		// file.setFSOutputStream(fos);
		// file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		// file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		// file.setUploadOffsetDateTime(OffsetDateTime.now());
		// file.setUploadUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	/**
	 * 
	 */
	// Get URL
	@Override
	public String presignedGetObject(String bucketName, String objectName) throws FileServerException {
		throw new FileServerException("Not Implemented in FileServer V1. presignedGetObject(String bucketName, String objectName)");
	}

	/**
	 * 
	 */
	public String presignedGetObject(String bucketName, String objectName, int expires_seconds) throws FileServerException {
			throw new FileServerException("Not Implemented in FileServer V1. presignedGetObject(String bucketName, String objectName, int expires_seconds)");
	}

	/**
	 * 
	 */
	// Remove
	@Override
	public void removeObject(String bucketName, String objectName) throws FileServerException {
		String url=objectName;
		try {
			logger.debug("remove: " + url);
			remove(url);
				
		} catch (IOException e) {
			logger.error(e);

			throw new FileServerException(e);
		}
	}
	

	/**
	 * 
	 * 
	 */
	@Override																			
	public String getRelativeURLForFile(String srcfilename, String objectName, String bucket, String repo_type) throws IOException {
		if (!isStarted())
			throw new IOException("File Server is not started.");
		
		String relativePlainURL = getUrl(srcfilename, objectName, bucket, repo_type);
		
		try {
			getSystemMetricsService().getMeterV1GetObject(bucket).mark();
			getSystemMetricsService().getMeterV1GetObject().mark();
		} catch (Exception e) {
			logger.error(e.getClass().getName()+" | getSystemMetricsService()");
		}
		
		if (isEncrypted()) 
			return getUrl(FSUtils.getEncrytedFileName(srcfilename), objectName, bucket, repo_type);
		else  
			return relativePlainURL;
	}
 	
	
	/** 
	 *
	 * 
	 */
	@Override
	public FSOutputStream getFSOutputStream(String srcfilename, String objectName, String bucket, String repo_type) throws IOException {
		if (!isStarted())
			throw new IOException("File Server is not started.");
		FileWSupport fs = createFileWSupport(srcfilename, objectName, bucket, repo_type, isEncrypted());
		FSOutputStream writer;
		if (isEncrypted())
			writer = new EncryptedFSOutputWriter(this, fs);
		else
			writer = new SimpleFSOutputStream(this, fs);
		
		try {
			getSystemMetricsService().getMeterV1PutObject(bucket).mark();
			getSystemMetricsService().getMeterV1PutObject().mark();
		} catch (Exception e) {
			logger.error(e.getClass().getName()+" | getSystemMetricsService()");
		}
		
		return writer;
	}

 	/**
	 * If the file was saved in a different OS and then ported
	 * the separator in the url will be different. It
	 * is necessary to normalize.
	 *  
	 */
	
	public File getFileToRead(String url) throws IOException {
		if (!isStarted())
			throw new IOException("File Server is not started.");
		
		if (url ==null) {
			throw new IOException("url is null");
		}
		
		String osuri = url.replace(separator_to_replace, File.separator);
		
		String path = getAbsolutePath(osuri);
		File file =new File(path);
		if (file.exists())
			return file;
		return null;
	}


	/**
	 * 
	 * 
	 */
	@Override
	public FSInputStream getFSInputStream(String url, long size) throws IOException {
		if (!isStarted())
			throw new IOException("File Server is not started.");
		FileRSupport fs = createFileRSupport(url, size);
		FSInputStream reader;
		 if (isEncrypted())
			 reader = new EncryptedFSInputStream(fs, this);
		 else
			 reader = new SimpleFSInputStream(fs);
		 return reader;
     }
	
	@Override
	public void addToCache(String url, File file) throws IOException {
		String osuri = url.replace(separator_to_replace, File.separator);
		if (!isStarted())
			throw new IOException("File Server is not started.");
		cache.add(osuri, file);			
	}
 
	@Override
	public void close() {
		this.started=false;
		if (isCacheEnabled())
			cache.close();
		
		// db.close();
		
		dbdirs.remove(getRoot());
		dbdirs.remove(getCacheDirectory());
	}
	
	@Override
	public void remove(String uri) throws IOException {
		String osuri = uri.replace(separator_to_replace, File.separator);
		if (!isStarted())
			throw new IOException("File Server is not started.");
		File file = new File(osuri);
		if (file.exists()) {
			long size = FileUtils.sizeOf(file);
			logger.debug("remove " + uri);
			KbeeFileUtils.deleteQuietly(new File(osuri));
			substractSize(size);
		}
	}
	
	@Override
	public SecretKeySpec getKey() {
		return this.secretKey;
	}

	/**
	 * 
	 * 
	 */
	@Override
	public boolean isEncrypted() {
		 return this.encrypted;
	 }

	/**
	 * 
	 */
	@Override
	public boolean isCacheEnabled() {
		return this.cacheenabled;
	}

	
	/**
	 * 
	 */

	@Override
	public String getName() {
		return 	this.name;
	}
	
	
	/**
	 * 
	 * 
	 */

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("name: " + name );
		// str.append("\nencrypted: " + (isEncrypted()?"YES":"NO"));
		str.append("\ncache enabled: " + (isCacheEnabled()?"YES":"NO"));
		str.append("\ndir generator strategy: " + subdirgenenartor!=null ?subdirgenenartor.getName():"null");
		str.append("\nroot dir: " + this.root);
		str.append("\ncache dir: " + this.cacheDirectory);
		return str.toString();
	}

 	/**
	 * 
	 * 
	 */
 	@Override
	public long getSize() {
		return this.size;
	}

	/**
	 *
	 * 
	 */
	public synchronized void calculateMetadata() {

		boolean old=this.started; 
		this.started=false;
 		   
		long to=System.currentTimeMillis();
		this.total = Long.valueOf(0); 
		this.size  = Long.valueOf(0); 
	
		
		if (this.root!=null) {
			count(new File(getRoot()));
	
			this.total_fast = Long.valueOf(0);
			this.size_fast  = Long.valueOf(0);
	 
			this.total_slow = Long.valueOf(0);
			this.size_slow  = Long.valueOf(0);
			
	 		countFast(new File(getRoot() + File.separator + FileServerV1.FAST));
			countSlow(new File(getRoot() + File.separator + FileServerV1.SLOW));

		
			long t1=System.currentTimeMillis();
	
			logger.info("Done. " + String.valueOf((double) (t1-to)/1000.0) +" secs.");
			
	  		logger.info("Total Fast: "+this.total_fast +" files"); 
			logger.info("Size Fast: "+ NumberFormatter.formatFileSize(this.size_fast));
			data.put("totalFast", this.total_fast);
			data.put("sizeFast", this.size_fast);
		
	 		logger.info("Total Slow: "+this.total_slow +" files"); 
			logger.info("Size Slow: "+ NumberFormatter.formatFileSize(this.size_slow));
			data.put("totalSlow", this.total_slow);
			data.put("sizeSlow", this.size_slow);
	
			logger.info("Size: "+ NumberFormatter.formatFileSize(this.size));
			data.put("total", this.total);
			data.put("size", this.size);
		
			//db.commit();
			this.started=old;
		}
	}
	
	/**
	 * 
	 */
	public void cleanDataDirectory() {
		boolean old = this.started;
		this.started=false;		
		String FSDataDirectory  = root + File.separator + "fsdata";
		try {
			logger.info("Deleting "+FSDataDirectory);
			KbeeFileUtils.forceDelete(new File(FSDataDirectory));
		} catch (IOException e) {
			logger.error("Error Deleting " + FSDataDirectory);
			logger.error(e.getMessage());
			
		}
		this.started=old;
	}

	/**
	 * 
	 * @param byteswritten
	 * 
	 */
	public synchronized void addSize(long byteswritten) {
 		addSize(byteswritten, FileServerV1.FAST);
  	
 		 
 	}

	@Override
	public long getTotalFiles() {
		try {
		return this.total.longValue();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " total error");
			this.total = Long.valueOf(0); 
			return this.total.longValue();
		}
	}

	
	/**
	 * 
 	 * @param byteswritten
 	 * 
	 */
	public synchronized void addSize(long byteswritten, String repo_type) {
	
		try {
		
			this.size += byteswritten;

			if ( byteswritten>0)
				this.total++;
			else
				this.total--;
			
			data.put("total", this.total);
			data.put("size", this.size);
	
			if (repo_type.equals(FileServerV1.FAST)) {
	
				this.size_fast += byteswritten;
	
				if ( byteswritten>0)
					this.total_fast++;
				else
					this.total_fast--;
				
				data.put("totalFast", this.total_fast);
				data.put("sizeFast", this.size_fast);
				
			} else {
	
				this.size_slow += byteswritten;
	
				if ( byteswritten>0)
					this.total_slow++;
				else
					this.total_slow--;
				
				data.put("totalSlow", this.total_slow);
				data.put("sizeSlow", this.size_slow);
	 		}
	  		
			
			// db.commit();
		
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | " + e.getMessage() + " | Adding File Size to Counter");
		}
	}

	
	@Override
	public long getTotalFilesFast() {
		try {
			return this.total_fast.longValue();
		} catch (Exception e) {
			this.total_fast = Long.valueOf(0); 
			return this.total_fast.longValue();
		}
	}

	@Override
	public long getTotalFilesSlow() {
		try {
			return this.total_slow.longValue();
		} catch (Exception e) {
			
			this.total_slow = Long.valueOf(0); 
			return this.total_slow.longValue();
	}

	}
  	
	@Override
	public String getRootDirectory() {
		return new String(getRoot());
	}

	@Override
	public void pause() {
		this.started=false;
	}

	@Override
	public void resume() {
		this.started=true;
	}

	protected  void substractSize(long byteswritten) {
		 addSize((int) (-1 * byteswritten));
	}

	protected String getCacheDirectory() {
 		if ( this.cacheDirectory !=null)
 			return this.cacheDirectory;
 		this.cacheDirectory=getWorkDir() + File.separator + "cache";
 		return this.cacheDirectory; 
	}
 	
	protected void createDirsIfNotExist(FileWSupport fs) throws IOException {
		FSUtils.createDirsIfNotExist(getRoot()+File.separator+fs.url);
	}

	protected void add(FileSupport fs, File file) throws IOException {
		String abspath = getRoot() + File.separator + fs.url;
		File destFile = new File(abspath);
		FileUtils.copyFile(file, destFile);
	}
	
	/**
	 * for Writer
	 * 
	 * @param filename
	 * @param contentOwnerId
	 * @return
	 * @throws IOException
	 * 
	 */
	private FileWSupport createFileWSupport(String srcfilename, String id, String domain, String repo_type, boolean bencrypted) throws IOException {
	
		FileWSupport fs = new FileWSupport();
		String relativePlainURL = getUrl(srcfilename, id, domain, repo_type);
		
		if (bencrypted) 
			fs.url = getUrl(FSUtils.getEncrytedFileName(srcfilename), id, domain, repo_type);
		else  
			fs.url = relativePlainURL;
		
		String abspath = getAbsolutePath(fs.url);
		
		File destFile = new File(abspath);
		fs.destfile = destFile;
		
		if (isCacheEnabled()) 
			fs.cachefile = cache.getFileToWrite(relativePlainURL);
		else
			fs.cachefile = null;
		return fs;
	}
	

	
	private boolean isWindows() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows"))
			return true;
		return false;
	}

	private String getRoot() {

		if (this.root!=null)
			return this.root;

		synchronized (this) {
			this.root="work";
			
			if (this.root!=null) {
				for (String dir: dbdirs) {
					if (dir.startsWith(this.root) || this.root.startsWith(dir))
						throw new KbeeRuntimeException("Root dir already in use " + dir);
				}
			}

			File rootdir = new File(this.root);
			
			if (!(rootdir.exists() && rootdir.isDirectory())) { 
				startupLogger.info("Creating root dir: " + this.root);
				try {
					KbeeFileUtils.forceMkdir(rootdir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
			dbdirs.add(this.root);
			
			File rootfastsubdir = new File(this.root+File.separator + getFastSubdir());
			if (!(rootfastsubdir.exists() && rootfastsubdir.isDirectory())) { 
				startupLogger.info("Creating fast subdir: " + this.root+File.separator + getFastSubdir() );
				try {
					KbeeFileUtils.forceMkdir(rootfastsubdir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
			
			File rootslowsubdir = new File(this.root+File.separator + this.getSlowSubdir());
			if (!(rootfastsubdir.exists() && rootslowsubdir.isDirectory())) { 
				startupLogger.info("Creating slow subdir: " + this.root+File.separator + getSlowSubdir() );
				try {
					KbeeFileUtils.forceMkdir(rootslowsubdir);
				} catch (IOException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
		
		return this.root;
	}

	
	private String getFastSubdir() {
		return FAST;
	}

	private String getSlowSubdir() {
		return SLOW;
	}
	
	
 	/** 
	 * 
	 */
	private FileRSupport createFileRSupport(String uri, long size) throws IOException {
		String url=uri.replace(separator_to_replace, File.separator);
		File file = new File(getRoot() + File.separator + url);
		if (!file.exists()) 
			throw new IOException("File System. File  not in FileServer: " + uri);
			FileRSupport fs = new FileRSupport();
		if (isCacheEnabled()) {
			fs.cachefile = cache.get(url);
			fs.cachetowrite = cache.getFileToWrite(url);
 		}
		else
			fs.cachefile = null;
		fs.serverfile=file;
		fs.url=url;
		fs.filesize=size;
		
		return fs;
		
	}

	private boolean isStarted() {
		return this.started;
	}
	
	
 	/**
	 * 
	 * @throws IOException
	 * 
	 */
 	private void start() throws IOException {
		
		if (isWindows()) 
			separator_to_replace="/";
		else
			separator_to_replace="\\";
			
		if (this.root!=null) {
			for (String dir: dbdirs) {
				
				if (dir.startsWith(this.root) || this.root.startsWith(dir))
					throw new IOException("Root dir already in use " + dir) ;
			}
		}
		
		startupLogger.info("Starting FileServer");
		startupLogger.info("Name: " + this.getName());
		startupLogger.info("Encrypted: " + (isEncrypted()?"YES":"NO"));
		startupLogger.info("Cache: " + (isCacheEnabled()?"YES":"NO"));
		startupLogger.info( subdirgenenartor!=null?subdirgenenartor.getName():"");
		
		startupLogger.info("OS: " + (isWindows()?"Windows":"Linux"));
		startupLogger.info("Directory: " + this.root);
		
		
		this.data  = new HashMap<String, Long>();
		
		
		// cleanUpWorkdirectory();
		
		// startMapDB();
		
 		if (isCacheEnabled() && cache==null) {
			cache = FSCache.getInstance();
		}
		
 		if (isEncrypted()) {
 			
	  		// --- Secret Key  ------------------------------------------------------------------
			//
			KeyGenerator kgen;
			try {
					kgen = KeyGenerator.getInstance("AES");
			    	kgen.init(128);  											// or 192 or 256
			    	SecretKey skey = kgen.generateKey();
			    	byte[] raw = skey.getEncoded();
			    	secretKey = new SecretKeySpec(raw, "AES");
					// --------------------------------------------------------------------------
			    	//
					// setKey(defaultKey.getBytes(Charset.forName("UTF-8")));
			    	//
					// --------------------------------------------------------------------------
			} catch (NoSuchAlgorithmException e) {
				throw new IOException(e.getMessage());
			}
 		}
 		
 		this.started=true;
	}
 	
 	/**
	 * 
	 */
	public void cleanUpWorkdirectory() {
		
		File dir = new File(getWorkDir()+ File.separator +  "File System");	

		if (!dir.exists())  {
			try {
				KbeeFileUtils.forceMkdir(dir);
			} catch (Exception e) {
				logger.error(e);

			}
			return;
		}
		
		String today 	= workdf.format(LocalDate.now());
		String today_1 	= workdf.format(LocalDate.now().minusDays(1));
		String today_2  = workdf.format(LocalDate.now().minusDays(2));

		if (!dir.isDirectory()) {
			try {
				KbeeFileUtils.deleteQuietly(dir);
			} catch (Exception e) {
				logger.error(e);

			}
			return;
		}

		if (dir.listFiles()==null)
			return;

		logger.debug("Removing all subdirs of " + dir.getName() + "  | but: " + today + " | " + today_1 + " | " + today_2);
		
		for (File fi: dir.listFiles()) {
			if (fi.exists() && fi.isDirectory()) {
				String s = FilenameUtils.getBaseName(fi.getName());
				logger.debug(s);
				if (!s.equals(today) && !s.equals(today_1) && !s.equals(today_2)) {
					logger.info("Cleaning up directory: " + fi.getAbsolutePath());
					KbeeFileUtils.deleteQuietly(fi);
				}
			}
		}
	}

	/** 
	 * @throws IOException
	 */
	private void startMapDB() throws IOException {

		/**
		startupLogger.info("Starting DEPRECATED MapDB");
		String FSDataDirectory  = getRoot() + File.separator + "fsdata";
		
		startupLogger.info("Data: " + FSDataDirectory);

		File fsdatadir = new File(FSDataDirectory);
		
		if (!(fsdatadir.exists() && fsdatadir.isDirectory())) { 
			startupLogger.info("Creating directory " + FSDataDirectory);
			KbeeFileUtils.forceMkdir(fsdatadir);
		}

		try {
		
			db = DBMaker.fileDB(new File(FSDataDirectory+File.separator+"mapdb"))
					.closeOnJvmShutdown()
					.transactionEnable()
					.make();
			
			data = db.hashMap(getName(), Serializer.STRING, Serializer.LONG).createOrOpen();
			
			if (data.containsKey("total"))
				this.total=data.get("total");
			else
				this.total = Long.valueOf(0); 
			
			if (data.containsKey("size"))
				this.size=data.get("size");
			else
				this.size = Long.valueOf(0); 
 			
								
			if (data.containsKey("totalFast"))
				this.total_fast=data.get("totalFast");
			else
				this.total_fast = Long.valueOf(0); 
			
			if (data.containsKey("sizeFast"))
				this.size_fast=data.get("sizeFast");
			else
				this.size_fast = Long.valueOf(0); 
 			

			if (data.containsKey("totalSlow"))
				this.total_slow=data.get("totalSlow");
			else
				this.total_slow=Long.valueOf(0); 
			
			if (data.containsKey("sizeSlow"))
				this.size_slow=data.get("sizeSlow");
			else
				this.size_slow=Long.valueOf(0); 

			startupLogger.info("Size: " + FSUtils.formatFileSize(this.size));
			startupLogger.info("Total: " + this.total);


		} catch(org.mapdb.DBException e) {

			startupLogger.error(e.getMessage());
			startupLogger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			
			if (e.getClass().getName().startsWith("org.mapdb.DBException$WrongFormat")) {
				
				KbeeFileUtils.forceDelete(new File(FSDataDirectory+File.separator+"mapdb"));
				
				db = DBMaker.fileDB(new File(FSDataDirectory+File.separator+"mapdb"))
						.closeOnJvmShutdown()
						.transactionEnable()
						.make();
				
				data = db.hashMap(getName(), Serializer.STRING, Serializer.LONG).createOrOpen();
				
				db.commit();
				
				if (data!=null) 
					calculateMetadata();
			}
			
			
		} catch(Exception e) {
			
			startupLogger.error("---------------------------------------------------------------------------");
			startupLogger.error("DEPRECATED Map DB metrics seem damaged. It may required to recalculate.");
			startupLogger.error("---------------------------------------------------------------------------");
			startupLogger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			
		}
		*/
		
		
		//finally {
			
			if (this.total_fast==null)
				this.total_fast = Long.valueOf(0); 
			
			if (this.size==null)
				this.size=Long.valueOf(0); 
			
			if (this.total==null)
				this.total=Long.valueOf(0); 
				
			if (this.total_fast==null)
				total_fast = Long.valueOf(0); 
			
			if (this.size_fast==null)
				size_fast = Long.valueOf(0); 
		 	
			if (this. total_slow==null)
				 total_slow = Long.valueOf(0); 
		 	
			if (this.size_slow==null)
				 size_slow = Long.valueOf(0); 
			
			if (data==null)
				data = new HashMap<String, Long>();
			
		//}
	}
	
	
  	private SubDirectoryGenerationStrategy getSubDirectoryGenerationStrategy() {
			if (this.subdirgenenartor==null)
				this.subdirgenenartor = new StandardDirectoryStrategy(getRoot());
  		return this.subdirgenenartor;
  	}
	 
	private String getUrl(String filename, String id, String domain, String repo_type) throws IOException {
		return getSubDirectoryGenerationStrategy().generateRelativePath(new SubDirGenerationStrategyContext(filename, id, domain, repo_type));
	}
	
	private String getAbsolutePath(String url) {
		return getRoot() + File.separator + url;
	}

	
	private void count(File dir) {
		File[] list = (dir).listFiles();
			for (int n = 0; n<list.length; n++) {
				 if (list[n].isDirectory()) {
					 count(list[n]);
				 }
				 else {
					 this.size += FileUtils.sizeOf(list[n]);
					 this.total++;
					 
					 if (this.total%101==0)
						 logger.info("Partial total: " + this.total);
				 }
			}
		}


  	private void countFast(File dir) {
		File[] list = (dir).listFiles();
			for (int n = 0; n<list.length; n++) {
				 if (list[n].isDirectory()) {
					 countFast(list[n]);
				 }
				 else {
					 this.size_fast += FileUtils.sizeOf(list[n]);
					 this.total_fast++;
					 
					 if (this.total_fast%101==0)
						 logger.info("Partial total fast: " + this.total_fast);
				 }
			}
		}


	private void countSlow(File dir) {
		File[] list = (dir).listFiles();
			for (int n = 0; n<list.length; n++) {
				 if (list[n].isDirectory()) {
					 countSlow(list[n]);
				 }
				 else {
					 this.size_slow += FileUtils.sizeOf(list[n]);
					 this.total_slow++;
					 
					 if (this.total_slow%101==0)
						 logger.info("Partial total slow: " + this.total_slow);
				 }
			}
		}

 	
	@Override
	public String getRelativeURLForFile(String srcfilename, String srcid, String domain) throws IOException {
		return getRelativeURLForFile(srcfilename, srcid, domain, FileServerV1.FAST);
	}

	@Override
	public FSOutputStream getFSOutputStream(String srcfilename, String objectname, String bucket) throws IOException {
		return getFSOutputStream(srcfilename, objectname, bucket, FileServerV1.FAST);
	}
	
	public String getUrl(String srcfilename, String objectname, String bucket) throws IOException {
		return getUrl(srcfilename, objectname, bucket, FileServerV1.FAST);
	}

	@Override
	public String ping() {
		return "ok";
	}

	public String getLocalCacheDirectory(String uid) throws FileServerException {
		  String dirname = getWorkDir()+ File.separator +  "File System" + File.separator +  workdf.format(LocalDateTime.now()) + File.separator + uid;
		  File dir = new File(dirname);
		  if (!dir.exists() || !dir.isDirectory())  {
			  logger.info("Creating directory " + dirname);
			  try {
				  KbeeFileUtils.forceMkdir(dir);
			  } catch (IOException e) {
					logger.error(e);
					throw new FileServerException("Can not create Work Directory " + dirname);
			  }
		  }
		  return dirname;
	}

	
	
	@Override
	public Integer getShard(String bucketName, String objectName) {
		return shard;
	}

	
	private SystemMetricsService getSystemMetricsService() {
		if (this.mtrics==null)
			this.mtrics = ServiceLocator.getService(SystemMetricsService.class);
		return this.mtrics;
	}

	@Override
	public String getFSId(Integer shard) {
		return "File System";
	}

	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException {
			putObject(bucketName, objectName, filename, stream, size, contentType);
	}

	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {
		putObject(bucketName, objectName, filename, stream, contentType);
	}

	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream)	throws FileServerException {
		putObject(bucketName, objectName, filename, stream);
	}

	@Override
	public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException {
		return getObject(bucketName, objectName);
	}

	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName) throws FileServerException {
		return presignedGetObject(bucketName, objectName);
	}

	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds) throws FileServerException {
		return presignedGetObject(bucketName, objectName, expires_seconds);
	}

	@Override
	public File getDownloadedFile(String fsid, String bn, String on, String fileName) throws FileServerException {
		return getDownloadedFile(bn, on, fileName);
	}

	@Override
	public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException {
		removeObject(fsid, bucketName, objectName);
	}

	@Override
	public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException {
		return isObject(bucketName, objectName);
	}

	@Override
	public Integer getShard(String fsid) {
		return this.shard;
	}


	@Override
	public List<String> listBuckets() throws FileServerException {
		throw new FileServerException("Not Implemented in FileServer V1. listBuckets()");
	}
	

	@Override
	public Map<String, String> getInfo() {
		return new HashMap<String, String>();
	}

}
	
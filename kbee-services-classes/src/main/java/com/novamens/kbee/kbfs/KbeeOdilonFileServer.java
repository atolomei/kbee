package com.novamens.kbee.kbfs;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import io.odilon.client.ODClient;
import io.odilon.client.OdilonClient;
import io.odilon.client.error.ODClientException;
import io.odilon.model.Bucket;
import io.odilon.model.ObjectMetadata;
import io.odilon.model.SystemInfo;
import io.odilon.model.list.ResultSet;
import io.odilon.util.FileNameNormalizer;
import kbee.util.PropertiesFactory;

public class KbeeOdilonFileServer implements FileServerOdilon {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeOdilonFileServer.class.getName());
	
	static final int DEFAULT_EXPIRING_TIME = 60 * 60 * 24 * 7; // 7 days
	
	static private Logger startupLogger = LogManager.getLogger("StartupLogger");

	static final double MB = 1000000.0;
	
 	private String endpoint;
	private String accessKey;
	private String secretKey;
	
	private OdilonClient odilonClient;

	private LocalFileServerCache fslocalcache;
	private SystemMetricsService metrics;
	
	private String assigned_shard_str;
	
	private Integer assigned_shard;
	private double probability;
	private String fsid = "";

	private boolean isReadOnly = false;
	private boolean minor =  false;
	
	
	public KbeeOdilonFileServer() throws FileServerException {
		this(null, null, null, null);
	}

	public KbeeOdilonFileServer(String endPoint, String accessKey, String secretKey, final String fsid) throws FileServerException {
		this(endPoint, accessKey, secretKey, fsid, 1, 1.0);
	}

	/**
	 * @throws FileServerException 
	 */
	public KbeeOdilonFileServer(String endPoint, String accessKey, String secretKey, final String fsid, final Integer shard) throws FileServerException {
		this(endPoint, accessKey, secretKey, fsid, shard, 1.0);
	}

	/**
	 * @throws FileServerException 
	 */
	public KbeeOdilonFileServer(String endPoint, String accessKey, String secretKey, final String fsid, final Integer shard, final double probability) throws FileServerException {
		
	 	String ODILON_ACCESS_KEY_VARIABLE = System.getenv().get("ODILON_ACCESS_KEY");
	 	String ODILON_SECRET_KEY_VARIABLE = System.getenv().get("ODILON_SECRET_KEY");
		
	 	String URL				= PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.endpoint",  "http://localhost:9234").trim();
	 	String ACCESSKEY		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.accesskey", ODILON_ACCESS_KEY_VARIABLE!=null?ODILON_ACCESS_KEY_VARIABLE:"odilon").trim();
	 	String SECRETKEY	 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.secretkey", ODILON_SECRET_KEY_VARIABLE!=null?ODILON_SECRET_KEY_VARIABLE:"odilon").trim();
	 	String FSID		    	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.fsid", "fs-dev1").trim();
	 	
		 	if (endPoint==null) 
		 		endPoint=URL;
		 	
	 		if (accessKey==null)
				accessKey=ACCESSKEY;
	 		
	 		if (secretKey==null)
	 			secretKey=SECRETKEY;
	
		if (endPoint==null || accessKey==null || secretKey==null)
			throw new IllegalArgumentException("Parameters can not be null.");
	
		try {									
	    	
	    	this.endpoint  = endPoint.trim();
	    	this.accessKey = accessKey.trim();
	    	this.secretKey = secretKey.trim();
	    	
	    	if (fsid!=null)
	    		this.fsid=fsid.toLowerCase().trim();
	    	else
	    		this.fsid=FSID;
	    		
	    	setShard(shard);
	    	
	    	this.probability=probability;
	    	
	    	startupLogger.info("Odilon_"+String.valueOf(shard)+". Starting " 	+ this.getClass().getSimpleName());
	    	startupLogger.info("Odilon_"+String.valueOf(shard)+". Endpoint " 	+ this.endpoint);
	    	startupLogger.info("Odilon_"+String.valueOf(shard)+". Access Key " 	+ this.accessKey);
	    	startupLogger.info("Odilon_"+String.valueOf(shard)+". Secret Key " 	+ this.secretKey);
	    	
	    	String s_port = "9200";
	    	String arrep[] = this.endpoint.split(":");
	    	
	    	String protocol = arrep[0].trim();
	    	String url = arrep[1].trim();
	    	
	    	if(arrep.length>2) {
	    		s_port = arrep[arrep.length-1];
	    	}
	    	else {
	    		s_port= protocol.equals("https")?"443":"80";
	    	}
	    
	    	Integer port = Integer.valueOf(9200);
	    	
	    	try {
	    		port = Integer.valueOf(s_port);
	    	} catch (Exception e) {
	    		logger.error(e);
	    		port = Integer.valueOf(9200);
	    	}
	    	
	        this.odilonClient = new ODClient(protocol+":"+url, port, this.accessKey, this.secretKey);
	    	
	    	startupLogger.info("Odilon_"+String.valueOf(shard)+". Startup  Successful");
	    	
		} catch (Exception e) {
	
			logger.error(e);
			
			startupLogger.error(" {} | {} | {} ", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			startupLogger.debug(e.getClass().getName(), e);
			
			throw new FileServerException(e);
		}
	}

	@Override
	public String getDisplayName() {
		return this.getClass().getSimpleName() +
 				( (getShard()!=null) ? ( "_"+ String.valueOf(getShard())) : "");
	}

	public KBFSStorageType getKBFSStorageType() {
		return KBFSStorageType.Odilon;
	}

	@Override
	public List<String> listBuckets() throws FileServerException {
		
		
		try {
			List<Bucket> list = this.odilonClient.listBuckets();
			List<String> ret = new ArrayList<String>();
			list.forEach(i -> ret.add(i.getName()));
			return ret;
			
		} catch (ODClientException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
		
	}


	@Override
	public String normalize(String name) {
		return FileNameNormalizer.normalize(name);
	}
	
	
	/**
     * Uses Minio Interface
     * 
     * @param bucketName

     * @throws FileServerException
     */
    public Iterator<io.odilon.model.list.Item<ObjectMetadata>> listObjects(String bucketName) throws FileServerException {
        
        if (bucketName==null) {
            throw new IllegalArgumentException("bucketName is null");
        }
            
        boolean found;
            
        try {

                found = odilonClient.existsBucket(bucketName);
                
                if (found) {
                    ResultSet<io.odilon.model.list.Item<ObjectMetadata>> myObjects = odilonClient.listObjects(bucketName);
                    return myObjects;

              } else
                  throw new FileServerException(bucketName + " b:" + bucketName + " does not exist");

            } catch (Exception e) {
                    logger.error(e, "b:" + (bucketName!=null?bucketName:""));
                    throw new FileServerException(e);
            }
    }
	
	
	
	/**
	 * 
	 * 
	 */
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream,	long size, String contentType) throws FileServerException {
		
		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"") + (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
		try {
			
			boolean found = this.odilonClient.existsBucket(bucketName);
			
			if (!found) 
			  this.odilonClient.createBucket(bucketName);

			Optional<Long> ol;
			
			if (size>0)
				ol=Optional.of(Long.valueOf(size));
			else {
				if (stream instanceof LengthAwareInputStream) {
					ol = Optional.of(((LengthAwareInputStream)stream).getLength());
				}
				else {
					ol=Optional.empty();
				}
			}	
				
			this.odilonClient.putObjectStream(
					bucketName, 
					objectName, 
					stream, 
					Optional.of(filename),
					ol);
			
			getSystemMetricsService().getMeterOdilonPutObject(bucketName).mark();
			getSystemMetricsService().getMeterOdilonShardPutObject(getShardStr()).mark();
			getSystemMetricsService().getMeterOdilonPutObject().mark();

			
		}
		
		catch (Exception e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}

	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream,	String contentType) throws FileServerException {
			putObject(shard, bucketName, objectName, filename, stream, 0, contentType);

	}

	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
			putObject(shard, bucketName, objectName, filename, stream, 0, null);

	}

	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size,	String contentType) throws FileServerException {
		putObject( getShard(), bucketName, objectName, filename, stream, size, contentType);
	}

	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {
		putObject(getShard(), bucketName, objectName, filename, stream, 0, contentType);
	}

	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
		putObject( getShard(), bucketName, objectName, filename, stream, 0, null);
	}

	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid -> " + fsid);
		return presignedGetObject(bucketName, objectName);
	}

	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid -> " + fsid);
		return presignedGetObject(bucketName, objectName, expires_seconds);
	}

	@Override
	public String presignedGetObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));
		return presignedGetObject(bucketName, objectName);
	}

	@Override
	public String presignedGetObject(Integer shard, String bucketName, String objectName, int expires_seconds) throws FileServerException {
		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));
		return presignedGetObject(bucketName, objectName, expires_seconds);
	}

	@Override
	public String presignedGetObject(String bucketName, String objectName) throws FileServerException {
		return presignedGetObject(bucketName, objectName, DEFAULT_EXPIRING_TIME);
	}

	@Override
	public String presignedGetObject(String bucketName, String objectName, int expires_seconds)	throws FileServerException {
		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			
			String ret = this.odilonClient.getPresignedObjectUrl(bucketName, objectName, Optional.ofNullable(expires_seconds));
			
			getSystemMetricsService().getMeterOdilonGetObject(bucketName).mark();
			getSystemMetricsService().getMeterOdilonShardGetObject(getShardStr()).mark();
			getSystemMetricsService().getMeterOdilonGetObject().mark();

			return ret;
			
			} catch (ODClientException e) {
				logger.error(e);
				throw new FileServerException(e);
			}
			catch (Exception e) {
				logger.error(e);
				throw new FileServerException(e);
			}
	}

	@Override
	public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		return getObject(bucketName, objectName);
	}

	@Override
	public InputStream getObject(String bucketName, String objectName) throws FileServerException {

		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {

			InputStream stream = this.odilonClient.getObject(bucketName, objectName);
			
			getSystemMetricsService().getMeterOdilonGetObject(bucketName).mark();
			getSystemMetricsService().getMeterOdilonShardGetObject(getShardStr()).mark();
			getSystemMetricsService().getMeterOdilonGetObject().mark();
			
			return stream;
			
		} catch (ODClientException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}


	@Override
	public void removeObject(String bucketName, String objectName) throws FileServerException {

		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
		try {

			this.odilonClient.deleteObject(bucketName,  objectName);
			
			getSystemMetricsService().getMeterOdilonPutObject(bucketName).mark();
			getSystemMetricsService().getMeterOdilonShardPutObject(getShardStr()).mark();
			getSystemMetricsService().getMeterOdilonPutObject().mark();

		} catch (ODClientException e) {
			logger.error(e);
			throw new FileServerException(e);
		}

	}

	@Override
	public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException {
		
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		removeObject( bucketName,  objectName);
	}

	@Override
	public String ping() {
		return this.odilonClient.ping();
	}

	@Override
	public boolean isObject(String bucketName, String objectName) throws FileServerException {
		
		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			return this.odilonClient.existsObject(bucketName,  objectName);
				
		} catch (IOException e) {
			logger.error(e);
			throw new FileServerException(e);
		} catch (ODClientException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}

	@Override
	public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		return isObject(bucketName,  objectName);
	}

	@Override
	public String getFSId(Integer shard) {
		return this.fsid;
	}

	@Override
	public Integer getShard() {
		return this.assigned_shard;
	}
	
	@Override
	public void setProbability(double p) {
		this.probability=p;
	}
	
	@Override
	public double getProbability() {
		return this.probability;
	}

	
	public void setReadOnly(boolean b) {
		this.isReadOnly=b;
	}
	
	@Override
	public boolean isReadOnly() {
		return isReadOnly;
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
	 */
	public String reconnect(String url, String accessKey, String secretKey) throws FileServerException {
		
		
		synchronized (this.odilonClient) {
			try {
				this.endpoint = url;
				this.accessKey = accessKey;
				this.secretKey = secretKey;
				this.odilonClient = new ODClient(url, 8200, accessKey, secretKey);
				logger.info("Connection Successful");
				
			} catch (Exception e) {

				logger.error(e);
				startupLogger.debug(e.getClass().getName(), e);
		
				throw new FileServerException(e);
			}
		}
		
		return ping();
	}
	


	@Override
	public String getEndPoint() {
		return this.endpoint;
	}

	@Override
	public String getAccessKey() {
		return this.accessKey;
	}

	@Override
	public String getSecretKey() {
		return this.secretKey;
	}

	@Override
	public String getFSId() {
		return fsid;
	}

	@Override
	public Integer getShard(String shardid) {
		return assigned_shard;
	}

	@Override
	public String ping(Integer shard) {
		return ping();
	}

	@Override
	public String getEndPoint(Integer shard) {
		return endpoint;
	}

	@Override
	public String getAccessKey(Integer shard) {
		return accessKey;
	}

	@Override
	public String getSecretKey(Integer shard) {
		return secretKey;
	}


	@Override
	public InputStream getObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		
		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));
		
		return getObject(bucketName, objectName);
	}

	
	
	
	@Override
	public File getDownloadedFile(Integer shard, String bucketName, String objectName, String fileName)
			throws FileServerException {

		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));

		return getDownloadedFile(bucketName, objectName,  fileName);
	}
	
	@Override
	public File getDownloadedFile(String bucketName, String objectName, String fileName) throws FileServerException {

	 	if (bucketName==null || objectName==null ||objectName.equals("null")) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
	 	InputStream stream = null;
	 	
		try {

			boolean exists;
			
			try {

				exists = this.odilonClient.existsObject(bucketName, objectName);

				if (!exists) {
					String cause= "b:" + bucketName + " o:" + objectName +  " -> does not exist";
					throw new FileServerException(cause);
				}
				
			} catch (ODClientException e) {
				logger.error(e);
				throw new FileServerException(e);
			}
			
			 if (getLocalFileServerCache().containsKey(bucketName, objectName)) {
				  	File file = getLocalFileServerCache().get(bucketName, objectName);
				  	if (file!=null) {
				  		logger.debug("Cache hit: " + fileName);
				  		getSystemMetricsService().getCounterOdilonCacheHit().inc();
				  		return file;
				  	}
			  }
			  
			  logger.debug("Cache miss: " + fileName);
			  logger.debug("Cache Status: " + String.valueOf(this.getLocalFileServerCache().getTotalItems()) + " files. " + (String.format("%8.2f", Double.valueOf(this.getLocalFileServerCache().getTotalDisk()).doubleValue()/MB)).trim()+" MB");
			  
			  try {
				  stream = this.odilonClient.getObject(bucketName, objectName);
			  } catch (ODClientException e) {
				  logger.error(e);
				  throw new FileServerException(e);  
			  }
			  
			  getSystemMetricsService().getMeterOdilonGetObject().mark();
			  getSystemMetricsService().getMeterOdilonGetObject(bucketName).mark();
			  getSystemMetricsService().getMeterOdilonShardGetObject(getShardStr()).mark();
			  getSystemMetricsService().getCounterOdilonCacheMiss().inc();

			  getLocalFileServerCache().put(bucketName, objectName, stream, fileName);
			  
			  return getLocalFileServerCache().get(bucketName, objectName);
		
		  } catch (Exception e) {
			  	String key = (bucketName!=null?bucketName:"null")+" "+(objectName!=null?objectName:"null");
			  	logger.error(e, " key: " +  key, e.getMessage());
				throw new FileServerException(e);
		  }
		finally {
			if (stream!=null) {
				try {
					stream.close();
				} catch (IOException e) {
					logger.warn(e);
				}
				
			}
		}
	}

	@Override
	public void removeObject(Integer shard, String bucketName, String objectName) throws FileServerException {

		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));

		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			
			this.odilonClient.deleteObject(bucketName, objectName);
			
		} catch (ODClientException e) {
			logger.error(e);
			throw new FileServerException(e);
		}
		
	}

	@Override
	public boolean isObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		
		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));

		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			
			return this.odilonClient.existsObject(bucketName, objectName);
			
		} catch (ODClientException | IOException e) {
			throw new FileServerException(e);
		}
	}

	@Override
	public String reconnect(Integer shard, String url, String accessKey, String secretKey) throws FileServerException {
		return reconnect(url, accessKey, secretKey);
	}

	public void setShard(Integer shard) {
		this.assigned_shard=shard;
		this.assigned_shard_str=new String(this.assigned_shard.toString());
	}
	
	
	private String getShardStr() {
		return this.assigned_shard_str;
	}

	
	
	@Override
	public Integer getShard(String bucketName, String objectName) {
		return assigned_shard;
	}

	
	
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String fileName)	throws FileServerException {
	
		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
		if (!getShard().equals(shard))
			throw new FileServerException("Invalid shard -> " + String.valueOf(shard));

		putObject(bucketName, objectName, fileName);
		
	}

	@Override
	public void putObject(String bucketName, String objectName, String filePath) throws FileServerException {

		if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		String contentType = null;
		FileInputStream fis = null;
		long size = 0;
		 
		 try {
			 String name = new File(filePath).getName();
			 size = new File(filePath).length();
			 contentType = Files.probeContentType( (new File(filePath)).toPath());
			 fis = new FileInputStream(filePath);
			 
			 putObject(getShard(), bucketName, objectName, name, new BufferedInputStream(fis), size, contentType);
			 
		 } catch (IOException e) {
				logger.error(e);
		 		throw new FileServerException(e);
		 }
	}

	
	@Override
	public File getDownloadedFile(String fsid, String bn, String on, String fileName) throws FileServerException {
		
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		
		if (bn==null || on==null) {
	 		String cause=(bn==null?" bucketName is null ":"")+ (on==null?" objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		return getDownloadedFile(bn, on, fileName);
		
	}


	@Override
	public Map<String, String> getInfo() {
		
		Map<String,String> map =  new HashMap<String, String>();
		
		 try {
			 SystemInfo info = this.odilonClient.systemInfo();
			 return info.getColloquial();
		 
		 } catch (Exception e) {
				logger.error(e);
				map.put("Info", e.getClass().getName() +" - " + e.getMessage());
		 }
	 
		return map;
	}
	
	
	private LocalFileServerCache getLocalFileServerCache() {
		if (fslocalcache==null)
			fslocalcache = ServiceLocator.getService(LocalFileServerCache.class);
		return fslocalcache;
	}

	
	private SystemMetricsService getSystemMetricsService() {
		if (this.metrics==null)
			this.metrics = ServiceLocator.getService(SystemMetricsService.class);
		return this.metrics;
	}


	/**
	 * 
	private String getContentType(String src) {

		if (FSUtils.isPdf(src))
			return "application/pdf";
		
		if (FSUtils.isImage(src))  {
			String str = FilenameUtils.getExtension(src);
			if (str!=null && (str.toLowerCase().equals("jpg") ||  str.toLowerCase().equals("jpeg")))
				return "image/jpeg"; 
			return "image/"+str;
		}
		if (FSUtils.isVideo(src)) {
			return "video/"+FilenameUtils.getExtension(src);
		}
		
		if (FSUtils.isAudio(src))
			return "audio/"+FilenameUtils.getExtension(src);
		
		return "application/octet-stream";
	}
	 */
}

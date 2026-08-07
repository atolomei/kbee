package com.novamens.kbee.kbfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;

import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidExpiresRangeException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;

/**
 * 
 * <p>
 *   put(File)
 *   put(OutputStream)
 *   
 *   InpuStream get(id)
 *   File get(id)
 *   
 *   Minio: 1 bucket x dominio
 *   
 *   ServerFast
 *   ServerSlow
 *   
 *   1 bucket x dominio
 *   dominio / anio / mes / XXX
 *   
 *
 *   MinioClient minioClient = new MinioClient("https://play.minio.io:9000", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
 *
 *	Endpoint	URL to object storage service.
 * 	Access Key	Access key is like user ID that uniquely identifies your account.
 *	Secret Key	Secret key is the password to your account.
 *
 *   putObject
 *   getObject
 *   
 *   PUT
 *   GET
 *</p>
 */
			
public class KbeeMinioFileServer implements FileServerMinio {
																
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeMinioFileServer.class.getName());
	
 	static private Logger startupLogger = LogManager.getLogger("StartupLogger");

	static final double MB = 1000000.0;
	static private final int MAX_FNAME_LENGTH = 440;

 	static final int DEFAULT_EXPIRING_TIME = 60 * 60 * 24 * 7; // 7 days
 	
 	static final int BUFFER_SIZE = 8192;
 	
	private String endpoint;
	private String accessKey;
	private String secretKey;
	
	private MinioClient minioClient;

	private LocalFileServerCache fslocalcache;
	private SystemMetricsService metrics;
	
	private String assigned_shard_str;
	private Integer assigned_shard;
	private double probability;
	private String fsid = "";

	private boolean isReadOnly = false;
	private boolean minor =  false;
	
	
	public KbeeMinioFileServer() throws FileServerException {
			this(null, null, null, null);
	}
	
	public KbeeMinioFileServer(String endPoint, String accessKey, String secretKey, final String fsid) throws FileServerException {
			this(endPoint, accessKey, secretKey, fsid, 1, 1.0);
	}
	
	/**
	 * @throws FileServerException 
	 */
	public KbeeMinioFileServer(String endPoint, String accessKey, String secretKey, final String fsid, final Integer shard) throws FileServerException {
		this(endPoint, accessKey, secretKey, fsid, shard, 1.0);
	}
	
	/**
	 * @throws FileServerException 
	 */
	public KbeeMinioFileServer(String endPoint, String accessKey, String secretKey, final String fsid, final Integer shard, final double probability) throws FileServerException {
			
	 	String MINIO_ACCESS_KEY_VARIABLE = System.getenv().get("MINIO_ACCESS_KEY");
	 	String MINIO_SECRET_KEY_VARIABLE = System.getenv().get("MINIO_SECRET_KEY");
		
	 	String URL				= PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.endpoint",  "http://localhost:9000").trim();
	 	String ACCESSKEY		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.accesskey", MINIO_ACCESS_KEY_VARIABLE!=null?MINIO_ACCESS_KEY_VARIABLE:"").trim();
	 	String SECRETKEY	 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.secretkey", MINIO_SECRET_KEY_VARIABLE!=null?MINIO_SECRET_KEY_VARIABLE:"").trim();
	 	String FSID		    	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.fsid", "fs-dev1").trim();
	 	
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
	    		
	    	this.assigned_shard=shard;
	    	this.assigned_shard_str=new String(this.assigned_shard.toString());
	    	
	    	this.probability=probability;
	    	
	    	startupLogger.info("Minio_"+String.valueOf(shard)+". Starting " 	+ this.getClass().getName());
	    	startupLogger.info("Minio_"+String.valueOf(shard)+". Endpoint " 	+ this.endpoint);
	    	startupLogger.info("Minio_"+String.valueOf(shard)+". Access Key " 	+ this.accessKey);
	    	startupLogger.info("Minio_"+String.valueOf(shard)+". Secret Key " 	+ this.secretKey);
	    	
	    	this.minioClient = new MinioClient(this.endpoint, this.accessKey, this.secretKey);
	    	
	    	startupLogger.info("Minio_"+String.valueOf(shard)+". Startup  Successful");
	    	
		} catch (InvalidEndpointException | InvalidPortException e) {

			logger.error(e);
			
			startupLogger.error(" {} | {} | {} ", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			startupLogger.debug(e.getClass().getName(), e);
			
			throw new FileServerException(e);
		}
	}
	


	public String getDisplayName() {
 		return this.getClass().getSimpleName() +
 				( (getShard()!=null) ? ( "_"+ String.valueOf(getShard())) : "");
 				
 	}
	

	public KBFSStorageType getKBFSStorageType() {
			return KBFSStorageType.Minio;
	}
	
	/**
	 *  PUT 1/4 
	 */
	@Override															
	public void putObject(String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
		putObject(bucketName, objectName, filename, stream, getContentType(filename));
	}

	/**
	 *  PUT 2/4
	 *   
	 * @param bucketName
	 * @param objectName
	 * @param stream
	 * @param size
	 * @param contentType
	 */							
	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException {
		
	 	if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
		try {

			boolean found = this.minioClient.bucketExists(bucketName);
			
			if (!found) 
			  this.minioClient.makeBucket(bucketName);
			
			Map<String, String> user_values = new HashMap<String, String>();

			user_values.put("name", normalize(filename));
			user_values.put("Content-Type", contentType);


			this.minioClient.putObject(bucketName, objectName, stream, size, user_values);
			             
			
			/**
			putObject(bucketName, objectName, stream, Long.valueOf(size), 
			      		 Map<String, String> headerMap, 
			    		 ServerSideEncryption sse, 
			      		 String contentType);
			*/      		 
			
			
			
			getSystemMetricsService().getMeterV2PutObject(bucketName).mark();
			getSystemMetricsService().getMeterV2ShardPutObject(getShardStr()).mark();
			getSystemMetricsService().getMeterV2PutObject().mark();
			

			
			} catch (RegionConflictException | InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException   | ErrorResponseException | InternalException | IOException | XmlPullParserException e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		}
		
		catch (Exception e) {
			logger.error(e);
			throw new FileServerException(e);
		}
	}
	
	/**
	 *
	 *  PUT 3/4
	 *  
	 * @param bucketName
	 * @param objectName
	 * @param stream
	 * @param size
	 * @param contentType
	 */																	
	@Override
	public void putObject(String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {
		

		if (bucketName==null || objectName==null ||objectName.equals("null")) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			
			// Create bucket if it doesn't exist
			boolean found = this.minioClient.bucketExists(bucketName);
			
			if (!found) 
			  this.minioClient.makeBucket(bucketName);
			
			this.minioClient.putObject(bucketName, objectName, stream, contentType);
			//  ServerSideEncryption serverSideEncryption = ServerSideEncryption.withManagedKeys("my-minio-key", null);
			//	this.minioClient.putObject(bucketName, objectName, stream, (Long)null, (Map)null, serverSideEncryption, contentType);
			//  this.minioClient.putObject(bucketName, objectName, stream, (Long)null, (Map)null, ServerSideEncryption.atRest(), contentType);
			getSystemMetricsService().getMeterV2ShardPutObject(getShardStr()).mark();
			getSystemMetricsService().getMeterV2PutObject(bucketName).mark();
			getSystemMetricsService().getMeterV2PutObject().mark();
			
			
		} catch (RegionConflictException  | InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException |  
				ErrorResponseException | InternalException | IOException | XmlPullParserException e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		
		} catch (Exception e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		}
		
	}
	

	@Override
	public List<String> listBuckets() throws FileServerException {
	
		List<String> list = new ArrayList<String>();
		
		 try {
	
			 this.minioClient.listBuckets().forEach( item -> list.add( item.name()));
			 
			
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
				| NoResponseException | ErrorResponseException | InternalException | InvalidResponseException
				| IOException | XmlPullParserException e) {

			logger.error(e);
			throw new FileServerException(e);

		}
		
			
		return list;
	}
	
	
	
	/**
	 * 
	 * PUT 4/4
	 * 
	 * @param bucketName
	 * @param objectName
	 * @param fileName
	 * 
	 * Create bucket if it doesn't exist.
	 * 
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void putObject(String bucketName, String objectName, String fileName) throws FileServerException {

			boolean found;

			if (bucketName==null || objectName==null ||objectName.equals("null")) {
		 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
		 		throw new FileServerException(cause);
		 	}
			
			try {

				found = this.minioClient.bucketExists(bucketName);
				
			} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
					| InsufficientDataException| ErrorResponseException | InternalException
					| IOException | XmlPullParserException e) {
				
				logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
				throw new FileServerException(e);
			}
			
			catch (Exception e) {
				logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
				throw new FileServerException(e);
			}
			

			
			if (!found) {
				try {
					
					this.minioClient.makeBucket(bucketName);
					
				} catch (InvalidKeyException | InvalidBucketNameException | RegionConflictException
						| NoSuchAlgorithmException | InsufficientDataException 
						| ErrorResponseException | InternalException | IOException | XmlPullParserException e) {
					
					logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
					throw new FileServerException(e);
				}
				catch (Exception e) {
					logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
					throw new FileServerException(e);
				}
			}
			
			try {

				this.minioClient.putObject(bucketName, objectName, fileName);
				getSystemMetricsService().getMeterV2PutObject(bucketName).mark();
				getSystemMetricsService().getMeterV2ShardPutObject(getShardStr()).mark();
				getSystemMetricsService().getMeterV2PutObject().mark();

				
			} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
					| InsufficientDataException | ErrorResponseException | InternalException
					| IOException | XmlPullParserException e) {

				logger.error(e);
				throw new FileServerException(e);
				
			} catch (Exception e) {
				logger.error(e);
				throw new FileServerException(e);
			}
	}

	
	/**
	 * 
	 * 
	 */
	@Override
	public void removeObject(String bucketName, String objectName) throws FileServerException {

		if (bucketName==null || objectName==null ||objectName.equals("null")) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			if (!this.minioClient.bucketExists(bucketName)) 
					throw new InvalidBucketNameException(bucketName, "b:  " + bucketName + " | bucket does not exist");
			
			
		} catch ( InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException | MinioException e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		}
			
		try {
			this.minioClient.removeObject(bucketName, objectName);
			
			} catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException | MinioException e) {
				logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
				throw new FileServerException(e);
			}
	}
	
	/**
	 *
	 */
	@Override
	public String presignedGetObject(String bucketName, String objectName) throws FileServerException {
		return presignedGetObject(bucketName, objectName, DEFAULT_EXPIRING_TIME);
	}


	/**
	 * @param bucketName
	 * @param objectName
	 * @param expires_seconds
	 */
	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds) throws FileServerException {
												
	 	if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
	 	
	 	if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);

		try {
				return minioClient.presignedGetObject(bucketName, objectName, expires_seconds);
		}
		catch (Exception e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		}
			
	}

	/**
	 * 
	 */
	@Override
	public boolean isObject(String bucketName, String objectName) throws FileServerException {

		if (bucketName==null || objectName==null ||objectName.equals("null")) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
			  this.minioClient.statObject(bucketName, objectName);
			  return true;
			  
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			return false;
		} catch ( ErrorResponseException| InternalException | IOException | XmlPullParserException e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		} catch (Exception e) {
			logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
			throw new FileServerException(e);
		}
	}
	
	
	/**
	 * @param bucketName
	 * @param objectName
	 * @param expires_seconds
	 */
	@Override
	public String presignedGetObject(String bucketName, String objectName, int expires_seconds) throws FileServerException {

	 	if (bucketName==null || objectName==null) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}

		try {
				return minioClient.presignedGetObject(bucketName, objectName, expires_seconds);
				
			} catch (InvalidBucketNameException | InsufficientDataException | ErrorResponseException | InternalException | InvalidExpiresRangeException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e) {
				logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
				throw new FileServerException(e);
			}
			catch (Exception e) {
				logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
				throw new FileServerException(e);
			}
	}

	
	@Override
	public File getDownloadedFile(String fsid, String bn, String on, String fileName)  throws FileServerException  {
		if (fsid!=null && !fsid.equals(this.getFSId())) 
	 		throw new FileServerException("FSId invalid " + fsid);
		return getDownloadedFile(bn, on, fileName);
	}


	/***
	 * 
	 */
	@Override
	public File getDownloadedFile(String bucketName, String objectName, String fileName) throws FileServerException {
		
		/**Check whether the object exists using statObject().
		 If the object is not found, statObject() throws an exception,
		 else it means that the object exists.
		 Execution is successful.
		 */

	 	if (bucketName==null || objectName==null ||objectName.equals("null")) {
	 		String cause=(bucketName==null?" bucketNamet is null ":"")+ (objectName==null?"objectName is null":"");
	 		throw new FileServerException(cause);
	 	}
		
	 	InputStream stream = null;
	 	
		try {
		   	  /** 
			   Check whether the object exists using statObject().
			   If the object is not found, statObject() throws an exception,
			   else it means that the object exists.
			   Execution is successful.
			  */
			  this.minioClient.statObject(bucketName, objectName);

			  if (getLocalFileServerCache().containsKey(bucketName, objectName)) {
				  	File file = getLocalFileServerCache().get(bucketName, objectName);
				  	if (file!=null) {
				  		logger.debug("Cache hit: " + fileName);
				  		getSystemMetricsService().getCounterV2KBFSCacheHit().inc();
				  		return file;
				  	}
			  }
			  
			  logger.debug("Cache miss: " + fileName);
			  logger.debug("Cache Status: " + String.valueOf(this.getLocalFileServerCache().getTotalItems()) + " files. " + (String.format("%8.2f", Double.valueOf(this.getLocalFileServerCache().getTotalDisk()).doubleValue()/MB)).trim()+" MB");
			  
			  stream = this.minioClient.getObject(bucketName, objectName);
			  getSystemMetricsService().getMeterV2GetObject(bucketName).mark();
			  getSystemMetricsService().getMeterV2ShardGetObject(getShardStr()).mark();
			  getSystemMetricsService().getMeterV2GetObject().mark();
			  getSystemMetricsService().getCounterV2KBFSCacheMiss().inc();
			  getLocalFileServerCache().put(bucketName, objectName, stream, fileName);
			  
			  return getLocalFileServerCache().get(bucketName, objectName);
		
		  } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException | MinioException e) {
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

	/**
	 * 
	 * @param bucketName
	 * @param objectName
	 * @param fileName
	 */								
	@Override
	public InputStream getObject(String bucketName, String objectName) throws FileServerException {
		
		 	if (bucketName==null || objectName==null) {
		 		String cause=(bucketName==null?" bucketNamet is null ":"") + (objectName==null?"objectName is null":"");
		 		throw new FileServerException(cause);
		 	}

		 	/** Check whether the object exists using statObject().
			 	If the object is not found, statObject() throws an exception,
			 	else it means that the object exists.
			 	Execution is successful.
			 */
		
		 	InputStream stream = null;
		 
			try {
				  /** 	Check whether the object exists using statObject().
				   		If the object is not found, statObject() throws an exception,
				   		else it means that the object exists.
				   		Execution is successful.
				  */
				  this.minioClient.statObject(bucketName, objectName);
				  
				  stream = this.minioClient.getObject(bucketName, objectName);
				  
				  getSystemMetricsService().getMeterV2GetObject(bucketName).mark();
				  getSystemMetricsService().getMeterV2ShardGetObject(getShardStr()).mark();
				  getSystemMetricsService().getMeterV2GetObject().mark();
				  
				  return stream;
			
			  } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException | MinioException e) {
				  	logger.error(e, "b:" + (bucketName!=null?bucketName:"") + "o:" + (objectName!=null?objectName:""));
					throw new FileServerException(e);
			  }
	}

	/**
	 * 
	 * 
	 */
	@Override
	public String ping() {
		try {
			@SuppressWarnings("unused")
			List<Bucket> list = this.minioClient.listBuckets();
			return "ok";
			
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getName() + ". " + e.getMessage();
		}
	}

	
	/**
	 * Uses Minio Interface
	 * 
	 * @param bucketName

	 * @throws FileServerException
	 */
	public Iterable<Result<Item>> listObjects(String bucketName) throws FileServerException {
		
	 	if (bucketName==null) {
	 		throw new IllegalArgumentException("bucketName is null");
	 	}
			
	 	boolean found;
			
		try {

				found = minioClient.bucketExists(bucketName);
				
				if (found) {
				    Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);
					return myObjects;

			  } else
				  throw new FileServerException(bucketName + " b:" + bucketName + " does not exist");

			} catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e) {
					logger.error(e, (bucketName!=null?bucketName:""));
					throw new FileServerException(e);
			} catch (MinioException e) {
					logger.error(e, "b:" + (bucketName!=null?bucketName:""));
					throw new FileServerException(e);
			}
	}
	
	/**
	 * 
	 */
	public String reconnect(String url, String accessKey, String secretKey) throws FileServerException {
											
		synchronized (this.minioClient) {
			try {
				this.endpoint = url;
				this.accessKey = accessKey;
				this.secretKey = secretKey;
				this.minioClient = new MinioClient(url, accessKey, secretKey);
				logger.info("Connection Successful");
				
			} catch (InvalidEndpointException | InvalidPortException e) {

				logger.error(e, url);
				startupLogger.debug(e.getClass().getName(), e);
		
				throw new FileServerException(e);
			}
		}
		return ping();
	}

	public int getCacheSize() {
		return getLocalFileServerCache().getTotalItems();
	}
	
	public long getCacheUsage() {
		return getLocalFileServerCache().getTotalDisk();
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
	 */
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

	
	
	@Override
	public String normalize(String str) {
		 String p=str.replaceAll("[ |\\t|\\s|(|)]", "")
				 .replace("á", "a")
				 .replace("é", "e")
				 .replace("í", "i")
				 .replace("ó", "o")
				 .replace("ú", "u")
				 .replace("ñ", "n")
				 .replace(";", "")
				 .replace(":", "")
		 		 .replace("°", "")
				 .replace("|", "")
				 .replace("#", "")
				 .replace("$", "")
				 .replace("%", "")
				 .replace("&", "")
				 .replace("/", "")
				 .replace("¡", "")
				 .replace("?", "")
				 .replace("=", "")
				 .replace("}", "")
				 .replace("{", "")
				 .replace(":", "")
				 .replace("¿", "")
				 .replace("°", "")
				 .replace("*", "")
				 .replace("\"", "")
				 .replace("Á,", "A")
				 .replace("É,", "E")
				 .replace("Í,", "I")
				 .replace("Ó,", "O")
				 .replace("Ú,", "U")
				 .replace("Ñ",  "N");
		 
		 if (p.length()<MAX_FNAME_LENGTH)
			 return p;
		 
		 String sf = FilenameUtils.getExtension(str);
		 
		 if (sf.length()>0) {
			 return p.substring(0,MAX_FNAME_LENGTH)+"."+sf;
		 }
		 else
			 return p.substring(0, MAX_FNAME_LENGTH);
	}

	
	private String getShardStr() {
		return this.assigned_shard_str;
	}
	
	public void setShard(Integer shard) {
		this.assigned_shard=shard;
		this.assigned_shard_str=new String(this.assigned_shard.toString());
	}
	
	
	@Override
	public Integer getShard(String bucketName, String objectName) {
		if (assigned_shard==null)
			assigned_shard = Integer.valueOf(1);
		return assigned_shard;
	}
	

	/**
	 * For a Minio File Server the Shard is not meaningful.
	 * It is used by the ShardedMinioFileServer to select the right FileServerMinio
	 */
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream,	long size, String contentType) throws FileServerException {
		putObject(bucketName, objectName, filename, stream,	size, contentType);
	}
	
	
	
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream,	String contentType) throws FileServerException {
		putObject(bucketName, objectName, filename, stream,	contentType);
	}
	
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream)	throws FileServerException {
		putObject(bucketName,objectName, filename, stream);
	}
	
	@Override
	public void putObject(Integer shard, String bucketName, String objectName, String fileName)	throws FileServerException {
		putObject(bucketName, objectName, fileName);
	}
	
	@Override
	public String presignedGetObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		return presignedGetObject(bucketName, objectName);
	}
	
	@Override																					
	public String presignedGetObject(Integer shard, String bucketName, String objectName, int expires_seconds) throws FileServerException {
		return presignedGetObject(bucketName, objectName, expires_seconds);
	}
	@Override
	public InputStream getObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		return getObject(bucketName, objectName);
	}
	
	@Override
	public File getDownloadedFile(Integer shard, String bucketName, String objectName, String fileName)	throws FileServerException {
		return getDownloadedFile(bucketName, objectName,fileName);
	}

	@Override
	public void removeObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		removeObject(bucketName, objectName);
	}
	
	@Override
	public String ping(Integer shard) {
		return ping();
	}
	@Override
	public boolean isObject(Integer shard, String bucketName, String objectName) throws FileServerException {
		return isObject(bucketName, objectName);
	}
	
	@Override
	public String getEndPoint(Integer shard) {
		return getEndPoint();
	}
	@Override
	public String getAccessKey(Integer shard) {
		return getAccessKey();
	}
	@Override
	public String getSecretKey(Integer shard) {
		return getSecretKey();
	}
	

	@Override
	public String getFSId(Integer shard) {
		return getFSId();
	}
	@Override
	public String getFSId() {
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
	
	@Override
	public String reconnect(Integer shard, String url, String accessKey, String secretKey) throws FileServerException {
		return reconnect(url, accessKey, secretKey);
	}

	@Override
	public Integer getShard(String shardid) {
		if (this.fsid!=null && this.fsid.equals(shardid))
			return getShard();
		return null;
	}

	@Override
	public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (this.fsid!=null && this.fsid.equals(fsid))
				return getObject(bucketName, objectName);
		else
			throw new FileServerException("FSID invalid " + fsid);
	}


	@Override
	public String presignedGetObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		return presignedGetObject(bucketName, objectName);
	}

	@Override
	public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		removeObject(bucketName, objectName);
	}

	@Override
	public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException {
		if (fsid!=null && !fsid.equals(this.getFSId()))
	 		throw new FileServerException("FSId invalid " + fsid);
		return isObject(bucketName, objectName);
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

	@Override
	public Map<String, String> getInfo() {
		return new HashMap<String, String>();
	}

	
	
}

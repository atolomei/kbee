
package com.novamens.kbee.kbfs;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.minio.errors.*;
import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;


/**
 * <p>
 * 
 * ---------------------------------------
 * prod -> para todos los produccion
 * ---------------------------------------
 * demo-1,-2-3, etc. ->
 * dev-1
 * dev-at
 * dev-af
 * 
 * testing-1
 * testing-2

 *     // static final private int BUFFER_SIZE = 8192;
 *     
 *     
 *     	kbfsAmazonS3.accesskey=AKIAWTIE7VJ7
		kbfsAmazonS3.secretkey=C6xX9WqnjHsAMru97+6d/kgHm
		kbfsAmazonS3.region=us-east-1
		kbfsAmazonS3.environment=kbee-dev


 */		
public class KbeeAmazonS3FileServer implements FileServerS3 {


	static private final int BYTES_PER_PART = 5*1024*1024;
	
    static private  final String _ACCESSKEY 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("amazonS3.accesskey", "").trim();
    static private  final String _SECRETKEY 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("amazonS3.secretkey", "").trim();
    static private  final String REGION 		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("amazonS3.region", Region.US_EAST_1.toString()).trim();
    
    static private final String ENV 		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("amazonS3.environment", "kbee-dev").trim(); // dev / testing / qa / prod
    static private final String ENABLED 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("amazonS3.enabled", "no").trim(); // dev or prod

    static private  String ACCESSKEY_SUFFIX = "J73N6FZHF7";
    static private  String SECRETKEY_SUFFIX = "AI3f/BBxqohEHY3jip0h"; 
    
    static private final double MB = 1000000;
    static private final int MAX_FNAME_LENGTH = 440;
    static private final int DEFAULT_EXPIRING_TIME = 60 * 60 * 24 * 7; // 7 days

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeAmazonS3FileServer.class.getName());
    static private Logger startupLogger = LogManager.getLogger("StartupLogger");

    private String fsid = "";
    
    private LocalFileServerCache fslocalcache;
    private SystemMetricsService metrics;

    private S3Client s3Client;
    private String accessKey;
    private String secretKey;
    private OffsetDateTime timeConnected;

	private boolean minor =  false;
	private boolean isReadOnly = false;
	
	
	public KbeeAmazonS3FileServer() throws FileServerException {
        this(_ACCESSKEY + ACCESSKEY_SUFFIX, _SECRETKEY + SECRETKEY_SUFFIX, ENV);
    }


    // Credentials are taken from:
    //
    // .aws/credentials
    // .aws/config
    //
    // private AWSCredentials credentials;
    // private Region usWest2 = Region.getRegion(Regions.US_EAST_1);

    /** --------------------------------------------------------------
     * @param accessKey
     * @param secretKey
     * @param fsid
     * @param shard
     * @param probability
     * @throws FileServerException
     */
    
    public KbeeAmazonS3FileServer(String accessKey, String secretKey, final String env) throws FileServerException {
    								
        if (accessKey == null || secretKey == null)
            throw new IllegalArgumentException("accessKey or secretKey is null");

        if (env != null)
            this.fsid = env.toLowerCase().trim();

        try {
            
            this.accessKey = accessKey;
            this.secretKey = secretKey;
    
            if(accessKey.isEmpty() || secretKey.isEmpty() || (!(ENABLED.toLowerCase().trim().equals("yes") || ENABLED.toLowerCase().trim().equals("true")))){
            	s3Client=null;
                startupLogger.info("AmazonS3 file server connection not set.");
                startupLogger.info("amazonS3.enabled="+ENABLED.toLowerCase().trim());
                return;
            }

            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            s3Client = S3Client.builder().
            		region(Region.of(REGION)).
            		credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();
            

            this.timeConnected = OffsetDateTime.now();
            
            startupLogger.info("Amazon S3 accessKey -> " + accessKey);
            startupLogger.info("Amazon S3 env -> " + env);
            
            startupLogger.info("Amazon S3 Startup Successful.");
            startupLogger.info("--------------------------------------------------------");
            
            logger.debug("accessKey -> " + accessKey);
            logger.debug("secretKey -> " + (((secretKey!=null) && (secretKey.length()>5)) ? secretKey.substring(0,5) : ""));
            logger.debug("env -> " + env);
            
        } catch (Exception e) {
            logger.error(e);
            startupLogger.error(" {} | {} | {} ", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            startupLogger.debug(e.getClass().getName(), e);

            throw new FileServerException(e);
        }
    }

    
	@Override
	public String getDisplayName() {
 		return this.getClass().getSimpleName();
 	}

    
	@Override
	public KBFSStorageType getKBFSStorageType() {
		return KBFSStorageType.AmazonS3;
	}

    public synchronized void close() {

        if (getS3() != null)
            getS3().close();
    }
    
    @Override
    public String getEnvironment() {
    	return getFSId();
    }

    /**
	 * Uses Minio Interface
	 * 
	 * @param bucketName

	 * @throws FileServerException
	 */
	public ListIterator<S3Object> listObjects(String bucketName) throws FileServerException {
		
			try {

					ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).build();
					ListObjectsResponse response = this.s3Client.listObjects(request);
			        List<S3Object> objects = response.contents();
			        ListIterator<S3Object> listIterator = objects.listIterator();
					return listIterator;

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
    public void putObject(String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException {
        putObject(bucketName, objectName, filename, stream, contentType);
    }


    @Override
    public void putObject(String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
        putObject(bucketName, objectName, filename, stream, getContentType(filename));
    }

    @Override
    public void putObject(String baseBucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {

        if (baseBucketName == null || objectName == null || objectName.equals("null")) {
            String cause = (baseBucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }
        
        checkConnectionSet();
        
        try {
        
        	// s3Client.HttpClientPoolAvailableCount();
        	
        	String bucketName = baseBucketName;
        	
        	synchronized (this.s3Client) {
		        	// Create bucket if it doesn't exist
		            boolean found = false;
		            try {
		                this.s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
		                found = true;
		                
		            } catch (NoSuchBucketException e) {
		            	logger.warn(e);
		            	logger.debug("Will try to create bucket -> " + bucketName);
		            	
		            } catch (Exception e) {
		            	logger.error(e);
		            }
		
		            if (!found)
		                this.s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        	}
        	
        	
            if (!(stream instanceof BufferedInputStream)) {
                stream = new BufferedInputStream((InputStream) stream);
            }

            //
            // First create a multipart upload and get upload id
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder().bucket(bucketName).key(objectName).build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
            String uploadId = response.uploadId();

            int partNum = 1;
            final int bytesPerPart=BYTES_PER_PART;
            byte[] bytes = new byte[bytesPerPart];
            List<CompletedPart> completedParts = new ArrayList<>();
            boolean moreContent=true;
            while(moreContent){
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder().bucket(bucketName).key(objectName).uploadId(uploadId).partNumber(partNum).build();
                int readBytes = getAvailableBytes(stream, bytes, bytesPerPart);
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, readBytes);
                moreContent=readBytes>=bytesPerPart;
                String etag = s3Client.uploadPart(uploadPartRequest, RequestBody.fromByteBuffer(byteBuffer)).eTag();
                CompletedPart completedPart = CompletedPart.builder().partNumber(partNum).eTag(etag).build();
                completedParts.add(completedPart);
                partNum++;
            }
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(completedParts).build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder().bucket(bucketName).key(objectName).uploadId(uploadId).multipartUpload(completedMultipartUpload).build();
            s3Client.completeMultipartUpload(completeMultipartUploadRequest);

            getSystemMetricsService().getMeterS3PutObject(bucketName).mark();
            getSystemMetricsService().getMeterS3PutObject().mark();


        } catch (S3Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }


    }

    

    private int getAvailableBytes(InputStream inputStream, byte[] output, int expectedReadSize) throws IOException, InternalException {
        int bytesRead;
        int totalBytesRead;
        for(totalBytesRead = 0; totalBytesRead < expectedReadSize; totalBytesRead += bytesRead) {
            bytesRead = inputStream.read(output, totalBytesRead, expectedReadSize-totalBytesRead);
            if (bytesRead < 0) {
                break;
            }
        }
        return totalBytesRead;
    }

    @Override
    public void putObject(String bucketName, String objectName, String fileName) throws FileServerException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(fileName))) {
            putObject(bucketName, objectName, fileName, stream, getContentType(fileName));
        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }


    @Override
    public String presignedGetObject(String baseBucketName, String objectName, int expires_seconds)
            throws FileServerException {
        if (baseBucketName == null || objectName == null) {
            String cause = (baseBucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }
        checkConnectionSet();
        try {
            
        	String bucketName= baseBucketName;
        	
        	S3Presigner presigner = S3Presigner.create();

            PresignedPutObjectRequest presignedRequest =
                    presigner.presignPutObject(z -> z.signatureDuration(Duration.ofSeconds(expires_seconds))
                            .putObjectRequest(por -> por.bucket(bucketName).key(objectName)));

            return presignedRequest.url().toExternalForm();
        } catch (Exception e) {
            logger.error(e);
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

    @Override
    public String presignedGetObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return presignedGetObject(bucketName, objectName);
    }

    @Override
    public String presignedGetObject(Integer shard, String bucketName, String objectName, int expires_seconds) throws FileServerException {
        return presignedGetObject(bucketName, objectName, expires_seconds);
    }


    /**
     * @param bucketName
     * @param objectName
     * @param expires_seconds
     */
    @Override
    public String presignedGetObject(String fsid, String bucketName, String objectName, int expires_seconds) throws FileServerException {
        if (fsid != null && !fsid.equals(this.getFSId()))
            throw new FileServerException("FSId invalid " + fsid);

        return presignedGetObject(bucketName, objectName, expires_seconds);
    }

    @Override
    public String presignedGetObject(String fsid, String bucketName, String objectName) throws FileServerException {
        if (fsid != null && !fsid.equals(this.getFSId()))
            throw new FileServerException("FSId invalid " + fsid);
        return presignedGetObject(bucketName, objectName);
    }


    @Override
    public InputStream getObject(String fsid, String bucketName, String objectName) throws FileServerException {
        if (this.fsid != null && this.fsid.equals(fsid))
            return getObject(bucketName, objectName);
        else
            throw new FileServerException("FSID invalid " + fsid);
    }

    @Override
    public InputStream getObject(String baseBucketName, String objectName) throws FileServerException {

        if (baseBucketName == null || objectName == null) {
            String cause = (baseBucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }
        checkConnectionSet();
        try {
        	
        	String bucketName= baseBucketName;
        	
            this.s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(objectName).build());

            getSystemMetricsService().getMeterS3GetObject(bucketName).mark();
            getSystemMetricsService().getMeterS3GetObject().mark();
            return response;

        } catch (S3Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }

    @Override
    public File getDownloadedFile(String basebucketName, String objectName, String fileName) throws FileServerException {
        if (basebucketName == null || objectName == null || objectName.equals("null")) {
            String cause = (basebucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }
        checkConnectionSet();
        InputStream stream = null;
        try {
            
        	String bucketName= basebucketName;
        	
        	//Check if the object exists.
            this.s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());

            if (getLocalFileServerCache().containsKey(bucketName, objectName)) {
                File file = getLocalFileServerCache().get(bucketName, objectName);
                if (file != null) {
                    logger.debug("Cache hit: " + fileName);
                    getSystemMetricsService().getCounterS3KBFSCacheHit().inc();
                    return file;
                }
            }

            logger.debug("Cache miss: " + fileName);
            logger.debug("Cache Status: " + String.valueOf(this.getLocalFileServerCache().getTotalItems()) + " files. " + (String.format("%8.2f", Double.valueOf(this.getLocalFileServerCache().getTotalDisk()).doubleValue() / MB)).trim() + " MB");

            stream = this.getObject(bucketName, objectName);
            getSystemMetricsService().getMeterS3GetObject(bucketName).mark();
            getSystemMetricsService().getMeterS3GetObject().mark();
            getSystemMetricsService().getCounterS3KBFSCacheMiss().inc();
            
            getLocalFileServerCache().put(bucketName, objectName, stream, fileName);
            return getLocalFileServerCache().get(bucketName, objectName);
            
        } catch (AwsServiceException | SdkClientException e) {
            logger.error(e);
            throw new FileServerException(e);
        }
        
        finally {
        	if (stream!=null)
				try {
					stream.close();
				} catch (IOException e) {
					logger.error(e);
				}
        }
    }


    /**
     *
     */
    @Override
    public File getDownloadedFile(String fsid, String bn, String on, String fileName) throws FileServerException {
        if (fsid != null && !fsid.equals(this.getFSId()))
            throw new FileServerException("FSId invalid " + fsid);
        return getDownloadedFile(bn, on, fileName);
    }

    @Override
    public File getDownloadedFile(Integer shard, String bucketName, String objectName, String fileName)
            throws FileServerException {
        return getDownloadedFile(bucketName, objectName, fileName);
    }

    @Override
    public void removeObject(String basebucketName, String objectName) throws FileServerException {
        if (basebucketName == null || objectName == null || objectName.equals("null")) {
            String cause = (basebucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }
        try {
        	
        	String bucketName = basebucketName;
        	logger.debug("S3 deleteObject -> " + bucketName + " / " + objectName);
            this.s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectName).build());
            
        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }

    @Override
    public void removeObject(String fsid, String bucketName, String objectName) throws FileServerException {
        if (fsid != null && !fsid.equals(this.getFSId()))
            throw new FileServerException("FSId invalid " + fsid);

        removeObject(bucketName, objectName);
    }


    @Override
    public boolean isObject(String basebucketName, String objectName) throws FileServerException {
        if (basebucketName == null || objectName == null || objectName.equals("null")) {
            String cause = (basebucketName == null ? " bucketNamet is null " : "") + (objectName == null ? "objectName is null" : "");
            throw new FileServerException(cause);
        }

        try {
        	String bucketName=basebucketName;
            this.s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
            return true;
            
        } catch (NoSuchKeyException e) {
            logger.error(e);
            return false;
        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }

    @Override
    public boolean isObject(String fsid, String bucketName, String objectName) throws FileServerException {
        if (fsid != null && !fsid.equals(this.getFSId()))
            throw new FileServerException("FSId invalid " + fsid);
        checkConnectionSet();
        return isObject(bucketName, objectName);
    }

    @Override
    public String getEndPoint() {
        return "";
    }

    @Override
    public String getAccessKey() {
        return this.accessKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }



    public String getFSId() {
        return this.fsid;
    }


    
    @Override
    public String reconnect() throws FileServerException {
    	
        synchronized (this.s3Client) {
            try {
            	
            	logger.debug("Reconnecting Amazon S3 Client");
            	
                this.close();
                
                s3Client = null;
                
                AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
                
                s3Client = S3Client.builder().region(Region.of(REGION)).credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();

                startupLogger.info("Amazon S3 accessKey -> " + accessKey);
                startupLogger.info("Amazon S3 env -> " + this.fsid);
                startupLogger.info("--------------------------------------------------------");
                
                logger.debug("accessKey -> " + accessKey);
                logger.debug("secretKey -> " + secretKey);

                timeConnected = OffsetDateTime.now();
                
                logger.info("Connection Successful");
                
            } catch (S3Exception e) {
            	
                logger.error(e);
                startupLogger.debug(e.getClass().getName(), e);
                throw new FileServerException(e);
            }
            
        }
        return ping();
    }

    
    @Override
    public String ping(Integer shard) {
        return ping();
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
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, long size, String contentType) throws FileServerException {
        putObject(bucketName, objectName, filename, stream, size, contentType);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream, String contentType) throws FileServerException {
        putObject(bucketName, objectName, filename, stream, contentType);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String filename, InputStream stream) throws FileServerException {
        putObject(bucketName, objectName, filename, stream);
    }

    @Override
    public void putObject(Integer shard, String bucketName, String objectName, String fileName) throws FileServerException {
        putObject(bucketName, objectName, fileName);
    }


    @Override
    public InputStream getObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return getObject(bucketName, objectName);
    }


    @Override
    public void removeObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        removeObject(bucketName, objectName);

    }

    @Override
    public boolean isObject(Integer shard, String bucketName, String objectName) throws FileServerException {
        return isObject(bucketName, objectName);
    }

    
    public OffsetDateTime getDateConnected() {
		return timeConnected;
	}

    public void setReadOnly(boolean b) {
        this.isReadOnly = b;
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    private LocalFileServerCache getLocalFileServerCache() {
        if (fslocalcache == null)
            fslocalcache = ServiceLocator.getService(LocalFileServerCache.class);
        return fslocalcache;
    }


    private SystemMetricsService getSystemMetricsService() {
        if (this.metrics == null)
            this.metrics = ServiceLocator.getService(SystemMetricsService.class);
        return this.metrics;
    }

    
    /** ----------------------------------------
     * 
     * Amazon S3 supports only 1 shard
     * We us FSId for "prod" "dev"
     *  
     ------*/
	public Integer getShard(String bucketName, String objectName) {return Integer.valueOf(0);}
	public String  getFSId(Integer shard) {return this.getFSId();}
	public Integer  getShard(String fsid)  {return Integer.valueOf(0);}


	@Override
	public boolean isEnabled() {
		return (s3Client!= null) && (ENABLED.toLowerCase().trim().equals("yes") || ENABLED.toLowerCase().trim().equals("true")); 
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
    @Override
    public String ping() {
        try {
        	
            checkConnectionSet();
            
            this.s3Client.listBuckets();
            
            logger.debug("ping ok");
            
            return "ok";
            
        } catch (Exception e) {
            logger.error(e);
            return e.getClass().getName() + ". " + e.getMessage();
        }
    }

    /**
     * @return
     */
    protected S3Client getS3() {
        return s3Client;
    }


    private String getContentType(String src) {

        if (FSUtils.isPdf(src))
            return "application/pdf";

        if (FSUtils.isImage(src)) {
            String str = FilenameUtils.getExtension(src);
            if (str != null && (str.toLowerCase().equals("jpg") || str.toLowerCase().equals("jpeg")))
                return "image/jpeg";
            return "image/" + str;
        }
        if (FSUtils.isVideo(src)) {
            return "video/" + FilenameUtils.getExtension(src);
        }

        if (FSUtils.isAudio(src))
            return "audio/" + FilenameUtils.getExtension(src);

        return "application/octet-stream";
    }

	private void checkConnectionSet() throws FileServerException{
        if(s3Client == null) {
            throw new FileServerException("AmazonS3 file server connection not set.");
        }
    }
	
	
    //@SuppressWarnings("unused")
	public String normalize(String str) {
        String p = str.replaceAll("[ |\\t|\\s|(|)]", "")
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
                .replace("Ñ", "N");

        if (p.length() < MAX_FNAME_LENGTH)
            return p;

        String sf = FilenameUtils.getExtension(str);

        if (sf.length() > 0) {
            return p.substring(0, MAX_FNAME_LENGTH) + "." + sf;
        } else
            return p.substring(0, MAX_FNAME_LENGTH);
    }

	@Override
	public List<String> listBuckets() throws FileServerException {
	
		List<String> list = new ArrayList<String>();
		
		 try {
			 ListBucketsResponse res = getS3().listBuckets();
			 res.buckets().forEach( item -> list.add( item.name()));
			 
	        } catch (Exception e) {
	            logger.error(e);
	            throw new FileServerException(e);
	        }
			
		 	return list;
	}


	@Override
	public Map<String, String> getInfo() {
		return new HashMap<String, String>();
	}


    

    
}
 

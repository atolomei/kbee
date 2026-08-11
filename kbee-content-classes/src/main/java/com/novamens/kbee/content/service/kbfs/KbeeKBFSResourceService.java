package com.novamens.kbee.content.service.kbfs;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;


import com.novamens.kbee.kbfs.encryption.EncryptionService;

import org.apache.commons.io.FilenameUtils;
import org.springframework.transaction.annotation.Propagation;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.kbfs.KBFSInputStreamWrapper;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.KBFSService;
import com.novamens.kbfs.v1.FSOutputStream;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.util.KbeeRuntimeException;

import io.odilon.util.FileNameNormalizer;

/**
 * <b>FileServerV1</b>
 * <b>FileServerMinio</b>
 * 
 * <p>
 * <!-- File Server V1 -->
 * <bean id="com.novamens.kbee.kbfs.v1.FileServerFactory" class="com.novamens.kbee.kbfs.v1.KbeeFileServerFactory">
 * <property name="service" ref="com.novamens.kbfs.v1.FileServerV1"/>
 * </bean>
 * <bean id="com.novamens.kbfs.v1.FileServerV1" class="com.novamens.kbee.kbfs.v1.KbeeFileServer"/>
 * <p>
 * -----------------------------------------------------------------------------------------------------------------------------
 
 * <bean id=" com.novamens.kbee.kbfs.KbeeFileServerMinioFactory" class="com.novamens.kbee.kbfs.KbeeFileServerV2Factory">
 * <property name="service" ref="com.novamens.kbfs.FileServerMinio"/>
 * </bean>
 * <bean id="com.novamens.kbfs.FileServerMinio" class="com.novamens.kbee.kbfs.KbeeShardedMinioFileServer"/>
 *
 * <bean id="FileServerS3Factory" class="com.novamens.spring.service.SpringSystemServiceFactory">
 * <property name="bean" value="FileServerS3"/>
 * </bean>
 * <bean id="FileServerS3" class="com.novamens.kbee.kbfs.KbeeAmazonS3FileServer"/>
 *
 * 
 **/

public class KbeeKBFSResourceService implements KBFSResourceService {

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeKBFSResourceService.class.getName());

    static private int MAX_FNAME_LENGTH = 440;
    static private int MAX_DOMAIN_NAME_LENGTH = 32;

    private KBFile file;
    private Content content;
    
    private KBFSService kbfodilon;  // it may be a sharded
    private KBFSService kbfminio;  // it may be a sharded
    private KBFSService kbfs1;
    private KBFSService kbfsAmazonS3;

    private KBFSStorageType defaultStorageType;
    private Integer preasigned_shard = null;

    // Spring
    private ContentDao contentDao;
    
    
    public KbeeKBFSResourceService() {
    }

    public KbeeKBFSResourceService(KBFile file) {
        this.file = file;
    }
    
    public KbeeKBFSResourceService(Content content) {
    	this.content = content;
    }

    @Override
    public void putObject(String filename, InputStream stream) throws FileServerException {
        putObject(filename, stream, true, null);
    }

    @Override
    public void putObject(String filename, InputStream stream, String contentType) throws FileServerException {
        putObject(filename, stream, true, contentType);
    }
    
    public String normalize(String name) {
        KBFSStorageType type = getDefaultKBFSStorageType();
        String normalized = getKBFSService(type).normalize(name);
        return normalized;
    }

    /***
     * PREASIGNO -> [Type + Shard] +  Stream
     */
    public void putObject(String filename, InputStream stream, boolean update_metainfo, String contentType) throws FileServerException {

        long start = System.currentTimeMillis();

        String bn = getBucketName();
        String on = getObjectName(filename);

        Integer i_shard;

        KBFSStorageType type = getDefaultKBFSStorageType();
        InputStream filtredStream = null;

        KBFSInputStreamWrapper kbfsInputStreamWrapper = null;

        try {

            kbfsInputStreamWrapper = new KBFSInputStreamWrapper(stream, filename);
            filtredStream = kbfsInputStreamWrapper;

            // ---------------------------------------------------
            //
            //  KBFSStorageType.KBFS1 encryption is not suported
            //
            if (this.file.getDomain().isEncryptFiles()) {
                this.file.setIsEncrypted(true);
                filtredStream = getEncryptionService().encryptStream(filtredStream);
                on += ".enc";
            }

            if (type == KBFSStorageType.KBFS1) {
            	throw new KbeeRuntimeException("invalid file server");
                //String kbfs1FileName = this.file.getIsEncrypted() ? filename + ".enc" : filename;
                //uploadkbfs1(kbfs1FileName, bn, on, filtredStream);
                
            } else {

            	
            	i_shard = getAssignedShard(type, bn, on); 
                getKBFSService(type).putObject(i_shard, bn, on, filename, filtredStream, contentType);
                this.file.setObjectName(on);
                this.file.setShard(i_shard);
                this.file.setFSID(getKBFSService(type).getFSId(i_shard));
            }

            kbfsInputStreamWrapper.close();
            
            if (this.file instanceof KBFileImpl)
                ((KBFileImpl) this.file).setSize(kbfsInputStreamWrapper.getFileSize());

            if (logger.isDebugEnabled()) {
                if (kbfsInputStreamWrapper.getFileSize() != kbfsInputStreamWrapper.getBytesRead()) {
                    logger.error("Error. FileSize: " + String.valueOf(kbfsInputStreamWrapper.getFileSize()) + "  " + String.valueOf(kbfsInputStreamWrapper.getBytesRead()));
                }
            }

            this.file.setStorageType(type); // esto determina a que File Server va el pdf

            if (this.file instanceof KBFileImpl)
                ((KBFileImpl) this.file).setName(filename);

            this.file.setBucketName(bn);

            if (update_metainfo) {
                
            	this.file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
                this.file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
                this.file.setUploadOffsetDateTime(OffsetDateTime.now());
                this.file.setUploadUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
                
            } else {
            	
                if (this.file.getUploadUser() == null)
                    this.file.setUploadUser(ServiceLocator.getService(SecurityService.class).getSessionUser());

                if (this.file.getLastModifiedUser() == null)
                    this.file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());

                if (this.file.getLastModifiedOffsetDateTime() == null)
                    this.file.setLastModifiedOffsetDateTime(OffsetDateTime.now());

                if (this.file.getUploadOffsetDateTime() == null)
                    this.file.setUploadOffsetDateTime(OffsetDateTime.now());
            }

            // ----------
            // these values are calculated only AFTER the stream is closed
            // ----------
            
            this.file.setSHA256(kbfsInputStreamWrapper.getSHA256());
            this.file.setWidth(kbfsInputStreamWrapper.getWidth());
            this.file.setHeight(kbfsInputStreamWrapper.getHeight());


        } catch (IOException e) {
            
        	logger.error(e);
            throw new FileServerException(e, "probably can not create KBFSInputStreamWrapper due to some problem with the cache Workdir");

        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);

        } finally {
            if (filtredStream != null) {
                try {
                    filtredStream.close();

                } catch (IOException e) {
                    logger.error(e);
                }
            }

            try {
                
            	if (kbfsInputStreamWrapper != null)
                    kbfsInputStreamWrapper.close();
                
            } catch (IOException e) {
                logger.error(e);
            }


            if (logger.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                logger.debug("putObject duration " + (type != null ? type.getKey() : "") + ":" + String.valueOf(end - start) + " ms");
            }
        }
    }

    private Integer getAssignedShard(KBFSStorageType type, String bn, String on) {
    	if (preasigned_shard==null)
    		return getKBFSService(type).getShard(bn, on);
    	return preasigned_shard;
	}
    
    public void setPreassignedShard(Integer pre_assigned_shard) {
    		this.preasigned_shard =pre_assigned_shard;
    }
    
	/**
     *
     */
    @Override
    public String presignedGetObject() throws FileServerException {
        String bn = file.getBucketName();
        String on = file.getObjectName();
        String fsid = file.getFSID();
        if (this.file.getStorageType() == KBFSStorageType.KBFS1) {
            if (on == null)
                on = file.getUrl();
        }
        if (fsid != null)
            return getKBFSService().presignedGetObject(fsid, bn, on);
        return getKBFSService().presignedGetObject(bn, on);
    }

    /**
     *
     */
    @Override
    public String presignedGetObject(int expires_seconds) throws FileServerException {
        String bn = file.getBucketName();
        String on = file.getObjectName();
        String fsid = file.getFSID();
        if (this.file.getStorageType() == KBFSStorageType.KBFS1) {
            if (bn == null)
                bn = file.getDomain().getName();
            if (on == null)
                on = file.getUrl();
        }
        if (fsid != null)
            return getKBFSService().presignedGetObject(fsid, bn, on, expires_seconds);
        return getKBFSService().presignedGetObject(bn, on, expires_seconds);
    }

    /**
     * [1/2]
     * Important: Caller must close the InputStream
     *
     * @throws FileServerException
     */
    @Override
    public InputStream getObject() throws FileServerException {
        String bn = file.getBucketName();
        String on = file.getObjectName();
        String fsid = file.getFSID();
        if (this.file.getStorageType() == KBFSStorageType.KBFS1) {
            if (bn == null)
                bn = file.getDomain().getName();
            //if (on == null) {
                on = file.getUrl();
            //}
        }
        
        
        InputStream stream;
        
        if (fsid != null) {
            stream = getKBFSService().getObject(fsid, bn, on);
        } else {
            stream = getKBFSService().getObject(bn, on);
        }
        return !this.file.getIsEncrypted() ? stream : getEncryptionService().decryptStream(stream);
    }


    /**
     * [1/2]
     */
    @Override
    public File getDownloadedFile() throws FileServerException {
        
    	String bn = file.getBucketName();
        String on = file.getObjectName();
        
        String fsid = file.getFSID();
        
        if (this.file.getStorageType() == KBFSStorageType.KBFS1) {
            if (bn == null)
                bn = file.getDomain().getName();
            if (on == null)
                on = file.getUrl();
        } else if (this.file.getStorageType() == KBFSStorageType.External) {
            if (bn == null)
                bn = file.getDomain().getName();
            if (on == null)
                on = String.valueOf(file.getId());
        }

        
        InputStream inputStream = null;
        InputStream resultStream = null;
        
        
       /**
        * get sure all streams close before returning
        */
       try {
    	   
			         LocalFileServerCache cache = ServiceLocator.getService(LocalFileServerCache.class);
			           
			         File cfile = cache.get(bn,on);
			           
			         if (cfile!=null)
			          	return cfile;

           
    	           if (this.file.getStorageType() == KBFSStorageType.External) {
			            try {
			                inputStream = file.getInputStream();
			            } catch (IOException e) {
			                logger.error(e);
			                throw new FileServerException(e);
			            }
			            
			        } else {
			        	
			            if (fsid != null) {
			                inputStream = getKBFSService().getObject(fsid, bn, on);
			            } else {
			                inputStream = getKBFSService().getObject(bn, on);
			            }
			        }
			
			        logger.debug("cache put -> " + this.file.getTitle());
			        
			        try {
				    
			        	resultStream = !this.file.getIsEncrypted() ? inputStream : getEncryptionService().decryptStream(inputStream);
				        cache.put(bn, on, resultStream, file.getFileName());
			        
			        } finally {
			        	
			        	if (inputStream!=null) { 
							try {
								logger.debug("i- closing InputStream for -> " + this.file.getTitle());
								inputStream.close();
							} catch (IOException e) {
								logger.error(e);
							}	
						}
			
			        	
			        	if (resultStream!=null) { 
							try {
								logger.debug("ii- closing  resultStream  for -> " + this.file.getTitle());
								resultStream.close();
							} catch (IOException e) {
								logger.error(e);
							}	
						}
			        	
			        }
			
			        return cache.get(bn, on);
       
       } finally {
    	   
    	   if (inputStream != null)
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error(e);
			}
    	   
    	   
    	   if (resultStream != null)
			try {
				resultStream.close();
			} catch (IOException e) {
				logger.error(e);			
			}
    	   
       }

    }


    /**
     * 
     * 
     */
    @Override
    public boolean isObject() throws FileServerException {
        try {
            if (file.getFSID() != null)
                return getKBFSService(file.getStorageType()).isObject(file.getFSID(), file.getBucketName(), file.getObjectName());
            return getKBFSService(file.getStorageType()).isObject(file.getBucketName(), file.getObjectName());

        } catch (Exception e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }

    /**
     * this method allows for a resource to be removed from the Object Storage
     * 
     */
    @Override
    public void removeObject() throws FileServerException {

    	try {
	    	
    		String bn = file.getBucketName();
	        String on = file.getObjectName();
	
	        if (this.file.getStorageType() == KBFSStorageType.KBFS1) {
	
	        	if (bn == null)
	                bn = file.getDomain().getName();
	            
	            if (on == null)
	                on = file.getUrl();
	        }
	
	        logger.debug("removeObject -> " + bn + " -" + on);
	        getKBFSService().removeObject(bn, on);
	        
    	} catch (Exception e) {
    		logger.error(e);
    		throw new FileServerException(e);
    	}
    	
    	/**
    	 * this method is not atomic
    	 * it allows for a resource to be removed from the Object Storage 
    	 * and later setisExistInObjectStorage() fail 
        try {
        	this.file.setisExistInObjectStorage(false);
            this.file.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
            this.file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
            getContentDao().save(this.file);
        } catch (ContentMgmtException e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    	 */
    }

    /**
     * 
     * 
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRED)
    public void update() throws FileServerException {
        try {
        	getContentDao().save(this.file);
        	
        } catch (ContentMgmtException e) {
            logger.error(e);
            throw new FileServerException(e);
        }
    }

    // Spring
    @Override
    public KBFSService getKBFS2() {
        return this.kbfminio;
    }

 // Spring
    @Override
    public KBFSService getOdilon() {
        return this.kbfodilon;
    }
    
    public void setOdilon(KBFSService kbfs) {
        this.kbfodilon = kbfs;
    }
    
    
    public void setKBFS2(KBFSService kbfs2) {
        this.kbfminio = kbfs2;
    }

    @Override
    public KBFSService getKBFS1() {
        return kbfs1;
    }

    public void setKBFS1(KBFSService kbfs1) {
        this.kbfs1 = kbfs1;
    }

    @Override
    public KBFSService getKBFSAmazonS3() {
        return kbfsAmazonS3;
    }

    public void setKBFSAmazonS3(KBFSService kbfsAmazonS3) {
        this.kbfsAmazonS3 = kbfsAmazonS3;
    }

    @Override
    public KBFile getKBFile() {
        return file;
    }
    
    public Content getContent() {
    	return content;
    }

    @Override
    public KBFSService getKBFSService() {
        return getKBFSService(this.file.getStorageType());
    }

    
    public void setDefaultKBFSStorageType(KBFSStorageType type) {
        this.defaultStorageType = type;
    }

    /**
     * @return
     */
    public KBFSStorageType getDefaultKBFSStorageType() {

        if (this.defaultStorageType != null)
            return this.defaultStorageType;

        
        if (getKBFile()!=null && getKBFile().getDomain().getStorageType() != null) {
            this.defaultStorageType = getKBFile().getDomain().getStorageType();
            return this.defaultStorageType;
        }
        else {
            if (getContent()!=null && getContent().getDomain().getStorageType() != null) {
                this.defaultStorageType = getContent().getDomain().getStorageType();
                return this.defaultStorageType;
            }
        }

        this.defaultStorageType = KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));

        return this.defaultStorageType;
    }


    /**
     * @param type
     * @return
     */
    protected KBFSService getKBFSService(KBFSStorageType type) {
        if 		(type == KBFSStorageType.Minio) 		return this.kbfminio;
        else if (type == KBFSStorageType.Odilon) 		return kbfodilon;
        else if (type == KBFSStorageType.KBFS1) 		return kbfs1;
        else if (type == KBFSStorageType.MinioArchive) 	return kbfminio;
        else if (type == KBFSStorageType.AmazonS3) 	return kbfsAmazonS3;
        else if (type == KBFSStorageType.LocallyMapped) throw new KbeeRuntimeException("not implemented");
        else if (type == KBFSStorageType.External) 		throw new KbeeRuntimeException("not implemented");
        return kbfs1;
    }


    /**
     * 
     * ObjectName contains the path 
     * 
     * @param filename
     * @param bucket
     * @param objectName
     * @param stream
     * @throws IOException
     */
    private void uploadkbfs1(String filename, String bucket, String objectName, InputStream stream) throws IOException {
        FSOutputStream fos = null;							
        try {
            logger.debug("uploadkbfs1 " + filename);
            fos = ServiceLocator.getService(FileServerV1.class).getFSOutputStream(filename, objectName, bucket);
            try {
                @SuppressWarnings("unused")
                int tot = copy(stream, fos, 4096);
                
                file.setUrl(fos.getRelativeUrl());
                file.setObjectName(fos.getRelativeUrl());
                
                file.setBucketName(bucket);
                this.file.setShard(1);
                this.file.setFSID("File System");
                
                
                logger.debug("uploadkbfs1 done ok  | " + fos.getRelativeUrl());
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }

        } catch (IOException e) {
            logger.debug("uploadkbfs1 error ");
            logger.error(e);
            throw e;
        }
    }


    /**
     * @param in
     * @param out
     * @param bufSize
     * @return
     * @throws IOException
     */
    private int copy(final InputStream in, final FSOutputStream out, final int bufSize) throws IOException {
        final byte[] buffer = new byte[bufSize];
        int bytesCopied = 0;
        while (true) {
            int byteCount = in.read(buffer, 0, buffer.length);
            if (byteCount <= 0) {
                break;
            }
            out.write(buffer, 0, byteCount);
            bytesCopied += byteCount;
        }
        return bytesCopied;
    }

 
    
    private String getBucketName() {

        String ret;

        if (getDefaultKBFSStorageType() == KBFSStorageType.AmazonS3) {
        	//
        	// int buk = Math.abs(this.file.getDomain().getName().hashCode()) % 800;
            // String subbucket = String.format("%03d", buk);
        	// "prod" or "dev" - distribute domains across 800 buckets
            // ret = ((FileServerS3) getKBFSAmazonS3()).getEnvironment() + "-" + subbucket;              
        	// ------------------
        	// kbee-dev
        	// kbee-prod
        	// kbee-testing
        	// ------------------
        	//
        	ret = ((FileServerS3) getKBFSAmazonS3()).getEnvironment();
        
        } 
        else
            ret = ServiceLocator.getService(SystemPropertiesService.class).getServerIdPrefix() + this.file.getDomain().getName();

        logger.debug(ret);
        return ret;
    }

    /**
     * @param filename
     * @return
     */
    private String getObjectName(String filename) {

        OffsetDateTime no = OffsetDateTime.now();

        String ret;
        if (getDefaultKBFSStorageType() == KBFSStorageType.AmazonS3) {

            ret = String.valueOf(normalizeDomainName(this.file.getDomain().getName())) + "/" +
                    String.valueOf(no.getYear()) + "/" +
                    String.valueOf(no.getMonth().getValue()) + "/" +
                    String.valueOf(ServiceLocator.getService(ContentFactoryService.class).getNewResourceOId()) + "-" + internalnormalize(filename);
        }
        else if (getDefaultKBFSStorageType() == KBFSStorageType.Minio) { 
        
        	ret = String.valueOf(no.getYear()) + "/" +
                  String.valueOf(no.getMonth().getValue()) + "/" +
                  String.valueOf(ServiceLocator.getService(ContentFactoryService.class).getNewResourceOId()) + "-" + internalnormalize(filename);
        }
        
        else if (getDefaultKBFSStorageType() == KBFSStorageType.Odilon) {
        	
        	//
        	// Odilon -> Spring does not accept "/" as part of the url
        	//
        	String normalized = normalize(filename);
        	
        	ret = 	String.valueOf(no.getYear()) + "-" +
        				String.valueOf(no.getMonth().getValue()) + "-" +
        				//String.valueOf(ServiceLocator.getService(ContentFactoryService.class).getNewResourceOId()) + "-" + normalize(filename);
        				String.valueOf(ServiceLocator.getService(ContentFactoryService.class).getNewResourceOId()) + "-" + normalized;
        }
        else {
        		
        	ret = 	String.valueOf(no.getYear()) + "-" +
        				String.valueOf(no.getMonth().getValue()) + "-" +
        				String.valueOf(ServiceLocator.getService(ContentFactoryService.class).getNewResourceOId()) + "-" + internalnormalize(filename);
        }

        return ret;
        
    }


    private String normalizeDomainName(String name) {

        if (name == null)
            throw new IllegalArgumentException("domain name is null");

        String p = name.replaceAll("[ |\\t|\\s|(|)]", "")
        
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
                .replace("*", "")
                .replace("<", "")
                .replace(">", "")
                .replace("\\", "")
                .replace("+", "")
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")
                .replace("/", "")
                .replace("¡", "")
                .replace("?", "")
                .replace("=", "")
                .replace("}", "")
                .replace("{", "")
                .replace(":", "")
                .replace("Á,", "A")
                .replace("É,", "E")
                .replace("Í,", "I")
                .replace("Ó,", "O")
                .replace("Ú,", "U")
                .replace("Ñ", "N");
        
        
        if (p.length() < MAX_DOMAIN_NAME_LENGTH)
            return p;

        return p.substring(0, MAX_DOMAIN_NAME_LENGTH);

    }

    /**
     * @param str
     * @return
     */
    private String internalnormalize(String str) {
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
                .replace("*", "")
                .replace("<", "")
                .replace(">", "")
                .replace("\\", "")
                .replace("+", "")
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")
                .replace("/", "")
                .replace("¡", "")
                .replace("?", "")
                .replace("=", "")
                .replace("}", "")
                .replace("{", "")
                .replace(":", "")
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


    private EncryptionService getEncryptionService() {
        return (EncryptionService) ServiceLocator.getService(EncryptionService.class);
    }


    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao dao) {
        contentDao = dao;
    }

}

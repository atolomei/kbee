package com.novamens.kbee.content.service;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.UserImagesService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbfs.FileServerException;

import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.util.KbeeFileUtils;

import kbee.util.FSUtils;
import kbee.util.logging.Logger;


public class KbeeUserImagesService implements UserImagesService, EventListener  {
	
	static final int BUFFER_SIZE = 8192;
	//private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserImagesService.class.getName());

	private  static Logger logger = Logger.getLogger(KbeeUserImagesService.class.getName());
	
	
	private static  Logger startupLogger =  Logger.getLogger("StartupLogger");
 	
 	
 	
	private ContentDao dao;
	
	private boolean is_initilized = false;
	private Domain kbee = null;

	// TODO HA
	//
	private List<KBFile> images_list_x = null;
	
	private User kbee_root  =null;
	
	
    public KbeeUserImagesService() {
    	
	}
    

    @Transactional (propagation = Propagation.REQUIRED)
    public synchronized void startUp() {
		if (!isInitialized()) 
			start();
    }
    
    
    public KBFile getDefaultImage(String username) {

		if (username==null)
			return null;

		if (getImages()==null || getImages().isEmpty()) {
			return null;
		}
		
		long start = System.currentTimeMillis();
		
		String arr[]= username.split("@");
		String name = arr[0];
		int code = name.hashCode();
		int index = Math.abs(code) % getImages().size();
		
		logger.debug(" image assign: "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
		
		return getImages().get(index);
	}
	
    
    @Override
    public synchronized void evict() {
    	if (this.images_list_x!=null) {
    		
    		this.images_list_x.clear();
    		this.images_list_x=null;
    		
    	}
    }

    
	/**
	 *  
	 * IMPORTANT: The Hibernate Session must exist before calling this method.
	 * 
	 * 
	 */
	private void start() {
		try {
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			
			startupLogger.info("Setting up User Image Service.");
			
			if (getDomainKbee()==null || getKbeeRoot()==null) {
				startupLogger.error("-----------------------------------------------------------------");
				startupLogger.error("FATAL ERROR: Domain kbee or Kbee Root user does not exist.");
				startupLogger.error("-----------------------------------------------------------------");
				return;
			}
		
			/**
			for (PackageResourceReference ref: ServiceLocator.getService(BrandingWebService.class).getUserImages()) {
				if (!existsFileInKbee(ref.getName())) {
					ref.getResource().get;
					addFileToKbee(file);
				}
			}
			**/
			
			List<File> files_to_move=new ArrayList<File>();
			
			try {
				for (File file: getZipFiles()) {
					extractFile(file);
					files_to_move.add(file);
				}

			} catch (Exception e) {
				logger.error(e);
			}
			
			
			int counter=0;
			for (File file: getCandidateFiles()) {		
				if (!existsFileInKbee(file)) {
					try {
						addFileToKbee(file);
						files_to_move.add(file);
						counter++;
						if (counter % 100 == 99) 
							getContentDao().flush();
					} catch (Exception e) {
						startupLogger.error(e.getClass().getName() + ". " + (e.getMessage()!=null?e.getMessage():""));
					}
				}
				else
					files_to_move.add(file);
			}
		
			if (files_to_move==null || files_to_move.isEmpty())
				return;


			// Backup Files  ----------------------------------------------------------------------------
			//
			//
			File backup_base = new File(ServiceLocator.getService(ApplicationServerService.class).getImagesDir() + File.separator + "backup");
			
			if (backup_base.exists() && !backup_base.isDirectory()) {
				try {
					startupLogger.info("Deleting File" + backup_base);
					KbeeFileUtils.forceDelete(backup_base);
				} catch (IOException  e) {
					startupLogger.error( e);
				}
			}
			
			if (!backup_base.exists()) {
				try {
					startupLogger.info("Creating Directory " + backup_base);
					KbeeFileUtils.forceMkdir(backup_base);
					
				} catch (Exception e) {
					startupLogger.error(e);
				}
			}

			
			
			for (File file: files_to_move) {
				File dest =  new File(ServiceLocator.getService(ApplicationServerService.class).getImagesDir() + File.separator + "backup" + File.separator + file.getName());
				if (dest.exists()) {
					try {
						startupLogger.info("Deleting " + dest);
						KbeeFileUtils.forceDelete(dest);
					} catch (Exception e) {
						startupLogger.error(e);
					}
				}
				try {
					FileUtils.moveFile(file, dest);
				} catch (Exception e) {
					startupLogger.error(e);
				}
			}
		}
		finally {
			 	evict();
				setInitialized(true);
		}
	}
	
	

	private List<File> getZipFiles() {
		
		List<File> files = new ArrayList<File>();
		File base = new File(ServiceLocator.getService(ApplicationServerService.class).getImagesDir());
		
		if (!base.exists() || !base.isDirectory())
			return files;
		
		File arrfiles [] = base.listFiles();
		
		for (File file: arrfiles) {
				if (file.isFile()) {
							if (FilenameUtils.isExtension(file.getName(), "zip")) {
								files.add(file);
								startupLogger.info("adding zip -> " + file.getAbsolutePath());		
							}
				}
		}
		startupLogger.info("Total zip: "+ String.valueOf(files.size()));
		return files;
	}

	
	private void extractFile(File sfile) {
		
		File fileZip = sfile;
		File destDir = new File(ServiceLocator.getService(ApplicationServerService.class).getImagesDir());
		
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        
        startupLogger.debug(" zip -> " + sfile.getName());
        
		try {
				zis = new ZipInputStream(new FileInputStream(fileZip));
				
		        ZipEntry zipEntry = zis.getNextEntry();
		        while (zipEntry != null) {
		        	
		        	File newFile = newFile(destDir, zipEntry);
		        	
		            if (zipEntry.isDirectory()) {
		                if (!newFile.isDirectory() && !newFile.mkdirs()) {
		                    throw new IOException("Failed to create directory " + newFile);
		                }
		            } else {
		                // fix for Windows-created archives
		                File parent = newFile.getParentFile();
		                if (!parent.isDirectory() && !parent.mkdirs()) {
		                    throw new IOException("Failed to create directory " + parent);
		                }
		                
		                
		                // write file content
		                startupLogger.debug("extracting -> " + newFile.getAbsolutePath());
		                
		                FileOutputStream fos = new FileOutputStream(newFile);
		                int len;
		                while ((len = zis.read(buffer)) > 0) {
		                    fos.write(buffer, 0, len);
		                }
		                fos.close();
		            }
		            zipEntry = zis.getNextEntry();
		        }
		        zis.closeEntry();
		        // zis.close();
		        
		} catch (FileNotFoundException e) {
			startupLogger.error(e);
			
		} catch (IOException e) {
			startupLogger.error(e);
		}
		finally {
			if (zis!=null) {
				try {
					zis.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}


	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {

		File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }
	    return destFile;
	}
	



	
	
	private List<File> getCandidateFiles() {
		
		List<File> files = new ArrayList<File>();
		File base = new File(ServiceLocator.getService(ApplicationServerService.class).getImagesDir());
		
		if (!base.exists() || !base.isDirectory())
			return files;
		
		File arrfiles [] = base.listFiles();
		
		for (File file: arrfiles) {
				if (file.isFile()) {
					if (FSUtils.isImage(file)) {
						files.add(file);
						startupLogger.info("adding candidate " + file.getAbsolutePath());
					}
				}
		}
		
		startupLogger.info("Total candidates: "+ String.valueOf(files.size()));

		return files;
	}
    

	/**
	 * @param file
	 * @return
	 * 
	 */
	private boolean existsFileInKbee(File file) {
		String name = file.getName();
		KBFile kfile = (KBFile) getContentDao().findResourceByName(KBFileImpl.class, name, getDomainKbee().getId());
		return (kfile != null);
	}
	
//	private boolean existsFileInKbee(String name) {
//		KBFile kfile = (KBFile) getContentDao().findResourceByName(KBFileImpl.class, name, getDomainKbee().getId());
//		return (kfile != null);
//	}
//
//	
//	/**
//	 *  
//	 * @param file
//	 * @throws IOException
//	 */
//	
//	private void setWH(KBFileImpl file) throws IOException {
//		if (FSUtils.isImage(file.getFile())) {
//			SimpleImageInfo imageInfo;
//			int nw, nh;
//			try {
//				imageInfo = new SimpleImageInfo(file.getFile());
//				nw  = imageInfo.getWidth();
//				nh = imageInfo.getHeight();
//			}
//			catch (IOException e) {
//				nw = 0;
//				nh = 0;
//			}
//			file.setWidth(nw);
//			file.setHeight(nh);
//		}
//	}

	

	/**
	 * @param file
	 */
	private void addFileToKbee(File file) throws ContentMgmtException, IOException {
		
		String file_name = file.getName();
		KBFileImpl kb_file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(file.getName());
		kb_file.setDomain(getDomainKbee());
		kb_file.setCreationOffsetDateTime(OffsetDateTime.now());
		kb_file.setLastModifiedUser(getSessionUser());
		kb_file.setCreationOffsetDateTime(OffsetDateTime.now());
		kb_file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		kb_file.setUploadOffsetDateTime(OffsetDateTime.now());
		
		String title = FilenameUtils.getBaseName(file.getName()).replaceAll("(-|_)", " ");
		kb_file.setTitle(title);
		kb_file.setState(ObjectState.ENABLED);
		
		startupLogger.debug("adding to kbee: " + file.getName());
		
		BufferedInputStream stream = null;

		boolean isok=false;
		try {
			stream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
			kb_file.getService(KBFSResourceService.class).putObject(file_name, stream);
			isok=true;
			
		} catch (FileServerException | ServiceNotFoundException e) {
			startupLogger.error(e.getClass().getName()+ " | " + e.getMessage());
			throw new ContentMgmtException(e);
		}
			catch (Exception e) {
				startupLogger.error(e.getClass().getName()+ " | " + e.getMessage());
				throw new ContentMgmtException(e);
		}
		finally {
			if (stream!=null)
				stream.close();
			
			if (isok) {
				try {
					getContentDao().save(kb_file);
					startupLogger.debug("saved: " + kb_file.getTitle());
				} catch (Exception e) {
					startupLogger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				}
			}
		}
  	}

	 
	public void setInitialized(boolean ini) {
		this.is_initilized=ini;
	}
	
 
	@Override
	public boolean isInitialized() {
		return is_initilized;
	}


	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
			if (event instanceof EvictCacheServiceEvent)
				this.evict();
	}
	
	
	private ContentDao getContentDao() {
		if (dao==null)	 {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 dao = (ContentDao) beans.getBean("contentDao");
		 }
		return dao;
	}
	
 	private Domain getDomainKbee() {
		if (this.kbee==null) {
			this.kbee = getContentDao().findDomainByName("kbee"); 
		}
		return this.kbee;
	}
	
 	private User getKbeeRoot() {
		if (this.kbee_root==null) 
			this.kbee_root = getDomainKbee().getService(DomainService.class).getRootUser();
		return this.kbee_root;
	}
	
	
 	@Override
    public List<KBFile> getImages() {
    	if (this.images_list_x==null) {
    		    
    		long start = System.currentTimeMillis();
    		
    			this.images_list_x = new ArrayList<KBFile>();
    			
    			for (KBFile im: getContentDao().getDefaultUserImages()) {
    				try {
    					logger.debug("checking :" + im.getTitle());
	    				if ((im.isBinaryFile()) && (im.getFile().exists()))
	    					this.images_list_x.add(im);
    				} catch (Exception e) {
    					logger.warn((im.getTitle()!=null?im.getTitle():"")+ " " + e.getClass().getName());
    				}
    			}
    			
    			startupLogger.debug("Total User Images available: "+ String.valueOf(this.images_list_x.size()));
    			startupLogger.debug("Time : "+ String.valueOf(System.currentTimeMillis()-start)+ " ms");
    			
    			return this.images_list_x;
    	}
    	return this.images_list_x;
    }
	

	private User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}


}

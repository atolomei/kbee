 package com.novamens.kbee.content.webapi.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.util.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.Classificable;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.EventService;
import com.novamens.kbee.content.event.AppCheckinEvent;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.kbfs.FileServerException;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.util.KbeeRuntimeException;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.IBinaryResource;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.PropertiesFactory;

/**
 * 
 * 
 * 28/10/2022 SHA256 reemplaza a CRC32
 *
 */
public class BinaryFileUpdateHandler extends FileUpdateAbstractHandler {

	static final String contentTemplate = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.webapi.contenttemplate", "File");
	
	static private Logger logger = LogManager.getLogger(BinaryFileUpdateHandler.class.getName());
	static private kbee.util.logging.Logger klogger = new kbee.util.logging.Logger(logger);
	

	@Transactional
	public ITransaction update(ApiFile file) {
		try {
			su(getDomain(file));
			
			lock(file);
		
			Content content = getOrCreateExternalContent(file);
			
			List<String> updates = update(content, file);
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			
			if (!content.isHeadVersion()) {
				content.getService(ContentService.class).checkin();
			}
			else {
				ServiceLocator.getService(EventService.class).fire(new AppCheckinEvent(content));
			}
			
			ITransaction transaction  = getTransaction(getProxy(file));
			
			return transaction;
		}
		catch (ApiException e) {
			logger.error(e);
			throw e;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		finally {
			unlock(file);
		}
	}
	
	/** 
	 * 
	 * 
	 */
	@Transactional
	public ITransaction update(ApiFile file, InputStream stream) {
		try {
			su(getDomain(file));
			
			lock(file);
			
			Content content = getOrCreateExternalContent(file);
			
			List<String> updates = update(content, file);
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			if (file.getResources().size()==1) {
				updates.addAll(setResource(content, file.getResources().get(0) , stream));
			}
			else {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.RESOURCE_NOT_FOUND);
			}
			
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			 
			if (!content.isHeadVersion()) {
				content.getService(ContentService.class).checkin();
			}
			
			ITransaction transaction = getTransaction(getProxy(file));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (IOException | ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		finally {
			unlock(file);
		}
	}
	
	/**
	 * 
	 * 
	 */
	@Transactional
	public ITransaction zipupdate(ApiFile file, InputStream stream) {
		try {
			su(getDomain(file));
			
			lock(file);
			
			Content content = getOrCreateExternalContent(file);
			
			List<String> updates = update(content, file);
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			if (file.getResources().size()==1) {
				updates.addAll(setZipResources(content, stream));
			}
			else {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.RESOURCE_NOT_FOUND);
			}
			
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			 
			if (!content.isHeadVersion()) {
				content.getService(ContentService.class).checkin();
			}
			
			ITransaction transaction = getTransaction(getProxy(file));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (IOException | ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		finally {
			unlock(file);
		}
	}
	
	/**
	 * 
	 * 
	 */
	protected Domain getDomain(ApiObject file) {
		Domain domain = super.getDomain(file);
		if (domain == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
		}
		return domain;
	}
	
	/**



	 */
	private List<String> update(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		
		if (!isWriteable(content)) {
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
		}
		
		updates.addAll(setTitle(content, file));
			
		updates.addAll(setAttributes((Classificable)content, file));
		
		updates.addAll(setCustomAttributes(content, file));

		return updates;
	}
	
	/**
	 */
	private List<String> setResource(Content content, ApiResource resource, InputStream stream) throws IOException, ContentMgmtException {
		List<String> updates = new ArrayList<String>();
		List<String> resources = new ArrayList<String>();
		KBFile kbfile = getFile(resource, stream);
		updates.addAll(addFile(content, kbfile));
		resources.add(resource.getName());
		updates.addAll(removeResources(content, resources));
		return updates;
	}
	
	/**
	 */
	private List<String> setZipResources(Content content, InputStream stream) throws IOException, ContentMgmtException {
		List<String> updates = new ArrayList<String>();
		
		List<String> resources = new ArrayList<String>();
		
		ZipInputStream zipstream = getZipStream(stream);
		
		ZipEntry zipentry = zipstream.getNextEntry();
		
		while (zipentry!=null) {
			File file = getZipEntry(zipstream);
			
			IBinaryResource resource = new IBinaryResource();
			resource.setName(zipentry.getName());
			
			KBFile kbfile = getFile(resource, new FileInputStream(file));
			
			updates.addAll(addFile(content, kbfile));
			
			resources.add(resource.getName());
			
			zipentry = zipstream.getNextEntry();
		}
		
		updates.addAll(removeResources(content, resources));
		
		return updates;
	}
	
	/**
	 */
	private List<String> addFile(Content content, KBFile kbfile) throws IOException, ContentMgmtException {
		List<String> updates = new ArrayList<String>();
		boolean addfile = true;
		for (KBFile resourcefile : ((ResourceContainer)content).getFiles()) {
			if (resourcefile.getName().equals(kbfile.getName())) {
				if (!equals(resourcefile, kbfile)) {
					((ResourceContainer)content).removeFile(resourcefile);
				}
				else {
					try {
						KBFSResourceService service = kbfile.getService(KBFSResourceService.class);
						service.removeObject();
						getContentDao().delete(kbfile);
						addfile = false;
					}
					catch (FileServerException e) {
						logger.error(e);
						throw new IOException(e);
					}
				}
				break;
			}
		}
		if (addfile) {
			((ResourceContainer)content).addFile(kbfile);
			updates.add("Add "+kbfile.getName());
		}
		return updates;
	}
	
	/**
	 * 
	 */
	private List<String> removeResources(Content content, List<String> names)  {
		List<String> updates = new ArrayList<String>();
		for (KBFile resourcefile : ((ResourceContainer)content).getFiles()) {
			boolean found = false;
			for (String resourcename : names) {
				if (resourcefile.getName().equals(resourcename)) {
					found = true;
					break;
				}
			}
			if (!found) {
				((ResourceContainer)content).removeFile(resourcefile);
				updates.add("Remove "+resourcefile.getName());
			}
		}	
		return updates;
	}

	/**
	 * 
	 */
	private KBFile getFile(ApiResource resource, InputStream stream) throws IOException {
		try {
			
			String filepath = new String(resource.getName().getBytes("Windows-1252"), "UTF-8");
			KBFileImpl file = new KBFileImpl();
			
			
			file.setOId(ServiceLocator.getService(ContentFactoryService.class).getResourceNewOId());
			
			file.setLastModifiedUser(getUser());
			file.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			file.setDomain(getDomain());
			file.setUploadUser(getUser());
			file.setUploadOffsetDateTime(OffsetDateTime.now());
			file.setState(ObjectState.ENABLED);
			
			// KBFS V1, V2 
			InputStream is = null;
			try {
				file.getService(KBFSResourceService.class).putObject(filepath, stream);
			} 
			catch (FileServerException | ServiceNotFoundException e) {
				klogger.error(e);
			} 
			finally {
				if (is!=null)
					IOUtils.closeQuietly(is);
			}
			
			getCurrentSession().save(file);
			
			return file;
		}
		finally {
		}
	}
	
	/**
	 * 
	 */
	private ZipInputStream getZipStream(InputStream stream) throws IOException {
		FileOutputStream  out = null;
		try {
			File zipfile = File.createTempFile("temp", ".zip");
			zipfile.deleteOnExit();
			out = new FileOutputStream(zipfile);
			int len;
			byte[] buffer = new byte[2048];
			while ((len = stream.read(buffer)) > 0)	{
				out.write(buffer, 0, len);
			}
			return new ZipInputStream(new FileInputStream(zipfile));
		}
		finally {
			if (out!=null) out.close();
		}
	}
	
	/**


	 */
	private File getZipEntry(ZipInputStream zipstream) throws IOException {
		FileOutputStream  out = null;
		try {
			File file = File.createTempFile("temp", ".tmp");
			file.deleteOnExit();
			int len;
			out = new FileOutputStream(file);
			byte[] buffer2 = new byte[2048];
			while ((len = zipstream.read(buffer2)) > 0) {
				out.write(buffer2, 0, len);
			}
			return file;
		}
		finally {
			if (out!=null) out.close();
		}
	}

	
	/** ------------------------------------------------------------------------------------------------------------------------
	 *  
	 *  VER SHA256 
	 *  con el que ya esta calculado
	 *  
	 */
	private boolean equals(KBFile file1, KBFile file2) {
		
		
		
		if (file1.getSize()!=file2.getSize()) 
			return false;
		
		
		String sha1 = getSHA256(file1);
		String sha2 = getSHA256(file2);
		
		if (sha1==null && sha2==null) {
			logger.error("SHA is null for BOTH files -> " + file1.getOId().toString() + " and " + file2.getId().toString());
			return true;
		}
			
		if (sha1!=null && sha2!=null)
			return sha1.equals(sha2);
		
		
		logger.error("SHA is null for ONE of the files -> " + file1.getOId().toString() + " and " + file2.getId().toString());
		return false;
		
		
		//logger.debug("Usign CRC");
		//long crc1 = getCRC(file1);
		//long crc2 = getCRC(file2);
		//return crc1==crc2;
	}
	
	
	private String getSHA256(KBFile file) {
		try {
			
			String sha256 = file.getSHA256();		
			return sha256;
			
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 
	private long getCRC(KBFile file) {
		try {
			
			long crc32 = org.apache.commons.io.FileUtils.checksumCRC32(file.getFile());
			return crc32;
		}
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
	}
	*/
	
	/** 
	 */
	private void lock(ApiFile file) {
		if (file.getExternalId()==null || "".equals(file.getExternalId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ATTRIBUTE_IS_REQUIRED, "externalid");
		}
		ServiceLocator.getService(ValueLockerService.class).lock(file.getExternalId());
	}
	
	/** 
	 */
	private void unlock(ApiFile file) {
		if (file.getExternalId()==null || "".equals(file.getExternalId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ATTRIBUTE_IS_REQUIRED, "externalid");
		}
		ServiceLocator.getService(ValueLockerService.class).unlock(file.getExternalId());
	}
	
	/** 
	 */
	public String getContentTemplate(ApiFile file) {
		String template = file.getClassName();
		if (template==null) {
			template = SystemParameters.get("com.novamens.content.webapi.contenttemplate", "File");
		}
		return template;
	}
	
	/** 
	 */
	private Session getCurrentSession() {
		return getSessionFactory().getCurrentSession();	
	}
	
	/** 
	 */
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}

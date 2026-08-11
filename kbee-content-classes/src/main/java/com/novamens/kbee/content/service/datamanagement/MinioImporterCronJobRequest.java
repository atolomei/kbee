package com.novamens.kbee.content.service.datamanagement;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;
			
public class MinioImporterCronJobRequest extends AbstractCronJobRequest {
														
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(MinioImporterCronJobRequest.class.getName());
	
	private int max = 0;
	
	public MinioImporterCronJobRequest() {
		setName("MinioImporterCronJobRequest");
		setDescription("Imports KBFiles into Minio.");
	}

	/**
	 * 
	 * 
	 * BUCKET
	 * ------
	 * 
	 * "kbee"
	 * 
	 * "domain/test"
	 * "domain/data"
	 * "domain/data"
	 * "domain/work"
	 * "domain/export"
	 * 
	 * OBJECTKEY
	 * ---------
	 * 123456782018
	 * 000043452018
	 * 000043452019
 	 * 
	 */
	@Override
	public void execute() {
		
		logger.debug("Executing " + this.toString());

		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		
		FileServerMinio minio = getFileServerMinio();
		
		List<KBFile> list = null;
		
		try {
			
			list = getContentDao().getKBFilesFromFileServer("kbee", 5);

			if (list==null || list.isEmpty())
				return;

			
		} catch (ContentMgmtException e) {
			logger.error(e.getStackTrace());
		}

		
		for (KBFile kbfile: list) {
			try {
				
				java.io.File file = kbfile.getFile();
				
				
				if (file.exists() && !file.isDirectory()) {
										
					String basename = FilenameUtils.getBaseName(file.getName());
					String extension = FilenameUtils.getExtension(file.getName());
					normalize(basename);
					
					if (basename==null)
						basename="nomame";
					
					if (extension==null)
						extension="";
					else
						extension="." + extension;
				
					String bucketName = kbfile.getDomain().getName();
					
					String idpad = String.format("%08d", ((Long) kbfile.getId()).longValue());
					
					String objectName = basename + "-" +   extension;
					String fileName = FilenameUtils.getName(file.getName());
					
					BufferedInputStream	inputStream = null;
					
					try {

						inputStream = new BufferedInputStream(new FileInputStream(file));
						minio.putObject(bucketName, objectName, fileName, inputStream, file.length(),  getContentType(file.getName()));

						//kbfile.setBucket(bucketName);
						//kbfile.setObjectKey(bucketName);

					} catch (FileNotFoundException e) {
						logger.error(e.getStackTrace());
					}
					finally {
						if (inputStream!=null) { 
							try {
								inputStream.close();
							} catch (IOException e) {
								logger.error(e.getStackTrace());
							}
						}
					}
				}
				
			} catch (Exception e) {
				
			}
		}
		logger.debug("done.");
		
	}

	private String normalize(String str) {
		 String p1 = str;
		 return  p1.replaceAll("[ |\\t|(|)]", "")
				 .replace("á", "a")
				 .replace("é", "e")
				 .replace("í", "i")
				 .replace("ó", "o")
				 .replace("ú", "u")
				 .replace("ñ", "n")
				 .replace(";", "")
				 .replace(":", "");
	 }
	
	private  FileServerMinio getFileServerMinio() {
		return (FileServerMinio) ServiceLocator.getService(FileServerMinio.class);
	}
	
	private  FileServerV1 getFileServerV1() {
		return ServiceLocator.getService(FileServerV1.class);
	}

	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/**
	 * 
	 * @param src
	 * @return
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

	
}

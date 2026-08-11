package com.novamens.kbee.content.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.util.io.IOUtils;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.FileService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.ObjectState;
import com.novamens.file.PdfService;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeSignedFile;
import com.novamens.kbfs.FileServerException;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.signature.SignatureException;

import kbee.util.FSUtils;

public class KbeeFileService extends KbeeResourceService implements FileService {
			
	private KBFile file;
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFileService.class.getName());

	public KbeeFileService() {
	}
	
	public KbeeFileService(KBFile file) {
		super(file);
		this.file = file;
	}

	public KBFile getFile() {
		return file;
	}
		
//	@Override
//	public File getThumbnailFile(ThumbnailSize size) throws IOException {
//		
//		KBFile kbfile = getFile();
//		
//		String domain_name="";
//		
//		try {									
//			ThumbnailService ths = ServiceLocator.getService(ThumbnailService.class);
//			if (kbfile!=null) {
//				domain_name=kbfile.getDomain().getName();
//				return ths.getThumbnailFile(kbfile.getId().toString(), domain_name, kbfile.getFile(), size);
//			}
//			throw new RuntimeException("File is null");
//		} 
//		catch (IOException e) {
//			logger.debug(e);
//			throw e;
//		}
//	}
//	
//	public InputStream getThumbnail(ThumbnailSize size) throws IOException {
//		KBFile kbfile = getFile();
//		try {
//			File file = null;
//			file = ServiceLocator.getService(ThumbnailService.class).getThumbnailFile(kbfile.getId().toString(),  kbfile.getDomain().getName(), kbfile.getFile(), size);
//			return new FileInputStream(file);
//		} 
//		catch (IOException e) {
//			logger.debug(e);
//			throw (e);
//		}	
//	}
	
	@Override
	public KBFile setVersion(KBFile version)  {
		
		int fileversion = getFile().getVersion();
		
		if (fileversion==0) {
			((KBFileImpl)file).setVersion(1);
			fileversion = 1;
		}
		
		KBFileImpl kbversion = (KBFileImpl)version;
		
		kbversion.setPreviousVersion(getFile());
		kbversion.setVersion(fileversion+1);
		kbversion.setOId(getFile().getOId());
		
		getContentDao().saveTX(kbversion);
		
		return version;
	}

	
	@Override
	public KBFile sign(UserSignature signature, String signaturestream) throws SignatureException {
		try {
			KBFile kbfile = getFile();
			if (!FSUtils.isPdf(kbfile.getName())) return null;
			String filename = getSignedName(kbfile);
			File temp = File.createTempFile(kbfile.getName(), ".pdf");
			File signedtemp = File.createTempFile(filename, ".pdf");
			
		    FileUtils.copyFile(kbfile.getFile(), temp);			
			
		    OutputStream signedoutput = new FileOutputStream(signedtemp);
			Certificate certificate = signature.getCertificate();
			PrivateKey privateKey = signature.getPrivateKey();
			Certificate caCertificate = kbfile.getDomain().getCertificate();
			//InputStream input = kbfile.getInputStream();
			ServiceLocator.getService(PdfService.class).sign(temp, caCertificate, certificate, privateKey, signedoutput, signaturestream);
			//ServiceLocator.getService(PdfService.class).sign(input, caCertificate, certificate, privateKey, signedoutput);
			signedoutput.close();
			
			KBFileImpl version = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filename);
			version.setDomain(kbfile.getDomain());
			version.setLocalPath(kbfile.getLocalPath());
			version.setName(filename);
			version.setTitle(kbfile.getTitle());
			version.setState(ObjectState.ENABLED);
			version.setCreationOffsetDateTime(OffsetDateTime.now());
			version.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
			version.setUploadOffsetDateTime(OffsetDateTime.now());

			InputStream is = null;
			try {
				is = new FileInputStream(signedtemp);
				version.getService(KBFSResourceService.class).putObject(filename, is);
			} 
			catch (FileServerException | ServiceNotFoundException e) {
				logger.error(e);
				throw new IOException(e);
			} 
			finally {
				if (is!=null)
					IOUtils.closeQuietly(is);
			} 
			
			version.setPreviousVersion(kbfile);
			version.setVersion(kbfile.getVersion()+1);
			version.setSigned(true);
			version.setSignature(new KbeeSignedFile(signature));
			version.setOId(kbfile.getOId());
			
			getContentDao().saveTX(version);
			
			signedtemp.delete();
			temp.delete();
			
			return version;
		}
		catch(IOException e) {
			throw new SignatureException(e);
		}
	}

	public KBFile getSigned(UserSignature signature, String signaturestream) throws IOException {
//		try {
			KBFile kbfile = getFile();
			if (!FSUtils.isPdf(kbfile.getName())) return null;
			String filename = getSignedName(kbfile);
			File temp = File.createTempFile(kbfile.getName(), ".pdf");
			File signedtemp = File.createTempFile(filename, ".pdf");
			
		    FileUtils.copyFile(kbfile.getFile(), temp);			
			
		    OutputStream signedoutput = new FileOutputStream(signedtemp);
			ServiceLocator.getService(PdfService.class).getSigned(temp, signedoutput, signaturestream);
			signedoutput.close();
			
			KBFileImpl signed = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filename);
			signed.setDomain(kbfile.getDomain());
			signed.setLocalPath(kbfile.getLocalPath());
			signed.setName(filename);
			signed.setTitle(kbfile.getTitle());
			signed.setState(ObjectState.ENABLED);
			signed.setCreationOffsetDateTime(OffsetDateTime.now());
			signed.setLastModifiedUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
			signed.setUploadOffsetDateTime(OffsetDateTime.now());

			InputStream is = null;
			try {
				is = new FileInputStream(signedtemp);
				signed.getService(KBFSResourceService.class).putObject(filename, is);
			} 
			catch (FileServerException | ServiceNotFoundException e) {
				logger.error(e);
				throw new IOException(e);
			} 
			finally {
				if (is!=null)
					IOUtils.closeQuietly(is);
			} 
			
			signed.setOId(kbfile.getOId());
			
			getContentDao().saveTX(signed);
			
			signedtemp.delete();
			temp.delete();
			
			return signed;
//		}
//		catch(IOException e) {
//			throw new SignatureException(e);
//		}
	}
	
	@Override
	public KBFSResourceService getKBFSService() {
		return file.getService(KBFSResourceService.class);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getSignedName(KBFile file) {
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		String prefix = filename.substring(0, i);
		String signedname = prefix+"_signed.pdf";
		return signedname;
	}
	
}

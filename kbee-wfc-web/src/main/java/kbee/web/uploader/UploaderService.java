package kbee.web.uploader;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.util.io.IOUtils;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.kbfs.LengthAwareInputStreamWrapper;
import com.novamens.kbfs.FileServerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.service.SystemService;

import kbee.util.logging.Logger;

public class UploaderService implements SystemService {
	
	
	private static Logger logger = Logger.getLogger(UploaderService.class.getName());

	public KBFile upload(String name, String path, InputStream stream, long contentLength) 
		throws IOException {
		
		User user = ServiceLocator
			.getService(SecurityService.class)
			.getSessionUser();
		Domain domain = ServiceLocator
			.getService(UserService.class)
			.getSessionUserProfile()
			.getDomain();
		
		KBFileImpl kbfile = (KBFileImpl) ServiceLocator
			.getService(ContentFactoryService.class)
			.createKBFile(name);
		kbfile.setDomain(domain);
		kbfile.setLocalPath(path);
		kbfile.setName(name);
		String title = FilenameUtils.getBaseName(name);
		
		title= ((title!=null &&title.length()>1) ? title.replace(".", "") : "-");
		kbfile.setTitle(title);
		kbfile.setState(ObjectState.ENABLED);
		kbfile.setCreationOffsetDateTime(OffsetDateTime.now());
		kbfile.setLastModifiedUser(user);
		kbfile.setUploadOffsetDateTime(OffsetDateTime.now());
		
		try {
			InputStream streamproxy = contentLength>0 
				? new LengthAwareInputStreamWrapper(stream, contentLength)
				: stream;		
			kbfile.getService(KBFSResourceService.class).putObject(name, streamproxy);
			(getContentDao()).saveTX(kbfile);
		} 
		catch (FileServerException | ServiceNotFoundException e) {
			logger.error(e);
			throw new IOException(e);
		} 
		finally {
			if (stream!=null) {
				IOUtils.closeQuietly(stream);
			}	
		}
		
		return kbfile;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

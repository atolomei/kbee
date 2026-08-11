package kbee.replica;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.wicket.util.io.IOUtils;

import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbfs.FileServerException;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

import kbee.api.model.ApiResource;

public class KBFileReplicaHandler extends AbstractReplicaHandler<ApiResource, KBFileImpl> {

	public KBFileReplicaHandler(Replica replica, ApiResource iresource) {
		super(replica, iresource);
	}
	
	@Override
	protected void replicateIn(KBFileImpl file) throws ReplicaException {
		ApiResource resource = getObject();

		if (file.getBucketName()==null) {
		
			BufferedInputStream reader = null;
		
			try {
			
				reader = new BufferedInputStream(getReplicaApi().getResource(resource.getHRef()));
				String filepath = new String(resource.getName().getBytes("Windows-1252"), "UTF-8");
			
				// KBFileImpl file = new KBFileImpl();
				//KBFileImpl file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filepath);
				//file.setOId(ServiceLocator.getService(ContentFactoryService.class).getResourceNewOId());
			
				file.setName(filepath);
				file.setInPortalVersion(true);
			
				file.setCreationOffsetDateTime(resource.getLastModifiedDate());
				//file.setLastModifiedUser(getLocalUser(resource.getLastModifiedUser()));
				file.setLastModifiedUser(getSessionUser());
				file.setLastModifiedOffsetDateTime(resource.getLastModifiedDate());
				//file.setUploadUser(getLocalUser(resource.getLastModifiedUser()));
				file.setUploadUser(getSessionUser());
				file.setDomain(getSessionDomain());
				file.setState(ObjectState.ENABLED);
			
				try {
					file.getService(KBFSResourceService.class).putObject(filepath, reader);
				} 
				catch (FileServerException | ServiceNotFoundException e) {
					logger.error(e);
					throw new ReplicaException(e);
				} 
				finally {
					if (reader!=null)
						IOUtils.closeQuietly(reader);
				}
			}
			catch (Exception e) {
				throw new ReplicaException(e);
			}
			finally {
				try {
					if (reader!=null)
						reader.close();
				}
				catch (IOException e) {
					throw new ReplicaException(e);
				}
			}
		}		
		update(file);
	}
	
	@Override
	protected boolean replicable (KBFileImpl local) {
		return true;
	}

	@Override
	protected KBFileImpl createLocal() throws ReplicaException {
		try {
			ApiResource resource = getObject();
			String filepath = new String(resource.getName().getBytes("Windows-1252"), "UTF-8");
			KBFileImpl file = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filepath);
			file.setOId(ServiceLocator.getService(ContentFactoryService.class).getResourceNewOId());
			update(file);
			return file;
		}
		catch (Exception e) {
			throw new ReplicaException(e);
		}
	}
	
}

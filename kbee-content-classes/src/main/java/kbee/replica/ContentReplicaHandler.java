package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiResource;
import kbee.api.model.IResourceTag;

public class ContentReplicaHandler extends ClassificableReplicaHandler<ApiFile, KbeeIDoc> {

	KbeeIDoc local = null;
	
	public ContentReplicaHandler(Replica replica, ApiFile ifile) {
		super(replica, ifile);
	}
	
	@Override
	protected void replicateIn(KbeeIDoc local) throws ReplicaException {
		ApiFile remote = getObject();
		local.setTitle(remote.getDisplayName());
//		local.setVersion(remote.getVersion());
//		if (remote.getWorkspace()!=null) {
//			local.setHeadVersion(false);
//		}
//		else {
//			local.setHeadVersion(true);
//			local.setWorkspace(null);
//		}
		
		if (remote.getState().equals("ARCHIVED"))
			local.setState(ObjectState.ARCHIVED);
		if (remote.getState().equals("DELETED"))
			local.setState(ObjectState.DELETED);
		
//		local.setLastModifiedUser(getLocalUser(remote.getLastModifiedUser()));
		
//		local.setLastModifiedUser(getSessionUser());
	//	local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
				
		syncClassifiers(remote, local, getClassifiers(local.getContentTemplate()));
		syncAttributes(remote, local, local.getContentTemplate().getAttributes());
		
		for(ApiResource resource : remote.getResources()) {
			if ("file".equals(resource.getRel())) {
				KBFileImpl localfile = replicated(KBFileImpl.class, resource);
				
				ResourceTag tag = null;
				if (resource.getTag()!=null) {
					IResourceTag itag = getReplicaApi().getResourceTag(resource.getTag().getId());
					tag = replicated(KbeeResourceTag.class, itag);
				}
				if (!containsresource(local, localfile)) {
					if (tag!=null)
						local.addFile(localfile, tag);
					else
						local.addFile(localfile);
				}
				else {
					if (tag!=null)
						local.setTag(localfile, tag);
				}
			}
			else {
//				KbeeExternalResource localresource = getLocal(KbeeExternalResource.class, resource);
//				if (localresource==null || resource.getLastModifiedDate().isAfter(localresource.getLastModifiedOffsetDateTime())) {
//					if (localresource==null) { 
//						localresource = createExternal();
//						localresource.setDomain(local.getDomain());
//						syncResource(resource, localresource);
//						update(localresource);
//						setLocal(resource, localresource);
//					}
//					syncResource(resource, localresource);
//					update(localresource);
//					if (!containsresource(local, localresource)) {
//						local.addResource(localresource);
//					}
//				}
			}
		}
	
		update(local);
		
		local.getService(ContentService.class).checkin();
	}
	
	@Override
	public KbeeIDoc getLocal() {
		if (local!=null) {
			return local;
		}			
		KbeeIDoc idoc = super.getLocal();
		if (idoc!=null) {
			if (idoc.getWorkspace()==null) {
				idoc = (KbeeIDoc)idoc.getService(ContentService.class).checkout();
			}
			else {
				if (!idoc.getWorkspace().equals(getSessionUser().getId())) {
					throw new RuntimeException("locked");
				}
			}
		}
		getContentDao().flush();
		local = idoc;
		return idoc;
	}

	@Override
	protected KbeeIDoc createLocal() {
		KbeeIDoc idoc = (KbeeIDoc)ServiceLocator.getService(ContentFactoryService.class).create(getObject().getClassName());
		return idoc;
	}
	
	protected boolean replicable(KbeeIDoc local) {
		return true;
	}
	
	private boolean containsresource(KbeeIDoc idoc, Resource file) {
		for (KBFile idocfile : idoc.getFiles()) {
			if (idocfile.getId().equals(file.getId())) {
				return true;
			}
		}
		return false;
	}
	
	private List<Classifier> getClassifiers(ContentTemplate template) {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			classifiers.add(classifiertemplate.getClassifier());
		}
		return classifiers;
	}
}

package kbee.web.resource;

import java.time.OffsetDateTime;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Resource;
import com.novamens.content.document.TreeFile;
import com.novamens.content.resource.ExternalResource;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.security.User;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.model.ObjectModel;

public class ResourceModel implements IModel<Resource> {
	
	private static final long serialVersionUID = 1L;
	
	private Resource resource;
	private IModel<Resource> model;
	private IModel<User> usermodel;
	private String title, description, url;
	private boolean publicresource, inportalversion;
	private OffsetDateTime lastModifiedDate;
	public ResourceModel(Resource resource) {
		model = new ObjectModel<Resource>(resource);
		title = resource.getTitle();
		description = resource.getDescription();
		publicresource = resource.isPublicArea();
		inportalversion = resource.isInPortalVersion();
		lastModifiedDate = resource.getLastModifiedOffsetDateTime();
		usermodel = new ObjectModel<User>(resource.getLastModifiedUser(), true);
		if (resource instanceof ExternalResource) {
			url = ((ExternalResource)resource).getUrl();
		}
		this.resource = resource;
	}
	public Resource getObject() {
		
		if (this.resource==null) {
			this.resource = model.getObject();
			this.resource.setTitle(title);
			if (this.resource instanceof KbeeExternalResource)	((KbeeExternalResource)this.resource).setDescription(description);
			else if (this.resource instanceof KBFileImpl)		((KBFileImpl)this.resource).setDescription(description);
			
			else if (this.resource instanceof TreeFile)
				throw new KbeeRuntimeException("TreeFile not supported in this version");
			
			((AbstractResource)this.resource).setPublic(publicresource);
			this.resource.setLastModifiedOffsetDateTime(lastModifiedDate);
			this.resource.setInPortalVersion(inportalversion);
			this.resource.setLastModifiedUser(usermodel.getObject());
			
			if (this.resource instanceof ExternalResource)
				((ExternalResource) this.resource).setUrl(url);
		}	
		return this.resource;
	}
	
	public void setObject(Resource resource) {
	}
	
	public void detach() {
		if (this.resource!=null) {
			title = this.resource.getTitle();
			description = resource.getDescription();
			publicresource = resource.isPublicArea();
			inportalversion = resource.isInPortalVersion();
			lastModifiedDate = resource.getLastModifiedOffsetDateTime();
			usermodel = new ObjectModel<User>(resource.getLastModifiedUser(), true);
			if (this.resource instanceof ExternalResource) {
				url = ((ExternalResource)resource).getUrl();
			}
			this.resource = null;
			model.detach();
		}
	}
}

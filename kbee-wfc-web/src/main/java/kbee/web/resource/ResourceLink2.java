package kbee.web.resource;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;

import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.web.media.StandAlonePlayerPage;

public class ResourceLink2 extends Link<Resource> {
	private static final long serialVersionUID = 1L;

	private IModel<Resource> resource_model;
	private IModel<Content> content_model;
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceLink2.class.getName());

	/**
	 * @param id
	 * @param resourcemodel
	 * @param contentmodel
	 */
	public ResourceLink2(String id, IModel<Resource> resourcemodel, IModel<Content> contentmodel) {
		super(id);
		setResourceModel(resourcemodel);
		setContentModel(contentmodel);
		add(new AttributeModifier("target", "_blank"));
	}
	
	/**
	 * @param id
	 * @param resourcemodel
	 */
	public ResourceLink2(String id, IModel<Resource> resourcemodel) {
		super(id);
		setResourceModel(resourcemodel);
		add(new AttributeModifier("target", "_blank"));
	} 
	
	@Override
	public void onClick() {
		
		if (isSupportUser() && !isRoot())
			return;
		
		Resource resource = getResourceModel().getObject();
		
		// Image
		if (isImage(resource)) {
			Resource res = getResourceModel().getObject();
			KBFile file = (KBFile) res;
			StandAlonePlayerPage playerPage = new StandAlonePlayerPage(new ObjectModel<KBFile>(file));
			setResponsePage(playerPage);
		}
		// Video
		else if (isVideo(resource)) {
			Resource res = getResourceModel().getObject();
			KBFile file = (KBFile) res;
			StandAlonePlayerPage playerPage = new StandAlonePlayerPage(new ObjectModel<KBFile>(file));
			setResponsePage(playerPage);
		}
		// Audio
		else if (isAudio(resource)) {
			Resource res = getResourceModel().getObject();
			KBFile file = (KBFile) res;
			StandAlonePlayerPage playerPage = new StandAlonePlayerPage(new ObjectModel<KBFile>(file));
			setResponsePage(playerPage);
		}
		// all the other file types, some open in the browser and some download, depending on the browser settings
		else { 
			String resourcehref=null;
			try {
				if (resource instanceof ExternalResource) {
					resourcehref = ((ExternalResource)resource).getUrl();
				}
				else  {
					ResourceReference resourceReference = getContentModel()!=null ? new WebResourceReference(resource, getContentModel().getObject()) : new WebResourceReference(resource);
					resourcehref =	RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(RequestCycle.get().urlFor(resourceReference, null)));
					logger.debug(resourcehref);
				}
			} 
			catch (Exception e) {
				logger.error(e);
			}
			if (!ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()) || ServiceLocator.getService(SecurityService.class).isRoot()) {
				if (resourcehref!=null)
					setResponsePage(new RedirectPage(resourcehref));
			}
		}
	}
	
	public boolean isAudio() {
		return isAudio(getResourceModel().getObject());
	}

	public boolean isVideo() {
		return isVideo(getResourceModel().getObject());
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (resource_model!=null)
			resource_model.detach();
		if (content_model!=null)
			content_model.detach();
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private boolean isImage(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isImage(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
		return false;
	}
	
	private boolean isVideo(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isVideo(file.getFileName());
			} catch (Exception e) {
				logger.error(e);

			}
		}
		return false;
	}
						
	private boolean isAudio(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isAudio(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
		return false;
	}
	
	private void setResourceModel(IModel<Resource> model) {
		this.resource_model=model;
	}
	
	private IModel<Resource> getResourceModel() {
		return this.resource_model;
	}
					
	private void setContentModel(IModel<Content> model) {
		this.content_model=model;
	}
	
	private IModel<Content> getContentModel() {
		return this.content_model;
	}
}

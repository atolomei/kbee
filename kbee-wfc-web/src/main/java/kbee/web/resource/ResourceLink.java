package kbee.web.resource;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.web.media.StandAlonePlayerPage;

public class ResourceLink<T extends Content> extends Link<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceLink.class.getName());
	
//	private static final String server = (String) PropertiesFactory.getInstance("kbee").getProperties().get("server");
//	private static final String port = (String) PropertiesFactory.getInstance("kbee").getProperties().get("port");
//	
//	
//	
//	private static String s_protocol; 
//	
//	static {
//		if (server!=null) {
//			s_protocol = server.startsWith("http")?"http":"https";
//		}
//		else
//			s_protocol="http";
//	}
	
	private IModel<Resource> resource_model;
	private IModel<T> content_model;

	public ResourceLink(String id, IModel<Resource> resourcemodel, IModel<T> contentmodel) {
		super(id);
		setResourceModel(resourcemodel);
		setContentModel(contentmodel);
		add(new AttributeModifier("target", "_blank"));
	}
	
	public ResourceLink(String id, IModel<Resource> resourcemodel) {
		super(id);
		setResourceModel(resourcemodel);
		add(new AttributeModifier("target", "_blank"));
	} 
	
	@Override
	public void onClick() {
		
		if (isSupportUser() && !isRoot())
			return;
		
		Resource resource = getResourceModel().getObject();
		
		/**
		 * Image, Video, Audio
		 */
		if (isImage(resource) || isVideo(resource) || isAudio(resource)) {
			setResponsePage(new StandAlonePlayerPage(new ObjectModel<KBFile>((KBFile) resource)));
		}
		else {
			/**
			 	all the other file types, 
			 	some open in the browser and some download, depending on the browser settings
			 	
			 */
			String resourcehref = null;
			try {
				resourcehref = resource instanceof ExternalResource ? ((ExternalResource)resource).getUrl() : getResourceURL();
			} 
			catch (Exception e) {
				logger.error(e);
			}
			
			if (resourcehref!=null) {
				setResponsePage(new RedirectPage(resourcehref));
			}
			else {
				logger.error(" resourcehref is null");
			}
		}
	}
	
	
	public boolean isAudio() {
		return isAudio(getResourceModel().getObject());
	}

	public boolean isVideo() {
		return isVideo(getResourceModel().getObject());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (resource_model!=null)
			resource_model.detach();
		if (content_model!=null)
			content_model.detach();
	}
	
	
	/***
	 *  
	 * 
	 * 
	 * 
	 */
	
	
	
	protected String getResourceURL() {
		
		Resource resource = getResourceModel().getObject();
		String href=null;
		
		Content content = getContentModel()!=null ? getContentModel().getObject() : null;
	
		content = (Content)Proxy.Unproxy(content);
		
		String path = getPath(resource, content);
		
		ResourceReference resourceReference = getContentModel()!=null 
				? (isShared() ? new SharedResourceReference(resource, content) : new WebResourceReference(resource, path, content)) 
				: (isShared() ? new SharedResourceReference(resource) : new WebResourceReference(resource));
		
		href =	RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(RequestCycle.get().urlFor(resourceReference, null)));

		 logger.debug(href);

		return href;
	}
	
	protected boolean isShared() {
		return false;
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	private String getPath (Resource resource, Content content) {
		for (ContentResource contentresource : ((KbeeResourceContainer)content).getContentResources()) {
			if (contentresource!=null &&
					contentresource.getResource()!=null &&
					contentresource.getResource().getId().equals(resource.getId())) {
				if (contentresource.getFolder()!=null) {
					String path = contentresource.getFolder().getName();
					String folderpath = getPath(contentresource.getFolder(), content);
					if (folderpath!=null) {
						path = folderpath + "/" + path;
					}
					return path;
				}
				else {
					return null;
				}
			}
		}
		return null;
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
					
	private void setContentModel(IModel<T> model) {
		this.content_model=model;
	}
	
	private IModel<T> getContentModel() {
		return this.content_model;
	}
}
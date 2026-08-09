package kbee.web.resource;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;

public abstract class ResourceView extends Panel {
	private static final long serialVersionUID = 1L;

	private IModel<Resource> model;
	private IModel<Content> contentModel;
	
	private int index = 0;
	private boolean isReadOnly = true;
	private ViewMode viewMode = ViewMode.THUMBNAIL;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResourceView.class.getName());
	
	public ResourceView(String id, IModel<Resource> model, IModel<Content> contentModel, int index) {
		super(id);
		setModel(model);
		setContentModel(contentModel);
		setIndex(index);
	}
	
	public void setModel(IModel<Resource> model) {
		this.model = model;
	}
	
	public IModel<Resource> getModel() {
		return model;
	}
	
	protected Resource getModelObject() {
		return getModel().getObject();
	}
	
	public void setContentModel(IModel<Content> model) {
		this.contentModel = model;
	}
	
	public IModel<Content> getContentModel() {
		return contentModel;
	}
	
	public Content getContent() {
		return getContentModel().getObject();
	}
	
	public Resource getResource() {
		return getModel().getObject();
	}
	
	public int getIndex() {
		return index;
	}
	@Override
	public void onDetach() {
		getModel().detach();
		getContentModel().detach();
		super.onDetach();
	}
	
	protected void setIndex(int index) {
		this.index = index;
	}
	
	public ViewMode getViewMode() {
		return this.viewMode;
	}
	
	public void setViewMode(ViewMode mode) {
		this.viewMode=mode;
	}
	
	public boolean isAudio() {
		return isAudio(getModel().getObject());
	}

	public boolean isVideo() {
		return isVideo(getModel().getObject());
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isImage(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isImage(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
			}
		}
		return false;
	}
	
	protected boolean isVideo(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isVideo(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
			}
		}
		return false;
	}
						
	protected boolean isAudio(Resource resource) {
		if (resource instanceof KBFile) {
			KBFile file = (KBFile) resource;
			try {
				return FSUtils.isAudio(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
			}
		}
		return false;
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	protected boolean isReadOnly() {
		return this.isReadOnly;
	}

	protected void setReadOnly(boolean value) {
		this.isReadOnly = value;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
}
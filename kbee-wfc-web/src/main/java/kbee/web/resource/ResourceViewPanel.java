package kbee.web.resource;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceURI;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.SignedFile;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.markup.html.form.DraggableBehavior;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.FSUtils;
import kbee.util.NumberFormatter;
import kbee.util.logging.Logger;
import kbee.web.page.InvisibleImage;

@SuppressWarnings("serial")
public class ResourceViewPanel<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ResourceView.class.getName());
	
	private ViewMode view_mode = ViewMode.ICON;
	private IModel<T> contentModel;
	private IModel<Resource> model;
	private boolean linksEnabled = true;
	
	public ResourceViewPanel(String id, IModel<Resource> model, IModel<T> contentmodel) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);
		setContentModel(contentmodel);
	}
	
	public ResourceViewPanel(String id, IModel<Resource> model, IModel<T> contentmodel, int index) {
		super(id, model);				
		setModel(model);
		setContentModel(contentmodel);
	}

	public ViewMode getViewMode() {
		return this.view_mode;
	}
	
	public void setViewMode(ViewMode mode) {
		this.view_mode=mode;
	}
	
	public boolean isImageVisible() {
		return true;
	}
	
	public IModel<T> getContentModel() {
		return contentModel;
	}

	public void setContentModel(IModel<T> contentModel) {
		this.contentModel = contentModel;
	}
	
	public Content getContent() {
		return getContentModel().getObject();
	}

	public IModel<Resource> getModel() {
		return model;
	}

	public void setModel(IModel<Resource> model) {
		this.model = model;
	}
	
	public boolean isLinksEnabled() {
		return linksEnabled;
	}
	
	public void setLinksEnabled(boolean value) {
		linksEnabled = value;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer image_container = new WebMarkupContainer("image-container") {
			@Override
			public boolean isVisible() {
				return isImageVisible();
			}
		};

		add(image_container);
		
		image_container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getViewMode().getImageContainerCss();
			}
		}));
		
		ResourceLink<T> imageLink = new ResourceLink<T>("image-link", getModel(), getContentModel()) {
			@Override
			public boolean isVisible() {
				return isImageVisible();
			}
			@Override
			public boolean isEnabled() {
				if (!isLinksEnabled() || (isSupportUser() && !isRoot()))
					return false;
				return true;
			}
			@Override
			protected CharSequence getURL() 	{
				return isStateLess() || isShared() ? super.getResourceURL() : super.getURL();
			}
			@Override
			public boolean isShared() {
				return ResourceViewPanel.this.isShared();
			}
		};
		
		image_container.add(imageLink);
		
		Image player = new InvisibleImage("player");
		
		imageLink.add(getImage());
		imageLink.add(getIcon());
		imageLink.add(player);
		imageLink.add(new DraggableBehavior() {
			protected Component getContainment() {
				return null;
			}
		});
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
																		
		WebMarkupContainer titleLink = new ResourceLink<T>("title-link", getModel(), getContentModel()) {
			@Override
			protected CharSequence getURL() 	{
				return isStateLess() || isShared() ? super.getResourceURL() : super.getURL();
			}
			@Override
			public boolean isShared() {
				return ResourceViewPanel.this.isShared();
			}
		};
		body.add(titleLink);
		titleLink.add(new Label("resource-title", new Model<String>() {
			public String getObject() {
				return (ResourceViewPanel.this.getModel().getObject().getTitle()!=null ? 
					ResourceViewPanel.this.getModel().getObject().getTitle() :
					ResourceViewPanel.this.getModel().getObject().getName());
			}
		}));
		
		Label rdes = new Label("resource-description", new Model<String>() {
			public String getObject() {
				return ResourceViewPanel.this.getModel().getObject().getDescription();
			}
		}) {
			@Override
			public boolean isVisible() {
				return ResourceViewPanel.this.getModel().getObject().getDescription()!=null;
			}
		};
		rdes.setEscapeModelStrings(false);
		body.add(rdes);
		
		Label rsign = new Label("resource-signature", () -> getSignatureDescription()) {
			@Override
			public boolean isVisible() {
				return isSigned();
			}
		};
		rsign.setEscapeModelStrings(false);
		body.add(rsign);

		
		Label url = new Label("url", new Model<String>() {
			public String getObject() {
				return ( (ResourceViewPanel.this.getModel().getObject() instanceof ExternalResource) ?
						((ExternalResource) ResourceViewPanel.this.getModel().getObject()).getUrl() :
						"");	
			}
			}) {
			public boolean isVisible() {
				return ResourceViewPanel.this.getModel().getObject() instanceof ExternalResource;
			}
		};
		body.add(url);
					
		// Uploaded by  ---------------------------------------------------------------------------------------------------------
		//
			
		Resource kbfile =  ResourceViewPanel.this.getModel().getObject();

		User user = kbfile.getUploadUser();
		String flname = null;	
		
		if (user==null)	
			flname="n/a";
		else
			flname=user.getFirstLastName();
	
		String dateformatted = format(kbfile.getUploadOffsetDateTime());
		String wxh = "";
			
		if (kbfile !=null)  {
			int w = ResourceViewPanel.this.getModel().getObject().getWidth(); 
			if (w>0) {
				int h = ResourceViewPanel.this.getModel().getObject().getHeight();
				wxh = " · " + String.valueOf(w)+" x "+String.valueOf(h) + " pixels";
			}
			else
				wxh = "";
		}
			
		String size = NumberFormatter.formatFileSize(kbfile.getSize(), getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault());
		
		IModel<String> uploadedlabelmodel = kbfile.getVersion()>0 
			? getLabel("fileupload.versionuploadedby",	flname,	dateformatted,	size, wxh, String.valueOf(kbfile.getVersion()))
			: getLabel("fileupload.uploadedby",	flname,	dateformatted,	size, wxh);
		
			
		Label uploadedLabel = new Label("resource-uploaded", uploadedlabelmodel);
		uploadedLabel.setVisible(kbfile.getSize()>0);
		body.add(uploadedLabel.setEscapeModelStrings(false));
		
		// last modified by ---------------------------------------------------------------------------------------------------------
		
		User eduser = ResourceViewPanel.this.getModel().getObject().getLastModifiedUser();
		String dateedited = ResourceViewPanel.this.getModel().getObject().getLastModifiedOffsetDateTimeColloquial();
		
		String fedname = (eduser!=null?eduser.getFirstLastName():"n/a");

		Label redby = new Label("resource-lastmodified-by", getLabel("file.editedby", fedname, dateedited)) {
				public boolean isVisible() {
					return false;
					//if (ResourceViewPanel.this.getModel().getObject() instanceof KBFile) {
						//OffsetDateTime  edited = ResourceViewPanel.this.getModel().getObject().getLastModifiedOffsetDateTime();						
						//OffsetDateTime  uploaded = ((KBFile) ResourceViewPanel.this.getModel().getObject()).getUploadOffsetDateTime();
						//if (edited!=null && uploaded!=null && edited.isAfter(uploaded.plusSeconds(1))) {
						//	return true;	
						//}
						//return true;
				//}
				//return false;
			}
		};

		redby.setEscapeModelStrings(false);
		body.add(redby); 
		
		if (showFolders() && getContentModel()!=null) {
			String path = getPath(kbfile);
			if (path!=null && !"".equals(path.trim())) {
				body.add(new Label("resource-folder", getLabel("file.folder", path)));
			}
			else {
				body.add(new Label("resource-folder", "na").setVisible(false));
				
			}
		}
		else {
			body.add(new Label("resource-folder", "na").setVisible(false));
		}
		
		add(body);
	}
	
	public Resource getResource() {
		return model.getObject();
	}
	
	public boolean isSigned() {
		return (getResource() instanceof KBFile && ((KBFile)getResource()).isSigned());
	}
	
	public String getSignatureDescription() {
		
		if (!isSigned()) 
			return null;
		
		if ((KBFile)getResource()==null)
			return null;
		
		List<SignedFile> signatures = ((KBFile)getResource()).getSignatures();
		
		SignedFile signed =  !signatures.isEmpty() ? signatures.get(0) : null;
		
		if (signed==null)
			return null;
				
		String date = ServiceLocator.getService(DateTimeService.class).timeElapsed(signed.getDate());
		String description = getLabelString("file.signature", 
			signed.getSignature().getUser().getFirstLastName(),
			date);
		return description;
	}
	
	public boolean isImage() {
		if (getResource() instanceof KBFile) {
			KBFile file = (KBFile)getResource();
			try {
				return FSUtils.isImage(file.getFileName());
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
			}
		}
		return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onBeforeRender() {
		super.onBeforeRender();
		ResourceLink<T> imageLink = (ResourceLink<T>)get("image-container:image-link");
		imageLink.addOrReplace(getImage());
		imageLink.addOrReplace(getIcon());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (contentModel!=null) 
			contentModel.detach();
		if (model!=null) 
			model.detach();
	}
	
	protected boolean isStateLess() {
		return false;
	}
	
	protected boolean isShared() {
		return false;
	}
	
	protected boolean showFolders() {
		return false;
	}
	
	protected String format(OffsetDateTime time) {
		if (time==null)
			return "";
					
		ZonedDateTime zdate = ZonedDateTime.ofInstant(time.toInstant(), getDefaultZoneId(null));
		return ServiceLocator.getService(DateTimeService.class).timeElapsed(
				zdate, 
				getDefaultZoneId(null), 
				getDefaultLocale(), 
				DateTimeService.DATE_COLlOQUIAL_AGO, ""
				);
	}
	
	protected boolean shouldAddAntiCacheParameter() {
		return false;
	}
	
	protected Image getImage() {
		Image image;
		if (getViewMode()==ViewMode.ICON || getViewMode()==ViewMode.NOIMAGE) {
			image = new InvisibleImage("image");
		}
		else {
			ThumbnailSize size =  getViewMode()==ViewMode.THUMBNAIL ? ThumbnailSize.SMALL : ThumbnailSize.W980;
			if (isShared()) {
				image = new SharedResourceThumbnailImage<>("image", getModel(), size) ;
			}
			else {
				image = new ResourceThumbnailImage<>("image", getModel(), size) {
					protected boolean shouldAddAntiCacheParameter()	{
						return ResourceViewPanel.this.shouldAddAntiCacheParameter();
					}
				};
			}
		}
		return image;
	}
	
	protected WebMarkupContainer getIcon() {
		WebMarkupContainer icon = new WebMarkupContainer("glyphicon");
		if (getViewMode()==ViewMode.ICON) {
			icon.add(new AttributeModifier("class", getModel().getObject().getGlyphIcon()));
		}
		else {
			icon.setVisible(false);
		}
		return icon;
	}
	
	private String getPath(Resource resource) {
		ResourceURI uri = ((ResourceContainer)getContent()).getURI(resource);
		return uri!=null ? uri.toString() : null;
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ZoneId getDefaultZoneId(String zoneId) {
        if (zoneId != null)
            return ZoneId.of(zoneId);
        ZoneId zid = null;
        User user = getSessionUser();
        if (user != null)
            zid = user.getZoneId();
        if (zid == null)
            zid = ZoneId.systemDefault();
        return zid;
    }
	
	private Locale getDefaultLocale() {
        Locale locale = null;
        User user = getSessionUser();
        if (user != null)
            locale = user.getLocale();
        if (locale == null)
            locale = Locale.getDefault();
        return locale;
    }
}
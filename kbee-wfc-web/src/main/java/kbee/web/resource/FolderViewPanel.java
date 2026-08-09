package kbee.web.resource;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.FSUtils;
import kbee.web.page.InvisibleImage;

@SuppressWarnings("serial")
public class FolderViewPanel<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(FolderViewPanel.class.getName());

	private ViewMode view_mode = ViewMode.ICON;
	//private IModel<T> contentModel;
	private IModel<Resource> model;
	private boolean linksEnabled = true;
	
	public FolderViewPanel(String id, IModel<Resource> model) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);
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
	
//	public IModel<T> getContentModel() {
//		return contentModel;
//	}
//
//	public void setContentModel(IModel<T> contentModel) {
//		this.contentModel = contentModel;
//	}

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
	
	public void onClick(AjaxRequestTarget target) {
		
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
		
		AjaxLink<?> imageLink = new AjaxLink<T>("image-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				FolderViewPanel.this.onClick(target);
			}
		};
		
		image_container.add(imageLink);
		
		Image player = new InvisibleImage("player");
		
		// imageLink.add(getImage());
		
		
		imageLink.add(getIcon());
		imageLink.add(player);
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
																		
		AjaxLink<?> titleLink = new AjaxLink<Void>("title-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				FolderViewPanel.this.onClick(target);
			}
		};
		
		body.add(titleLink);
		
		titleLink.add(new Label("resource-title", new Model<String>() {
			public String getObject() {
				return (FolderViewPanel.this.getModel().getObject().getTitle()!=null ? 
					FolderViewPanel.this.getModel().getObject().getTitle() :
					FolderViewPanel.this.getModel().getObject().getName());
			}
		}));
		
		Label rdes = new Label("resource-description", new Model<String>() {
			public String getObject() {
				return FolderViewPanel.this.getModel().getObject().getDescription();
			}
		}) {
			@Override
			public boolean isVisible() {
				return FolderViewPanel.this.getModel().getObject().getDescription()!=null;
			}
		};
		
		rdes.setEscapeModelStrings(false);
		body.add(rdes);
		
		// last modified by ---------------------------------------------------------------------------------------------------------
		
		User eduser = FolderViewPanel.this.getModel().getObject().getLastModifiedUser();
		String dateedited = FolderViewPanel.this.getModel().getObject().getLastModifiedOffsetDateTimeColloquial();
		
		String fedname = (eduser!=null?eduser.getFirstLastName():"n/a");

		Label redby = new Label("resource-lastmodified-by", getLabel("file.editedby", fedname, dateedited));
		
		redby.setEscapeModelStrings(false);
		body.add(redby); 
		
		add(body);
	}
	
	public Resource getResource() {
		return model.getObject();
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
	public void onBeforeRender() {
		super.onBeforeRender();
		WebMarkupContainer imageLink = (WebMarkupContainer)get("image-container:image-link");
		imageLink.addOrReplace(getImage());
		imageLink.addOrReplace(getIcon());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null) 
			model.detach();
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
		// return ServiceLocator.getService(DateTimeService.class).timeElapsed(time, null);
		
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
	
	protected Image getImage() {
		Image image;
		//if (getViewMode()==ViewMode.ICON || getViewMode()==ViewMode.NOIMAGE) {
			image = new InvisibleImage("image");
		//}
		//else {
//			ThumbnailSize size =  getViewMode()==ViewMode.THUMBNAIL ? ThumbnailSize.SMALL : ThumbnailSize.W980;
//		image = new ResourceThumbnailImage<>("image", getModel(), size) ;
//		}
		return image;
	}
	
	
	protected WebMarkupContainer getIcon() {
		WebMarkupContainer icon = new WebMarkupContainer("glyphicon");
		//if (getViewMode()==ViewMode.ICON) {
			icon.add(new AttributeModifier("class", getModel().getObject().getGlyphIcon() + " " + getViewMode().getElementCss()));
		//}
		//else {
		//	icon.setVisible(false);
		//}
		return icon;
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
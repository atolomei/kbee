package kbee.web.content.template;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import com.novamens.content.service.ContentService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.markup.html.form.DraggableBehavior;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.util.NumberFormatter;
import kbee.util.logging.Logger;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.ResourceThumbnailImage;

@SuppressWarnings("serial")
public class TemplateViewPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(TemplateViewPanel.class.getName());
	
	private ViewMode view_mode = ViewMode.ICON;
	private IModel<Content> model;
	
	public TemplateViewPanel(String id, IModel<Content> model) {
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
	
	public IModel<Content> getModel() {
		return model;
	}

	public void setModel(IModel<Content> model) {
		this.model = model;
	}
	
	public Content getContent() {
		return getModel().getObject();
	}
	
//	public IModel<Resource> getResourceModel() {
//		Resource resource = ((ResourceContainer)getContent()).getResources().get(0);
//		IModel<Resource> model = new ObjectModel<Resource>(resource);
//		return model;
//	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Content content = getContent();
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		
		AjaxLink<Void> titleLink = new AjaxLink<Void>("title-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(TemplateViewPanel.this);
				TemplateViewPanel.this.onClick(target);
			}
		};
		
		titleLink.add(new Label("title", content.getTitle()));
		
		body.add(titleLink);
		
		String subtitle = content.getService(ContentService.class).getConsoleSubtitle();
		
		Label subtitleLabel = new Label("subtitle", subtitle);
		
		subtitleLabel.setEscapeModelStrings(false);
				
		body.add(subtitleLabel);
		
		String dateformatted = format(content.getLastModifiedOffsetDateTime());
		
		Label lastmodified = new Label("lastmodified", dateformatted);
		
		lastmodified.setEscapeModelStrings(false);
		
		body.add(lastmodified);

		body.add(new Label("lastmodified-by", content.getLastModifiedUser().getDisplayName()));
		
		body.add(new AttributeModifier("style", () -> getBodyStyle()));
		
		add(body);

	}
	
	
	public void onClick(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected String getBodyStyle() {
		return "";
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
	
//	protected boolean shouldAddAntiCacheParameter() {
//		return false;
//	}
//	
//	protected Image getImage() {
//		Image image;
//		if (getViewMode()==ViewMode.ICON || getViewMode()==ViewMode.NOIMAGE) {
//			image = new InvisibleImage("image");
//		}
//		else {
//			ThumbnailSize size =  getViewMode()==ViewMode.THUMBNAIL ? ThumbnailSize.SMALL : ThumbnailSize.W980;
//			image = new ResourceThumbnailImage("image", getResourceModel(), size) {
//				protected boolean shouldAddAntiCacheParameter()	{
//					return TemplateViewPanel.this.shouldAddAntiCacheParameter();
//				}
//			};
//		}
//		return image;
//	}
//
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
//	
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
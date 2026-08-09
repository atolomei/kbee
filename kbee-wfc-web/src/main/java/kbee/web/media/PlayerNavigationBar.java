package kbee.web.media;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.web.entity.UserHeaderPanel;
import kbee.web.nav.NavigationPanel;
import kbee.web.resource.WebThumbnailReference;

@SuppressWarnings("serial")
public class PlayerNavigationBar extends NavigationPanel<KBFile> {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(PlayerNavigationBar.class.getName());

	public static final ResourceReference EMPTY_PHOTO = new PackageResourceReference( UserHeaderPanel.class, "NoPicture.gif");

	IModel<KBFile> model;
	
	public PlayerNavigationBar(String id, IModel<KBFile> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}

	public IModel<KBFile> getModel() {
		 return model;
	}
	
	public void setModel( IModel<KBFile> model) {
		this.model = model;
	}

	@Override
	public void navigate() {
	}

	@Override
	public boolean isFromContentBase() {
		return false;
	}

	public void onReturn(AjaxRequestTarget target)  {
		target.appendJavaScript("closewindow();");
	}
	
	@Override
	public void onDetach() {
		if (model!=null)
			getModel().detach();
		super.onDetach();
	}

	protected Component newCloseLink()  {
		AbstractLink link = new AjaxLink<Void>("close-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("closewindow();");
 			}
		};	
		return link;
	}

	protected Component originalSize()  {
		AbstractLink link = new AjaxLink<Void>("original-size-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			@Override
			public boolean isVisible() {
				return false;
			}
			
		};	
		return link;
	}

	protected Component rotateRight()  {
		AbstractLink link = new AjaxLink<Void>("rotate-right-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			@Override
			public boolean isVisible() {
				return false;
			}
		};	
		return link;
	}

	protected Component rotateLeft()  {
		AbstractLink link = new AjaxLink<Void>("rotate-left-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
 			}
			@Override
			public boolean isVisible() {
				return false;
			}
		};	
		return link;
	}

	protected Component newDownloadLink()  {
		try {
			File file;
			file = PlayerNavigationBar.this.getModel().getObject().getFile();
			if (file!=null) {
				Link<?> link = new DownloadLink("download-link", file);
				link.setEnabled(isRoot() || !isSupportUser());
				return link;
			}
			else {
				Link<?> link = new DownloadLink("download-link", new File("nofile"));
				link.setVisible(false);
				return link;
			}
			
		} catch (IOException e) {
			Link<?> link = new DownloadLink("download-link", new File("nofile"));
			link.setVisible(false);
			return link;
		}
	}

	public void onInitialize() {
		super.onInitialize();
		
		addOrReplace(newCloseLink());
		addOrReplace(newDownloadLink());
	
		addOrReplace(originalSize());
		addOrReplace(rotateRight());
		addOrReplace(rotateLeft());
		User user = getModel().getObject().getLastModifiedUser();
		String sd;
		
		
		
		try {
			sd = ServiceLocator.getService(DateTimeService.class).format(getModel().getObject().getLastModifiedOffsetDateTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z);
			
		
		
		} catch (Exception e) {
			logger.error(e.getClass().getSimpleName());
			sd = "";
		}
		
		Label name = new Label("name", user.getFirstLastName());
		Label date = new Label("date", sd);
		Image ph = new Image("photo", getPhoto()) { 
			private static final long serialVersionUID = 1L;
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		};
		add(name);
		add(date);
		add(ph);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		StringBuilder script = new StringBuilder();
		script.append("function closewindow() {\n");
		script.append("	if (window.opener && window.opener.refresh) { window.opener.refresh(); };\n");
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		script.append("}\n");
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "closewindow"));
	}

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}

	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	protected Person getPerson() {
		User user = getModel().getObject().getLastModifiedUser();
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		if (profile!=null && profile.getEntity()!=null && profile.getEntity() instanceof Person)
			return (Person)profile.getEntity();
		else
			return null;	
	}

	protected ResourceReference getPhoto() {
		Person person = getPerson();
		ResourceReference photoreference;
		if (person!=null && person.getPhoto()!=null) {
			photoreference = new WebThumbnailReference(person.getPhoto(), ThumbnailSize.MINI);
		}
		else {
			photoreference = EMPTY_PHOTO;
		}
		return photoreference;
	}

	//protected KbeeUser getUser() {
	//	return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	//}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}

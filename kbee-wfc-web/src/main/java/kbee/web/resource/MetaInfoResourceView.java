package kbee.web.resource;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;

import kbee.util.NumberFormatter;
import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class MetaInfoResourceView extends ResourceView {
	private static final long serialVersionUID = 1L;

	static public final PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");
	static public final PackageResourceReference PLAYER = new PackageResourceReference(AbstractKbeeWebPage.class, "player-small.png");
	
	private static Logger logger = Logger.getLogger(MetaInfoResourceView.class.getName());
	
	public MetaInfoResourceView(String id, IModel<Resource> model, IModel<Content> contentModel) {
		this(id, model, contentModel, 0);
	}

	public MetaInfoResourceView(String id, IModel<Resource> model, IModel<Content> contentModel, int index) {
		super(id, model, contentModel, index);
		setOutputMarkupId(true);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer imageContainer = new WebMarkupContainer("image-container") {
			@Override
			public boolean isVisible() {
				return isImageVisible();
			}
		};
		
		imageContainer.add(new AttributeModifier("class", () -> getViewMode().getImageContainerCss()));
		imageContainer.add(getImageLink());
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		
		body.setOutputMarkupId(true);
		
		body.add(getTitleLink());
		body.add(getDescription());
		body.add(getUrl());
		body.add(getUploadInfo());
		body.add(getModificationInfo());
		body.add(getEditor());
		
		Panel menu = getMenu();
		
		add(new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return menu.isVisible();
			}
		});
		
		((WebMarkupContainer)get("menu-container")).add(menu);
	
		add(imageContainer);
		add(body);
	}

	public Component getImage() {	
		return null;
	}
	
	public Component getGlyphIcon() {
		return null;
	}
	
	public boolean isImageVisible() {
		return true;
	}
	
	public String getResourceTitle() {
		return getModelObject().getTitle()!=null?getModelObject().getTitle():getModelObject().getName();
	}
	
	public void edit(AjaxRequestTarget target) {
		((ResourceEditor)getEditor()).enable(target);
		target.add(get("body"));
	}

	public void closeEditor(AjaxRequestTarget target) {
		target.add(get("body"));
	}
	
	protected Panel getMenu() {
			
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(getModel());
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						edit(target);
					}
					@Override
					public String getLabel() {	
						return MetaInfoResourceView.this.getLabel("menu.edit").getObject();
					}
					@Override
					public boolean isVisible() {
						return true;
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				}	
			);
			
			menu.addItem(id ->
				new DonwloadMenuItemPanelV5<Resource>(id) {
					@Override 
					public String getLabel() {
						return  MetaInfoResourceView.this.getLabel("menu.download").getObject();
					}
					@Override
					protected File getFile() {
						if (getModel().getObject() instanceof KBFile) {
							File file;
							try {
								file = ((KBFile) getModel().getObject()).getFile();
								if (file==null) 
									logger.error("file is null " + ((KBFile) getModel().getObject()).getName());
								return file;
							} 
							catch (IOException e) {
								logger.error(e);
							}
						}
						return null;
					}
					@Override
					public boolean isVisible() {
						return (getModel().getObject() instanceof KBFile);
					}
					@Override
					public boolean isEnabled()  {
						//if (isSupportUser() && !isRoot())
						//	return false;
						return true;
					}
				}
			);
			
			
			return menu;
		}
	
	protected Panel newEditor() {
		return new ResourceEditor(getModel());
	}
	
	protected Panel getEditor() {
		Panel editor = (Panel)get("body:editor"); 
		if (editor==null) {
			editor = newEditor();
		}
		if (editor==null) { 
			editor = new InvisiblePanel("editor");
		}	
		return editor;
	}
	
	protected Link<?> getImageLink() {
		ResourceLink2 link = new ResourceLink2("image-link", getModel(), getContentModel()) {
			@Override
			public boolean isVisible() {
				return isImageVisible();
			}
			@Override
			public boolean isEnabled() {
				if (isSupportUser() && !isRoot())
					return false;
				return true;
			}
		};
		
		link.add(getImage());
		link.add(getGlyphIcon());
		link.add(getPlayer());
		
		return link;
	}
	
	protected Link<?> getTitleLink() {
		Link<?> titleLink = new ResourceLink2("title-link", getModel(), getContentModel());
		titleLink.add(new Label("resource-title", () -> getResourceTitle()));
		return titleLink;
	}
	
	protected Label getUrl() {
		Label url = new Label("url", new Model<String>() {
			public String getObject() {
				String url = ((ExternalResource) getModelObject()).getUrl();
				if (url==null)	return "";
				return url;
			}
		});
		url.setVisible(getModelObject() instanceof ExternalResource);
		return url;
	}
	
	protected Label getDescription() {
		Label description = new Label("resource-description", () -> getModelObject().getDescription()) {
			public boolean isVisible() {
				return getModelObject().getDescription()!=null;
			}
		};
		description.setEscapeModelStrings(false);
		return description;
	}
	
	protected Image getPlayer() {
		ResourceReference imagereference = 
				isVideo() && (getViewMode()==ViewMode.THUMBNAIL || getViewMode()==ViewMode.THUMBNAIL_LARGE) ?
					PLAYER :
					MENU_ICON;
		Image playerimage = new Image("player", imagereference) {
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		};
		return playerimage;
	}
	
	protected Label getUploadInfo() {
		Label info;
		if (getModelObject() instanceof KBFile) {
			KBFile kbfile = (KBFile) getModelObject();
			
			User user = kbfile.getUploadUser();
			
			String filename = (user==null) ? "n/a" : user.getFirstLastName();
			
			String dateformatted = kbfile.getUploadOffsetDateTimeColloquial();
			
			//String inportal = kbfile.isInPortalVersion() ? 
			//	getLabel("inportal").getObject() : getLabel("notinportal").getObject();
				
			String size = NumberFormatter.formatFileSize(kbfile.getSize(),
				getSessionUser().getLocale(), 
				kbfile.getWidth()>0 ? " · " + String.valueOf(kbfile.getWidth())+" x "+String.valueOf(kbfile.getHeight()) + " pixels" : "");
			
			String pixeles = kbfile.getWidth()>0 ?
				" · " + String.valueOf(kbfile.getWidth())+" x "+String.valueOf(kbfile.getWidth()) + " pixels" :
				"";
			
			IModel<String>  model = getLabel("fileupload.uploadedby", 
				filename, 
				dateformatted,
				size,
				pixeles);
			
			info = new Label("resource-uploaded", model);
			info.setEscapeModelStrings(false);
		}
		else {
			info = new Label("resource-uploaded", "na");
			info.setVisible(false);
		}
		return info;
	}
	
	protected Label getModificationInfo() {
		IModel<String> model = getLabel("file.editedby", 
			getModelObject().getLastModifiedUser()!=null ? getModelObject().getLastModifiedUser().getFirstLastName() : "n/a",
			getModelObject().getLastModifiedOffsetDateTimeColloquial());
		model.getObject();
		Label info = new Label("resource-lastmodified-by", model) {
			public boolean isVisible() {
				if (getModelObject() instanceof KBFile) {
					OffsetDateTime edited = getModelObject().getLastModifiedOffsetDateTime();						
					OffsetDateTime uploaded = ((KBFile) getModelObject()).getUploadOffsetDateTime();
					if (edited!=null && uploaded!=null && edited.isAfter(uploaded.plusSeconds(90))) {
						return true;	
					}
					return false;
				}
				return true;
			}
		};	
		info.setEscapeModelStrings(false);
		return info;
	}
	
	protected boolean isSessionUserWorkspace() {
		return (MetaInfoResourceView.this.getContent().getWorkspace()!=null && MetaInfoResourceView.this.getContent().getWorkspace().equals((Long) getSessionUser().getId()));
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
}
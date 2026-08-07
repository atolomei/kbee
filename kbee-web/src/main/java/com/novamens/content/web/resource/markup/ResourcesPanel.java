package com.novamens.content.web.resource.markup;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
 
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeFile;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.TreeFileResource;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.resource.markup.model.NewExternalResourceData;
import com.novamens.content.web.treefile.markup.TreeFileCreationModal;
import com.novamens.content.web.treefile.markup.TreeFilePage;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.UploadMenuItemPanelV5;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.ProxyModel;

import kbee.util.NumberFormatter;
import kbee.util.logging.Logger;
import kbee.web.console.action.UploadAction;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.ResourceListUpdateEvent;
import kbee.web.resource.ResourceGlyphIcon;
import kbee.web.resource.ResourceIcon;
import kbee.web.resource.ResourceLink;
import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceThumbnailImage;

import com.novamens.wicket.markup.html.modal.Dialog.Button;


/**
 *  Max file size upload: 30 GB
 *
 * @param <T>
 */
@SuppressWarnings("serial")
@Deprecated
public class ResourcesPanel<T extends Content> extends ObjectEditorPanel<T> {
	 static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ResourcesPanel.class.getName());

	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	private static final double GB = 1000000000.0;
	
	static PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");
	static PackageResourceReference PLAYER	  = new PackageResourceReference(AbstractKbeeWebPage.class, "player-small.png");

	private boolean ispublic_area = true; // if this ResourcesPanel is part of the public or private area
	
	private boolean updated = false;
	
	@SuppressWarnings("unused")
	private boolean received_from_another_tab = false;
	
	private List<IModel<Resource>> resources;
	private List<IModel<Resource>> deleted = new ArrayList<IModel<Resource>>();
	
	private ResourceView activeview = null;
	
	private boolean toolbar_visible= true;
	
	private ViewMode viewmode = ViewMode.THUMBNAIL;
	
	private String defaultGroupId = null;
	
	public class FileUploader extends Fragment {
		public FileUploader() {
			super("file-uploader", "file-uploader-fragment", ResourcesPanel.this);
			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles-button");
			pickfiles.setVisible(getGroups().size()<=1);
			pickfiles.add(new AttributeModifier("class", isPublicArea() ? "btn btn-md btn-default": "btn btn-md btn-default"));
			add(pickfiles);
			
			WebMarkupContainer groupsbutton = new WebMarkupContainer ("groups-button");
			groupsbutton.setVisible(getGroups().size()>1);
			groupsbutton.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
			groupsbutton.add(new AttributeModifier("data-toggle", "dropdown"));
			add(groupsbutton);
			
			ContextMenuPanel<Void> groupsmenu = new ContextMenuPanel<Void>("groups-menu", null);
			
			if (getGroups().size()>1) {
				int g = 0;
				for (ResourceTag group : getGroups()) {
					String groupName = group.getName();
					String groupId = String.valueOf(((KbeeResourceTag)group).getId());
					String dropElement =  g++==0 ? "resources-panel" : null;
					groupsmenu.addItem(id ->
						new UploadMenuItemPanelV5<Void>(id, dropElement) {
							@Override
							public String getLabel() {	
								return groupName;
							}
							@Override
							protected String getUploadUrl() {
								Content content = ResourcesPanel.this.getModelObject();
								return "/upload?id="+ content.getId() +"&class="+getContentClass(content)+"&public="+String.valueOf(isPublicArea())+"&group="+groupId;
							}	
							@Override
							protected String getRefreshFunction() {
								return "refreshfiles"+ResourcesPanel.this.getMarkupId()+"()";
							}
							@Override
							protected Component getResourcesView() {
								return ResourcesPanel.this.get("resources-view");
							}	
						}
					);
				};
			}	
					
			add(groupsmenu);
		}
	}
	
	public class LinkFactory extends Fragment {
		public LinkFactory() {
			super("link-factory", "link-factory-fragment", ResourcesPanel.this);
			add(new AjaxLink<Void>("add-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					WebMarkupContainer editorcontainer = (WebMarkupContainer)ResourcesPanel.this.get("resources-view:new-resource-editor");
					NewExternalResourceEditor editor = new NewExternalResourceEditor(new Model<NewExternalResourceData>(new NewExternalResourceData()));
					editorcontainer.add(new AttributeModifier("class", getViewMode().getElementCss())); 
					editorcontainer.addOrReplace(editor);
					editorcontainer.setVisible(true);
					((TextField<?>)editor.get("form:title")).beforeRender();
					target.focusComponent(((TextField<?>)editor.get("form:title")).getInput());
					target.add(ResourcesPanel.this.get("resources-view"));
					//refresh(target);
				}
			});
		}
		@Override
		public boolean isVisible() {
			return isPublicArea() && ResourcesPanel.this.getModelObject().getContentTemplate().isLinkResources();
		}
	}
	
	public class TreeUploader extends Fragment {
		public TreeUploader() {
			super("tree-uploader", "tree-uploader-fragment", ResourcesPanel.this);
			setOutputMarkupId(true);
			AjaxLink<Void> addtree = new AjaxLink<Void>("add-tree") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					((Modal)TreeUploader.this.get("add-tree-modal")).open(target);
				}
			};
			add(addtree);
			add(new TreeFileCreationModal<T>("add-tree-modal", getModel()) {
				@Override
				public void onClose(AjaxRequestTarget target) {
					super.onClose(target);
					setResources(((ResourceContainer)getModelObject()).getResources());
					target.add(ResourcesPanel.this);
				}
			});
		}
		@Override
		public boolean isVisible() {
			return isPublicArea() && ResourcesPanel.this.getModelObject().getContentTemplate().isTreeFileResources();
		}
	}
	
	public class Toolbar extends Fragment {
		public Toolbar() {
			super("toolbar", "toolbar-fragment", ResourcesPanel.this);
			setOutputMarkupId(true);
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
				
			if (get("right-menu")!=null)
				return;
			
			add(getMenu());
			
			final long QUOTA    = getDomain().getQuota();
			final double DQUOTA = Double.valueOf(QUOTA).doubleValue();
			
			add(new WebMarkupContainer("quotalimit") {
				@Override
				public boolean isVisible() {
					if(!isPublicArea())
						return false;
					 // We use local Hard Disk storage for Quota. (External storage does not count)
					long used  = getDomainMetricsServices().getHardDisk(getDomain());
					double dused  = Double.valueOf((double) used / (double) GB).doubleValue();
					if (QUOTA > 0 &&  (DQUOTA < dused)) 
						return true;
					return false;
				}
			});

			final long used  = getDomainMetricsServices().getHardDisk(getDomain());
			final double dused  = Double.valueOf((double) used / (double) GB).doubleValue();
			 
			WebMarkupContainer ul = new WebMarkupContainer("upload-panel") {
				@Override
				public boolean isVisible() {
					if (QUOTA > 0 && (DQUOTA < dused)) 
						return false;
					return getEditor().isEditionEnabled();
				}
			};
			add(ul);

			add(new WebMarkupContainer("dragdrop") {
				@Override
				public boolean isVisible() {
					 if (QUOTA > 0 &&  (DQUOTA < dused)) 
						return false;
					 return getEditor().isEditionEnabled();
				}
			});
			
			ul.add(new FileUploader());
			ul.add(new TreeUploader());
			ul.add(new LinkFactory());
		}
		
		private WebMarkupContainer getMenu() {
			
			WebMarkupContainer panelmenu = new WebMarkupContainer("right-menu");
			WebMarkupContainer resmlink = new WebMarkupContainer("res-menulink");
			panelmenu.add(resmlink);
			
			ContextMenuPanel<T> resmenu = new ContextMenuPanel<T>("res-menu", getModel());
			
			resmenu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ResourcesPanel.this.viewmode = ViewMode.ICON;
						getSessionUser().getService(PreferencesService.class).setIntValue(ResourcesPanel.class.getSimpleName(),"viewmode", ViewMode.ICON.ordinal());
						refresh(target);
					}
					@Override
					public boolean isEnabled() {
						return ResourcesPanel.this.viewmode != ViewMode.ICON;
					}
					@Override
					public String getLabel() {	
						return "Icon";
					}
				}
			);
			
			resmenu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ResourcesPanel.this.viewmode = ViewMode.THUMBNAIL;
						getSessionUser().getService(PreferencesService.class).setIntValue(ResourcesPanel.class.getSimpleName(),"viewmode", ViewMode.THUMBNAIL.ordinal());
						refresh(target);
					}
					@Override
					public boolean isEnabled() {
						return ResourcesPanel.this.viewmode != ViewMode.THUMBNAIL;
					}
					@Override
					public String getLabel() {	
						return "Thumbnail";
					}
				}
			);
			
			resmenu.addItem(id ->
				new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ResourcesPanel.this.viewmode = ViewMode.THUMBNAIL_LARGE;
						getSessionUser().getService(PreferencesService.class).setIntValue(ResourcesPanel.class.getSimpleName(),"viewmode", ViewMode.THUMBNAIL_LARGE.ordinal());
						refresh(target);
					}
					@Override
					public boolean isEnabled() {
						return ResourcesPanel.this.viewmode != ViewMode.THUMBNAIL_LARGE;
					}
					@Override
					public String getLabel() {	
						return "Image";
					}
				}
			);

			resmenu.addItem(new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new SeparatorMenuItemPanelV5<T>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
					};
				}
			});
			
			resmenu.addItem(new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<T>(id) {
						 
						@Override
						public String getLabel() {	
							return new StringResourceModel("downloadall", ResourcesPanel.this, null).getObject();
						}
						@Override
						public boolean isVisible() {
							return true;
						}
						@Override
						protected File getFile() {
							if (isPublicArea())
								return getModelObject().getService(ContentExportService.class).getPublicResourcesExport();
							else
								return getModelObject().getService(ContentExportService.class).getPrivateResourcesExport();
						}
						
						@Override
						public boolean isEnabled()  {
							try {
								return (isRoot() || !isSupportUser());
							} catch (Exception e) {
								logger.error(e, getSessionUser().getUserName());
								return false;
							}
						}
					};
				}
			});

			panelmenu.add(resmenu);
			return panelmenu;
		}
	}

	/**
	 * 
	 * 
	 */
	public class IconResourceView extends MetainfoResourceView {
		
		public IconResourceView(String id, IModel<Resource> model, int index) {
			super(id, model, index);
		}
		@Override
		public Component getImage(IModel<Resource> model) {
			return (new ResourceIcon("image", model.getObject()).setVisible(false));
		}
		@Override
		public Component getGlyphIcon(IModel<Resource> model) {
			String gi;

			if (model==null || model.getObject()==null)
				return new ResourceGlyphIcon("glyphicon");
			
			gi = model.getObject().getGlyphIcon(); 
			
			return new ResourceGlyphIcon("glyphicon", gi);
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public class ThumbnailResourceView extends MetainfoResourceView {
	
		public ThumbnailResourceView(String id, IModel<Resource> model, int index) {
			super(id, model, index);
		}
		@Override
		public Component getImage(IModel<Resource> model) {
			return new ResourceThumbnailImage<T>("image", ResourcesPanel.this.getModel(), model);
		}
		
		@Override
		public Component getGlyphIcon(IModel<Resource> model) {
			return new Label("glyphicon", "").setVisible(false);
		}
	}
	
	
	public class ThumbnailLargeResourceView extends MetainfoResourceView {
		public ThumbnailLargeResourceView(String id, IModel<Resource> model, int index) {
			super(id, model, index);
		}
		@Override
		public Component getImage(IModel<Resource> model) {
			return new ResourceThumbnailImage<T>("image", ResourcesPanel.this.getModel(), model, ThumbnailSize.LARGE);
		}
		@Override
		public Component getGlyphIcon(IModel<Resource> model) {
			return new Label("glyphicon", "").setVisible(false);
		}
	}
	
	public class MetainfoResourceView extends ResourceView {
		
		private boolean showversions = false;
		
		public MetainfoResourceView(String id, IModel<Resource> model, int index) {
			super(id, "metainfo-view-fragment", model);
			
			setOutputMarkupId(true);
			
			setIndex(index);
		
			WebMarkupContainer image_container = new WebMarkupContainer("image-container") {
				@Override
				public boolean isVisible() {
					return isImageVisible();
				}
			};
				
			image_container.add(new AttributeModifier("class",
					new Model<String>() {
					@Override
					public String getObject() {
						return getViewMode().getImageContainerCss();
					}
			}));
			
			add(image_container);
			
			ResourceLink<T> imageLink = new ResourceLink<T>("image-link", model, ResourcesPanel.this.getModel()) {
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
			
			
			image_container.add(imageLink);
			imageLink.add(getImage(model));
			imageLink.add(getGlyphIcon(model));
		
			if (imageLink.isVideo() && (getViewMode()==ViewMode.THUMBNAIL || getViewMode()==ViewMode.THUMBNAIL_LARGE)) {
				Image player;
				player = new Image("player", PLAYER) { 
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
				};
				imageLink.add(player);
			}
			else {
				Image player;
				player = new Image("player", MENU_ICON) { 
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
				};
				imageLink.add(player.setVisible(false));
			}
			
			WebMarkupContainer body = new WebMarkupContainer("body");
			body.setOutputMarkupId(true);
																			
			WebMarkupContainer titleLink = new ResourceLink<T>("title-link", model, ResourcesPanel.this.getModel());
			
			body.add(titleLink);
			
			titleLink.add(new Label("resource-title", new Model<String>() {
				public String getObject() {
					String title = MetainfoResourceView.this.getModel().getObject().getTitle();
					if (title==null) 
						title = MetainfoResourceView.this.getModel().getObject().getName();
					return title;
				}
			}));
			
			
			Label rdes = new Label("resource-description", new Model<String>() {
				public String getObject() {
					return MetainfoResourceView.this.getModel().getObject().getDescription();
				}
			}) {
				@Override
				public boolean isVisible() {
					return MetainfoResourceView.this.getModel().getObject().getDescription()!=null;
				}
			};
			
			rdes.setEscapeModelStrings(false);
			body.add(rdes);

			body.add( (new Label("url", new Model<String>() {
				public String getObject() {
						String url = ((ExternalResource) MetainfoResourceView.this.getModel().getObject()).getUrl();
						if (url==null)
							return "";
					return url;
				}
			})).setVisible(MetainfoResourceView.this.getModel().getObject() instanceof ExternalResource));

			// Uploaded by  ---------------------------------------------------------------------------------------------------------
			//
			if (MetainfoResourceView.this.getModel().getObject() instanceof KBFile) {
				Model<String> uploadedbymodel = new Model<String>() {
					public String getObject() {
						KBFile kbfile = (KBFile) MetainfoResourceView.this.getModel().getObject();
						User user = kbfile.getUploadUser();
						String flname = null;	
						if (user==null)	
							flname="n/a";
						else
							flname=user.getFirstLastName();
						String dateformatted = kbfile.getUploadOffsetDateTimeColloquial();
						String wxh;
						
						
						//String inportal = ((KBFile) MetainfoResourceView.this.getModel().getObject()).isInPortalVersion() ? 
						//	getStringLabel("inportal") : getStringLabel("notinportal");
						
						
						int w = ((KBFile) MetainfoResourceView.this.getModel().getObject()).getWidth(); 
						if (w>0) {
							int h = ((KBFile) MetainfoResourceView.this.getModel().getObject()).getHeight();
							wxh = " · " + String.valueOf(w)+" x "+String.valueOf(h) + " pixels";
						}
						else
							wxh = "";
						IModel<String> labelmodel = kbfile.getVersion()>1 ? 
							
								getLabel("fileupload.versionuploadedby", 
								
										String.valueOf(kbfile.getVersion()), 
										flname, 
										dateformatted, 
										NumberFormatter.formatFileSize(MetainfoResourceView.this.getModel().getObject().getSize(), getSessionUser().getLocale()), 
										wxh) :
											
							getLabel("fileupload.uploadedby", 
								flname, // name
								dateformatted,  // uploaded
								NumberFormatter.formatFileSize(MetainfoResourceView.this.getModel().getObject().getSize(), getSessionUser().getLocale()), // size 
								wxh); // pixels
						
						String 	label = labelmodel.getObject();
						return label;
					}
				};
				body.add(new Label("resource-uploaded", uploadedbymodel).setEscapeModelStrings(false));
			}
			else {
				body.add(new Label("resource-uploaded", "na").setVisible(false));
			}
			
			
			// last modified by ---------------------------------------------------------------------------------------------------------
			Model<String> editedbymodel = new Model<String>() {
				public String getObject() {
					String labelkey = "file.editedby", inportal = "";
					User eduser = MetainfoResourceView.this.getModel().getObject().getLastModifiedUser();
					String dateedited = MetainfoResourceView.this.getModel().getObject().getLastModifiedOffsetDateTimeColloquial();
					String fedname = (eduser!=null?eduser.getFirstLastName():"n/a");
					if (MetainfoResourceView.this.getModel().getObject() instanceof ExternalResource) {
						inportal = ((ExternalResource) MetainfoResourceView.this.getModel().getObject()).isInPortalVersion() ? 
							getStringLabel("inportal") : getStringLabel("notinportal");
						labelkey = "external.editedby";	
					}
					IModel<String> label = getLabel(labelkey, fedname, dateedited, inportal);
					return label.getObject();
				}
			};
			Label editedby = new Label("resource-lastmodified-by", editedbymodel) {
				public boolean isVisible() {
					if (MetainfoResourceView.this.getModel().getObject() instanceof KBFile) {
						OffsetDateTime  edited = MetainfoResourceView.this.getModel().getObject().getLastModifiedOffsetDateTime();						
						OffsetDateTime  uploaded = ((KBFile) MetainfoResourceView.this.getModel().getObject()).getUploadOffsetDateTime();
						if (edited!=null && uploaded!=null && edited.isAfter(uploaded.plusSeconds(90))) {
							return true;	
						}
						return false;
					}
					return true;
				}
			};
			editedby.setEscapeModelStrings(false);
			body.add(editedby);
			
			body.add(new ResourceEditor(model));
			body.add(new VersionsPanel(model) {
				@Override
				public boolean isVisible() {
					return showversions && super.isVisible();
				}
				@Override
				protected void onClose(AjaxRequestTarget target) {
					showversions = false;
					target.add(MetainfoResourceView.this);
				}
			});

			Panel menuPanel = getMenu();
			
			WebMarkupContainer menulink = new WebMarkupContainer("menulink") {
				public boolean isVisible() {
					return !isReadOnly();
				}
			};
			
			add(menulink);
			add(menuPanel);
			add(body);
		}

		public Component getImage(IModel<Resource> model) {	return null;}
		
		public Component getGlyphIcon(IModel<Resource> model) {return null;}
		
		public boolean isImageVisible() {
			return true;
		}
		
		@SuppressWarnings("unchecked")
		public void edit(AjaxRequestTarget target) {
			if (activeview!=null) {
				((MetainfoResourceView)activeview).closeEditor(target);
			}
			getEditor().enable(target);
			target.add(get("body"));
			activeview = this;
		}

		public void closeEditor(AjaxRequestTarget target) {
			getEditor().close(target);
			target.add(get("body"));
		}
		
		protected Panel getMenu() {
			
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(getModel());
			
			menu.addItem(id ->
				new DonwloadMenuItemPanelV5<Resource>(id) {
					@Override 
					public String getLabel() {
						return ResourcesPanel.this.getStringLabel("menu.download");
					}
					@Override
					protected File getFile() throws IOException {
						if (getModel().getObject() instanceof KBFile) {
							return ((KBFile) getModel().getObject()).getFile();
						}
						return null;
					}
					@Override
					public boolean isVisible() {
						return (getModel().getObject() instanceof KBFile);
					}
					@Override
					public boolean isEnabled()  {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						showversions = !showversions;
						target.add(MetainfoResourceView.this);
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.showversions");
					}
					@Override
					public boolean isVisible() {
						return getModel().getObject() instanceof KBFile;
					}
				}
			);
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						return isWriteable(getModel().getObject());
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						edit(target);
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.edit");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
								return false;
						return true;
					}
					@Override
					public boolean isEnabled() {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					}
				}	
			);
			
			menu.addItem(id ->
				new UploadMenuItemPanelV5<Resource>(id) {
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.upload-version");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (!(getModel().getObject() instanceof KBFile))
							return false;
						return true;
					}
					@Override
					public boolean isEnabled() {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					}
					@Override
					protected String getUploadUrl() {
						Content content = ResourcesPanel.this.getModelObject();
						String resourceid = String.valueOf(getModelObject().getId());
						return "/versionupload?id="+ content.getId() +"&class="+getContentClass(content)+"&resource="+resourceid;
					}	
					@Override
					protected String getRefreshFunction() {
						return "refreshfiles"+ResourcesPanel.this.getMarkupId()+"()";
					}
					@Override
					protected Component getResourcesView() {
						return ResourcesPanel.this.get("resources-view");
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						up(MetainfoResourceView.this.getModel());
						target.add(ResourcesPanel.this);
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.up");
					}
					@Override
					public boolean isVisible() {	
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						return MetainfoResourceView.this.getIndex()>0;
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						down(MetainfoResourceView.this.getModel());
						target.add(ResourcesPanel.this);
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.down");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						return MetainfoResourceView.this.getIndex()<getResources().size()-1;
					}
				}
			);
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						return isWriteable(getModel().getObject());
					};
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<Resource> model = MetainfoResourceView.this.getModel();
						moveToPrivateArea(model);
						target.add(ResourcesPanel.this);
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (!isPublicArea())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						if (!ResourcesPanel.this.getModelObject().getContentTemplate().isPrivateNotes())
							return false;
						return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(ResourcesPanel.this.getModelObject());
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getLabel("menu.movetoprivatearea", ResourcesPanel.this.getModelObject().getContentTemplate().getPrivate_notes_label()).getObject();
					}
					@Override
					public boolean isEnabled()  {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					};
				}
			);

			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<Resource> model = MetainfoResourceView.this.getModel();
						moveToPublicArea(model);
						target.add(ResourcesPanel.this);
					}
					@Override
					public boolean isVisible() {	
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (isPublicArea())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						if (!ResourcesPanel.this.getModelObject().getContentTemplate().isPrivateNotes())
							return false;
						return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(ResourcesPanel.this.getModelObject());
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.movetopublic");
					}
					@Override
					public boolean isEnabled()  {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					}
				}
			);

			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						((Dialog)ResourcesPanel.this.get("confirm-dialog")).open(target, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Ok.key())) {
									IModel<Resource> model = MetainfoResourceView.this.getModel();
									delete(MetainfoResourceView.this.getModel());
									try {
										IDoc idoc = ServiceLocator.getService(UserService.class).getUploadAndCreateContainer();
										if (model.getObject() instanceof KBFile) {
											idoc.addFile((KBFile) model.getObject());
											List<String> list = new ArrayList<String>();
											list.add("add file " + model.getObject().getTitle());
											
											long start=System.currentTimeMillis();
											idoc.getService(ContentService.class).update(list);
											logger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
										}
									} 
									catch (ContentMgmtException e) {
										logger.error(e, getSessionUser().getUserName());
									}
									target.add(ResourcesPanel.this.get("resources-view"));
								}
							}
						}, getModelObject().getTitle());
					}
					@Override
					public boolean isVisible() {	
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						return isWriteable(getModel().getObject());
					}
					@Override
					public String getLabel() {	
						return ResourcesPanel.this.getStringLabel("menu.delete-restore-bulkupload");
					}
					@Override
					public boolean isEnabled()  {
						if (isSupportUser() && !isRoot())
							return false;
						return true;
					}
				}
			);
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly())
							return false;
						if (!getEditor().isEditionEnabled())
							return false;
						if (!isSessionUserWorkspace())
							return false;
						if (! (getModel().getObject() instanceof KBFile)) 
							return false;
						return isWriteable(getModel().getObject());
					}
				}
			);
			
			menu.addItem(new MenuItemFactory<Resource>() {
				@Override
				public AbstractMenuItemPanelV5<Resource> getItem(String id) {
					return new AjaxMenuItemPanelV5<Resource>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							
							((Dialog)ResourcesPanel.this.get("remove-dialog")).open(target, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										delete(MetainfoResourceView.this.getModel());
										target.add(ResourcesPanel.this.get("resources-view"));
									}
								}
							}, getModelObject().getTitle());
						}
						@Override
						public boolean isVisible() {
							if (isReadOnly())
								return false;
							
							if (getModel().getObject() instanceof TreeFileResource) 
								return false;
							
							if (!getEditor().isEditionEnabled())
								return false;
							
							if (!isSessionUserWorkspace())
									return false;

							return isWriteable(getModel().getObject());
						}
						@Override
						public String getLabel() {	
							return ResourcesPanel.this.getStringLabel("menu.delete");
						}
						
						
						@Override
						public boolean isEnabled()  {
							if (isSupportUser() && !isRoot())
								return false;
							return true;
						}
						
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<Resource>() {
				@Override
				public AbstractMenuItemPanelV5<Resource> getItem(String id) {
					return new LinkMenuItemPanel<Resource>(id) {
						@Override
						public void onClick() {
							TreeFile treefile = ((TreeFileResource)getModel().getObject()).getTreeFile();
							while (treefile.getParent()!=null) {
								treefile = treefile.getParent();
							}
							IModel<TreeFile> treemodel = new ObjectModel<TreeFile>(treefile);
							setResponsePage(new TreeFilePage(treemodel));
						}
						@Override
						public boolean isVisible() {
							if (getModel().getObject() instanceof TreeFileResource) 
								return true;
							return false;
						}
						@Override
						public String getLabel() {	
							return ResourcesPanel.this.getStringLabel("menu.treefile");
						}
						@Override
						public String getTarget() {	
							return "_blank";
						}
						@Override
						public boolean isEnabled()  {
							return true;
						}
					};
				}
			});

			
			return menu;
		}
	
		@SuppressWarnings("unchecked")
		protected ResourceEditor getEditor() {
			return ((ResourceEditor)MetainfoResourceView.this.get("body:editor"));
		}
	}

	public class ResourceView extends Fragment {
		private IModel<Resource> model;
		private int index;
		public ResourceView(String id, String markupid, IModel<Resource> model) {
			super(id, markupid, ResourcesPanel.this);
			setModel(model);
		}
		public void setModel(IModel<Resource> model) {
			this.model = model;
		}
		public IModel<Resource> getModel() {
			return model;
		}
		public Resource getResource() {
			return getModel().getObject();
		}
		public int getIndex() {
			return index;
		}
		@Override
		public void onDetach() {
			if (model!=null)
				model.detach();
			super.onDetach();
		}
		protected void setIndex(int index) {
			this.index = index;
		}
	}
	

	public class InitialBlockPanel extends Fragment {
		public InitialBlockPanel(String id, String markupid) {
			super(id, markupid, ResourcesPanel.this);
		}
	}
	
	public class NewExternalResourceEditor extends Fragment implements Editor<NewExternalResourceData> {

		private IModel<NewExternalResourceData> model;
		private IModel<ResourceTag> groupmodel;

		public NewExternalResourceEditor(IModel<NewExternalResourceData> model) {
			super("editor", "editor-fragment", ResourcesPanel.this);
			
			setOutputMarkupId(true);
			
			setModel(model);
			
			Form<NewExternalResourceData> form = new com.novamens.wicket.markup.html.form.Form<NewExternalResourceData>("form", Disposition.VERTICAL);
			form.setOutputMarkupId(true);
			 
			form.add(new TextField<String>("title", true) {
				@Override
				public boolean autofocus() {
					return true;
				}
			});
			
			form.add(new TextAreaField<String>("description", 15, 20));
			
			form.add(new BooleanField("inPortalVersion") {
				public boolean isVisible() {
					return isPublicArea();
				}
			});
			
			form.add(new TextField<String>("Url", true));
			
			
			form.add(new ChoiceField<ResourceTag>("group", new PropertyModel<ResourceTag>(this, "group") ,()->getGroups()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
				}
				public boolean isVisible() {
					return false;
				}
			});
						
			form.add(new AjaxSubmitLink("save-link", form)	{
				public void onSubmit(AjaxRequestTarget target) {
					addExternal(getModelObject());
					getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
						@Override
						public void component(Field<?> field, IVisit<Void> visit) {
							field.cancel();
						}
					});
					ResourcesPanel.this.get("resources-view:new-resource-editor").setVisible(false);;
					refresh(target);
				}
				@Override
				protected void onError(AjaxRequestTarget target) {
					form.visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
						@Override
						public void component(Field<?> field, IVisit<Void> visit) {
							if (field.hasErrorMessage()) {
								target.focusComponent(field.getInput());
								visit.stop();
							}
						} 
					});
					target.add(form);
				}
			});
			
			form.add(new AjaxLink<Void>("cancel-link")	{
				public void onClick(AjaxRequestTarget target) {
					getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
						@Override
						public void component(Field<?> field, IVisit<Void> visit) {
							field.cancel();
						}
					});
					ResourcesPanel.this.get("resources-view:new-resource-editor").setVisible(false);;
					refresh(target);
				}
			});
			add(form);
		}
		public void update(AjaxRequestTarget target) {
		}
		public void update(NewExternalResourceData resource) {
		}
		public void edit(AjaxRequestTarget target) {
		}
		public void cancel(AjaxRequestTarget target) {
		}
		public void setModel(IModel<NewExternalResourceData> model) {
			this.model = model;
		}
		@Override
		public IModel<NewExternalResourceData> getModel() {
			return model;
		}
		@Override
		public Form<?> getForm() {
			return (Form<?>)get("form");
		}
		@Override
		public NewExternalResourceData getModelObject() {
			return getModel().getObject();
		}
		@Override
		public boolean isEditionEnabled() {
			return true;
		}
		@Override
		public List<String> getUpdatedParts() {
			return null;
		}
		@Override
		public void setUpdatedPart(String updatedPart) {
		}
		@Override
		public List<UpdatedField> getUpdatedFields() {
			return null;
		}
		@Override
		public void setUpdatedField(UpdatedField updatedField) {
		}
		@Override
		public boolean isReadOnly() {
			return false;
		}
		@Override
		public boolean isFullWidth() {
			return false;
		}
		@Override
		public boolean isNew() {
			return false;
		}
		@Override
		public void setIsNew(boolean isnew) {
		}
		public ResourceTag getGroup() {
			return groupmodel!=null ? groupmodel.getObject() : null;
		}
		public void setGroup(ResourceTag group) {
			groupmodel = new ObjectModel<ResourceTag>(group);
		}
	}
	
	public class VersionsPanel extends Fragment  {

		private IModel<Resource> model;

		public VersionsPanel(IModel<Resource> model) {
			super("versions", "versions-fragment", ResourcesPanel.this);
			
			setModel(model);
			
			add(new AjaxLink<Void>("close") {
				public void onClick(AjaxRequestTarget target) {
					VersionsPanel.this.onClose(target);
				}
			});
			
			add(new ListView<IModel<Resource>>("version", getVersions()) {
				protected void populateItem(ListItem<IModel<Resource>> item) {
					IModel<Resource> model = item.getModelObject();
					Resource resource = model.getObject();
					WebMarkupContainer link = new ResourceLink<T>("link", model);
					link.add(new Label("name", resource.getDisplayName()));
					item.add(link);
					item.add(new Label("version", resource.getVersion()));
					item.add((new Label("date", resource.getLastModifiedOffsetDateTimeColloquial())).setEscapeModelStrings(false));
					item.add(new Label("user", resource.getLastModifiedUser().getDisplayName()));
					model.detach();
				}
			});
		}
		public Resource getResource() {
			return model.getObject();
		}
		public List<IModel<Resource>> getVersions() {
			List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
			Resource version = model.getObject();
			while (version.getPreviousVersion()!=null) {
				resources.add(new ProxyModel<Resource>(version.getPreviousVersion()));
				version = version.getPreviousVersion();
			}
			return resources;
		}
		public IModel<Resource> getModel() {
			return this.model;
		}
		public void setModel(IModel<Resource> model) {
			this.model = model;
		}
		@Override
		public boolean isVisible() {
			return getResource() instanceof KBFile && getResource().getPreviousVersion()!=null;
		}
		@Override
		public void onDetach() {
			super.onDetach();
			model.detach();
		}
		protected void onClose(AjaxRequestTarget target) {
			
		}
	}	
	
	/**
	 *
	 */
	public class ResourceEditor extends Fragment implements Editor<Resource> {
		
		private boolean enabled = false;

		private IModel<Resource> model;
		private IModel<ResourceTag> groupmodel;
		private boolean groupupdated = false;

		public ResourceEditor(IModel<Resource> model) {
			super("editor", "editor-fragment", ResourcesPanel.this);
			
			setOutputMarkupId(true);
			setModel(model);
			
			setGroup(ResourcesPanel.this.getGroup(model.getObject()));
			
			Form<Resource> form = new com.novamens.wicket.markup.html.form.Form<Resource>("form", Disposition.VERTICAL);
			form.setOutputMarkupId(true);
			
			form.add(new TextField<String>("title", true) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updated = true;
				}
				@Override
				public String getPart() {
					return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
				}
				protected boolean autofocus() {
					return true;
				}
			});
			
			form.add(new TextAreaField<String>("description", 12, 20) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updated = true;
				}
				@Override
				public String getPart() {
					return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
				}
			});

			if (model.getObject() instanceof ExternalResource) {
				form.add(new TextField<String>("Url", true) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						updated = true;
					}
					@Override
					public String getPart() {
						return ResourceEditor.this.getModel().getObject().getName() + " " +super.getPart();
					}
				});
			}
			else
				form.add( (new Label("Url", "")).setVisible(false));

			form.add(new BooleanField("inPortalVersion") {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updated = true;
				}
				public boolean isVisible() {
					return ResourcesPanel.this.isPublicArea();
				}
			});
			
			form.add(new ChoiceField<ResourceTag>("group", new PropertyModel<ResourceTag>(this, "group") ,()->getGroups()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					groupupdated = true;
				}
				public boolean isVisible() {
					return !ResourcesPanel.this.getGroups().isEmpty();
				}
			});
						
			form.add(new AjaxSubmitLink("save-link", form) {
				public void onSubmit(AjaxRequestTarget target) {
					if (groupupdated) {
						ResourcesPanel.this.setGroup(getModelObject(), getGroup());
						target.add(ResourcesPanel.this);
					}
					if (updated) {
						getModelObject().setLastModifiedOffsetDateTime(OffsetDateTime.now());
						getModelObject().setLastModifiedUser(getSessionUser());
					}
					enabled = false;
					updated = true;
					target.add(ResourceEditor.this.getParent().getParent());
				}
			});
			
			form.add(new AjaxLink<Void>("cancel-link") {
				public void onClick(AjaxRequestTarget target) {
					enabled = false;
					getForm().visitChildren(Field.class, new IVisitor<Field<?>, Void>() {
						@Override
						public void component(Field<?> field, IVisit<Void> visit) {
							field.cancel();
						}
					});
					target.add(ResourceEditor.this.getParent());
				}
			});
			add(form);
		}
		@SuppressWarnings("unchecked")
		public void enable(AjaxRequestTarget target) {
			enabled = true;
			((TextField<String>)get("form:title")).onBeforeRender();
			target.add(ResourceEditor.this.getParent());
			target.focusComponent(((TextField<String>)get("form:title")).getInput());
		}
		public void setModel(IModel<Resource> model) {
			this.model = model;
		}
		public IModel<Resource> getModel() {
			return model;
		}
		public void update(AjaxRequestTarget target) {
		}
		public void update(Resource resource) {
		}
		public void edit(AjaxRequestTarget target) {
		}
		public void close(AjaxRequestTarget target) {
			enabled = false;
		}
		public Form<?> getForm() {
			return ResourcesPanel.this.getEditor().getForm();
		}
		public Resource getModelObject() {
			return getModel().getObject();
		}
		public boolean isEditionEnabled() {
			return true;
		}
		public boolean isReadOnly() {
			return false;
		}
		public List<String> getUpdatedParts() {
			return null;
		}
		public void setUpdatedPart(String updatedPart) {
			ResourcesPanel.this.setUpdatedPart(updatedPart);
		}
		public List<UpdatedField> getUpdatedFields() {
			return null;
		}
		public void setUpdatedField(UpdatedField updatedField) {
		}
		@Override 
		public boolean isVisible() {
			return enabled;
		}
		@Override
		public boolean isFullWidth() {
			return false;
		}
		@Override
		public boolean isNew() {
			return false;
		}
		@Override
		public void setIsNew(boolean isnew) {
			
		}
		public ResourceTag getGroup() {
			return groupmodel!=null ? groupmodel.getObject() : getDefaultGroup();
		}
		public void setGroup(ResourceTag group) {
			groupmodel = group!=null ? new ObjectModel<ResourceTag>(group) : null;
		}
	}
	

	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			fire(new EditorEvent(target));
			setResources(getResources(((ResourceContainer)getModelObject())));
			refresh(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refreshfiles"+ResourcesPanel.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshfiles"+ResourcesPanel.this.getMarkupId()));
		}
	}


	
	/** ---------------------------------------------------------------------------------------. 
	 * Constructor
	 */
	public ResourcesPanel() {
		this("resources");
	}
	

	/** ---------------------------------------------------------------------------------------. 
	 * Constructor
	 */
	public ResourcesPanel(String id, PageParameters parameters) {
		super(id);
		setPublicArea(true);
		stAdd();
	}

	
	
	public ResourcesPanel(String id) {
		this(id, true);
	}

	/** 
	 * isPublicArea: Private Notes or Resources
	 */
	public ResourcesPanel(String id, boolean isPublicArea) {
		super(id);
		setPublicArea(isPublicArea);
		stAdd();
	} 
	
	
	private void stAdd() {
		initSettings();
		
		add(new RefreshBehavior());
		
		add(new Dialog("remove-dialog", "dialog.delete.title", "dialog.delete.message", Dialog.Cancel, Dialog.Delete));
		add(new Dialog("confirm-dialog", "dialog.confirm.title", "dialog.confirm.message", Dialog.Cancel, Dialog.Ok));
		
		add((new InitialBlockPanel("initial-block", "initial-block-fragment") {
			public boolean isVisible() {
				return getEditor().isEditionEnabled() && (getResources()==null || getResources().size()==0);
			}
		}).setOutputMarkupId(true));
		
		
		// Can not be placed in the addListeners method because it executes before setting setPublic
		//
		//
		add(new WicketEventListener<ResourceListUpdateEvent>() {
			public void onEvent(ResourceListUpdateEvent event) {
				if (event.must_refresh_public_list() && isPublicArea())
					received_from_another_tab = true;
				else if (!event.must_refresh_public_list() && !isPublicArea())
					received_from_another_tab = true;
			}
		});
		
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		
		if (this.resources == null) {
			addOrReplace(new Toolbar() {
				public boolean isVisible() {
					return isToolBarVisible();
				}
			});
			setResources(getResources(((ResourceContainer)getModelObject())));
		}
		else {
			if (getResources(((ResourceContainer)getModelObject())).size()!=getResources().size()) {
				setResources(getResources(((ResourceContainer)getModelObject())));
			}
		}
		
		addResourcesView();
	}

	@Override
	public void updateModel() {
		
		if (!this.updated) 
			return;
		
		List<Resource> resources = new ArrayList<Resource>();
		
		for(IModel<Resource> model : getResources()) {
			resources.add(model.getObject());
		}
		
		((KbeeResourceContainer)getModel().getObject()).setResources(resources, isPublicArea());
		this.updated = false;
	}

	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode=mode;
	}
	
	public List<IModel<Resource>> getResources() {
		return this.resources;
	}
	
	public List<IModel<Resource>> getResources(IModel<ResourceTag> model) {
		List<IModel<Resource>> resources = getResources(model.getObject());
		model.detach();
		return resources;
	}
	
	public List<IModel<Resource>> getResources(ResourceTag group) {
		List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
		for (IModel<Resource> model : this.resources) {
			ResourceContainer content = (ResourceContainer)getModelObject();
			Resource resource = model.getObject();
			ResourceTag resouregroup = content.getTag(resource);
			if (resouregroup == null) {
				resouregroup = getDefaultGroup();
			}
			if (resouregroup!=null && resouregroup.equals(group)) {
				resources.add(model);
			}
		}
		return resources;
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		if (!getEditor().isEditionEnabled())
			return;

		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(UploadAction.class,"js/plupload/plupload.full.min.js")));
		
		String resourcesview = get("resources-view").getMarkupId();
		
		String script = "var uploader = new plupload.Uploader({"+
				"runtimes : 'html5, html4',"+
				"browse_button : 'pickfiles', "+
				"drop_element: 'resources-panel', "+
				"url : \"/upload?id="+ getModel().getObject().getId() +
					"&class="+getContentClass(getModel().getObject())+
					"&public="+String.valueOf(isPublicArea())+
					"&group="+getDefaultGroupId()+"\", " +
				"filters : {"+
				"	max_file_size : '300000mb',"+
				"	mime_types: ["+
				"		{title : \"Image files\", extensions : \"jpg,gif,png,webp\"},"+
				"		{title : \"Pdf files\",   extensions : \"pdf\"},"+
				"		{title : \"Zip files\",   extensions : \"zip\"},"+
				"		{title : \"All files\",   extensions : \"*\"},"+
				"		{title : \"MS Office\",   extensions : \"doc,docx,xls,xlsx,ppt,pptx\"}"+
				"	]"+
				"},"+
				"flash_swf_url : '/plupload/js/Moxie.swf',"+
				"silverlight_xap_url : '/plupload/js/Moxie.xap',"+
				"init: {"+
				"	PostInit: function() {"+
				"		document.getElementById('pickfiles').onclick = function() {"+
				"			uploader.start();"+
				"			return false;"+
				"		};"+
				"	},"+
				"	FilesAdded: function(up, files) {"+
				"		top.filesUploaded = 0; " + 
				"		top.filesAdded = 0; "+ 
				"		plupload.each(files, function(file) {"+
				"			file.name = file.name.replace('á', 'a'); file.name = file.name.replace('Á', 'A'); "+
				"			file.name = file.name.replace('é', 'e'); file.name = file.name.replace('É', 'E'); "+
				"			file.name = file.name.replace('í', 'i'); file.name = file.name.replace('Í', 'I'); "+
				"			file.name = file.name.replace('ó', 'o'); file.name = file.name.replace('Ó', 'O'); "+
				"			file.name = file.name.replace('ú', 'u'); file.name = file.name.replace('Ú', 'U'); "+
				"			file.name = file.name.replace('ñ', 'n'); file.name = file.name.replace('Ñ', 'N'); "+
				"			file.name = file.name.replace('æ', 'a'); file.name = file.name.replace('#', '-'); "+
				"			top.filesAdded = top.filesAdded+1; "+
				"			document.getElementById('"+resourcesview+"').innerHTML = " +
				
				"'<div>" +
				"<span class=\"file-name-upload\" >' + file.name + '</span> <span class=\"file-size-upload\"> (' + plupload.formatSize(file.size) + ')</span>" +
				"<div class=\"progress\">" +
		 		"	<div id=\"' + file.id + '\" class=\"progress-bar\" role=\"progressbar\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">"+
				"		<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%' + '</span>"+
		  		"	</div>"+
				"</div>"+
				"</div>' + document.getElementById('"+resourcesview+"' ).innerHTML;"+
				"			setTimeout(function () { uploader.start(); }, 500);"+
				"		})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		top.filesUploaded=top.filesUploaded+1; " + 
				"		if (top.filesUploaded>=top.top.filesAdded) { " + 
				"			setTimeout(function () { refreshfiles"+getMarkupId()+"(); }, 500);" + 
				"		};"+
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById(file.id).style.width = file.percent+'%';"+
				"		document.getElementById(file.id).innerHTML = '<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%</span>';"+
				"	},"+
				"	Error: function(up, err) {"+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message;"+
				"		setTimeout(function () { refreshfiles"+getMarkupId()+"(); }, 2000);"+
				"	}"+
				"}"+
				"});"+
				"uploader.init();";

		if (isEnabled()) {
			response.render(OnLoadHeaderItem.forScript(script));
		}
	}

	public boolean isToolBarVisible() {
		return this.toolbar_visible;
	}
	
	public void setToolbarBarVisible(boolean visible) {
		this.toolbar_visible=visible;
	}

	public boolean isReadOnly() {
		return getEditor().isReadOnly();
	}
	
	public boolean isPublicArea() {
		return ispublic_area;
	}
	
	public void setPublicArea(boolean value) {
		ispublic_area = value;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.resources!=null) {
			for (IModel<Resource> model : this.resources) 
				model.detach();
		}
		if (this.deleted!=null) {
			for (IModel<Resource> model : this.deleted) 
				model.detach();
		}
	}
	
	protected List<Resource> getResources(ResourceContainer content) {
		return content.getResources(isPublicArea());
	}
	
	protected void refresh(AjaxRequestTarget target) {
		int size = getResources().size();
		if (size<=1)
			target.add(ResourcesPanel.this.get("initial-block"));
		target.add(ResourcesPanel.this);
	}


	protected void setResources(List<Resource> resources) {
		this.resources = new ArrayList<IModel<Resource>>();
		for (Resource resource : resources) {
			if (resource!=null && !deleted(resource)) {
				this.resources.add(new ResourceModel(resource));
			}
		}
	}

	protected void addResourcesView() {
		
		WebMarkupContainer view = new WebMarkupContainer("resources-view");
		view.setOutputMarkupId(true);
		
		NewExternalResourceEditor editor = new NewExternalResourceEditor(new Model<NewExternalResourceData>(new NewExternalResourceData()));

		WebMarkupContainer editorcontainer = new WebMarkupContainer("new-resource-editor");
		editorcontainer.add(editor);
		editorcontainer.setVisible(false);
		view.add(editorcontainer);
		
		
		view.add(new ListView<IModel<ResourceTag>>("groups-list", () -> getGroupsModels()) {
			public boolean isVisible() {
				return !getGroups().isEmpty() && isPublicArea();
			}
			protected void populateItem(ListItem<IModel<ResourceTag>> item) {
				ResourceTag group = item.getModelObject().getObject();
				item.add(new Label("group-label", group.getName()));
				item.add(new ListView<IModel<Resource>>("resources-list", () -> getResources(item.getModelObject())) {
					protected void populateItem(ListItem<IModel<Resource>> item) {
						switch (getViewMode()) {
							case NOIMAGE: {
								item.add(new IconResourceView("resource-view", item.getModelObject(), item.getIndex()) {
									public boolean isImageVisible() {
										return false;
									}
								});
								break;
							}
							case ICON: {
								item.add(new IconResourceView("resource-view", item.getModelObject(), item.getIndex()));
								break;
							}
							case THUMBNAIL: {
								item.add(new ThumbnailResourceView("resource-view", item.getModelObject(), item.getIndex()));
								break;
							}
							case THUMBNAIL_LARGE: {
								item.add(new ThumbnailLargeResourceView("resource-view", item.getModelObject(), item.getIndex()));
								break;
							}
						}
						
						item.add(new AttributeModifier("data-id", "resource_"+item.getModelObject().getObject().getId()));
						item.add(new AttributeModifier("class", getViewMode().getElementCss())); // grid2,3,4
					}
				});
			}
		});
		
		view.add(new ListView<IModel<Resource>>("resources-list", new PropertyModel<List<IModel<Resource>>>(this, "resources")) {
			public boolean isVisible() {
				return getGroups().isEmpty() || !isPublicArea();
			}
			protected void populateItem(ListItem<IModel<Resource>> item) {
				switch (getViewMode()) {
					case NOIMAGE: {
						item.add(new IconResourceView("resource-view", item.getModelObject(), item.getIndex()) {
							public boolean isImageVisible() {
								return false;
							}
						});
						break;
					}
					case ICON: {
						item.add(new IconResourceView("resource-view", item.getModelObject(), item.getIndex()));
						break;
					}
					case THUMBNAIL: {
						item.add(new ThumbnailResourceView("resource-view", item.getModelObject(), item.getIndex()));
						break;
					}
					case THUMBNAIL_LARGE: {
						item.add(new ThumbnailLargeResourceView("resource-view", item.getModelObject(), item.getIndex()));
						break;
					}
				}
				
				item.add(new AttributeModifier("data-id", "resource_"+item.getModelObject().getObject().getId()));
				item.add(new AttributeModifier("class", getViewMode().getElementCss())); // grid2,3,4
			}
		});
		
		view.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getViewMode().getListCss();
			}
		})); 
		
		addOrReplace(view);
	}
	
	protected void onContainerFluid(AjaxRequestTarget target) {
	}

	protected String getStringLabel(String resourceKey) {
		return ((new StringResourceModel(resourceKey, this, null)).getString());
	}

	protected void delete(IModel<Resource> model) {
		int index = getIndex(model);
		this.resources.remove(index);
		this.deleted.add(model);
		setUpdatedPart("delete "+model.getObject().getName());
		this.updated = true;
	}

	protected void moveToPrivateArea(IModel<Resource> model) {
		if (model.getObject() instanceof KBFileImpl) {
			((ResourceContainer)getModelObject()).setPrivate(model.getObject());
 			try {
				getModelObject().getService(ContentService.class).update(model.getObject(), model.getObject().getTitle() + " ->  Private Area");
				int index = getIndex(model);
				this.resources.remove(index);
				this.updated = true;
			} 
			catch (ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
			}
		} 
		else {
			if (model.getObject() instanceof TreeFile) {
				throw new KbeeRuntimeException("TreeFile not supported");
			}
		}
	}

	protected void moveToPublicArea(IModel<Resource> model) {
		if (model.getObject() instanceof KBFileImpl) {
			((ResourceContainer)getModelObject()).setPublic(model.getObject());
			try {
				getModelObject().getService(ContentService.class).update(model.getObject(), model.getObject().getTitle() + " ->  Public Area");
				int index = getIndex(model);
				this.resources.remove(index);
				this.updated = true;
			} 
			catch (ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
			}
		}
	}
	
	protected void setGroup(Resource resource, ResourceTag group) {
		((ResourceContainer)getModelObject()).setTag(resource, group);
		try {
			getModelObject().getService(ContentService.class).update(resource, resource.getTitle() + " ->  "+ group.getName());
			setResources(((ResourceContainer)getModelObject()).getResources(isPublicArea()));
		} 
		catch (ContentMgmtException | ServiceNotFoundException e) {
			logger.error(e);
		}
	}
	
	protected void sort(List<String> ids) {
		if (ids.size()!=getResources().size())
			return;
		List<IModel<Resource>> sorted = new ArrayList<IModel<Resource>>();
		int sortedindex = 0;
		boolean orderupdate = false;
		for (String id : ids) {
			int index = 0;
			boolean found = false;
			for (IModel<Resource> model : getResources()) {
				if (String.valueOf(model.getObject().getId()).equals(id)) {
					found = true;
					break;
				}
				else
					index++;
			}
			if (found) {
				if (sortedindex!=index) {
					orderupdate = true;
				}
				sorted.add(getResources().get(index));
			}	
		}
		if (sorted.size()==getResources().size() && orderupdate) {
			this.resources = sorted;
			setUpdatedPart("resources order");
			updated = true;
		}
	}
	
	
	@Override
	protected void addListeners() {
		super.addListeners();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	private DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}
	
	private boolean isActivityResource(Resource resource) {
		List<String> activityResources =  getModel().getObject().getService(ContentService.class).getActivityResources();
		for (String resourceid : activityResources) {
			if (resourceid.equals(String.valueOf((Long)resource.getId()))) {
				return true;
			}
		}
		return false;
	}
	
	private KbeeUser getSessionUser() {
		return  (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private void down(IModel<Resource> model) {
		int index = getIndex(model);
		IModel<Resource> downfile = this.resources.get(index+1);
		this.resources.set(index+1, model);
		this.resources.set(index, downfile);
		updated = true;
		setUpdatedPart("resources order");
	}
	
	private void up(IModel<Resource> model) {
		int index = getIndex(model);
		IModel<Resource> upfile = this.resources.get(index-1);
		this.resources.set(index-1, model);
		this.resources.set(index, upfile);
		this.updated = true;
		setUpdatedPart("resources order");
	}

	private int getIndex(IModel<Resource> model) {
		int index = 0;
		Resource resource = model.getObject();
		for (IModel<Resource> resourcemodel : resources) {
			if (resource.getId().equals(resourcemodel.getObject().getId()))
				break;
			else
				index++;
		}
		return index;
	}
	
	private boolean deleted(Resource resource) {
		for (IModel<Resource> model : deleted) {
			if (resource.getId().equals(model.getObject().getId())) {
				return true;
			}
		}
		return false;
	}
	
	private List<ResourceTag> getGroups() {
		return getModelObject().getContentTemplate().getResourceTags();
	}
	
	private List<IModel<ResourceTag>> getGroupsModels() {
		List<IModel<ResourceTag>> models = new ArrayList<IModel<ResourceTag>>();
		for (ResourceTag group : getModelObject().getContentTemplate().getResourceTags()) {
			models.add(new ObjectModel<ResourceTag>(group));
		}
		return models;
	}
	
	private ResourceTag getGroup(Resource resource) {
		return ((ResourceContainer)getModelObject()).getTag(resource);
	}

	private String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase(); // TODO VER LOWERCASE
	}
	
	private String getDefaultGroupId() {
		if (defaultGroupId==null) {
			defaultGroupId = getGroups().isEmpty() ? "" : String.valueOf(((KbeeResourceTag)getGroups().get(0)).getId());
		}
		return defaultGroupId; 
	}
	
	private ResourceTag getDefaultGroup() {
		return getGroups().isEmpty() ? null : getGroups().get(0);
	}
	
	private void initSettings() {
		
		int view = getSessionUser().getService(PreferencesService.class).getIntValue(ResourcesPanel.class.getSimpleName(),"viewmode", ViewMode.ICON.ordinal());
		
		if		(ViewMode.ICON.ordinal()==view)					ResourcesPanel.this.viewmode = ViewMode.ICON;
		else if (ViewMode.NOIMAGE.ordinal()==view)				ResourcesPanel.this.viewmode = ViewMode.NOIMAGE;
		else if (ViewMode.THUMBNAIL.ordinal()==view)			ResourcesPanel.this.viewmode = ViewMode.THUMBNAIL;
		else if (ViewMode.THUMBNAIL_LARGE.ordinal()==view)		ResourcesPanel.this.viewmode = ViewMode.THUMBNAIL_LARGE;
	}
	
	private void addExternal(NewExternalResourceData data) {

		KbeeExternalResource resource = new KbeeExternalResource();
		int index = getResources().size() + 1;
		
		resource.setName("link " + String.valueOf(index));
		resource.setTitle(data.getTitle());
		resource.setUrl(data.getUrl());
		resource.setInPortalVersion(data.isInPortalVersion());
		resource.setDescription(data.getDescription());
		//resource.setGroup(getDefaultGroup());
		
		Domain domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		
		resource.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		resource.setLastModifiedUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
		
		resource.setDomain(domain);
		
		try {
			getModel().getObject().getService(ContentService.class).addExternalResource(resource);
			this.resources.add(new ResourceModel(resource));
			this.updated =true;
			setUpdatedPart("add link ");
		} 
		catch (ContentMgmtException e) {
			logger.error(e, getSessionUser().getUserName());
		}
	}
	
	private boolean isSessionUserWorkspace() {
		return (ResourcesPanel.this.getModelObject().getWorkspace()!=null && ResourcesPanel.this.getModelObject().getWorkspace().equals((Long) getSessionUser().getId()));
	}

	/**
	 * @param resource
	 * @return
	 */
	private boolean isWriteable(Resource resource) {
		if (isActivityResource(resource)) 
			return true;
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		if (service.isRoot())
			return true;
		
		boolean value;
		WorkflowService ws = getModelObject().getService(WorkflowService.class);
		if (ws!=null && ws.getTask()!=null) {
			value = ((WebTask)ws.getTask()).getEnableEditingAllResources();
		}
		else {
			ContentSystemSecurityService content_service  = (ContentSystemSecurityService) ServiceLocator.getService(ContentSystemSecurityService.class);
			Acl acl = (Acl) content_service.getAcl(getModelObject());
			
			value = acl.checkPermission(getSessionUser(), KbeePermission.WRITE);
		}
		
		return value;
	}
}
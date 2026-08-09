package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.ResourceRemoved;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.CloseResourceVersionsPanelEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.UploadMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.page.InvisibleImage;
import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;
import kbee.web.uploader.UploadBehavior;

@SuppressWarnings("serial")
public class EResourcePanel extends EFieldPanel<KbeeEResource> implements ResourcesPanel, IFormModelUpdateListener  {
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EResourcePanel.class.getName());

	private ViewMode viewmode = ViewMode.ICON;
	
	private IModel<Resource> resourceModel;
	private IModel<Content> contentModel;
	private boolean updated = false;
	private WebMarkupContainer layout;

	public class ControlFragment extends Fragment {
		
		private  boolean fmt_helpvisible = false;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourcePanel.this);
			
			setOutputMarkupId(true);
			
			addOrReplaceView();
			
			String s=getField().getSublabel();
			Label ls=new Label("subtitle", 
					s!=null?
					new Model<String>(getField().getSublabel())
					: "");
			ls.setEscapeModelStrings(false);
			ls.setVisible(getField().getSublabel()!=null);
			add(ls);
			
			
			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles") {
				public boolean isVisible() {
					return getResourceModel()==null && !isReadOnly() && EResourcePanel.this.isEnabled() && isEditionEnabled();
				}
			};
			pickfiles.setOutputMarkupId(true);
			add(pickfiles);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
				public boolean isVisible() {
					return getResourceModel()!=null ;
				}
			};
			menu.setOutputMarkupId(true);
			menu.add(getMenu(getResourceModel()));
			add(menu);
			
			Label helplabel = new Label("help", () -> getHelpText()) {
				public boolean isVisible() {
					return fmt_helpvisible;
				}
			};
			helplabel.setVisible(false);
			AjaxLink<?> helplink = new AjaxLink<Void>("help-link") {
				public void onClick(AjaxRequestTarget target) {
					fmt_helpvisible = !fmt_helpvisible;
					target.add(getContainer());
				}
				public boolean isVisible() {
					return getHelpText()!=null;
				}
			};
			
			
			
			add(new UploadMenuItemPanelV5<Void>("upload-link") {
				@Override
				public String getLabel() {	
					return getLabelString("menu.upload-version");
				}
				@Override
				protected String getUploadUrl() {
					return EResourcePanel.this.getUploadUrl();
				}	
				@Override
				protected String getRefreshFunction() {
					Component editor = getEditor(EResourcePanel.this);
					return editor!=null ? "refreshfiles"+editor.getMarkupId()+"('"+EResourcePanel.this.getControl().getMarkupId()+"');" : "";
				}
				@Override
				protected Component getResourcesView() {
					return EResourcePanel.this.get("container:control:resource-view");
				}
				
				@Override
				public boolean isVisible() {
					return getField().isToolbar() && !isReadOnly() && isEditionEnabled() && getResourceModel()!=null ;
				}
			});
			
			add(helplink);
			add(helplabel);
		}
		public void refresh(AjaxRequestTarget target) {
			addOrReplaceView();
			target.add(this);
		}
		private void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resource-view");
			view.setOutputMarkupId(true);
			if (getResourceModel()!=null) {
				view.add(new ResourceViewPanel<Content>("view", getResourceModel(), getContentModel()) {
					public ViewMode getViewMode() {
						return EResourcePanel.this.getViewMode();
					}
				});
			}
			else {
				view.add(new InvisibleImage("view"));				
			}
			view.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getViewMode().getListCss();
				}
			})); 
			addOrReplace(view);
		}
	}	
	
	/*** -----------------------------------------------------------
	 * 
	 * @param id
	 * @param field
	 * @param data
	 * 
	 */
	public EResourcePanel(String id, KbeeEResource field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
	}
	
	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode = mode;
	}
	
	public void setContent(IModel<Content> model) {
		this.contentModel = model;
	}
	
	public void setContent(Content content) {
		this.contentModel = new ObjectModel<Content>(content);
	}
	
	public IModel<Content> getContentModel() {
		return contentModel;
	}
	
	public IModel<Resource> getResourceModel() {
		return resourceModel;
	}
	
	public Resource getResource() {
		return resourceModel!=null ? resourceModel.getObject() : null;
	}
	
	public void clear() {
		Resource resource = getResource();
		setResourceModel(null);
		updated = true;
		fireScanAll(new EAjaxFormEvent(null, getField(), getData()));
		setUpdatedField(new ResourceRemoved(getData().getForm(), getLabel(), resource));
	}	
	
	@Override
	public void add(Resource resource) {
		setResourceModel(resource);
		getData().setData(getField(), getResourceModel());
		updated = true;
		fireScanAll(new EAjaxFormEvent(null, getField(), getData()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), resource));
		getEditor().update((AjaxRequestTarget)null);
	}

	@Override
	public void addVersion(Resource resource, Resource version) {
		add(version);
	}
	
	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<CloseResourceVersionsPanelEvent<?>>() {
			@Override
			public void onEvent(CloseResourceVersionsPanelEvent<?> event) {
				if ( event.getId().equals(getResourceModel().getObject().getId()) &&   EResourcePanel.this.getContainer().get("versions").isVisible()) {
					EResourcePanel.this.getContainer().addOrReplace(new InvisiblePanel("versions"));
					event.getRequestTarget().add( EResourcePanel.this.getContainer());
				}
			}
		});
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}

	@Override
	public void updateModel() {
		if (updated) {
			getData().setData(getField(), getResourceModel());
			updated = false;
		}
	}
		
	/** --------------------------------------------
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setResourceModel();
		
		String p = getPreference("viewmode",  ViewMode.ICON.getLabel());
	    viewmode = p.equals(ViewMode.ICON.getLabel()) ? ViewMode.ICON : ViewMode.THUMBNAIL;
		
		
		getContainer().add(new InvisiblePanel("versions"));
		
		
		layout = new WebMarkupContainer("horizontal-layout");
		layout.setOutputMarkupId(true);
		getContainer().add(layout);

		if (getDisposition()==Disposition.HORIZONTAL) {
			layout.add(new ControlFragment("control"));
			getContainer().add(new InvisiblePanel("control"));
		}
		else {
			getContainer().add(new ControlFragment("control"));
			layout.add(new InvisiblePanel("control"));
			layout.setVisible(false);
		}
	
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		getControl().add(new UploadBehavior() {
			@Override
			public boolean isEnabled() {
				return EResourcePanel.this.isEnabled() && !isReadOnly() && isEditionEnabled();
			}
			@Override
			protected String getUrl() {
				return EResourcePanel.this.getUploadUrl();
			}
			@Override
			protected String getDropElement() {
				return getContainer().getMarkupId();
			}
			@Override
			public Component getResourcesPanel() {
				return EResourcePanel.this.get("container:control:resource-view");
			}
			@Override
			public void bind(Component component) {
				Component editor = getEditor(component);
				if (editor!=null) {
					boolean found = false;
					for (Behavior behavior : editor.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							found = true;
							break;
						}
					}
					if (!found) {
						editor.add(new RefreshBehavior(editor.getMarkupId()));
					}
					else {
						setBehaviorId(editor.getMarkupId());
					}
				}
			}
			@Override
			protected void onUpload(AjaxRequestTarget target, String component) {
				fireScanAll(new EAjaxRefreshEvent(target, component));
			}
			@Override
			protected String getBrowseButton() {
				return EResourcePanel.this.get("container:control:pickfiles").getMarkupId();
			}
		});
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				if (handle(event)) {
					refresh(event.getRequestTarget());
				}
			}
			public boolean handle(EAjaxRefreshEvent event) {
				return getControl().getMarkupId().equals(event.getComponentId());
			}
		});
		
		getContainer().add(new ConfirmationDialog("confirmation-dialog"));

	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		Component parent = getParent();
		while (parent!=null && !(parent instanceof EFormEditor)) {
			parent = parent.getParent();
		}
		if (parent!=null) {
			for (Behavior behavior : parent.getBehaviors()) {
				behavior.beforeRender(this);
			}
		}	
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (contentModel!=null)
			contentModel.detach();
		if (resourceModel!=null)
			resourceModel.detach();
	}
	
	protected void setValues(List<?> values) {
		this.resourceModel = null;
		for (Object object : values) {
			if (object instanceof Resource) {
				setResourceModel((Resource)object);
				break;
			}
		}
	}
	
	protected void setResourceModel(Resource resource) {
		this.resourceModel = resource!=null ? new ResourceModel(resource) : null;
	}
	
	protected void setResourceModel() {
		Resource resource = (Resource)getData().getData(getField());
		if (getData() instanceof EFormContentData) {
			setContent(((EFormContentData)getData()).getContent());
		}
		if (resource!=null) {
			this.resourceModel = new ResourceModel(resource);
 		}
	}
	
	protected Panel getMenu(IModel<Resource> model) {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.ICON);
					setPreference("viewmode", ViewMode.ICON.getLabel());
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
						return getViewMode()!= ViewMode.ICON;
				}
				@Override
				public String getLabel() {	
					return getLabelString("icon");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.THUMBNAIL);
					setPreference("viewmode", ViewMode.THUMBNAIL.getLabel());
					refresh(target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL;
				}
				@Override
				public String getLabel() {	
					return getLabelString("thumbnail");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.THUMBNAIL_LARGE);
					setPreference("viewmode", ViewMode.THUMBNAIL_LARGE.getLabel());
					refresh(target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL_LARGE;
				}
				@Override
				public String getLabel() {	
					return getLabelString("image");
				}
		});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					onUpdate(target);
				}
				@Override
				public String getLabel() {	
					return EResourcePanel.this.getLabel("menu.showversions").getObject();
				}
				@Override
				public boolean isVisible() {
					return true;
				}
				@Override
				public boolean isEnabled() {
					return getResource().getPreviousVersion()!=null;
				}
				public void onUpdate(AjaxRequestTarget target) {
					if (EResourcePanel.this.getContainer().get("versions").isVisible()) {
						EResourcePanel.this.getContainer().addOrReplace(new InvisiblePanel("versions"));
					}
					else {
						EResourcePanel.this.getContainer().addOrReplace(new EResourceVersionsPanel("versions", getResourceModel(), true) {
							public void onClose(AjaxRequestTarget target) {
								onUpdate(target);
							}
							
						});	
					}
					target.add(EResourcePanel.this.getContainer());
				}
			});
	
		menu.addItem(id ->
			new DonwloadMenuItemPanelV5<Void>(id) {
				@Override 
				public String getLabel() {
					return  EResourcePanel.this.getLabel("menu.download").getObject();
				}
				@Override
				protected File getFile() {
					if (getResource() instanceof KBFile) {
						File file;
						try {
							file = ((KBFile) getResource()).getFile();
							if (file==null) 
								logger.error("file is null " + ((KBFile) getResource()).getName());
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
					return (getResource() instanceof KBFile);
				}
				@Override
				public boolean isEnabled()  {
					return true;
				}
			});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return  true;
				}
			});

		menu.addItem(id ->
			new UploadMenuItemPanelV5<Void>(id) {
				@Override
				public String getLabel() {	
					return getLabelString("menu.upload-version");
				}
				@Override
				protected String getUploadUrl() {
					return EResourcePanel.this.getUploadUrl();
				}	
				@Override
				protected String getRefreshFunction() {
					Component editor = getEditor(EResourcePanel.this);
					return editor!=null ? "refreshfiles"+editor.getMarkupId()+"('"+EResourcePanel.this.getControl().getMarkupId()+"');" : "";
				}
				@Override
				protected Component getResourcesView() {
					return EResourcePanel.this.get("container:control:resource-view");
				}
				
				@Override
				public boolean isVisible() {
					return !isReadOnly() && isEditionEnabled() && getResourceModel()!=null;
				}
			});
		
//		menu.addItem(id ->
//			new AjaxMenuItemPanelV5<Void>(id) {
//				@Override
//				public void onClick(AjaxRequestTarget target) {
//					setResourceModel(null);
//					refresh(target);
//				}
//				@Override
//				public String getLabel() {	
//					return getLabelString("menu.delete");
//				}
//				@Override
//				public boolean isVisible() {
//					return !isReadOnly() && getResource()!=null && isEditionEnabled();
//				}
//		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					IModel<String> ms = EResourcePanel.this.getLabel("dialog.delete.message", getResource().getTitle());
					getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, Button button) {
							if (button.key().equals(Dialog.Delete.key())) {
								try { 
									clear();
									refresh(target);
								} 
								catch (Exception e) {
									logger.error(e);
								}
							}
						}
					});
				}	
				@Override
				public String getLabel() {
					return getLabelString("menu.delete");
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly() && getResource()!=null && isEditionEnabled();
				}
		});
		
		return menu;
	}
	
	protected String getUploadUrl() {
		String url;
		if (getResourceModel()!=null) {
			String resourceid = String.valueOf(getResourceModel().getObject().getId());
			url = "/versionupload?";
			if (getContentModel()!=null) {
				Content content = getContentModel().getObject();
				String classname = Proxy.getClassName(content).toLowerCase();
				url += "id="+content.getId();
				url += "&class="+classname+"&";
			}
			url += "resource="+resourceid;
			url += "&path="+EResourcePanel.this.getPath();
		}
		else {
			url = "/formupload?path="+EResourcePanel.this.getPath();
		}
		return url;
	}
	
	protected EFormEditor getEditor(Component component) {
		Component editor = component.getParent();
		while (editor!=null && !(editor instanceof EFormEditor)) {
			editor = editor.getParent();
		}
		return (EFormEditor)editor;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		getControl().refresh(target);
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("container:confirmation-dialog");
	}
	
	private ControlFragment getControl() {
		return (ControlFragment)this.get("container:control");
	}

	
}
package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.ResourceMoved;
import com.novamens.content.form.ResourceRemoved;
import com.novamens.content.form.ResourceUpdated;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.UploadMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;
import kbee.web.uploader.UploadBehavior;

@SuppressWarnings("serial")
public class EResourcesPanel extends EFieldPanel<KbeeEResources> implements ResourcesPanel, IFormModelUpdateListener {
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(EResourceSystemPanel.class.getName());

	private List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentModel;
	private boolean updated = false;
	
	final long QUOTA = getDomain()!=null?getDomain().getQuota():0;
	final double DQUOTA = Double.valueOf(QUOTA).doubleValue();
	private static final double GB = 1000000000.0;

	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourcesPanel.this);
			
			setOutputMarkupId(true);
			
			Label s=new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles") {
				public boolean isVisible() {
					return isEditionEnabled() && !isReadOnly() && EResourcesPanel.this.isEnabled() && !isQuotaLimit();
				}
			};
			pickfiles.setOutputMarkupId(true);
			add(pickfiles);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container");
			menu.setOutputMarkupId(true);
			menu.add(getMenu());
			add(menu);
			
			addOrReplaceView();
			
			add(new WebMarkupContainer("quotalimit") {
				@Override
				public boolean isVisible() {
					return isQuotaLimit();
				}
			});
		}
		public void refresh(AjaxRequestTarget target) {
			target.add(get("menu-container"));
			target.add(get("resources-view"));
		}
		protected Panel getMenu() {
			
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
						return EResourcesPanel.this.getLabel("icon").getObject();
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
						return EResourcesPanel.this.getLabel("thumbnail").getObject();
					}
			});
			
			return menu;
		}
		private void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.setOutputMarkupId(true);
			view.add(new ListView<IModel<Resource>>("resources-list", new PropertyModel<List<IModel<Resource>>>(EResourcesPanel.this, "resources")) {
				protected void populateItem(ListItem<IModel<Resource>> item) {
					item.add(new ResourceView(item.getModelObject()));
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
		private boolean isQuotaLimit() {
			
			if (getDomain()==null)
				return false;
			// We use local Hard Disk storage for Quota. (External storage does not count)
			long used  = getDomainMetricsServices().getHardDisk(getDomain());
			double dused  = Double.valueOf((double) used / (double) GB).doubleValue();
			if (QUOTA > 0 &&  (DQUOTA < dused)) 
				return true;
			return false;
		}
	}
	
	
	public class ResourceView extends Fragment {
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EResourcesPanel.this);
			setOutputMarkupId(true);
			add(new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
				public ViewMode getViewMode() {
					return EResourcesPanel.this.getViewMode();
				}
			});
			add(getMenu(model));
			add(new ResourceEditor("editor", model, getContentModel()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					getData().setData(getField(), getResources());
					updated = true;
					EResourcesPanel.this.setUpdatedField(new ResourceUpdated(getData().getForm(), EResourcesPanel.this.getLabel(), getModelObject()));
					target.add(ResourceView.this);
				}
				@Override
				public void onClose(AjaxRequestTarget target) {
					target.add(ResourceView.this);
				}
			});
			add(new EResourceVersionsPanel("versions", model));
		}
		protected void edit(AjaxRequestTarget target) {
			((ResourceEditor)get("editor")).edit(target);
			target.add(ResourceView.this);
		}
		protected void showVersions(AjaxRequestTarget target) {
			((EResourceVersionsPanel)get("versions")).open(target);
			target.add(ResourceView.this);
		}
		protected Panel getMenu(IModel<Resource> model) {
			
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(model);
			
			menu.addItem(id ->
				new DonwloadMenuItemPanelV5<Resource>(id) {
					@Override 
					public String getLabel() {
						return getLabelString("menu.download");
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
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						showVersions(target);
					}	
					@Override
					public boolean isVisible() {
						return getModelObject().getVersion()>1;
					}
					@Override
					public String getLabel() {
						return getLabelString("menu.showversions");
					}
			});
			
			menu.addItem(id ->
				new UploadMenuItemPanelV5<Resource>(id) {
					@Override
					public String getLabel() {	
						return getLabelString("menu.upload-version");
					}
					@Override
					protected String getUploadUrl() {
						Content content = getContentModel().getObject();
						String resourceid = String.valueOf(getModelObject().getId());
						String classname = Proxy.getClassName(content).toLowerCase();
						return "/versionupload?id="+ content.getId() +
							"&class="+classname+
							"&resource="+resourceid+
							"&path="+EResourcesPanel.this.getPath();
					}	
					@Override
					protected String getRefreshFunction() {
						Component editor = getEditor(EResourcesPanel.this);
						return editor!=null ? "refreshfiles"+editor.getMarkupId()+"('"+EResourcesPanel.this.getMarkupId()+"');" : "";
					}
					@Override
					protected Component getResourcesView() {
						return EResourcesPanel.this.get("container:control:resources-view");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						edit(target);
					}	
					@Override
					public String getLabel() {
						return getLabelString("menu.edit");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
			});
			
			
			if (internalWriteable()) {
				for (IModel<ResourceTag> tagmodel : getResourceTags()) {
					boolean tags = false;
					if (!tagmodel.getObject().equals(getFieldTag())) {
						if (!tags)
						menu.addItem(id ->
							new SeparatorMenuItemPanelV5<Resource>(id) {
								@Override
								public String getCssClass() {
									return "divider";
								}
								@Override
								public boolean isVisible() {
									if (isReadOnly() || !isEditionEnabled())
										return false;
									return true;
								}
							}
						);
						menu.addItem(id ->
							new AjaxMenuItemPanelV5<Resource>(id) {
								@Override
								public void onClick(AjaxRequestTarget target) {
									setTag(target, getModelObject(), tagmodel.getObject());
									refresh(target);
								}	
								@Override
								public String getLabel() {
									return getLabelString("menu.move", tagmodel.getObject().getDisplayName());
								}
								@Override
								public boolean isVisible() {
									if (isReadOnly() || !isEditionEnabled())
										return false;
									return true;
								}
						});
						tags=true;
					}
				}
			}

			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<String> ms = EResourcesPanel.this.getLabel("dialog.delete.message", getModelObject().getTitle());
						getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									try { 
										delete(getModelObject());
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
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
			});

			
			
			return menu;
		}

	}	
	
	
	/***
	 * 
	 * 
	 * @param id
	 * @param field
	 * @param data
	 */
	public EResourcesPanel(String id, KbeeEResources field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
	}
	
	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode = mode;
	}
	
	public Disposition getDisposition() {
		return Disposition.VERTICAL;
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
	
	public Content getContent() {
		return getContentModel().getObject();
	}
	
	public List<IModel<Resource>> getResources() {
		return resources;
	}
	
	@Override
	public void add(Resource resource) {
		this.resources.add(new ObjectModel<Resource>((Resource)resource));
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	@Override
	public void addVersion(Resource resource, Resource version) {
		int index = 0;
		for (IModel<Resource> model : this.resources) {
			if (model.getObject().equals(resource)) {
				IModel<Resource> versionmodel = new ResourceModel(version);
				this.resources.set(index, versionmodel);
				versionmodel.detach();
				model.detach();
				break;
			}
			index++;
		}
		updated = true;
	}
	
	public void delete(Resource resource) {
		for (IModel<Resource> model : this.resources) {
			if (model.getObject().equals(resource)) {
				this.resources.remove(model);
				break;
			}
		}
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceRemoved(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	public void setTag(AjaxRequestTarget target, Resource resource, ResourceTag tag) {
		((ResourceContainer)getContent()).setTag(resource, tag);
		setUpdatedField(new ResourceMoved(getData().getForm(), getLabel(), resource, tag.getDisplayName()));
 		for (IModel<Resource> model : getResources()) {
			if (model.getObject().equals(resource)) {
				getResources().remove(model);
				break;
			}
		}
		fireScanAll(new EAjaxFormResourceEvent(target, getField(), getData(), resource, tag));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}
	
	public void updateModel() {
		if (updated) {
			getResources().forEach(model -> model.getObject());
			getData().setData(getField(), getResources());
			updated = false;
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		String p = getPreference("viewmode",  ViewMode.ICON.getLabel());
	    viewmode = p.equals(ViewMode.ICON.getLabel()) ? ViewMode.ICON : ViewMode.THUMBNAIL;
		
		setResources();
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		layout.add(new ControlFragment("control"));
		getContainer().add(new ControlFragment("control"));
		getContainer().add(layout);
		
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		getContainer().get("label").add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
			}
		}));
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			layout.add(new AttributeModifier("class", Width.W10.getCss()));
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
		add(new UploadBehavior() {
			@Override
			public boolean isEnabled() {
				return EResourcesPanel.this.isEnabled();
			}
			@Override
			protected String getUrl() {
				return "/formupload?path="+EResourcesPanel.this.getPath();
			}
			@Override
			protected String getDropElement() {
				return getContainer().getMarkupId();
			}
			@Override
			public Component getResourcesPanel() {
				return EResourcesPanel.this.get("container:control:resources-view");
			}
			@Override
			public void bind(Component component) {
				Component parent = component.getParent();
				while (parent!=null && !(parent instanceof EFormEditor)) {
					parent = parent.getParent();
				}
				if (parent!=null) {
					boolean found = false;
					for (Behavior behavior : parent.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							setBehaviorId(((RefreshBehavior)behavior).getId());
							found = true;
							break;
						}
					}
					if (!found)
					parent.add(new RefreshBehavior(parent.getMarkupId()));
				}
			}
			@Override
			protected void onUpload(AjaxRequestTarget target, String component) {
				fireScanAll(new EAjaxRefreshEvent(target, component));
			}
			@Override
			protected String getBrowseButton() {
				return EResourcesPanel.this.get("container:control:pickfiles").getMarkupId();
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
				return EResourcesPanel.this.getMarkupId().equals(event.getComponentId());
			}
		});
		
		add(new WicketEventListener<EAjaxFormResourceEvent>() {
			@Override
			public void onEvent(EAjaxFormResourceEvent event) {
				if (event.getTag()!=null && event.getTag().equals(getFieldTag())) {
					addIfNotExist(event.getResource());
				}
				refresh(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<EAjaxFormResourceAddedEvent>() {
			@Override
			public void onEvent(EAjaxFormResourceAddedEvent event) {
				if (event.getTag().equals(getFieldTag())) {
					if (!contains(event.getResource())) {
						((EAjaxFormResourceAddedEvent)event).getEditor().setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), event.getResource()));
					}
					addIfNotExist(event.getResource());
				}
				refresh(event.getRequestTarget());
			}
		});
		
		getContainer().add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<Resource> model : getResources()) {
			model.detach();
		}
		if (contentModel!=null)
			contentModel.detach();
	}
	
	protected void refresh(AjaxRequestTarget target) {
		getControl().refresh(target);
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("container:confirmation-dialog");
	}
	
	private void addIfNotExist(Resource resource) {
		boolean found = contains(resource);
		if (!found && resource instanceof Resource && !(resource instanceof ResourceFolder)) {
			updated = true;
			findPage();
			getResources().add(new ResourceModel((Resource)resource));
			getData().setData(getField(), getResources());
			fireScanAll(new EAjaxFormEvent(null, getField()));
		}
	}
	
	private boolean contains(Resource resource) {
		boolean found = false;
		for (IModel<Resource> model : getResources()) {
			if (model.getObject().equals(resource)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	private ControlFragment getControl() {
		return (ControlFragment)this.get("container:control");
	}
	
	private List<IModel<ResourceTag>> getResourceTags() {
		List<IModel<ResourceTag>> tags = new ArrayList<IModel<ResourceTag>>();
		for (ResourceTag tag : getContent().getContentTemplate().getResourceTags()) {
			tags.add(new ObjectModel<ResourceTag>(tag));
		}
		return tags;
	}
	
	private void setResources() {
		List<?> resources = (List<?>)getData().getData(getField());
		setContent(((EFormContentData)getData()).getContent());
		this.resources.clear();
		if (resources!=null) {
			for (Object resource : resources) {
				if (resource instanceof Resource && !(resource instanceof ResourceFolder)) {
					this.resources.add(new ResourceModel((Resource)resource));
				}
			}
		}
	}
	
	private EFormEditor getEditor(Component component) {
		Component editor = component.getParent();
		while (editor!=null && !(editor instanceof EFormEditor)) {
			editor = editor.getParent();
		}
		return (EFormEditor)editor;
	}
	
	private ResourceTag getFieldTag() {
		ResourceTag fieldtag = ((EResourceModel<?>)getField().getModel()).getTag();
		return fieldtag;
	}
	
	private boolean internalWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
	}
	
	private DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}
}
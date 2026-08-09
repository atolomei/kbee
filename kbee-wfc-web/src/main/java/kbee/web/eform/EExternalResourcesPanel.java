package kbee.web.eform;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.content.form.KbeeEExternalResources;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;

@SuppressWarnings("serial")
public class EExternalResourcesPanel extends EFieldPanel<KbeeEExternalResources> implements ResourcesPanel, IFormModelUpdateListener {
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(EResourceSystemPanel.class.getName());

	private List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentModel;
	private boolean updated = false;
	
	//final long QUOTA = getDomain()!=null?getDomain().getQuota():0;
	//final double DQUOTA = Double.valueOf(QUOTA).doubleValue();
	//private static final double GB = 1000000000.0;

	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EExternalResourcesPanel.this);
			
			setOutputMarkupId(true);
			
			Label s=new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
//			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles") {
//				public boolean isVisible() {
//					return isEditionEnabled() && !isReadOnly() && EExternalResourcesPanel.this.isEnabled();
//				}
//			};
//			pickfiles.setOutputMarkupId(true);
//			add(pickfiles);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container");
			menu.setOutputMarkupId(true);
			menu.add(getMenu());
			add(menu);
			
			addOrReplaceView();
			
//			add(new WebMarkupContainer("quotalimit") {
//				@Override
//				public boolean isVisible() {
//					return isQuotaLimit();
//				}
//			});
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
				new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						addNewResource();
						refresh(target);
					}	
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					public String getLabel() {	
						return getLabelString("menu.new");
					}
			});

			
			return menu;
		}
		private void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.setOutputMarkupId(true);
			view.add(new ListView<IModel<Resource>>("resources-list", new PropertyModel<List<IModel<Resource>>>(EExternalResourcesPanel.this, "resources")) {
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
	}
	
	
	public class ResourceView extends Fragment {
		IModel<Resource> model;
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EExternalResourcesPanel.this);
			this.model = model;
			setOutputMarkupId(true);
			addView();
			add(getMenu(model));
			add(new ResourceEditor("editor", model, getContentModel()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					getData().setData(getField(), getResources());
					updated = true;
					EExternalResourcesPanel.this.setUpdatedField(new ResourceUpdated(getData().getForm(), EExternalResourcesPanel.this.getLabel(), getModelObject()));
					refresh(target);
				}
				@Override
				public void onClose(AjaxRequestTarget target) {
					target.add(ResourceView.this);
				}
			});
		}
		protected void edit(AjaxRequestTarget target) {
			((ResourceEditor)get("editor")).edit(target);
			target.add(ResourceView.this);
		}
		protected void refresh(AjaxRequestTarget target) {
			model.detach();
			addView();
			target.add(ResourceView.this);
		}
		protected void addView() {
			addOrReplace(new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
				public ViewMode getViewMode() {
					return EExternalResourcesPanel.this.getViewMode();
				}
				@Override
				protected boolean shouldAddAntiCacheParameter() {
					return true;
				}
			});
		}
		protected Panel getMenu(IModel<Resource> model) {
			
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(model);
			
//			menu.addItem(id ->
//				new DonwloadMenuItemPanelV5<Resource>(id) {
//					@Override 
//					public String getLabel() {
//						return getLabelString("menu.download");
//					}
//					@Override
//					protected File getFile() throws IOException {
//						if (getModel().getObject() instanceof KBFile) {
//							return ((KBFile) getModel().getObject()).getFile();
//						}
//						return null;
//					}
//					@Override
//					public boolean isVisible() {
//						return (getModel().getObject() instanceof KBFile);
//					}
//				}
//			);
			
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
						IModel<String> ms = EExternalResourcesPanel.this.getLabel("dialog.delete.message", getModelObject().getTitle());
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
	public EExternalResourcesPanel(String id, KbeeEExternalResources field, IModel<EFormData> data) {
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
	
	public void addNewResource() {
		KbeeExternalResource resource = new KbeeExternalResource();
		resource.setDomain(getDomain());
		resource.setName("New Url");
		resource.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		resource.setLastModifiedUser(getSessionUser());
		getContent().getService(ContentService.class).addExternalResource(resource);
		this.resources.add(new ResourceModel(resource));
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	@Override
	public void addVersion(Resource resource, Resource version) {
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

		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				if (handle(event)) {
					refresh(event.getRequestTarget());
				}
			}
			public boolean handle(EAjaxRefreshEvent event) {
				return EExternalResourcesPanel.this.getMarkupId().equals(event.getComponentId());
			}
		});
		
		add(new WicketEventListener<EAjaxFormResourceEvent>() {
			@Override
			public void onEvent(EAjaxFormResourceEvent event) {
				if (event.getTag().equals(getFieldTag())) {
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
				if (resource instanceof ExternalResource) {
					this.resources.add(new ResourceModel((Resource)resource));
				}
			}
		}
	}
	
	private ResourceTag getFieldTag() {
		ResourceTag fieldtag = ((EResourceModel<?>)getField().getModel()).getTag();
		return fieldtag;
	}
	
	private boolean internalWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
	}
}
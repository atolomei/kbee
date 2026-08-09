package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.resource.KBFile;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;

@SuppressWarnings("serial")
public class EResourcesViewer extends EFieldPanel<KbeeEResources>  {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentModel;
	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourcesViewer.this);
			
			setOutputMarkupId(true);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container");
			menu.setOutputMarkupId(true);
			menu.add(getMenu());
			add(menu);
			
			addOrReplaceView();
		}
	
		public void refresh(AjaxRequestTarget target) {
			target.add(get("resources-view"));
			target.add(get("menu-container"));
		}
		
		protected Panel getMenu() {
			
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setViewMode(ViewMode.ICON);
						refresh(target);
					}	
					@Override
					public boolean isEnabled() {
						return getViewMode()!= ViewMode.ICON;
					}
					@Override
					public String getLabel() {	
						return EResourcesViewer.this.getLabel("icon").getObject();
					}
			});
		
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setViewMode(ViewMode.THUMBNAIL);
						refresh(target);
					}	
					@Override
					public boolean isEnabled() {
						return getViewMode()!= ViewMode.THUMBNAIL;
					}
					@Override
					public String getLabel() {	
						return EResourcesViewer.this.getLabel("thumbnail").getObject();
					}
			});
		
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						setViewMode(ViewMode.THUMBNAIL_LARGE);
						refresh(target);
					}
					@Override
					public boolean isEnabled() {
						return getViewMode()!= ViewMode.THUMBNAIL_LARGE;
					}
					@Override
					public String getLabel() {	
						return EResourcesViewer.this.getLabel("image").getObject();
					}
			});
			
			return menu;
		}
		
		private void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.setOutputMarkupId(true);
			view.add(new ListView<IModel<Resource>>("resources-list", new PropertyModel<List<IModel<Resource>>>(EResourcesViewer.this, "resources")) {
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
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EResourcesViewer.this);
			setOutputMarkupId(true);
			add(new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
				@Override
				public ViewMode getViewMode() {
					return EResourcesViewer.this.getViewMode();
				}
				@Override
				public boolean isShared() {
					return EResourcesViewer.this.isShared();
				}
				@Override
				protected String format(OffsetDateTime time) {
					
					if (getSessionUser()==null)
						return ServiceLocator.getService(DateTimeService.class).format(time);
					
					Locale locale = getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault();
					return ServiceLocator.getService(DateTimeService.class).getDomainInOriginalGMTDateDisplayString(time, locale);
				}
			});
			add(new EResourceVersionsPanel("versions", model));
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
			
			return menu;
		}
	}	

	
	public EResourcesViewer(String id, KbeeEResources field, IModel<EFormData> data) {
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
	
	public List<IModel<Resource>> getResources() {
		return resources;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
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
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1" : "";
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
				return EResourcesViewer.this.getMarkupId().equals(event.getComponentId());
			}
		});
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (contentModel!=null)
			contentModel.detach();
	}
	
	protected void add(Resource resource) {
		this.resources.add(new ResourceModel(resource));
	}
	
	protected boolean isShared() {
		return false;
	}
	
	protected void setResources() {
		List<?> resources = (List<?>)getData().getData(getField());
		setContent(((EFormContentData)getData()).getContent());
		this.resources.clear();
		if (resources!=null) {
			for (Object resource : resources) {
				if (resource instanceof Resource) {
					add((Resource)resource);
				}
			}
		}
	}
}
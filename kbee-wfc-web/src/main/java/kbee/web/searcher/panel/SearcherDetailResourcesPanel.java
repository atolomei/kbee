 package kbee.web.searcher.panel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceViewPanel;


@SuppressWarnings("serial")
public class SearcherDetailResourcesPanel<T extends Content> extends SearcherDetailPanel<T> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SearcherDetailResourcesPanel.class.getName());
	
	private List<IModel<Resource>> resources;
	private List<IModel<ResourceTag>> groups;
	
	private boolean isInternal = false;
	private boolean isPortal = false;
	
	private ViewMode view_mode = ViewMode.THUMBNAIL;

	private WebMarkupContainer resources_list_legacy;
	
	
	public SearcherDetailResourcesPanel(String id, IModel<T> model,  IModel<Site> site_model) {
		this(id, model, site_model, true, false);
	}
	
	
	public SearcherDetailResourcesPanel(String id, IModel<T> model,  IModel<Site> site_model, boolean isPortal, boolean isInternal) {
		super(id, model, site_model);
		
		setOutputMarkupId(true);
		
		int view =  ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue(this.getClass().getSimpleName(),"viewmode", ViewMode.ICON.ordinal());

		this.isPortal=isPortal;
		this.isInternal=isInternal;

		if		(ViewMode.ICON.ordinal()==view)					setViewMode(ViewMode.ICON);
		else if (ViewMode.NOIMAGE.ordinal()==view)				setViewMode(ViewMode.NOIMAGE);
		else if (ViewMode.THUMBNAIL.ordinal()==view)			setViewMode(ViewMode.THUMBNAIL);
		else if (ViewMode.THUMBNAIL_LARGE.ordinal()==view)		setViewMode(ViewMode.THUMBNAIL_LARGE);
		else setViewMode(ViewMode.THUMBNAIL);
	
		/*
		setGroups(getModelObject().getContentTemplate().getResourceTags());
		
		this.isPortal=isPortal;
		this.isInternal=isInternal;
		
		if (getModelObject() instanceof ResourceContainer) {
			if (isPortal())
				setResources(((ResourceContainer)getModelObject()).getPortalEnabledResources());
			
			
			else if (isInternal()) {
				List<Resource> list = ((ResourceContainer)getModelObject()).getResources(false);
				setResources(list);
			}
			else
				setResources(((ResourceContainer)getModelObject()).getResources(true));
		}
		*/
	}
	
	
	
	public boolean isPortal() {
		return this.isPortal;
	}
	
	public boolean isInternal() {
		return this.isInternal;
	}
	
	public ViewMode getViewMode() {
		return this.view_mode;
	}
	
	public void setViewMode(ViewMode mode) {
		this.view_mode=mode;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setGroups(getModelObject().getContentTemplate().getResourceTags());
		
		if (getModelObject() instanceof ResourceContainer) {
			if (isPortal())
				setResources(((ResourceContainer)getModelObject()).getPortalEnabledResources());
			
			
			else if (isInternal()) {
				List<Resource> list = ((ResourceContainer)getModelObject()).getResources(false);
				setResources(list);
			}
			else
				setResources(((ResourceContainer)getModelObject()).getResources(true));
		}

		addComponents();
	}
	
	
	public void changeViewMode(AjaxRequestTarget target) {
		if (getViewMode() == ViewMode.ICON)
			setViewMode(ViewMode.THUMBNAIL);
		else if (getViewMode() == ViewMode.THUMBNAIL)
			setViewMode(ViewMode.THUMBNAIL_LARGE);
		else
			setViewMode(ViewMode.ICON);
		resources_list_legacy.add(new AttributeModifier("class", getViewMode().getLabel() + " media-list "));
		
		
		((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(this.getClass().getSimpleName(),"viewmode", getViewMode().ordinal());
		
		target.add(SearcherDetailResourcesPanel.this);
	}
	
	public void changeViewMode(ViewMode mode, AjaxRequestTarget target) {
		setViewMode(mode);
		resources_list_legacy.add(new AttributeModifier("class", getViewMode().getLabel() + " media-list "));
		((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(this.getClass().getSimpleName(),"viewmode", getViewMode().ordinal());
		
		target.add(this);
	}
	
	public String getCss() {
		return "";
	}
	
	
	
	public boolean isVisible() {
		return this.getResources().size()>=0;
	}
	
	private void setResources(List<Resource> resources) {
		this.resources = new ArrayList<IModel<Resource>>();
		for (Resource resource : resources) {
			this.resources.add(new ObjectModel<Resource>(resource));
		}
	}
	
	
	
	private Panel getMenu() {
		
		ContextMenuPanel<T> resmenu = new ContextMenuPanel<T>("menu", getModel());
		
		resmenu.addItem(id ->
			new AjaxMenuItemPanelV5<T>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					changeViewMode(ViewMode.ICON, target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.ICON;
				}
				@Override
				public String getLabel() {	
					return SearcherDetailResourcesPanel.this.getLabel("icon").getObject();
				}
			}
		);
		
		resmenu.addItem(id ->
			new AjaxMenuItemPanelV5<T>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					changeViewMode(ViewMode.THUMBNAIL, target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL;
				}
				@Override
				public String getLabel() {	
					return SearcherDetailResourcesPanel.this.getLabel("thumbnail").getObject();
				}
			}
		);
		
		resmenu.addItem(id ->
			new AjaxMenuItemPanelV5<T>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					changeViewMode(ViewMode.THUMBNAIL_LARGE, target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL_LARGE;
				}
				@Override
				public String getLabel() {	
					return SearcherDetailResourcesPanel.this.getLabel("image").getObject();
				}
			}
		);


		
		
		resmenu.addItem(id ->
		new SeparatorMenuItemPanelV5<T>(id) {
			@Override
			public String getCssClass() {
				return "divider";
			}
			@Override
			public boolean isVisible() {
				return  true;
			}
		}
	);


		
		
		
		resmenu.addItem(id ->
			new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<T>(id) {
				 
				@Override
				public String getLabel() {	
					return SearcherDetailResourcesPanel.this.getLabel("download-all").getObject();
				}
				@Override
				public boolean isVisible() {
					return true;
				}
				@Override
				protected File getFile() {
					return getModelObject().getService(ContentExportService.class).getPublicResourcesExport();		
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
		});
		
		return resmenu;
	}
	
	protected String getlabel(String key) {
		return new StringResourceModel(key, this, null).getObject();
	
	}


	//private KbeeUser getSessionUser() {
	//	return  (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	//}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getSessionUser() );
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	
	private void addComponents() {

		WebMarkupContainer container = new WebMarkupContainer("resources-main-container");
		add(container);
		
		WebMarkupContainer legacy_section = new WebMarkupContainer("legacysection");
		legacy_section.setVisible(getGroups().isEmpty() && !getResources().isEmpty());
		
		container.add(legacy_section);
		
		legacy_section.add(getMenu());
		legacy_section.add(new Label("resources.title", getModelObject().getContentTemplate().getResourcesLabel()) {
			public boolean isVisible() {
				return getGroups().isEmpty() && !getResources().isEmpty();
			}
		});
		
		container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return ((getViewMode() == ViewMode.THUMBNAIL_LARGE)  
					? (getCss() + " detail-area filelist gridmode")
					: (getCss() + " detail-area filelist listmode"));
			}
		}));
		
		
		container.add(new ListView<IModel<ResourceTag>>("resource-group", () -> getGroups()) {
			@Override
			protected void populateItem(ListItem<IModel<ResourceTag>> item) {
				item.add(getMenu());
				item.add(new AttributeModifier("class",  getViewMode().getLabel() + " media-list "));
				
				String na=item.getModelObject().getObject().getName();
				
				item.add(new Label("group-label", na));
				
				item.add(new ListView<IModel<Resource>>("resource-item", () -> getResources(item.getModelObject())) {
					@Override
					protected void populateItem(ListItem<IModel<Resource>> item) {
						ResourceViewPanel<T> r= new ResourceViewPanel<T>("resource-view", item.getModelObject(), SearcherDetailResourcesPanel.this.getModel());
						r.setViewMode(getViewMode());
						item.add(r);
					}
				});
				item.setVisible(!getResources(item.getModelObject()).isEmpty());
			}
			@Override
			public boolean isVisible() {
				return !getGroups().isEmpty() && !getResources().isEmpty();
			}
		});
		
		
		// -----legacy -------------------------------
		//
		resources_list_legacy = new WebMarkupContainer("legacy-resources-list") {
			@Override
			public boolean isVisible() {
				return getGroups().isEmpty() && !getResources().isEmpty();
			}
		};
		
		resources_list_legacy.add( new AttributeModifier("class", getViewMode().getLabel() + " media-list "));
		
		resources_list_legacy.add(new ListView<IModel<Resource>>("resource-item", () -> getResources()) {
			@Override
			protected void populateItem(ListItem<IModel<Resource>> item) {
				ResourceViewPanel<T> r= new ResourceViewPanel<T>("resource-view", item.getModelObject(), SearcherDetailResourcesPanel.this.getModel());
				r.setViewMode(getViewMode());
				item.add(r);
			}
		});
	
		legacy_section.add(resources_list_legacy);
	}
	
	private List<IModel<ResourceTag>> getGroups() {
		return this.groups;
	}
	
	private void setGroups(List<ResourceTag> groups) {
		this.groups = new ArrayList<IModel<ResourceTag>>();
		for (ResourceTag group : groups) {
			this.groups.add(new ObjectModel<ResourceTag>(group));
		}
	}
	
	private List<IModel<Resource>> getResources(IModel<ResourceTag> group) {
		List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
		for (IModel<Resource> model : this.resources) {
			ResourceContainer content = (ResourceContainer)getModelObject();
			Resource resource = model.getObject();
			ResourceTag resouregroup = content.getTag(resource);
			if (resouregroup == null) {
				resouregroup = getGroups().get(0).getObject();
			}
			if (resouregroup!=null && resouregroup.equals(group.getObject())) {
				resources.add(model);
			}
		}
		return resources;
	}

	private List<IModel<Resource>> getResources() {
		return this.resources;
	}


}

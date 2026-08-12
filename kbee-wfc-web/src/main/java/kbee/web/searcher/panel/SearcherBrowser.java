package kbee.web.searcher.panel;


import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jsoup.Jsoup;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

import kbee.util.logging.Logger;
import kbee.web.console.BaseBrowser;
import kbee.web.console.Browser;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.content.menu.AclMenuItem;
import kbee.web.content.menu.AuditTrailMenuItem;
import kbee.web.content.menu.DeleteMenuItem;
import kbee.web.content.menu.ProcessLauncherMenu;
import kbee.web.dashboard.LabelPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;

/**
 * Resultados de busqueda Searcher
 */
@SuppressWarnings("serial")
public class SearcherBrowser extends SearcherPanel {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SearcherBrowser.class.getName());
	
	static protected final int MAX_SINPPET_LENGHT = 280;
	
	static final public String TO_ESC="<br\\s*/>\\s*<br\\s*/>";
	
	static final public String EDITABLE_ICON =	" <i class=\"ml-2 small fa-duotone fa-solid fa-pen-to-square\"></i>";
	
	// <i class="fa-sharp-duotone fa-solid fa-pen-circle"></i>
	
	static final public String EDITABLE_ICON_CSS =	"fa-regular fa-pen-circle editable";
	static final public String LOCK_ICON_CSS =	"fa-regular fa-lock";
	static final public String FOLDER_CSS =	"far fa-folder";
	
	//static final public String EDITABLE_ICON =	" <i class=\"ml-2 small fa-solid fa-square\"></i>";

	private Searcher searcher;
	private IModel<Site> siteModel;
	private String consoleName;
	private IModel<User> model_wuser;
	private String browserType;
	
	

	
	
	
	
	
	
	public SearcherBrowser(String id, IModel<Site> siteModel) {
		super(id, "results");
		setOutputMarkupId(true);
		setSiteModel(siteModel);
		if (siteModel!=null)
			consoleName=siteModel.getObject().getOId().toString();
		setOutputMarkupId(true);
		addListeners();
	}

	public SearcherBrowser(String id, Query query, IModel<Site> siteModel) {
		super(id, "results");
		setOutputMarkupId(true);
		if (siteModel!=null)
			consoleName=siteModel.getObject().getOId().toString();
		setQuery(query);
		setSiteModel(siteModel);
		addListeners();
	}
	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}
	
	public Site getSite() {
		return siteModel.getObject();
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}

	public void setQuery(Query query) {
		searcher = new Searcher(query);
	}

	public Query getQuery() {
		return getSearcher().getQuery();
	}

	public Searcher getSearcher() {
		return searcher;
	}
	
	public String getConsoleKey() {
		return this.consoleName;
	}
	
	public String getBrowserType() {
		return browserType;
	}

	public void setBrowserType(String browserType) {
		this.browserType = browserType;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getSearcher() != null)
			getSearcher().detach();
		if (this.siteModel!=null)
			this.siteModel.detach();
		if (model_wuser!=null)
			model_wuser.detach();
	}
	
	protected Panel getContentMenu(IModel<Content> model) {
		
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
		
		menu.setWicket(true);

		menu.addItem(id ->
			new MenuItemPanelV5<Content>(id) {
				public void onClick() {
					try {
				 		fireScanAll(new ClickEvent<Content>(null, model, getIndex()));
					} 
					catch (Exception e) {
						setResponsePage(new ApplicationErrorPage<>(e));
					}
				}
				@Override 
				public String getLabel() {
					return getLabelString("open");
				}
		});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Content>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return isWriteable(getModel());
				}
			}
		);
		
		List<MenuItemFactory<Content>> list =  (new ProcessLauncherMenu<Content>(model)).getItems();
		
		if (list.size()>0) {
			menu.addItem(id ->
			new HeaderMenuItemPanelV5<Content>(id) {
				@Override
				public String getLabel() {
					return getLabelString("edition");
				}
			});
				
					
			for (MenuItemFactory<Content> item :list) {
				menu.addItem(item);
			}
		
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);
		}
	
		menu.addItem(id -> 
			new AclMenuItem<Content>(id));
		
		menu.addItem(id -> 
			new AuditTrailMenuItem<Content>(id, this)); 
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Content>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return !getModelObject().isLocked() && 
						isDeleteable(getModel());
				}
			}
		);

		menu.addItem(id -> 
			new DeleteMenuItem<Content>(id, this) {
				protected void refresh(AjaxRequestTarget target) {
					target.add((BaseBrowser<?>)getBrowser());
				}
		});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Content>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
			}
		);
		
		menu.addItem(id ->
			new SubMenuAjaxUserListItemPanel<Content>(id, 
				model,   
				getSiteModel().getObject().getOId().toString(), 
				getSiteModel(), 
				UserListItem.PUBLISHED)
		);
	
		return menu;
	}
	
	protected boolean isVisible(Facet facet) {
		Facet realfacet;
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible("portals");
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		if (realfacet instanceof ClassifierHierarchicalFacet) {
			return ((ClassifierHierarchicalFacet) realfacet).getClassifier().isVisible("portals");
		}
		if (realfacet instanceof ClassifierFacet) {
			return ((ClassifierFacet) realfacet).getClassifier().isVisible("portals");
		}
		return true;
	}
	
	protected IModel<String> getItemLabel(IModel<Content> modelObject) {
		return  new Model<String>(modelObject.getObject().getDisplayName()  );
	}
	
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		
		@SuppressWarnings("unchecked")
		ListPanel<Content> panel = (ListPanel<Content>) getBrowser().getPanel(ListPanel.class);
		
		if (panel==null) 
			return null;
		
		ListDisplayMode mode=panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
		StringBuilder str = new StringBuilder();
		try {
			Content content = modelObject.getObject(); 
			if (content.getWorkspace()!=null) {
				Activity ac = content.getService(WorkflowService.class).getActivity();
				if (ac!=null) {
					String task= content.getService(WorkflowService.class)
						.getActivity()
						.getTask().getDisplayName();
					str.append(task + " - ");
				}
				else {
					if (isPending(modelObject)) {
						str.append( getLabelString("pending") + " - ");						
					}
				}
			}
			
			String ty=content.getService(ContentService.class).getPortalSubtitle();
			
			if (ty!=null &&  ty.length()>0)
				str.append(ty);
			else {
				String ta=content.getContentTypeClassificationAsString();
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
					str.append(", ");
				}
				String st=content.getWorkflowStatusClassificationAsString();
				str.append(st);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
	}
	
	protected WebMarkupContainer getItemTags(IModel<Content> modelObject) {
		try {
			 Content c=(Content) modelObject.getObject();
			 String nr = (String) c.getService(PropertyService.class).getProperty(PropertyService.PROPERTY_HAS_TAGS);
			 if (nr==null || nr.equals("0"))
				 return new InvisiblePanel("labels");
			return new LabelSetPanel<Content>("labels", new ObjectModel<Content>(c), false, true, false);
		} 
		catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("labels", e);
		}
	}
	
	protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
		try {

			logger.debug("Getting more info panel for content "+modelObject.getObject().getId());
			
			@SuppressWarnings("unchecked")
			ListPanel<Content> panel = (ListPanel<Content>) getBrowser().getPanel(ListPanel.class);
			
			if (panel==null) 
				return new InvisiblePanel("more-info-container");
			
			ListDisplayMode mode=panel.getListDisplayMode();
			
			if (mode.isCompact())
				return new InvisiblePanel("more-info-container");
		 	
			
			if (modelObject.getObject().getWorkspace()==null || modelObject.getObject().getWorkspace()<1) {
		 		return new InvisiblePanel("more-info-container");
			}
				
			String note = modelObject.getObject().getService(WorkflowService.class).getTaskComment();
				
			if (note==null)
				return new InvisiblePanel("more-info-container");
			
			note=note.replaceAll(TO_ESC,"<br />");
			
			return new LabelPanel("more-info-container", getSnippet(note));
		}  
		catch (Exception e) {
			logger.error(e);
			return new LabelPanel("more-info-container",  new Model<String>(e.getClass().getSimpleName()));
		}
	}
	
	protected boolean isWriteable(IModel<Content> model) {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isWriteable(model.getObject());
	}
	
	protected boolean isDeleteable(IModel<Content> model) {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isDeleteable(model.getObject());
	}
	
	protected boolean ishasACheckoutVersion(IModel<Classificable> model) {
		if (model.getObject() instanceof Content) {
			return ((Content) model.getObject()).isLocked();
		}
		return false;
	}

	
	protected boolean isPending(IModel<Content> model) {
		if (model.getObject().getWorkspace()>0) {
			if (model.getObject().getWorkspace().toString().equals(getPendingModelUser().getObject().getId().toString())) {
				return true;
			}
		}
		return false;
	}
	
	protected IModel<User> getPendingModelUser() {
		if (model_wuser == null) {
			User user = getDomain().getService(DomainService.class).getWorkflowUser();
			model_wuser = new ObjectModel<User>(user);
		}
		return model_wuser;
	}
	
	 
	protected IModel<String> getSnippet(String text) {
		
		if (text==null || text.isEmpty())
			return new Model<String>();
		
		String s = null;
		if (text.length()>MAX_SINPPET_LENGHT)
			s = text.substring(0, MAX_SINPPET_LENGHT)+"...";
		else
			s=text;
		org.jsoup.safety.Safelist list = org.jsoup.safety.Safelist.basic();
		list.removeTags("p");
		
		String cleaned = Jsoup.clean(s, list);
		String t1 = cleaned;
		return  new Model<String>(t1);
	}
	
	
	protected Browser<?> getBrowser() {
		return (Browser<?>)get("browser");
	}
}

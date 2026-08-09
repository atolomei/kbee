package kbee.web.searcher.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.sitemanager.SiteAdminOpenEvent;
import kbee.web.resource.BrowserPage;
import kbee.web.searcher.SearcherForm;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;

@SuppressWarnings("serial")
public class SearcherMinimalTopToolbar<T> extends KBPanel {
	private static final long serialVersionUID = 1L;

//	static final boolean ONLY_USER_ACCOUNT = true;

//	private static AtomicBoolean IS_HELP_VISIBLE= null;
//	
//	public static boolean isHelpEnabled() {
//		if (IS_HELP_VISIBLE!=null)
//			return 	IS_HELP_VISIBLE.get();
//			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
//		return 	IS_HELP_VISIBLE.get();
//	}
//	
//	static AtomicBoolean IS_SITE_FAVS_VISIBLE;
	/*
	 * public static boolean isSiteFavsEnabled() { if (IS_SITE_FAVS_VISIBLE!=null)
	 * return IS_SITE_FAVS_VISIBLE.get(); IS_SITE_FAVS_VISIBLE = new
	 * AtomicBoolean(((ContentDao)
	 * (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).
	 * findSystemParameterValueByKey("sitefavs.enabled",
	 * "no").toLowerCase().trim().equals("yes")); return IS_SITE_FAVS_VISIBLE.get();
	 * 
	 * }
	 */

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherMinimalTopToolbar.class.getName());
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
						
	final boolean is_workflow = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId());;
	
	//private Boolean isHome = null;
	//private Boolean isInstitutional = null;
	//private Boolean isExplorer = Boolean.valueOf(true);
	
	private String specific_css;
	private String name;
	private IModel<Site> siteModel;

	

	/**
	 * 
	 */
	public SearcherMinimalTopToolbar(IModel<Site> site_model) {
		this("global-top-toolbar", site_model);
	}

	/**
	 * 
	 */
	public SearcherMinimalTopToolbar(String id, IModel<Site> site_model) {
		super(id);
		setSiteModel(site_model);
		setOutputMarkupId(true);
		addListeners();
	}
	
	/**
	 * 
	 */
	public SearcherMinimalTopToolbar(String id, IModel<Site> site_model,  Searcher searcher, long index, String console_name) {
		super(id);
		setSiteModel(site_model);
		setOutputMarkupId(true);
		//this.searcher=searcher;
		addListeners();
	}
	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}
	
	public void setName(String name)  {
		this.name=name;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		//if (getSiteModel()==null || getSiteModel().getObject()==null)
		//	throw new IllegalArgumentException("site is null");
		
		addComponents();
	}

	public void setSpecificCss(String  css) {
		this.specific_css=css;
	}
	
	/**
	 * @param addsearcher
	 * @param searcher
	 */
	protected void addComponents() {		

		WebMarkupContainer navbar = new WebMarkupContainer("top-navbar");
		
		String homecss= " home ";
		
		navbar.add( new AttributeModifier("class", " navbar navbar-default navbar-fixed-top kbsearcher " + homecss + 
				(this.specific_css!=null? (this.specific_css+" "):"") +
				ServiceLocator.getService(BrandingService.class).getSearchLibraryApplicationCss()));
		add(navbar);
		

		if (isSearchForm()) {
			SearcherForm f=new SearcherForm("search",  getSiteModel(), getSiteModel().getObject().getTitle());
			f.setAdvancedSearchLinkVisible(false);
			navbar.add(f);
		}
		else  
			navbar.add(new InvisiblePanel("search"));
	}

	public String getName() {
		return this.name;
	}
	
//	/**
//	 * @param id
//	 * @return
//	 */
//	protected Component newSettingsPanel(String id)  {
//		
//		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
//
//		menu.addItem(new MenuItemFactory<Void>() {
//			@Override
//			public AbstractMenuItemPanelV5<Void> getItem(String id) {
//				return new MenuItemPanelV5<Void>(id) {
//					public void onClick() {
//					}
//					@Override 
//					public String getLabel() {
//						return "Settings 1";
//					}
//					@Override 
//					public String getTarget() {
//						return "_blank";
//					}
//				};
//			}
//		});
//		
//		return menu;
//	}


	public boolean isSearchForm() {
		return true;
	}
	
	public class SearchFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public SearchFragment(String id) {
			super(id, "search-fragment", SearcherMinimalTopToolbar.this);
		}
	}

	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public void onNavigate(T object) {};
	
	public Component getSiteManagerMenu() {
		AjaxLink<Void> link = new AjaxLink<Void>("site-manager") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("new SiteAdminEvent(target, getSiteModel()) -> " + getSiteModel().getObject().getTitle());
				fire(new SiteAdminOpenEvent<Site>(target, getSiteModel()));
			}
		};
		// link.setVisible(ServiceLocator.getService(BrandingService.class).isHelpEnabled());
		link.setVisible(false);
		return link;
	}
	
	public void addListeners() {
		add(new WicketEventListener<SearcherOnChangeEvent>() {
			public void onEvent(SearcherOnChangeEvent event) {
				try {
					setResponsePage(new BrowserPage(getSiteModel(), new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), event.getParameters())));
				} 
				catch (Exception e) {
					logger.error(e);
				}		
			}
		});
	}

	protected boolean hasWorkspace() {
		return true;
	}
	
	protected boolean isSubmitFile() {
		return true;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}	
	
 	protected Component newNavigator(Searcher searcher, long index)  {
 		DummyBlockPanel panel = new DummyBlockPanel ("navigator", new Model<String>("new ResourcesPanel<T>"));
 		return panel;
	}

// 	protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
// 		return null;
// 	}
 	
// 	public boolean isExportSavedQueries() {
// 		return false;
//	}
}

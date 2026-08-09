package kbee.web.portal6;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.site.logging.SiteStatInEvent;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.KbeeWebPage;
import kbee.web.portal6.sitemanager.SiteAdminCloseEvent;
import kbee.web.portal6.sitemanager.SiteAdminOpenEvent;
import kbee.web.searcher.page.AbstractSearcherPage;
import kbee.web.searcher.page.SearcherGlobalTopToolbar;
import kbee.web.searcher.page.SearcherResultsPage;

public class AbstractKbeePortal6DiagrammablePage extends KbeeWebPage<Page> {
				
	private static final long serialVersionUID = 1L;
	
	//private static final ResourceReference KBEE_SEARCHER_CSS = new CssResourceReference(SearcherResultsPage.class, "searcher.css");
	
	

	private static final ResourceReference ICONS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");

	private static final ResourceReference COMPONENTS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");
	private static final ResourceReference CORE_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	private static final ResourceReference APP_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/app.js");
	
	
	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");

	private static final ResourceReference KBEE_BOOTSTRAP_CSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractKbeePortal6DiagrammablePage.class.getName());

	static private Logger siteStatslogger = LogManager.getLogger("SiteStats");
	
	private Panel footer = null;

	private boolean footer_is_null = false;
	private boolean has_footer = false;
	private String specific_css;
	
	private IModel<Site> siteModel;
	
	// private IModel<Page> pageModel;

	private boolean site_manager_created = false;
	
	private long start, end; 	
	private boolean is_log_visit = false;

	public AbstractKbeePortal6DiagrammablePage() {
		addListeners();
		add(new InvisiblePanel("navigation"));
		setOutputMarkupId(true);
		setLogVisit(true);
	}
	
	
	public AbstractKbeePortal6DiagrammablePage(IModel<Page> model) {
		super(model);
		addListeners();
		add(new InvisiblePanel("navigation"));
		setOutputMarkupId(true);
		setLogVisit(true);
	}
	
	
	/**
	 * We use locally installed fonts from css stylesheet
	 */
	public String getFonts() { 
		return "";
	}
	
	
	protected boolean isHome() {
		return false;
	}
	

	public void setSpecificCss(String  css) {
		this.specific_css=css;
	}
	
	
	
	public String getName() {
		return "General";
	}
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
 		setMenu(new InvisiblePanel("menu"));

 		WebMarkupContainer c= new WebMarkupContainer("site-manager-external-ajax-container");
 		c.setVisible(false);
 		add(c);
 		c.add(new InvisiblePanel("site-manager"));

 		try {
 		
 			
 				//
 				// add all area panels
 				//
 			
		 		SearcherGlobalTopToolbar<Page> s=new SearcherGlobalTopToolbar<Page>( "navigation", getSiteModel());
		 		s.setSpecificCss(this.specific_css);
				s.setName(getName());
				//s.setSearchForm(isSearchForm());
				s.setHome(isHome());
				//s.setInstitutional(isInstitutional());
				setNavigation(s);
 		} catch (Exception e) {
 			logger.error(e);
			setNavigation(new InvisiblePanel("navigation"));
 		}
	}
	
	public void setNavigation(Panel navigation) {
		addOrReplace(navigation);
	}
	
	public Panel getNavigation() {
		return (Panel)get("navigation");
	}
	
	
	public  void setMenu(Panel nav) {
		addOrReplace(nav);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (get("menu")!=null)
			get("menu").detach();
		
		if (getSiteModel()!=null)
			getSiteModel().detach();
		
		//if (getPageModel()!=null)
		//	getPageModel().detach();
		
	}
	
	
	public Panel getMenu() {
		return (Panel) get("menu");
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (!has_footer) {
			addOrReplace(getFooter()!=null?getFooter():new InvisiblePanel("searcher-footer"));
			has_footer=true;
		}
	}
	
 	
	@Override
	public void onRender() {
		start = System.currentTimeMillis();
		super.onRender();
		end = System.currentTimeMillis();
		
		if (logger.isDebugEnabled()) {
			logger.debug( this.getPage().getClass().getSimpleName() + " - Render:" + String.valueOf(System.currentTimeMillis()-start)+" ms ");
		}
	}	
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  


		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));

		response.render(CssHeaderItem.forReference(CORE_CSS));
		response.render(JavaScriptHeaderItem.forReference(APP_JS));
		
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
		
		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		
		// response.render(CssHeaderItem.forReference(KBEE_SEARCHER_CSS));
		
		
		response.render(CssHeaderItem.forReference(BL));
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));

		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}

	
	/**
	 * @return
	 * 
	 * false: no footer will be rendered <br/>
	 * true: bean "console-footer", like {@link ConsoleFooterPanel}
	 * 
	 */
	protected boolean isFooterRequired() {
		return true;
	}
	
	/**
	 * SearcherFooterPanel
	 */ 
	protected Panel getFooter() {
		
		if (!isFooterRequired())
			return null;
		
		if (!footer_is_null && footer==null) {
			try {
				footer = (Panel) ServiceLocator.getService(BeansService.class).getBean("searcher-footer", "searcher-footer", getSiteModel());
				footer_is_null=false;
				logger.debug(footer!=null ? footer.getClass().getName()  :"");
				
			} catch (Exception e) {
				
				logger.debug(e.getClass().getName() + " no bean 'searcher-footer' was defined");
				footer=null;
				footer_is_null=true;
			}
		}
		has_footer=true;
		return footer;
	}
	
	/**
	 * Esto es para que no se mande kbee2.css que es obsoleto pero no puede sacarse por ahora.
	 */
	@Override
	protected ResourceReference getCssResource() {
		return KBEE_BOOTSTRAP_CSS;
	}
	
	protected void addModals() 	 {
		add(new InvisiblePanel("page-confirmation-dialog"));
		add(new InvisiblePanel("tip"));
		
	}


	protected boolean hasPermissions() {
		return true;
		//return getDomain().isPortalLibrary() && is_user;
	}

	
		
	
	protected void setTipPanel(Panel panel) {
		addOrReplace(panel);
	}

	protected boolean hasTips() {
		return false;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected UserProfile getUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(" isDomainKbee " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return false;
		}
	}

	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	

	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}


	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}


	//public IModel<Page> getPageModel() {
	//	return pageModel;
	//}


	//public void setPageModel(IModel<Page> pageModel) {
	//	this.pageModel = pageModel;
	//}

	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		if (this.is_log_visit)
			logVisit();
	}

	
	public void setLogVisit(boolean b) {
		this.is_log_visit = b;
	}


	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SEARCHER;
	}
	
	protected String getUserPreference(String key, String defaultValue) {
		String s=getUserPreference(key);
		return s!=null?s:defaultValue;
	}
	
	protected String getUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			return user.getService(PreferencesService.class).getValue(getName(), key);
		return null;
	}

	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue(getName(), key, value);
	}
	
	/** 
	 *  Reports 
	 **/										
	protected String getPageType()     {return "search";} 												// con | det  
	protected String getContentTitle() {return null;} 													// content title or user title, ...
										
	protected String getStatsPageTitle() {return "search";} 											// for console page, it is the name of the console 
	protected Long getStatsPageId() {return Long.valueOf(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return null;} 												   		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content


	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected Site getSite(PageParameters parameters) {
		Site site = null;		
		StringValue oid = parameters.get("siteurl");
		if (!oid.isNull() && !oid.isEmpty()) 
			site = getPortalDao().findSiteByURI(oid.toString(), getDomain());
		return site;
	}


	
	

	protected void addListeners() {
			
		add(new WicketEventListener<SiteAdminOpenEvent<? extends PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SiteAdminOpenEvent<? extends PortalObject> event) {
				Panel panel = getSiteManagerPanel();
				
				logger.debug("Site Manager: " + panel!=null ? panel.getClass().getName():"null");
				
				if (panel!=null) {
					panel.getParent().setVisible(true);
					panel.setVisible(true);
				}
				
				event.getRequestTarget().add(AbstractKbeePortal6DiagrammablePage.this);
			}
		});
		
									
		add(new WicketEventListener<SiteAdminCloseEvent<? extends PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SiteAdminCloseEvent<? extends PortalObject> event) {
				//logger.debug("close site manage -> " + event.getModel().getObject().getTitle());
				// external-ajax-container
				event.getRequestTarget().add(getSiteManagerPanel().getParent());
				getSiteManagerPanel().setVisible(false);
				
			}
		});

	}
	
	protected Panel getSiteManagerPanel() {

		if (!this.site_manager_created) {
			Panel sitemanager = (Panel) ServiceLocator.getService(BeansService.class).getBean("site-manager", getModel());
			WebMarkupContainer cont=new WebMarkupContainer("site-manager-external-ajax-container");
			cont.setOutputMarkupId(true);
			addOrReplace(cont);
			cont.addOrReplace(sitemanager);
			this.site_manager_created = true;
		}
		return (Panel) get("site-manager-external-ajax-container:site-manager");
	}

	
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}

	protected Serializable getCId() {return null;}
	protected Serializable getContentOId() 		{return null;}	  				// for content
	protected Integer getContentVersion() {return null;}
	
	
	
	boolean visit_logged =false;
	protected void setVisitLogged(boolean b) {
		this.visit_logged = b;
	}
	protected boolean isVisitLogged() {
		return this.visit_logged;
	}
	
	protected void logVisit() {
		
		if (isVisitLogged())
			return;
		
		// --------------------------------------------------------------------------
		// Agregar la info al log de Stat
		//
		try {
			
			 

			SiteStatInEvent stat = new SiteStatInEvent();

			stat.domain_id = Long.valueOf(getDomain().getId().toString());
			stat.sessionId = WebSession.get().getId();
			
			stat.page_type = getPageType();
			
			if (getSiteModel()!=null && getSiteModel().getObject()!=null) 
				stat.site_id = (Long) getSiteModel().getObject().getId();
			else
				stat.site_id = Long.valueOf(getApplicationMenuSection().getId()); 	// Section (security, tasks)
			
			stat.site_title = getApplicationMenuSection().getKey();       		// Section
			
			// for console page, it is the name of the console
			//
			stat.page_id =  getStatsPageId();
			stat.page_title = getStatsPageTitle();
			
			stat.user_id = Long.valueOf(getSessionUser().getId().toString());
			stat.user_name = getSessionUser().getFirstLastName();
			stat.timestamp = OffsetDateTime.now();

			stat.user_agent = ((WebRequest) getRequest()).getHeader("User-Agent");
			stat.sessionId = WebSession.get().getId();
			
			stat.render_milisecs = Long.valueOf(end - start);

			 stat.content_title = getContentTitle(); 	// content title or user/domain/dataset title
			 
			 stat.OId = null;  
			 stat.objectId  =  getObjectId();  			// for User, Domain, DataSet, etc.

			 
			 stat.contentId  =  getContentId(); 			// for Content
			 
			 stat.content_long_id = getCId()!=null ? (Long) getCId() : Long.valueOf(0);
			 
			 stat.objectId  =  getObjectId();  			// for User, Domain, DataSet, etc.
			stat.content_version = getContentVersion() !=null ? getContentVersion() : Integer.valueOf(0);

			stat.OId  =  getContentOId()!=null ? getContentOId().toString() : null;
			

			// Para que se logue en la Base de Datos
			// El logger de la Clase debe grabar en
			// el Appender "SiteStats"
			siteStatslogger.info(stat);

			
		} catch (Exception e) {
			logger.error(e);
		}
		finally {
			setVisitLogged(true);
		}
	}
	

}

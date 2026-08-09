package kbee.web.page;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.support.SupportService;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.OnSearchSuggestionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.site.logging.SiteStatInEvent;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.util.DummyBlockPanel;

//import jakarta.servlet.http.Cookie;
import kbee.web.behavior.AbstractApplicationBehavior;
import kbee.web.console.CloseInfoTopPanel;
import kbee.web.console.PageErrorPanel;
import kbee.web.console.PageInfoPanel;
import kbee.web.event.wicket.ShowTipOfTheDayEvent;
import kbee.web.search.SearchPanel;
import kbee.web.search.SuggesterSearchPanel;
import kbee.web.service.ApplicationSiteMapService;

/**
 * ConsolePage
 * AbstractApplicationPage
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractApplicationPage<T> extends KbeeWebPage<T> {
	private static final long serialVersionUID = 1L;

	
	private static final ResourceReference ICONS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");
	
	private static final ResourceReference COMPONENTS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");


	
	private static final ResourceReference CORE_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	private static final ResourceReference APP_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/app.js");

	private static final ResourceReference POPPER_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/popper.min.js");
	
	private static final ResourceReference KBEE_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/kbee.js");

	
	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference KBEE_BOOTSTRAP_CSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractApplicationPage.class.getName());

	static private Logger siteStatslogger = LogManager.getLogger("SiteStats");
	
	// 1 Day
	static private final int COOKIE_DURATION = 86400 * 1;
	
	static final String TIP_CATEGORY = Tip.GENERAL;

	private boolean isSearch = false;
	private String name;
	private long start, end;
	private boolean is_log_visit = false;
	private Panel footer = null;
	private boolean footer_is_null = false;
	private boolean has_footer = false;
	
	private boolean isAvancedSearch=false;
	private boolean isClearAllSearch=false;
	
	private boolean isSuggester = false;
	private String searchPlaceHolder;

	private  Component top_navigation;
	
	private  boolean visit_logged = false;
	
	private String initial_tab;
	
	private boolean is_infopanel=false;
	
	
	
	public AbstractApplicationPage() {
		this(null, null, null, null);
	}
	
	public AbstractApplicationPage(IModel<T> model) {
		this(model, null, null, null);
	}
	
	public AbstractApplicationPage(IModel<T> model, Component navigation) {
		this(model, navigation, null);
	}
	
	public AbstractApplicationPage(IModel<T> model, Component navigation, WebMarkupContainer menu) {
		this(model, navigation, null, null);
	}

	public AbstractApplicationPage(IModel<T> model, Component top_navigation, Component menu, Panel pageContentHeader) {
		super(model);
		addListeners();
		addModals();
		setName(getClass().getSimpleName());
		add((top_navigation!=null)?top_navigation:new InvisiblePanel("navigation"));
		add((menu!=null)?menu:new InvisiblePanel("menu"));
		add((pageContentHeader!=null)?pageContentHeader:new InvisiblePanel("page-content-header"));
	}
	
	
	
	@Override
	public void onInitialize() {
			super.onInitialize();
			
			if (this.hasInfoPanel()) 			
				setInfoTopPanel(new DummyBlockPanel("info-panel"));
			else								
			{
				WebMarkupContainer c =new WebMarkupContainer("info-panel-container");
				c.add(new InvisiblePanel("info-panel"));
				c.setOutputMarkupId(true);
				c.setVisible(true);
				add(c);
			}

			PageParameters  param = getPageParameters();
			if (param!=null) {
				StringValue a = param.get("tab");
				if (!a.isNull() && !a.isEmpty())
					 initial_tab = a.toString();
			}
	}
	
	protected void refreshInfoArea(AjaxRequestTarget requestTarget) {
		if (requestTarget!=null) {
			requestTarget.add(get("info-panel-container"));
		}
	}

	
	public void setInfoTopPanel(Panel panel) {
		
		if (panel==null)
			throw new IllegalArgumentException("info-panel can not be null");
		
		if (!panel.getId().contentEquals("info-panel"))
			throw new IllegalArgumentException("Info Panel must have id info-panel");
		
		is_infopanel = panel.isVisible();
		
		WebMarkupContainer c = (WebMarkupContainer) get("info-panel-container");
		c.addOrReplace(panel);
	}

	
	public boolean hasInfoPanel() {
		return is_infopanel;
	}
	
	
	public void setPageContentHeader(Panel pch) {
		if (!pch.getId().equals("page-content-header"))
			throw new IllegalArgumentException(" Panel id must be = 'page-content-header'");
		addOrReplace(pch);
	}
	
	
	
	public String getInitialTab() {
		return this.initial_tab;
	}
	
	public void setInitialTab(String tab) {
		this.initial_tab = tab;
	}
	
	/**
	 * Top Navigation
	 * 
	 * @param top_navigation
	 */
	public void setTopNavigation(Component top_navigation) {
		this.top_navigation = top_navigation;
		addOrReplace(this.top_navigation);
	}
	
	//public Component getTopNavigation() {
	//	return top_navigation;
	//}
	
	public Panel getPageContentHeader() {
		return (Panel) get("page-content-header");
	}
	
	public  void setMenu(Component nav) {
		addOrReplace(nav);
	}
	
	public void setErrorModal(Panel panel) {
		addOrReplace(panel);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			if (get("menu")!=null)
				get("menu").detach();
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public Panel getMenu() {
		return (Panel) get("menu");
	}
	
	public void setLogVisit(boolean b) {
		this.is_log_visit = b;
	}
	
	public boolean isAdvancedSearch() {
		return this.isAvancedSearch;
	}
	
	public boolean isClearAllSearch() {
		return this.isClearAllSearch;
	}
	
	public Dialog getErrorDialog() {
		if (get("page-error-dialog") instanceof Dialog)
			return  (Dialog) get("page-error-dialog");
		addOrReplace(new ErrorDialog("page-error-dialog"));
		return  (Dialog) get("page-error-dialog");
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
//		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  
//
		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
		
		response.render(JavaScriptHeaderItem.forReference(APP_JS));
		response.render(CssHeaderItem.forReference(CORE_CSS));

		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));

		response.render(JavaScriptHeaderItem.forReference(POPPER_JS));
		response.render(JavaScriptHeaderItem.forReference(KBEE_JS));
		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));

	}
	
	
	boolean beh_added = false;
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	
		start = System.currentTimeMillis();
		
		if (!beh_added) {
			Collection<AbstractApplicationBehavior> behaviors = ServiceLocator.getService(BeansService.class).getBeansOfType(AbstractApplicationBehavior.class).values();
			for (Behavior behavior : behaviors) 
				add(behavior);
			beh_added = true;
		}
		
		if (!has_footer) {
			addOrReplace(getFooter()!=null?getFooter():new InvisiblePanel("console-footer"));
			has_footer=true;
		}
	}
	
	@Override
	public void onRender() {
		super.onRender();
	}	
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		end = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug( this.getPage().getClass().getSimpleName() + " - Render:" + String.valueOf(end-start)+" ms ");
		}
		if (this.is_log_visit) {
			logVisit();
		}	
	}
	
	protected Modal getErrorModal() {
		return (Modal) get("error-modal");
	}
	
	protected void setAdvancedSearch(boolean b) {
		this.isAvancedSearch=b;
	}
	
	protected void setClearAllSearch(boolean b) {
		this.isClearAllSearch=b;
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
	 * ConsoleFooterPanel
	 */ 
	protected Panel getFooter() {
		
		if (!isFooterRequired())
			return null;
		
		if (!footer_is_null && footer==null) {
			try {
				footer = (Panel) ServiceLocator.getService(BeansService.class).getBean("console-footer", "console-footer");
				footer_is_null=false;
				
			} catch (Exception e) {
				
				logger.debug(e.getClass().getName() + " no bean 'console-footer' was defined");
				footer=null;
				footer_is_null=true;
			}
		}
		has_footer=true;
		return footer;
	}
	
	/**
	 * 
	 */
	@Override
	protected ResourceReference getCssResource() {
		return KBEE_BOOTSTRAP_CSS;
	}
	
	protected void addModals() 	 {
		add(new InvisiblePanel("page-confirmation-dialog"));
		add(new InvisiblePanel("tip"));
		add(new InvisiblePanel("page-error-dialog"));
	}
	
	protected boolean hasPermissions() {
		return false;
	}

	protected void addListeners() {
		
		add(new WicketEventListener<ShowTipOfTheDayEvent>() {
			@Override
			public void onEvent(ShowTipOfTheDayEvent event) {
				showRandomNextTip(event.getRequestTarget());
			}
		});
		
		
		add(new WicketEventListener<CloseInfoTopPanel>() {
			@Override
			public void onEvent( CloseInfoTopPanel event) {
				AbstractApplicationPage.this.setInfoTopPanel(new InvisiblePanel("info-panel"));
				AbstractApplicationPage.this.refreshInfoArea(event.getRequestTarget());
			}
		});

		
		add(new WicketEventListener<ErrorPageEvent>() {
			@Override
			public void onEvent(ErrorPageEvent event) {
				AbstractApplicationPage.this.setInfoTopPanel(new PageErrorPanel("info-panel", event.getThrowable()));
				AbstractApplicationPage.this.refreshInfoArea(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<InfoPageEvent>() {
			@Override
			public void onEvent(InfoPageEvent event) {
				IModel<String> title= event.getTitle();
				IModel<String> text= event.getText();
				String css = event.getCss();
				AbstractApplicationPage.this.setInfoTopPanel(new PageInfoPanel("info-panel", title, text, css));
				AbstractApplicationPage.this.refreshInfoArea(event.getRequestTarget());
			}
		});

		
		/**
		add(new WicketEventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				logger.error(event.getThrowable());
				IModel<String> titlemodel = new Model<String>("Error");
				IModel<String> messagemodel = new Model<String>("<h3>"+event.getThrowable().getClass().getSimpleName() +"</h3><br/><p> " + event.getThrowable().getMessage()+"</p>");
				((ErrorDialog) getErrorDialog()).open(event.getRequestTarget(), titlemodel, messagemodel);
			}
		});
		**/
	}	
	
	protected void setTipPanel(Panel panel) {
		addOrReplace(panel);
	}

	protected String getTipCategory() {
		return TIP_CATEGORY;
	}

	protected boolean hasTips() {
		return false;
	}
	
	protected void showNextTip(int index, AjaxRequestTarget target) {
		SupportService service = ServiceLocator.getService(SupportService.class);
		
		Tip next_tip = service.getNext(getSessionUser(), getTipCategory(),index);
		if (next_tip==null)
			return;
		addTipPanel(next_tip);
	}

	protected void showRandomNextTip(AjaxRequestTarget requestTarget) {
		SupportService service = ServiceLocator.getService(SupportService.class);
		Tip next_tip = service.getRandomNext(getSessionUser(), getTipCategory(), Tip.GENERAL);
		if (next_tip==null)
			return;
		addTipPanel(next_tip);
		requestTarget.add(get("tip").getParent());
		
	}

	protected void acceptTipOfTheDay(AjaxRequestTarget target) {
		String cookieKey = getSessionUser().getId().toString() + "-" + getTipCategory() + "-tip_of_the_day";
		WebResponse  res = (WebResponse) RequestCycle.get().getResponse();
		
		Cookie new_cookie = new Cookie(cookieKey, String.valueOf(System.currentTimeMillis()));
		
		
		new_cookie.setMaxAge(COOKIE_DURATION);
		res.addCookie(new_cookie);
	}
	
	protected void addTipPanel(Tip tip) {
		InvisiblePanel p=new InvisiblePanel("tip"); 
		setTipPanel(p);
	}
	
	protected void showTipOfTheDay() {
		SupportService service = ServiceLocator.getService(SupportService.class);
		Tip tip = service.getTipOfTheDay(getSessionUser(), getTipCategory());
		if (tip==null)
			return;
		addTipPanel(tip);
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
			Person person = getPerson();
			if (person==null)
				return false;
			return person.getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(" isDomainKbee " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return false;
		}
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

	protected Person getPerson() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	protected Integer getContentVersion() {
		return null;
	}
	
	protected void setSearchPlaceHolder(String placeHolder) {
		this.searchPlaceHolder=placeHolder;
	}
	
	protected String getSearchPlaceHolder() {
		return this.searchPlaceHolder;
	}
	
	protected void setSuggester(boolean suggester) {
		isSuggester = suggester;
	}
	
	protected boolean isSuggester() {
		return isSuggester;
	}

	protected void onSearch(AjaxRequestTarget target, String text) {
		fire(new OnSearchEvent(target, text));
	}
	
	protected void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
		fire(new OnSearchSuggestionEvent(target, suggestion));
	}
	
	protected void setSearchPanel(boolean spanel) {
		isSearch=spanel;
	}
	
	protected boolean isSearchPanel() {
		return isSearch;
	}
	
	/**
	 * - SearchPlaceHolder
	 * - isSearch
	 * - isSuggester 
	 */
	
	protected Panel getSearchPanel()  {
		return getSearchPanel("search"); 
		
	}
	protected Panel getSearchPanel(String id)  {
		
		if (!isSearchPanel())
			return new InvisiblePanel(id);
		
		if (isSuggester()) {
			
			return new SuggesterSearchPanel(id,  getName(), getSearchPlaceHolder(), isAdvancedSearch(), isClearAllSearch()) {
				@Override
				public void onSearch(AjaxRequestTarget target, String text) {
					AbstractApplicationPage.this.onSearch(target, text);
				}
				@Override
				public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
					AbstractApplicationPage.this.onSearch(target, suggestion);
				}
				@Override
				protected List<Suggestion> getSuggestions(String pattern) {
					return AbstractApplicationPage.this.getSuggestions(pattern); 
				}
				@Override
				protected boolean includeInfo() {
					return AbstractApplicationPage.this.includeInfo(); 
				}
				@Override
				protected String getInfo(Suggestion suggestion) {
					return AbstractApplicationPage.this.getInfo(suggestion); 
				}
			};
		}
		else {						
			return new SearchPanel(id,  getName(), getSearchPlaceHolder(), isAdvancedSearch(), isClearAllSearch()) {
				@Override
				public void onSearch(AjaxRequestTarget target, String text) {
					AbstractApplicationPage.this.onSearch(target, text);
				}
			};
		}
	}
	
	protected List<Suggestion> getSuggestions(String pattern) {
		return new ArrayList<Suggestion>();
	}
	
	protected boolean includeInfo() {
		return false; 
	}
	
	protected String getInfo(Suggestion suggestion) {
		return null; 
	}
	
	protected Component getMainTopbar() {
		try {
			return ServiceLocator.getService(ApplicationSiteMapService.class).getMainTopBar();
		} 
		catch (Exception e) {
			logger.error(e);
			return new DummyBlockPanel("navigation");
		}
	}

	protected Component getMainLaternalMenu() {
		try  {
			return  getMainLaternalMenu(getApplicationMenuSection().getKey());
		} 
		catch (Exception e) {
			logger.error(e);
			return new DummyBlockPanel("menu");
		}
	}
	
	protected void setName(String name) {this.name=name;}

	protected String getName() {return this.name;}
	 
	protected String getPageType() {return "page";}  				// con | det | det-version | task 	 
	protected String getContentTitle() {return null;} 				// content title or user title, ...

	protected String getStatsPageTitle() {return getName();} 		// for console page, it is the name of the console 
	private Long getStatsPageId() {return Long.valueOf(0);}         // for console page, it is the name of the console
														
	protected String getObjectId() {return null;}    				// for user, domain, ...
	protected String getContentId() {return null;}	 				// for content
	protected Serializable getContentOId() {return null;}	 		// for content
	protected Serializable getCId() {return null;}	 				// for content
	
	/**
	 * page_type = "Con"
	 * Site id = Console id ?
	 * Site Name = nombre de la consola
	 * 
	 * Content ID 
	 * Content OID
	 * Version 
	 */
	protected void logVisit() {
		
		// --------------------------------------------------------------------------
		// Agregar la info al log de Stat
		//

		if (isVisitLogged())
			return;
		
		
		try {

			SiteStatInEvent stat = new SiteStatInEvent();

			stat.domain_id = getDomain()!=null? Long.valueOf(getDomain().getId().toString()) : null;
			stat.sessionId = WebSession.get().getId();
			
			stat.page_type = getPageType();
			
			stat.site_id = Long.valueOf(getApplicationMenuSection().getId()); // Section (security, tasks)
			stat.site_title = getApplicationMenuSection().getKey();       // Section

			// for console page, it is the name of the console
			//
			stat.page_id =  getStatsPageId();
			stat.page_title = getStatsPageTitle();
			
			stat.user_id = getSessionUser()!=null ? Long.valueOf(getSessionUser().getId().toString()) : null;
			stat.user_name = getSessionUser()!=null ? getSessionUser().getFirstLastName() : null;
			stat.timestamp = OffsetDateTime.now();

			stat.user_agent = ((WebRequest) getRequest()).getHeader("User-Agent");
			stat.sessionId = WebSession.get().getId();
			
			stat.render_milisecs = Long.valueOf(end - start);

			 stat.content_title = getContentTitle(); 	// content title or user/domain/dataset title
			 stat.contentId  =  getContentId(); 			// for Content
			 
			 stat.content_long_id = getCId()!=null ? (Long) getCId() : Long.valueOf(0);
			 stat.OId  =  getContentOId()!=null ? getContentOId().toString() : null; 			
			 stat.objectId  =  getObjectId();  			// for User, Domain, DataSet, etc.
			 
			stat.content_version = getContentVersion() !=null ? getContentVersion() : Integer.valueOf(0);

			// Para que se logue en la Base de Datos
			// El logger de la Clase debe grabar en
			// el Appender "SiteStats"
			siteStatslogger.info(stat);
		} 
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			this.setVisitLogged(true);
			
		}
	}
	
	
	protected void setVisitLogged(boolean b) {
		this.visit_logged = b;
	}
	protected boolean isVisitLogged() {
		return this.visit_logged;
	}
}
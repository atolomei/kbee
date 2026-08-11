package kbee.web.content.nav;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.HelpService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Suggestion;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.Console;
import kbee.web.nav.NavBarUserMenu;
import kbee.web.notification.NotificationDelete;
import kbee.web.notification.UserNotificationsPage;
import kbee.web.portal6.directory.DirectoryMenuPanel;
import kbee.web.search.SearchPanel;
import kbee.web.search.SuggesterSearchPanel;

@SuppressWarnings("serial")
public class MainTopbar extends KBPanel {
			
	private static final long serialVersionUID = 1L;

	static private final String BELL = "fal fa-bell";
	
	private boolean isAlerts = true;
																
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GlobalNavigationBar.class.getName());


						
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	
	private Boolean is_domain_kbee = null;
	
	private String console_name= null;

	private Integer total_notifications_user;
	
	private static AtomicBoolean IS_HELP_VISIBLE= null;

	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
		return 	IS_HELP_VISIBLE.get();
	}

	/**
	 *  Listener UpdateNotifications 
	 */
	
	public class NotificationsFragment extends Fragment {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NotificationsFragment (String id) {
			super(id, "notifications-fragment", MainTopbar.this);
			
			this.setOutputMarkupId(true);
			
			Link<Void> link = new Link<Void>("notifications-link") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage( getUserNotificationsPage());
				}
				
			};
			
			
			Label tn =new Label("total-notifications", new Model<String>() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getObject() {
					return String.valueOf(geTotalNotificationsUser());
				}
			}) {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return geTotalNotificationsUser()>0;
				}
			};
			
			WebMarkupContainer icon = new WebMarkupContainer("icon");
			
			icon.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getObject() {
						try {
						if (geTotalNotificationsUser()>0)
							return BELL;
						else
							return BELL;
						} catch (Exception e) {
							logger.error(e);
							return BELL;
						}
				};
			}));
			
			link.add(icon);
			link.add(tn);
			add(link);
		}

	}
	
	
	public class HelpFragment extends Fragment {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HelpFragment(String id) {
			super(id, "help-fragment", MainTopbar.this);
			
			Link<Void> link = new Link<Void>("help-link") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					Page page = getPage();
					if (page instanceof AbstractKbeeWebPage) {
						String page_key = ((AbstractKbeeWebPage) page).getPageHelpKey();
						String section_key = ((AbstractKbeeWebPage) page).getPageInternalSectionHelpKey();
						String domain_type = getDomain().getDomainType().getLabel();
						String url = ServiceLocator.getService(HelpService.class).getHelpUrl(domain_type + "-" + page_key + (section_key!=null? ("-" + section_key):""));
						setResponsePage(new RedirectPage(url));
					}
				}
			};
			link.add(new AttributeModifier("target", "_blank"));
			link.setVisible( ServiceLocator.getService(BrandingService.class).isHelpEnabled());
			add(link);
		}
	}
	
					
	/**
	 * 
	 * 
	 */
	public class BrandFactoryFragment extends Fragment {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public BrandFactoryFragment(String id) {
			super(id, "brand-factory-fragment", MainTopbar.this);
			add(new Label("product", ServiceLocator.getService(BrandingService.class).getApplicationShortName()));
		}
	}

	

	/**
	public class BrandIDocFragment extends Fragment {
		private static final long serialVersionUID = 1L;

		public BrandIDocFragment(String id) {
			super(id, "brand-idoc-fragment", MainTopbar.this);
			
			this.setOutputMarkupId(true);
			
			Image img=null;
			img = new Image("brand",  getApplicationIcon()) {
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
			};

			
			
			Link<Void> rplink = new Link<Void>("rp-link") {
					@Override
					public void onClick() {
						setResponsePage(new RedirectPage(ServiceLocator.getService(BrandingService.class).getApplicationURL()));
					}
			};
			rplink.add(new AttributeModifier("class", "brand visible-md  visible-lg hidden-xs hidden-sm " + ServiceLocator.getService(BrandingService.class).getProductKey()));
			add(rplink);
			img.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getApplicationIconCss()));
			rplink.add(img);

			
			Link<Void> dlink = new Link<Void>("domain-link") {
				@Override
				public void onClick() {
					if (getDomain().getWebsite()!=null && getDomain().getWebsite().length()>1)
						setResponsePage(new RedirectPage(getDomain().getWebsite()));
				}
			};
					
			add(dlink);
			
			Label product_name = new Label("product", ServiceLocator.getService(BrandingService.class).getApplicationName());	
			dlink.add(product_name);
			
			Label domain_name = new Label("domain-name", getDomain().getOrganization());	
			dlink.add(domain_name);
			dlink.add( new AttributeModifier("class", "brand visible-md  visible-lg  brand-" + ServiceLocator.getService(BrandingService.class).getProductKey()));
		}
	}
	*/

	
	
	/*** -------------------------------------------------------------------------------
	 * 
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public MainTopbar() {
		this("navigation");
	}
	
	public MainTopbar(String id) {
		this(id, null, false);
	}
	
	public MainTopbar(String id, String returnLabel) {
			this(id, returnLabel, false);
	}
	
	public MainTopbar(String id, String returnLabel, boolean isSearch) {
		super(id);
		this.isSearch=isSearch;
	}
	
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	
	
	public void setIsAlerts(boolean b) {
		this.isAlerts=b;
	}

	//public void setIsNotes(boolean b) {
	//	this.isNotes=b;
	//}
	
	public String getConsoleName() {
		return console_name;
	}
	
	public void navigate() {
		onReturn();
	};
	
	@Deprecated
	public void navigate(AjaxRequestTarget target) {
		navigate();
	}
	
	public void onStartWorkflow() {
		
	};
	
	
	
	public void onReturn() {
		
	};
	
	public void setEditor(Editor<?> editor) {
		
	};
	
	public void setSearchPlaceHolder(String str) {
		if(get("search")!=null) {
			Component c = get("search");
			if (c instanceof kbee.web.search.SearchPanel)
				((kbee.web.search.SearchPanel) get("search")).setPlaceHolder(str);
		}
	}
	
	public boolean isFromContentBase() {
		return false;
	};
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
	}
	
	protected boolean isSuggester() {
		return false;
	}
	
	protected void onReturn(AjaxRequestTarget target) 		 					{}
	protected void onSearch(AjaxRequestTarget target, String text)  			{}
	protected void onSearch(AjaxRequestTarget target, Suggestion suggestion)  	{}
	
	
	/**
 	protected Component newNavigator(Searcher searcher, long index)  {
		return new NavigatorPanel<T>("navigator", searcher, (int)index) {
			public void onNavigate(T object) {
				MainTopbar.this.onNavigate(object);
			}
		};
	}**/

	/**
 	protected Component newIDocBrandPanel()  {
		Component co = new BrandIDocFragment("brand-idoc");
		co.setVisible(!isDomainKbee());
		return co;
	} 
	*/
	
	

	protected Component newFactoryBrandPanel()  {
		Component co = new BrandFactoryFragment("brand-factory");
		co.setVisible(isDomainKbee());
		return co;
	}
	

	protected Component newSearchPanel()  {
		String str = (getConsoleName()!=null?
				getLabel("search", getConsoleName()).getObject() :
				getLabel("search-alone").getObject());

		if (isSuggester()) {
			return new SuggesterSearchPanel("search", str) {
				
				@Override
				public void onSearch(AjaxRequestTarget target, String text) {
					MainTopbar.this.onSearch(target, text);
				}
				@Override
				public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
					MainTopbar.this.onSearch(target, suggestion);
				}
				@Override
				protected List<Suggestion> getSuggestions(String pattern) {
					return MainTopbar.this.getSuggestions(pattern); 
				}
			};
		}
		else {
			return new SearchPanel("search", str) {
				@Override
				public void onSearch(AjaxRequestTarget target, String text) {
					MainTopbar.this.onSearch(target, text);
				}
			};
		}
	}

	protected List<Suggestion> getSuggestions(String pattern) {
		return new ArrayList<Suggestion>();
	}
	

	protected void addComponents() {
		
		WebMarkupContainer navbar = new WebMarkupContainer("top-navbar");
		
		String s = ServiceLocator.getService(BrandingService.class).getApplicationCss();
		navbar.add(new AttributeModifier("class", isDomainKbee() ? " navbar navbar-default navbar-fixed-top "+ "nav"+s + " factory": "navbar navbar-default navbar-fixed-top " + "nav"+s));
		add(navbar);
		
		WebMarkupContainer hm = new WebMarkupContainer("hamburger-menu");
		navbar.add(hm);
				
		// navbar.add(newIDocBrandPanel());  		// iDOC
		navbar.add(newFactoryBrandPanel()); 	// kbee/iDOC: Factory		
		navbar.add(newSettingsPanel());
		navbar.add(newNotificationsPanel());
		navbar.add(newPortalPanel()); 
		navbar.add(newHelpPanel());
		navbar.add(newUserPanel());
		navbar.add(new InvisiblePanel("navigator"));
		if (isSearchPanel())
			navbar.add((newSearchPanel()));
		else
			navbar.add(new InvisiblePanel("search"));

		addListeners();
		
	}

	protected Component newSettingsPanel()  {
			return new InvisiblePanel("settings");

	}
	
	protected Component newNotificationsPanel()  {
			return  new NotificationsFragment("notifications");
 	}
	protected Component newHelpPanel()  {
		return  isHelpEnabled() ? new HelpFragment("help")  : new InvisiblePanel("help");
	}
	
	protected Component newReturnPanel(String returnLabel)  {
		WebMarkupContainer panel = new WebMarkupContainer("return-fragment");
		AjaxLink<?> returnLink = new AjaxLink<Void>("return-link") {
			public void onClick(AjaxRequestTarget target) {
				onReturn(target);
			}
		};
		returnLink.add(new Label("return-label", returnLabel));	
		panel.add(returnLink);
		return panel;
	}
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	protected Component newUserPanel()  {
		return new NavBarUserMenu("user");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected Console<?> getConsole() {
		return getConsole(getPage().iterator());
	}
	
	protected Console<?> getConsole(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Console<?>) {
				return (Console<?>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Console<?> console = getConsole(((WebMarkupContainer)component).iterator());
					if (console!=null) {
						return console;
					}
				}
			}
		}
		return null;
	}
	
	protected IRequestablePage getUserNotificationsPage() {
		return  new UserNotificationsPage(new ObjectModel<Person>(getPerson()));
	}

	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
	
	protected void addListeners() {

		add(new WicketEventListener<NotificationDelete>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(NotificationDelete event) {
				event.getRequestTarget().add(MainTopbar.this);
			}
		});
		
		
		/**
		add(new WicketEventListener<WorkNotesGridUpdate>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent( WorkNotesGridUpdate event) {
				event.getRequestTarget().add(MainTopbar.this);
			}
		});
		**/
	}
	

	
	
/**	

  	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		
		logger.debug("Fire Scan All " + event.getClass().getSimpleName());
		
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		
		fire(event, getPage().iterator(), false);
	}

	@SuppressWarnings("unchecked")
	public void fire(Event event) {
		
		logger.debug("Fire " + event.getClass().getSimpleName());
		
		boolean handled=false;
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
					handled = true;
					break;
				}
			}
		if (!handled) 
			fire(event, getPage().iterator());
	}
	
*/


	public boolean isSearch = true;
	
	public void setHasSearchPanel(boolean b) {
		this.isSearch=b;
	}
	
	public boolean isSearchPanel() {
		return this.isSearch;
	}


	protected int geTotalNotificationsUser() {
		if (total_notifications_user!=null)
			return total_notifications_user.intValue();
		total_notifications_user = Integer.valueOf(ServiceLocator.getService(NotificationService.class).getTotalNotifications(getSessionUser()));
		return total_notifications_user.intValue();
	}

	
	private Component newPortalPanel() {
	
		 if (ServiceLocator.getService(BrandingService.class).getProductKey().equals("RPDD") || ServiceLocator.getService(BrandingService.class).getProductKey().equals("RPCS"))
				 return new InvisiblePanel("portal");
		 
		if (getDomain().isPortalLibrary())
			return new DirectoryMenuPanel("portal", new ObjectModel<Person>(getPerson()));
		else 
			return new InvisiblePanel("portal");
	}

	private PackageResourceReference getApplicationIcon() {
		return ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getApplicationIcon();
	}


	private boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {						
				this.is_domain_kbee = Boolean.valueOf(getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				logger.error(e);
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

}

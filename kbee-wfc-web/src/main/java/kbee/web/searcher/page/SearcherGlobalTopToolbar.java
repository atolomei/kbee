package kbee.web.searcher.page;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.HelpService;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.BookmarksPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.event.EditableListEvent;
import com.novamens.kbee.wicket.markup.html.event.ExplorerOpenEvent;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.util.logging.Logger;
import kbee.web.nav.NavBarUserMenu;
import kbee.web.portal6.directory.DirectoryMenuPanel;
import kbee.web.portal6.sitemanager.SiteAdminOpenEvent;
import kbee.web.searcher.SearcherForm;
import kbee.web.service.ApplicationSiteMapService;


@SuppressWarnings("serial")
public class SearcherGlobalTopToolbar<T> extends KBPanel {
	private static final long serialVersionUID = 1L;

	static final boolean ONLY_USER_ACCOUNT = true;

	private static AtomicBoolean IS_HELP_VISIBLE= null;
	
	static AtomicBoolean IS_SITE_FAVS_VISIBLE;

	private static Logger logger = Logger.getLogger(SearcherGlobalTopToolbar.class.getName());
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_admin = is_root || ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
	final boolean is_workflow = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.WORKSPACE.getId());;
	
	private Boolean isHome = null;
	private Boolean isInstitutional = null;
	private Boolean isExplorer = Boolean.valueOf(true);
	private Boolean isSeachForm = null;
	
	private String specific_css;
	private String name;
	private long index;
	private Searcher searcher;
	private IModel<Site> siteModel;
	WebMarkupContainer navbar = new WebMarkupContainer("top-navbar");
	private BookmarksPanel book_panel = null;
	private Boolean explorerOn = Boolean.valueOf(false);
	private Integer total_notifications_user;
	
	 
	public SearcherGlobalTopToolbar(IModel<Site> site_model) {
		this("global-top-toolbar", site_model);
	}
 
	public SearcherGlobalTopToolbar(String id, IModel<Site> site_model) {
		super(id);
		setSiteModel(site_model);
		setOutputMarkupId(true);
		addListeners();
	}
	
	public SearcherGlobalTopToolbar(String id, IModel<Site> site_model,  Searcher searcher, long index, String console_name) {
		super(id);
		setSiteModel(site_model);
		setOutputMarkupId(true);
		this.searcher=searcher;
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
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (getSiteModel()==null || getSiteModel().getObject()==null)
			throw new IllegalArgumentException("site is null");
		
		addComponents();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (getSiteModel()!=null)
			getSiteModel().detach();
	}
	
	public void setSpecificCss(String  css) {
		this.specific_css=css;
	}
	
	protected void addComponents() {		

		String homecss= isHome() ? " home " : "";
		String instcss= !isHome() ? " institutional " : "";
		
		navbar.add( new AttributeModifier("class", " navbar navbar-default navbar-fixed-top kbsearcher " + homecss  +  instcss + 
				(this.specific_css!=null? (this.specific_css+" "):"") +
				ServiceLocator.getService(BrandingService.class).getSearchLibraryApplicationCss()));
		add(navbar);
		
		navbar.add(new BrandFragment("brand"));

		if (isSearchForm()) {
			SearcherForm f = new SearcherForm("search",  getSiteModel(), getSiteModel().getObject().getTitle());
			f.setAdvancedSearchLinkVisible(false);
			navbar.add(f);
		}
		else  
			navbar.add(new InvisiblePanel("search"));
					
		if (this.searcher==null)
			navbar.add((new InvisiblePanel("navigator")));
		else
			navbar.add(newNavigator(this.searcher, this.index));

		navbar.add(new NavBarUserMenu("user", ONLY_USER_ACCOUNT) {
			@Override
			protected WebPage getAccountPage() {
				return ServiceLocator
					.getService(ApplicationSiteMapService.class)
					.getPage("user-myaccount-page");
			}
		});
		
		navbar.add(new MediaInfoFragment("media-info"));
		navbar.add(isHelpEnabled() ? new HelpFragment("help") : new InvisiblePanel("help"));
		
		navbar.add(new InvisiblePanel("settings"));
		navbar.add(isSiteFavsEnabled() ? new MyFavsFragment("myfavs") : new InvisiblePanel("myfavs"));
		
		navbar.add(new MyListsFragment("mylists"));
		
		navbar.add(getSiteManagerMenu());
	
		addExplorer();
		addEditable();
	}
	
	public static boolean isSiteFavsEnabled() {
		if (IS_SITE_FAVS_VISIBLE!=null)
			return 	IS_SITE_FAVS_VISIBLE.get();
			IS_SITE_FAVS_VISIBLE = new AtomicBoolean(
				((ContentDao) (ServiceLocator.getService(BeansService.class)
					.getBean("contentDao")))
					.findSystemParameterValueByKey("sitefavs.enabled", "no")
					.toLowerCase().trim().equals("yes"));
		return 	IS_SITE_FAVS_VISIBLE.get();
	}
	
	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(
				((ContentDao) (ServiceLocator
					.getService(BeansService.class)
					.getBean("contentDao")))
					.findSystemParameterValueByKey("help.enabled", "no")
					.toLowerCase().trim().equals("yes"));
			
		return 	IS_HELP_VISIBLE.get();
	}

	protected Component newSettingsPanel(String id)  {
		
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
					}
					@Override 
					public String getLabel() {
						return "Settings 1";
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});
		
		return menu;
	}

	
	public void setHome(boolean b) {
		this.isHome = b;
	}
	
	public boolean isHome() {
		if (isHome==null)
			isHome = false;
		return isHome.booleanValue();
	}

	public boolean isExplorer() {
		if (isExplorer==null)
			isExplorer = false;
		return  isExplorer.booleanValue();
	}

	public void setExplorer(boolean b) {
		isExplorer = Boolean.valueOf(b);
	}
					
	public void setInstitutional(boolean b) {
		this.isInstitutional =b;
	}

	public boolean isInstitutional() {
		if (isInstitutional==null)
			isInstitutional = Boolean.valueOf(false);
		return  isInstitutional.booleanValue();
	}

	public boolean isSearchForm() {
		if (isSeachForm==null)
			isSeachForm = false;
		return  isSeachForm.booleanValue();
	}
	
	public void setSearchForm( boolean b) {
		isSeachForm =  Boolean.valueOf (b);
	}
	 
	protected void addExplorer() {
		navbar.addOrReplace( isExplorer() ? new ExplorerFragment("explorer") : new InvisiblePanel("explorer"));
	}

	protected void addEditable() {
		navbar.addOrReplace( new EditableFragment("editable"));
	}
	
	public Component getSiteManagerMenu() {
		AjaxLink<Void> link = new AjaxLink<Void>("site-manager") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("new SiteAdminEvent(target, getSiteModel()) -> " + getSiteModel().getObject().getTitle());
				fire(new SiteAdminOpenEvent<Site>(target, getSiteModel()));
			}
		};
		link.setVisible(false);
		return link;
	}

//	protected boolean hasWorkspace() {
//		return true;
//	}
	
	protected boolean isSubmitFile() {
		return true;
	}
	
	protected int geTotalNotificationsUser() {
		if (total_notifications_user!=null)
			return total_notifications_user.intValue();
		total_notifications_user = Integer.valueOf(ServiceLocator.getService(NotificationService.class).getTotalNotifications(getSessionUser()));
		return total_notifications_user.intValue();
	}
	
 	protected Component newNavigator(Searcher searcher, long index)  {
 		DummyBlockPanel panel = new DummyBlockPanel ("navigator", new Model<String>("new ResourcesPanel<T>"));
 		return panel;
	}

 	protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
 		return null;
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
 	
 	public boolean isExportSavedQueries() {
 		return false;
 	}
	
	public void setExplorerOn(boolean explorerOn) {
		this.explorerOn = Boolean.valueOf(explorerOn);
	}
	
	public Boolean isExplorerOn() {
		if (explorerOn==null)
			explorerOn = Boolean.valueOf(false);
		return explorerOn;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isWorkflowUser() {
		return is_admin || 
			UserProfileType.WORKFLOW_PARTICIPANT.equals(
				ServiceLocator.getService(UserService.class).getSessionUserProfile().getType());
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public class SearchFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public SearchFragment(String id) {
			super(id, "search-fragment", SearcherGlobalTopToolbar.this);
		}
	}
	
	public class MyListsFragment extends Fragment {
		public MyListsFragment(String id) {
			super(id, "mylists-fragment", SearcherGlobalTopToolbar.this);
			setOutputMarkupId(true);
			AjaxLink<Void> link = new AjaxLink<Void>("link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					MyListsFragment.this.onClick(target);
				}
			};
			add(link);
			add(new InvisiblePanel("bookmarks"));
		}
		@Override
		public boolean isEnabled() {
			return true;
		}
		public void onClick(AjaxRequestTarget target) {
			if (book_panel==null) {
				String key=getSiteModel().getObject().getOId().toString();
				SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(),  new HashMap<String, Object>());
				book_panel = new  BookmarksPanel("bookmarks", sq, key, getSiteModel(), true, true, true) {
					protected void close(AjaxRequestTarget target) {
						book_panel.setVisible(false);
						target.add(  MyListsFragment.this);
					}
					@Override
					protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
						return SearcherGlobalTopToolbar.this.getGridExportSavedQueryMenuItem(id,  model);
					}
				};
				book_panel.setExportSavedQueries(isExportSavedQueries());
				MyListsFragment.this.addOrReplace(book_panel);
				target.add( MyListsFragment.this);		
			}
			else {
				book_panel .setVisible(!book_panel.isVisible());
				target.add( MyListsFragment.this);
			}
		}
	}	
	
	
	public class MyFavsFragment extends Fragment {
		public MyFavsFragment(String id) {
			super(id, "favs-fragment", SearcherGlobalTopToolbar.this);
			add(new DirectoryMenuPanel("favs-panel", new ObjectModel<Person>(getPerson())));
		}
	}	
	
	
	public class SubmitFragment extends Fragment {
		public SubmitFragment(String id) {
			super(id, "submit-fragment", SearcherGlobalTopToolbar.this);
			Link<Void> link = new Link<Void>("submit-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage( new RedirectPage("/myhome"));
				}
			};
			
		 
			link.setVisible(isSubmitFile());
			add(link);
		}
	}	
	
	
	public class MediaInfoFragment extends Fragment {
		public MediaInfoFragment(String id) {
			super(id, "media-info-fragment", SearcherGlobalTopToolbar.this);
			
			Link<Void> link = new Link<Void>("media-info-link") {
				@Override
				public void onClick() {
				}
				@Override
				public boolean isVisible() {
					return getPage() instanceof SearcherDetailVideoPage; 
				}
			};
			add(link);
		}
	}	

	
	public class ExplorerFragment extends Fragment {
		public ExplorerFragment(String id) {
			super(id, "explorer-fragment", SearcherGlobalTopToolbar.this);
			Link<Void> link = new Link<Void>("explorer-link") {
				@Override
				public void onClick() {
					explorerOn = Boolean.valueOf(!explorerOn.booleanValue());
					addExplorer();
					fire(new ExplorerOpenEvent<Site>(getSiteModel()));
				}
			};
			
			if ( isExplorerOn() ) {
				link.add( new AttributeModifier("class", "selected" )); 
			}
			link.setVisible(isExplorer());
			addOrReplace(link);
		}
	}
		
	
	public class EditableFragment extends Fragment {
		public EditableFragment(String id) {
			super(id, "editable-fragment", SearcherGlobalTopToolbar.this);
			Link<Void> link = new Link<Void>("editable-link") {
				@Override
				public void onClick() {
					setEditable(!isEditableOn());
					fire(new EditableListEvent<Site>(getSiteModel()));
				}
			};
			add(link);
			if ( isEditableOn() ) {
				link.add( new AttributeModifier("class", "selected" )); 
			}
		}
		public boolean isEditableOn() {
			return "true".equals(Session.get().getAttribute("editables"));
		}
		public void setEditable(boolean value) {
			Session.get().setAttribute("editables", value ? "true" : "false");
		}
		@Override
		public boolean isVisible() {
			return isWorkflowUser();
		}
	}
		

	public class HelpFragment extends Fragment {
		public HelpFragment(String id) {
			super(id, "help-fragment", SearcherGlobalTopToolbar.this);
			
			Link<Void> link = new Link<Void>("help-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					Page page = getPage();
					if (page instanceof AbstractKbeeWebPage) {
						String page_key = ((AbstractKbeeWebPage) page).getPageHelpKey();
						String section_key = ((AbstractKbeeWebPage) page).getPageInternalSectionHelpKey();
						String url = ServiceLocator.getService(HelpService.class).getHelpUrl(page_key+ (section_key!=null? ("-" + section_key):""));
						setResponsePage(new RedirectPage(url));
					}
				}
			};
			link.add(new AttributeModifier("target", "_blank"));
			link.setVisible(ServiceLocator.getService(BrandingService.class).isHelpEnabled());
			add(link);
		}
	}	
	

 
	public class BrandFragment extends Fragment {
		public BrandFragment(String id) {
			super(id, "brand-fragment", SearcherGlobalTopToolbar.this);
			
			this.setOutputMarkupId(true);

			Link<Void> dlink = new Link<Void>("domain-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage(new SearcherHomePage(getSiteModel()));
				}
			};
			add(dlink);

			Label product_name = new Label("product", getSiteModel().getObject().getTitle());
			dlink.add(product_name);
			dlink.add(new AttributeModifier("title", getSiteModel().getObject().getTitle()));
			Label domain_name = new Label("domain-name", getDomain().getOrganization());	
			dlink.add(domain_name);
			dlink.add( new AttributeModifier("class", "hidden-sm hidden-xs  brand-domain brand-" + ServiceLocator.getService(BrandingService.class).getProductKey()));
		}
	}

 
	
	
	
	/**
	 *  Listener UpdateNotifications 
	 */
	
	public class NotificationsFragment extends Fragment {
		public NotificationsFragment (String id) {
			super(id, "notifications-fragment", SearcherGlobalTopToolbar.this);
			
			this.setOutputMarkupId(true);
			
			AjaxLink<?> link = new AjaxLink<Void>("notifications-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(new SearcherNotificationsPage(getSiteModel()));
				}
				
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					IAjaxCallListener listener = new IAjaxCallListener() {
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							String s = null, s1=null, s2=null;
							String id = component.getMarkupId();
							String bel;
							int total = geTotalNotificationsUser();
							if (total>0) {
								bel="fal fa-bell";
								String str_total=String.valueOf(total);																																						
								s2="<span style=\"color: white;   float:left; background: #d02424; right: 0px; line-height: 1em; padding: 4px; font-size: 11px;   min-width: 19px;   text-align: center; margin-left: -5px; border-radius: 4px; top: 0;\">"+str_total+"</span>"+"';";
							}
							else {
								s2="';";
								bel="fal fa-bell";
							}
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\" "+ bel + "\" style=\"float:left;\"></i>";
							 s ="setTimeout(function () {" + s1 + s2 +"}, 350);";
							 
							return s; 
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin spinning fa-fw\"></i>'";
							return s;
						}
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}
				
				@Override
				public boolean isVisible() {
					return ServiceLocator.getService(BrandingService.class).isNotificationEnabled();
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
							return "fal fa-bell";
						else
							return "fal fa-bell";
						} catch (Exception e) {
							logger.error(e);
							return "fal fa-bell";
						}
				};
			}));
			
			link.add(icon);
			link.add(tn);
			add(link);
		}
	}
	
	public class MyNotesFragment extends Fragment {
		public MyNotesFragment(String id) {
			super(id, "mynotes-fragment", SearcherGlobalTopToolbar.this);
			this.setOutputMarkupId(true);
			Link<Void> link = new Link<Void>("mynotes-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					User user = getSessionUser();
					com.novamens.content.entity.Person person = getContentDao().findUserProfileByUser(user).getPerson();
					setResponsePage(new SearcherUserNotesPage(new ObjectModel<Person>(person), getSiteModel()));
				}
			};
			add(link);
		}
	}
}

package kbee.web.security.user;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.entity.Person;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.UserSet;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.event.ClickBackEvent;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.console.CursorNavigator;



import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.CursorNavigationEvent;
import kbee.web.nav.HomeBC;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanelV6;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.panel.ClickItemEvent;
import kbee.web.security.UsersQuery;
import kbee.web.user.UserAvatarPanel;
import kbee.web.workflow.task.PageTaskToolbar;

/**
 * Open PageHeader
 * Std PageHeader
 */
@SuppressWarnings("serial")
public class UserPage extends ConsoleObjectPage<Person> implements NavigablePage<Person> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(UserPage.class.getName());
	
	final boolean is_root = ServiceLocator
		.getService(SecurityService.class)
		.isRoot(); 
	final boolean is_domain_admin = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_federated_security  = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());
 	final boolean is_support = 
 		ServiceLocator
 		.getService(SecurityService.class)
 		.isMember(KbeeGlobalRole.SUPPORT.getId());

	private boolean is_myaccount;
	
	private boolean editionEnabled = false;
						
	private Navigator<Person> navigator;
	
	
	
	/**
	 * @param parameters
	 */
	public UserPage(PageParameters parameters) {
		Person person = getPerson(parameters);
		if (person!=null) {
			setModel(new ObjectModel<Person>(person));
			setLogVisit(true);
		}
	}

	/**
	 * 
	 */
	public UserPage(IModel<Person> model) {
		this(model, null);
	}

	/**
	 * 
	 */
	public UserPage(IModel<Person> model, IModel<Cursor> cursor_model) {
		super(model, cursor_model);
		setLogVisit(true);
	}
	

	public boolean isEditionEnabled() {
		return this.editionEnabled;
	}
	

	public void setEditionEnabled(boolean b) {
		this.editionEnabled=b;
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		if (navigator!=null)
			navigator.detach();
	}
	
					
	public Navigator<Person> getNavigator() {
		return this.navigator;
	}

	public void setNavigator(Navigator<Person> navigator) {
		this.navigator=navigator;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		if (getModel()!=null && hasPermissions()) {
			addComponents();
			
			PageContentHeaderPanel<Person> panel=new PageContentHeaderPanel<Person>(getModel());
			
			User user=getModel().getObject().getProfile(UserProfile.class).getUser();
			
			 panel.setAvatarPanel(new UserAvatarPanel("avatar", new ObjectModel<User>(user)));
			 panel.setSubLine(new Model<String>(getModel().getObject().getWorkPosition()));
			 
			panel.setTitle(getModel().getObject().getFirstLastName());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.users", this, null).getObject()));
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			panel.setMenuPanel(getContentHeaderPanelMenuPanel());
			setSearchPanel(true);
			setClearAllSearch(false);
			setAdvancedSearch(false);
			setSuggester(false);
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			r_list.add(getSearchPanel("panel"));
			r_list.add(getSearchNavigation("panel"));
			PageTaskToolbar<Person> toolbar = new PageTaskToolbar<Person>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);
			
			setPageContentHeader(panel);
			
		}
		else {
			add(new ErrorPanel("editor"));
		}
	}

	
	public Panel getSearchNavigation(String id)  {
		
		if (getCursorModel()==null)
			return new InvisiblePanel(id);
		
		if (getNavigator()==null) {
			Navigator<Person> c=new CursorNavigator<Person>(getCursorModel().getObject());
			setNavigator(c);
		}
		
		NavigatorPanelV6<Person> na = new NavigatorPanelV6<Person>("panel", getNavigator());
		na.setResultsPanel(true);
		return na;
	}
	
	@Override
	protected Page getNavigatePage(Person object, IModel<Cursor> mc) {
		return new UserPage(new ObjectModel<Person>(object), mc);
	}
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		if (this.is_myaccount) {
			
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			
			bc.addElement( new HomeBC());
			
			bc.addElement( new AccountDropDownBC());			
			bc.addElement(new BCElement(new StringResourceModel("my-account", UserPage.this, null)));
			return bc;
		}
		else {
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
			
			bc.addElement(new HomeBC());
			bc.addElement(new SecurityDropDownMenuBC());	
			bc.addElement(new UsersBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getFirstLastName())));
			return bc;
		}
	}
	
	public UserSet getUserSet() {
		UserSet userset = getContentDao().getUserSet();
		if (userset==null)
			logger.error("UserSet is null for domain " + getDomain().getName());
		return userset;
	}
	
	public Query newQuery() {
		return new UsersQuery(getQueryIndex(), getUserSet());
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	/**
	 * 
	 * @param mo
	 * @param index
	 */
	public void onNavigate(IModel<Person> mo, int index) {
			try {
					// getCursorModel().getObject().setIndex(index);
					UserPage page = new UserPage(mo, getCursorModel());
					page.setNavigator(getNavigator());
					setResponsePage(page);
					
			} 
			catch (Exception e) {
				logger.error(e);
				setResponsePage( new ApplicationErrorPage<>(e));
			}
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		
		add(new WicketEventListener<ClickBackEvent<Person>>() {
			@Override
			public void onEvent(ClickBackEvent<Person> event) {
						setResponsePage( new UsersPage());
			}
		});
		
		
		add(new WicketEventListener<ClickItemEvent<Person>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickItemEvent<Person> event) {
				try {
							Person person = event.getModel().getObject();
							
							getCursorModel().getObject().setIndex(event.getIndex());
							UserPage page = new UserPage( new ObjectModel<Person>(person), getCursorModel());
							
							Navigator<Person> ns=getNavigator();
							
							
							page.setNavigator(ns);
							
							setResponsePage(page);
							getPage().detach();
							return;
						
				} catch (Exception e) {
					logger.error(e);
					setResponsePage( new ApplicationErrorPage<>(e));
					
				}
			}
		});

		
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=newQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new UsersPage( new ObjectModel<UserSet>(getUserSet()), q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
		
		
		add(new WicketEventListener<CursorNavigationEvent<Person>>() {
			public void onEvent(CursorNavigationEvent<Person> event) {
				UserPage.this.onNavigate(event.getModel(), event.getIndex());
				event.detach();
			}
		});
		
	}	


	/**
	 * 
	 */
 	@Override
	public boolean hasPermissions() {
			
		if (getModel().getObject()==null)
			return false;
			
		if (!getDomain().getId().equals(getModel().getObject().getDomain().getId()))
			return false;
			
		if (is_security || is_domain_admin || is_root || is_support) {
			return true;
		}
		
		//if (is_federated_security) {
		return ServiceLocator
			.getService(UserService.class)
			.isUserAdmin(getModelObject());
		//}
 	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}

	protected void addComponents() {
		
		IModel<Person> model = getModel();
		
		setPageTitle(new Model<String>(model.getObject().getLastFirstName()));
		UserMainPanel editor = new UserMainPanel(model) {
			@Override
			public void onCancel(AjaxRequestTarget target) {
			}
		};
		
		editor.setEditionEnabled(this.isEditionEnabled());
		add(editor);
		getPageParameters().set("id", model.getObject().getProfile(UserProfile.class).getUser().getId());
	}
	
	protected String getTabParameter() {
		StringValue tab = getPageParameters().get("tab");
		
		if (!tab.isNull() && !tab.isEmpty()) {
			return tab.toOptionalString();
		}
		return null;
		
	}

	protected Person getPerson(PageParameters parameters) {
		Person person = null;
		try {
			
			StringValue id = parameters.get("id");
			if (!id.isNull() && !id.isEmpty()) {
				UserProfile up = getContentDao().findUserProfileByUserId(id.toLong());
				if (up!=null) {
					person = getContentDao().findUserProfileByUserId(id.toLong()).getPerson();
					if (person!=null && !person.getDomain().equals(getDomain())) {
						person = null;
					}
				}
			}	
		} catch (Exception e ) {
			logger.error(e);
			
		}
		return person;
	}
	
	
	@Override
	protected String getName() {
		return "user";
	}
	
	@Override
	protected void addModals() 	 {
		super.addModals();
		addOrReplace(new ErrorDialog("page-error-dialog"));
	}
	
	/** 
	 *  Reports 
	 **/
	protected String getPageType()     {
		return "det";
	} 													 // con | det
	
	protected String getContentTitle() {
		return getModel().getObject().getLastFirstName();
	} 				// content title or user title, ...

	protected String getStatsPageTitle() {
		return getModel().getObject().getLastFirstName();
	} 			// for console page, it is the name of the console
	
	protected Long getStatsPageId() {return Long.valueOf(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content
}

package kbee.web.security.user;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.UserSet;
import com.novamens.content.user.UserProfile;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.DomainType;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.security.UsersQuery;

public class GroupMembersPage extends ConsoleObjectPage<Group> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GroupMembersPage.class.getName());
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
 	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private static final long serialVersionUID = 1L;

	private boolean is_myaccount;
	
	private boolean editionEnabled = false;
	
	public GroupMembersPage(PageParameters parameters) {
		Group person = getGroup(parameters);
		if (person!=null) {
			setModel(new ObjectModel<Group>(person));
			setLogVisit(true);
		}
	}

	public GroupMembersPage(IModel<Group> model) {
		this(model, null);
	}
	
	public GroupMembersPage(IModel<Group> model, IModel<Cursor> cursor_model) {
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
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		if (getModel()!=null && hasPermissions()) {
			addComponents();
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getModel().getObject().getDisplayName());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.users", this, null).getObject()));
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			panel.setMenuPanel(getContentHeaderPanelMenuPanel());
			setSearchPanel(true);
			setClearAllSearch(false);
			setAdvancedSearch(false);
			setSuggester(false);
			panel.setSearchPanel(getSearchPanel());
			panel.setSearchNavigatorPanel(getSearchNavigation());
			setPageContentHeader(panel);
		}
		else {
			add(new ErrorPanel("editor"));
		}
	}

	
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		if (this.is_myaccount) {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement( new AccountDropDownBC());			
			// bc.addElement(new BCElement(new StringResourceModel("my-account", UserPage.this, null)));
			return bc;
		}
		else {
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
			boolean is_basic = getDomain().getDomainType()==DomainType.EXPRESS;
			if (!is_basic)
				bc.addElement(new SecurityDropDownMenuBC());
			else
				bc.addElement(new BCElement(new Model<String>("Security")));
			bc.addElement(new UsersBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
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
	 */
 	@Override
	public boolean hasPermissions() {
			
		if (getModel().getObject()==null)
				return false;
			
		// Session User's Domain must be the same as ObjectModel´s Domain
		//
		//if (!getDomain().getId().equals(getModel().getObject().getDomain().getId()))
		//		return false;
			
		return is_domain_admin || is_root || is_support; 
 	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}

	protected void addComponents() {
		
		IModel<Group> model = getModel();
		
		// setPageTitle(new Model<String>(model.getObject().getLastFirstName()));
		
		/**
		UserMainPanel editor = new UserMainPanel(model) {
			@Override
			public void onCancel(AjaxRequestTarget target) {
				Component navigation = getTopNavigation();
				if (navigation instanceof GlobalNavigationBar<?>) {
					((GlobalNavigationBar<?>)navigation).onReturn();
				}
			}
		};
		
		editor.setEditionEnabled(this.isEditionEnabled());
		add(editor);
		getPageParameters().set("id", model.getObject().getProfile(UserProfile.class).getUser().getId());
		**/
	}
	
	
	protected String getTabParameter() {
		StringValue tab = getPageParameters().get("tab");
		if (!tab.isNull() && !tab.isEmpty()) {
			return tab.toOptionalString();
		}
		return null;
	}

	protected Group getGroup(PageParameters parameters) {
		Group person = null;
		try {
			StringValue id = parameters.get("id");
			if (!id.isNull() && !id.isEmpty()) {
					//person = getContentDao().findGr .findUserProfileByUserId(id.toLong()).getPerson();
					//if (person!=null && !person.getDomain().equals(getDomain())) {
					//	person = null;
					//}
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
	protected String getPageType()     {return "group";} 													 // con | det  
	protected String getContentTitle() {return getModel().getObject().getDisplayName();} 				// content title or user title, ...

	protected String getStatsPageTitle() {return getModel().getObject().getDisplayName();} 			// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content

}

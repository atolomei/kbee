package kbee.web.security.role;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.notes.BillboardPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.InfoPageEvent;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.RolesQuery;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class RolePage extends ConsoleObjectPage<Role> {
			
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
 	final boolean is_support 		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RolePage.class.getName());

	private static final long serialVersionUID = 1L;

	public RolePage(PageParameters parameters) {
		Role role = getRole(parameters);
		if (role!=null) {
			setModel(new ObjectModel<Role>(role));
		}
	}

	public RolePage(IModel<Role> model) {
		super(model);
	}
	
	
	public RolePage(IModel<Role> model, IModel<Cursor> cursor_model) {
		super(model, cursor_model);
	}
	
	
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
			
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		
		if (getModel()==null || getModel().getObject()==null) 
			throw new IllegalArgumentException("Model can not be null");
		
		if (hasPermissions()) {
			
			addComponents(getModel(), false, false); 
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getModel().getObject().getDisplayName());
			MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
			
			bc.addElement(new HomeBC());
			bc.addElement(new SecurityDropDownMenuBC());
			bc.addElement(new RoleDropDownBC());
			bc.addElement(new BCElement(new Model<String>(getModel().getObject().getDisplayName())));
			
			panel.setBreadcrumbPanel(bc);
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.roles", this, null).getObject()));
			setSearchPanel(true);
			setAdvancedSearch(false);
			setSuggester(false);
			
			// panel.setSearchPanel(getSearchPanel());			
			//panel.setSearchNavigatorPanel(getSearchNavigation());
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			r_list.add(getSearchPanel("panel"));
			r_list.add(getSearchNavigation("panel"));
			PageTaskToolbar<Role> toolbar = new PageTaskToolbar<Role>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);

			
			
			setPageContentHeader(panel);
			setLogVisit(true);
			
		}
		else {
			add(new InvisiblePanel("editor"));
		}
		
		} catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("editor", e));
		}
	}
	
	@Override
	protected Page getNavigatePage(Role object, IModel<Cursor> mc) {
		return new RolePage(new ObjectModel<Role>(object), mc);
	}

	
	
	@Override
	public boolean hasPermissions() {
		
		if (getModel()==null || getModel().getObject()==null)
			return false;
			
		// Session User's Domain must be the same as  ObjectModel´s Domain
		if (!getDomain().equals(getModel().getObject().getDomain()))
				return false;
			
		return is_security || is_domain_admin || is_root || is_support; 
	}

	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}

	protected void addComponents(IModel<Role> model, boolean edition, boolean isNew) {

		setPageTitle(new Model<String>(model.getObject().getName()));
		RoleMainPanel editor = new RoleMainPanel(model, isNew) {
			@Override
			public void onCancel(AjaxRequestTarget target) {
//				Panel navigation = getNavigation();
//				if (navigation instanceof GlobalNavigationBar<?>) {
//					((GlobalNavigationBar<?>)navigation).onReturn();
//				}
			}
			
			protected void onClose(AjaxRequestTarget target) {
				setResponsePage(new RolesPage());
			}
			
		};
		editor.setEditionEnabled(edition);
		add(editor);
		getPageParameters().set("id", ((KbeeAbstractRole)model.getObject()).getId());
	}

	protected Role getRole(PageParameters parameters) {
		Role role = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			role = getContentSecurityDao().findRoleById(id.toLong());
			if (role!=null && !role.getDomain().equals(getDomain())) {
				role = null;
			}
		}	
		return role;
	}

	
	public Query newQuery() {
		return new RolesQuery(getQueryIndex());
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=newQuery();
				q.getParameters().put("text",new TextFilter(event.getText()));
				
				q.getParameters().put("sort", "relevance");
				setResponsePage(new RolesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
		

		/**
		add(new WicketEventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				
				logger.debug(event.getThrowable().getClass().getName());
				logger.debug(event.getThrowable().getMessage());
				logger.debug(event.getThrowable().getCause());
				
				IModel<String> titlemodel = new Model<String>("Error");
				IModel<String> messagemodel = new Model<String>("<h3>"+event.getThrowable().getClass().getSimpleName() +"</h3><br/><p> " + event.getThrowable().getMessage()+"</p>");
				
				((ErrorDialog) getErrorDialog()).open(event.getRequestTarget(), titlemodel, messagemodel);
				
			}
		});
		**/
	}	
	
	
	protected String getPageType()     		{return "det";} 													// con | det  
	protected String getContentTitle() 		{return getModel().getObject().getDisplayName();} 					// content title or user title, ...
	protected String getStatsPageTitle() 	{return getModel().getObject().getDisplayName();} 					// for console page, it is the name of the console 
	protected Long getStatsPageId() 		{return new Long(0);} 								                // for console page, it is the name of the console
	protected String getObjectId()  		{return new ObjectId(getModel().getObject()).toString();}    		// for user, domain, ...
	protected String getContentId() 		{return null;}	  													// for content

	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}

package kbee.web.content.console;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.web.suggestion.service.WorkspaceSearchSuggestionService;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Task;

import kbee.web.console.Console;
import kbee.web.console.ContentConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.MyWorkspaceDropDownBC;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.nav.WorkspaceBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class WorkspacePage extends ContentConsolePage<Content> {
				
	private static final long serialVersionUID = 1L;
										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkspacePage.class.getName());
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_mon = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId());
	final boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	
	public WorkspacePage(PageParameters parameters) {
		super(null);
		setName(WorkspaceConsole.NAME);
	}
	
	public WorkspacePage() {
		super(null);
		setName(WorkspaceConsole.NAME);
	}
	
	public WorkspacePage(Query query) {
		super(query);
		setName(WorkspaceConsole.NAME);
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public Console<Content> newConsole(Query query) {
		WorkspaceConsole c = new WorkspaceConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return WorkspacePage.this.getConsolePage(query, index);
			}
		};
		return c;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new WorkspacePage(query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
						
		panel.setTitle(new StringResourceModel("mytasks", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		panel.setMenuPanel(getContentHeaderPanelMenuPanel());
		
		setSearchPlaceHolder(new StringResourceModel("search-in-my-tasks", this, null).getObject());
		
		setSuggester(true); 			//  Search supports suggester
		setSearchPanel(true); 			//  include Search
		setAdvancedSearch(true); 		//  button advanced search
		
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Content> toolbar = new PageTaskToolbar<Content>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		
		
		setPageContentHeader(panel);
		
		
		/**
		Site site;
		try {
				site = ServiceLocator.getService(PortalDirectoryService.class).findDashboardSite(getSessionUser());
				 if (site==null)
					 site=ServiceLocator.getService(SiteFactoryService.class).createDashboardSite(getPerson());
		
		} catch (PortalException e) {
				logger.error(e);
		}
		**/

		// Site da=getPortalDao().
		// Site site = getPortalDao().findSiteById(Long.valueOf(3543512));
		// String st =site.treeString();
		// logger.debug(st);
		// 
		//
		//
		//
		//
		
	}
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
			
			bc.addElement( new HomeBC());
 			
			bc.addElement(new MyWorkspaceDropDownBC());
 			bc.addElement(new BCElement("mytasks"));
			
 			return bc;
			
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	
	
	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user", String.valueOf(getSessionUser().getId()));
		return getDomain().getService(WorkspaceSearchSuggestionService.class).getSuggestions(pattern, parameters); 
	}
	
	
	@Override
	protected String getUrl(IModel<Content> model) {
		String url;
		Content content = model.getObject();
		WorkflowService ws = content.getService(WorkflowService.class);
		if (ws!=null) {
			Task task = ws.getTask();
			if (task!=null) {
				String content_class_name = model.getObject().getClassCode();
				String task_name = task.getName().replaceAll("\\s", "-").toLowerCase();
				url = "task/" + content_class_name + "/"  + task_name + "/" + String.valueOf(content.getId());
			}
			else
				url = super.getUrl(model);
		}
		else
			url = super.getUrl(model);
		return url;
	}
	
	
	@Override
	protected boolean isSuggester() {
		return true;
	}

	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
					// getQuery().getParameters().put("text", new TextFilter(event.getText()));
					getQuery().getParameters().put("text", event.getText());
					getQuery().getParameters().put("sort", "relevance");
					setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_mon || is_support; 
	}


	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		str.append("<p><b>Domain Admin</b> and <b>Support users</b> can access this Page.");
		str.append("If you are none of them: you need <b>My Tasks</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		return str.toString();
	}
	
	public PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
}

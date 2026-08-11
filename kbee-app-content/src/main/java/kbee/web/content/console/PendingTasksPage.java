package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.UserSet;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Task;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class PendingTasksPage extends ConsolePage<Content> {
		
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PendingTasksPage.class.getName());
	
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_pending				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	

	public PendingTasksPage() {
		super(null);
	}
	
	public PendingTasksPage(Query query) {
		super(query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle( new StringResourceModel("pending", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		panel.setMenuPanel(getContentHeaderPanelMenuPanel());
		
		setSearchPlaceHolder(new StringResourceModel("search-in-pending", this, null).getObject());
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(true); // button advanced search
		
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Content> toolbar = new PageTaskToolbar<Content>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		setPageContentHeader(panel);
		setLogVisit(true);

	}
	
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
					//getQuery().getParameters().put("text", new TextFilter(event.getText()));
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
	
	
	//protected Panel getAdvancedSearchPanel() {
	//	return new AdvancedSearchButton<UserSet>("advancedsearch-panel", PendingTasksConsole.KEY);
	//}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
	 		MenuBreadCrumbPanel<Void> bc =new MenuBreadCrumbPanel<Void>();
	 		
	 		
	 		bc.addElement( new HomeBC());
	 		
	 		bc.addElement(new TasksDropDownMenuBC());
			 bc.addElement(new BCElement("pending"));
			 return bc;

		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
	
	
	
	public Console<Content> newConsole(Query query) {
					
		return new PendingTasksConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return new PendingTasksPage(query);
			}
			 
		};
	}
	 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new PendingTasksPage(query);
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}

	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_pending || is_support; 
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

	
	
	
	
	
	
	
	
}

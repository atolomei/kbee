package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ContentConsolePage;
import kbee.web.draftresources.DraftDropDownResourcesBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class PublicResourcesPage extends ContentConsolePage<Content> {
	
private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PublicResourcesPage.class.getName());
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	//final boolean is_my_box =  ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_MY_RESOURCES.getId());
	 
	public  PublicResourcesPage(PageParameters parameters) {
		super(null);
		setName(MyBoxConsole.NAME);
	}
	
	public  PublicResourcesPage() {
		super(null);
		setName(MyBoxConsole.NAME);
	}
	
	public  PublicResourcesPage(Query query) {
		super(query);
		setName(PublicBoxConsole.NAME);
	}

	
	
	

	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public Console<Content> newConsole(Query query) {
		MyBoxConsole c = new PublicBoxConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return  PublicResourcesPage.this.getConsolePage(query, index);
			}
		};
		return c;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new  PublicResourcesPage(query);
	}
	
	/**
	 * 
	 * icon
	 * size
	 * uploaded
	 * 
	 * "email text"
	 * 
	 * type
	 * status
	 * label
	 * list
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		// getDomain().getService(DomainModelBuilderService.class).buildMyBox();
		// convertUploadAndCreateContainer();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
						
		panel.setTitle(new StringResourceModel("draft-folder-public", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		panel.setMenuPanel(getContentHeaderPanelMenuPanel());
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).setParameters( new Object[] { PublicResourcesPage.this.getName() }) .getObject());
		
		//setSearchPlaceHolder(new StringResourceModel("search-in-public-drafts", this, null).getObject());
		
		setSuggester(true); 			//  Search supports suggester
		setSearchPanel(true); 			//  include Search
		setAdvancedSearch(false); 		//  button advanced search
		
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Content> toolbar = new PageTaskToolbar<Content>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
	}
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
			bc.addElement( new HomeBC());
			
			bc.addElement(new DraftDropDownResourcesBC());
 			bc.addElement(new BCElement(new StringResourceModel("draft-folder-public", this, null)));
			return bc;
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	
	/**
	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user", String.valueOf(getSessionUser().getId()));
		return getDomain().getService(WorkspaceSearchSuggestionService.class).getSuggestions(pattern, parameters); 
	}
	**/
	
	/**
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
	**/
	
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
		return true; // is_root || is_admin; 
	}


	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		//str.append("<p><b>Domain Admin</b> and <b>Support users</b> can access this Page. ");
		//str.append("If you are none of them: you need <b>My Tasks</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		return str.toString();
	}
	
	/**
	 * 
	 * 
	 
	public void convertUploadAndCreateContainer() {

		try {
			
			IDoc idoc = ServiceLocator.getService(UserService.class).getUploadAndCreateContainer();
			
			if (idoc==null)
				return;
			
			ContentTemplate template = null;
			
			for (ContentTemplate t: getContentDao().getTemplates(getDomain())) {
				if (t.getAlias()!=null && t.getAlias().equals("mybox")) {
					template=t;
					break;
				}
			}
			
			if (template==null)
				return;

			DataSet da = null;
			Classifier type = null;
			DataSetMember me = null;
			
			for (ClassifierTemplate c: template.getClassifiers()) {
				if (c.getClassifier().isContentType()) {
					type=c.getClassifier();
					da=c.getClassifier().getDataSet();
					break;
				}
			}
			
			if (da!=null) 
				me = getContentDao().findMemberByValue(da, getContentDao().findSystemParameterValueByKey("datasetvalue.mybox.strvalue", "Resource"));
			
			 
			
			List<KBFile> list = idoc.getFiles();
			
			for (KBFile file: list) {
				IDoc c = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(template.getName());
				c.addResource(file);
				c.setTitle(file.getTitle());
				c.setLastModifiedOffsetDateTime(file.getLastModifiedOffsetDateTime());
				c.setLastModifiedUser(file.getLastModifiedUser());
				c.setState(ObjectState.DRAFT);
				
				if (me!=null) {
					Classification cl=new KbeeClassification(type, me, c);
					c.addClassification(cl);
				}
				
				c.getService(ContentService.class).update();
				
				logger.debug(c.getTitle());
			}
			
			idoc.setResources(new ArrayList<Resource>());
			idoc.getService(ContentService.class).update();
			
				
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	*/


}

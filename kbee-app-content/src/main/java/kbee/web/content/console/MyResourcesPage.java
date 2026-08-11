package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.domain.provisioning.DomainModelBuilderService;
import com.novamens.kbee.content.model.KbeeClassification;
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
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

/**
 * 
 *
 */
public class MyResourcesPage extends ContentConsolePage<Content> {
	
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkspacePage.class.getName());
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	//final boolean is_my_box =  ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_MY_RESOURCES.getId());;
	 
	public  MyResourcesPage(PageParameters parameters) {
		super(null);
		setName(MyBoxConsole.NAME);
	}
	
	public  MyResourcesPage() {
		super(null);
		setName(MyBoxConsole.NAME);
	}
	
	public  MyResourcesPage(Query query) {
		super(query);
		setName(MyBoxConsole.NAME);
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public Console<Content> newConsole(Query query) {
		MyBoxConsole c = new MyBoxConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return  MyResourcesPage.this.getConsolePage(query, index);
			}
		};
		return c;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new  MyResourcesPage(query);
	}
	
	/**
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
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		// convertUploadAndCreateContainer();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
						
		panel.setTitle(new StringResourceModel("mybox", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		panel.setMenuPanel(getContentHeaderPanelMenuPanel());
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).setParameters( new Object[] { MyResourcesPage.this.getName() }) .getObject());
		
		setSuggester(true); 			//  Search supports suggester
		setSearchPanel(true); 			//  include Search
		setAdvancedSearch(false); 		//  button advanced search
		
		// panel.setSearchPanel(getSearchPanel());
		
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
 			bc.addElement(new BCElement("mybox"));
 			
			return bc;
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
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
		 
		return true; // is_my_box || is_root || is_admin; 
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
			template=getDomain().getService(DomainService.class).getResourcesTemplate();
			
			if (template==null)
				return;

			DataSet da = null;
			Classifier type = null;
			DataSetMember me = null;

			da = getDomain().getService(DomainService.class).getResourcesTypeClassifier().getDataSet();
			if (da!=null) 
				me = getDomain().getService(DomainService.class).getResourcesTypeDataSetMember();
			
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

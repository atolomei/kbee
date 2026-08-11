package kbee.web.content.console;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.userlist.UserList;
import com.novamens.content.web.content.markup.LabelsModal;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.lock.ValueLockerService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.SolrSearcherNavigator;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.content.panel.ShareModal;
import kbee.web.dashboard.LabelPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.object.AuditTrailModal;
import kbee.web.panel.ClickItemEvent;
import kbee.web.panel.ListSimpleItemMainPanel;
import kbee.web.searcher.panel.SearcherContentViewPanel;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;


/**
 * @param <T>
 */
@SuppressWarnings("serial")				
public abstract class ContentConsole<T extends Content> extends AbstractFacetedConsole<T> {
	
	static final public String PROPERTY_UNREAD = "unread";
	static final public String TO_ESC="<br\\s*/>\\s*<br\\s*/>";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentConsole.class.getName());
	
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final protected boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();

	private boolean is_send_email;

	private boolean eforms = true;
	private IModel<User> model_wuser;
	
	
	public ContentConsole(String id, String name, Query query) {
		super(id, name, query);
		setOutputMarkupId(true);
		setListBrowser(true);
	}
	
	public ContentConsole(String name, Query query) {
		super(name, query);
		setOutputMarkupId(true);
		setListBrowser(true);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			createFavListIfNotExists();
			this.is_send_email = (root || role_admin) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			this.is_send_email = true;

		}
	}
	
	public List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain());
	}
	
	public List<Attribute> getAttributes() {
		return getContentDao().getAttributes(getDomain());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model_wuser!=null)
			model_wuser.detach();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Page getPageV6(IModel<T> model) 	{
		try {
			Page page=(Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page" , model);
			
			if (page instanceof NavigablePage<?>) {
				((NavigablePage<Content>)page).setNavigator(getNavigator(model));
			}
			
			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
				
	}
	
	@Override
 	protected boolean isVisible(Facet facet) {
		Facet realfacet;
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible(getName());
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		return !realfacet.getName().equals("state");
	}

	
	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, ContentConsole.this, null).getString();
	}
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb();
	};
	
	@Override
	protected IModel<T> getModel(T object) {
		return new ObjectModel<T>(object, true);
	}

	@Override
	protected void addModals () {
		super.addModals();
		addOrReplace(new AuditTrailModal<T>("audit-trail-modal"));
		addOrReplace(new ShareModal<T>("send-email-modal"));
		addOrReplace(new LabelsModal<T>("labels-modal"));
	}

	 
	@Override
	protected void addListeners() {
		super.addListeners();
		
		
		add(new WicketEventListener<GridPanelNullObjectEvent<?>>() {
			@Override
			public void onEvent(GridPanelNullObjectEvent<?> event) {
				ServiceLocator.getService( AppMonitoringService.class).attempToFixIndex(getSessionUser());
			}
		});
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list= event.getUserList();
				FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
				if (event.isApply()) {
					getQuery().setParameter("userlist", String.valueOf(list.getObject().getId()));
					//setQuery(new WorkspaceUserListQuery(list.getObject(), getQueryIndex()));
					panel.getParameters().put("userlist", new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName()));
					panel.setParameters(panel.getParameters());
				} else {
					setQuery(newQuery());
				}
				// para el refresh deberia bastar con el set query y el refresh solo en la consola
				getBrowser().setQuery(getQuery());
				panel.setQuery(getQuery());
				getBrowser().refresh(event.getRequestTarget());
				refresh(event.getRequestTarget());
				list.detach();
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
		
		add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Content>>() {
			@Override
			public void onEvent(MyListsUserListItemUpdateObjectEvent<Content> event) {
				FeedbackHelper.showInfoToast(event.getListModel().getObject().getName() + " <br/> " + event.getModel().getObject().getDisplayName());
				ContentConsole.this.refresh(event.getRequestTarget());		
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsUserListItemUpdateObjectEvent;
			}
		});

		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			@Override
			public void onEvent(SidePanelEvent event) {
				// TODO: REPONER CUANDO SE RESUELVA LO DEL NEW HEADER RAUL
				// event.getRequestTarget().add(get("content-header"));
			}
		});


		add(new WicketEventListener< PreviewClickEvent2<T> >() {
			@Override
			public void onEvent(PreviewClickEvent2<T> event) {
				WebPage page = getPortalPreviewPage(event.getModel());
				if (page!=null)
					setResponsePage(page);	
			}
		});
		
		add(new WicketEventListener<ClickEvent<T>>() {
			@Override
			public void onEvent(ClickEvent<T> event) {
				onClickEvent(event);
			}
		});
		

		add(new WicketEventListener<ClickItemEvent<T>>() {
			@Override
			public void onEvent(ClickItemEvent<T> event) {
				onClickEvent(event);
			}
		});


		
	}
	
	
	/**
	 * Overloaded by {@link WorkspaceConsole} and {link MonitorConsole}
	 */
	protected void checkAndMarkAsRead(IModel<T> model) {}
	
	
	protected Component newIcon() {
		return new WebMarkupContainer("icon");
	}
	
	@Override
	protected Panel getPanel(IModel<T> model) {
		return getPanel(model, null);
	}
	
	/**
	 * 
	 * Spring will retrieve:
	 * 
	 * {@link IDocHitExpandedPanel}
	 * {@link TextHitExpandedPanel}
	 * 
	 */
	@Override
	protected Panel getPanel(IModel<T> model, List<String> snippets) {
		String bean = getContentClass(model.getObject())+"-V6panel"; 
		//bean += !eforms ? "-panel" : ;
		ViewMode view_mode = ((DataViewPanel<?>) getBrowser().getPanel(DataViewPanel.class)).getViewMode();
		try {
			Object textparameter = getQuery().getParameters().get("text");
			String query = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (String)textparameter;
			
			Panel panel = (Panel)ServiceLocator.getService(BeansService.class).getBean(bean, model, view_mode, isWorkflowConsole(), query, snippets);
			
			if (panel!=null) 
				return panel;	
			
			logger.error("------------------------------\nNo bean panel " + bean + " | getPanel(IModel<T> model, List<String> snippets)\n------------------------------\n");
			
			if (model.getObject().getClassCode().equals(IDoc.CLASS_CODE)) 
				return (Panel)ServiceLocator.getService(BeansService.class).getBean("kbeeidoc-panel", model, view_mode, isWorkflowConsole(), query, snippets);
							
			else if (model.getObject().getClassCode().equals(OrganizationalText.CLASS_CODE))
				return (Panel)ServiceLocator.getService(BeansService.class).getBean("kbeeorganizationaltext-panel", model, view_mode, isWorkflowConsole(), query, snippets);
			
			return null;
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null")+"Can not resolve Spring bean " + bean);
			return new ErrorPanel("editor", e);
		}
	}
	
	protected Page getPage(IModel<T> model) {
			return getPage(model, null, 0, true);
	}
	
	/**
	 * 
	 * index se ignora ???
	 * 
	 * @param model
	 * @param searcher
	 * @param index
	 * @param read_only
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Page getPage(IModel<T> model, Searcher searcher, long index,  boolean read_only) {
		Page page;
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		 if (workflowService!=null && workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) { 
			page = getTaskPage(model);
		 }
		else {
			try {
				// IDOCPageV6 and TextPageV6
				page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page", model);
				
				
				if (page instanceof NavigablePage<?>) {
					((NavigablePage<Content>)page).setNavigator(getNavigator(model));
				}
				
				
				//, searcher, index
				// logger.debug(page.getClass().getName());
			} catch (Exception e) {
				page=new kbee.web.error.ApplicationErrorPage<>(e);
			}
		}
		return page;
	}

	protected Page getTaskPage(IModel<T> model) {
		return getTaskPage(model, false);
	}

	/**
	 *  
	* <p> The Tasks pages are stand alone. Can't distinguish
	* on a page if the source is the workspace or monitor or pending,
	* so editing will be enabled as long as the workspace
	* of the content is the same as the user, regardless
	* of the origin of the page. </p>
	* 	 * 
	 **/
	@SuppressWarnings("unchecked")
	protected Page getTaskPage(IModel<T> model, boolean select_preference) {
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			Task task = workflowService.getTask();
			
			TaskPage<T> page = (TaskPage<T>)((WebTask)task).getPage(workflowService.getContext());
		
			page.setNavigator(getNavigator(model));
			page.setSource( ContentConsole.this.getName());
			
			// Ver Nota Arriba
			//
			if (model.getObject().getWorkspace()>0) {
				if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
					page.setEditionEnabled(true);
					page.setReadOnly(false);
				}
				else {
					page.setEditionEnabled(false);
					page.setReadOnly(true);
				}
			}
			else {
				page.setEditionEnabled(isEditionEnabled());
				page.setReadOnly(isReadOnly());
			}
			return page;
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
	}
	
	protected String getSubtitleColumn(SearchResult obj) {
		try {
			Content c=(Content) obj.getObject();
			String ty=c.getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
			return ty;
		}
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
	}

	protected Panel getNavigationPanel(IModel<T> model, long index) {
		Panel panel = new ContentNavigationBar<T>("navigation", model, getSearcher(), index);
		return panel;
	}
	
	protected Navigator<Content> getNavigator(IModel<T> model) {
		long index = getIndex(model.getObject());
		if (index<0) index=0;
		Navigator<Content> navigator = new SolrSearcherNavigator<Content>(getSearcher(), index);
		return navigator;
	}

	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	protected void lock(IModel<Content> model) {
		ServiceLocator.getService(ValueLockerService.class).lock(model.getObject().getId());
	}
	
	protected void unlock(IModel<Content> model) {
		ServiceLocator.getService(ValueLockerService.class).unlock(model.getObject().getId());
	}
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	protected WebTask getTask(IModel<Content> model) {
		return (WebTask)model.getObject().getService(WorkflowService.class).getTask();
	}
	
	protected IModel<WorkflowContext> getWorkflowModel(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<T>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	


	protected WebPage getPortalPreviewPage(IModel<T> model) {
		WebPage page = null;
		if (model.getObject().getContentTemplate().isVideo() || model.getObject().getContentTemplate().isAudio()) 
			 	page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video", model.getObject());
		else if (model.getObject().getContentTemplate().isImage()) 
				page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video", model.getObject());
		else 
				page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-text" , model.getObject());

		return page;
	}
	
	/**
	 * This method can change the bck color of the whole row
	 * for example for deleted objects.
	 */
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return null;
	}
	

	
	protected void onClickEvent(ClickEvent<T> event) {
		checkAndMarkAsRead(event.getModel());
		Page page = ContentConsole.this.getPage(event.getModel(), getSearcher(), getIndex(event.getModel().getObject()), false);
		if (page!=null)
			setResponsePage(page);
	}
													
	protected void onClickEvent(ClickItemEvent<T> event) {
		checkAndMarkAsRead(event.getModel());
		Page page = ContentConsole.this.getPage(event.getModel(), getSearcher(), getIndex(event.getModel().getObject()), false);
		if (page!=null)
			setResponsePage(page);
	}
	
	protected PropertyDao getPropertyDao() {
		return (PropertyDao) ServiceLocator.getService(BeansService.class).getBean("propertyDao");
	}
	
	protected void createFavListIfNotExists() {
		try {
			KbeeUser user=(KbeeUser) getSessionUser();
			 long total = getPropertyDao().getTotalListConsole(getSessionUser(), getName());
			 if (total==0)
				 ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, getName(), new StringResourceModel("favorites", this, null).getObject());
		}
		catch (Exception e) {
			 logger.error(e);
		 }
	}
	
	protected boolean eforms() {
		return eforms;
	}
	
	public IModel<User> getPendingModelUser() {
		if (model_wuser == null) {
			User user = getDomain().getService(DomainService.class).getWorkflowUser();
			model_wuser = new ObjectModel<User>(user);
		}
		return model_wuser;
		
	}

	
	


	
	
	
	
	
	
	
	
	
	
	/**-----------------------------------------------------
	 *  List ->
	 *   
	 *  Item panel
	 *  Tags panel
	 *  More panel
	 *  extended panel
	 *  
	 *  
	 * 
	 */
	
	@Override
	protected Panel getItemListPanel(IModel<T> model , int index) {

		ListSimpleItemMainPanel<T> ls= new ListSimpleItemMainPanel<T>("item", model, index,false) {

			private static final long serialVersionUID = 1L;
			protected void onClick() {
					fireScanAll(new ClickEvent<T>(null, getModel(), 0));
			}
			
			@Override
			protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
				return  ContentConsole.this.getItemTags(modelObject);
			}
			
			protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
				return  ContentConsole.this.getMoreInfoPanel(modelObject);
			}
			
			protected IModel<String> getItemLabel(IModel<T> modelObject) {
				return  new Model<String>(modelObject.getObject().getDisplayName());
			}

			protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
				return ContentConsole.this.getItemLabelMeta(modelObject);
			}
		};
		return ls;
	}

	
	/**
	 *  Expanded Panel for ListBrowser 
	 *  
	 *  */
	protected Panel getListRowPanel(IModel<T> model)  {
		//, int index, boolean expanded, List<String> snippets) {
		Query query = getQuery();
		Object textfilter = query.getParameters().get("text");
		String textquery = textfilter instanceof Filter ? (String)((Filter)textfilter).getValue() : (textfilter!=null ? textfilter.toString() : null);
		SearcherContentViewPanel<T> panel = new SearcherContentViewPanel<T>("item", model, null, getSearcher(), textquery, 0, false);
		panel.setContext(getName());
		return panel;
	}

	

	
	/**
	 * 
	 * 
	 * @param modelObject
	 * @return
	 */
	protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
		try {

			@SuppressWarnings("unchecked")
			ListPanel<T> panel = (ListPanel<T>) getBrowser().getPanel(ListPanel.class);
			
			if (panel==null) 
				return new InvisiblePanel("more-info-container");
			
			ListDisplayMode mode=panel.getListDisplayMode();
			
			if (mode.isCompact())
				return new InvisiblePanel("more-info-container");
			
			if (modelObject.getObject().getWorkspace()==null || modelObject.getObject().getWorkspace()<1)
				return new InvisiblePanel("more-info-container");
				
			String note = modelObject.getObject().getService(WorkflowService.class).getTaskComment();
				
			if (note==null)
				return new InvisiblePanel("more-info-container");
			
			note=note.replaceAll(TO_ESC,"<br />");
			
		return new LabelPanel("more-info-container", getSnippet(note));
		}  catch (Exception e) {
			logger.error(e);
			return new LabelPanel("more-info-container",  new Model<String>(e.getClass().getSimpleName()));
		}
	}
	
	

	protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
		try {
			Content c=(Content) modelObject.getObject();
			String nr = (String) c.getService(PropertyService.class).getProperty(PropertyService.PROPERTY_HAS_TAGS);
			if (nr==null || nr.equals("0"))
				return new InvisiblePanel("labels");
			return new LabelSetPanel<Content>("labels", new ObjectModel<Content>(c), false, true, false);
		}
		catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("labels", e);
		}
	}
	
	/**
	 * @param modelObject
	 * @return
	 */
	protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
		
		@SuppressWarnings("unchecked")
		ListPanel<T> panel = (ListPanel<T>) getBrowser().getPanel(ListPanel.class);
		
		if (panel==null) 
			return null;
		
		ListDisplayMode mode=panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
		StringBuilder str = new StringBuilder();
		try {
			
			if (modelObject.getObject().getWorkspace()!=null) {
				com.novamens.workflow.Activity ac = modelObject.getObject().getService(WorkflowService.class).getActivity();
				if (ac!=null) {
					String task=modelObject.getObject().getService(WorkflowService.class).getActivity().getTask().getDisplayName();
					str.append(task + " - ");
				}
				else {
					if (isPending(modelObject)) {
						str.append( new StringResourceModel("pending", this, null).getObject() + " - ");						
					}
				}
			}
			
			String ty=modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			
			if (ty!=null &&  ty.length()>0)
				str.append(ty);
			else {
				String ta=modelObject.getObject().getContentTypeClassificationAsString();
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
					str.append(", ");
				}
				String st=modelObject.getObject().getWorkflowStatusClassificationAsString();
				str.append(st);
			}
			

		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
	}
	
	
	protected boolean isEditionEnabled() {
		return false;
	}
	
	protected boolean isReadOnly() {
		return true;
	}
	
	protected boolean isWorkflowConsole() {
		return false;
	}
	
	@Override
	protected boolean isMyListsEnabled() {
		return true;
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return true;
	}
	
	protected Panel getTipPanel() {
		return (Panel) get("tip");
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	protected boolean isDownload() {
		return is_send_email;
	}
	
	protected boolean isSendByEmail() {
		return is_send_email;
	}
	
	protected boolean isPending(IModel<T> model) {
		if (model.getObject().getWorkspace()>0) {
			if (model.getObject().getWorkspace().toString().equals(getPendingModelUser().getObject().getId().toString())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected String getIcon(IModel<T> model) {
		return null;
	}
	
	protected boolean isFolder(IModel<Content> model) {
		return false;
	}
	
	protected boolean isCheckout(IModel<Content> model) {
		if ((model.getObject().isHeadVersion()) && (model.getObject().getVersion()>0))
			return true;
		return false;
	}

	protected boolean isPrivateNotes(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(model.getObject());
	}
	
	protected boolean isAuditReadable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isAuditTrailReadable(model.getObject());
	}
	
	protected boolean isWriteable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected boolean isMonitorable(IModel<Content> model) {
		try {
			return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected boolean isTakeable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(model.getObject());
	}
	
	protected boolean isTerminable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTerminable(model.getObject());
	}
	
	protected boolean isDeleteable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(model.getObject());
	}
	
	protected boolean isDeleteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(content);
	}
	
	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
}

package kbee.web.dashboard;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListItemsPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.logging.ReadEvent;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.content.console.MyTasksContentMenu;
import kbee.web.content.console.WorkspaceConsole;
import kbee.web.content.console.WorkspacePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.idoc.IDocHitExpandedPanelV6;
import kbee.web.panel.AlertPanel;
import kbee.web.workflow.task.TaskPage;

public class DashboardMyTasksWidgetPanel extends DashboardContentWidgetPanel implements PortalViewRender {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardMyTasksWidgetPanel.class.getName());
	
	static final int LIMIT = 60;

	private  int size;
	private  long total;
	private List<IModel<UserList>> m_lists = null;
	
	

	public DashboardMyTasksWidgetPanel(String id) {
		this(id, "mytasks");
	}
	
	public DashboardMyTasksWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setTitle(getLabel("mytasks"));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (m_lists!=null) 
			m_lists.forEach(item -> item.detach());
	}
	
	public List<IModel<UserList>> getLists() {
		if (m_lists!=null)
			return m_lists;
		m_lists = new ArrayList<IModel<UserList>>();
		for (UserList list: ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists("mytasks")) {
				m_lists.add( new ObjectModel<UserList>(list));		
		}
		return m_lists;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
	
		add(new WicketEventListener<ClickEvent<Content>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Content> event) {
				if ((event.getContext()!=null) && event.getContext().equals("mytasks"))
					DashboardMyTasksWidgetPanel.this.onClick(event.getModel(), event.getIndex());
			}
		});
	}

	@Override
	public void onInitialize() {
		
		setViewModeCriteria(getUserPreference("view-list", "comfortable"));
		setSortCriteria(getUserPreference("sort", "title"));
		
		setItems();
		
		List<ITabKB> tabs = new ArrayList<ITabKB>();
		
		try {
			int nTab = getIntUserPreference("mytasks");
			setInitialSelectedTab(nTab);
			for (IModel<UserList> m_list:getLists() ) {		
				tabs.add(new AbstractTabKB(new Model<String>(m_list.getObject().getDisplayName())) {
					private static final long serialVersionUID = 1L;
					final String f_title=m_list.getObject().getDisplayName();
					final IModel<UserList> f_m_list = m_list;
					@Override
					public IModel<String> getTitle() {
						return new Model<String>(f_title);
					}
					@Override
					public Panel getPanel(String panelId) {
						MyListItemsPanel panel=new MyListItemsPanel(panelId, f_m_list, false, "mytasks");
						panel.setTargetBlank(false);
						return  panel;
					}
				});
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		setTabs(tabs);
		
		setHelp(true);
		setEdit(false);
		super.onInitialize();
	}
	
	protected void onSort(AjaxRequestTarget target, String string) {
		setSortCriteria(string);
		setUserPreference("sort", string);
		refresh(target);
	}
	
	
	@Override
	protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
		try {
			
			if (getViewModeCriteria().equals("compact"))
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

	
	

	@Override
	protected  WebMarkupContainer getExpandedPanel(String id, IModel<Content> model) {
		try {
			if (model.getObject()!=null && model.getObject() instanceof IDoc) {
				IDocHitExpandedPanelV6 panel = new IDocHitExpandedPanelV6(id, new ObjectModel<IDoc>( (IDoc) model.getObject()), true);
				return panel;
		}
		else {
			logger.error(model!=null && model.getObject()!=null? model.getObject().getClass().getName()+" -> not IDOC":"null");
			return new ErrorPanel(id, new Model<String>(model!=null && model.getObject()!=null? model.getObject().getClass().getName()+" -> not IDOC":"null"));
		}
		} catch (Exception e) {
			return new ErrorPanel(id, e); 
		}
	}
	
	@Override	
	public IModel<String> getIconCss(IModel<Content> model) {
		try { 
			String nr = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
			if (nr!=null && nr.equals("yes")) {
				return new Model<String>("fa fa-square panel-centered");	
			}
			else {
				return null;
			}
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
			return null;
		}	
	}

	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		setItems();
		addSingleList();
		super.refresh(target);
	}
	
	protected void setItems() {
		
		StringBuilder str = new StringBuilder();
		
		int index = 0;
		List<IModel<Content>> items = new ArrayList<IModel<Content>>();
		try {
			ResultSet tasks = getTasks();
			while (tasks.hasNext() && index++<LIMIT) {
				items.add(new ObjectModel<Content>((Content)tasks.next().getObject()));
			}
			size = items.size();
			total = tasks.size();
		} catch (Exception  e) {
			str.append(e.getClass().getSimpleName() + " " + e.getMessage());
			logger.error(e);
		}
		
		try {
			boolean b_title_sort = getSortCriteria()==null || getSortCriteria().equals("title");
	
			if (b_title_sort) {
				items.sort(new Comparator<IModel<Content>>() {
					@Override
					public int compare(IModel<Content> o1, IModel<Content> o2) {
						try {
							return o1.getObject().getDisplayName().compareToIgnoreCase(o2.getObject().getDisplayName());
						}
						catch (Exception e) {
							return 0;	
						}
					}
				});
			}
			else {
				items.sort(new Comparator<IModel<Content>>() {
					@Override
					public int compare(IModel<Content> o1, IModel<Content> o2) {
						try {							
							boolean after= o1.getObject().getLastModifiedOffsetDateTime().isAfter(o2.getObject().getLastModifiedOffsetDateTime());
							return after ? -1 : 1;
						}
						catch (Exception e) {
							return 0;	
						}
					}
				});
			}
		} catch (Exception  e) {
			logger.error(e);
			if (str.length()>0)
				str.append("<br>");
			str.append(e.getClass().getSimpleName() + " " + e.getMessage());
		}
		

		if (str.length()>0) {
			AlertPanel<Void> pa=new AlertPanel<Void>("base-alert", AlertPanel.DANGER, null, null, new Model<String>(str.toString()));
		    pa.add(new org.apache.wicket.AttributeModifier("style", "margin-top: 15px; float: left;  width: 100%;"));
			setAlertPanel(pa);
		}

		
		setItems(items);
	}
	
	protected ResultSet getTasks() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getMyTasks();
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		WebMarkupContainer  pa = ServiceLocator.getService(InlineHelpWebService.class).getPanel("help", getSessionUser().getLocale(), InlineHelpWebService.HOME_MYTASKS);
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_MYTASKS));
	}
	
	/**
	 * 
	 */
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		StringBuilder str = new StringBuilder();
		try {
			
			String task=modelObject.getObject().getService(WorkflowService.class).getActivity().getTask().getDisplayName();
			str.append("<span>" + task + "</span>");
			 	
			if (task!=null)
				str.append(" - ");
			
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
			
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			
			if (date!=null) {
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(getZid()));
				String tst = getDateTimeService().timeElapsed(zd, ZoneId.of(getZid()), getSessionUserLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(" - "+ tst);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onClick(IModel<Content> model, int index) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		try {
			TaskPage<Content> page = null;
			if (workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) {
				Task task = workflowService.getTask();
				page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
				page.setNavigator(getNavigator(index));
				if (model.getObject().getWorkspace()>0) {
					if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
						checkAndMarkAsRead(model);
						page.setEditionEnabled(true);
						page.setReadOnly(false);
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
				}
				else {
					page.setEditionEnabled(false);
					page.setReadOnly(true);
				}
			}
			if (page==null)
				throw new IllegalArgumentException("page is null");
			
			page.setSource(WorkspaceConsole.NAME);
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	protected void checkAndMarkAsRead(IModel<Content> model) {
		if ((model.getObject().getWorkspace()!=null) && (model.getObject().getWorkspace().equals(getSessionUser().getId()))) {
			String uread = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
			if (uread!=null && uread.equals("yes")) {
				model.getObject().getService(PropertyService.class).removeProperty(PROPERTY_UNREAD);
				try {
					
					long start=System.currentTimeMillis();
					model.getObject().getService(ContentService.class).update(new ReadEvent(model.getObject(), "Task opened"));
					logger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
				} 
				catch (ServiceNotFoundException | ContentMgmtException e) {
					logger.error(e, getSessionUser().getUserName());
				}
			}
		 }
	}
	
	
	@Override
	protected Panel getMenu(IModel<Content> model, final int index) {
		try {
			return new MyTasksContentMenu("menu", model, index);
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs", getLabel("no-items"), "fal fa-coffee");	
	}

	@Override
	protected void onClickAll() {
		setResponsePage(new WorkspacePage());
	}
	
	@Override
	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable") ?"cozy" : "standard");
	}
	
	@Override
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}
	
	@Override
	protected boolean isExpandVisible() {
		return true;
	}
	
	@Override
	protected String getBodyStyle() {
		return "min-height: 320px; overflow-y:auto;";  // this overflow-y  to be removed if the panel has more than 1 tab
	}
	
	@Override
	protected IModel<String> getViewingString() {
		return size==total ? 
			getLabel("all-items", String.valueOf(size)) : 
			getLabel("recently-modifieds", String.valueOf(size), getIntegerNumberFormat().format(total));
	}
	
	protected boolean isExpand() {
		return false;
	}
	
	protected IModel<String> getAllString() {
		return getLabel("mytasks");
	}
	
	
	protected String getName() {
		return "home-mytasks";
	}
	
	protected boolean isMenuVisible() {
		return true;
	}
	
	public boolean isViewMode() {
		return true;
	}

	protected boolean isSort() 	{
		return true;
	}

	@Override
	protected void onViewMode(AjaxRequestTarget target, String criteria) {
		setViewModeCriteria(criteria);
		setUserPreference("view-list", getViewModeCriteria());
		refresh(target);
	}
}
package kbee.web.dashboard;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.content.console.PendingTasksConsole;
import kbee.web.content.console.PendingTasksContextMenu;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.idoc.IDocHitExpandedPanelV6;
import kbee.web.workflow.task.TaskPage;

public class DashboardPendingTasksWidgetPanel extends DashboardContentWidgetPanel  implements PortalViewRender {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DashboardMyTasksWidgetPanel.class.getName());
	static final int LIMIT = 30;

	int size = 0;
	long total = 0;
	
	
	private NumberFormat integer_nf = null;
		
	public DashboardPendingTasksWidgetPanel(String id) {
		this(id, "pending");
	}
	
	public DashboardPendingTasksWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);								
		this.integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
		integer_nf.setMinimumFractionDigits(0);
		integer_nf.setMaximumFractionDigits(0);
		integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		setTitle(getLabel("pending"));
	}
	
	protected void onSort(AjaxRequestTarget target, String string) {
		setSortCriteria(string);
		setUserPreference("sort", string);
		refresh(target);
	}
	
	/**
	@Override
	public PopupSettings getPopupSettings() {
		return new PopupSettings(  PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
				PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
				PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
	}
	**/
	
	protected boolean isExpandVisible() {return true;}
	
	@Override
	protected String getBodyStyle() {
		return "min-height: 180px;  overflow-y:auto;"; // this overflow-y  to be removed if the panel has more than 1 tab
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
				IDocHitExpandedPanelV6 panel = new IDocHitExpandedPanelV6(id, new ObjectModel<IDoc>( (IDoc) model.getObject()));
				return panel;
		}
		else {
			return new ErrorPanel(id, new Model<String>("not IDOC") );
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
			logger.error(e);
			return null;
		}	
	}
	
	@Override
	public void onInitialize() {
		
		
		setItems();
		setCollapsed( getUserPreference("expanded", getItems().size()>0 ? "yes" : "no").equals("no"));
		
		setViewModeCriteria(getUserPreference("view-list", "comfortable"));
		
		
		setSortCriteria(getUserPreference("sort", "title"));
		 
		super.onInitialize();
	}
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		setItems();
		addSingleList();
		super.refresh(target);
	}
	
	protected void setItems() {
		int index = 0;
		List<IModel<Content>> items = new ArrayList<IModel<Content>>();
		ResultSet tasks = getTasks();
		
		if (tasks!=null) {
			while (tasks.hasNext() && index++<LIMIT) {
				items.add(new ObjectModel<Content>((Content)tasks.next().getObject()));
			}
			total = tasks.size();
		}
		
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
		
		
		this.size = items.size();
		setItems(items);
		
	}
	
	protected ResultSet getTasks() {
		try {
			KbeeUser us = (KbeeUser) getSessionUser();
			return us.getService(UserDashboardService.class).getPendingTasks();
		} 
		catch (Exception e) 
		{	
			logger.error(e);
			return null;
		}
	}
	
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		StringBuilder str = new StringBuilder();
		try {
			
			String task=modelObject.getObject().getService(WorkflowService.class).getTask().getDisplayName();
			str.append(task);
			if (task!=null)
				str.append(" - ");
			String ty=modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			if (ty!=null &&  ty.length()>0) {
				str.append(ty);
			}
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

	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs", new StringResourceModel("no-items", this,null), "fal fa-coffee");	
	}
	
	
	@Override
	protected IModel<String> getViewingString() {
		if (size==total)
			return new StringResourceModel("all-items", this, null).setParameters(new Object[] {String.valueOf(size)} );
			else
		return new StringResourceModel("recently-modified", this, null).setParameters(new Object[] {String.valueOf(size),  getIntegerNumberFormat().format(total)} );
	}
	
	
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
		
			page.setSource(PendingTasksConsole.NAME);
			
			setResponsePage(page);
		
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	@Override
	protected Panel getMenu(IModel<Content> model, int index) {
		try {
			PendingTasksContextMenu m=new PendingTasksContextMenu("menu", model, index) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void refresh(AjaxRequestTarget target) {
					DashboardPendingTasksWidgetPanel.this.refresh(target);	
				}
			};
			return m;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		WebMarkupContainer  pa = ServiceLocator.getService(InlineHelpWebService.class).getPanel("help", getLocale(), InlineHelpWebService.HOME_PENDING);
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_PENDING));
	}
	
	@Override
	protected void onClickAll() {
		setResponsePage(new PendingTasksPage());
	}
	
	protected IModel<String> getAllString() {
		return getLabel("pending");
	}
	
	protected boolean isIconVisible() {
		return false;
	}
	
	
	
	protected String getName() {
		return "home-pending";
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
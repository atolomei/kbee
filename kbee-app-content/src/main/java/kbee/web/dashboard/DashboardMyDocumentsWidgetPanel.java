package kbee.web.dashboard;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.web.idoc.markup.ContentPageV6;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.logging.ReadEvent;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.content.console.ContentBaseConsole;
import kbee.web.content.console.MyDocumentsPage;
import kbee.web.content.console.MyTasksContentMenu;
import kbee.web.content.console.WorkspaceConsole;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.idoc.IDocHitExpandedPanelV6;
import kbee.web.nav.NavigablePage;
import kbee.web.panel.AlertPanel;
import kbee.web.workflow.task.TaskPage;

public class DashboardMyDocumentsWidgetPanel extends DashboardContentWidgetPanel implements PortalViewRender {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DashboardMyTasksWidgetPanel.class.getName());
	
	static final int LIMIT = 50;

	int size;
	long total;
	

	public DashboardMyDocumentsWidgetPanel(String id) {
		this(id, "mydocuments");
	}
	
	public DashboardMyDocumentsWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setTitle(getLabel("mydocuments"));
	}
	
	public boolean isViewMode() {
		return true;
	}
	
	
	
	@Override
	public IModel<String> getIconCssTitle(IModel<Content> model) {
	
		if (model==null)
			return null;
		
		Content content=model.getObject();
		
		if (content==null)
			return null;
		
		
		if (content.getWorkspace()!=null && content.getWorkspace()>0) {
			return getLabel("monitor");
		}
		else {
				return null;
		}
		
	}
	
	
	@Override	
	public IModel<String> getIconCss(IModel<Content> model) {

		
		if (model==null)
			return null;
		
		Content content=model.getObject();
		
		if (content==null)
			return null;
		
		
		if (content.getWorkspace()!=null && content.getWorkspace()>0) {
			
			// TODO ICON
			return new Model<String>("fal fa-pen-to-square panel-centered");
		}
		else {
				return null;
		}
		
				
		//String nr = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
		//if (nr!=null && nr.equals("yes")) {
		//	return new Model<String>("fa fa-square panel-centered");	
		//}
		//else {
		//	return null;
		//}
	}

	@Override
	public void onInitialize() {
		setViewModeCriteria(getUserPreference("view-list", "comfortable"));
		setSortCriteria(getUserPreference("sort", "title"));
		setItems();
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
		}  
		catch (Exception e) {
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
				logger.error(model!=null && model.getObject()!=null? model.getObject().getClass().getName()+" -> not IDOC":"null");
				return new ErrorPanel(id, new Model<String>(model!=null && model.getObject()!=null? model.getObject().getClass().getName()+" -> not IDOC":"null"));
			}
		} 
		catch (Exception e) {
			return new ErrorPanel(id, e); 
		}
	}

	protected IModel<String> getItemLabelMeta(IModel<Content> model) {
		StringBuilder str = new StringBuilder();
		try {
			if (model.getObject().getService(WorkflowService.class).getActivity()!=null)
				return getTaskMeta(model);
			else
				return getContentMeta(model);
		} 
		catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
	}
	
	
	protected IModel<String> getTaskMeta(IModel<Content> modelObject) {
	
		StringBuilder str = new StringBuilder();
		try {
			

			//str.append("<span>" + modelObject.getObject().getService(WorkflowService.class).getActivity().getProcess().getProcedure().getDisplayName() +" - " + "</span>");
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
	
	protected IModel<String> getContentMeta(IModel<Content> modelObject) {
		StringBuilder str = new StringBuilder();
		try {
			
			String ty=modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			
			if (ty!=null &&  ty.length()>0) {
				str.append(ty);
			}
			else {
				String ta=modelObject.getObject().getContentTypeClassificationAsString();
				
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
				}
				
				
				String st=modelObject.getObject().getWorkflowStatusClassificationAsString();
				
				if (st!=null &&  st.length()>0) {
					if (ta!=null && ta.length()>0)
						str.append(", ");
					str.append(st);
				}
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
	protected WebMarkupContainer getHelpPanel() {
		WebMarkupContainer  pa = ServiceLocator.getService(InlineHelpWebService.class).getPanel("help", getSessionUser().getLocale(), InlineHelpWebService.HOME_MYTASKS);
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_MYTASKS));
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
			ResultSet tasks = getDocuments();
			while (tasks.hasNext() && index++<LIMIT) {
				items.add(new ObjectModel<Content>((Content)tasks.next().getObject()));
			}
			size = items.size();
			total = tasks.size();
		} 
		catch (Exception  e) {
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
		} 
		catch (Exception  e) {
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
	
	protected ResultSet getDocuments() {
		KbeeUser us = (KbeeUser) getSessionUser();
		return us.getService(UserDashboardService.class).getMyDocuments();
	}
	
	@Override
	protected void onClick(IModel<Content> model, int index) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		Page page;
		if (workflowService.getTask()!=null)
			page = getTaskPage(model, index);
		else
			page = getLibraryPage(model, index);
		setResponsePage(page);
	}
	
	@SuppressWarnings("unchecked")
	protected Page getTaskPage(IModel<Content> model, int index) {
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
			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			return new ApplicationErrorPage<>(e);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Page getLibraryPage(IModel<Content> model, int index) 	{
		try {
			List<IModel<Content>> mi= new ArrayList<IModel<Content>>();
			getItems().forEach(item ->	 {  mi.add(new ObjectModel<Content>((Content) item.getObject()));	 });
			Page page=(Page) ServiceLocator.getService(BeansService.class).getBean( getContentClass(model.getObject()) + "-page" , model);
			if (page instanceof NavigablePage<?>)
				((NavigablePage<Content>)page).setNavigator(getNavigator(index));
			((ContentPageV6) page).setSource(ContentBaseConsole.NAME);

			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
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
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			if (workflowService.getTask()!=null) {
				return new MyTasksContentMenu("menu", model, index);
			}	
			else {
				return getLibraryMenu(model, index);
			}
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	@SuppressWarnings("serial")
	protected Panel getLibraryMenu(IModel<Content> model, final int index) {
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);

		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Content>(id) {
				@Override 
				public String getLabel() {
					return getLabelString("open");
				}
				@Override
				public void onClick(AjaxRequestTarget target) throws Exception {
					try {
						DashboardMyDocumentsWidgetPanel.this.onClick(getModel(), index);				
					} 
					catch (Exception e) {
						setResponsePage(new ApplicationErrorPage<>(e));
						logger.error(e);	
					}
				}
		});
			
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Content>(id) {
				public void onClick(AjaxRequestTarget target) {
					fire(new ShareContentEvent<Content>(target, getModel()));
				}
				@Override 
				public String getLabel() {
					return getLabelString("share");
				}
				@Override 
				public boolean isEnabled() {
					return isSendByEmail();
				}
		});
			
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Content>(id) {
				public void onClick(AjaxRequestTarget target) {
					fire(new AuditTrailContentEvent<Content>(target, getModel()));
				}
				@Override 
				public String getLabel() {				
					return getLabelString("audit");
				}
		});
	 
		return menu;
	}
	
	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs", getLabel("no-items"), "fal fa-coffee");	
	}

	@Override
	protected void onClickAll() {
		setResponsePage(new MyDocumentsPage());
	}
	
	@Override
	protected void onViewMode(AjaxRequestTarget target, String criteria) {
		setViewModeCriteria(criteria);
		setUserPreference("view-list", getViewModeCriteria());
		refresh(target);
	}

	@Override
	protected IModel<String> getViewingString() {
		return size==total ? 
			getLabel("all-items", String.valueOf(size)) : 
			getLabel("recently-modifieds", String.valueOf(size), getIntegerNumberFormat().format(total));
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
	protected String getBodyStyle() {
		return "min-height: 320px; overflow-y:auto;";  // this overflow-y  to be removed if the panel has more than 1 tab
	}
	
	protected boolean isExpand() {
		return false;
	}
	
	protected IModel<String> getAllString() {
		return getLabel("mydocuments");
	}
	
	protected String getName() {
		return "home-mydocuments";
	}
	
	protected boolean isMenuVisible() {
		return true;
	}
	
	@Override
	protected boolean isExpandVisible() {
		return true;
	}

	protected boolean isSort() 	{
		return true;
	}
}
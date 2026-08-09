package kbee.web.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.Notification;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.ProxyModel;
import com.novamens.workflow.Task;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.workflow.task.TaskPage;

/**
 * ---------------------
 * ContentPublish	 
 * ---------------------
 * Content Title
 * Metadata
 * Published by 
 * ---------------------
 */
@SuppressWarnings("serial")
public class UserNotificationsPanel extends ConsoleSidePanel {

	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger =kbee.util.logging.Logger.getLogger(UserNotificationsPanel.class.getName());

	private List<Notification> notifications_list = null;

	int max_to_fetch = 100;
	
	
	public UserNotificationsPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<User>("mark-all", new ObjectModel<User>(getSessionUser())) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					for (Notification no: getNotifications()) {
						ServiceLocator.getService(NotificationService.class).markAsRead(no);
					}
					target.add(UserNotificationsPanel.this);
				} 
				catch (Exception e) {
					logger.error(e);
				}
			}
		});
		
		add(new ListView<Notification>("item", new ListModel<Notification>(new Model<Panel>(this), "notifications")) {
			@Override
			protected void populateItem(ListItem<Notification> item) {
				NotificationPanel panel = new NotificationPanel("user-notification-panel", item.getModel()) {
					@Override
					protected void onTitleClick(IModel<Notification> model) {
						UserNotificationsPanel.this.onTitleClick(model);
					}
					protected void onAfterDelete(AjaxRequestTarget target) {
						UserNotificationsPanel.this.detach();
						reload(target);
						fire(new NotificationDelete(target));
					}
				};
				item.add(panel);
			}
		});
	}

	public int getListSize() {
		return getNotifications().size();
	}
	public UserNotificationsPanel() {
		this("user-notifications");
	}
	
	
	
	public void onClose(AjaxRequestTarget target) {}

	/**
	 * @return
	 */
	public List<Notification> getNotifications() {
		try {
			if (notifications_list!=null)
				return notifications_list;
			
			this.notifications_list= getContentDao().getAlertNotifications(getSessionUser());
			
		} catch (ContentMgmtException e) {
			logger.error(e.getMessage());
			return new ArrayList<Notification>();
		}
		
		catch (javax.persistence.PersistenceException  e2 ) {
			logger.error(e2);
			ServiceLocator.getService(NotificationService.class).evict();
			notifications_list = new ArrayList<Notification>();
		}
		catch (Exception e1) {
			logger.error(e1);
			this.notifications_list = new ArrayList<Notification>();
		}
		
		return this.notifications_list;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.notifications_list=null;
	}

	/** 
	 * The confirmation Dialog is provided by the ApplicationPage
	 */
	protected ConfirmationDialog getConfirmationDialog() {
		return null;
	}

	
	protected void onTitleClick(IModel<Notification> model) {
		if (model.getObject() instanceof ContentNotification) {
			Content content = ((ContentNotification) model.getObject()).getContent();
			if (content !=null && content.getState()==ObjectState.ENABLED)
				setResponsePage(getContentPage(content));
			else
				setResponsePage(new ApplicationErrorPage<Content>( new Model<String>(model.getObject().getTitle() + " " + "not-in-library"), new Model<String>("")));
		}
		else
			logger.debug("not (model.getObject() instanceof ContentPublishNotification)");
	}
	
	protected Page getContentPage(Content content) {
		Page page;
		WorkflowService workflowService = content.getService(WorkflowService.class);
		
		IModel<Content> model = new ProxyModel<Content>(content);
		
		if (workflowService!=null && workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) { 
			page = getTaskPage(model);
		}
		else {
			page = (Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page", model);
		}
		
		return page;
	}
	
	protected Page getTaskPage(IModel<Content> model) {
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			Task task = workflowService.getTask();
			TaskPage<?> page = (TaskPage<?>)((WebTask)task).getPage(workflowService.getContext());
	
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
			
			//page.setTopNavigation(getNavigationPanel(model));
			
			return page;
		} 
		catch (RuntimeException e) {
			return null;
		}
	}


	
	/**
 
	protected Panel getNavigationPanel(IModel<Content> model) {
		IModel<WorkflowContext> workflowmodel  =  getWorkflowModel(model);
		Panel panel = new MonitorNavigationBar<Content>(workflowmodel) {
			@Override
			public void onNavigate(Content content) {
//				IModel<Content> model = getModel(content);
//				IModel<WorkflowContext> workflowmodel = MonitorConsole.this.getWorkflowModel(model);
//				if (workflowmodel!=null) {
//					setWorkflowModel(workflowmodel);
//					boolean select_preference = true;
//					Page page = MonitorConsole.this.getPage(model, select_preference, false);
//					((AbstractApplicationPage<Content>)page).setNavigation(this);
//					setResponsePage(page);
//				}
//				else {
//					setResponsePage(MonitorConsole.this.getPage(model, getIndex()));
//				}
			} 
//			@Override
//			public void onReturn() {
//				setResponsePage(getConsolePage(getQuery()));
//			}
//			
//			@Override
//			public void onDetach() {
//				super.onDetach();
//				MonitorConsole.this.onDetach();
//			}
		};
		return panel;
	}

	
	private IModel<WorkflowContext> getWorkflowModel(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<Content>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	*/
	
	private String getContentClass(Content content) {
		content = (Content)getContentDao().reload(content);
		return Proxy.getClassName(content).toLowerCase();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}

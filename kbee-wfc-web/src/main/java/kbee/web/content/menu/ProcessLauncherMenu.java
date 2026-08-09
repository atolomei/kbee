package kbee.web.content.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class ProcessLauncherMenu<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ProcessLauncherMenu.class.getName());

	protected final boolean root  = ServiceLocator.getService(SecurityService.class).isRoot();
	
	private IModel<T> model;
	
	public ProcessLauncherMenu(IModel<T> model) {
		super("menu");
		this.model = model;
	}
	
	public List<MenuItemFactory<T>> getItems() {
		int index = 0;
		List<MenuItemFactory<T>> items = new ArrayList<>();
		final int launchers =  getLaunchers(model).size();
		for (int process_launcher=0;  process_launcher<launchers; process_launcher++) {
			final int p_i= index++;
			MenuItemFactory<T> item = new MenuItemFactory<T>() {
				@Override
				public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new MenuItemPanelV5<T>(id) {
						public void onClick() {
							try {
								Content content = (Content)getModel().getObject();
								if (!content.isLocked()) {
									Procedure procedure = null;
									procedure = getLaunchers(model).get(p_i).getProcedure();
									com.novamens.workflow.Process process = content
											.getService(WorkflowService.class)
											.startProcess(procedure);
									Content newcontent = ((KbeeContext)process.getContext()).getContent();
									IModel<Content> sourcemodel = new ObjectModel<>(content);
									IModel<Content> model = new ObjectModel<>(newcontent);
									model.detach();
									Page page = getTaskPage(sourcemodel, model);
									setResponsePage(page);
								}
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage( new ApplicationErrorPage<>(e));
							}
						}
						public String getTarget() {
							return "_blank";
						}
						public String getLabel() {
							return getLabelString("contextmenu.checkout") + " - " + getLaunchers(model).get(p_i).getDisplayName();
						}
						@Override
						public boolean isVisible() {
							if (!isWriteable(getModel()))
								return false;
							if (!getModel().getObject().isHeadVersion()) {
								return false;
							}
							if  (getDomain().getService(WorkflowDomainService.class)!=null      	&& 
								getLaunchers(model).size()>0 										&& 
								getLaunchers(model).get(p_i).executeable())
									return true;
							return false;
						}
						@Override
						public boolean isEnabled() {
							if (isSupportUser() && !isRoot())
								return false;
							return !((Content)getModel().getObject()).isLocked();
						}
					};
				}
			};
			if (isWriteable(model)) {
				items.add(item);
			}
		}
		return items;
	}
	
	
	protected Page getTaskPage(IModel<Content> sourcemodel, IModel<Content> model) {
		return getTaskPage(sourcemodel, model, false);
	}
	
	@SuppressWarnings("unchecked")
	protected Page getTaskPage(IModel<Content> sourcemodel, IModel<Content> model, boolean select_preference) {
		try {
			WorkflowService workflowService = model
				.getObject()
				.getService(WorkflowService.class);
			Task task = workflowService.getTask();
			
			TaskPage<Content> page = (TaskPage<Content>)((WebTask)task)
				.getPage(workflowService.getContext());
		
//			page.setNavigator(getNavigator(sourcemodel));
//			page.setSource(getSource());
			
			if (model.getObject().getWorkspace()>0 && 
					getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
				page.setEditionEnabled(true);
				page.setReadOnly(false);
			}
			else {
				page.setEditionEnabled(false);
				page.setReadOnly(true);
			}
			return page;
		} 
		catch (Exception e) {
			logger.error(e);
			return new ApplicationErrorPage<Void>(e);
		}
	}
	
//	protected Navigator<Content> getNavigator(IModel<Content> model) {
//		return null;
//	}
//	
//	protected String getSource() {
//		return null;
//	}
	
	private List<ProcessLauncher> getLaunchers( IModel<T> model) {
		if (getDomain()==null)
			return  new ArrayList<>();
		return getDomain().getService(WorkflowDomainService.class)==null 
			? new ArrayList<>() 
			: getDomain()
				.getService(WorkflowDomainService.class)
				.getContextLaunchers(model.getObject());
	}
	
	private boolean isSupportUser() {
		return ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	private boolean isWriteable(IModel<T> model) {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isWriteable((Content)model.getObject());
	}
	
	private boolean isRoot() {
		return this.root;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}

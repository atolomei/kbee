package com.novamens.content.web.nav.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

import com.novamens.workflow.Task;

import kbee.web.content.panel.ShareModal;
import kbee.web.object.AuditTrailModal;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class NavBarContentMenu<T extends Content> extends ModelPanel<T> {
	
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(NavBarContentMenu.class.getName());

	private Boolean is_readonly;
	

	public NavBarContentMenu(String id) {
		super(id);
		
 
		ContextMenuPanel<T> menu = new ContextMenuPanel<T>(getModel()) {
			public IModel<T> getModel() {
				return NavBarContentMenu.this.getModel();
			}
		};
		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					public void onClick(AjaxRequestTarget target) {
						startWorkflow();
					}
					@Override 
					public String getLabel() {
						return NavBarContentMenu.this.getStringLabel("contentbase.tools.startworkflow");
					}
					
					@Override 
					public boolean isEnabled() {
						return true;
					}
					
					@Override 
					public boolean isVisible() {

						if (isReadOnly())
							return false;
						
						return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getModelObject()) &&
							!getModelObject().isLocked() 		&&
							getModelObject().isHeadVersion() 	&&
							!getModelObject().isRecycled() 		&&
							!getModelObject().isArchived();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					@Override
					
					public void onClick(AjaxRequestTarget target) {
						try {
							ProcessLauncher launcher = getLaunchers().get(0);
							Content content = getModel().getObject().getService(ContentService.class).checkout();
							content.getService(WorkflowService.class).startProcess(launcher.getProcedure());
							target.add(NavBarContentMenu.this.getPage());
						} 
						catch (RuntimeException e) {
							logger.error(e.getClass().getName() + "Checkout in ContentBaseconsole contextual menu" );
						}
						
						
					}
					@Override
					public String getLabel() {	
						return NavBarContentMenu.this.getStringLabel("contentbase.tools.checkout");
					}
					
					@Override 
					public boolean isEnabled() {
					
						if (isReadOnly())
							return false;
						
						
						return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getModelObject()) &&
							!getModelObject().isLocked() 		&&
							getModelObject().isHeadVersion() 	&&
							!getModelObject().isRecycled() 		&&
							!getModelObject().isArchived();
					}
					
					public List<ProcessLauncher> getLaunchers() {
						return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
							getDomain().getService(WorkflowDomainService.class).getContextLaunchers(getModelObject());
					}
				};	
			}	
		});

		add(menu);
		
		addOrReplace(new AuditTrailModal<T>("audit-modal"));
		addOrReplace(new ShareModal<T>("send-email-modal"));
	}

	

	
	public void setReadOnly(boolean b) {
		this.is_readonly = Boolean.valueOf(b);
	}
	
	
	public boolean isReadOnly() {

		if (this.is_readonly != null)
			return this.is_readonly.booleanValue();
		
		// if Content is External and External Cabinet is ReadOnly 
//		if (getModelObject().isExternal()) {
//			List<Library> cabinets = getContentDao().getLibraries();
//			for (Library c:cabinets) {
//				if (c.getKey().equals(Library.EXTERNAL) && c.isReadOnly()) {
//					this.is_readonly = Boolean.valueOf(true);
//					return this.is_readonly; 
//				}
//			}
//		}
		
		this.is_readonly = Boolean.valueOf(false);
		return this.is_readonly;
	}
	
	
	@SuppressWarnings("unchecked")
	protected void startWorkflow() {
		List<ProcessLauncher>	launchers = getDomain().getService(WorkflowDomainService.class).getContextLaunchers(getModelObject());

		if (launchers.isEmpty() || getModelObject().isLocked()) 
			return;
		
		ProcessLauncher launcher = launchers.get(0);
		
		T newcontent = (T)getModelObject().getService(ContentService.class).checkout();
		
		WorkflowService workflowService = newcontent.getService(WorkflowService.class);
		
		workflowService.startProcess(launcher.getProcedure());
		
		Task task = workflowService.getTask();
		
		TaskPage<?> page =  (TaskPage<?>)((WebTask)task).getPage(workflowService.getContext());
		
		page.setEditionEnabled(true);

		page.setTopNavigation(new TaskNavigationBar<T>(new WorkflowContextModel<T>(workflowService.getContext())));
		
		setResponsePage(page);
	}
	
	protected String getStringLabel(String resourceKey) {
		return ((new StringResourceModel(resourceKey, this, null)).getString());
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}

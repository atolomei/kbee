package com.novamens.content.web.workflow.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.lock.ValueLockerService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowException;

import kbee.web.util.NavigationEvent;
import kbee.web.workflow.task.TaskPage;

@Deprecated
@SuppressWarnings("serial")
public class MonitorActionsPanel<T extends Content> extends ModelPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;
	
	private boolean task, taskstarted;
	
	public MonitorActionsPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
		task = getTask()!=null;
		taskstarted = isTaskStarted();
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("assignation-link")==null) {
			
			
			add(new AjaxLink<Void>("assignation-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					openAssign(target);
				}
				@Override
				public boolean isVisible() {
					return false;
 				}
			});
						
			add(new WorkingIndicatorAjaxLinkV5<Void>("assign-to-me-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					try {
						lock(getContent());
						if (getTask()!=null) {
							User user = getActivityUser();
							if (!user.equals(getUser())) {
								String note = getPanelLabel("taskinfo.message.reassignedfrom").getObject() + 
									user.getFirstLastName() + " " + 
									getPanelLabel("taskinfo.label.by").getObject() + " " + getUser().getFirstLastName();
								getContent().getService(WorkflowService.class).reassign(getUser(), note);
							}
							fire(new NavigationEvent());
						}
						else {
							getErrorDialog().open(target, getPanelLabel("taskinfo.error.notask"));
						}
					}
					finally {
						unlock(getContent());
					}
				}
				@Override
				public boolean isVisible() {
					return false;
				}
				@Override
				protected String getLabel() {
					return getPanelLabel("taskinfo.action.reassigntome").getObject();
				}
				@Override
				protected String getWorkingLabel() {
					return getPanelLabel("taskinfo.action.reassigntome.working").getObject();
				}
			});
				
			add(new WorkingIndicatorAjaxLinkV5<Void>("take-link") {
				@Override
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					boolean lock = true;
					try {
						if (getPage() instanceof TaskPage) {
							lock(getContent());
							if (isTaskStarted()) {
								unlock(getContent());
								lock = false;
								getErrorDialog().open(target, getPanelLabel("taskinfo.error.nolonger"));
							}
							else {
								getContent().getService(WorkflowService.class).startTask();
								((TaskPage<T>)getPage()).setEditionEnabled(true);
								target.add(getPage());
								taskstarted = isTaskStarted();
							}
						}
						else {
							throw new KbeeRuntimeException("This Panel must be used in TaskPage only");
						}
					}
					catch (WorkflowException e) {
						unlock(getContent());
						lock = false;
						getErrorDialog().open(target, new Model<String>(e.getMessage()));
					}
					finally {
						if (lock)
						unlock(getContent());
					}
				}
				@Override
				protected String getLabel() {
					return getPanelLabel("taskinfo.action.take").getObject();
				}
				@Override
				protected String getWorkingLabel() {
					return getPanelLabel("taskinfo.action.take.working").getObject();
				}
				@Override
				public boolean isVisible() {
					return task 
						&& isTakeable(getContent()) 
						&& !taskstarted;
 				}
			});
			add(new AssignationModal<T>());
			add(new ErrorDialog("error-dialog"));
		}
	}
	
	private void lock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).lock(content.getId());
	}
	
	private void unlock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).unlock(content.getId());
	}

	@SuppressWarnings("unchecked")
	private void openAssign(AjaxRequestTarget target) {
		String title = ((KbeeContext)getModelObject()).getContent().getTitle();
		((AssignationModal<T>)get("assignation-modal")).open(target, getModel(), new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				if (button.isSubmit()) {
					fire(new NavigationEvent());
				}
			}
		}, getActivity()!=null ? getActivity().getEnabledGroups() : null, title);	
	}
	
	private WebTask getTask() {
		return (WebTask)getModelObject().getTask();
	}
	
	private Activity getActivity() {
		return getModelObject().getCurrentActivity();
	}
	
	private Content getContent() {
		return ((KbeeContext)getModelObject()).getContent();
	}
	
	private User getActivityUser() {
		return ((KbeeContext)MonitorActionsPanel.this.getModelObject()).getUser();
	}
	
	private boolean isTaskStarted() {
		return MonitorActionsPanel.this.getModelObject().getTime()!=null;
	}
	
	@SuppressWarnings("unused")
	private boolean isMonitorable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(content);
	}

	private boolean isTakeable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content);
	}
	
	@SuppressWarnings("unused")
	private boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	@SuppressWarnings("unused")
	private boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	private IModel<String> getPanelLabel(String key) {
		return (new StringResourceModel(key, MonitorActionsPanel.this, null));
	}
	
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private ErrorDialog getErrorDialog() {
		return (ErrorDialog) get("error-dialog");
	}
	
//	@SuppressWarnings("unused")
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
	
	public IModel<WorkflowContext> getModel() {
		return super.getModel();
	}
	
	public void setModel(IModel<WorkflowContext> model) {
		super.setModel(model);
	}
	
	public WorkflowContext getModelObject() {
		if (getModel()!=null)
			return getModel().getObject();
		return null;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}

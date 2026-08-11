package kbee.web.content.console;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.web.user.markup2.ContentLabelMenuItemFactory;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.workflow.Priority;
import com.novamens.workflow.WorkflowException;

import kbee.web.dashboard.DashboardMonitorTasksWidgetPanel;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.workflow.task.WorkflowPriorityEvent;
import kbee.web.workflow.task.WorkflowPriorityMenuItemFactory;

public class PendingTasksContextMenu extends ContentContextMenu {

	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyTasksContentMenu.class.getName());
	
	int index;

	private boolean is_send_email;

	protected final boolean root		   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	protected boolean isDownload() {
		return is_send_email;
	}
	
	protected boolean isSendByEmail() {
		return is_send_email;
	}
	

	
	
	public  PendingTasksContextMenu (String id, IModel<Content> model, int index) {
		super(id, model,  PendingTasksConsole.NAME);
		
		this.index=index;
	
		this.is_send_email = (root || role_admin) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
		
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		if (getModel()==null || getModel().getObject()==null) {
			return; 
		}
			
		
		addItem(new MenuItemFactory<Content>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					private static final long serialVersionUID = 1L;
					
					//@Override
					//public PopupSettings getPopupSettings() {
					//	PopupSettings popup = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
					//		PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
					//		PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
					//	return popup;
					//}
					
					@Override 
					public String getLabel() {
						return  PendingTasksContextMenu.this.getLabel("open").getObject();
					}
					protected CharSequence getTaskURL() {
						return  PendingTasksContextMenu.this.getPageUrl(getModel());
					}	
					@Override
					protected AbstractLink getNewLink(String id) {
						try {
						ExternalLink link = new ExternalLink(id, getTaskURL().toString());
						link.setPopupSettings(getPopupSettings());
						return link;
						} 
						catch (Exception e) {
							logger.error(e);
							return new ExternalLink(id, "");
						}
						}
				};
			}
		});
		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
							fire(new ShareContentEvent<Content>(target, getModel()));
					}
					@Override 
					public String getLabel() {
						return PendingTasksContextMenu.this.getLabel("share").getObject();
					}
					@Override 
					public boolean isEnabled() {
						return isSendByEmail();
					}
				};
			}
		});

		
		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						getModelObject().getService(ContentSubscriptionService.class).subscribe(getPerson());
						PendingTasksContextMenu.this.refresh(target);
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("subscribe", this, null).getObject();
					}
					@Override
					public boolean isVisible() {
						return !getModelObject().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
						}
				};
			}
		});

		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						getModelObject().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
						PendingTasksContextMenu.this.refresh(target);
					}
					@Override 
					public String getLabel() {
						return new StringResourceModel("unsubscribe", this, null).getObject();
					}
					@Override
					public boolean isVisible() {
						return getModelObject().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
						}
				};
			}
		});
		
		

		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						fire(new AuditTrailContentEvent<Content>(target, getModel()));
					}
					@Override 
					public String getLabel() {				
						return PendingTasksContextMenu.this.getLabel("audit").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		
		
		
		
		/**
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					
					@Override
					public void onClick(AjaxRequestTarget target) {
					
						
						
	
						boolean lock = true;
						try {
							lock(getModel());
							if (isTaskStarted()) {
								unlock(getModel());
								lock = false;
								getErrorDialog().open(target, getConsoleLabel("nolonger"));
							}
							else {
								getModelObject().getService(WorkflowService.class).startTask();
								FeedbackHelper.showSuccessToast( getLabel() + " <br /> " + getModel().getObject().getDisplayName());
								resetSelection();
							}
							refresh(target);
						}
						catch (WorkflowException e) {
							unlock(getModel());
							lock = false;
							getErrorDialog().open(target, new Model<String>(e.getMessage()));
						}
						finally {
							if (lock) unlock(getModel());
						}
	
					}
					@Override 
					public String getLabel() {
						return PendingTasksContextMenu.this.getLabel("take").getObject();
					}
					@Override 
					public String getWorkingLabel() {
						return PendingTasksContextMenu.this.getLabel("working").getObject();
					}
					@Override
					public boolean isVisible() {
						try {
							return isTakeable(getModel());
						} catch (Exception e) {
							logger.error(e);
							return true;
						}
					}
					
					public boolean isTaskStarted() {
						WorkflowService ws = getModelObject().getService(WorkflowService.class);
						if (ws!=null && ws.getContext().getTime()==null)
							return false;
						else
							return true;
					}
				};
			}
		});
*/
		
 
		/**
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						// Modal modal = MyTasksContentMenu.this.getAuditTrailModal();
						// ((AuditTrailModal<Content>)modal).open(target, getModel());
					}
					@Override 
					public String getLabel() {				
						return PendingTasksContextMenu.this.getLabel("audit").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		**/
		
	}

	protected void refresh(AjaxRequestTarget target) {
		
	}
	
}


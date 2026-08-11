package kbee.web.content.console;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.web.user.markup2.ContentLabelMenuItemFactory;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.DeleteContentEvent;
import com.novamens.kbee.wicket.markup.html.event.LabelContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;


@SuppressWarnings("serial")
public class MyTasksContentMenu extends  ContentContextMenu {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyTasksContentMenu.class.getName());
	
	int index;
	private boolean is_send_email;

	protected final boolean root		   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	
	public MyTasksContentMenu(String id, IModel<Content> model, int index) {
		super(id, model, WorkspaceConsole.NAME);
		
		this.index=index;
		
		if (logger.isDebugEnabled()) {
			if (model==null || model.getObject()==null)
				logger.error("error model or object is null");
		}
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		if (getModel()==null || getModel().getObject()==null) {
			return; 
		}

		this.is_send_email = (root || role_admin) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
		
		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					
					
					//@Override
					//public PopupSettings getPopupSettings() {
					//	PopupSettings popup = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
					//		PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
					//		PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
					//	return popup;
					//}
					
					@Override 
					public String getLabel() {
						return  MyTasksContentMenu.this.getLabel("open").getObject();
					}
					protected CharSequence getTaskURL() {
						return  MyTasksContentMenu.this.getPageUrl(getModel());
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
						if (getModel().getObject().getWorkspace().equals(getSessionUser().getId())) {
							fire(new ShareContentEvent<Content>(target, getModel()));
						}
					}
					@Override 
					public String getLabel() {
						return MyTasksContentMenu.this.getLabel("share").getObject();
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
				SubMenuAjaxUserListItemPanel<Content> submenu = new SubMenuAjaxUserListItemPanel<Content>(id, getModel(), WorkspaceConsole.NAME, UserListItem.NEWEST);
				return submenu;
				}
			});

		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {

				SubmenuAjaxItemPanelV5<Content> submenu = new SubmenuAjaxItemPanelV5<Content>(id, getModel()) {
					@Override
					public boolean isVisible() {
						return isWriteable(getModel());
					}
					@Override
					public String getLabel() {
						return  MyTasksContentMenu.this.getLabel("bc.labels").getObject();
					}
					protected void addItems() {
						for (IModel<LabelMember> label: getLabelMembers(getModel().getObject().getContentTemplate()))  {
							addItem(new ContentLabelMenuItemFactory(label, getModel()) {
								@Override
								public void onUpdate(AjaxRequestTarget target) {
									 fire(new LabelContentEvent<Content>(target, getModel()));
								}
							});
						}
					}
				};
				return submenu;
			}
		});
		
		
		
		/**
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				SubmenuAjaxItemPanelV5<Content> submenu = new SubmenuAjaxItemPanelV5<Content>(id, getModel(), "far fa-angle-down") {
					@Override
					public boolean isVisible() {
						return isPrivateNotes( getModel()) || isMonitorable( getModel());
					}
					
					@Override
					public String getLabel() {
						return MyTasksContentMenu.this.getLabel("priority").getObject();
					}
				};
				for (IModel<Priority> label: getPriorities(getModel())) {
						submenu.addItem(new WorkflowPriorityMenuItemFactory<Content>(getModel(), label) {
							@Override
							public void onUpdate(AjaxRequestTarget target) {
								 fire(new WorkflowPriorityEvent(target));
							}
						});
				}
				return submenu;
			}
		});
		**/

		
		

		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						fire(new AuditTrailContentEvent<Content>(target, getModel()));
					}
					@Override 
					public String getLabel() {				
						return MyTasksContentMenu.this.getLabel("audit").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		
		
		
		
		/**
		 * 
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
						@Override 
						public String getLabel() {
								return  MyTasksContentMenu.this.getLabel("download").getObject();
						}
						@Override
						public boolean isDeleteFileAfterDownload()  {
							return true;
						}
						@Override
						protected File getFile() {
							return getModelObject().getService(ContentExportService.class).getHTMLExport();
						}
						
						@Override
						public boolean isEnabled()  {
							try {
								return root || !isSupportUser();
							} catch (Exception e) {
								logger.error(e, getSessionUser().getUserName());
								return false;
							}
						}
						
						@Override
						public boolean isVisible()  {
							try {
								return true;
							} catch (Exception e) {
								logger.error(e, getSessionUser().getUserName());
								return false;
							}
						}
					};
			}
		});
		**/
		
		
		
		
		addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						getModelObject().getService(ContentSubscriptionService.class).subscribe(getPerson());
						FeedbackHelper.showInfoToast("OK");
						target.add( MyTasksContentMenu.this);
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
						FeedbackHelper.showInfoToast("OK");
						target.add( MyTasksContentMenu.this);
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
				return new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						try {
							return true;
							//return getTask(getModel())!=null && (getTask(getModel()).enableCancel() || ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getModelObject()));
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return false;
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
						fire(new DeleteContentEvent<Content>(target, getModel()));
						
						/**
						getConfirmationDialog().open(target,
							getConsoleLabel("workspace.cancelconfirmation.message", getModel().getObject().getTitle(), getTask(getModel()).getName()), 
							Dialog.Delete, 
							new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										getModelObject().getService(WorkflowService.class).cancel();
										refresh(target);
									}
								}
						});
						**/
					}
					@Override 
					public String getLabel() {
						return MyTasksContentMenu.this.getLabel("delete").getObject();
					}
					@Override
					public boolean isVisible() {
						try {
							//return   getTask(getModel())!=null && 
							//		(getTask(getModel()).enableCancel() || ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getModelObject()));
							
							return true;
							
						} catch (Exception e) {
							logger.error(e, getSessionUser().getUserName());
							return false;
						}
					}
				};
			}
		});
	}
	
	
	protected boolean isDownload() {
		return is_send_email;
	}
	
	protected boolean isSendByEmail() {
		return is_send_email;
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
}

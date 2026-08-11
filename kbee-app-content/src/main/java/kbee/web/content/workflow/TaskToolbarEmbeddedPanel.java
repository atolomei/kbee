package kbee.web.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTakeTaskEvent;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignToMeEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.dashboard.LabelPanel;
import kbee.web.eform.EAjaxRefreshEvent;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.CancelWorkflowEvent;
import kbee.web.event.wicket.ErrorEvent;

import kbee.web.idoc.SharedTaskPage;
import kbee.web.model.procedure.EndConditionModel;
import kbee.web.model.procedure.TaskModel;
import kbee.web.workflow.task.ActionEvent;
import kbee.web.workflow.task.TaskErrorEvent;

public class TaskToolbarEmbeddedPanel<T extends Content> extends KBPanel {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskToolbarEmbeddedPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;
	private Editor<T> editor;
	private IModel<Task> taskmodel;
	private List<IModel<ManualEndCondition>> actions = null;
	private IModel<T> model_content;
	private boolean is_send_email;
	private String workflow_useri_id= null;
											
	protected final boolean is_root		   	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean is_admin     	= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	
	public TaskToolbarEmbeddedPanel(IModel<WorkflowContext> model) {
		this("navigation", model);
	}
	
	public TaskToolbarEmbeddedPanel(String id, IModel<WorkflowContext> model) {
		super(id);
		setWorkflowModel(model);
	}
	
	public IModel<T> getContentModel() {
		return model_content;
	}
	
	public Task getTask() {
		return getWorkflowModel()!=null ? getWorkflowModel().getObject().getTask() : null; 
	}
	
	public IModel<Task> getTaskModel() {
		return taskmodel;
	}
	
	@SuppressWarnings("unchecked")
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model_content=new ObjectModel<T>( (T)  ((KbeeContext) model.getObject() ).getContent());
		this.model = model;
		WorkflowContext context = model.getObject();
		taskmodel = new TaskModel(context.getTask());
		//taskmodel = new TaskModel(new ObjectModel<Procedure>(context.getProcedure()), context.getTask());
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	public String getWorkflowUser() {
		try {
			if (this.workflow_useri_id!=null)
				return this.workflow_useri_id;
			User user = getDomain().getService(DomainService.class).getWorkflowUser();
			this.workflow_useri_id = user!=null?String.valueOf(user.getId()):null;
			return this.workflow_useri_id;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		this.is_send_email = (isRoot() || isAdmin()) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
		
		add(deletePanel());
		add(savePanel());
		add(refreshPanel());
		add(actionsPanel());
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.model_content!=null)
			this.model_content.detach();
		if (model!=null)
			model.detach();
	
		if (taskmodel!=null)
			taskmodel.detach();
		
		if (actions!=null) {
			for (IModel<ManualEndCondition> model : getActions()) 
				model.detach();
		}
		
		if ( editor!=null && editor instanceof IDetachable) {
			((IDetachable) editor).detach();
		}
	}
	
	/**
	 * 
	 * VER ALEJO PERMISOS
	 *  
	 * @param iNDEX
	 * @return
	 */
	protected boolean isActionEnabled(int iNDEX) {
		T content = getEditor().getModelObject();
		getEditor().update(content);
		return getActions().get(iNDEX).getObject().isEnabled(content);
	}
	
	
	
	public List<IModel<ManualEndCondition>> getActions() {
		if (actions==null) {
			actions = new ArrayList<IModel<ManualEndCondition>>();
			for (EndCondition action :  ((WebTask) getTask()).getEndConditions()) {
				if (action instanceof ManualEndCondition && ((ManualEndCondition) action).isEnabled()) {
					actions.add(new EndConditionModel<ManualEndCondition>(getTaskModel(), (ManualEndCondition)action));
				}
			};
		}
		return actions;
	}
	

	@SuppressWarnings("serial")
	protected Component actionsPanel() {
		
		if (getTask()==null) {
			LabelPanel lp = new LabelPanel("actions-container", new Model<String>("Task is null"));
			return lp;
		}

		WebMarkupContainer w=new WebMarkupContainer("actions-container");
		ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>("menu", new Model<Panel>(this));
		w.add(menu);
		boolean is_takeable = isTakeable();
		
		// TAKE --------------
		//
		if (is_takeable) {

			menu.addItem(id ->
				new HeaderMenuItemPanelV5<Panel>(id) {
					@Override
					public String getLabel() {
						return getLabelString("menu-pending-task");
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Panel>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							fire(new KbeeTakeTaskEvent(getTask(), target));
						}
						catch(Exception e) {
							logger.error(e);
						}
					}
					@Override
					public String getLabel() {
						return getLabelString("take");
					}
				}						
			);
				
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Panel>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);
		}
		
		
		
		if (getRunningActivity()!=null && getRunningActivity().getUser().equals(getSessionUser())) {
			menu.addItem(id ->
				new HeaderMenuItemPanelV5<Panel>(id) {
					@Override
					public String getLabel() {
						return getLabelString("workflow-actions");
					}
				}
			);

		// WORKFLOW ACTIONS -------------- --------------------------------------------------------------------- --------------
				
		int total = getActions().size();
			for (int index=0; index<total;index++) {
				final int INDEX=index;
				menu.addItem(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new AjaxMenuItemPanelV5<Panel>(id) {
							
							
							@Override
							public boolean isEnabled() {
								return isActionEnabled(INDEX);
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								try {
									fire(new ActionEvent(target, getTask(), getActions().get(INDEX).getObject()));
									target.add(TaskToolbarEmbeddedPanel.this);
								}
								catch(Exception e) {
									logger.error(e);
									fire(new TaskErrorEvent(target, getTask(), getActions().get(INDEX).getObject()));
								}
							}
							
							@Override
							public String getLabel() {
								return getActions().get(INDEX).getObject().getLabel();
							}
						};
					}						
				});
			}

			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new SeparatorMenuItemPanelV5<Panel>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
								return true;
						}
					};
				}
			});
		}

		// WORKFLOW ACTIONS -------------- --------------------------------------------------------------------- --------------
		
		// ------- MONITOR REASSIGN --------------------------------------
			
		   if (!is_takeable) {
			
				menu.addItem(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new MenuItemPanelV5<Panel>(id) {
							private static final long serialVersionUID = 1L;
							
							/**
							 * @throws Exception
							 */
							@Override
							public void onClick() throws Exception {
								try {
									fire(new ReassignToMeEvent<T>(null, getContentModel()));
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
							
							@Override
							public PopupSettings getPopupSettings() {
								//return new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
								//	PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
								//	PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
								return null;
							}
							
							@Override
							public boolean isVisible() {
								try {
									return isMonitorable(getContentModel());
								} 
								catch (Exception e) {
									logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
									return false;
								}
	   						}
							@Override 
							public boolean isEnabled() {
								return  !isPending(getContentModel().getObject()) && 
										!isOwner(getContentModel().getObject()) &&
										isTaskEnabled(getContentModel().getObject());
							}
							@Override 
							public String getLabel() {
								return new StringResourceModel("reassign-to-me", this, null).getObject();
							}
						};
					}
				});

				
		 
				/**
				 * 
				 * Opens the Task in a new Tab
				 * 
				 * @throws Exception
				 */	
			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new AjaxMenuItemPanelV5<Panel>(id) {
						@Override 
						public String getLabel() {
							return new StringResourceModel("reassign", this, null).getObject();
						}
						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								fire(new ReassignEvent<T>(target, getContentModel()));
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<>(e));
							}
						}
						public boolean isEnabled() {
							return !isPending(getContentModel().getObject()) && isTaskEnabled(getContentModel().getObject());
						}
						@Override
						public boolean isVisible() {
							return isMonitorable(getContentModel()) && (getContentModel().getObject().getWorkspace()!=null);
   						}
					};
				}
			});
			
			
			
			
			
			
			
			
			
			
			
			
			
			// ------- SHARE --------------------------------------
			//
			//
			//
			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new SeparatorMenuItemPanelV5<Panel>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
								return true;
						}
					};
				}
			});
		   }
		   
			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new AjaxMenuItemPanelV5<Panel>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							try {
								fire(new ShareContentEvent<T>(target, getContentModel()));
								target.add(TaskToolbarEmbeddedPanel.this);
							}
							catch(Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
							}
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("share", TaskToolbarEmbeddedPanel.this, null).getObject();
			
						}
						
						@Override 
						public boolean isVisible() {
							if ( TaskToolbarEmbeddedPanel.this.getContentModel().getObject().getState()==ObjectState.ENABLED ||
								 TaskToolbarEmbeddedPanel.this.getContentModel().getObject().getState()==ObjectState.ARCHIVED)
							return true;
							return false;
						}
						
						@Override 
						public boolean isEnabled() {
							
							return isSendByEmail();
						}
						
					};
				}						
			});
			

			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new AjaxMenuItemPanelV5<Panel>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							try {
								getContentModel().getObject().getService(ContentSubscriptionService.class).subscribe(getPerson());
								FeedbackHelper.showInfoToast(new StringResourceModel("subscribe", TaskToolbarEmbeddedPanel.this, null).getObject());
								target.add(TaskToolbarEmbeddedPanel.this);
							}
							catch(Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
							}
						}
						@Override
						public boolean isVisible() {
							return !getContentModel().getObject().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
   						}
						
						@Override
						public String getLabel() {
							return new StringResourceModel("subscribe", TaskToolbarEmbeddedPanel.this, null).getObject();
			
						}
					};
				}						
			});


			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new AjaxMenuItemPanelV5<Panel>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							try {
								getContentModel().getObject().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
								FeedbackHelper.showInfoToast(new StringResourceModel("unsubscribe", TaskToolbarEmbeddedPanel.this, null).getObject());
								target.add(TaskToolbarEmbeddedPanel.this);
							}
							catch(Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
							}
						}
						@Override
						public boolean isVisible() {
							return getContentModel().getObject().getService(ContentSubscriptionService.class).isSubscribed(getPerson());
   						}
						
						@Override
						public String getLabel() {
							return new StringResourceModel("unsubscribe", TaskToolbarEmbeddedPanel.this, null).getObject();
			
						}
					};
				}						
			});

			
			
			menu.addItem(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new SeparatorMenuItemPanelV5<Panel>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
								return true;
						}
					};
				}
			});
			
			/**
			 * 
			 * Opens the Task in a new Tab
			 * 
			 * @throws Exception
			 */	
		menu.addItem(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					@Override 
					public String getLabel() {
						return new StringResourceModel("audit", this, null).getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try {
							fire(new AuditTrailContentEvent<T>(target, getContentModel()));
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<>(e));
						}
					}
					public boolean isEnabled() {
						return true;
					}
					@Override
					public boolean isVisible() {
						return isMonitorable(getContentModel()) && (getContentModel().getObject().getWorkspace()!=null);
						}
				};
			}
		});
		

			
			
			
			if (is_root) {
			
				
				menu.addItem(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new MenuItemPanelV5<Panel>(id) {
							public void onClick() {
								try {
										setResponsePage( new SharedTaskPage( getWorkflowModel().getObject()));
										
								} catch (Exception e) {
									logger.error(e);
									setResponsePage( new ApplicationErrorPage<>(e));
								}
							}
							@Override 
							public String getLabel() {
								return "Shared Task Page (root)";
							}
							@Override 
							public String getTarget() {
								return "_blank";
							}
							
							public boolean isVisible() {
								return is_root;
							}
						};
					}
				});
			}
			
			return w;
	}
	
	@SuppressWarnings("serial")
	protected Component deletePanel() {
		return new AjaxLink<Void>("delete-link") {
			public void onClick(AjaxRequestTarget target) {
				fire(new CancelWorkflowEvent(target));
			}
			
			@Override
			public boolean isVisible() {
				return getTask()!=null && ((WebTask)getTask()).isCancelEnabled() && getRunningActivity()!=null && getRunningActivity().getUser().equals(getSessionUser());
			}
		};
	}
	
	protected Component savePanel()  {
		return new AjaxSubmitLink("submit-link") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target) {
 				Editor<T> editor = getEditor();
 				editor.update(target);
 			}
			@Override
			public void onError(AjaxRequestTarget target) {
			}
			@Override
			public boolean isVisible() {
				return getTask()!=null && !isReadOnly() &&  getRunningActivity()!=null && getRunningActivity().getUser().equals(getSessionUser());
			}
			@Override
			public Form<?> getForm() {
				return getEditor().getForm();
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span class=\"far fa-save\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 220);";
						
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
						s += "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw\" style=\"font-size:20px; padding:0; font-weight:300; color:inherit;\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
	}

	
	
	@SuppressWarnings("serial")
	protected Component refreshPanel()  {
		return new AjaxLink<Void>("refresh-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new EAjaxRefreshEvent(target,null));
				fireScanAll(new RefreshClickEvent(target));
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span class=\"far fa-sync\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 220);";
						
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
						s += "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw\" style=\"font-size:20px; padding:0; font-weight:300; color:inherit; \"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
	}	
	
	protected Editor<T> getEditor() {
		if (this.editor==null) {
			this.editor = getEditor(getPage().iterator());
		}
		return this.editor;
	}
	
	@SuppressWarnings("unchecked")
	protected Editor<T> getEditor(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Editor<?>) {
				return (Editor<T>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Editor<T> editor = getEditor(((WebMarkupContainer)component).iterator());
					if (editor!=null) {
						return editor;
					}
				}
			}
		}
		return null;
	}
	
	protected Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected boolean isDownload() {
		return is_send_email;
	}
	
	protected boolean isSendByEmail() {
		return is_send_email;
	}
	
	protected boolean isAdmin() {
		return is_admin;
	}

	protected  boolean isRoot() {
		return is_root;
	}
	
	private boolean isTakeable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(
				((KbeeContext) getWorkflowModel().getObject()).getContent());
	}
	
	private boolean isReadOnly() {
		if (getTask().isReadOnly()) return true;
		for (EForm form : ((WebTask)getTask()).getForms()) {
			if (!((KbeeTaskForm)form).isReadOnly()) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean isMonitorable(IModel<T> model) {
		try {
			return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected boolean isPending(T content) {
		String wuid = getWorkflowUser();
		if (wuid!=null && content.getWorkspace()!=null) {
			return content.getWorkspace().toString().equals(wuid);
		}	
		return false;
	}
	
	protected boolean isOwner(T content) {
		Serializable uid = getSessionUser().getId();
		if (uid!=null && content.getWorkspace()!=null) {
			return content.getWorkspace().toString().equals(String.valueOf(uid));
		}	
		return false;
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isTaskEnabled(Content content) {
		if (is_admin) {
			return true;
		}
		else {
			User user= getSessionUser();
			WorkflowService workflowService = content.getService(WorkflowService.class);
			for (Group group : workflowService.getActivity().getEnabledGroups()) {
				if (user.isMember(group)) {
					return true;
				}
			}
		}
		return false;
	}
}

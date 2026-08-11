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
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.service.DomainService;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.content.user.UserProfile;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListItemsPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.DeleteContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignToMeEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.content.console.MonitorConsole;
import kbee.web.content.console.MonitorPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.help.InlineHelpWebService;
import kbee.web.idoc.IDocHitExpandedPanelV6;
import kbee.web.panel.AlertPanel;
import kbee.web.query.MonitorQuery;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class DashboardMonitorTasksWidgetPanel extends DashboardContentWidgetPanel implements PortalViewRender {
	private static final long serialVersionUID = 1L;
	
	static final int LIMIT = 60;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardMonitorTasksWidgetPanel.class.getName());

	private int size;
	private long total;
	
	private IModel<User> model_wuser;
	private List<IModel<UserList>> m_lists = null;
	
	public DashboardMonitorTasksWidgetPanel(String id) {
		this(id, "monitor");
	}
	
	public DashboardMonitorTasksWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setTitle(getLabel("monitor"));
	}
	
	public boolean isViewMode() {
		return true;
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<Content>>() {
			@Override
			public void onEvent(ClickEvent<Content> event) {
				if ((event.getContext()!=null) && event.getContext().equals("monitor"))
					DashboardMonitorTasksWidgetPanel.this.onClick(event.getModel(), event.getIndex());
			}
		});

		add(new WicketEventListener<ApplySavedQueryEvent>() {
			@Override
			public void onEvent(ApplySavedQueryEvent sevent) {
				if (sevent.getQuery().getConsole().equals(MonitorConsole.KEY)) {
					MonitorQuery q =new MonitorQuery(getQueryIndex());
					q.setParameters(sevent.getQuery().getParameters());
					setResponsePage( new MonitorPage(q));
				}
			}
		});
	}
	
	@Override
	public void onInitialize() {
		
		setViewModeCriteria(getUserPreference("view-list", "comfortable"));
		
		setSortCriteria(getUserPreference("sort", "title"));
		
		setItems();
		
		int nTab = getIntUserPreference("monitor");
		setInitialSelectedTab(nTab);
		
		List<ITabKB> tabs = new ArrayList<ITabKB>();
		
		for (IModel<UserList> m_list:getLists() ) {		
			tabs.add(new AbstractTabKB(new Model<String>(m_list.getObject().getDisplayName())) {
				final String f_title=m_list.getObject().getDisplayName();
				final IModel<UserList> f_m_list = m_list;
				@Override
				public IModel<String> getTitle() {
					return new Model<String>(f_title);
				}
				@Override
				public Panel getPanel(String panelId) {
					MyListItemsPanel panel=new MyListItemsPanel(panelId, f_m_list, false, "monitor");
					panel.setTargetBlank(false);
					return  panel;
				}
			});
		}
		
		final String myq= getLabelString("my-queries");
		final boolean isExportSavedQueries = false;
		tabs.add(new AbstractTabKB(getLabel("my-subcriptions")) {
			@Override
			public Panel getPanel(String panelId) {
				try {
					SubscriptionsPanel panel = new SubscriptionsPanel(panelId) {
						protected void onClick(IModel<ContentSubscription> modelObject, int index) {
							IModel<Content> mod=new ObjectModel<Content>(modelObject.getObject().getContent());
							DashboardMonitorTasksWidgetPanel.this.onClick(mod, index);
						}
					};
					panel.setSortCriteria( getSortCriteria());
					return panel;
				} 
				catch (Exception e) {
					logger.error(e);
					return new ErrorPanel(panelId, e);
				}
			}
		});

		tabs.add(new AbstractTabKB(getLabel("my-queries")) {
			public IModel<String> getTitle() {
				return new Model<String>(myq);
			}
			@Override
			public Panel getPanel(String panelId) {
				return new SavedQueriesPanel(panelId, MonitorConsole.KEY, null, new MonitorQuery(getQueryIndex()), isExportSavedQueries,  false, false);
			}
		});
		
		setTabs(tabs);
		super.onInitialize();
	}
	
	public List<IModel<UserList>> getLists() {
		if (m_lists!=null)
			return m_lists;
		m_lists = new ArrayList<IModel<UserList>>();
		for (UserList list: ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists("monitor")) {
				m_lists .add( new ObjectModel<UserList>(list));		
		}
			
		return m_lists;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model_wuser!=null)
			model_wuser.detach();
		if (m_lists!=null) 
			m_lists.forEach(item -> item.detach());

		
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
	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable")  ? "cozy" : "standard");
	}
	
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}
	
	protected void onSort(AjaxRequestTarget target, String string) {
		setSortCriteria(string);
		setUserPreference("sort", string);
		refresh(target);
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		WebMarkupContainer  pa = ServiceLocator.getService(InlineHelpWebService.class).getPanel("help", getLocale(), InlineHelpWebService.HOME_MONITOR);
		if (pa!=null) 
			return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_MONITOR));
	}
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		setItems();
		addTabsLists();
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		return us.getService(UserDashboardService.class).getMonitoredTasks();
	}
	
	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs", new StringResourceModel("no-items", this,null), "fal fa-coffee");	
	}
	
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		return getItemLabelMetaDefault(modelObject);
	}
	
	protected IModel<String> getItemLabelMetaDefault(IModel<Content> modelObject) {
		StringBuilder str = new StringBuilder();
		
		try {
			
			Long workspace_id = modelObject.getObject().getWorkspace();

			// 
			// Subtitle
			//
			String subtitle=modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			
			if (subtitle==null || subtitle.length()==0) {
				String ta=modelObject.getObject().getContentTypeClassificationAsString();
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
					str.append(", ");
				}
				subtitle=modelObject.getObject().getWorkflowStatusClassificationAsString();
			}
			
			// Task
			//
			String task_name = modelObject.getObject().getService(WorkflowService.class).getTask().getDisplayName();
			String task_workspace  = "";
			
			if (workspace_id!=null) {
				UserProfile up = getContentDao().findUserProfileByUserId(workspace_id);
				if (up!=null) {
					if (model_wuser == null) {
						User user = getDomain().getService(DomainService.class).getWorkflowUser();
						model_wuser = new ObjectModel<User>(user);						 
					}
					
					//if (getPendingModelUser().getObject().getId().equals(workspace_id)) 
						task_workspace = "<span class=\"highlight\">" + up.getPersonFirstLastName()+"</span>";
					//else
					//	task_workspace = "<span>" + up.getPersonFirstLastName()+"</span>";
						
				}
			}
			
			
			// ---
			// Date
			// 
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			String task_date = null;
			if (date!=null) {
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(getZid()));
				task_date = getDateTimeService().timeElapsed(zd, ZoneId.of(getZid()), getSessionUserLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
			}
			else
				task_date = "";
			
			if (getViewModeCriteria().equals("comfortable") ) {
				
				//str.append(separator);
				str.append(task_workspace + " - " + task_name + " - " + subtitle +" - " + task_date);
			}
			else {
			
				str.append(task_workspace + " - " + task_name + " - " + subtitle + " - " + task_date);
				//str.append(separator);
				//str.append(task_date);
			}
			
			
			//if (task!=null)
			//	str.append(" - ");

			
	
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
		
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
		
		if (model.getObject()==null) {
			setResponsePage(new ApplicationErrorPage<>( new Model<String>("Content no longer exists")));
			return;
		}
			
		try {
			
			TaskPage<Content> page = null;
			
			if (    workflowService.getTask()!=null &&
					workflowService.getContext().getProcess().isRunning()) {
				
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
			
			if (page==null) {
				setResponsePage(new ApplicationErrorPage<>( new Model<String>("The Content is no longer executing a business process")));
				model.getObject().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
				return;
			}
			
			page.setSource(MonitorConsole.KEY);
			setResponsePage(page);
			
			
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}

		
	/***
	 * 
	 * 
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<Content> model, final int index) {

			try {
				
				
				if (model==null || model.getObject()==null)
					return new InvisiblePanel("menu");
				
				
				ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
										
				menu.setOutputMarkupId(true);
				
				menu.addItem(new MenuItemFactory<Content>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new AjaxMenuItemPanelV5<Content>(id) {
							@Override 
							public String getLabel() {
								return new StringResourceModel("open", this, null).getObject();
							}
							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									DashboardMonitorTasksWidgetPanel.this.onClick(getModel(), index);				
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
						};
					}
				});
				
				
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new SubMenuAjaxUserListItemPanel<Content>(id, model, "monitor", UserListItem.NEWEST);
					}
				});
				
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new AjaxMenuItemPanelV5<Content>(id) {
							public void onClick(AjaxRequestTarget target) {
									fire(new ShareContentEvent<Content>(target, getModel()));
							}
							@Override 
							public String getLabel() {
								return DashboardMonitorTasksWidgetPanel.this.getLabel("share").getObject();
							}
							@Override 
							public boolean isEnabled() {
								return isSendByEmail();
							}
						};
					}
				});

				
				
				menu.addItem(id ->
					new AjaxMenuItemPanelV5<Content>(id) {
							@Override 
							public String getLabel() {
								return getLabelString("reassign");
							}
							@Override
							public boolean isVisible() {
								return isMonitorable(getModel());
							}
							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									fire(new ReassignEvent<Content>(target, getModel()));
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
				});
				


				menu.addItem(id ->
					new AjaxMenuItemPanelV5<Content>(id) {
							private static final long serialVersionUID = 1L;
							@Override 
							public String getLabel() {
								return  getLabelString("reassign-to-me");
							}
							@Override
							public boolean isVisible() {
								return isMonitorable(getModel());
							}
							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									fire(new ReassignToMeEvent<Content>(target, getModel()));
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
				});
				
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new AjaxMenuItemPanelV5<Content>(id) {
							public void onClick(AjaxRequestTarget target) {
								getModelObject().getService(ContentSubscriptionService.class).subscribe(getPerson());
								FeedbackHelper.showInfoToast(new StringResourceModel("subscribe",  DashboardMonitorTasksWidgetPanel.this, null).getObject());
								refresh(target);
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

				
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new AjaxMenuItemPanelV5<Content>(id) {
							public void onClick(AjaxRequestTarget target) {
								getModelObject().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
								FeedbackHelper.showInfoToast(new StringResourceModel("unsubscribe",  DashboardMonitorTasksWidgetPanel.this, null).getObject());
								refresh(target);
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

				
				
				
				

				
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new AjaxMenuItemPanelV5<Content>(id) {
							
							
							public void onClick(AjaxRequestTarget target) {
								try {
									fire(new AuditTrailContentEvent<Content>(target, getModel()));
								} catch  (Exception e) {
									logger.error(e);
									fire (new ErrorEvent<>(target, e)); 
								}
							}
							@Override 
							public String getLabel() {
								return DashboardMonitorTasksWidgetPanel.this.getLabel("audit").getObject();
							}
							
							@Override
							public boolean isEnabled() {
								return true;
							}
						};
					}
				});
				
				
				
//				menu.addItem(new MenuItemFactory<Content>() {
//					@Override
//					public AbstractMenuItemPanelV5<Content> getItem(String id) {
//						return new AjaxMenuItemPanelV5<Content>(id) {
//							public void onClick(AjaxRequestTarget target) {
//								fire(new DeleteContentEvent<Content>(target, getModel()));
//							}
//							@Override 
//							public String getLabel() {
//								return getLabelString("delete");
//							}
//							@Override
//							public boolean isEnabled() {
//								return  (getModel().getObject().getWorkspace()!=null &&
//									getModel().getObject().getWorkspace().equals(getSessionUser().getId()) );
//							}
//							@Override
//							public boolean isVisible() {
//								try {
//									//return   getTask(getModel())!=null && 
//									//		(getTask(getModel()).enableCancel() || ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getModelObject()));
//								
//									return true;
//									
//								} catch (Exception e) {
//									logger.error(e, getSessionUser().getUserName());
//									return false;
//								}
//							}
//						};
//					}
//				});
				return menu;
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser().getUserName());
				return new InvisiblePanel("menu");
			}
	}

	@Override
	protected void onClickAll() {
		setResponsePage(new MonitorPage());			
	}
	
	
	public IModel<User> getPendingModelUser() {
		if (model_wuser == null) {
			User user = getDomain().getService(DomainService.class).getWorkflowUser();
			model_wuser = new ObjectModel<User>(user);
		}
		return model_wuser;
		
	}
	protected boolean isPending(IModel<Content> model) {
		if (model.getObject().getWorkspace()>0) {
			if (model_wuser == null) {
				User user = getDomain().getService(DomainService.class).getWorkflowUser();
				model_wuser = new ObjectModel<User>(user);
			}
			
			if (model.getObject().getWorkspace().toString().equals( model_wuser.getObject().getId().toString())) {
				return true;
			}
		}
		return false;
	}
	
	@Override	
	public IModel<String> getIconCss(IModel<Content> model) {
		try { 
			if (isPending(model))
				return null;
			
			String nr = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
			
			if (nr!=null && nr.equals("yes")) {
				return new Model<String>("fa fa-square panel-centered");	
			}
			else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
			return null;
		}	
	}
	
	protected boolean isMenuVisible() {
		return true;
	}

	protected String getName() {
		return "home-monitor";
	}
	
	protected IModel<String> getAllString() {
		return getLabel("monitor");
	}

	protected boolean isExpandVisible() {
		return true;
	}

	protected boolean isSort() 	{
		return true;
	}
	
	protected IModel<String> getListTitle() {
		return getLabel("recent-activity");
	}
	
	@Override
	protected void onViewMode(AjaxRequestTarget target, String criteria) {
		setViewModeCriteria(criteria);
		setUserPreference("view-list", getViewModeCriteria());
		refresh(target);
	}

	@Override
	protected String getBodyStyle() {
		return "min-height: 400px;";
	}
	
	@Override
	protected  WebMarkupContainer getExpandedPanel(String id, IModel<Content> model) {
		try {
			if (model.getObject()!=null && model.getObject() instanceof IDoc) {
				IDocHitExpandedPanelV6 panel = new IDocHitExpandedPanelV6(id, new ObjectModel<IDoc>( (IDoc) model.getObject()), true);
				return panel;
			}
			else {
				return new ErrorPanel(id, new Model<String>("not IDOC") );
			}
		} 
		catch (Exception e) {
			return new ErrorPanel(id, e); 
		}
	}
	
	protected boolean isMonitorable(IModel<Content> model) {
		try {
			return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
}







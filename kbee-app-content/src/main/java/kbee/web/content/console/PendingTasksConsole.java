package kbee.web.content.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;

import com.novamens.content.model.Classifier;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.console.markup.TakeTasksButton;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.content.web.nav.markup.TaskNavigationBar;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowException;

import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.TargetBlankTitleColumnPanel;
import kbee.web.console.TitleColumnPanel;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.TaskPriorityColumn;
import kbee.web.console.tools.ExportContentToolButton;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.MonitorBC;
import kbee.web.nav.PendingTasksBC;
import kbee.web.nav.TasksSectionBC;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ExportContentsPage;
import kbee.web.query.PendingTasksQuery;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;

@SuppressWarnings("serial")
public abstract class PendingTasksConsole extends ContentConsole<Content> {
	private static final long serialVersionUID = 1L;								
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PendingTasksConsole.class.getName());
	
	static final public String NAME = "pending";
	
	final boolean role_admin   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_monitor = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
	final boolean role_pending = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	private List<NavigationOrder> orders;
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> selection_toolbar;
	

	public PendingTasksConsole (Query query) {
		super(NAME, query);
	}
	
	
	@Override
	protected String getIcon(IModel<Content> model) {
		if (isCheckout(model))
				return "cell-icon fal fa-lock";
		
		if (isFolder(model))
			return "cell-icon fa-light fa-folder";
			
		return null;
		
	}
	
	protected boolean isCheckout(IModel<Content> model) {
		if ((model.getObject().isHeadVersion()) && (model.getObject().getNextVersion()>0))
			return true;
		return false;
	}

	
	protected boolean isFolder(IModel<Content> model) {
		// TODO AT
		return false;
	}
	
	
	@Override
	public List<NavigationOrder> getOrders() {
		
		if (this.orders!=null) 
			return this.orders;
		 
		this.orders = super.getOrders();
 		
		Collections.sort(orders, new Comparator<NavigationOrder>() {
			public int compare(NavigationOrder order1, NavigationOrder order2) {
				try {
					return order1.getLabel().compareToIgnoreCase(order2.getLabel());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		return orders;
	}

	
	
		@Override
		public void addListeners() {
			super.addListeners();

//			/**
//			 * Apply UserList
//			 */
//			add(new WicketEventListener<MyListsApplyUserListEvent>() {
//				@Override
//				public void onEvent(MyListsApplyUserListEvent event) {
//					IModel<UserList> list= event.getUserList();
//					setQuery(new WorkspaceUserListQuery(list.getObject(), getQueryIndex()));
//					FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
//					panel.getParameters().put("userlist", new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName()));
//					panel.setParameters(panel.getParameters());
//					// para el refresh deberia bastar con el set query y el refresh solo en la consola
//					getBrowser().setQuery(getQuery());
//					panel.setQuery(getQuery());
//					getBrowser().refresh(event.getRequestTarget());
//					refresh(event.getRequestTarget());
//					list.detach();
//				}
//				@Override
//				public boolean handle(com.novamens.event.Event event) {
//					return event instanceof MyListsApplyUserListEvent;
//				}
//			});

			

//			/**
//			 * Add/remove Object to List
//			 */
//
//			add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Content>>() {
//				@Override
//				public void onEvent(MyListsUserListItemUpdateObjectEvent<Content> event) {
//							PendingTasksConsole.this.refresh(event.getRequestTarget());		
//				}
//				@Override
//				public boolean handle(com.novamens.event.Event event) {
//					return event instanceof MyListsUserListItemUpdateObjectEvent;
//				}
//			});
		}
	
		
		
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (GridColumn<?,?> column: getColumns()) {
			column.detach();
		}
		
		if (this.selection_toolbar!=null) {
			for (ToolbarItem item: selection_toolbar) {
				item.detach();
			}
		}
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new PendingTasksQuery(getQueryIndex()));
	}
	
	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new MonitorBC());
	};
	
	@Override
	protected boolean isEditionEnabled() {
		return false;
	}
	
	 
	@Override
	protected Panel getMenu(IModel<Content> model) {
		
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					public void onClick() {
						try {
							if ((getModel().getObject().getWorkspace()!=null && 
								 getModel().getObject().getService(WorkflowService.class).isPending())) 
									setResponsePage(PendingTasksConsole.this.getTaskPage(getModel()));
								else 
									setResponsePage(new ApplicationErrorPage<Content>( new Model<String>(getName()) , new Model<String>("File No longer in Workspace")));
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
							}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("pendingtasks.contextmenu.open").getObject();
					}
					//@Override 
					//public String getTarget() {
					//	return "_blank";
					//}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new SubMenuAjaxUserListItemPanel<Content>(id, model, PendingTasksConsole.this.getName(), UserListItem.NEWEST);
			}
		});

		
		
		
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = PendingTasksConsole.this.getAuditTrailModal();
						((AuditTrailModal<Content>)modal).open(target, getModel());
					}
					@Override 
					public String getLabel() {
						return PendingTasksConsole.this.getLabel("pendingtasks.contextmenu.audit").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						
						if (isSupportUser())
							return true;
						
						if (isWriteable(getModel()))
								return true;
						
						if ( isAuditReadable(getModel()))
							return true;
						
						return false;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Content>() {
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
								getErrorDialog().open(target, getConsoleLabel("pendingtasks.error.nolonger"));
							}
							else {
								getModelObject().getService(WorkflowService.class).startTask();
								FeedbackHelper.showInfoToast( getLabel() + " <br /> " + getModel().getObject().getDisplayName());
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
						return PendingTasksConsole.this.getLabel("pendingtasks.contextmenu.take").getObject();
					}
					@Override 
					public String getWorkingLabel() {
						return getConsoleLabel("pendingtasks.contextmenu.take.working").getObject();
					}
					@Override
					public boolean isVisible() {
						return isTakeable(getModel());
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


		
		/**
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public MenuItemPanel<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						boolean lock = true;
						try {
							lock(getModel());
							if (isTaskStarted()) {
								unlock(getModel());
								lock = false;
								getErrorDialog().open(target, getConsoleLabel("pendingtasks.error.nolonger"));
							}
							else {
								
								getModelObject().getService(WorkflowService.class).startTask();
								AssignationModal<Content> modal = (AssignationModal<Content>) PendingTasksConsole.this.get("assignation-modal");
								String title = getModelObject().getTitle();
								IModel<WorkflowContext> model = getWorkflowModel(getModel());
								Task task = model.getObject().getTask();
								modal.open(target, model, new Modal.Handler() {
									@Override
									public void onClick(AjaxRequestTarget target, Button button) {
										if (button.isSubmit()) {
											refresh(target);
										}
									}
								}, ((KbeeTask)task).getEnabledGroups(), title);
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
						return PendingTasksConsole.this.getLabel("pendingtasks.contextmenu.take-and-reassign").getObject();
					}
					@Override 
					public String getWorkingLabel() {
						return getConsoleLabel("pendingtasks.contextmenu.take.working").getObject();
					}
					@Override
					public boolean isVisible() {
						return isTakeable(getModel());
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

		**/
		
		return menu;
	}
	
	
	 /**
	  * 
	  */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (columns!=null)
			return columns;
				
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();


		{
			
			//LinkPredicateKbeeGridColumn<Content> titleColumn =
			//		new LinkPredicateKbeeGridColumn<Content>("title", getLabel("titlecolumn"), "title_sort",
			//				obj -> obj.getDisplayName(), obj -> getModel(obj));
			// titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			// columns.add(titleColumn);
			
			
			
			
			
			this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {
				
	 			@Override
	 			public String getCssClass()	{
	 				return super.getCssClass() + " mylist";
	 			}
	 			
	 			@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(PendingTasksConsole.this.getName(), (Content) object.getObject());
						if (list==null)
							return new Model<String>("");
						StringBuilder str=new StringBuilder(); 
						for (UserList u:list) {
							if (str.length()>0)
								str.append(", ");
							str.append(u.getTitle());
						}
						return new Model<String>(str.toString());
							
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass().getSimpleName());
					}
				}
				@Override
				protected String getContextKey() {
					return PendingTasksConsole.this.getName() + super.getContextKey();
				}
				
				@Override
				public boolean isPreferred() {
					return false;
				}
		});
			

			this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
				@Override
				public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
					try {
						Object object = resultmodel.getObject().getObject();
						IModel<Content> objectmodel = getModel((Content)object);
						cellItem.add(new TitleColumnPanel<Content>(componentId, objectmodel) {
							@Override
							protected String getCss() {
								return "btn-link";
							}
						});
					} catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						cellItem.add(new Label(componentId, e.getClass().getSimpleName()+ " " + e.getMessage()));
					}
				}
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					Content content = (Content) object.getObject();
					return ()-> content.getTitle();
				}
				@Override
				public String getCssClass() {
					return "col title col-xs-1 col-md-1 col-lg-1";
				}
				@Override
				protected String getContextKey() {
					return PendingTasksConsole.this.getName() + super.getContextKey();
				}
				
				@Override
				public int getDefaultWidth() {
					return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
				}
			});
		}

		{
			KbeePredicateGridColumn<Content> taskColumn = new KbeePredicateGridColumn<>("task", getLabel("taskcolumn"),
					obj ->   getTaskColumnDisplayName(obj)  );
			taskColumn.setContextKey(this.getName() + taskColumn.getContextKey());
			this.columns.add(taskColumn);
		}

		
		
			this.columns.add(new TaskPriorityColumn<Content>("priority", getLabel("taskprioritycolumn"), null) { 
				@Override
				protected String getContextKey() {
					return PendingTasksConsole.this.getName() + super.getContextKey();
				}
			});
		
		

		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return PendingTasksConsole.this.getName() + super.getContextKey();
			}
		});

		{
			KbeePredicateGridColumn<Content> modifiedUserColumn = new KbeePredicateGridColumn<>("modifieduser", getLabel("modifieduser"),
					obj ->   String.valueOf(obj.getLastModifiedUser().getFirstLastName()));
			modifiedUserColumn.setContextKey(this.getName() + modifiedUserColumn.getContextKey());
			modifiedUserColumn.setPreferred(false);
			this.columns.add(modifiedUserColumn);
		}

		
		for (Classifier classifier : getClassifiers()) {
			if (classifier.isVisible(NAME) && classifier.getState()==ObjectState.ENABLED) {
				this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
			}
		}
		
		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				return new Model<String>(String.valueOf(((Content)object.getObject()).getOId()));
			}
			@Override
			protected String getContextKey() {
				return PendingTasksConsole.this.getName() + super.getContextKey();
			}
		});

		/**
		{
			KbeePredicateGridColumn<Content> idColumn = new KbeePredicateGridColumn<>("id", getLabel("idcolumn"),
					obj -> String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}*/

		
		return this.columns;
	}

	protected String getTaskColumnDisplayName(Content col) {
		WorkflowService workflowService = col.getService(WorkflowService.class);
		String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
		return taskname;
	}


	@Override
	protected Panel getNavigationPanel(IModel<Content> model, long index) {
		Panel panel;
  		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null && workflowService.getTask()!=null) {
			IModel<WorkflowContext> workflowmodel  =  getWorkflowModel(model);
			
			panel = new TaskNavigationBar<Content>("navigation", workflowmodel, getSearcher(), index) {
				@Override
				@SuppressWarnings("unchecked")
				public void onNavigate(Content content) {
					IModel<Content> model = getModel(content);
					IModel<WorkflowContext> workflowmodel = PendingTasksConsole.this.getWorkflowModel(model);
			
					if (workflowmodel!=null) {
						Page page = PendingTasksConsole.this.getPage(model);
						((AbstractApplicationPage<Content>)page).setTopNavigation(this);
						setWorkflowModel(workflowmodel);
						setResponsePage(page);
					}
					else {
						setResponsePage(PendingTasksConsole.this.getPage(model));
					}
				} 
//				@Override
//				public void onReturn() {
//					setResponsePage(getConsolePage(getQuery()));
//				}
//				
				/**
				 * This detach requires to detach the MonitorConsole also because this
				 * is an inline class.
				 */
				@Override
				public void onDetach() {
					super.onDetach();
					PendingTasksConsole.this.onDetach();
				}
			};
		}
		else {
			panel = super.getNavigationPanel(model, index);
		}
		
		return panel;
	}
	

	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();

		this.selection_toolbar.add(new TakeTasksButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GenericBatchActionPage page = new GenericBatchActionPage(PendingTasksConsole.this.getBrowser().getSelection()) {
					
					public String getIcon() {
						return "far fa-inbox-in fa-fw";
					}
					
					@Override
					protected IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("pendingtasks.batch.take");
					}
					
					public IModel<String> getTitle() {
						return getConsoleLabel("pendingtasks.batch.take");
					}
					public IModel<String> getType() {
						return getConsoleLabel("pendingtasks.batch.class");
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						String rc = "";
						try {
							lock(model);
							if (!isTaskStarted(model)) {
								model.getObject().getService(WorkflowService.class).startTask();
							}
							else {
								rc = getConsoleLabel("pendingtasks.error.nolonger").getObject(); 
							}
						}
						catch (WorkflowException e) {
							logger.error(e);
							rc = e.getClass().getName();
						}
						finally {
							unlock(model);
						}
						return rc;
					}
					public boolean isTaskStarted(IModel<Content> model) {
						WorkflowService ws = model.getObject().getService(WorkflowService.class);
						if (ws!=null && ws.getContext().getTime()==null)
							return false;
						else
							return true;
					}
				};
				
				
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new TasksSectionBC());
				list.add(new PendingTasksBC());
				list.add(new BCElement(getConsoleLabel("pendingtasks.batch.take")));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}
		});

		
		// Export
		// 
		this.selection_toolbar.add(new ExportContentToolButton<Content>(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			protected void onClick(AjaxRequestTarget target) {
				setResponsePage(new ExportContentsPage(getListModel(), new PendingTasksBC()) {
						@Override
						public void onClose() {
							setResponsePage(new PendingTasksPage());
						}
					});				
					refresh(target);
			}
			
		});
		

		
		return this.selection_toolbar;
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		
		
		return items;
	}
	
	 
	protected boolean isReadOnly() {
		return true;
	}
	
	 
	@Override
	protected boolean isWorkflowConsole() {
		return true;
	}
	
	@Override
	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, PendingTasksConsole.this, null).getString();
	}
	
	
	@Override
	protected boolean hasTopPanel() {
		return true;
	}

	@Override
	protected Panel getTopPanel() {
	try {
			return new AdvancedSearchContentSelectorPanel("top", getName());
		
		} catch (Exception e) {
			logger.error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}

	
}

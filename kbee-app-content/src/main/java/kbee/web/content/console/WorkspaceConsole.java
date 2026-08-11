package kbee.web.content.console;


import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;

import com.novamens.workflow.Priority;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.TitleColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.TaskPriorityColumn;
import kbee.web.console.tools.ExportContentToolButton;
import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.nav.RefreshParentBehavior;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.nav.WorkspaceBC;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ExportContentsPage;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;
import kbee.web.workflow.task.WorkflowPriorityEvent;
import kbee.web.workflow.task.WorkflowPriorityMenuItemFactory;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.console.markup.GlyphiconColumnPanel;
import com.novamens.content.web.console.markup.NewContentButton;
import com.novamens.content.web.content.classify.markup.BatchClassifyPage;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.content.web.nav.markup.TaskNavigationBar;
import com.novamens.content.web.user.markup2.ContentLabelMenuItemFactory;
import com.novamens.content.web.workflow.markup.WorkflowBatchActionsPage;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;

import com.novamens.kbee.content.multidimensional.GroupFacet;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.multidimensional.TaskFacet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ActionsButton;
import com.novamens.kbee.wicket.markup.html.console.browser.DeleteButton;
import com.novamens.kbee.wicket.markup.html.console.browser.EditButton;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.DateColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.UserListsColumn;

import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import com.novamens.logging.ReadEvent;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

/**
 * 
 */
@SuppressWarnings("serial")
public abstract class WorkspaceConsole extends ContentConsole<Content> {

	private static final long serialVersionUID = 1L;
							
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(WorkspaceConsole.class.getName());
	
	final boolean role_pending = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	
	final private static String KEY = "workspace";
	final public static String NAME = "mytasks";
	
	private List<GridColumn<SearchResult,String>> columns = null;
	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_toolbar;
	private Boolean has_launchers = null;
	
	private Map<Long, List<IModel<LabelMember>>> labels = new HashMap<Long, List<IModel<LabelMember>>>();
	
	private List<IModel<Priority>> priorities_model;
	
					
	public WorkspaceConsole(Query query) {
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
	
	
	/**
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<GridPanelNullObjectEvent>() {
			@Override
			public void onEvent(GridPanelNullObjectEvent event) {
				if (isWorkflowConsole()) {
					ServiceLocator.getService( AppMonitoringService.class).attempToFixIndex(getSessionUser());
				}
			}
		});
		
		
		/**
		 * Apply Label
		 */
		
		add(new WicketEventListener<LabelEvent>() {
			@Override
			public void onEvent(LabelEvent event) {
				WorkspaceConsole.this.refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof LabelEvent;
			}
		});

		add(new WicketEventListener<WorkflowPriorityEvent>() {
			@Override
			public void onEvent(WorkflowPriorityEvent event) {
				WorkspaceConsole.this.refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof WorkflowPriorityEvent;
			}
			
		});

	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			getColumns().forEach(item -> item.detach());
			this.columns=null;
			
			if( this.priorities_model!=null) {
				this.priorities_model.forEach(item -> item.detach());
			}
			
			if (this.items!=null) 
				this.items.forEach(item -> item.detach());
			
			if (this.selection_toolbar!=null)
				this.selection_toolbar.forEach(item -> item.detach());
			
			if (this.labels!=null)
				this.labels.forEach((k, v) -> v.forEach(item->item.detach()));
			} catch (Exception e) {
				
				kblogger.error(e);
				
			}
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new kbee.web.query.WorkspaceQuery(getQueryIndex()));
	}
	
	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new WorkspaceBC());
	}
	
	@Override
	protected boolean isEditionEnabled() {
		return true;
	}
	
	/***
	 * 
	 */
	@Override
	protected void checkAndMarkAsRead(IModel<Content> model) {
		if ((model.getObject().getWorkspace()!=null) && (model.getObject().getWorkspace().equals(getSessionUser().getId()))) {
			String uread = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
			if (uread!=null && uread.equals("yes")) {
				model.getObject().getService(PropertyService.class).removeProperty(PROPERTY_UNREAD);
				try {
					
					long start=System.currentTimeMillis();
					model.getObject().getService(ContentService.class).update(new ReadEvent(model.getObject(), "Task opened"));
					kblogger.debug("ContentService.class).update() -> "+ String.valueOf(System.currentTimeMillis()-start)+" ms");
				} 
				catch (ServiceNotFoundException | ContentMgmtException e) {
					kblogger.error(e, getSessionUser().getUserName());
				}
			}
		 }
	}

	
	/**
	 * Contextual Menu for each Element
	 */
	@Override
	protected Panel getMenu(IModel<Content> model) {
		
		try {
			
			ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
									
			menu.setOutputMarkupId(true);
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new MenuItemPanelV5<Content>(id) {
						
						/**
						@Override
						public PopupSettings getPopupSettings() {
							PopupSettings popup = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
								PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
								PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR);
							return popup;
						}
						**/
						@Override 
						public String getLabel() {
							return getConsoleLabel("workspace.contextmenu.open").getObject();
						}
						protected CharSequence getTaskURL() {
							return WorkspaceConsole.this.getPageUrl(getModel());
						}	
						@Override
						protected AbstractLink getNewLink(String id) {
							try {
								return  new ExternalLink(id, getTaskURL().toString());
							} 
							catch (Exception e) {
								kblogger.error(e);
								return new ExternalLink(id, "");
							}
							}
					};
				}
			});
			

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					SubMenuAjaxUserListItemPanel<Content> submenu = new SubMenuAjaxUserListItemPanel<Content>(id, model, WorkspaceConsole.this.getName(), UserListItem.NEWEST);
					return submenu;
					}
				});

			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {

					SubmenuAjaxItemPanelV5<Content> submenu = new SubmenuAjaxItemPanelV5<Content>(id, model) {
						@Override
						public boolean isVisible() {
							return isWriteable(getModel());
						}
						
						@Override
						public String getLabel() {
							return getConsoleLabel("workspace.contextmenu.labels").getObject();
						}
					
						protected void addItems() {
							for (IModel<LabelMember> label: getLabelMembers(getModel().getObject().getContentTemplate()))  {
								addItem(new ContentLabelMenuItemFactory(label, model) {
									@Override
									public void onUpdate(AjaxRequestTarget target) {
										 fire(new LabelEvent(target));
									}
								});
							}
						}
					};
					return submenu;
				}
			});


			
				menu.addItem(new MenuItemFactory<Content>() {
					@Override
					public AbstractMenuItemPanelV5<Content> getItem(String id) {
						SubmenuAjaxItemPanelV5<Content> submenu = new SubmenuAjaxItemPanelV5<Content>(id, model, "far fa-angle-down") {
							@Override
							public boolean isVisible() {
								return isPrivateNotes(model) || isMonitorable(model);
							}
							
							@Override
							public String getLabel() {
								return getConsoleLabel("workspace.contextmenu.priority").getObject();
							}
						};
						for (IModel<Priority> label: getPriorities(model)) {
								submenu.addItem(new WorkflowPriorityMenuItemFactory<Content>(model, label) {
									@Override
									public void onUpdate(AjaxRequestTarget target) {
										 fire(new WorkflowPriorityEvent(target));
									}
								});
						}
						return submenu;
					}
				});
			
			

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						@SuppressWarnings("unchecked")
						public void onClick(AjaxRequestTarget target) {
							Modal modal = WorkspaceConsole.this.getAuditTrailModal();
							((AuditTrailModal<Content>)modal).open(target, getModel());
						}
						@Override 
						public String getLabel() {				
							return WorkspaceConsole.this.getLabel("workspace.contextmenu.audittrail").getObject();
						}
						
						@Override
						public boolean isEnabled() {
							return true;
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						@SuppressWarnings("unchecked")
						public void onClick(AjaxRequestTarget target) {
							if (getModel().getObject().getWorkspace().equals(getSessionUser().getId())) {
								Modal modal = WorkspaceConsole.this.getSendByEmailModal();
								((ShareModal<Content>)modal).open(target, getModel());
							}
						}
						@Override 
						public String getLabel() {
							return WorkspaceConsole.this.getLabel("workspace.contextmenu.share").getObject();
						}
						@Override 
						public boolean isEnabled() {
							return isSendByEmail();
						}
					};
				}
			});
	
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
							@Override 
							public String getLabel() {
									return WorkspaceConsole.this.getLabel("workspace.contextmenu.download").getObject();
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
									return (isRoot() || !isSupportUser());
								} catch (Exception e) {
									kblogger.error(e, getSessionUser().getUserName());
									return false;
								}
							}
							
							@Override
							public boolean isVisible()  {
								try {
									return true;
								} catch (Exception e) {
									kblogger.error(e, getSessionUser().getUserName());
									return false;
								}
							}
						};
				}
			});
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						try {
							return getTask(getModel())!=null && (getTask(getModel()).isCancelEnabled() || ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getModelObject()));
						} catch (Exception e) {
							kblogger.error(e, getSessionUser().getUserName());
							return false;
						}
					}
			});
			
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						if (getModelObject().getWorkspace()==null || !getModelObject().getWorkspace().equals(getSessionUser().getId())) {
							getErrorDialog().open(target, new Model<String>("File No longer in Workspace"));
							refresh(target);
							return;
						}
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
					}
					@Override 
					public String getLabel() {
						return WorkspaceConsole.this.getLabel("workspace.contextmenu.cancel").getObject();
					}
					@Override
					public boolean isVisible() {
						try {
							return   getTask(getModel())!=null && 
									(getTask(getModel()).isCancelEnabled() || ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(getModelObject()));
						} catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName());
							return false;
						}
					}
			});
			
			return menu;
			
		} catch (Exception e) {
			kblogger.error(e, getSessionUser().getUserName()+ " " + ((model!=null && model.getObject()!=null)?model.getObject().toString():"null"));
			return new InvisiblePanel("menu");
		}
	}
	
	/***  
	 * 
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;

		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		this.columns.add(new GridColumn<SearchResult, String>("unread", getLabel("workspace.column.unread")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					Object object = resultmodel.getObject().getObject();
					IModel<Content> objectmodel = getModel((Content)object);
					cellItem.add(new GlyphiconColumnPanel<Content>(componentId, objectmodel) {
						@Override
						public boolean isVisible() {
							try { 
								String nr = (String) getModelObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
								return nr!=null && nr.equals("yes");
							} catch (Exception e) {
								kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
								return false;
							}
						};
						
						@Override
						protected IModel<String> getAnchorTitle() {
							return getConsoleLabel("workspace.unreadtask");
						}
					});
				} 
				catch (Exception e) {
					cellItem.add(new Label(componentId, ""));
				}
			}
			@Override
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content)object.getObject();
				String nr = (String) content.getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
				boolean isRead= nr!=null && nr.equals("yes");
				return ()-> isRead ? "read" : "unread";
			}
			@Override
			public boolean isExportable() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
			@Override
			public int getWidth() {
				return GridPanel.ICON_COL_WIDTH;
			}
			@Override
			public int getXPadding()	{
				return 3;
			}
 			@Override
			public String getCssClass() {
				return "col short col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
			@Override
			public boolean isFixed() {
				return true;
			}
			@Override
			public boolean isResizable() {
				return false;
			}
		});


		GridColumn<SearchResult, String> iconc = new GridColumn<SearchResult, String>("icon", getLabel("workspace.column.icon")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = null;
				try {
					object = resultmodel.getObject().getObject();
				} catch (Exception e) {
					kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
					cellItem.add(new InvisiblePanel(componentId));
					return;
				}
				
				IModel<Content> objectmodel = getModel((Content)object);
				
				cellItem.add(new ImageColumnPanel<Content>(componentId, objectmodel) {
					@SuppressWarnings("unchecked")
					@Override
					protected Image getImage(String id) {
						try {
						Content object = (Content) getModel().getObject();
						if (object instanceof ResourceContainer) {
							ResourceContainer rc = 	(ResourceContainer) object; 
							for (KBFile res: rc.getFiles()) {
								if (res.isImage() || res.isVideo())
									return new ResourceThumbnailImage(id, new ObjectModel<Resource>((Resource) res), ThumbnailSize.MINI);
							}
							if(rc.getFiles().size()>0)
								return new ResourceThumbnailImage(id, new ObjectModel<Resource>((Resource) rc.getFiles().get(0)) , ThumbnailSize.MINI);
							}
						} catch (Exception e) {
							kblogger.error(e, getSessionUser().getUserName());
						}
						return null;
					}
				});
			}

			@Override
			public boolean isExportable() {
				return false;
			}
			

			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
		};
		
		iconc.setPreferred(false);
		this.columns.add(iconc);
				
				
		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("workspace.column.title"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = null;
				try {
					object = resultmodel.getObject().getObject();
				} catch (Exception e) {
					kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
					cellItem.add(new InvisiblePanel(componentId));
					return;
				}
				IModel<Content> objectmodel = getModel((Content)object);
				cellItem.add(new TitleColumnPanel<Content>(componentId, objectmodel) {
					@Override
					protected String getCss() {
						return "cell-label btn-link";
					}
				});
			}
			
			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
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
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
		});
		

 		
			this.columns.add(new GridColumn<SearchResult, String>("task", getLabel("workspace.column.task"), "task_sort") {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						WorkflowService workflowService = ((Content)object.getObject()).getService(WorkflowService.class);
						String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
						return new Model<String>(taskname);
					} catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
						return new Model<String>("err reindex req.");
					}
				}
				@Override
				protected String getContextKey() {
					return WorkspaceConsole.this.getName() + super.getContextKey();
				}
			});
 		
 		
 		
		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("workspace.column.date"), "modified") {
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});
		

		
 		
	 	this.columns.add(new TaskPriorityColumn<Content>("priority", getLabel("workspace.column.priority"), "priority_sort") { 
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
	 	// Content Type
	 	//
		for (Classifier classifier : getClassifiers()) {
			if (classifier.isContentType()) {
			if (classifier.isVisible(KEY) && classifier.getState()==ObjectState.ENABLED) {
					this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
				}
			}
		}

		// The Rest
		//
		for (Classifier classifier : getClassifiers()) {
			if (!classifier.isContentType()) {
				if (classifier.isVisible(KEY) && classifier.getState()==ObjectState.ENABLED) {
					ClassifierColumn<Content> c=new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName());
					this.columns.add(c);
				}
			}
		}
		
		for (Attribute attribute : getAttributes()) {
			if (attribute.getState()==ObjectState.ENABLED  && attribute.isVisible(KEY)) {
				if (attribute.isDate()) {
					this.columns.add(new AttributeDateColumn(new ObjectModel<Attribute>(attribute), getName(), false));
				}
				else {
					this.columns.add(new AttributeColumn(new ObjectModel<Attribute>(attribute), getName(), false));
				}
			}
		}
		
		this.columns.add(new UserListsColumn("mylists", getLabel("mylists")) {
			@Override
			protected String getConsole() {
				return WorkspaceConsole.this.getName();
			}
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("workspace.column.contentclass")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
					return new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName());
					} catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
						return new Model<String>("err reindex req.");
					}
				}
				@Override
				protected String getContextKey() {
					return WorkspaceConsole.this.getName() + super.getContextKey();
				}
				
				@Override
				public boolean isPreferred() {
					return true;
				}
				
		});

		
		this.columns.add(new DateColumn<Content>("duedate", getLabel("duedatecolumn"), "duedate", DateTimeService.MONTH_DAY_YEAR_LABEL) {
				@Override
				protected OffsetDateTime getOffsetDateTime(Content content) {
					try {
						return content.getService(WorkflowService.class).getContext().getDueDate();
					} 
					catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName());
						return  null;
					}
				}
				@Override
				protected String getContextKey() {
					return WorkspaceConsole.this.getName() + super.getContextKey();
				}
				@Override
				public boolean isPreferred() {
					return true;
				}
		});
		
		
		
		
		this.columns.add(new DateColumn<Content>("processstart", getLabel("processstartcolumn"), null) {
			@Override
			protected OffsetDateTime getOffsetDateTime(Content content) {
				try {
					WorkflowService workflowService = content.getService(WorkflowService.class);
					return workflowService.getActivity().getProcess().getStartTime();
				
				} catch (Exception e) {
					kblogger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

					return  null;
				}
			}
			
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("workspace.column.id")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					return new Model<String>(String.valueOf(((Content)object.getObject()).getOId()));
				} 
				 catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName()+  " | probably requires reindexing.");
					return new Model<String>("err reindex req.");
				}
			}
			@Override
			protected String getContextKey() {
				return WorkspaceConsole.this.getName() + super.getContextKey();
			}
		});
		
		return this.columns;
	}


	/**
	 * @param obj
	 * @return
	 */
	protected String getUserLists(Content obj) {
		try {
			List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(WorkspaceConsole.this.getName(), obj);
			if (list==null)
				return "";
			StringBuilder str=new StringBuilder(); 
			for (UserList u:list) {
				if (str.length()>0)
					str.append(", ");
				str.append(u.getTitle());
			}
			return str.toString();
				
		} catch (Exception e) {
			kblogger.error(e, getSessionUser().getUserName());
			return e.getClass().getSimpleName();
		}
		
	}

	/** ------------------------------------
	 * 
	 * 
	 * Bulk Actions (Selection)
	 * 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();

		this.selection_toolbar.add(new EditButton(browser, Align.TOP_NONE, true) {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(AjaxRequestTarget target) {
				getPage().setResponsePage( new BatchClassifyPage(WorkspaceConsole.this.getBrowser().getSelection()) {
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = WorkspaceConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new TaskNavigationBar<Content>(getWorkflowModel(model)));
						return page;
					}
				});
			}
			
			public IModel<String> getLabel() {
				return new StringResourceModel("workspace.batch.classify", WorkspaceConsole.this, null); 
			}
			@Override
			protected String getAnchorTitle() {
				return new StringResourceModel("workspace.batch.classify", WorkspaceConsole.this, null).getObject();
			}
		});
		
		
		// Task Resolution
		//
		this.selection_toolbar.add(new ActionsButton(browser, ToolbarItem.Align.TOP_NONE, true) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getPage().setResponsePage(new WorkflowBatchActionsPage(WorkspaceConsole.this.getBrowser().getSelection()) {
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = WorkspaceConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new TaskNavigationBar<Content>(getWorkflowModel(model)));
						return page;
					}
				});
			}
			@Override
			protected String getAnchorTitle() {
				return new StringResourceModel("workspace.actions", WorkspaceConsole.this, null).getObject();
			}			
		});

		
		// Delete
		// GenericBatchActionPage is a "generic" object actions page.
		this.selection_toolbar.add(new DeleteButton(browser, ToolbarItem.Align.TOP_NONE, true) {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				
				
				GenericBatchActionPage page = new GenericBatchActionPage(WorkspaceConsole.this.getBrowser().getSelection()) {

					public String getIcon() {
						return "far fa-trash-alt";
					}
					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("workspace.batch.delete");
					}
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("workspace.batch.class");
					}
					@Override
					public IModel<String> getReturnLabel() {
						return getConsoleLabel("workspace.batch.delete.return");
					}
					@Override
					public IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("delete");
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						try {
							if (getTask(model)==null) {
								model.getObject().getService(ContentService.class).delete();
								return "";
							}
							else
							if (!getTask(model).isCancelEnabled() && !isMonitorable(model)) {
								return "not enabled";
							}
							else {
								model.getObject().getService(WorkflowService.class).cancel();
								return "";
							}
						} catch (Exception e) {
							kblogger.error(e, getSessionUser().getUserName());
							return e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
						}
					}
					@Override
					protected String getExecuteButtonCss() {
						return "btn btn-sm btn-danger";
					}
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = WorkspaceConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new TaskNavigationBar<Content>(getWorkflowModel(model)));
						return page;
					}
				};
				
			
				MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
	 			bc.addElement(new TasksDropDownMenuBC());
	 			bc.addElement(new BCElement("mytasks"));
	 			bc.addElement(new BCElement(getConsoleLabel("delete")));
	 			page.setBreadCrumbPanel(bc);
				setResponsePage(page);
				
			}
			
			@Override
			protected String getAnchorTitle() {
				return getConsoleLabel("workspace.batch.deleteanchor").getObject();
			}
		});

		// Export
		//
		this.selection_toolbar.add(new ExportContentToolButton<Content>(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			protected void onClick(AjaxRequestTarget target) {
				setResponsePage(new ExportContentsPage(getListModel(), new WorkspaceBC()) {
						@Override
						public void onClose() {
							setResponsePage(new WorkspacePage());
						}
					});				
					refresh(target);
			}
		});
		
		
		/**
		this.selection_toolbar.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				SubMenuAjaxUserListItemPanel<Content> submenu = new SubMenuAjaxUserListItemPanel<Content>(id, model, WorkspaceConsole.this.getName(), UserListItem.NEWEST);
				return submenu;
				}
			});
			**/
		
		
		return this.selection_toolbar;
	}
								
	
	
	
	protected boolean hasLaunchers() {
		if (this.has_launchers!=null) 
			return this.has_launchers.booleanValue();
		for(ProcessLauncher launcher: getDomain().getService(WorkflowDomainService.class).getLaunchers()) {
			if (launcher.isEnabled() && launcher.executeable() && launcher.getContentTemplate().getState()==ObjectState.ENABLED) {
				 this.has_launchers=Boolean.valueOf(true);
				 return this.has_launchers.booleanValue(); 
			}
		}
		this.has_launchers=Boolean.valueOf(false);
		return this.has_launchers.booleanValue();
	}
	
	
 	/** --------------------------------------------------------- 
	 * Browser Toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.items!=null)
			return this.items;

		
			this.items = super.getToolbarItems(browser);
		
		
		/**
		 *  New Button
		**/
		this.items.add(new NewContentButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected void onStart(com.novamens.workflow.Process process) {
				Content newcontent = ((KbeeContext)process.getContext()).getContent();
				setQuery(newQuery());
				IModel<Content> model = getModel(newcontent);
				model.detach();
				setDefaults(model.getObject());
				Page page = WorkspaceConsole.this.getPageV6(model);
				if (page!=null)
					page.add(new RefreshParentBehavior());
				else
					page = new ApplicationErrorPage<Content>(new Model<String>("No Editor Page for " +newcontent.getClass().getName()), new Model<String>(getName()));
				setResponsePage(page);
			}
		});

		// Bulk Upload
		//
		/**
		  this.items.add(new LinkButton(browser, ToolbarItem.Align.TOP_LEFT, getConsoleLabel("batch-create")) {
			@Override
			public void onClick() {
				getPage().setResponsePage(new TaskBatchCreatePage<Content>());
			}
			@Override
			public boolean isVisible() {
				return role_bulk && hasLaunchers();
			}
		 });
		 **/

		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return WorkspaceConsole.this.getName();}, new Model<String>(WorkspaceConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);
		
		return items;
	}
	
	/***
	 * 
	 */
	@Override
	protected Panel getNavigationPanel(IModel<Content> model, long index) {
		Panel panel = null;
  		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null && workflowService.getTask()!=null) {
			IModel<WorkflowContext> workflowmodel  =  getWorkflowModel(model);
			panel = new TaskNavigationBar<Content>("navigation", workflowmodel, getSearcher(), index) {
				@Override
				@SuppressWarnings("unchecked")
				public void onNavigate(Content content) {
					IModel<Content> model = getModel(content);
					IModel<WorkflowContext> workflowmodel = WorkspaceConsole.this.getWorkflowModel(model);
					if (workflowmodel!=null) {
						Page page = WorkspaceConsole.this.getPageV6(model);
						((AbstractApplicationPage<Content>)page).setTopNavigation(this);
						setWorkflowModel(workflowmodel);
						setResponsePage(page);
					}
					else {
						
						// ERROR ??
						// TODO VER AT IDocPageV6
						setResponsePage(WorkspaceConsole.this.getPageV6(model));
					}
				} 
				/**
				 * This detach requires to detach the  Console 
				 * because this is an inline class.
				 */
				@Override
				public void onDetach() {
					super.onDetach();
					WorkspaceConsole.this.onDetach();
				}
			};
		}
		else {
			panel = super.getNavigationPanel(model, index);
		}
		return panel;
	}
	
	/***
	 *
	 * 
	 * 
	 */
	protected void setDefaults(Content content) {
		try {
			ContentTemplate template = content.getContentTemplate();
			boolean updated = false;
			for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
				Classifier classifier = classifiertemplate.getClassifier();
				String membersid = getSessionUser().getService(PreferencesService.class).getValue("default-"+ template.getName(), classifier.getUniqueName());
				if (membersid!=null) {
					StringTokenizer tokenizer = new StringTokenizer(membersid, ";");
					List<DataSetMember> members = new ArrayList<DataSetMember>();
					while (tokenizer.hasMoreTokens()) {
						DataSetMember member = getContentDao().findMemberById(Long.valueOf(tokenizer.nextToken()));
						if (member != null) {
							members.add(member);
						}
					}
					if (!members.isEmpty()) {
						content.setClassification(classifier, members);
						updated = true;
					}
				}
			}

			for (AttributeTemplate attributetemplate : template.getAttributes()) {
				String valuesstring = getSessionUser().getService(PreferencesService.class).getValue("default-"+ template.getName(), attributetemplate.getAttribute().getName());
				if (valuesstring!=null) {
					StringTokenizer tokenizer = new StringTokenizer(valuesstring, ";");
					List<String> values = new ArrayList<String>();
					while (tokenizer.hasMoreTokens()) {
						values.add(tokenizer.nextToken());
					}
					if (!values.isEmpty()) {
						content.setAttributeValues(attributetemplate.getAttribute(), values);
						updated = true;
					}
				}	
			}
			if (template.getTitleRuleTemplate()!=null && !"".equals(template.getTitleRuleTemplate()) && updated) {
				ExtractionRule titleRule = content.getContentTemplate().getTitleRule();
				String title = (String)titleRule.extract(content);
				content.setTitle(title);
				//ContentTextTemplate texttemplate = new KbeeContentTextTemplate(template.getTitleRuleTemplate());
				//String title = texttemplate.getText(new ContentVariableResolverWeb<Content>(content));
			}
			if (updated) {
				List<String> parts = new ArrayList<String>();
				parts.add("Set Defaults");
				content.getService(ContentService.class).update(parts);
			}
		}
		catch (Exception e) {
			kblogger.error(e);
		}
	}
	
	
	@Override
	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, WorkspaceConsole.this, null).getString();
	}
	
	@Override
	protected boolean isWorkflowConsole() {
		return true;
	}
	
	@Override
	protected boolean isReadOnly() {
		return false;
	}
	
	

	/***
	 * 
	 * 
	 */
	@Override
	protected boolean isVisible(Facet facet) {
		
		Facet realfacet;
		
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible(KEY);
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		
		if (realfacet instanceof TaskFacet || realfacet instanceof GroupFacet) 
				return true;
		
		return !realfacet.getName().equals("lastmodifieduser") && !realfacet.getName().equals("usermember") && !realfacet.getName().equals("state");
	}
	
	
	
	@Override
	protected Page getPageV6(IModel<Content> model) 	{
	
		if (model.getObject()==null) {
			kblogger.error("model is null");
			return null;
		}
		
		if (model.getObject().getWorkspace()==null) {
			kblogger.error("workspace is null");
			return null;
		}

		if (!model.getObject().getWorkspace().equals(getSessionUser().getId())) {
			kblogger.error("workspace is different");
			return null;
		}
  		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null && workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) 
			return getTaskPage(model);
		
		return super.getPage(model);
	}
	
	protected String getPageUrl(IModel<Content> model) 	{
		Task task = null;
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			task = workflowService.getTask();
			String url =  "/task/id/";
			url += eforms() ? "v6/" : "";
			url += task.getId().replaceAll("\\s", "-").toLowerCase() + "/" + model.getObject().getId();
			return url;
		} 
		catch (Exception e) {
			kblogger.error(e, (model!=null && model.getObject()!=null) ? model.getObject().toString()  : "null");
			if (task==null) {
				ServiceLocator.getService( AppMonitoringService.class).attempToReindexContent(model.getObject());
			}
			return "";
		}
		
	}

	/***
	 * 
	 * 
	 */
	protected List<IModel<Priority>> getPriorities(IModel<Content> model) {
		if (this.priorities_model!=null)
			return this.priorities_model;
		this.priorities_model = new ArrayList<IModel<Priority>>();
		this.priorities_model.add(new Model<Priority>(Priority.Standard));
		this.priorities_model.add(new Model<Priority>(Priority.High));
		this.priorities_model.add(new Model<Priority>(Priority.Urgent));
		
		return this.priorities_model; 
	}
	
	
	/**
	 * TODO: IMPROVE THIS. BY NOW WE SUPPORT ONLY 1 TAG CLASSIFIER (LABELSET) SYSTEM WIDE.
	 * 
	 * @param model
	 * @return
	 */
	protected List<IModel<LabelMember>> getLabelMembers(ContentTemplate ct) {

		if (this.labels.containsKey((Long) ct.getId()))
				return this.labels.get((Long) ct.getId());
			
		
		List<IModel<LabelMember>> xl = new ArrayList<IModel<LabelMember>>();
		
		List<ClassifierTemplate> list = ct.getClassifiers(); //getDataSet().getClassifiers();
			 for (ClassifierTemplate ca: list) {
				 if (ca.getClassifier() !=null && ca.getClassifier().getState()==ObjectState.ENABLED && (ca.getClassifier().getDataSet() instanceof LabelSet)) {
					 for (DataSetMember dm: getContentDao().getMembers(ca.getClassifier().getDataSet(), "strvalue")) {
						 if (dm.getState()==ObjectState.ENABLED)
							 xl.add(new ObjectModel<LabelMember>((LabelMember) dm)); 
					 }
				 }
			 }
			Collections.sort(xl, new Comparator<IModel<LabelMember>>() {
				@Override
				public int compare(IModel<LabelMember> a, IModel<LabelMember> b) {
					try { 
						if (a.getObject()!=null && b.getObject().getDisplayName()==null)
							return -1;
						if (b.getObject()!=null && a.getObject().getDisplayName()==null)
							return -1;
						return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
					} catch (Exception e) {
						kblogger.error(e);
						return 0;
					}
				}
			});
			this.labels.put((Long) ct.getId(), xl);
			return this.labels.get((Long) ct.getId());
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
			kblogger.error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private boolean isWriteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(content);
	}
}
  
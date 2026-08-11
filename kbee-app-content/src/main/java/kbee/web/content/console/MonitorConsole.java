package kbee.web.content.console;


import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;

import com.novamens.content.service.ContentService;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.console.markup.GlyphiconColumnPanel;
import com.novamens.content.web.console.markup.ReAssignButton;
import com.novamens.content.web.console.markup.TakeTasksButton;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.content.web.nav.markup.MonitorNavigationBar;
import com.novamens.content.web.workflow.markup.AssignationModal;

import com.novamens.content.web.workflow.markup.BatchAssignationPanel;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;

import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.GroupFacet;
import com.novamens.kbee.content.multidimensional.StateFacet;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.content.workflow.multidimensional.TaskFacet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.grid.DateColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;

import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.logging.ReadEvent;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
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
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowException;

import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.TitleColumnPanel;
//import kbee.web.console.TargetBlankTitleColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.TaskPriorityColumn;
import kbee.web.console.tools.ExportContentToolButton;
import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.nav.MonitorBC;
import kbee.web.nav.TasksSectionBC;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ExportContentsPage;
import kbee.web.panel.ListSimpleItemMainPanel;
import kbee.web.panel.ListSimplePanel;
import kbee.web.query.MonitorQuery;

import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;
import kbee.web.security.AclPage;


/**
 * 
 *
 */
@SuppressWarnings("serial")
public abstract class MonitorConsole extends ContentConsole<Content> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MonitorConsole.class.getName());

	static final public String KEY = "monitor";
	
	final boolean role_monitor 	 = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
	final boolean role_support	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_pending   = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	
	private String workflow_useri_id= null; 
	private List<GridColumn<SearchResult,String>> columns = null;
	private List<ToolbarItem> selection_toolbar;
	private List<ToolbarItem> items;

	/**
	 * @param id
	 * @param query
	 */
	public MonitorConsole(String id, Query query) {
		super(id, KEY, query);
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
		if ((!model.getObject().isHeadVersion()) && (model.getObject().getVersion()>1))
			return true;
		return false;
	}

	
	protected boolean isFolder(IModel<Content> model) {
		// TODO AT
		return false;
	}
	
	
	public MonitorConsole(Query query) {
		super(KEY, query);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			
			for (GridColumn<?,?> column: getColumns()) 
				column.detach();
			
			if (this.selection_toolbar!=null) 
				this. selection_toolbar.forEach(item -> item.detach());
			
			if (this.items!=null) 
				this.items.forEach(item -> item.detach());
			
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	
	@Override
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		return getItemLabelMetaDefault(modelObject);
	}
	
	
	protected IModel<String> getItemLabelMetaDefault(IModel<Content> modelObject) {
		
		@SuppressWarnings("unchecked")
		ListPanel<Content> panel = (ListPanel<Content>) getBrowser().getPanel(ListPanel.class);
		
		if (panel==null) 
			return null;
		
		ListDisplayMode mode=panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
		
		StringBuilder str = new StringBuilder();
		
		try {
			Long workspace_id = modelObject.getObject().getWorkspace();
			//String separator = "<br/>";

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
					task_workspace = "<span class=\"NNN-highlight\">" + up.getPersonFirstLastName()+"</span>";
				}
			}
			
			try {
			// ---
			// Date
			// 
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			String task_date = null;
			if (date!=null) {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				String zid = service.getMapZoneIds().get( getSessionUser().getTimeZone());
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(zid));
				task_date = ServiceLocator.getService(DateTimeService.class).timeElapsed(zd, ZoneId.of(zid), getSessionUser().getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(task_workspace + " - " + task_name + " - " + task_date);
			}
			else
				str.append(task_workspace + " - " + task_name);
			
			
				
			} catch (Exception e) {
				logger.error(e);
				str.append(e.getClass().getName());
			}

			
	
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString());
		
	}
	
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null) 
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		
		this.columns.add(new GridColumn<SearchResult, String>("unread", getLabel("unreadcolumn")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					if (resultmodel.getObject()==null) {
						cellItem.add(new Label(componentId, ""));
						return;
					}
					Object object = resultmodel.getObject().getObject();
					IModel<Content> objectmodel = getModel((Content)object);
					cellItem.add(new GlyphiconColumnPanel<Content>(componentId, objectmodel) {
							@Override
							public boolean isVisible() {
								 String nr = (String) getModelObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
									return nr!=null && nr.equals("yes");
							}
							
							@Override
							protected IModel<String> getAnchorTitle() {
								return new Model<String>("New Task (unread)");
							}
						});
				}
				catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						cellItem.add(new Label(componentId, "x"));
				}
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content)object.getObject();
				String nr = (String) content.getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
				boolean isRead= nr!=null && nr.equals("yes");
				return ()-> isRead ? "read" : "unread";
			}

			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public int getWidth() {
				return GridPanel.ICON_COL_WIDTH;
			}
			
			@Override
			public boolean isHeaderMenu() {
				return false;
			}
			
			@Override
			public String getCssClass() {
				return "col short col-xs-1 col-md-1 col-lg-1";
			}
			
			@Override
			public boolean isResizable() {
				return false;
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
			public int getXPadding()	{
				return 3;
			}
			
		});
		
	
		GridColumn<SearchResult, String> iconc = new GridColumn<SearchResult, String>("icon", getLabel("iconcolumn")) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {

					Object object = resultmodel.getObject().getObject();
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
											return new ResourceThumbnailImage<>(id, new ObjectModel<Resource>((Resource) rc.getFiles().get(0)) , ThumbnailSize.MINI);
										}
									return null;
							} catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

								return null;
						}
						}
					});
				}
				catch (Exception e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

					cellItem.add(new Label(componentId, e.getClass().getSimpleName()));
				}

			}

			@Override
			public boolean isExportable() {
				return false;
			}

			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
		};
		
		iconc.setPreferred(false);
		this.columns.add(iconc);
		
		
		
		
	 	this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {
				
	 			@Override
	 			public String getCssClass()	{
	 				return super.getCssClass() + " mylist";
	 			}
	 			
	 			
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(MonitorConsole.this.getName(), (Content) object.getObject());
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
					return MonitorConsole.this.getName() + super.getContextKey();
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
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
			}
		});
		
		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
		});

		
		 
		GridColumn<SearchResult, String> gc = new GridColumn<SearchResult, String>("task", getLabel("taskcolumn"), "task_sort") {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						WorkflowService workflowService = ((Content)object.getObject()).getService(WorkflowService.class);
						String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
						return new Model<String>(taskname);
				
					} catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						return new Model<String>(e.getClass().getSimpleName() + " "+ e.getMessage());
					}
				}

				@Override
				protected String getContextKey() {
					return MonitorConsole.this.getName() + super.getContextKey();
				}
			};
			this.columns.add(gc);
		 
		
		
		
		
			this.columns.add(new TaskPriorityColumn<Content>("priority", getLabel("taskprioritycolumn"), "priority_sort") { 
				@Override
				protected String getContextKey() {
					return MonitorConsole.this.getName() + super.getContextKey();
				}
				@Override
				public boolean isPreferred() {
					return false;
				}
			});
		
		
		
		GridColumn<SearchResult, String> wk = new GridColumn<SearchResult, String>("workspace", getLabel("workspacecolumn"), "workspace_sort") {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					
					Content content = (Content)object.getObject();
					Long wks = content.getWorkspace();
					
					if (wks!=null && wks.longValue()>0) {
						KbeeUser user = (KbeeUser) ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserById(wks);
							
						
						if (user.isEnabled())
								return new Model<String>(user.getFirstLastName());
							else {
								if (getWorkflowUser().equals(user.getId().toString())) 
									return new Model<String>(user.getFirstLastName());
								else
									return new Model<String>(user.getFirstLastName()+" <span class=\"ago\">( "+ user.getState().getLabel(getSessionUser().getLocale()) + " )</span>");
								
							}
					}
					else
						return new Model<String>("err");
					
					} catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						return new Model<String>( e.getClass().getSimpleName());
					}
			}
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				try {
					Content object = (Content) model.getObject().getObject();
					Long wks = object .getWorkspace();
					if (wks!=null && wks.longValue()>0) {
						if (getWorkflowUser().equals(String.valueOf(wks))) {
							return "pending";
						}
					}
					return null;
				} catch (Exception e) {
					logger.error(e);
					return null;
				}
			}
			
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
		};
		this.columns.add(wk);
		

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

		
		
		
		
		 
			this.columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("contentclasscolumn")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					try {
						return new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName());
					} catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

						return new Model<String>( e.getClass().getSimpleName());
					}
				}
				@Override
				protected String getContextKey() {
					return MonitorConsole.this.getName() + super.getContextKey();
				}
				
			});
		
		
		
		for (Attribute attribute: getAttributes()) {
			if (attribute.getState()==ObjectState.ENABLED  && attribute.isVisible(KEY)) {
				if (attribute.isDate())
					this.columns.add(new AttributeDateColumn(new ObjectModel<Attribute>(attribute), getName()));
				else
					this.columns.add(new AttributeColumn(new ObjectModel<Attribute>(attribute), getName()));
			}
		}

		
		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					return new Model<String>(String.valueOf(((Content)object.getObject()).getOId()));
				} catch (Exception e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

					return new Model<String>( e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return false;
			}

		});
		

		this.columns.add(new DateColumn<Content>("taskstart", getLabel("taskstartcolumn"),"assigned") {
			@Override
			protected OffsetDateTime getOffsetDateTime(Content content) {
				try {
					return content.getService(WorkflowService.class).getContext().getTime();
				} 
				catch (Exception e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
					return  null;
				}
			}
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
			//
			// Date is in User's TimeZone
			//
			this.columns.add(new DateColumn<Content>("duedate", getLabel("duedatecolumn"), "duedate", DateTimeService.MONTH_DAY_YEAR_LABEL) {
				@Override
				protected OffsetDateTime getOffsetDateTime(Content content) {
					try {
						return content.getService(WorkflowService.class).getContext().getDueDate();
					} 
					catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						return  null;
					}
				}
	
				@Override
				protected String getDateClass(IModel<SearchResult> resultmodel) {
					try {
						
						Content content = (Content) resultmodel.getObject().getObject();
						OffsetDateTime date = content.getService(WorkflowService.class).getContext().getDueDate();
						
						if (date==null)
							return "date-container";
						
						if (date.truncatedTo(ChronoUnit.DAYS).equals(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)))
							return "date-container due-today";
						
						if (date.isAfter(OffsetDateTime.now()))
							return "date-container";
						
						return "date-container expired";
						
					} 
					catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

					}
					return "date-container ";
				}
	
				@Override
				protected String getNullValue() {
					return "";
				}
				
				@Override
				protected String getContextKey() {
					return MonitorConsole.this.getName() + super.getContextKey();
				}
				@Override
				public boolean isPreferred() {
					return false;
				}
			});
		
		
		
		// users' timezone
		//
		this.columns.add(new DateColumn<Content>("processstart", getLabel("processstartcolumn"), null) {
			@Override
			protected OffsetDateTime getOffsetDateTime(Content content) {
				try {
					WorkflowService workflowService = content.getService(WorkflowService.class);
					return workflowService.getActivity().getProcess().getStartTime();
				} 
				catch (Exception e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

					return  null;
				}
			}
			
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		
		this.columns.add(new GridColumn<SearchResult, String>("modifieduser", getLabel("modifieduser")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				try {
					return new Model<String>(String.valueOf(((Content)object.getObject()).getLastModifiedUser().getFirstLastName()));
				} catch (Exception e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return MonitorConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		
		//if (logger.isDebugEnabled()) {
		//	this.columns.forEach(item -> logger.debug(item.getId()+ " " + item.isPreferred()));
		//}
		
		return this.columns;
	}
	
	/**
	 * Workflow User has all pending tasks
 	 */
	public String getWorkflowUser() {
		if (this.workflow_useri_id!=null)
			return this.workflow_useri_id;
		User user = getDomain().getService(DomainService.class).getWorkflowUser();
		this.workflow_useri_id = user!=null?String.valueOf(user.getId()):null;
		return this.workflow_useri_id;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new MonitorQuery(getQueryIndex()));
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
	}

	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new MonitorBC());
	}

	@Override
	protected boolean isEditionEnabled() {
		return false;
	}
	
	@Override
	protected void checkAndMarkAsRead(IModel<Content> model) {
		
		if ((model.getObject().getWorkspace()!=null) && (model.getObject().getWorkspace().equals(getSessionUser().getId()))) {
			String uread = (String) model.getObject().getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
			if (uread!=null && uread.equals("yes")) {
				model.getObject().getService(PropertyService.class).removeProperty(PROPERTY_UNREAD);
				try {
					model.getObject().getService(ContentService.class).update(new ReadEvent(model.getObject(), "Task opened"));
				} 
				catch (ServiceNotFoundException | ContentMgmtException   e) {
					logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
				}
			}
		}
	}


	/**
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<Content> model) {

		try {
		
			ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
			
			menu.addItem(id ->
				new MenuItemPanelV5<Content>(id) {
					public void onClick() {
						try {	
							checkAndMarkAsRead(getModel());
							if ((getModel().getObject().getWorkspace()!=null)) 
								setResponsePage(MonitorConsole.this.getTaskPage(getModel()));
							else 
								setResponsePage(new ApplicationErrorPage<Content>( new Model<String>(getName()) , new Model<String>("File No longer in Workspace")));
						} 
						catch (Exception e) {
							logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
							setResponsePage(new ApplicationErrorPage<Content>(e));
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("monitor.contextmenu.open").getObject();
					}
				});
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new SubMenuAjaxUserListItemPanel<Content>(id, model, MonitorConsole.this.getName(), UserListItem.NEWEST);
				}
			});

			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						@SuppressWarnings("unchecked")
						public void onClick(AjaxRequestTarget target) {
							try {
								if ((getModel().getObject().getWorkspace()!=null) && (getModel().getObject().getWorkspace()>0)) {
										Modal modal = MonitorConsole.this.getSendByEmailModal();
										((ShareModal<Content>)modal).open(target, getModel());
								}
							} catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								fire (new ErrorEvent<>(target, e));
							}
						}
						
						@Override 
						public boolean isEnabled() {
							if (isSupportUser())
								return false;
							
							return isSendByEmail();
						}
						
						@Override 
						public String getLabel() {
							return MonitorConsole.this.getLabel("monitor.contextmenu.sendbyemail").getObject();
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
							try {
								 Modal modal = MonitorConsole.this.getAuditTrailModal();
								 ((AuditTrailModal<Content>)modal).open(target, getModel());
								
							} 
							catch  (Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e)); 
							}
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.audittrail").getObject();
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
						public void onClick(AjaxRequestTarget target) {
							getModelObject().getService(ContentSubscriptionService.class).subscribe(getPerson());
							refresh(target);
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.subscribe").getObject();
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
							refresh(target);
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.unsubscribe").getObject();
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
						return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
							@Override 
							public String getLabel() {
								return MonitorConsole.this.getLabel("monitor.contextmenu.download").getObject();
							}
							@Override
							public boolean isDeleteFileAfterDownload()  {
								return true;
							}
							@Override
							protected File getFile() {
								File file = getModelObject().getService(ContentExportService.class).getHTMLExport();
								return file;
							}

							@Override 
							public boolean isEnabled() {
									if (isSupportUser())
										return false;
								return isSendByEmail();
							}
							
							
							@Override
							public boolean isVisible()  {
								try {
									return true;
								} 
								catch (Exception e)	{
									logger.error(e);
									return true;
								}
							}
						};
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new MenuItemPanelV5<Content>(id) {
						public void onClick() {
							setResponsePage(new AclPage(getModel()));
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.acl").getObject();
						}
						@Override 
						public String getTarget() {
							return "_blank";
						}
						@Override 
						public boolean isEnabled() {
							try {
							if (isSupportUser())
								return true;
							return isWriteable(getModel());
							} catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");

								return false;
							}
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
								getModelObject().getService(ContentService.class).reindex();
							} 
							catch (Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
							}
							refresh(target);
						}
						@Override 
						public String getLabel() {
							return "Reindex <span class=\"only-root\">(admin)</span>";
						}
						@Override
						public boolean isVisible() {
							if (root || isAdminUser()) 
								return true;
							return false;
						}
					};
				}
			});
			
			if (model.getObject().getService(WorkflowService.class).getTask() instanceof UserTask) {
			
			menu.addItem(new MenuItemFactory<Content>() {
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
								boolean pending = getWorkflowUser().equals(String.valueOf(getModel().getObject().getWorkspace()));
								return (pending && isTakeable(model)) || isMonitorable(getModel());
							} 
							catch  (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								return false;
							}
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
							boolean lock = true;
							try {
								lock(getModel());
								if (isTaskStarted()) {
									unlock(getModel());
									lock = false;
									getErrorDialog().open(target, getConsoleLabel("monitor.error.nolonger"));
								}
								else {
									getModelObject().getService(WorkflowService.class).startTask();
									FeedbackHelper.showInfoToast("Taken " + getModel().getObject().getDisplayName());
									resetSelection();
								}
								refresh(target);
							}
							catch (WorkflowException e) {
								unlock(getModel());
								lock = false;
								FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
								getErrorDialog().open(target, new Model<String>(e.getMessage()));
							}
							finally {
								if (lock)
									unlock(getModel());
							}
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.take").getObject();
						}
						@Override 
						public String getWorkingLabel() {
							return getConsoleLabel("monitor.contextmenu.take.working").getObject();
						}
						@Override
						public boolean isEnabled() {
							if (isSupportUser())
								return false;
							return true;
						}
						@Override
						public boolean isVisible() {
							try {
								return isPending(getModelObject()) && isTakeable(model) ;
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								return false;
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
			
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
							//setResponsePage(new ForcedActionsPage(getModel()));
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.forcedactions").getObject();
						}
						@Override
						public boolean isVisible() {
							
							try {
								if (!isBatchEnabled(getModel()))
									return false;
								return isTerminable(getModel());
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								
								return false;
							}
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
							try {
								AssignationModal<Content> modal = (AssignationModal<Content>)MonitorConsole.this.get("assignation-modal");
								String title = getModelObject().getTitle();
								IModel<WorkflowContext> model = getWorkflowModel(getModel());
								Activity activity = model.getObject().getCurrentActivity();
								modal.open(target, model, new Modal.Handler() {
									@Override
									public void onClick(AjaxRequestTarget target, Button button) {
										if (button.isSubmit()) {
											refresh(target);
										}
									}
								}, activity.getEnabledGroups(), title);
							} 
							catch (Exception e) {
								fire (new ErrorEvent<>(target, e));
							}
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.assignation").getObject();
						}
						@Override
						public boolean isEnabled() {
							return !isPending(getModelObject()) && isTaskEnabled(getModelObject());
						}
						@Override
						public boolean isVisible() {
							try {
								return isMonitorable(getModel());
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								return false;
							}
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

							boolean lock = true;
							try {
								lock(getModel());
								if (isTaskStarted()) {
									unlock(getModel());
									lock = false;
									getErrorDialog().open(target, getConsoleLabel("monitor.error.nolonger"));
								}
								else {
									getModelObject().getService(WorkflowService.class).startTask();
									AssignationModal<Content> modal = (AssignationModal<Content>)MonitorConsole.this.get("assignation-modal");
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
								}
								refresh(target);
							}
							catch (WorkflowException e) {
								unlock(getModel());
								lock = false;
								getErrorDialog().open(target, new Model<String>(e.getMessage()));
							}
							finally {
								if (lock)
									unlock(getModel());
							}
						}
						
						public boolean isTaskStarted() {
							WorkflowService ws = getModelObject().getService(WorkflowService.class);
							if (ws!=null && ws.getContext().getTime()==null)
								return false;
							else
								return true;
						}
						
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.take-assignation").getObject();
						}
						@Override
						public boolean isEnabled() {
							return isPending(getModelObject());
						}
						
						@Override
						public boolean isVisible() {
							try {
								return isPending(getModelObject()) && isTakeable(model) ;
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								return false;
							}
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
							if (getModelObject().getWorkspace()!=null) {
								User user = ((com.novamens.kbee.content.workflow.KbeeContext) getWorkflowModel(getModel()).getObject()).getUser();
								if (!user.equals(getSessionUser())) {
									String note = getConsoleLabel("monitor.reassignedfrom").getObject() + 
										user.getFirstLastName() + " " + 
										getConsoleLabel("monitor.by").getObject() + " " + getSessionUser().getFirstLastName();
								
									getModelObject().getService(WorkflowService.class).reassign(getSessionUser(), note);
									
							    	FeedbackHelper.showInfoToast("Reassigned to My Tasks <br /> " + getModel().getObject().getDisplayName());
									refresh(target);
								}
								else {
									getErrorDialog().open(target, new Model<String>("INFO"), getConsoleLabel("monitor.error.alreadyinyourworkspace"));
								}
							}
							else {
								getErrorDialog().open(target, new Model<String>("INFO"), getConsoleLabel("monitor.error.nolonger"));
							}
							} catch (Exception e) {
								logger.error(e);;
								FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
							}
						}
						@Override 
						public String getLabel() {
							return getConsoleLabel("monitor.contextmenu.assignation-tome").getObject();
						}
						@Override 
						public String getWorkingLabel() {
							return getConsoleLabel("monitor.contextmenu.take.working").getObject();
						}
						
						@Override
						public boolean isEnabled() {
							return !isPending(getModelObject()) && !isOwner(getModelObject()) && isTaskEnabled(getModelObject());
						}
						
						@Override
						public boolean isVisible() {
							try {
								return isMonitorable(getModel());
							} 
							catch (Exception e) {
								logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
								return false;
							}
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new AjaxMenuItemPanelV5<Content>(id) {
						public void onClick(AjaxRequestTarget target) {
							try  {
								getModelObject().getService(WorkflowService.class).restartPrevious();
								FeedbackHelper.showInfoToast(getLabel());
								refresh(target);
							} 
							catch (Exception e) {
								logger.error(e);
								fire (new ErrorEvent<>(target, e));
							}
						}
						@Override 
						public String getLabel() {
							KbeeContext kbcontext = (KbeeContext)getModelObject().getService(WorkflowService.class).getContext();
							if (kbcontext.getPreviousTerminatedActivity()!=null &&
								getSessionUser().equals(kbcontext.getPreviousTerminatedActivity().getUser())) {
								return getConsoleLabel("monitor.contextmenu.restartprevious", kbcontext.getPreviousTerminatedActivity().getTask().getDisplayName()).getObject();
							}
							return "";	
						}
						@Override
						public boolean isVisible() {
							if (!isPending(getModelObject()))
								return false;	
							KbeeContext kbcontext = (KbeeContext)getModelObject().getService(WorkflowService.class).getContext();
							if (kbcontext.getPreviousTerminatedActivity()!=null &&
								getSessionUser().equals(kbcontext.getPreviousTerminatedActivity().getUser())) {
								return true;
							}
							return false;	
						}
					};
				}
			});
			
			}
				
			
			return menu;
		}
		catch (Exception e) {
			logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
			return new InvisiblePanel("menu");
		}
	}
	
	/**
	 * 
	 */
	@Override
	protected Panel getNavigationPanel(IModel<Content> model, long index) {
		Panel panel;
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null && workflowService.getTask()!=null) {
			IModel<WorkflowContext> workflowmodel  =  getWorkflowModel(model);
			panel = new MonitorNavigationBar<Content>("navigation", workflowmodel, getSearcher(), index) {
				@Override
				@SuppressWarnings("unchecked")
				public void onNavigate(Content content) {
					IModel<Content> model = getModel(content);
					IModel<WorkflowContext> workflowmodel = MonitorConsole.this.getWorkflowModel(model);
					if (workflowmodel!=null) {
						setWorkflowModel(workflowmodel);
						Page page = MonitorConsole.this.getTaskPage(model);
						((AbstractApplicationPage<Content>)page).setTopNavigation(this);
						setResponsePage(page);
					}
					else {
						setResponsePage(MonitorConsole.this.getPageV6(model));
					}
				} 
				@Override
				public void onReturn() {
					setResponsePage(getConsolePage(getQuery()));
				}
				/**
				 * This detach requires to detach the MonitorConsole also because this
				 * is an inline class.
				 */
				@Override
				public void onDetach() {
					super.onDetach();
					MonitorConsole.this.onDetach();
				}
			};
		}
		else {
			panel = super.getNavigationPanel(model, index);
		}
		
		return panel;
	}
	
	
	/**
	 * 
	 */
	@Override
	protected void addModals() {
		super.addModals();
		add(new AssignationModal<Content>());
	}


	/**
	 * 
	 */
	@Override
	protected boolean isWorkflowConsole() {
		return true;
	}

	
	/**
	 * 
	 * LEFT Selection Actions
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		this.selection_toolbar.add(new ReAssignButton(browser, Align.TOP_NONE, true) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				GenericBatchActionPage page = new GenericBatchActionPage(MonitorConsole.this.getBrowser().getSelection()) {
					public String getIcon() {
						return "far fa-inbox-in";
					}
					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("monitor.batch.reassign");
					}
					@Override
					public IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("reassign");
					}
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("monitor.batch.class");
					}
					@Override
					protected Panel newActionPanel() {
						return new BatchAssignationPanel("editor", getSelection()) {
							@Override
							public void onReturn() {
								setResponsePage(getConsolePage(getQuery()));
							}
							@Override
							public IModel<String> getReturnLabel() {
								return getConsoleLabel("monitor.batch.return");
							}
							@Override
							protected Page getPage(IModel<Content> model) {
								Page page = MonitorConsole.this.getPage(model);
								((AbstractApplicationPage<?>)page).setTopNavigation(new MonitorNavigationBar<Content>(getWorkflowModel(model)));
								return page;
							}
						};
					}
				};
				

				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new TasksSectionBC());
				list.add(new MonitorBC());
				list.add(new BCElement(getConsoleLabel("monitor.batch.reassign")));
				page.setBreadCrumb(list);
				setResponsePage(page);
				
			}
			@Override
			public boolean isVisible() {
				return isAdminUser();
			}
		});


		/**
		 * TAKE
		 * 
		 */
		this.selection_toolbar.add(new TakeTasksButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GenericBatchActionPage page = new GenericBatchActionPage(MonitorConsole.this.getBrowser().getSelection()) {
					
					public String getIcon() {
						return "far fa-inbox-in fa-fw";
					}
					
					@Override
					protected IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("monitor.batch.take");
					}
					
					public IModel<String> getTitle() {
						return getConsoleLabel("monitor.batch.take");
					}
					public IModel<String> getType() {
						return getConsoleLabel("monitor.batch.class");
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
								rc = getConsoleLabel("monitor.error.nolonger").getObject(); 
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
				list.add(new MonitorBC());
				list.add(new BCElement(getConsoleLabel("monitor.batch.take")));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}
		});
		
		
		
		// Export
		// 
		this.selection_toolbar.add(new ExportContentToolButton<Content>(browser, ToolbarItem.Align.TOP_LEFT, true) {
					@Override
					protected void onClick(AjaxRequestTarget target) {
						setResponsePage(new ExportContentsPage(getListModel(), new MonitorBC()) {
								@Override
								public void onClose() {
									setResponsePage(new MonitorPage());
								}
							});				
							refresh(target);
					}
					
		});
		
		return this.selection_toolbar; 
	}
	
	
	
	/**
	 * 
	 *  RIGHT. Toolbar Actions
	 *  
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.items!=null)
			return this.items;

	 	this.items = super.getToolbarItems(browser);
	 			
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return MonitorConsole.this.getName();}, new Model<String>(MonitorConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);

		return this.items; 
	}
	

	/**
	 * 
	 * 
	 * 
	 */

	@Override
	protected boolean isReadOnly() {
		return  false;
	}
	
	
	protected boolean isBatchEnabled(IModel<Content> model) {
		return model.getObject().getService(WorkflowService.class).isBatchEnabled();
	}

	
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

		if (realfacet instanceof ClassifierFacet) {
			return ((ClassifierFacet)realfacet).getClassifier().isVisible(KEY);
		}
		else {
			if (realfacet instanceof ClassifierHierarchicalFacet) {
				return ((ClassifierHierarchicalFacet)realfacet).getClassifier().isVisible(KEY);
			}
			else {
				if (realfacet instanceof StateFacet) {
					return false;
				}
				else {
					return true;
				}
			}	
		}			
	}
	
	@Override
	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, MonitorConsole.this, null).getString();
	}
	
	@Override
	protected Panel getTopPanel() {
		try {
			return new AdvancedSearchContentSelectorPanel("top", getName());
		} 
		catch (Exception e) {
			logger.error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	protected boolean hasTopPanel() {
		return true;
	}
	
	protected boolean isPending(Content content) {
		String wuid = getWorkflowUser();
		if (wuid!=null && content.getWorkspace()!=null) {
			return content.getWorkspace().toString().equals(wuid);
		}	
		return false;
	}
	
	protected boolean isOwner(Content content) {
		Serializable uid = getSessionUser().getId();
		if (uid!=null && content.getWorkspace()!=null) {
			return content.getWorkspace().toString().equals(String.valueOf(uid));
		}	
		return false;
	}
	
	protected boolean isTaskEnabled(Content content) {
		if (role_admin) {
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
	
	protected void loadQueryPreferences() {
		String qp=getUserPreference("queryParameters");
		if (qp==null)
			return;
		Map<String, Object> map =  convertWithStream(qp);
		getQuery().setParameters(map);
	}

	
	protected void saveQueryPreferences() {
		String sm=convertWithStream( getQuery().getParameters());
	    logger.debug(sm);
		setUserPreference("queryParameters",sm);
	}

}


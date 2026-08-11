package kbee.web.content.console;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.ContentService;
import com.novamens.content.web.console.markup.RecycleSourceSelector;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.command.AsyncCommand;

import com.novamens.kbee.content.command.EmptyRecycleBinCommand;
import com.novamens.kbee.content.command.RecycleBinCleanUpCommand;
import com.novamens.kbee.wicket.markup.html.console.browser.DeleteButton;
import com.novamens.kbee.wicket.markup.html.console.browser.LinkButton;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.service.ContentExportService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.command.panel.AsyncExecutorModal;
import kbee.web.command.panel.CommandModel;
import kbee.web.command.panel.CommandPage;
import kbee.web.console.BaseBrowser;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.nav.ContentSectionBC;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.query.RecycleBinQuery2;
import kbee.web.util.Property;

@SuppressWarnings("serial")
public abstract class RecycleBinConsole extends ContentConsole<Content> {
					
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RecycleBinConsole.class.getName());
	
	private List<NavigationOrder> orders;
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> selection_toolbar;


	public class DeleteableModel implements IModel<Content> {
		
		private Serializable id;
		private IModel<ContentTemplate> templatemodel;
		private Class<?> clazz;
		private String title;
		private boolean headversion;
		private Content content = null;
		
		public DeleteableModel(Content content) {
			id = content.getId();
			title = content.getTitle();
			headversion = content.isHeadVersion();
			templatemodel = new ObjectModel<ContentTemplate>(content.getContentTemplate());
			clazz = Proxy.getClass(content);
		}
		
		public Content getObject() {
			if (content==null) {
				try {
					content = (Content)clazz.newInstance();
				}
				catch(Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
				
				content.setId(id);
				content.setTitle(title);
				content.setContentTemplate(templatemodel.getObject());
				((KbeeContent)content).setHeadVersion(headversion);
			}
			return content;
		}
		public void setObject(Content content) {
			
		}
		
		public void detach() {
			content = null;
			templatemodel.detach();
		}
	}

 
	public RecycleBinConsole(Query query) {
		super("recycle", query);
	}


	
	@Override
	protected String getIcon(IModel<Content> model) {
		return null;
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
					logger.error(e);
					return 0;
				}
			}
		});
		return this.orders;
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
		return setUserPreference(new RecycleBinQuery2(getQueryIndex(), getSessionUser()));
	}

	
	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new RecycleBinBC());
	};

	
	@Override
	protected boolean isEditionEnabled() {
		return true;
	}

	
	@Override
	protected Panel getMenu(IModel<Content> model) {
		
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					public void onClick() {
						setResponsePage(RecycleBinConsole.this.getPageV6(getModel()));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("recyclebin.contextmenu.open").getObject();
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						if (getModelObject().getState()==ObjectState.DELETED) {
							Content previousVersion = ((KbeeContent)getModelObject()).getPreviousVersion();
							if (!getModelObject().isHeadVersion() && previousVersion!=null && (!previousVersion.isHeadVersion() || previousVersion.isLocked())) {
								if (previousVersion.isLocked())
									getErrorDialog().open(target, getConsoleLabel("recyclebin.error.locked"));
								else
									getErrorDialog().open(target, getConsoleLabel("recyclebin.error.newversion"));
							}
							else {
								try {
									getModelObject().getService(ContentService.class).restore();
								} catch (ContentMgmtException | ServiceNotFoundException e) {
									logger.error(e);
								}
								resetSelection();
								refresh(target);
							}
						}
					}
					@Override 
					public String getLabel() {
						if (!getModelObject().isHeadVersion()) {
							return getConsoleLabel("recyclebin.contextmenu.restore.workspace").getObject();
						}
						else {
							if (!getModelObject().getContentTemplate().isTemplate()) {
								return getConsoleLabel("recyclebin.contextmenu.restore.content").getObject();
							}
							else {
								return getConsoleLabel("recyclebin.contextmenu.restore.template").getObject();
							}
						}
					}
					@Override 
					public String getWorkingLabel() {
						return getConsoleLabel("recyclebin.contextmenu.restore.working").getObject();
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
								return RecycleBinConsole.this.getLabel("recyclebin.contextmenu.download").getObject();
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
						public boolean isEnabled()  {
							return (isRoot() || !isSupportUser());
						}
						
						@Override
						public boolean isVisible()  {
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
						Modal modal = RecycleBinConsole.this.getAuditTrailModal();
						((AuditTrailModal<Content>)modal).open(target, getModel());
					}
					@Override 
					public String getLabel() {
						return RecycleBinConsole.this.getLabel("recyclebin.contextmenu.audittrail").getObject();
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
				return new SeparatorMenuItemPanelV5<Content>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						getConfirmationDialog().open(target, getConsoleLabel("recyclebin.deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									executeDelete(target);
									resetSelection();
									RecycleBinConsole.this.refresh(target);
								}
							}
						});
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("recyclebin.contextmenu.delete").getObject();
					}
					@Override
					public boolean isEnabled() {
						return isDeleteable(getModel());
					}
					protected void executeDelete(AjaxRequestTarget target) {
						try {
							if (getModelObject().isHeadVersion()) {
								getModelObject().getService(ContentService.class).deleteAllVersions();
							}
							else {
								getModelObject().getService(ContentService.class).delete();
							}
						}
						catch (Exception e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getMessage()));
						}
					}
				};
			}
		});
		
		return menu;
	}

	

	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		{
			LinkPredicateKbeeGridColumn<Content> titleColumn =
					new LinkPredicateKbeeGridColumn<Content>("title", getLabel("recyclebin.column.title"), "title_sort",
							obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}
		
		//for (Classifier classifier : getClassifiers()) {
		//	if (classifier.isDefaultGridColumn()) 
		//		this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier)));
		//}
		
		{
			KbeePredicateGridColumn<Content> taskColumn = new KbeePredicateGridColumn<>("source", getLabel("recyclebin.column.source"),
					obj ->   getSourceColumnTextModel(obj).getObject()  );
			taskColumn.setContextKey(this.getName() + taskColumn.getContextKey());
			this.columns.add(taskColumn);
		}

		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return RecycleBinConsole.this.getName() + super.getContextKey();
			}
		});
		
		{
			KbeePredicateGridColumn<Content> deletedByColumn = new KbeePredicateGridColumn<>("deletedby", getLabel("deletedbycolumn"),
					obj ->   obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err" );
			deletedByColumn.setContextKey(this.getName() + deletedByColumn.getContextKey());
			this.columns.add(deletedByColumn);
		}
		
		
		for (Classifier classifier : getClassifiers()) {
			if (!classifier.isDefaultGridColumn())
				this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
		}
		
		

		{
				KbeePredicateGridColumn<Content> contentClassColumn = new KbeePredicateGridColumn<>("contentclass", getLabel("contentclasscolumn"),
						obj ->   obj.getContentTemplate().getDisplayName() );
				contentClassColumn.setContextKey(this.getName() + contentClassColumn.getContextKey());
				this.columns.add(contentClassColumn);
		}

		
		// String key = "content";
		for (Attribute attribute: getAttributes()) {
			if (attribute.getState()==ObjectState.ENABLED /*&& attribute.isVisible(key) */) {
				if (attribute.isDate())
					this.columns.add(new AttributeDateColumn(new ObjectModel<Attribute>(attribute), getName()));
				else
					this.columns.add(new AttributeColumn(new ObjectModel<Attribute>(attribute), getName()));
			}
		}

		{
			KbeePredicateGridColumn<Content> taskColumn = new KbeePredicateGridColumn<>("deletedby", getLabel("deletedbycolumn"),
					obj ->   obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err" );
			taskColumn.setContextKey(this.getName() + taskColumn.getContextKey());
			this.columns.add(taskColumn);
		}

		{
			KbeePredicateGridColumn<Content> idColumn = new KbeePredicateGridColumn<>("id", getLabel("idcolumn"),
					obj ->  String.valueOf(obj.getOId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}


		return this.columns;
	}

	private IModel<String> getSourceColumnTextModel(Content content) {
		if (content.isHeadVersion()) {
			if (content.getContentTemplate().isTemplate()) {
				return getLabel("recyclebin.source.template");
			}
			else {
				return getLabel("recyclebin.source.content");
			}
		}
		else {
			return getLabel("recyclebin.source.workspace");
		}
	}

	
	/***
	 * 
	 * 
	 * 
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
				
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected String getLabelStr() {
				return new StringResourceModel("recyclebin.tools.restore", this, null).getObject();
			}

			// Restore
			//
			@Override
			protected String getIcon() {
				return "far fa-recycle fa-fw";
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GenericBatchActionPage page = new GenericBatchActionPage(RecycleBinConsole.this.getBrowser().getSelection()) {
					@Override
					public String getIcon() {
						return "far fa-recycle";
					}
					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("recyclebin.batch.restore");
					}
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("recyclebin.batch.class");
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected IModel<String> getExecuteButtonLabel() {
						return new StringResourceModel("recyclebin.batch.restore", RecycleBinConsole.this, null);
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						Content content = model.getObject();
						if (content.getState()==ObjectState.DELETED) {
							Content previousVersion = ((KbeeContent)content).getPreviousVersion();
							if (!content.isHeadVersion() && previousVersion!=null && (!previousVersion.isHeadVersion() || previousVersion.isLocked())) {
								if (previousVersion.isLocked())
									return getConsoleLabel("recyclebin.error.locked").getObject();	
								else
									return getConsoleLabel("recyclebin.error.newversion").getObject();
							}
							else {
								try {
									content.getService(ContentService.class).restore();
								} catch (ContentMgmtException | ServiceNotFoundException e) {
									logger.error(e);
								}
							}
						}
						else {
							return getConsoleLabel("recyclebin.error.state").getObject();	
						}
						return "";
					}
					@Override
					protected List<Property<Content>> getSelectionProperties() {
						List<Property<Content>> properties = new ArrayList<Property<Content>>();
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.title", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								return new PropertyModel<String>(model, "title");
							}
							public String getCss() {
								return "col-lg-4";
							}
							public boolean isLink() {
								return true;
							}
						});
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.contentclass", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								return new PropertyModel<String>(model, "contentTemplate.name");
							}
							public String getCss() {
								return "col-lg-2";
							}
						});
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.source", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								if (model.getObject().isHeadVersion()) {
									if (model.getObject().getContentTemplate().isTemplate()) {
										return getConsoleLabel("recyclebin.source.template");	
									}
									else {
										return getConsoleLabel("recyclebin.source.content");	
									}
								}
								else {
									return getConsoleLabel("recyclebin.source.workspace");	
								}
							}
							public String getCss() {
								return "col-lg-2";
							}
						});
						return properties;
					}
					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = RecycleBinConsole.this.getPage(model);
						((AbstractApplicationPage<?>)page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
				};
				
				// new BreadCrumb(new ContentBaseBC(),), 
				
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new RecycleBinBC());
				list.add(new BCElement(new StringResourceModel("recyclebin.batch.title", RecycleBinConsole.this, null)));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}
		});
		
		
		//
		//
		
		this.selection_toolbar.add(new DeleteButton(browser, ToolbarItem.Align.TOP_LEFT, true) {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				GenericBatchActionPage page = new GenericBatchActionPage(RecycleBinConsole.this.getDeleteableSelection()) {
					@Override
					public String getIcon() {
						return "far fa-trash-alt";
					}
					@Override
					public IModel<String> getTitle() {
						return getConsoleLabel("recyclebin.batch.delete");
					}
					@Override
					public IModel<String> getType() {
						return getConsoleLabel("recyclebin.batch.class");
					}
					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}
					@Override
					protected String executeAction(IModel<Content> model) {
						Content content = getContentDao().findContentById(getContentClass(model.getObject()), String.valueOf(model.getObject().getId()));
						if (!isDeleteable(content)) {
							return "denied";
						}
						try {
							if (content.isHeadVersion()) {
								content.getService(ContentService.class).deleteAllVersions();
							}
							else {
								content.getService(ContentService.class).delete();
							}
						}
						catch ( Exception e) {
							return "error";
						}
						return "";
					}
					@Override
					protected IModel<String> getExecuteButtonLabel() {
						return new StringResourceModel("recyclebin.batch.delete", RecycleBinConsole.this, null);
					}
					@Override
					protected String getExecuteButtonCss() {
						return "btn btn-sm btn-danger";
					}
					@Override
					protected List<Property<Content>> getSelectionProperties() {
						List<Property<Content>> properties = new ArrayList<Property<Content>>();
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.title", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								return new PropertyModel<String>(model, "title");
							}
							public String getCss() {
								return "col-lg-4";
							}
							public boolean isLink() {
								return true;
							}
						});
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.contentclass", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								return new PropertyModel<String>(model, "contentTemplate.name");
							}
							public String getCss() {
								return "col-lg-2";
							}
						});
						properties.add(new Property<Content>() {
							public IModel<String> getLabel() {
								return new StringResourceModel("recyclebin.batch.source", RecycleBinConsole.this, null);
							}
							public IModel<String> getValue(IModel<Content> model) {
								if (model.getObject().isHeadVersion()) {
									if (model.getObject().getContentTemplate().isTemplate()) {
										return getConsoleLabel("recyclebin.source.template");	
									}
									else {
										return getConsoleLabel("recyclebin.source.content");	
									}
								}
								else {
									return getConsoleLabel("recyclebin.source.workspace");	
								}
							}
							public String getCss() {
								return "col-lg-2";
							}
						});
						return properties;
					}
					@Override
					protected Page getPage(IModel<Content> model) {
						Content content = getContentDao().findContentById(getContentClass(model.getObject()), String.valueOf(model.getObject().getId()));
						IModel<Content> model2 = new ObjectModel<Content>(content);
						Page page = RecycleBinConsole.this.getPage(model2);
						((AbstractApplicationPage<?>)page).setTopNavigation(new ContentNavigationBar<Content>(model2));
						return page;
					}
				};
				
				
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new RecycleBinBC());
				list.add(new BCElement(new StringResourceModel("recyclebin.batch.delete", RecycleBinConsole.this, null)));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}
		});
		
		
		
		
		return this.selection_toolbar;
	}
	

	/**
	 *
	 * 
	 * 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {

		List<ToolbarItem> items = new ArrayList<ToolbarItem>();

		items.add(new RecycleSourceSelector(browser, ToolbarItem.Align.TOP_LEFT));
		
	
		items.add(new LinkButton(browser, ToolbarItem.Align.TOP_LEFT, false, new StringResourceModel("recyclebin.tools.empty", this, null)) {
			
			@Override
			public IModel<String> getLinkCss() {
				return new Model<String>("btn btn-default btn-md");
			}
			
			@Override
			public boolean isVisible() {
				return isAdminUser();
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			public String getTarget() {
				return "_blank";
			}

			@Override
			public void onClick() {
				RecycleBinCleanUpCommand command = new RecycleBinCleanUpCommand();
				command.setDaysInternalFiles(Long.valueOf(0));
				command.setHoursExternalFiles(Long.valueOf(0));
				command.setDomain(getDomain());
				command.setDomainId(getDomain().getId());
				command.setUserId(getSessionUser().getId());
				ServiceLocator.getService(CommandService.class).register(command);
				command.execute();
				setResponsePage(new CommandPage(new CommandModel(command)));
			}
		});
		return items;
	}

	protected List<IModel<Content>> getDeleteableSelection() {
		List<IModel<Content>> selection = new ArrayList<IModel<Content>>();
		for (IModel<Content> model : getBrowser().getSelection()) {
			selection.add(new DeleteableModel(model.getObject()));
		}
		return selection;
	}

	
	protected void addModals () {
		super.addModals();
		addOrReplace(new AsyncExecutorModal("executor-dialog") {
			@Override
			public IModel<String> getTitle() {
				return getConsoleLabel("recyclebin.empty.title");
			}
			@Override
			protected IModel<String> getConfirmationMessage() {
				return getConsoleLabel("recyclebin.empty.confirmation");
			}
			@Override
			protected IModel<String> getExecutionMessage() {
				return getConsoleLabel("recyclebin.empty.execution");
			}
			@Override
			protected AsyncCommand getCommand() {
				return new EmptyRecycleBinCommand(getDomain());
			}
		});
	}
	

	protected boolean isReadOnly() {
		return true;
	}
}

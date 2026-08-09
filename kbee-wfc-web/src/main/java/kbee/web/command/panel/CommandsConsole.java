package kbee.web.command.panel;




import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.model.Classifier;

//import com.novamens.content.web.console.markup.TargetBlankObjectTitleColumnPanel;
//import com.novamens.content.web.object.markup.ObjectAuditModal;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.CommandListQuery;
import com.novamens.kbee.content.command.CommandProxy;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;

import com.novamens.kbee.wicket.markup.html.console.grid.SimpleDateColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.grid.TargetBlankObjectTitleColumnPanel;
import kbee.web.model.object.ObjectAuditModal;


public abstract class CommandsConsole extends AbstractFacetedConsole<Command> {
						
	private static final long serialVersionUID = 1L;
	
	final boolean is_root		  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_support	  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_domain_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandsConsole.class.getName());
												
	
	private List<GridColumn<SearchResult,String>> columns;
	private List<ToolbarItem> items;

	private DateTimeFormatter date_formatter;
	
	private CommandService service = null;
	
	public CommandsConsole(Query query) {
		super("commands", query);
	}
	
	
	@Override
	protected String getIcon(IModel<Command> model) {
		return null;
	}


	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.columns!=null)
			getColumns().forEach(item -> item.detach());
		
		this.service = null;
		
		this.date_formatter=null;
		
		if (this.items!=null) 
			this.items.forEach(item -> item.detach());
	}

	@Override
	public Query newQuery() {
		return  setUserPreference(new CommandListQuery());
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	
	/**
	 * 
	 * 
	 * return new WorkLoadHitExpandedPanel("editor", model);
	 */
	protected Panel getPanel(IModel<Command> model) {
		return new CommandHitExpandedPanel("editor", model);
	}
	
	protected Panel getPanel(IModel<Command> model, List<String> snippets) {
		return new CommandHitExpandedPanel("editor", model);
	}
	
	/** 
	 * Browser toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Command> browser) {
		if (this.items!=null)
			return this.items;
		this.items = new ArrayList<ToolbarItem>();
		
		/**
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return CommandsConsole.this.getName();}, new Model<String>(CommandsConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		**/
		
		return items;
	}
	
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}

	
	@Override
	protected IModel<Command> getModel(Command object) {
		return new CommandModel(object);
	}
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("commandsconsole.column.name"), "name") {
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Command> objectmodel = new CommandModel((Command)object);
				cellItem.add(new TargetBlankObjectTitleColumnPanel<Command>(componentId, objectmodel) {
					private static final long serialVersionUID = 1L;
					@Override
					protected String getCss() {
						return "cell-label btn-link";
					}
				});
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});
		

		this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("commandsconsole.column.status"), "status") {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getState().getLabel());
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			
			@Override
			protected String getLabelCss(IModel<SearchResult> model) {
				if (model.getObject()==null || model.getObject().getObject()==null) 
					return null;
				return ((Command) model.getObject().getObject()).getState().getCss();
			}
			
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});

		
		this.columns.add(new SimpleDateColumn<Command>("started", getLabel("commandsconsole.column.started"), "start") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
			
			protected OffsetDateTime getOffsetDateTime(Command object) {
				if (object==null)
					return null;
				return object.getDateCreated();
			}
		});


		this.columns.add(new SimpleDateColumn<Command>("terminated", getLabel("commandsconsole.column.terminated"), null) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
			protected OffsetDateTime getOffsetDateTime(Command object) {
				if (object==null)
					return null;
				return object.getDateTerminated();
			}
		});

		
		
		// progress
		//
		this.columns.add(new GridColumn<SearchResult, String>("progress", getLabel("commandsconsole.column.progress")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					NumberFormat nf = NumberFormat.getInstance(getUser().getLocale());
					nf.setMinimumFractionDigits(2);
					nf.setMaximumFractionDigits(2);
					nf.setRoundingMode(RoundingMode.HALF_UP);
					return new Model<String>( nf.format((((Command)result.getObject()).getProgress())) +" %");
					
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected String getLabelCss() {
					return "number-xxl";
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}
		});

		
		
		
		this.columns.add(new GridColumn<SearchResult, String>("class", getLabel("commandsconsole.column.class")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((CommandProxy)result.getObject()).getObject().getClass().getName());
				} catch (Exception e) {	
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});
																

		this.columns.add(new GridColumn<SearchResult, String>("total-items", getLabel("commandsconsole.column.total-items")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {							
					return new Model<String>(String.valueOf(((Command)result.getObject()).getTotalItems()));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});


		this.columns.add(new GridColumn<SearchResult, String>("total-items", getLabel("commandsconsole.column.total-items-processed")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {							
					return new Model<String>(String.valueOf(((Command)result.getObject()).getTotalItemsProcessed()));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});

		
		// estimated end time
		//														
		this.columns.add(new GridColumn<SearchResult, String>("estimated-end", getLabel("commandsconsole.column.estiamted-end")) {
					private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult result) {
						if (result.getObject()==null) 
							return new Model<String>("err");
						try {
							if (((Command)result.getObject()).isTerminated())
								return new Model<String>("");
							DateTimeService service = ServiceLocator.getService(DateTimeService.class);
							long msecs = Double.valueOf(((Command)result.getObject()).estimatedSecsToEnd()).longValue() * 1000;
							String ssecs= service.formatLapseSeconds(msecs, getSessionUser().getLocale());
							return new Model<String>(ssecs);
							
						} catch (Exception e) {			
							logger.error(e);
							return new Model<String>(e.getClass().getSimpleName());
						}
					}
					@Override
					protected String getContextKey() {
						return CommandsConsole.this.getName() + super.getContextKey();
					}
					@Override
					public String getCssClass() {
							return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
					}
				});
				



		
		this.columns.add(new GridColumn<SearchResult, String>("parameters", getLabel("commandsconsole.column.parameters")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getParameters().toString());
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});

		
		this.columns.add(new GridColumn<SearchResult, String>("result", getLabel("commandsconsole.column.result")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getResult());
				} catch (Exception e) {								
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});
		

		this.columns.add(new GridColumn<SearchResult, String>("comment", getLabel("commandsconsole.column.comment")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getResultComment());
				} catch (Exception e) {								
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});
		

		
		
		
		this.columns.add(new GridColumn<SearchResult, String>("description", getLabel("commandsconsole.column.description")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getDescription());
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});
		
		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("commandsconsole.column.id")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				if (result.getObject()==null) 
					return new Model<String>("err");
				try {
					return new Model<String>(((Command)result.getObject()).getId().toString());
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return CommandsConsole.this.getName() + super.getContextKey();
			}
		});

		return this.columns;
	}


	
	
	
	@SuppressWarnings("serial")
	@Override
	protected Panel getMenu(IModel<Command> model) {
		
		ContextMenuPanel<Command> menu = new ContextMenuPanel<Command>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<Command>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Command> getItem(String id) {
				return new LinkMenuItemPanel<Command>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
							return true;
					}
					@Override 
					public String getLabel() {
						return CommandsConsole.this.getLabel("contextmenu.open").getObject();
					}
					
					public String getTarget() {
						return "_bank";
					}
					
					public void onDetach() {
						super.onDetach();
						getModel().detach();
					}
					
					@Override
					public void onClick() throws Exception {
						try { 
							setResponsePage(new CommandPage(getModel()));
							getModel().detach();
						} 
						catch (Exception e) {
							getModel().detach();
							logger.error(e);
						}
					}
				};
			}
		});
		

		menu.addItem(new MenuItemFactory<Command>() {
			@Override
			public AbstractMenuItemPanelV5<Command> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Command>(id) {
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

		
		menu.addItem(new MenuItemFactory<Command>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Command> getItem(String id) {
				return new AjaxMenuItemPanelV5<Command>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
							return getModel().getObject().getState()==CommandState.RUNNING;
					}
					@Override 
					public String getLabel() {
						return CommandsConsole.this.getLabel("contextmenu.stop").getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try { 
							getModel().getObject().stop();
						} 
						catch (Exception e) {
							logger.error(e);
						}
						CommandsConsole.this.refresh(target);
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Command>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Command> getItem(String id) {
				return new AjaxMenuItemPanelV5<Command>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
							return getModel().getObject().getState()!=CommandState.RUNNING;
					}
					@Override 
					public String getLabel() {
						return CommandsConsole.this.getLabel("contextmenu.remove").getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try { 
							getCommandService().remove((Long) getModel().getObject().getId());
						} 
						catch (Exception e2) {
							logger.error(e2);
						}
						CommandsConsole.this.refresh(target);
					}
				};
			}
		});

		return menu;
	}

		
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}

	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	// protected abstract Page getConsolePage(Query query, long index);
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});
		
		
		add(new WicketEventListener<ClickEvent<Command>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Command> event) {
				
			}
		});
	}
	

	protected DateTimeFormatter getDateTimeFormatter() {
		if (this.date_formatter!=null)
			return this.date_formatter; 
		this.date_formatter = DateTimeFormatter.ofPattern("d MMM HH:mm:ss", getSessionUser().getLocale());
		return this.date_formatter; 
	}
	
	
	

	
	private CommandService getCommandService() { 
		if (this.service==null) { 
			try {
				this.service = (CommandService) ServiceLocator.getService(CommandService.class);
			} catch (RuntimeException e) {
				logger.error(e);
				return null;
			}
		}
		return this.service;
	}
	
}

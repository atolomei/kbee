package com.novamens.content.web.security.markup;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.Enumeration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.library.Library;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.RolesBC;
import kbee.web.nav.SecurityBC;
import kbee.web.nav.UsersBC;
import kbee.web.query.SolrRulesQuery;


@SuppressWarnings("serial")
public abstract class RulesConsole extends AbstractFacetedConsole<IQLRule> {
	private static final long serialVersionUID = 1L;
															
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RulesConsole.class.getName());
								
	final boolean is_support	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	private Locale user_locale;
	private  ZoneId user_zoneid;

	List<GridColumn<SearchResult,String>> columns;
	Map<Serializable, Command> list_command;
	
	
	public RulesConsole(Query query) {
		super("rules", query);
	}
	
	
	@Override
	 protected  IModel<IQLRule> getModel(IQLRule object) {
			return new ObjectModel<IQLRule>(object, true);
	}
	
    @Override
	protected String getIcon(IModel<IQLRule> model) {
		return null;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		/**
		MenuBreadCrumbPanel bc = new MenuBreadCrumbPanel();
			 
		DropdownMenuBC dd = new DropdownMenuBC();
		dd.addElement(new SecurityBC(), true);
		dd.addElement(new UsersBC());
		dd.addElement(new RolesBC());
		//dd.addElement(new GroupsBC());
		//dd.addElement(new RulesBC());
		bc.addElement(dd);
		 
		bc.addElement(new BCElement("rules"));
		add(bc);
		*/
		
		user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
		if (user_zoneid==null) 
			user_zoneid=ZoneId.systemDefault();
		user_locale = getSessionUser().getLocale();
	}
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
		list_command=null;
	}
	

	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new SolrRulesQuery(getQueryIndex()));
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	// protected abstract Page getConsolePage(Query query, long index);

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	};

	@Override
	protected Panel getMenu(IModel<IQLRule> model) {
		
		ContextMenuPanel<IQLRule> menu = new ContextMenuPanel<IQLRule>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<IQLRule>() {
			@Override
			public AbstractMenuItemPanelV5<IQLRule> getItem(String id) {
				return new AjaxMenuItemPanelV5<IQLRule>(id) {
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(RulesConsole.this.getPage(getModel(), RulesConsole.this.getIndex(getModel().getObject()), false));
					}
					@Override 
					public String getLabel() {
						return RulesConsole.this.getLabel("rulesconsole.contextmenu.open").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<IQLRule>() {
			@Override
			public AbstractMenuItemPanelV5<IQLRule> getItem(String id) {
				return new AjaxMenuItemPanelV5<IQLRule>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = RulesConsole.this.getAuditTrailModal();
						((ObjectAuditModal<IQLRule>)modal).open(target, getModel(), true);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("rulesconsole.contextmenu.audittrail").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<IQLRule>() {
			@Override
			public AbstractMenuItemPanelV5<IQLRule> getItem(String id) {
				return new SeparatorMenuItemPanelV5<IQLRule>(id) {
					private static final long serialVersionUID = 1L;
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
		
		menu.addItem(new MenuItemFactory<IQLRule>() {
			@Override
			public AbstractMenuItemPanelV5<IQLRule> getItem(String id) {
				return new AjaxMenuItemPanelV5<IQLRule>(id) {
					public void onClick(AjaxRequestTarget target) {
							getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
										}
										catch (DataIntegrityViolationException e) {
											 logger.error(e, getSessionUser().getUserName());
											if (getModel().getObject().isDerived()) {
												String objectid=getModel().getObject().getParentObjectId();
												String dname = null;
												if (objectid!=null) {
													try {
														Object object = getContentDao().findObjectById(new ObjectId(objectid));
														if (object instanceof com.novamens.dom.Object) 
																dname = ((com.novamens.dom.Object) object).getDisplayName();
													} catch (ContentMgmtException e1) {
														logger.error(e, getSessionUser().getUserName());
													}
												}
												if (dname!=null)
													getErrorDialog().open(target, getConsoleLabel("error.constraint.security-handler-name", dname));
												else
													getErrorDialog().open(target, getConsoleLabel("error.constraint.security-handler"));
											}
											else
												getErrorDialog().open(target, getConsoleLabel("error.constraint"));
										}
										catch (Exception e) {
											logger.error(e, getSessionUser().getUserName());
											getErrorDialog().open(target, new Model<String>(e.getMessage()));
										}
										RulesConsole.this.refresh(target);
									}
								}
							});
//						}
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						if (is_support && !is_root)
							return false;
						if (is_root)
							return true;
						if (getModel().getObject().isDerived()) 
							return false;
						return true;
					}
				};
			}
		});
		
		return menu;
	}


	/**
	 * 
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		{
			LinkPredicateKbeeGridColumn<IQLRule> titleColumn =
					new LinkPredicateKbeeGridColumn<>("title", getLabel("rulesconsole.column.name"), "title_sort",
							obj -> (obj).getName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}


		this.columns.add(new LastModifiedColumn<IQLRule>("modified", getLabel("rulesconsole.column.modified"), "modified") {
							 private static final long serialVersionUID = 1L;

							 @Override
							 protected String getContextKey() {
								 return RulesConsole.this.getName() + super.getContextKey();
							 }
						 }
		);

		{
			KbeePredicateGridColumn<IQLRule> statusColumn = new KbeePredicateGridColumn<IQLRule>("status", getLabel("rulesconsole.column.status"),
					obj -> getStatusColumnModel(obj,true).getObject()){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						IQLRule rule = (IQLRule) result.getObject();
						if (rule==null)
							return new Model<String>("err");
						return getStatusColumnModel(rule,false);
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setPreferred(false);
			this.columns.add(statusColumn);
		}


		
		this.columns.add(new GridColumn<SearchResult, String>("role", getLabel("rulesconsole.column.isrole")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				boolean isrole = ((IQLRule)object.getObject()).isDerived();
				return getStringModel(isrole,true);
				
			}

			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				boolean isrole = ((IQLRule)result.getObject()).isDerived();
				return getStringModel(isrole,false);
			}


			private Model<String> getStringModel(boolean isrole, boolean html) {
				String value;
				if(html)
					value = ("<span class=\"" + (isrole ? "yes" : "no") + "\">")
							+ (new StringResourceModel(isrole ? "yes" : "no", RulesConsole.this, null).getString())
							+ "</span>";
				else
					value=isrole ? "yes" : "no";
				return new Model<String>(value);
			}

			@Override
			protected String getContextKey() {
				return RulesConsole.this.getName() + super.getContextKey();
			}			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});



		{
			KbeePredicateGridColumn<IQLRule> modifiedByColumn = new KbeePredicateGridColumn<>("modifiedby", getLabel("rulesconsole.column.modifiedby"),
					obj -> String.valueOf((obj.getLastModifiedUser().getFirstLastName())));
			modifiedByColumn.setContextKey(this.getName() + modifiedByColumn.getContextKey());
			modifiedByColumn.setPreferred(false);
			this.columns.add(modifiedByColumn);
		}

		{
			KbeePredicateGridColumn<IQLRule> conditionColumn = new KbeePredicateGridColumn<IQLRule>("condition", getLabel("rulesconsole.column.condition"),
					obj -> obj.getDisplayCondition()){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						IQLRule rule = (IQLRule) result.getObject();
						if (rule==null)
							return new Model<String>("err");
						return ()-> rule.getCondition();
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			conditionColumn.setContextKey(this.getName() + conditionColumn.getContextKey());
			conditionColumn.setPreferred(false);
			this.columns.add(conditionColumn);
		}

		{
			KbeePredicateGridColumn<IQLRule> aclColumn = new KbeePredicateGridColumn<>("acl", getLabel("rulesconsole.column.acl"),
					obj ->  getAclDisplayIModel(obj).getObject());
			aclColumn.setContextKey(this.getName() + aclColumn.getContextKey());
			this.columns.add(aclColumn);
		}

		{
			KbeePredicateGridColumn<IQLRule> idColumn = new KbeePredicateGridColumn<>("id", getLabel("rulesconsole.column.id"),
					obj -> String.valueOf(obj.getId()));
			idColumn.setPreferred(false);
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}

		return this.columns;
	}


	private IModel<String> getAclDisplayIModel(IQLRule rule) {
		StringBuilder str = new StringBuilder();
		Acl acl = rule.getAcl();
		Enumeration<AclEntry> entries = acl.entries();
		while (entries.hasMoreElements()) {
			AclEntry aclentry = entries.nextElement();
			if (str.length()>0)
				str.append(" | ");
			str.append(aclentry.getPrincipal().getName()+" ( ");
			Enumeration<Permission> acle = aclentry.permissions();
			int n = 0;
			while (acle.hasMoreElements()) {
				if (n++>0)
					str.append(", ");
				str.append(acle.nextElement().toString());
			}
			str.append(" ) ");
		}
		String out = (str.length()==0?"-":str.toString());
		return new Model<String>( out);
	}

	private IModel<String> getStatusColumnModel(IQLRule rule, boolean html) {
		IModel<String> status;
		
		if(html)
			status = getLabel("enabled");
		else
			status = new Model<String>("enabled");

		for (Command command :getCommands().values()) {
			if (CommandState.RUNNING.equals(command.getState()) &&
				command.getParameter("rule")!=null &&
				rule.getId().equals(command.getParameter("rule"))) {
				if(html)
					status = getLabel("indexing");
				else
					status = new Model<String>("indexing");

				break;
			}
		}
		return status;
	}

	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<IQLRule>("audit-trail-modal"));
	}
	
	protected Page getPage(IModel<IQLRule> model, long index, boolean isnew) {
		
																										
		GlobalNavigationBar<IQLRule> navigationbar = new GlobalNavigationBar<IQLRule>("navigation",  getDisplayName().getObject()) {
		// GlobalNavigationBar<IQLRule> navigationbar = new GlobalNavigationBar<IQLRule>("navigation", getSearcher(), index, getDisplayName().getObject()) {
			@Override
			public void onNavigate(IQLRule rule) {
				Page page = new RulePage(new ObjectModel<IQLRule>(rule), this, false);
				setResponsePage(page);
			}
			@Override
			protected void onReturn(AjaxRequestTarget target) {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			
			@Override
			public void onDetach() {
				super.onDetach();
				RulesConsole.this.onDetach();
			}
			
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
		//navigationbar.setIsNotes(false);
		//navigationbar.setIsAlerts(false);
		Page page = new RulePage(model, navigationbar, isnew);
		return page;
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
		
		add(new WicketEventListener<ClickEvent<IQLRule>>() {
			@Override
			public void onEvent(ClickEvent<IQLRule> event) {
				setResponsePage(RulesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	
	
	/**
	 * 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<IQLRule> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		items.add(new NewRuleButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected void onCreate(int type) {
				try {
					Object rule = ServiceLocator.getService(SecurityContentMgmtService.class).createRule(type);
					Page page = RulesConsole.this.getPage(RulesConsole.this.getModel((IQLRule)rule), 0, true);
					setResponsePage(page);
				}
				catch (ContentCreationException e) {
					logger.error(e, getSessionUser().getUserName());
					throw new KbeeRuntimeException(e);
				}
			}
		});
		
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return RulesConsole.this.getName();}, new Model<String>(RulesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		
		
		items.add(infoButton);
		
		return items;
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	} 
	
	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		if (dt==null)
			return new Model<String>("err");
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
		return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
	}
	
	@Override
	protected Panel getPanel(IModel<IQLRule> model) {
		return new ExpandedPanel<IQLRule>("editor", this, model);
	}
	

	@Override
	protected Panel getPanel(IModel<IQLRule> model, List<String> snippets) {
		return new ExpandedPanel<IQLRule>("editor", this, model, snippets);
	}
	
	private Map<Serializable, Command> getCommands() {
		if (list_command!=null)
			return list_command; 
		list_command = ServiceLocator.getService(CommandService.class).getCommands();
		return list_command; 
	}
}
   
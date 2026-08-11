package kbee.web.security.role;


import java.time.OffsetDateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.novamens.kbee.wicket.markup.html.console.grid.*;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;

import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.system.SystemParameter;
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
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.RolesBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.query.RolesQuery;

import org.danekja.java.util.function.serializable.SerializableSupplier;



@SuppressWarnings("serial")
public abstract class RolesConsole extends AbstractFacetedConsole<Role> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RolesConsole.class.getName());
	
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
	final boolean is_security = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());;
	final boolean is_su = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SU.getId());

	private boolean is_deleted_visible = false;
	
	private List<GridColumn<SearchResult,String>> columns;
	
	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_toolbar;
	
	private Locale user_locale;
	private ZoneId user_zoneid;
	
	public RolesConsole(Query query) {
		super("roles", query);
		this.setDeletedVisible(getUserPreference("deleted-visible", "no").equals("yes"));
		this.setOutputMarkupId(true);
		setListBrowser(true);
	}
	

    @Override
	protected String getIcon(IModel<Role> model) {
		return null;
	}

    
	@Override
	 protected  IModel<Role> getModel(Role object) {
			return new ObjectModel<Role>(object, true);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}

	@Override
	public Query newQuery() {
		return setUserPreference(new RolesQuery(getQueryIndex()));
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new RolesBC());
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	@Override
	protected Panel getMenu(IModel<Role> model) {
		ContextMenuPanel<Role> menu = new ContextMenuPanel<Role>(model);
		menu.setOutputMarkupId(true);
	
		menu.addItem(new MenuItemFactory<Role>() {
			@Override
			public AbstractMenuItemPanelV5<Role> getItem(String id) {
				return new AjaxMenuItemPanelV5<Role>(id) {
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(RolesConsole.this.getPage(getModel(), RolesConsole.this.getIndex(getModel().getObject()), false, false));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("rolesconsole.contextmenu.open").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Role>() {
			@Override
			public AbstractMenuItemPanelV5<Role> getItem(String id) {
				return new AjaxMenuItemPanelV5<Role>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = RolesConsole.this.getAuditTrailModal();
						((ObjectAuditModal<Role>)modal).open(target, getModel(), true);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("rolesconsole.contextmenu.audittrail").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Role>() {
			@Override
			public AbstractMenuItemPanelV5<Role> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Role>(id) {
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
		

		menu.addItem(new MenuItemFactory<Role>() {
			@Override			
			public AbstractMenuItemPanelV5<Role> getItem(String id) {
				return new AjaxMenuItemPanelV5<Role>(id) {
					public void onClick(AjaxRequestTarget target) {		
						getConfirmationDialog().open(target, getConsoleLabel("rolesconsole.contextmenu.delete.confirmation", getModel().getObject().getName()), Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									executeDelete(target);
									RolesConsole.this.refresh(target);
								}
							}
						});
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("rolesconsole.contextmenu.delete").getObject();
					}
					@Override
					public boolean isEnabled() {
						
						if (is_support && !is_root)
							return false;
						
						if (!is_admin || !is_root)
							return false;
						
						return getContentSecurityDao().isDeletable(getModel().getObject());
						
						
					}
					@Override
					public boolean isVisible() {
						return true;
					}

					protected void executeDelete(AjaxRequestTarget target) {
						try {
							ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
						}
						catch (Exception e1) {
							try {
								getModel().detach();
							}
							catch (Exception e2) {
								logger.error(e2, getSessionUser().getUserName());
							}	
						}	
					}
				};
			}
		});
		
		return menu;
	}

 	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
	
		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), new Model<String>("St")));

		LinkPredicateKbeeGridColumn<Role> titleColumn = new LinkPredicateKbeeGridColumn<Role>("title",getLabel("rolesconsole.column.name"),"title_sort", obj->obj.getName(), col->getModel(col));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		titleColumn.setTarget(null);
		
		columns.add(titleColumn);

		this.columns.add(new GridColumn<SearchResult, String>("type", getLabel("rolesconsole.column.type")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				try {
					KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
					if (role==null)
						return new Model<String>("err");
					return getTypeDisplayModel(role,true);

				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass() + "|" +e.getMessage());
				}
			}
			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				try {
					KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
					if (role==null)
						return new Model<String>("err");

					return getTypeDisplayModel(role,false);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass() + "|" +e.getMessage());
				}
			}


			private IModel<String> getTypeDisplayModel(KbeeAbstractRole role, boolean html) {
				String label;
				if (role instanceof EntityRole) {
						if (((EntityRole) role).getClassifier()!=null) {
							if(html)
								label = "<span> " + role.getRoleType() + " </span><span class=\"iql-group-start\">(</span> <span class=\"iql-value\">" + (((EntityRole) role).getClassifier().getDisplayName() != null ? ((EntityRole) role).getClassifier().getDisplayName() : "") + "</span> <span class=\"iql-group-end\">)</span>";
							else
								label =  role.getRoleType() + " (" + (((EntityRole) role).getClassifier().getDisplayName() != null ? ((EntityRole) role).getClassifier().getDisplayName() : "") + ")";
						}else {
							if(html)
								label = "<span> " + role.getRoleType() + " </span><span class=\"iql-group-start\">(</span><span class=\"iql-group-end\">)</span>";
							else
								label = role.getRoleType();
						}
				}
				else {
					label = role.getRoleType();
				}

				if(html)
					label ="<span class=\"cell-label\">" + label + "</span>";

				return new Model<String>(label);
			}



			@Override
			public boolean isPreferred() {
				return true;
			}

			@Override
			public String getCssClass() {
				return "col col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return RolesConsole.this.getName() + super.getContextKey();
			}
		});

		{
			KbeePredicateGridColumn<KbeeAbstractRole> elementsColumn = new KbeePredicateGridColumn<>("members", getLabel("members"),	obj ->  String.format("%10d", getTotalMembers(obj)));
			elementsColumn.setContextKey(this.getName() + elementsColumn.getContextKey());
			elementsColumn.setCssValueResolver(obj ->   getNumberClass(obj));
			elementsColumn.setDefaultWidth(154);
			elementsColumn.setHeaderCssClass("centered");
			elementsColumn.setLabelCss("number-md");
			this.columns.add(elementsColumn);
		}
		{
			KbeePredicateGridColumn<KbeeAbstractRole> conditionColumn = new KbeePredicateGridColumn<KbeeAbstractRole>("condition", getLabel("rolesconsole.column.condition"), obj -> getDisplayCondition(obj) ){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
						if (role==null)
							return new Model<String>("err");
						return ()-> role.getCondition();
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			conditionColumn.setPreferred(false);
			conditionColumn.setContextKey(this.getName() + conditionColumn.getContextKey());
			this.columns.add(conditionColumn);
		}

		
		/**
		{
			KbeePredicateGridColumn<KbeeAbstractRole> apiColumn = new KbeePredicateGridColumn<KbeeAbstractRole>("api", getBrandLabel("isapi"),
					obj -> new 	StringResourceModel(obj.isApiEnabled() ?"yes":"no", this, null).getObject() ){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
						if (role==null)
							return new Model<String>("err");
						return ()-> role.isApiEnabled() ?"yes":"no";
					} catch (Exception e) {
						kblogger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			apiColumn.setPreferred(true);
			apiColumn.setContextKey(this.getName() + apiColumn.getContextKey());
			this.columns.add(apiColumn);
		}**/


		{
			KbeePredicateGridColumn<KbeeAbstractRole> apiColumn = new KbeePredicateGridColumn<KbeeAbstractRole>("isentityadmin", getLabel("rolesconsole.column.administrator"),
					obj -> new 	StringResourceModel(obj.isAdministrator() ?"yes":"no", this, null).getObject()){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
						if (role==null)
							return new Model<String>("err");
						return ()-> role.isAdministrator() ?"yes":"no";
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			apiColumn.setPreferred(true);
			apiColumn.setContextKey(this.getName() + apiColumn.getContextKey());
			this.columns.add(apiColumn);
		}

		
		
		{
			KbeePredicateGridColumn<KbeeAbstractRole> apiColumn = new KbeePredicateGridColumn<KbeeAbstractRole>("isdefault", getLabel("rolesconsole.column.isdefault"),
					obj -> new 	StringResourceModel(obj.isDefault() ?"yes":"no", this, null).getObject()){
				@Override
				public IModel<String> getCellAsString(SearchResult result) {
					try {
						KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
						if (role==null)
							return new Model<String>("err");
						return ()-> role.isDefault() ?"yes":"no";
					} catch (Exception e) {
						logger.error(e, getSessionUser().getUserName());
						return new Model<String>(e.getClass() + "|" +e.getMessage());
					}
				}
			};
			apiColumn.setPreferred(true);
			apiColumn.setContextKey(this.getName() + apiColumn.getContextKey());
			this.columns.add(apiColumn);
		}

		this.columns.add(new GridColumn<SearchResult, String>("groups", getLabel("rolesconsole.column.groups")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
				return getDisplayIModel(role,true);
			}

			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				KbeeAbstractRole role = (KbeeAbstractRole) result.getObject();
				return getDisplayIModel(role,false);
			}


			private IModel<String> getDisplayIModel(KbeeAbstractRole role, boolean html) {
				try {
					if (role==null)
						return new Model<String>("err");
					List<Group> list = role.getGroupsList();
					StringBuilder str = new StringBuilder();
					for (Group g: list) {
						if (str.length()>0) {
							if(html)
								str.append("<span class=\"ago\"> | </span>");
							else
								str.append(" | ");
						}
						str.append(g.getDisplayName());
					}
					return new Model<String>(str.toString());

				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					return new Model<String>(e.getClass().getName());
				}
			}


			@Override
			protected String getContextKey() {
				return RolesConsole.this.getName() + super.getContextKey();
			}
			
			// only in HitPanel
			//
			@Override
			public boolean isOnlyForExpandedHitPanel() {
				return true;
			} 
			
			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		this.columns.add(new LastModifiedColumn<KbeeAbstractRole>("modified", getLabel("rolesconsole.column.modified"), "modified"){
			@Override
			protected String getContextKey() {
				return RolesConsole.this.getName() + super.getContextKey();
			}
		});

		{
			KbeePredicateGridColumn<KbeeAbstractRole> modifiedByColumn = new KbeePredicateGridColumn<>("modifiedby", getLabel("rolesconsole.column.modifiedby"), obj -> String.valueOf(obj.getLastModifiedUser().getFirstLastName()));
			modifiedByColumn.setPreferred(false);
			modifiedByColumn.setContextKey(this.getName() + modifiedByColumn.getContextKey());
			this.columns.add(modifiedByColumn);
		}
		{
			SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
			DateKbeeColumn<KbeeAbstractRole> executedColumn = new DateKbeeColumn<>("created", getLabel("rolesconsole.column.created"), "created", (obj) -> obj.getCreationOffsetDateTime(), formatSupplier);
			executedColumn.setPreferred(false);
			columns.add(executedColumn);
		}
		{
			KbeePredicateGridColumn<KbeeAbstractRole> scopeColumn = new KbeePredicateGridColumn<>("scope", getLabel("rolesconsole.column.scope"), obj -> getScopeColumnText(obj));
			scopeColumn.setPreferred(false);
			scopeColumn.setContextKey(this.getName() + scopeColumn.getContextKey());
			this.columns.add(scopeColumn);
		}
		{
			KbeePredicateGridColumn<KbeeAbstractRole> descriptionColumn = new KbeePredicateGridColumn<>("description", getLabel("rolesconsole.column.description"), obj -> obj.getDescription());
			descriptionColumn.setPreferred(false);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}
		{
			KbeePredicateGridColumn<KbeeAbstractRole> descriptionColumn = new KbeePredicateGridColumn<>("alias", getLabel("rolesconsole.column.alias"), obj -> obj.getAlias());
			descriptionColumn.setPreferred(false);
			descriptionColumn.setContextKey(this.getName() + descriptionColumn.getContextKey());
			this.columns.add(descriptionColumn);
		}

		return this.columns;
	}

	
	protected String getDisplayCondition(KbeeAbstractRole obj) {
		String s=obj.getDisplayCondition();
		
		try {
		if (obj.getType()==DomainRole.TYPE)
			return s;
		if (obj instanceof EntityRole) {
			Classifier c=((EntityRole) obj). getClassifier();
			if (c!=null) {
				StringBuilder str = new StringBuilder();
				str.append("<span class=\"predicate\">" + c.getName() + "</span> <span class=\"iql-value\">( <i> " + new StringResourceModel("datasetmember", this, null).getObject()+  "</i> )</span>");
				if (s!=null && s.length()>0) {
					str.append("  <b>AND</b>  ( " + s+ " )");
				}
				return str.toString();
			}
		}
		return s;
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
	}


	private IModel<String> getBrandLabel(String string) {
		SystemParameter str=getContentDao().findSystemParameterByKey(string);
		if (str!=null)
			return new Model<String>(str.getValue());
		return new StringResourceModel(string);
	}


	private String getScopeColumnText(Object object) {
		String label = "";
		if (object instanceof EntityRole) {
			if (((EntityRole)object).getClassifier()!=null) {
				label = ((EntityRole)object).getClassifier().getDisplayName();
			}
		}
		return label;
	}


	/***
	 * 
	 * @param dt
	 * @return
	 */
	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		if (dt==null)
			return new Model<String>("err");
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
		return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
	}
	

	protected Page getPage(IModel<Role> model, long index, boolean edition, boolean isNew) {
		Searcher searcher = getSearcher();
	    SolrCursor soc = new SolrCursor((SolrResultSet) searcher.getResultSet(), index);
		Page page = new RolePage(model,  new SolrCursorModel(soc));
		return page;
	}

	
	@Override
	protected Panel getPanel(IModel<Role> model, List<String> snippets) {
		return new ExpandedPanel<Role>("editor", this, model, null);
	}
	
	@Override
	protected Panel getPanel(IModel<Role> model) {
		return new ExpandedPanel<Role>("editor", this, model);
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();

		add(new WicketEventListener<ClickEvent<Role>>() {
			@Override
			public void onEvent(ClickEvent<Role> event) {
				setResponsePage(RolesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false, false));
			}
		});
	}
	
 	/**
 	 * Selected Users 
 	 * Bulk Actions
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Role> browser) {
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		this.selection_toolbar = new ArrayList<ToolbarItem>();		
		return this.selection_toolbar;
	}
	
	/**
	 *  Toolbar Actions
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Role> browser) {
	
		if (items!=null)
			return items;
		
		this.items = new ArrayList<ToolbarItem>();
		
		this.items.add(new NewRoleButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected void onCreate(int type, IModel<Classifier> model) {
				try {
					Object role = ServiceLocator.getService(SecurityContentMgmtService.class).createRole(type, model!=null ? model.getObject() : null);
					boolean isnew=true;
					Page page = RolesConsole.this.getPage(RolesConsole.this.getModel((Role)role), 0, true, isnew);
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
				infoDialog.open(target,() -> {return RolesConsole.this.getName();}, new Model<String>(RolesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		this.items.add(infoButton);
		
		
		return items;		
	}

	@Override
	protected boolean hasTopPanel() {
		return false;
	}

	protected long getTotalMembers(KbeeAbstractRole role) {
			return getContentSecurityDao().getTotalMembers(role);
	}
	
	@Override
	protected void addModals () {
		super.addModals();
		addOrReplace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
	
	@Override
	protected String getDescription() {
		StringBuilder str = new StringBuilder();
			str.append("<section>");
			str.append("<h3>Roles</h3>");
			String d=ServiceLocator.getService(LanguageService.class).getString("role-console-description", getSessionUser().getLocale(), null);
			str.append(d!=null?("<p>"+d+"</p>"):"");
			str.append("</section>");
		return str.toString();
	}
	
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		if (!isDeletedVisible())
			return null;
		try {
			if (((KbeeAbstractRole) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)
				return "deleted-state";
			return null;
		} 
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return null;
		}
	}
	
	protected void setDeletedVisible(boolean b) {
		this.is_deleted_visible=b;
		setUserPreference("deleted-visible", (b?"yes":"no"));
	}
	
	protected boolean isDeletedVisible() {
		return this.is_deleted_visible;
	}
	
	protected String getNumberClass(KbeeAbstractRole obj) {
		try {
		long ref=  getTotalMembers(obj);
		return ref>0?"col number-md info" : "col number-md";
		} catch (Exception e) {
			logger.error(e);
			return "number-md";
		}
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
}
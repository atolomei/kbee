package kbee.web.rule;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.entity.Person;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.rule.ActionRulesQuery;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DataManagementBC;
import kbee.web.object.ObjectStatusColumn;


/**
 * 
 * Time dependant actions
 * 
 * {@see KbeeActionRule}
 *
 */
@SuppressWarnings("serial")
public abstract class ActionRulesConsole extends AbstractFacetedConsole<ActionRule> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ActionRulesConsole.class.getName());
							
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private Locale user_locale;
	private ZoneId user_zoneid;

	private List<GridColumn<SearchResult,String>> columns = null;

	public ActionRulesConsole(Query query) {
		super("rules", query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	protected String getIcon(IModel<ActionRule> model) {
		return null;
	}

	protected  IModel<ActionRule> getModel(ActionRule object) {
		return new ObjectModel<ActionRule>(object, true);
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new ActionRulesQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	 

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new DataManagementBC());
	};

	@Override
	protected Panel getMenu(IModel<ActionRule> model) {
		
		ContextMenuPanel<ActionRule> menu = new ContextMenuPanel<ActionRule>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(id -> new AjaxMenuItemPanelV5<ActionRule>(id) {
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(ActionRulesConsole.this.getPage(getModel(), ActionRulesConsole.this.getIndex(getModel().getObject()), false));
				}
				@Override 
				public String getLabel() {
					return ActionRulesConsole.this.getLabel("rulesconsole.contextmenu.open").getObject();
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<ActionRule>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = ActionRulesConsole.this.getAuditTrailModal();
					((ObjectAuditModal<ActionRule>)modal).open(target, getModel(), true);
				}
				@Override 
				public String getLabel() {
					return getConsoleLabel("rulesconsole.contextmenu.audittrail").getObject();
				}
			}
		);
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<ActionRule>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return  true;
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<ActionRule>(id) {
				public void onClick(AjaxRequestTarget target) {
					getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, Button button) {
							if (button.key().equals(Dialog.Delete.key())) {
								try {
									((KbeeActionRule) getModelObject()).getService(DOMObjectService.class).delete();
								}
								catch (DataIntegrityViolationException e) {
									logger.error(e);
									getErrorDialog().open(target, getConsoleLabel("error.constraint"));
								}
								catch (Exception e) {
									logger.error(e);
									getErrorDialog().open(target, new Model<String>(e.getMessage()));
								}
								ActionRulesConsole.this.refresh(target);
							}
						}
					});
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
					return true;
				}
			}
		);
		
		return menu;
	}

	/**
	 * Note that the Query is Hibernate Query
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), getLabel("st")));
		
		LinkPredicateKbeeGridColumn<ActionRule> titleColumn =
			new LinkPredicateKbeeGridColumn<ActionRule>("title", getLabel("rulesconsole.column.name"), "title_sort", rule->rule.getName(), rule->getModel(rule));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		columns.add(titleColumn);

		this.columns.add(new LastModifiedColumn<ActionRule>("modified", getLabel("rulesconsole.column.modified"), "modified") {
			@Override
			protected String getContextKey() {
				return ActionRulesConsole.this.getName() + super.getContextKey();
			}
		});
		
		KbeePredicateGridColumn<ActionRule> conditionColumn = new KbeePredicateGridColumn<>("condition", getLabel("rulesconsole.column.criteria"),  (rule) -> ((ActionRule)rule).getDisplayCondition());
		conditionColumn.setContextKey(this.getName() + conditionColumn.getContextKey());
		this.columns.add(conditionColumn);

		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<ActionRule> createdColumn = new DateKbeeColumn<ActionRule>("created", getLabel("created"), (obj)-> obj.getCreationOffsetDateTime(), formatSupplier);
		createdColumn.setContextKey(this.getName() + createdColumn.getContextKey());
		this.columns.add(createdColumn);

		{							
			KbeePredicateGridColumn<ActionRule> statusColumn = new KbeePredicateGridColumn<ActionRule>("status", getLabel("status"), obj ->  obj.getState() != null ? obj.getState().getLabel(getSessionUser().getLocale()) : "err" );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getSessionUser().getLocale()) : "err");
			statusColumn.setTextValueResolver((rule) ->rule.getState().getLabel(getSessionUser().getLocale()));
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setPreferred(false);
			this.columns.add(statusColumn);
		}

		{							
			KbeePredicateGridColumn<ActionRule> contentColumn = new KbeePredicateGridColumn<ActionRule>("content",	getLabel("rulesconsole.column.content"));
			contentColumn.setHtmlValueResolver(obj -> obj.isContentRule() ? 
			((obj.getContentOId() != null) ?  getContentLink(obj.getContentOId()) : "n/a" )
				: ""
			);
			contentColumn.setTextValueResolver(
					obj -> obj.isContentRule() ? 
							((obj.getContentOId() != null) ?  getContentTitle(obj.getContentOId()) : "n/a" )
								: ""
							);
					
			contentColumn.setContextKey(this.getName() + contentColumn.getContextKey());
			contentColumn.setPreferred(false);
			this.columns.add(contentColumn);
		}
		
		GridColumn<SearchResult, String> exec = new DateColumn<ActionRule>("execdate", getLabel("rulesconsole.column.execdate"), null) {
			@Override
			protected OffsetDateTime getOffsetDateTime(ActionRule object) {
				try {
					OffsetDateTime time = object.getExecutionDate();
					System.out.println(time);
					return time;
				} 
				catch (Exception e) {
					logger.error(e);
					return null;
				}
			}
		};
		
		this.columns.add(exec);
		

        this.columns.add(new LastModifiedColumn<ActionRule>("modifiedby", getLabel("modifiedby"), "modified") {
            @Override
            protected String getContextKey() {
                return ActionRulesConsole.this.getName() + super.getContextKey();
            }
       	});


        /**
        this.columns.add(new GridColumn<SearchResult, String>("modifieduser1", getLabel("user")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(String.valueOf(((ActionRule) object.getObject()).getLastModifiedUser().getFirstLastName()));
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());

                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
            @Override
            protected String getContextKey() {
                return ActionRulesConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });
        **/

        
        
        
        
        
        
        
		KbeePredicateGridColumn<ActionRule> idColumn = new KbeePredicateGridColumn<>("id", getLabel("rulesconsole.column.id"),  (obj) -> String.valueOf(obj.getId()));
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		idColumn.setPreferred(false);
		this.columns.add(idColumn);

		return this.columns;
	}
	
	 
	private String getContentTitle(Long contentOid) {
		
		if (contentOid==null)
			return null;
		
		Content c = getContentDao().findContentByOId(contentOid);
		
		if (c==null)
			return "";
		return c.getDisplayName();
	}
	
	private String getContentLink(Long contentOid) {
		Content c = getContentDao().findContentByOId(contentOid);
		if (c==null)
			return "";
		return "<a href=\"" + getServerUrl() + "/id/" + c.getOId().toString() +  "\" class=\"btn btn-link\" target=\"_blank\"> "  + c.getDisplayName() + "</a>";
	}

	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<ActionRule>("audit-trail-modal"));
	}
	
	protected Page getPage(IModel<ActionRule> model, long index, boolean isnew) {
																												
		GlobalNavigationBar<ActionRule> navigationbar = new GlobalNavigationBar<ActionRule>("navigation", getDisplayName().getObject()) {
			@Override
			public void onNavigate(ActionRule rule) {
				Page page = new ActionRulePage(new ObjectModel<ActionRule>(rule), this, false);
				setResponsePage(page);
			}
			@Override
			protected void onReturn(AjaxRequestTarget target) {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			
			@Override
			public void onDetach() {
				super.onDetach();
				ActionRulesConsole.this.onDetach();
			}
			
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
		Page page = new ActionRulePage(model, navigationbar, isnew);
		return page;
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ClickEvent<ActionRule>>() {
			@Override
			public void onEvent(ClickEvent<ActionRule> event) {
				setResponsePage(ActionRulesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<ActionRule> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		items.add(new NewRuleButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected void onClick() {
				try {
					Object ActionRule = ServiceLocator.getService(ContentFactoryService.class).createRule();
					Page page = ActionRulesConsole.this.getPage(ActionRulesConsole.this.getModel((ActionRule)ActionRule), 0, true);
					setResponsePage(page);
				}
				catch (ContentCreationException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
			@Override
			public boolean isVisible() {
				return is_admin;
			}
		});
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return ActionRulesConsole.this.getName();}, new Model<String>(ActionRulesConsole.this.getDescription()));
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

	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {
			if (((ActionRule) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED) return "archived-state";
			if (((ActionRule) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)  return "deleted-state";
			return null;
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
		
	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		if (dt==null)
			return new Model<String>("err");
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
		return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
	}
	
	@Override
	protected Panel getPanel(IModel<ActionRule> model) {
		return new ExpandedPanel<ActionRule>("editor", this, model);
	}
	
	@Override
	protected Panel getPanel(IModel<ActionRule> model, List<String> snippets) {
		return new ExpandedPanel<ActionRule>("editor", this, model, snippets);
	}
}   
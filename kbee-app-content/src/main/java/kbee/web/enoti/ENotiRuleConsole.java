package kbee.web.enoti;



import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.novamens.content.security.Role;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.dao.DataIntegrityViolationException;


import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.dashboard.LabelPanel;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.SecurityBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.query.ENotiRulesSystemQuery;


/**
 * System ENotiRule
 */
@SuppressWarnings("serial")
public abstract class ENotiRuleConsole extends AbstractSimpleConsole<ENotiRule> {
				
	private static final long serialVersionUID = 1L;
																					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ENotiRuleConsole.class.getName());
	
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_admin	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	@SuppressWarnings("unused")
	private Locale user_locale;
	private ZoneId user_zoneid;
	private List<ToolbarItem> items;

	private List<GridColumn<SearchResult,String>> columns;
	
	public ENotiRuleConsole( Query query) {
		super("ENotiRule", query);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
		if (user_zoneid==null) 
			user_zoneid=ZoneId.systemDefault();
		user_locale = getSessionUser().getLocale();
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
	protected boolean hasExpander() {
		return true;
	}
	
	@Override
	public Query newQuery() {
		return new ENotiRulesSystemQuery();
	}
	

	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	}
											

	protected Panel getPanel(IModel<ENotiRule> model, List<String> snippets) {
		return new ExpandedPanel<ENotiRule>("editor", this, model, null);
	}
	
	@Override
	protected Panel getPanel(IModel<ENotiRule> model) {
		return new ExpandedPanel<ENotiRule>("editor", this, model);
		//panel = new ENotiRuleDataHitPanel(model);
	}
	
	
	@Override
	protected Panel getMenu(IModel<ENotiRule> model) {
		
		ContextMenuPanel<ENotiRule> menu = new ContextMenuPanel<ENotiRule>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(id ->
			new  LinkMenuItemPanel<ENotiRule>(id) {
				public void onClick() {					
					setResponsePage(ENotiRuleConsole.this.getPage(getModel(), ENotiRuleConsole.this.getIndex(getModel().getObject()), false));
				}
				@Override 
				public String getLabel() {
					return ENotiRuleConsole.this.getLabel("enotirule.console.contextmenu.open").getObject();
				}
				public String getTarget() {
					return "_blank";
				}
			}
		);
		
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<ENotiRule>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = ENotiRuleConsole.this.getAuditTrailModal();
					((ObjectAuditModal<ENotiRule>)modal).open(target, getModel(), true);
				}
				@Override 
				public String getLabel() {
					return ENotiRuleConsole.this.getLabel("enotirule.console.contextmenu.audittrail").getObject();
				}
			}
		);
		

		menu.addItem(id ->
		new AjaxMenuItemPanelV5<ENotiRule>(id) {
			public void onClick(AjaxRequestTarget target) {
					IModel<String> ms;
					ms = getConsoleLabel("deleteconfirmation.message", getModel().getObject().getName());
					getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, Button button) {
							if (button.key().equals(Dialog.Delete.key())) {
								try { 
									executeDelete(target);
								} catch (Exception e) {
									logger.error(e);
								}
								ENotiRuleConsole.this.refresh(target);
							}
						}
					});
					refresh(target);
			}
			@Override 
			public String getLabel() {
				return ENotiRuleConsole.this.getLabel("enotirule.console.contextmenu.delete").getObject();
			}
			
			@Override
			public boolean isEnabled() {
				if (is_support && !is_root)
					return false;
				if (is_admin || is_root)
					return true;
				return false;
			}
			
			protected void executeDelete(AjaxRequestTarget target) {
				try {
					ServiceLocator.getService(ENotiRuleService.class).delete(getModelObject());
					
				}
				catch (DataIntegrityViolationException e) {
					getErrorDialog().open(target, getConsoleLabel("groupsconsole.error.constraint"));
				}
				catch (Exception e) {
					getErrorDialog().open(target, new Model<String>(e.getMessage()));
				}
			}
		}
	);

		
		return menu;
	}

	
	protected Page getPage(IModel<ENotiRule> model, long index, boolean isnew) {
		
		GlobalNavigationBar<ENotiRule> navigationbar = new GlobalNavigationBar<ENotiRule>("navigation",  getDisplayName().getObject()) {
		
		// GlobalNavigationBar<ENotiRule> navigationbar = new GlobalNavigationBar<ENotiRule>("navigation", getSearcher(), index, getDisplayName().getObject()) {
			@Override
			public void onNavigate(ENotiRule facet) {
				Page page = new ENotiRulePage(new ObjectModel<ENotiRule>(facet));
				setResponsePage(page);
			}
			@Override
			protected void onReturn(AjaxRequestTarget target) {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			
			@Override
			public void onDetach() {
				super.onDetach();
				ENotiRuleConsole.this.onDetach();
			}
			
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
		
		navigationbar.setIsAlerts(false);
		Page page = new ENotiRulePage(model);
		return page;
	}

	
	
	protected Page getENotiRulePage(IModel<ENotiRule> model, int index, final boolean editon) {
		Page page = new ENotiRulePage(model);
		return page;
	}

	
	private String getReceiversStr(ENotiRule rule) {
		
		StringBuilder str = new StringBuilder();
		
		
		if (rule==null || (rule.getReceivers().isEmpty() && rule.getRoleReceivers().isEmpty()))
			return str.toString();
		
		for (Principal p: rule.getReceivers()) {
			if (str.length()>0)
				str.append(" | ");
			str.append(p.getDisplayName());
			if (p instanceof User) {
				str.append("<span class=\"ago\"> ("+ new StringResourceModel("user", this, null).getObject()+") </span>");
			}
			else if (p instanceof Group) {
				str.append("<span class=\"ago\"> ("+new StringResourceModel("group", this, null).getObject()+") </span>");
			}
			else
				str.append("<span class=\"ago\"> ("+ p.getClass().getSimpleName() +") </span>");
		}

		for (Role roleReceiver : rule.getRoleReceivers()) {
			if (str.length()>0)
				str.append(" | ");
			str.append(roleReceiver.getDisplayName());
			str.append("<span class=\"ago\"> ("+ new StringResourceModel("role", this, null).getObject()+") </span>");
		}


		return str.toString();
	}
	
	/**
	 * Note that the Query is Hibernate Query
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		final String yes_str= new StringResourceModel("yes", this, null).getObject();
		final String no_str= new StringResourceModel("no", this, null).getObject();

		
        this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), getLabel("st")));
        
        
        
		LinkPredicateKbeeGridColumn<ENotiRule> titleColumn = new LinkPredicateKbeeGridColumn<ENotiRule>("title", getLabel("enotiruleconsole.column.name") , "title_sort", facet->facet.getDisplayName(), facet->getModel(facet));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		columns.add(titleColumn);

		
		this.columns.add(new LastModifiedColumn<ENotiRule>("modified", getLabel("enotiruleconsole.column.modified"), "modified") {
			@Override
			protected String getContextKey() {
				return ENotiRuleConsole.this.getName() + super.getContextKey();
			}
		});

		KbeePredicateGridColumn<ENotiRule> eventColumn = new KbeePredicateGridColumn<>("event", getLabel("enotiruleconsole.column.event"),	(rule) -> rule.getEventTypeStr(getSessionUser().getLocale()));
		eventColumn.setContextKey(this.getName() + eventColumn.getContextKey());
		columns.add(eventColumn);
				
		{							
			KbeePredicateGridColumn<ENotiRule> statusColumn = new KbeePredicateGridColumn<ENotiRule>("status", getLabel("enotiruleconsole.column.status"), obj ->  obj.getState() != null ? obj.getState().getLabel(getSessionUser().getLocale()) : "err" );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getSessionUser().getLocale()) : "err");
			statusColumn.setTextValueResolver((rule) ->rule.getState().getLabel(getSessionUser().getLocale()));
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}

		KbeePredicateGridColumn<ENotiRule> receiversColumn = new KbeePredicateGridColumn<ENotiRule>("receivers", getLabel("enotiruleconsole.column.receiversColumn"),	(rule) -> getReceiversStr(rule));
		receiversColumn.setPreferred(true);
		receiversColumn.setContextKey(this.getName() + receiversColumn.getContextKey());
		columns.add(receiversColumn);
		
		
		
		
		
		KbeePredicateGridColumn<ENotiRule> emailColumn = new KbeePredicateGridColumn<>("email", getLabel("enotiruleconsole.column.email"),	(rule) ->rule.isEmail()? "<span class=\"yes\">"+yes_str+"</span>":"<span class=\"no\">"+no_str+"</span>");
		emailColumn.setTextValueResolver((rule) ->rule.isEmail()? yes_str : no_str);
		emailColumn.setContextKey(this.getName() + emailColumn.getContextKey());
		columns.add(emailColumn);
		

		
		KbeePredicateGridColumn<ENotiRule> alertColumn = new KbeePredicateGridColumn<>("alert", getLabel("enotiruleconsole.column.alert"),	(rule) ->rule.isAlert()?"<span class=\"yes\">"+yes_str+"</span>":"<span class=\"no\">"+no_str+"</span>");
		alertColumn.setContextKey(this.getName() + alertColumn.getContextKey());
		alertColumn.setTextValueResolver((rule) ->rule.isAlert() ? yes_str : no_str);
		columns.add(alertColumn);
		
		
		
		
		
		
		
		
		KbeePredicateGridColumn<ENotiRule> conditionColumn = new KbeePredicateGridColumn<ENotiRule>("condition", getLabel("enotiruleconsole.column.condition"),  (rule) -> rule.getDisplayCondition());
		conditionColumn.setContextKey(this.getName() + conditionColumn.getContextKey());
		conditionColumn.setTextValueResolver((rule) -> rule.getCondition());
		columns.add(conditionColumn);

		
		KbeePredicateGridColumn<ENotiRule> emailTemplateColumn = new KbeePredicateGridColumn<>("emailtemplate", getLabel("enotiruleconsole.column.emailtemplate"),	(rule) -> getEmailTemplateLink(rule.getEmailTemplate()) );
		emailColumn.setContextKey(this.getName() + emailTemplateColumn.getContextKey());
		columns.add(emailTemplateColumn);
		
		KbeePredicateGridColumn<ENotiRule> idColumn = new KbeePredicateGridColumn<>("id",getLabel("enotiruleconsole.column.id"), (rule) -> String.valueOf(rule.getId()));
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		idColumn.setPreferred(false);
		columns.add(idColumn);
		
		
		return this.columns;
	}


	protected String getEmailTemplateLink(String key) {
		
		// getContentDao().getEmailTemplate(getDomain(),  getSessionUser().getLocale().getLanguage().equals("es")? "es":"eng", key);
		if (key==null || "".equals(key))
			return "<a class=\"btn-link\"  target=\"_blank\" href=\"emailtemplates\">"+key+"</a>";
		else
			return "<a class=\"btn-link\"  target=\"_blank\" href=\"emailtemplates/" + (getSessionUser().getLocale().getLanguage().equals("es")? "es":"en")+  "/"+key+"\">"+key+"</a>";
	}

	@Override
	protected boolean hasTopPanel() {
		return false;
	}

	
	/**
	 * 
	 */
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<ENotiRule>>() {
			@Override
			public void onEvent(ClickEvent<ENotiRule> event) {
				setResponsePage(ENotiRuleConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
		
	
	/**
	 * 
	 */
	@Override													
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<ENotiRule> browser) {
		
		if (items!=null)
			return items;
		
		items = new ArrayList<ToolbarItem>();
		
		items.add( new NewButton(browser, Align.TOP_LEFT) {
			@Override
			public boolean isEnabled() {
				if (is_root || is_admin) 
					return true;
				return false;
			}
			
			@Override
			public boolean isVisible() {
				if (is_root || is_admin) 
					return true;
				return false;
			}
			
			public void onClick() {
				try {
					ENotiRule rule = ServiceLocator.getService(ENotiRuleService.class).createEmailRule(getSessionUser(),String.valueOf(System.currentTimeMillis()) , true);
					rule.setName(new StringResourceModel("new-enoti", ENotiRuleConsole.this, null).getString());
					ENotiRulePage pa=new ENotiRulePage(new ObjectModel<ENotiRule>(rule), true, false, true);
					pa.setIsNew(true);
					setResponsePage(pa);
				}
				catch (Exception e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			};
		});
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return ENotiRuleConsole.this.getName();}, new Model<String>(ENotiRuleConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
		};
		this.items.add(infoButton);
		return items;
	}


	
	protected String getDescription() {
		return new StringResourceModel("description", this, null).getObject();
	}

	@Override
	 protected  IModel<ENotiRule> getModel(ENotiRule object) {
			return new ObjectModel<ENotiRule>(object, true);
	}

	@Override
	protected Panel getItemListPanel(IModel<ENotiRule> model, int index) {
		return new LabelPanel("item", new Model<String> (model.getObject().getDisplayName()));
	}
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<ENotiRule>("audit-trail-modal"));
	}
	

	
}



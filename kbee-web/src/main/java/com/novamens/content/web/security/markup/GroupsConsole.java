package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.wicket.model.StringResourceModel;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;

import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;

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
import kbee.web.nav.GroupsBC;
import kbee.web.nav.RolesBC;
import kbee.web.nav.RulesBC2;
import kbee.web.nav.SecurityBC;
import kbee.web.nav.UsersBC;
import kbee.web.query.GroupsQuery;


@SuppressWarnings("serial")
public abstract class GroupsConsole extends AbstractFacetedConsole<Group> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GroupsConsole.class.getName());
	
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	private List<GridColumn<SearchResult,String>> columns; 
	
	public GroupsConsole(Query query) {
		super("groups", query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		 MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
			 DropDownMenuBC dd = new DropDownMenuBC();
			 dd.addElement(new SecurityBC(), true);
			 dd.addElement(new UsersBC());
			 dd.addElement(new RolesBC());
			 dd.addElement(new GroupsBC());
			 dd.addElement(new RulesBC2());
			 bc.addElement(dd);
		 bc.addElement(new BCElement("groups"));
		 add(bc);
	}
	
	@Override
	protected String getIcon(IModel<Group> model) {
		return null;
	}	

	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
	
	@Override
	 protected  IModel<Group> getModel(Group object) {
			return new ObjectModel<Group>(object, true);
	}

	
	
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new GroupsQuery(getQueryIndex()));
	}
	
	// protected abstract Page getConsolePage(Query query, long index);
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	};
	
	protected boolean hasExpander() {
		return true;
	} 

	@Override
	protected Panel getMenu(IModel<Group> model) {
		
		ContextMenuPanel<Group> menu = new ContextMenuPanel<Group>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<Group>() {
			@Override
			public AbstractMenuItemPanelV5<Group> getItem(String id) {
				return new AjaxMenuItemPanelV5<Group>(id) {
					public void onClick(AjaxRequestTarget target) {
						setResponsePage(GroupsConsole.this.getPage(getModel(), GroupsConsole.this.getIndex(getModel().getObject()), false));
					}
					@Override 
					public String getLabel() {
						return GroupsConsole.this.getLabel("groupsconsole.contextmenu.open").getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Group>() {
			@Override
			public AbstractMenuItemPanelV5<Group> getItem(String id) {
				return new AjaxMenuItemPanelV5<Group>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = GroupsConsole.this.getAuditTrailModal();
						((ObjectAuditModal<Group>)modal).open(target, getModel(), true);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("groupsconsole.contextmenu.audittrail").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Group>() {
			@Override
			public AbstractMenuItemPanelV5<Group> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Group>(id) {
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

		
		menu.addItem(new MenuItemFactory<Group>() {
			@Override
			public AbstractMenuItemPanelV5<Group> getItem(String id) {
				return new AjaxMenuItemPanelV5<Group>(id) {
					public void onClick(AjaxRequestTarget target) {
						if (getModel().getObject().isDerived()) {
							getErrorDialog().open(target, getConsoleLabel("groupsconsole.isderived", getModel().getObject().getName()));
						}
						else {
							int size = ((KbeeGroup)getModel().getObject()).getMembers().size();
							IModel<String> ms;
							if (size==0)
								ms = getConsoleLabel("groupsconsole.deleteconfirmation.message", getModel().getObject().getName());
							else 
								ms = getConsoleLabel("groupsconsole.deleteconfirmation.message-with-members", getModel().getObject().getName(), String.valueOf(size));
							
							getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try { 
											executeDelete(target);
										
										} catch (Exception e) {
											logger.error(e);
										}
										
										GroupsConsole.this.refresh(target);
									}
								}
							});
						}
						refresh(target);
					}
					
					@Override 
					public String getLabel() {
						return getConsoleLabel("groupsconsole.contextmenu.delete").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						
						if (is_support && !is_root)
							return false;
						
						return  (!getModel().getObject().isCanonical()); 
								
					}
					
					protected void executeDelete(AjaxRequestTarget target) {
						try {
							ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
						}
						catch (DataIntegrityViolationException e) {
							getErrorDialog().open(target, getConsoleLabel("groupsconsole.error.constraint"));
						}
						catch (Exception e) {
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
		
		if (columns!=null)
			return columns;
		
				
		columns = new ArrayList<GridColumn<SearchResult,String>>();

		{
			LinkPredicateKbeeGridColumn<Group> titleColumn =
					new LinkPredicateKbeeGridColumn<>("title", getLabel("groupsconsole.column.name"), "title_sort", obj -> ((KbeeGroup) obj).getDisplayName(getUser().getLocale()), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}

		{
			KbeePredicateGridColumn<Group> typeColumn = new KbeePredicateGridColumn<>("type", getLabel("groupsconsole.column.type"),
					obj -> new StringResourceModel((obj.isCanonical() ? "canonical" : "standard"), GroupsConsole.this, null).getObject());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		
		this.columns.add(new LastModifiedColumn<Group>("modified", getLabel("groupsconsole.column.modified"), "modified"){
			private static final long serialVersionUID = 1L;

			@Override
			protected String getContextKey() {
				return GroupsConsole.this.getName() + super.getContextKey();
			}
		});
		
		{
			KbeePredicateGridColumn<Group> modifiedByColumn = new KbeePredicateGridColumn<>("modifiedby", getLabel("groupsconsole.column.modifiedby"),
					obj -> String.valueOf((obj.getLastModifiedUser().getFirstLastName())));
			modifiedByColumn.setPreferred(false);
			modifiedByColumn.setContextKey(this.getName() + modifiedByColumn.getContextKey());
			this.columns.add(modifiedByColumn);
		}

		columns.add(new GridColumn<SearchResult, String>("users", getLabel("groupsconsole.column.users")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				KbeeGroup  group = (KbeeGroup) result.getObject();
				if (group!=null) {
					return new Model<String>(String.valueOf(group.getMembers().size()));
				}
				else {
					return new Model<String>("err");
				}

			}

			@Override
			protected String getContextKey() {
				return GroupsConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			public String getCssClass() {
					return "col col-xs-1 col-md-1 col-lg-1 ui-resizable";
			}
			
			protected String getLabelCss(IModel<SearchResult> model) {
				return "number-md";
			}

			@Override
			protected String getLabelCss() {
				return "number-md";
			}
			
			@Override
			public String getHeaderCssClass() {
				return super.getHeaderCssClass()+" centered";
			}
		});


		{
			KbeePredicateGridColumn<Group> typeColumn = new KbeePredicateGridColumn<>("areacode", getLabel("groupsconsole.column.areacode"),
					obj -> obj.getAreaCode());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			this.columns.add(typeColumn);
		}

		
		
		{
			KbeePredicateGridColumn<Group> idColumn = new KbeePredicateGridColumn<>("id", getLabel("groupsconsole.column.id"),
					obj -> String.valueOf(obj.getId()));
			idColumn.setPreferred(false);
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}

		return columns;
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	protected Page getPage(IModel<Group> model, long index, boolean edition) {
			return getPage(model, index, edition, false);
	}

	
	
	protected Page getPage(IModel<Group> model, long index, boolean edition,  boolean isNew) {
        //Searcher searcher = getSearcher();
        //SolrCursor soc = new SolrCursor((SolrResultSet) searcher.getResultSet(), index);
        GroupPage page = new GroupPage(model) ; //, new SolrCursorModel(soc));
        //page.setEditionEnabled(edition);
        return page;

    }

	
	/**
	protected Page getPage(IModel<Group> model, long index, boolean edition, boolean isNew) {
	
		GlobalNavigationBar<Group> navigationbar = new GlobalNavigationBar<Group>("navigation", getDisplayName().getObject()) {
			
		// GlobalNavigationBar<Group> navigationbar = new GlobalNavigationBar<Group>("navigation", getSearcher(), index, getDisplayName().getObject()) {
			@Override
			public void onNavigate(Group group) {
				GroupPage page = new GroupPage(getModel(group), this, false, false, false);
				setResponsePage(page);
			}

			@Override
			public void onDetach() {
				super.onDetach();
				GroupsConsole.this.onDetach();
			}
			@Override
			protected void onReturn(AjaxRequestTarget target) {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
													
		navigationbar.setSearchPlaceHolder(new StringResourceModel("search-in-groups", GroupsConsole.this, null).getString());
		//navigationbar.setIsNotes(false);
		//navigationbar.setIsAlerts(false);
		Page page = new GroupPage(model, navigationbar, edition, false, isNew);
		
		return page;
	}
	*/
	
	
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
		
		add(new WicketEventListener<ClickEvent<Group>>() {
			@Override
			public void onEvent(ClickEvent<Group> event) {
				setResponsePage(GroupsConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	

	protected Panel getPanel(IModel<Group> model) {
		return new ExpandedPanel<Group>("editor", this, model, null);
	}
	
	
	protected Panel getPanel(IModel<Group> model,  List<String> snippets) {
		return new ExpandedPanel<Group>("editor", this, model, snippets);
	}
	
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Group> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		items.add(new NewGroupButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public void onClick() {
				try {
					Object group = ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
					setResponsePage(GroupsConsole.this.getPage(GroupsConsole.this.getModel((Group)group), 0, true, true));
				}
				catch (ContentCreationException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		});
		

		return items;
	}
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<Group>("audit-trail-modal"));
	}
}


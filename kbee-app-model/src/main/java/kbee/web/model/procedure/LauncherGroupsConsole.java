package kbee.web.model.procedure;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.ModelReference;
import com.novamens.content.service.DomService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarAlert;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.util.BreadCrumb;

import kbee.util.logging.Logger;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.object.ObjectStatusColumn;

@SuppressWarnings("serial")						
public abstract class LauncherGroupsConsole extends  AbstractFacetedConsole<LauncherGroup> {
	private static final long serialVersionUID = 1L;
												
	private static Logger logger = Logger.getLogger(LauncherGroupsConsole.class.getName());

	private List<ToolbarItem> items;
	
	private IModel<LauncherGroup> groupmodel;
	private String console;
	private List<GridColumn<SearchResult,String>> columns;
	private boolean is_deleted_visible = false;
	
	public LauncherGroupsConsole(Query query) {
		super("tags", query);
		this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
		setConsole(getName());
	}
	
	
	@Override
	protected String getIcon(IModel<LauncherGroup> model) {
		return null;
	}	

	
	@Override			
	protected  IModel<LauncherGroup> getModel(LauncherGroup object) {
		return new ObjectModel<LauncherGroup>(object, true);
	}
	
	public void setTag(IModel<LauncherGroup> model) {
		this.groupmodel = model;
	}
	
	public LauncherGroup getTag() {
		return groupmodel.getObject();
	}

	public String getConsole() {
		return this.console;
	}

	public void setConsole(String console) {
		this.console=console;
	}
	
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new LauncherGroupsQuery(isDeletedVisible()));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
		if (this.groupmodel!=null)
			this.groupmodel.detach();
		this.items=null;
	}
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new LauncherGroupsBC());
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	@Override
	protected Panel getMenu(IModel<LauncherGroup> model) {
		
		ContextMenuPanel<LauncherGroup> menu = new ContextMenuPanel<LauncherGroup>(model);
		
		menu.addItem(new MenuItemFactory<LauncherGroup>() {
			@Override
			public AbstractMenuItemPanelV5<LauncherGroup> getItem(String id) {
				return new AjaxMenuItemPanelV5<LauncherGroup>(id) {
					public void onClick(AjaxRequestTarget target) {
						getPage().setResponsePage(getGroupPage(getModel(), 0, false, false));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<LauncherGroup>() {
			@Override
			public AbstractMenuItemPanelV5<LauncherGroup> getItem(String id) {
				return new SeparatorMenuItemPanelV5<LauncherGroup>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<LauncherGroup>() {
			@Override
			public AbstractMenuItemPanelV5<LauncherGroup> getItem(String id) {
				return new AjaxMenuItemPanelV5<LauncherGroup>(id) {
					public void onClick(AjaxRequestTarget target) {
						List<ModelReference> references = getReferences(); 
						if (references!=null && !references.isEmpty()) {
							String message = "The tag cannot be deleted because it is referenced by</br>";
							for (int i=0; i<4 && i<references.size(); i++) {
								message += "</br><a target=\"_blank\" href=\""+references.get(i).getUrl()+"\">"+references.get(i).getDescription()+"</a>";
							}
							if (references.size()>4) {
								message += "</br></br> and others...";
							}
							InfoDialog infoDialog = (InfoDialog) getInformationModal();
							infoDialog.open(target,() -> {return "References";}, new Model<String>(message));
						}
						else {
							getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											((KbeeLauncherGroup)getModelObject()).getService(DomService.class).delete();
											FeedbackHelper.showSuccessToast(getLabel()+ " <br/>" + getModelObject().getDisplayName());
										}
										catch (DataIntegrityViolationException | ConstraintException e) {
											getErrorDialog().open(target, getConsoleLabel("error.constraint"));
										}
										catch (Exception e) {
											getErrorDialog().open(target, new Model<String>(e.getMessage()));
										}
										LauncherGroupsConsole.this.refresh(target);
									}
								}
							});
						}
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
					public boolean isEnabled() {
						if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
							return false;
						return !isSupport();
					}
					public List<ModelReference> getReferences() {
						return null;
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

		this.columns.add(new ObjectStatusColumn<KbeeLauncherGroup>("iconstatus", getName(), new Model<String>("St")));
		
		{
			LinkPredicateKbeeGridColumn<LauncherGroup> titleColumn = new LinkPredicateKbeeGridColumn<>("title", getLabel("name"), "title", obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			titleColumn.setTarget(null);
			columns.add(titleColumn);
		}
		
		this.columns.add(new LastModifiedColumn<KbeeLauncherGroup>("modified", getLabel("modified"), "modified") {
			@Override
			protected OffsetDateTime getOffsetDateTime(KbeeLauncherGroup object) {
				return object.getLastModifiedOffsetDateTime();
			}
			@Override
			protected String getContextKey() {
				return LauncherGroupsConsole.this.getName() + super.getContextKey();
			}
		});

		{
			KbeePredicateGridColumn<KbeeLauncherGroup> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"), "status",obj ->  obj.getState() != null ? obj.getState().getLabel(getSessionUser().getLocale()) : "err");
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getSessionUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}
		
		{						
			KbeePredicateGridColumn<KbeeLauncherGroup> typeColumn = new KbeePredicateGridColumn<>("alias", getLabel("alias"),	obj ->  obj.getAlias());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			typeColumn.setPreferred(true);
			this.columns.add(typeColumn);
		}

		{
			KbeePredicateGridColumn<KbeeLauncherGroup> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"), "status", 	obj ->  obj.getState() != null ? obj.getState().getLabel(getSessionUser().getLocale()) : "err"   );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getSessionUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setDefaultWidth(200);
			this.columns.add(statusColumn);
		}

		
		{
			KbeePredicateGridColumn<KbeeLauncherGroup> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),
				obj ->  String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}
		
		return this.columns;
	}
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			@Override
			public void onEvent(SidePanelEvent event) {
			}
		});
		
		add(new WicketEventListener<ClickEvent<LauncherGroup>>() {
			@Override
			public void onEvent(ClickEvent<LauncherGroup> event) {
				setResponsePage(getGroupPage(event.getModel(), event.getIndex(), false, false));
			}
		});
	}

	@Override													
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<LauncherGroup> browser) {
		if (items!=null)
			return items;
		
		items = new ArrayList<ToolbarItem>();

		items.add(new ToolbarAlert(browser, Align.TOP_LEFT) {
			protected IModel<String> getLabel() {
				return new StringResourceModel("readonly", this, null);
			}
			@Override
			public boolean isVisible() {
				if (isRoot())
					return false;
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS) 
					return true;
				return false;
			}
		});
		
		items.add( new NewButton(browser, Align.TOP_LEFT) {

			@Override
			protected IModel<String> getLabel() {
				return new StringResourceModel("new-group", LauncherGroupsConsole.this, null);
			}
			
			@Override
			public boolean isVisible() {
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
					return false;
				return true;
			}
			
			public void onClick() {
				try {
					LauncherGroup  group = (LauncherGroup)ServiceLocator.getService(ObjectFactoryService.class).createLauncherGroup(getLabelString("newelement"));
					((KbeeLauncherGroup) group).setName(getLabelString("newelement"));
					Page page = getGroupPage(LauncherGroupsConsole.this.getModel(group), 0, true, true);
					setResponsePage(page);
				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			};
		});
			
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return LauncherGroupsConsole.this.getName();}, new Model<String>(LauncherGroupsConsole.this.getDescription()));
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
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}

	protected Page getGroupPage(IModel<LauncherGroup> model, int index, final boolean editon, final boolean is_new) {
		return new LauncherGroupPage(model, editon, is_new);
	}
	
	@Override
	protected Panel getPanel(IModel<LauncherGroup> model, List<String> snippets) {
		return new ExpandedPanel<LauncherGroup>("editor", this, model, snippets);
	}
	
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {		
			if (rowmodel.getObject().getObject() instanceof com.novamens.dom.Object) {
				com.novamens.dom.Object object = (com.novamens.dom.Object) rowmodel.getObject().getObject();
				//if (object.getState()==ObjectState.ARCHIVED)				return "archived-state";
				if (object.getState()==ObjectState.DELETED)					return "deleted-state";	
			}
			
			return null;
				
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	@Override
	protected GridMenu getGridToolbarMenuItem() {
		GridMenu gridToolbarMenuItem = super.getGridToolbarMenuItem();

		gridToolbarMenuItem.addItem((itemId) -> new SeparatorMenuItemPanelV5<File>(itemId) {
			@Override
			public String getCssClass() {
				return "divider";
			}
		});

        MenuItemFactory<?> showDeletedUsersItem = (itemId) ->
        	new AjaxCheckMenuItemPanelV5<Object>(itemId) {
			    @Override
                   public String getLabel() {
                       return new StringResourceModel("show-deleted", LauncherGroupsConsole.this, null).getObject();
                   }
                   @Override
                   public void onClick(AjaxRequestTarget target) throws Exception {
                   	LauncherGroupsConsole.this.setDeletedVisible(!LauncherGroupsConsole.this.isDeletedVisible());
                   	setResponsePage(new LauncherGroupsPage());
                   }
                   @Override
                   public boolean isIconVisible() {
                        return LauncherGroupsConsole.this.isDeletedVisible();
                   }
                   @Override
                   public String getCssClass() {
                       if (isIconVisible())
                           return "label-selected";
                       else
                           return "label-no-selected";
                   }
	        };
					
        gridToolbarMenuItem.addItem(showDeletedUsersItem);
        return gridToolbarMenuItem;
    }

	
    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
    }
}
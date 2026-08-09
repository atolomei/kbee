package kbee.web.searcher.page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListEditor;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListItemsPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsDeleteListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsEmptyListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsPanel.UserListEditorFragment;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.console.CursorNavigator;
import kbee.web.content.panel.ShareModal;
import kbee.web.cursor.CursorListModel;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ClickSendByEmailEvent;
import kbee.web.model.service.ObjectModelService;
import kbee.web.nav.Navigator;
import kbee.web.panel.AlertPanel;
import kbee.web.panel.ClickItemEvent;
import kbee.web.service.ApplicationSiteMapService;

public class SearcherUserListPage extends AbstractSearcherPage<UserList> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherUserListPage.class.getName());
	
	private MyListItemsPanel panel;
	private MyListEditor editor;
	private WebMarkupContainer browser;

	
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}

	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	
	public SearcherUserListPage(PageParameters parameters) {
		try {
			UserList ul = getUserList(parameters);		
			if (ul!=null) {
				setModel(new ObjectModel<UserList>(ul));
				setSiteModel(new ObjectModel<Site>(getModel().getObject().getSite()));
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	public SearcherUserListPage(Object[] parameters) {
		if (parameters==null || parameters.length==0)
			throw new IllegalArgumentException ("must have a parameter IModel<UserList>");
		if (! ((parameters[0]) instanceof IModel<?>))
				throw new IllegalArgumentException ("must have a parameter IModel<UserList>");
		setModel((IModel<UserList>) parameters[0]);
		setSiteModel(new ObjectModel<Site>(getModel().getObject().getSite()));
		getPageParameters().set("id", getModel().getObject().getId().toString());
	}
	

	/**
	 * 
	 */
	public SearcherUserListPage(IModel<UserList> model) {
		setModel(model);
		setSiteModel(new ObjectModel<Site>(getModel().getObject().getSite()));
		getPageParameters().set("id", getModel().getObject().getId().toString());
	}

	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<Content>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Content> event) {
				try {
					
					Content content = event.getModel().getObject();
					
					if (!(content instanceof IDoc)) {
						logger.error("TBA ASSUMES CONTENT IS IDOC");
						return;
					}
					
					SearcherDetailDocumentPage<Content> page = new SearcherDetailDocumentPage<Content>(new ObjectModel<Content>(content), getSiteModel());

					List<IModel<Content>> mi = new ArrayList<IModel<Content>>();
					
					SearcherUserListPage.this.getModel().getObject().getItems().forEach(item -> {mi.add(new ObjectModel<Content>(item.getContent()));});
					CursorListModel<Content> cursor = new CursorListModel<Content> (mi, event.getIndex());
					CursorNavigator<Content> nav = new CursorNavigator<Content>(cursor, event.getIndex());
					page.setNavigator(nav);
					setResponsePage(page);
					
					mi.forEach(item-> item.detach());
					cursor.detach();
					nav.detach();
					getPage().detach();
					return;
				} 
				catch (Exception e) {
					logger.error(e);
					
				}
			}
		});

		
		
		
		
		
		
		
		
	}
	
	public void onDetach() {
		super.onDetach();
		if (getModel()!=null)
			getModel().detach();
	}
	
	protected boolean isConsole() {
		return false;
	}

	protected boolean hasPermissions() {
		return true;
		//return getDomain().isPortalLibrary() && is_user;
	}
	
	
	WebMarkupContainer menu_container;
	
	
	private void addTitle() {
					
		browser.addOrReplace(new Label("title", getModel().getObject().getTitle()));
		
		WebMarkupContainer wm =new WebMarkupContainer("description-container");
		wm.setVisible(getModel().getObject().getDescription()!=null);
		wm.add(new Label("description", getModel().getObject().getDescription()));
		
		browser.addOrReplace(wm);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		setOutputMarkupId(true);
		browser = new  WebMarkupContainer("browser");
		browser.setOutputMarkupId(true);
		add(browser);

		if (getModel()==null) {
			
			browser.addOrReplace(new Label("title", ""));
			WebMarkupContainer wm =new WebMarkupContainer("description-container");
			wm.setVisible(false);
			wm.add(new Label("description", ""));
			browser.addOrReplace(wm);
			menu_container = new WebMarkupContainer ("menu-container");
			browser.add(menu_container);
			menu_container.setVisible(false);
			WebMarkupContainer we =new WebMarkupContainer("empty-container");
			we.setVisible(false);
			browser.add(we);
			browser.add(new InvisiblePanel("userlist-editor"));
			browser.add (new ErrorNotAuthorizedPanel<>("userlist"));
			
			return;
		}


		
		
		
		
		setSiteModel(new ObjectModel<Site>(getModel().getObject().getSite()));
		setPageTitle(new Model<String>(getModel().getObject().getTitle()));
		
		browser.add(new InvisiblePanel("userlist-editor"));
		addModals();

		
		if (hasPermissions()) {

			addTitle();

			menu_container = new WebMarkupContainer ("menu-container");
			browser.add(menu_container);
			
			
			WebMarkupContainer we =new WebMarkupContainer("empty-container");
			browser.add(we);
			we.setVisible( 
					((getSessionUser()!=null) && 
					  getSessionUser().getId().equals( getModel().getObject().getOwner().getId())) && 
					  getModel().getObject().getItems().isEmpty()
					);
			
			we.add(new AlertPanel<UserList>("empty", AlertPanel.INFO,  getModel(),	new StringResourceModel("empty", this, null), null ));
			
			boolean isActions = getSessionUser().getId().equals( getModel().getObject().getOwner().getId());
			
			panel = new MyListItemsPanel("userlist", getModel(), isActions, getModel().getObject().getConsole());
			panel.setIsClose(false);
			browser.add(panel);
			
			
			if (getSessionUser()!=null &&  getSessionUser().equals( getModel().getObject().getOwner())) {
				menu_container.add(getUserListMenu());
				menu_container.setVisible(true);
			}
			else {
				menu_container.setVisible(false);
				menu_container.add(new InvisiblePanel("menu"));
			}
			
		
		} else {
			
			menu_container.setVisible(false);
			browser.add (new ErrorNotAuthorizedPanel<>("userlist"));
			browser.setVisible(false);
				
		}
	}
	
	
	
	public void onCloseEditor(AjaxRequestTarget target) {
		editor.setVisible(false);
	
		setModel ( editor.getModel() );
		addTitle();
		target.add(browser);
		
	}

	public void onEdit(AjaxRequestTarget target) {
		if (editor==null) {
			editor = new MyListEditor("userlist-editor", SearcherUserListPage.this.getModel()) {
				protected void onClose(AjaxRequestTarget target) {
					SearcherUserListPage.this.onCloseEditor(target);
				}
			};
			browser.addOrReplace(editor);
		}
		editor.setVisible(true);
		target.add(browser);
	}

	/**
	public List<IModel<UserListItem>> getItems() {
		List<IModel<UserListItem>> m_list = new ArrayList<IModel<UserListItem>>(); 
		List<UserListItem> list = getModel().getObject().getItems();
		for (UserListItem item: list) {
			m_list.add(new ObjectModel<UserListItem>( item ));
		}
	
		//list.sort(new Comparator<UserListItem>() {
		//	@Override
		//	public int compare(UserListItem a, UserListItem b) {
		//		try  { 
		//			return (((com.novamens.dom.Object) a.getObject()).getDisplayName().compareToIgnoreCase(((com.novamens.dom.Object) b.getObject()).getDisplayName()));
		//			
		//		} catch (Exception e) {
		//			return 0;
		//		}
		//	}
		//});
		//return m_list;
	}
**/
	
	
	@Override
	protected boolean isSearchForm() {
		return true;
	}
	

	
	
	protected UserList getUserList(PageParameters parameters) {
		UserList ul = null;		
		StringValue oid = parameters.get("id");
		if (!oid.isNull() && !oid.isEmpty()) {
			ul = getPropertyDao().getUserList(oid.toLong());
		}	
		return ul;
	}
	
	
	protected Panel getUserListMenu() {
		try {
				
				ContextMenuPanel<UserList> menu = new ContextMenuPanel<UserList>(getModel());
				
				menu.setOutputMarkupId(true);
				
					
				menu.addItem(new MenuItemFactory<UserList>() {
					@Override
					public AbstractMenuItemPanelV5<UserList> getItem(String id) {
						return new AjaxMenuItemPanelV5<UserList>(id) {
							
							@Override 
							public String getLabel() {
								return  SearcherUserListPage.this.getLabel("edit").getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									onEdit(target);
								} 
								catch (ContentMgmtException e) {
									logger.error(e);	
								}
							}
						};
					}
				});

				menu.addItem(id ->
				new SeparatorMenuItemPanelV5<UserList>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
			});
				
				
				menu.addItem(new MenuItemFactory<UserList>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<UserList> getItem(String id) {
						return new AjaxMenuItemPanelV5<UserList>(id) {
							private static final long serialVersionUID = 1L;
							@Override 
							public String getLabel() {
								return  SearcherUserListPage.this.getLabel("empty-list").getObject();
								
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									((KbeeUser) getSessionUser()).getService(UserListService.class).deleteAllItems(getModel().getObject());
									target.add(SearcherUserListPage.this);
									fireScanAll(new MyListsEmptyListEvent(target));
								} catch (Exception e) {
									logger.error(e);						
								}
							}
						};
					}
				});

		 

				

				menu.addItem(new MenuItemFactory<UserList>() {
					@Override
					public AbstractMenuItemPanelV5<UserList> getItem(String id) {
						return new AjaxMenuItemPanelV5<UserList>(id) {
							
							@Override 
							public String getLabel() {
								return  SearcherUserListPage.this.getLabel("delete").getObject();
								
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									((KbeeUser) getSessionUser()).getService(UserListService.class).delete(getModel().getObject());
									target.add(SearcherUserListPage.this);
									fireScanAll(new MyListsDeleteListEvent(target));
									
								} catch (Exception e) {
									logger.error(e);						
								}
							}
						};
					}
				});

				return menu;
				
			} catch (Exception e) {
				logger.error(e, getSessionUser().getUserName());
				return new InvisiblePanel("menu");
			}
		}
	
	
	
}

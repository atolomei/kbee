package com.novamens.kbee.wicket.markup.html.console.panel;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.query.SavedQuery;
import com.novamens.content.userlist.UserListService;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;


@SuppressWarnings("serial")
public class BookmarksPanel extends KBPanel {
		
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BookmarksPanel.class.getName());
	
	private String console = null;
	private IModel<Site> site_model;
	
	private boolean is_my_lists;
	
	private Query zquery;
	boolean exportSavedQueries = true;
	
	boolean isActions = false;
	boolean isAddToMain = false;
	boolean isClose = true;
	
	
	public BookmarksPanel(String id, Query query, String consoleKey, boolean isMyListsEnabled) {
		this(id, query, consoleKey, null, isMyListsEnabled, true, true);
	}
	
	public BookmarksPanel(String id, Query query, String consoleKey, IModel<Site> site_model, boolean isMyListsEnabled) {
		this(id, query, consoleKey, site_model, isMyListsEnabled, true, true);
	}
	
	public BookmarksPanel(String id, Query query, String consoleKey, IModel<Site> site_model, boolean isMyListsEnabled, boolean isExportSavedQuery, boolean isActions) {
		super(id);
		setOutputMarkupId(true);
		this.site_model = site_model;
		this.zquery=query;
		this.console=consoleKey;
		this.is_my_lists=isMyListsEnabled;
		this.exportSavedQueries=isExportSavedQuery;
		this.isActions=isActions;
		
		if(this.is_my_lists) {
			add(new WicketEventListener<MyListsDeleteListEvent>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(MyListsDeleteListEvent event) {
					event.getRequestTarget().add(BookmarksPanel.this);
				}
			});
			
			add(new WicketEventListener<MyListsAddListEvent>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(MyListsAddListEvent event) {
					event.getRequestTarget().add(BookmarksPanel.this);
				}
			});
			
			add(new WicketEventListener<MyListsRemoveAllEvent>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(MyListsRemoveAllEvent event) {
					event.getRequestTarget().add(BookmarksPanel.this);
				}
			});
		}
	}
	
	public IModel<String> getMyListTitle() {
		KbeeUser user = ((KbeeUser) getSessionUser());
		
		if (this.site_model!=null ) {
			String ul= " (" + String.valueOf( user.getService(UserListService.class).getUserLists(this.site_model.getObject()).size())+")";
			String myl=new StringResourceModel("mylists", this, null).getObject();
			return new Model<String>(myl + ul);
		}
		
		String ul= " (" + String.valueOf( user.getService(UserListService.class).getUserLists(this.console).size())+")";
		String myl=new StringResourceModel("mylists", this, null).getObject();
		return new Model<String>(myl + ul);

	}
	
	public IModel<String> getQueriesTitle() {
		KbeeUser user = ((KbeeUser) getSessionUser());
		if (this.site_model!=null ) {
			String uq= " (" + String.valueOf( user.getService(UserListService.class).getSavedQueries(this.site_model.getObject()).size())+")";
			String myq=new StringResourceModel("saved-queries", this, null).getObject();
			return new Model<String>(myq + uq);
		}
		String uq= " (" + String.valueOf( user.getService(UserListService.class).getSavedQueries(this.console).size())+")";
		String myq=new StringResourceModel("saved-queries", this, null).getObject();
		return new Model<String>(myq + uq);
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
		int selected=((KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue(this.console+ "-" + BookmarksPanel.class.getSimpleName(), "selected_tab", 0);
		if (((AjaxTabbedPanel<?>) get("tabs")).getTabs().size()>selected)
				((AjaxTabbedPanel<?>) get("tabs")).setSelectedTab(selected);
		else {
			logger.error("Selected error " + String.valueOf(selected));
		}
	}
	
	
	
	
	public boolean isAddToMain() {
		return this.isAddToMain;
	}


	public void setIsClose(boolean b) {
		this.isClose=b;
	}
	
	public boolean isClose() {
		return this.isClose;
	}
	
	@Override
	public void onInitialize( ) {
		super.onInitialize();
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		final boolean boo=isExportSavedQueries();
		/**
		 * SavedQueries
		 */
		tabs.add(new AbstractTab( getQueriesTitle()) {
			public IModel<String> getTitle() {
				return getQueriesTitle();
			}
			@Override
			public Panel getPanel(String panelId) {
				SavedQueriesPanel pa = new SavedQueriesPanel(panelId, getConsoleKey(), getSiteModel(),  getQuery(), boo,  isAddToMain(), isActions) {
					protected void close(AjaxRequestTarget target) {
						BookmarksPanel.this.close(target);
					}
					@Override
					protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
						return BookmarksPanel.this.getGridExportSavedQueryMenuItem(id, model);
					}
				};
				pa.setIsClose(isClose());
				return pa;
			}
		});

		/**
		 * My Lists
		 */

		if (is_my_lists) { 
			tabs.add(new AbstractTab(getMyListTitle()) {
				@Override
				public IModel<String> getTitle() {
					return getMyListTitle();
				}
				@Override
				public Panel getPanel(String panelId) {
					 return new MyListsBasePanel(panelId, getConsoleKey(),getSiteModel(),  isActions) {
						 protected void close(AjaxRequestTarget target) {
							BookmarksPanel.this.close(target);
						 }
					};
					
				}
			});
		}
		
		AjaxTabbedPanel<ITab> xtabs= new AjaxTabbedPanel<ITab>("tabs", tabs) {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(BookmarksPanel.this.console+ "-" + BookmarksPanel.class.getSimpleName(), "selected_tab", getSelectedTab());
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(this.getTabs().get(getSelectedTab()).getTitle().getObject());
			}
			@Override
			protected String  getNavCss() {
				return "nav nav-buttons";
			}
		};
		add(xtabs);
	}

	protected IModel<Site> getSiteModel() {
		return site_model;
	}

	
	public void setExportSavedQueries(boolean b) {
		this.exportSavedQueries=b;
	}
	
	public boolean isExportSavedQueries() {
		return this.exportSavedQueries; 
	}
	
	protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
		return null;
	}
	
	protected Query getQuery() {
		return this.zquery;
	}

	protected String getConsoleKey() {
		return this.console;
	}

	public void onDetach() {
		super.onDetach();
		
		if (site_model!=null)
			site_model.detach();
	}
	
	protected void close(AjaxRequestTarget target) {
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}

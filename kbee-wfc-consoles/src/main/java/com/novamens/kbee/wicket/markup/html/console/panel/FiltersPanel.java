package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.Map;

import org.apache.wicket.Component;
 
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.event.SwitchPanelsEvent;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;


@SuppressWarnings("serial")
public class FiltersPanel extends ConsoleSidePanel {
			
	private static final long serialVersionUID = 1L;
	private Query query;
	private Searcher searcher;
	private FiltersPanelInternalContainerPanel applied;

	private boolean isFavoritesEnabled = true;
	
	public FiltersPanel(String id, Query query) {
		super(id);
		setOutputMarkupId(true);
		setQuery(query);
	}
	
	
	public void onUpdate(AjaxRequestTarget target) {}
	public void onClose(AjaxRequestTarget target) {}
	public void onFavorites(AjaxRequestTarget target) {}
	
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
	
	public Searcher getSearcher() {
		return searcher;	
	}
	
	public Map<String, Object> getParameters() {
		return getQuery().getParameters();
	}

	public void setParameters(Map<String, Object> parameters) {
		getQuery().setParameters(parameters);
		this.applied=null;
		onBeforeRender();
	}
		
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (FiltersPanel.this.applied==null) {
			FiltersPanel.this.applied = new FiltersPanelInternalContainerPanel("filters-panel-internal-container", getQuery()) {
				
				@Override
				protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
					FiltersPanel.this.saveQuery(target,title,parameters2);
				}
				
				@Override
				protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
					FiltersPanel.this.saveDashboardQuery(target,title,parameters2);
				}
				@Override
				public Searcher getSearcher() {
					return FiltersPanel.this.getSearcher();	
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					FiltersPanel.this.onUpdate(target);
				}
				@Override
				public void onClose(AjaxRequestTarget target) {
					FiltersPanel.this.onClose(target);
				}
				@Override
				protected boolean isVisible(Facet facet) {
					return FiltersPanel.this.isVisible(facet);
				}
			};
			
			FiltersPanel.this.applied.setConsoleName(getConsoleName());
			FiltersPanel.this.applied.setConsoleDisplayName(getConsoleDisplayName());

			addOrReplace(FiltersPanel.this.applied);
		}
	}
	
	
	
	 
	public boolean isFiltersApplied() {
		if (this.applied==null)
			return false;
		return !this.applied.getParametersPanel().isEmpty();
	}

	public boolean isFavoritesEnabled() {
		return this.isFavoritesEnabled;
	}
	
	public void setFavoritesEnabled( boolean d) {
		this.isFavoritesEnabled=d;
	}
	
	@Override
	public void reload(AjaxRequestTarget target) {
		if (getSearcher()!=null) {
			getSearcher().refresh();
			getSearcher().setQuery(getQuery());
			if (this.applied!=null) 
				this.applied.setQuery(getQuery());
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addMenu();
	}
	
	protected void addCloseLink() {
		
		 WorkingIndicatorAjaxLinkV5<Void> close= new WorkingIndicatorAjaxLinkV5<Void>("close") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
		 };
		 add(close);
	}
	
	protected void addMenu() {
		WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return false;
			}
		};
		menu.add(getMenu());
		add(menu);
	}
	
	protected Panel getMenu() {
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
		            fireScanAll(new SwitchPanelsEvent(target));
				}	
				@Override
				public String getLabel() {	
					return getLabelString("switch-sides");
				}
		});
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					onClose(target);
				}	
				@Override
				public String getLabel() {	
					return getLabelString("close");
				}
		});
		return menu;
	}

	

	public void clearAll(AjaxRequestTarget target) {
		if (FiltersPanel.this.applied!=null) {
			Component panel  =  this.applied.get("parameters");
			if (panel!=null && panel instanceof ParametersPanel ) {
				((ParametersPanel) panel).clearAll(target);
			}
		
		}
		

	}
	
	public void clearAll() {
		if (FiltersPanel.this.applied!=null) {
			Component panel  =  this.applied.get("parameters");
			if (panel!=null && panel instanceof ParametersPanel ) {
				((ParametersPanel) panel).clearAll();
			}
		
		}
		

	}

	
 	
	protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {	}
	protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {	}

	
	protected boolean isVisible(Facet facet) {
		return true;
	}
}

package kbee.web.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.query.SavedQuery;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;

public abstract class BaseBrowser<T>  extends KBPanel implements Browser<T> {
	private static final long serialVersionUID = 1L;
	
	private Query query;
	private Layout layout;
	private String consoleName;
	
	public BaseBrowser(String id, String consoleName, Query query) {
		super(id);
		this.consoleName=consoleName;
		setQuery(query);
		//addListeners();
	}
	
	public abstract boolean isRememberQuery();
	
	public abstract String getBrowserType();
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.layout!=null)
			this.layout.detach();
	}
	
	/**
	 * SolR Queries allows the Browser to include filters
	 * 
	 * @see 		
	 * {@link com.novamens.indexer.query.Query}
	 * {@link HibernateQuery}
	 * {@link ListQuery}
	 * {@link SolrQuery}
	 * 
	 * 
	 * 
	 * @return   
	 */
	@Override
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
	
	public long getIndex(T object) {
		return -1;
	}
	
	public abstract <P extends WebMarkupContainer> P getPanel(Class<P> panelclass);
	public abstract <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass);
	
	public <P extends WebMarkupContainer> int getDisposition(Class<P> panelclass) {
		return this.getLayout().getDisposition(panelclass);
	}
	
	public List<NavigationOrder> getOrders() {
		return new ArrayList<NavigationOrder>();
	}
	
	public List<IModel<T>> getSelection() {
		return null;
	}
	
	public void refresh(AjaxRequestTarget target) {
		target.add(this);
	}

	public void reload(AjaxRequestTarget target) {
		resetSelection();
		target.add(this);		
	}
	
	public void resetSelection() {
	}
	
	public boolean isRightPanelVisible() {
		return false;
	}

	public boolean isTopPanelVisible() {
		return false;
	}
	
	protected Layout getLayout() {
		return layout;
	}
	
	protected void setLayout(Layout layout) {
		this.layout = layout;
	}
								
	public String getConsoleKey()	 		{
		return this.consoleName;
	}
	
	public String getConsoleDisplayName()	{
		return this.consoleName;
	}

	protected void addListeners() {
	}

	@Override
	public List<ToolbarItem> getSelectionToolbarItems() {
		return null;
	}
	
	public boolean isMyListsEnabled() {
		return false;
	}
	
	protected boolean isSavedQueriesEnabled() {
		return false;
	}

	public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
		return null;
	}
}

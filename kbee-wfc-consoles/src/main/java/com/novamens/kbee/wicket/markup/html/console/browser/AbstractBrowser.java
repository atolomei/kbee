package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.user.UserService;

import com.novamens.event.Event;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

import com.novamens.wicket.markup.html.modal.Modal;

import kbee.web.console.BaseBrowser;
import kbee.web.console.Layout;
/**
 * 
 * {@link Console} contains Browser, which contains a {@link GridPanel} 
 *
 * @param <T>
 */
public abstract class AbstractBrowser<T> extends BaseBrowser<T> {
	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractBrowser.class.getName());
	
	public AbstractBrowser(String id, String consoleName, Query query) {
		super(id,  consoleName, query);
		setOutputMarkupId(true);
	}
	
	public <P extends WebMarkupContainer> int getDisposition(Class<P> panelclass) {
		return this.getLayout().getDisposition(panelclass);
	}
	
	@Override
	public <P extends WebMarkupContainer> P getPanel(Class<P> panelclass) {
		if (getLayout()==null)
			onBeforeRender();
		return getLayout().getPanel(panelclass);
	}
	
	public <P extends WebMarkupContainer> P getPanel(int disposition) {
		if (getLayout()==null)
			onBeforeRender();
		return getLayout().getPanel(disposition);
	}
	
	public <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass) {
		if (getLayout()==null)
			onBeforeRender();
		getLayout().togglePanel(panelclass);
	}
	
	public void addPanel(Panel panel) {
		if (getLayout()==null)
			onBeforeRender();
		getLayout().addPanel(panel);
	}
	
	public abstract String getBrowserType();
	
	@Override
	public long getIndex(T object) {
		@SuppressWarnings("unchecked")
		DataViewPanel<T> view = getPanel(DataViewPanel.class);
		long index = view.getIndex(object);
		return index;
	}
	
	@SuppressWarnings("unchecked")
	public List<IModel<T>> getSelection() {
		DataViewPanel<T> view = getPanel(DataViewPanel.class);
		List<IModel<T>> selection = view.getSelection();
		return selection;
	}
	
	@Override
	public void resetSelection() {
		@SuppressWarnings("unchecked")
		DataViewPanel<T> view = getPanel(DataViewPanel.class);
		view.resetSelection();
	}
	
	@Override
	public boolean isRightPanelVisible() {
		Panel sidepanel = AbstractBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
		if (sidepanel!=null && sidepanel.isVisible())
			return true;
		return false;
	}

	@Override			
	public boolean isTopPanelVisible() {
		Panel toppanel = AbstractBrowser.this.getPanel(AbstractLayout.TOP_DISPOSITION);
		if (toppanel!=null && toppanel.isVisible())
			return true;
		return false;
	}

	
	protected boolean isDefaultTopPanelVisible() {
			return false;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	
		if (getLayout()==null) {
			initPreferences();
			setLayout(newLayout());
			add(getLayout());
			add(newSaveQueryModal());
		}
	}	
	
	@Override
	public void reload(AjaxRequestTarget target) {
		super.reload(target);
		resetSelection();
		setLayout(newLayout());
		replace(getLayout());
		target.add(this);		
	} 

	
	
	protected String getContextKey() {
		return "/browser/";
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	/**
	 * We save preferences on the Database
	 * 
	 * @see KbeePreferences
	 * 
	 * @param name
	 * @param value
	 */
	public void setPreference(String name, String value) {
		getSessionUser().getService(PreferencesService.class).setValue(getConsoleKey() + "-browser", name, value);
	}
	
	
	/**
	 * 
	 * @see KbeePreferences
	 * 
	 * @param name
	 * @return
	 */
	public String getPreference(String name) {
		return getSessionUser().getService(PreferencesService.class).getValue(getConsoleKey() + "-browser", name, "null");
	}
	

	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		
		//logger.debug("Fire Scan All ->  " + event.getClass().getSimpleName());
		
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		
		fire(event, getPage().iterator(), false);
	}

	
	/**
	 * Scans Page and all its components
	 * The first Component that listens to this event will handle it
	 * 
	 **/
	@SuppressWarnings("unchecked")
	public void fire(Event event) {
		
		//logger.debug("Fire ->  " + event.getClass().getSimpleName());
		
		boolean handled=false;
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
					handled = true;
					break;
				}
			}
		if (!handled) 
			fire(event, getPage().iterator());
	}
	
	
	
	protected abstract Modal newSaveQueryModal();
	protected abstract Layout newLayout();
	protected abstract IModel<T> getModel(T object);
	
	protected abstract Panel getPanel(IModel<T> model, List<String> snippets);
	protected abstract Panel getPanel(IModel<T> model);
	
	protected abstract Panel getMenu(IModel<T> model);
	protected abstract List<GridColumn<SearchResult, String>> getColumns();
	
	protected Toolbar getToolbar() {
		return new Toolbar("toolbar", getToolbarItems());
	}

	/**
	 * top panel. for advanced search
	 * @return
	 */
	protected Panel getTopPanel() {
		return new InvisiblePanel("top");
	}

	/**
	 * hit panel (row) expander 
	 * @return
	 */
	protected boolean hasExpander() {
		return false;
	}

	/**
	 * force right panel to stay
	 * @return
	 */
	protected boolean isShowAlwaysTwoPanels() {
		return true;
	}
	
	/**
	 * enable to select rows
	 * @return
	 */
	protected boolean isSelectionEnabled() {
		return true;
	}
	
	protected boolean isSettingsEnabled() {
		return true;
	}

	protected abstract boolean isFiltersEnabled();
		
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return null;
	}
	
	protected  abstract String getDefaultUserPreference(String key);
	
	
	/**
	 * show the three dots contextual menu
	 * @return
	 */
	protected boolean isMenuEnabled() {
		return true;
	}
	
	protected abstract List<ToolbarItem> getToolbarItems();
	 
	protected void initPreferences() {																
	}


}
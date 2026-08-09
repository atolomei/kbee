package com.novamens.kbee.wicket.markup.html.console.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;

import com.novamens.content.service.domain.DomainPreferencesService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

@SuppressWarnings("serial")
public abstract class DataViewPanel<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private Query query;
	private Searcher searcher = null;
	
	private Map<Serializable, SearchResult> selection = new HashMap<Serializable, SearchResult>();
	
	static final protected int PAGE_SIZE = 25;
	private int page_size = PAGE_SIZE; 
	
	private String date_format;
	
	private ViewMode viewMode = ViewMode.ICON;
	
	@SuppressWarnings("unchecked")
	protected class SelectionModel implements IModel<Boolean> {
		private SearchResult result;
		public SelectionModel(SearchResult result) {
			this.result = result;
		}
		public Boolean getObject() {
			return selection.get(getId((T)result.getObject()))!=null;
		}
		public void setObject(Boolean value) {
			if (value) 
				selection.put(getId((T)result.getObject()), this.result);
			else
				selection.remove(getId((T)result.getObject()));
		}
		public void detach() {
		}
	}
	
	public class ErrorPanel extends Fragment {
		public ErrorPanel(String errorMsg) {
			super("row-container", "error-row-fragment", DataViewPanel.this);
			add( (new Label("msg", errorMsg)).setEscapeModelStrings(false));
		}
	}
	
	public DataViewPanel(String id, Query query) {
		super(id);
	}
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
	
	public Searcher getSearcher() {
		return searcher;
	}
	
	public void refresh(AjaxRequestTarget target) {
		if (getSearcher()!=null)
			getSearcher().refresh();
	}
	
	protected abstract IModel<T> getModel(T object);
	
	public ViewMode getViewMode() {
		return this.viewMode;
	}
	
	public void setViewMode(ViewMode vm) {
		this.viewMode=vm;
		String s=this.getUserPreference(getContextKey()+".view-mode"); 
		if (s==null || !s.equals(String.valueOf(vm.getId())))
			this.setUserPreference(getContextKey()+".view-mode", String.valueOf(vm.getId()));
	}
	
	public String getDateFormat() {
		return date_format;
	}

	public void setDateFormat(String dateFormat) {
		if (date_format==null || !date_format.equals(dateFormat)) {
			date_format=dateFormat;
			this.setUserPreference("date-format", dateFormat);
		}
	}

	public boolean isShowNullItems() {
		return false;
	}

	public void setShowNullRows(boolean b) {
	}
	
	@SuppressWarnings("unchecked")
	public void selectAll(boolean value) {
		Iterator<Item<SearchResult>> items = getItems();
		this.selection.clear();
		if (value)
			while (items.hasNext()) {
				Item<SearchResult> item = items.next();
				this.selection.put(getId((T)item.getModelObject().getObject()), item.getModelObject());
			}
	}
	
	public void resetSelection() {
		this.selection.clear();
	}
	
	@SuppressWarnings("unchecked")
	public List<IModel<T>> getSelection() {
		List<IModel<T>> selection = new ArrayList<IModel<T>>();
		for (SearchResult selected : this.selection.values()) {
			selection.add(getModel((T)selected.getObject()));
		}
		return selection;
	}
	
	@SuppressWarnings("unchecked")
	public long getIndex(T object) {
		Iterator<Item<SearchResult>> items = getItems();
		int i = 0;
		Serializable objectId = getId(object);
		while (items.hasNext()) {
			Item<SearchResult> item = items.next();
			T itemObject = (T)item.getModelObject().getObject();
			Serializable itemObjectId = itemObject!=null ? getId(itemObject) : null;
			if ((itemObjectId!=null && itemObjectId.equals(objectId)) || (itemObject!=null && itemObject.equals(object))) {
				long cp = getDataView().getCurrentPage();
				long ip = getDataView().getItemsPerPage();
				return i + cp*ip;
			}
			i++;
		}
		return -1;
	}
	
	public int getPageSize() {
		return page_size;
	}

	public void setPageSize(int value) {
		page_size = value;
		this.setIntUserPreference("page-size", value);
		if (getDataView()!=null) {
			getDataView().setItemsPerPage(value);
		}	
	}
	
	public long getCurrentPage() {
		return getDataView().getCurrentPage();
	}
	
	public long getPageCount() {
		return getDataView().getPageCount();
	}

	public void setCurrentPage(long page) {
		getDataView().setCurrentPage(page);
	}
	
	public boolean isFirstPage() {
		return getCurrentPage()==0;
	}
	
	public boolean isLastPage() {
		return getCurrentPage()==getPageCount();
	}
	
	public Iterator<Item<SearchResult>> getItems() {
		Iterator<Item<SearchResult>> items = getDataView().getItems();
		return items;
	}
	
	@SuppressWarnings("unchecked")
	protected Panel getPanel(SearchResult result) {
		 return getPanel(getModel((T) result.getObject()), result.getSnippets());
	}
	
	@Override
	public void onDetach() {
		if (this.searcher!=null)
		this.searcher.detach();
		for (SearchResult selected : this.selection.values()) {
			selected.detach();
		}	
		super.onDetach();
	}
	
	protected abstract Panel getPanel(IModel<T> model, List<String> snippets);
	
	protected abstract DataView<SearchResult> getDataView();
	
	protected List<ToolbarItem>  getSelectionToolbarItems()  {
		return null;
	}
	
	protected Panel getMenu(IModel<T> model) {
		return null;
	}
	
	protected boolean hasExpander() {
		return true;
	}

	protected boolean isSelectionEnabled() {
		return true;
	}
	
	protected boolean isMenuEnabled() {
		return true;
	}
	
	protected void showError(String message) {
		if (((WebMarkupContainer) get("container"))!=null) {
			List<String> messages = new ArrayList<String>();
			messages.add(message);
			DataView<String> dv = new DataView<String>("row", new ListDataProvider<String>(messages)) {
				@Override
				protected void populateItem(Item<String> item) {
					item.add(new ErrorPanel(item.getModelObject()));
				}
			};
			((WebMarkupContainer) get("container")).addOrReplace(dv);
		}
		else {
			addOrReplace(new InvisiblePanel("container"));
		}
	}
	
	protected Serializable getId(T object) {
		if (object instanceof Identifiable)
			return ((Identifiable)object).getId();
		return null;
	}
	
	protected String getContextKey() {
		return "dataview/";
	}
	
	protected String getUserPreference(String key) {
		KbeeUser user = getUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getValue(getContextKey(), key);
		return null;
	}
	
	protected String getUserPreference(String key, String defaultValue) {
		KbeeUser user = getUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getValue(getContextKey(), key, defaultValue);
		return null;
	}

	protected int getIntUserPreference(String key, int defaultvalue) {
		KbeeUser user = getUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getIntValue(getContextKey(), key, defaultvalue);
		return PAGE_SIZE;
	}

	protected void setUserPreference(String key, String value) {
		KbeeUser user = getUser();
		if (user==null) 
			return;
		String val =user.getService(PreferencesService.class).getValue(getContextKey(), key);
		if (val==null || value==null || !val.equals(value))
			user.getService(PreferencesService.class).setValue(getContextKey(), key, value);
	}

	protected void setIntUserPreference(String key, int value) {
		KbeeUser user = getUser();
		if (user==null) 
			return;
		int val =user.getService(PreferencesService.class).getIntValue(getContextKey(), key);
		if (val==-1 || val!=value) {
			user.getService(PreferencesService.class).setIntValue(getContextKey(), key, value);
		}
	}
	
	protected String getDefaultUserPreference(String key) {
		Domain domain = getDomain();
		if (domain==null)
			return null;
		DomainPreferencesService service = domain.getService(DomainPreferencesService.class);
		if (service!=null) {
			String str = service.getValue(getContextKey(), key); 
			return str;
		}
		return null;
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	public void setSearcher(Searcher seacher) {
			this.searcher=seacher;
	}
}

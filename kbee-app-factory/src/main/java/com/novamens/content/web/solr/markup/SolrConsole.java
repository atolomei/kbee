package com.novamens.content.web.solr.markup;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.InvisibleConsoleSidePanel;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.dashboard.LabelPanel;
import kbee.web.nav.DataManagementBC;



@SuppressWarnings("serial")
public class SolrConsole extends AbstractSimpleConsole<IndexerDocument> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrConsole.class.getName());

	
	private static final long serialVersionUID = 1L;
	
	private List<ToolbarItem> items = null;
	
	private class SolrColumn extends GridColumn<SearchResult, String> {
		public SolrColumn(String id, IModel<String> displayModel) {
			super(id, displayModel);
		}	
		public SolrColumn(String id, IModel<String> displayModel, String sortProperty) {
			super(id, displayModel, sortProperty);
		}	
		@Override
		protected IModel<String> getLabelModel(SearchResult result) {
			if (result.getObject()==null) 
				return new Model<String>("err");
			IndexerDocument document = (IndexerDocument)result.getObject();
			return  new Model<String>(document.getFieldValue(getId()).toString());
		}
		@Override
		protected String getContextKey() {
			return SolrConsole.this.getName() + super.getContextKey();
		}
	}
	
	public SolrConsole(String id, Query query) {
		super(id, query);
	}
	
	public IModel<String> getDisplayName() {
		return new Model<String>("Solr");
	}
	
	@Override
	public Panel getMenu(IModel<IndexerDocument> row) {
		return null;
	}
	
	@Override
	public Query newQuery() {
		return null;
	}
	
	public Domain getDomain() {
		return null;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		bc.addElement(new DataManagementBC());
		bc.addElement(new BCElement(new Model<String>("Solr Gateway")));
		add(bc);
	}
	
	public List<GridColumn<SearchResult, String>> getColumns() {
		List<GridColumn<SearchResult,String>> columns = new ArrayList<GridColumn<SearchResult,String>>();

		columns.add(new SolrColumn("id", getLabel("column.id")));
		columns.add(new SolrColumn("domain", getLabel("column.domain")));
		columns.add(new SolrColumn("title", getLabel("column.title"), "title"));
		columns.add(new SolrColumn("type", getLabel("column.type")));
	
		return columns;
	}
	
	@Override
	protected Panel getPanel(IModel<IndexerDocument> model, List<String> snippets) {
		Panel panel = new SolrDocumentPanel("editor", model.getObject());
		return panel;
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<IndexerDocument> browser) {
		
		if (this.items!=null) 
			return this.items;
			
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return SolrConsole.this.getName();}, new Model<String>(SolrConsole.this.getDescription()));
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		
		
		this.items.add(infoButton);
		
		this.items = new ArrayList<ToolbarItem>();
		
		return this.items;
	}
	
	@Override
	protected ConsoleSidePanel getRightPanel() {
		return new InvisibleConsoleSidePanel("side");
	}

	@Override
	public String getDownloadFileName() {
		DateTimeFormatter day_df = DateTimeFormatter.ofPattern("YYYY-MM-dd", getSessionUser().getLocale());
		return getDisplayName().getObject().toLowerCase().replace(" ", "-") +  day_df.format(OffsetDateTime.now());
	}

	protected void addHeader() {
		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		bc.addElement(new DataManagementBC());
		bc.addElement(new BCElement(new Model<String>("Solr Gateway")));
		add(bc);
	}
	
	@Override
	protected IModel<IndexerDocument> getModel(IndexerDocument row) {
		return new Model<IndexerDocument>(row);
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	protected boolean isMenuEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	@Override
	protected boolean hasExpander() {
		return true;
	}
	
	
	@Override
	public Page getConsolePage(Query query, long index) {
		throw new KbeeRuntimeException("not done");
	}
	
	protected String getPreference(String name) {
		String value = getSessionUser().getService(PreferencesService.class).getValue(getName() + "-browser", name);
		return value;
	}
	
	protected  void setPreference(String name, String value) {
		getSessionUser().getService(PreferencesService.class).setValue(getName() + "-browser", name, value);
	}

	@Override
	protected Panel getItemListPanel(IModel<IndexerDocument> model, int index) {
			return new LabelPanel("item", new Model<String> (model.getObject().toString()));
	}

}
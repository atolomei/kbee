package com.novamens.content.web.sql.markup;


import java.math.RoundingMode;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.BrowserNavigationToolbar;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class SQLDataPanel extends Panel {
				
	private static final long serialVersionUID = 1L;

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLDataPanel.class.getName());

	
	private SQLQueryResultsProvider dataProvider;

	private String query;
	private List<IColumn<SQLQuerySearchResult, String>> columns;
	private List<ToolbarItem> toolbarItems = null;
	private boolean iswide = true;
	 
	public SQLDataPanel(String id, String query) throws SQLException {
		super(id);
		setOutputMarkupId(true);
		this.query=query;
		addTable();
	}
	
	public void setToolbarItems(List<ToolbarItem> items) {
		this.toolbarItems = items;
	}
	
	public List<ToolbarItem> getToolbarItems() {
		return this.toolbarItems;
	}

	public SQLQueryResultsProvider getDataProvider() {
		return dataProvider;
	}
	
	@Override
	public void onDetach() {
		dataProvider.detach();
		this.columns=null;
		super.onDetach();
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (isWide())
			get("results").add( new AttributeModifier("style", "min-width:4400px;"));
		else
			get("results").add( new AttributeModifier("style", "width:100%;"));
	}
	
	
	
	protected boolean isWide() {
		return iswide;
	}
	
	
	public void setWide( boolean b) {
		this.iswide=b;
	}
	
	/**
	 * @throws SQLException
	 */
	private void addTable() throws SQLException {
		
		// Table
		this.dataProvider = new SQLQueryResultsProvider(query);
		DataTable<SQLQuerySearchResult, String> table = new DataTable<SQLQuerySearchResult, String>("results", getColumns(), dataProvider, 260);
		table.setOutputMarkupId(true);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (SQLQueryResultsProvider) table.getDataProvider()));

		long size = this.dataProvider.size();
		
		NumberFormat nint = NumberFormat.getInstance(getSessionUser().getLocale());
		nint.setMinimumFractionDigits(0);
		nint.setMaximumFractionDigits(0);
		nint.setRoundingMode(RoundingMode.HALF_UP);

		addOrReplace(new BrowserNavigationToolbar("navigator", table, nint.format(size)) {
			public List<ToolbarItem> getToolbarItems() {
				return SQLDataPanel.this.getToolbarItems();
			}
		});
		addOrReplace(table);
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private List<IColumn<SQLQuerySearchResult, String>> getColumns() throws SQLException {

		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<IColumn<SQLQuerySearchResult, String>>();
		
		if(this.dataProvider.getResultSet()!=null) {
			ResultSetMetaData metadatos = this.dataProvider.getResultSet().getMetaData();
			for(int i=1;i<=metadatos.getColumnCount();i++){
				String columnName = metadatos.getColumnName(i);
				final int j = i;
				this.columns.add(new AbstractColumn<SQLQuerySearchResult, String>(new Model<String>(columnName), columnName) {
					private static final long serialVersionUID = 1L;
					@Override
					public void populateItem(Item<ICellPopulator<SQLQuerySearchResult>> cellItem, String componentId, IModel<SQLQuerySearchResult> rowModel) {
						SQLQuerySearchResult searRes = rowModel.getObject();
						String valor=(String) searRes.getObjects().get(j-1);
						
						if(valor!=null)
							cellItem.add(new Label(componentId, valor));
						else
							cellItem.add(new Label(componentId, ""));
					}
				});
			}
		}
		else {
			this.columns.add(new AbstractColumn<SQLQuerySearchResult, String>(new Model<String>(" "), " ") {
				@Override
				public void populateItem(Item<ICellPopulator<SQLQuerySearchResult>> cellItem, String componentId, IModel<SQLQuerySearchResult> rowModel) {
					cellItem.add(new Label(componentId, ""));
				}
			});
		}
		
		return this.columns;
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}

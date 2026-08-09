package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.novamens.wicket.markup.html.repeater.util.NavigationToolbar;

@SuppressWarnings("serial")
public class BrowserNavigationToolbar extends NavigationToolbar {
	private static final long serialVersionUID = 1L;
	
	List<ToolbarItem> toolbarItems = null;
	
	public BrowserNavigationToolbar(String id, DataTable<?,?> table, String totalstr) {
		super(id, table, totalstr, false);
	}
	
	public BrowserNavigationToolbar(String id, DataTable<?,?> table) {
		super(id, table, String.valueOf(table.getItemCount()), false);
	}
	
	public List<ToolbarItem> getToolbarItems() {
		return toolbarItems;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("items")!=null)
			return;
		
		WebMarkupContainer items = new WebMarkupContainer("items") {
			public boolean isVisible() {
				return getToolbarItems()!=null && !getToolbarItems().isEmpty();
			}
		};
		
		items.add(new ListView<ToolbarItem>("item-container", getToolbarItems()) {
			public void populateItem(ListItem<ToolbarItem> item) {
				item.add(item.getModelObject());
			}
		});
		
		add(items);
	}	
}

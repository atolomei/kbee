package com.novamens.content.web.sql.markup;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.api.SystemInfoAPIDashboardPanel;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;

public class SQLFiltersPanel extends ModelPanel<Person> {
		
	private static final long serialVersionUID = 1L;

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLFiltersPanel.class.getName());
	
	private List<ToolbarItem> toolbarItems = null;
	private String query;
	private boolean isfilters = false;
	private boolean iswide = true;
	
	public SQLFiltersPanel(String id) {
		this(id, null);
	}
	
			
	public SQLFiltersPanel(String id, String query) {
		super(id);
		setModel(new ObjectModel<Person>(getPerson()));
		setOutputMarkupId(true);
		this.query=query;
	}
	
	public void setToolbarItems(List<ToolbarItem> items) {
		this.toolbarItems = items;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		if (get("filters")==null) {
			add(new InvisiblePanel("filters"));
			this.isfilters=false;
		}
		else
			this.isfilters=true;
		
		addComponents(this.query);
		
		add(new WicketEventListener<FilterSelectorEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(FilterSelectorEvent event) {
				try {
					Map<String, Object> map = event.getFilters();
					if (map!=null) {
						String qe= (String) map.get("query");
						if (qe!=null) {
							SQLDataPanel panel = new SQLDataPanel("data", qe);
							panel.setToolbarItems(toolbarItems);
							SQLFiltersPanel.this.addOrReplace(panel);
						}
					}
				} 
				catch (SQLException e) {
					logger.error(e);
					String msg=e.getMessage();
					SQLFiltersPanel.this.addOrReplace(new ErrorPanel("data", e.getClass().getName(), msg));
				} 	
				
				event.getRequestTarget().add(SQLFiltersPanel.this);
			}
		});
	}
	
	public void addFiltersSelectorPanel(Panel panel) {
		SQLFiltersPanel.this.addOrReplace(panel);
	}
	
	public void setWide(boolean b) {
		this.iswide=b;
	}
	
	private void addComponents(String query) {
		try {
			SQLDataPanel panel = new SQLDataPanel("data", query);
			panel.setWide(isWide());
			panel.setToolbarItems(toolbarItems);
			SQLFiltersPanel.this.addOrReplace(panel);			
			SQLFiltersPanel.this.add(new AttributeModifier("class", "panel " + (isFilters()?" with-filters": " without-filters")));
		} 
		catch (SQLException e) {
			logger.error(e);
			String msg=e.getMessage();
			SQLFiltersPanel.this.addOrReplace(new ErrorPanel("data", e.getClass().getName(), msg));
		} 	
	}
	
	protected boolean isWide() {
		return iswide;
	}
	
	private boolean isFilters() {
		return isfilters;
	}

	
}

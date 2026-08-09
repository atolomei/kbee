package com.novamens.kbee.wicket.markup.html.console.grid;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.markup.html.form.SortableBehavior;

import kbee.util.logging.Logger;


@SuppressWarnings("serial")
public class GridConfigPanel extends Panel {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(GridConfigPanel.class.getName());
	
	static private List<Integer> PAGE_SIZES  = new ArrayList<Integer>();
	static  {
		
		PAGE_SIZES.add(Integer.valueOf(10));
		PAGE_SIZES.add(Integer.valueOf(20));
		PAGE_SIZES.add(Integer.valueOf(25));
		PAGE_SIZES.add(Integer.valueOf(30));
		PAGE_SIZES.add(Integer.valueOf(40));
		PAGE_SIZES.add(Integer.valueOf(50));
		PAGE_SIZES.add(Integer.valueOf(80));
		PAGE_SIZES.add(Integer.valueOf(120));
		PAGE_SIZES.add(Integer.valueOf(240));
		PAGE_SIZES.add(Integer.valueOf(480));
		PAGE_SIZES.add(Integer.valueOf(600));
		PAGE_SIZES.add(Integer.valueOf(1200));
	 }
	
	static private List<ViewMode> ICON_SIZES  = new ArrayList<ViewMode>();
									
	static  {
		ICON_SIZES.add(ViewMode.NOIMAGE);
		ICON_SIZES.add(ViewMode.ICON);
		ICON_SIZES.add(ViewMode.THUMBNAIL);
	 }
	
	static private List<String> DATE_MODES  = new ArrayList<String>();
	
	static  {
		DATE_MODES.add(DateTimeService.COLlOQUIAL_AGO_LABEL);
		DATE_MODES.add(DateTimeService.COLlOQUIAL_LABEL);
		DATE_MODES.add(DateTimeService.MONTH_DAY_YEAR_LABEL);
		DATE_MODES.add(DateTimeService.FULL_LABEL);
		DATE_MODES.add(DateTimeService.TIMESTAMP_LABEL);
	 }

	private GridPanel<?> grid;
	private List<GridColumn<SearchResult, String>> columns, preferredColumns;
	private Integer pageSize;
	private ViewMode iconSize;
	private String dateFormat;
	
	private boolean is_created = false;
	
	
	/** 
	*
	*	
	*
	*/
	public GridConfigPanel(String id, GridPanel<?> grid) {
		super(id);

		setOutputMarkupId(true);
		
		this.grid = grid;
		this.pageSize = Integer.valueOf(grid.getPageSize());
		this.iconSize = grid.getViewMode();
		this.dateFormat = grid.getDateFormat();
		
		add(new Label("columns-view").setVisible(false));
		add(new Label("page-size").setVisible(false));
		add(new Label("icon-size").setVisible(false));
		add(new Label("date-format").setVisible(false));
		
	}
	

	
	public List<GridColumn<SearchResult,String>> getColumns() {
		if (this.columns == null) {
			this.columns = new ArrayList<GridColumn<SearchResult,String>>();
			for (GridColumn<SearchResult, String> column : getPreferredColumns()) {
				if (!column.isFixed()) 
						this.columns.add(column);
			}
			for (GridColumn<SearchResult, String> column : grid.getColumns()) {
				if (!column.isFixed()) {
					if (!isPreferred(column)) {
						if (!column.isOnlyForExpandedHitPanel()) 
							this.columns.add(column);
					}
				}
			}
		
			if (logger.isDebugEnabled()) 
				this.columns.forEach(item-> logger.error( item.getId().contains("_") ? ("Warning: col id -> " + item.getId() + " may fail SortableBehavior ") : ""));
		}	
		
		
		return this.columns;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateformat) {
		this.dateFormat = dateformat;
	}

	
	public List<GridColumn<SearchResult,String>> getPreferredColumns() {
		
		if (this.preferredColumns == null) {
			this.preferredColumns = new ArrayList<GridColumn<SearchResult,String>>();
			
			//if (logger.isDebugEnabled())
			//	this.grid.getPreferredColumns().forEach( item -> logger.debug(item.getContextKey()));
			
			this.preferredColumns.addAll(this.grid.getPreferredColumns());
		}	
		return this.preferredColumns;
	}

	
	
	public boolean isPreferred(GridColumn<SearchResult,String> column) {
		for (GridColumn<SearchResult, String> preferredcolumn : getPreferredColumns()) {
			if (preferredcolumn.getId().equals(column.getId()))
				return true;
		}
		return false;
	}

	
	
	public void setPreferred(GridColumn<SearchResult,String> column) {
		if (isPreferred(column)) 
			return;		
		getPreferredColumns().add(column);
		sortPreferredColumns();
	}

	
	
	public void removePreferred(GridColumn<SearchResult,String> column) {
		for (GridColumn<SearchResult, String> preferredcolumn : getPreferredColumns()) {
			if (preferredcolumn.getId().equals(column.getId())) {
				getPreferredColumns().remove(preferredcolumn);
				break;
			}
		}
	}

	
	
	public void onBeforeRender() {
		super.onBeforeRender();
		if (!this.is_created) {
			addRowsView();
			this.is_created =true;
		}
	}

	public int getPageSize() {
		return pageSize.intValue();
	}
	
	public List<String> getDateFormats() {
		return DATE_MODES;
	}

	public List<Integer> getPageSizes() {
		return PAGE_SIZES;
	}

	
	public List<ViewMode> getIconSizes() {
		return ICON_SIZES;
	}

	
	public ViewMode getIconSize() {
		return this.iconSize;
	}
	
	
	
	protected void addRowsView() {
		
		WebMarkupContainer view = new WebMarkupContainer("columns-view");
		
		view.setOutputMarkupId(true);
		view.add(new ListView<GridColumn<SearchResult,String>> ("column", new PropertyModel< List<GridColumn<SearchResult,String>>>(this, "columns")) {
			public void populateItem(final ListItem<GridColumn<SearchResult,String>> item) {
				IModel<Boolean> preferredmodel = new IModel<Boolean>() {
					public Boolean getObject() {
						return isPreferred(item.getModelObject());
					}
					public void setObject(Boolean value) {
						if (value)
							setPreferred(item.getModelObject());
						else
							removePreferred(item.getModelObject());
					}
					public void detach() {
						
					}
				};
				CheckBox preferredcheck = new AjaxCheckBox("selector", preferredmodel) {
					protected void onUpdate(AjaxRequestTarget target) {
					}
				};
				item.add(preferredcheck);
				item.add((new Label("name", item.getModelObject().getDisplayModel())).setEscapeModelStrings(false));
				item.add(new AttributeModifier("data-id", "resource_"+item.getModelObject().getId()));
				item.add(new WebMarkupContainer("menulink"));
				item.add(getMenu(item));
			}
		});
		
		view.add(new SortableBehavior() {
			@Override
			public void onSort(AjaxRequestTarget target, List<String> ids) {
				sortColumns(ids);
			}
			@Override
			public String getItemSelector() {
				return "li.media";
			}
		});
		addOrReplace(view);

		ExtendedChoiceField<String> dateformat = new ExtendedChoiceField<String>("date-format", new PropertyModel<String>(this, "dateFormat"), new PropertyModel<List<String>>(this, "dateFormats")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				
			}
			@Override
			public String getIdValue(String value) {
				return value.toString();
			}
			@Override
			public String getDisplayValue(String value) {
				return new StringResourceModel(value, this, null).getObject();
			}
		};

		addOrReplace(dateformat);
		
		ExtendedChoiceField<Integer> psize = new ExtendedChoiceField<Integer>("page-size", new PropertyModel<Integer>(this, "pageSize"), new PropertyModel<List<Integer>>(this, "pageSizes")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setPageSize( getValue() );
			}
			@Override
			public String getIdValue(Integer value) {
				return value.toString();
			}
			@Override
			public String getDisplayValue(Integer value) {
				return value.toString();
			}
		};
		
		
		addOrReplace(psize);
							
		
		ExtendedChoiceField<ViewMode> iconsize = new ExtendedChoiceField<ViewMode>("icon-size", new PropertyModel<ViewMode>(this, "iconSize"), new PropertyModel<List<ViewMode>>(this, "iconSizes")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
			}
			@Override
			public String getIdValue(ViewMode value) {
				return String.valueOf(value.getId());
			}
			@Override
			public String getDisplayValue(ViewMode value) {
				return value.getDisplayName( getSessionUser().getLocale());
			}
		};
		
		addOrReplace(iconsize);
		
	}
		
	
	protected void setPageSize(Integer value) {
			this.pageSize=value;
	}


	/**
	 * 
	 * @param item
	 * @return
	 */
	protected Panel getMenu(final ListItem<GridColumn<SearchResult,String>> item) {
		ContextMenuPanel<GridColumn<SearchResult,String>> menu = new ContextMenuPanel<GridColumn<SearchResult,String>>(item.getModel());
		menu.addItem(new MenuItemFactory<GridColumn<SearchResult,String>>() {
			@Override
			public AbstractMenuItemPanelV5<GridColumn<SearchResult,String>> getItem(String id) {
				return new AjaxCheckMenuItemPanelV5<GridColumn<SearchResult,String>>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						up(getModelObject());
						sortPreferredColumns();
						target.add(GridConfigPanel.this);
					}
					@Override
					public String getLabel() {	
						return (new StringResourceModel("menu.up", GridConfigPanel.this, null)).getObject();
					}
					@Override
					public boolean isVisible() {	
						return item.getIndex()>0;
					}
				};
			}
		});
	
		
		menu.addItem(new MenuItemFactory<GridColumn<SearchResult,String>>() {
			@Override
			public AbstractMenuItemPanelV5<GridColumn<SearchResult,String>> getItem(String id) {
				return new AjaxMenuItemPanelV5<GridColumn<SearchResult,String>>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						down(getModelObject());
						sortPreferredColumns();
						target.add(GridConfigPanel.this);
					}
					@Override
					public String getLabel() {	
						return (new StringResourceModel("menu.down", GridConfigPanel.this, null)).getObject();
					}
					@Override
					public boolean isVisible() {	
						return item.getIndex()<getColumns().size()-1;
					}
				};
			}
		});
		
		
		
		
		
		menu.addItem(new MenuItemFactory<GridColumn<SearchResult,String>>() {
			@Override
			public AbstractMenuItemPanelV5<GridColumn<SearchResult,String>>  getItem(String id) {
				return new SeparatorMenuItemPanelV5<GridColumn<SearchResult,String>> (id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<GridColumn<SearchResult,String>>() {
			@Override
			public AbstractMenuItemPanelV5<GridColumn<SearchResult,String>> getItem(String id) {
				return new AjaxMenuItemPanelV5<GridColumn<SearchResult,String>>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						makeFirst(getModelObject());
						sortPreferredColumns();
						target.add(GridConfigPanel.this);
					}

					@Override
					public String getLabel() {
						return (new StringResourceModel("move-to-first", GridConfigPanel.this, null)).getObject();
					}
					@Override
					public boolean isVisible() {	
						return item.getIndex()<getColumns().size()-1;
					}
				};
			}
		});


		
		menu.addItem(new MenuItemFactory<GridColumn<SearchResult,String>>() {
			@Override
			public AbstractMenuItemPanelV5<GridColumn<SearchResult,String>> getItem(String id) {
				return new AjaxMenuItemPanelV5<GridColumn<SearchResult,String>>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						makeLast(getModelObject());
						sortPreferredColumns();
						target.add(GridConfigPanel.this);
					}
					@Override
					public String getLabel() {
						return (new StringResourceModel("move-to-last", GridConfigPanel.this, null)).getObject();
					}
					@Override
					public boolean isVisible() {	
						return item.getIndex()<getColumns().size()-1;
					}
				};
			}
		});

		
		
		
		
		
		
		
		
		return menu;
		
		
	}

	
	protected void up(GridColumn<SearchResult, String> upcolumn) {
		int i = 0;
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (upcolumn.getId().equals(column.getId())) {
				if (i>0) {
					GridColumn<SearchResult, String> previouscolumn = getColumns().get(i-1);
					getColumns().set(i-1, upcolumn);
					getColumns().set(i, previouscolumn);
					break;
				}
			}
			else {
				i++;
			}
		}
	}

	
	
	private void makeFirst(GridColumn<SearchResult, String> col) {
		int i = 0;
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (col.getId().equals(column.getId())) {
				if (i>0) {
					//getColumns().set(0, col);
					//GridColumn<SearchResult, String> nextcolumn = getColumns().get(i+1);
					//getColumns().set(i+1, upcolumn);
				}
				break;
			}
			else {
				i++;
			}
		}
	}


	private void makeLast(GridColumn<SearchResult, String> col) {
		int i = 0;
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (col.getId().equals(column.getId())) {
				if (i< (getColumns().size()-1)) {
					//getColumns().set(0, col);
					//GridColumn<SearchResult, String> nextcolumn = getColumns().get(i+1);
					//getColumns().set(i+1, upcolumn);
				}
				break;
			}
			else {
				i++;
			}
		}
	}

 

	
	protected void down(GridColumn<SearchResult, String> upcolumn) {
		int i = 0;
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (upcolumn.getId().equals(column.getId())) {
				if (i<getColumns().size()-1) {
					GridColumn<SearchResult, String> nextcolumn = getColumns().get(i+1);
					getColumns().set(i+1, upcolumn);
					getColumns().set(i, nextcolumn);
					break;
				}
			}
			else {
				i++;
			}
		}
	}

	
	protected void sortColumns(List<String> ids) {
		List<GridColumn<SearchResult, String>> sortedcolumns;
		sortedcolumns = new ArrayList<GridColumn<SearchResult,String>>();
		for (String id : ids) {
			for (GridColumn<SearchResult, String> column : getColumns()) {
				if (column.getId().equals(id)) {
					sortedcolumns.add(column);
					break;
				}
			}
		}
		this.columns = sortedcolumns;
		sortPreferredColumns();
	}

	
	protected void sortPreferredColumns() {
		List<GridColumn<SearchResult, String>> sortedcolumns;
		sortedcolumns = new ArrayList<GridColumn<SearchResult,String>>();
		for (GridColumn<SearchResult, String> column : getColumns()) {
			if (isPreferred(column))
				sortedcolumns.add(column);
		}
		this.preferredColumns = sortedcolumns;
	}

	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}

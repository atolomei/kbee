package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;

/**
 *
 *  @param <T>
 *            the type of the object that will be rendered in this column's cells
 * @param <S>
 *            the type of the sort property
 *            
 *            
 *            Important: It seems that there is a bug in the SortableBehavior js and columns with a "_" in the id cause the grid config panel to fail 
 *            (see: SortableBehavior:
 *                  public void onSort(AjaxRequestTarget target, List<String> ordered)) .
 * 
 */
 
@SuppressWarnings("serial")
public abstract class GridColumn<T, S> extends AbstractColumn<T, S> {

	
	static final public int DEFAULT_COLUMN_TEXT_WIDTH = 800;
	static final public int DEFAULT_COLUMN_WIDTH = 362;
	static final public int DEFAULT_TITLE_COLUMN_WIDTH = 420;
	
	static private final long serialVersionUID = 1L;

	private int defaultWidth = DEFAULT_COLUMN_WIDTH;
	
	private String id;
	private String labelcss;
	
	private boolean is_header_menu = true;
	
	private boolean initialized = false;
	private boolean is_visible=false;
	private boolean is_enabled=true;
	private int width = 0;

	private String grid_date_format;
	private String date_format;
	private boolean is_expanded=true;
	private boolean is_preferred=true;
	private boolean onlyForExpandedHitPanel = false;

	private String grid_context_key = "grid2";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridColumn.class.getName());
	
	
	public GridColumn(String id, IModel<String> displayModel) {
		this(id, displayModel,  null);
	}
	
	public GridColumn(String id, IModel<String> displayModel, S sortProperty) {
		super(displayModel, sortProperty);
		this.id = id;
	}
	
	
	public GridColumn(String id, IModel<String> displayModel, S sortProperty, String grid_context_key) {
		super(displayModel, sortProperty);
		this.id = id;
		this.grid_context_key= grid_context_key;
	}
	
	public String getContextKeyDebug() {
		return this.getContextKey();
	}
	
	
	
	private void popItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> model, boolean isExpanded) {
				
		IModel<String> labelModel;
		
		try {

			if (!isExpanded)
				labelModel = getLabelModel(model.getObject());
			else
				labelModel = getExpandedLabelModel(model.getObject());
		}
		
		catch (Exception e) {
			logger.error(e);
			labelModel = new Model<String>(e.getClass().getSimpleName());	
		}
		
		cellItem.add(new GridColumnPanel(componentId,labelModel) {
			@Override
			protected boolean isEscapeModelString() {
				return  GridColumn.this.isEscapeModelString(); 
			}
			
			@Override
			protected String getCellContainerCss() {
				return GridColumn.this.getCellContainerCss();
			}
	
			@Override
			protected String getLabelCss() {
				String s=GridColumn.this.getLabelCss(model);
				if (s!=null)
					return s;
				return GridColumn.this.getLabelCss();
			}
		});
		
 		Behavior modifier = getModifier();
		
		if (modifier!=null)	
			cellItem.add(modifier);
	}
 	
	
	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> model) {
		popItem(cellItem, componentId, model, false);
	}

	
	/**
	 *  Sometimes we need to trunc the grid cell (HTMLValueResolver) but the Expanded panel should display all the info.
	 *  @see {@link ExpandedPanel}
	 *  
	 */
	public void populateItemExpanded(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> model) {
		popItem(cellItem, componentId, model, true);
		}

	
	protected String getCellContainerCss() {
		return null;
	}
	
	public  void setLabelCss(String lcss) {
		this.labelcss=lcss;
	}
	
	
	protected String getLabelCss() {
		return this.labelcss;
	}
	
	
	protected String getLabelCss(IModel<T> model) {
		return null;
	}

	
	public boolean isEscapeModelString() {
		return false;
	}

	public String getId() {
		return id;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getWidth() {
		if (!initialized) {
			width = getUserPreference("width", getDefaultWidth());
			initialized = true;
		}
		return width;
	}
	
	public void onResize(int width) {
		setUserPreference("width", width);
		setWidth(width);
	}
	
	public boolean isResizable() {
		return true;
	}
	
	public void setPreferred(boolean b) {
		this.is_preferred = b;
	}
	
	public boolean isPreferred() {
		return this.is_preferred;
	}
	
	public void setDateFormat(String df) {
		this.date_format=df;
	}
	
	public String getDateFormat() {
		return this.date_format;
	}
	
	public void setGridDateFormat(String df) {
		this.grid_date_format=df;
	}
	
	public String getGridDateFormat() {
		return this.grid_date_format;
	}


	protected IModel<String> getExpandedLabelModel(T object) {
		return getLabelModel(object); 
	}

	
	protected IModel<String> getLabelModel(T object) {
		return new Model<String>(object.toString());
	}

	
	protected String getContextKey() {
		return "/"+grid_context_key+"/"+getId();
	}
	
	protected Behavior getModifier() {
		return null;
	}
	
	protected void setUserPreference(String key, int value) {
		KbeeUser user = getUser();
		if (user==null) 
			return;
		int val = user.getService(PreferencesService.class).getIntValue(getContextKey(), key);
		if (val!=value)
			user.getService(PreferencesService.class).setIntValue(getContextKey(), key, value);
	}
	
	protected int getUserPreference(String key, int default_value) {
		KbeeUser user = getUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getIntValue(getContextKey(), key, default_value);
		return 0;
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	/** 
	 * @return whether the column can not be moved or disabled.
	 */
	public boolean isFixed() {
		return false;
	}

	
	public int getXPadding() {
		return 15;
	}
	
	/** 
	 * @return whether the column exists for the expanded HitPanel, but not for Grids
	 */
	public boolean isOnlyForExpandedHitPanel() {
		return onlyForExpandedHitPanel;
	}

	public void setOnlyForExpandedHitPanel(boolean onlyForExpandedHitPanel) {
		this.onlyForExpandedHitPanel = onlyForExpandedHitPanel;
	}

	public String getHeaderCssClass() {
		return null;
	}

	
	public String getRowCssClass() {
		return null;
	}
	
	@Override
	public String getCssClass()	{
		return "col col-xs-1 col-md-1 col-lg-1";
	}

	
	public void setExpaned(boolean expanded) {
		is_expanded = expanded;
	}
	
	/**
	 * whether this column should be rendered on the expanded hitpanel
	 * @return
	 */
	public boolean isExpanded() {
		return is_expanded;
	}

	/**
	 * <p>This method is used when the Grid is exported. If the Cell has not been populated via getLabelModel() then this method must be overriden.</p>
	 * <p>Another case may be when getLabelModel() returns a String in HTML, and the exporting tool does not want to export HTML.</p>
	 * <p>for example, if the label is:  span class="enabled">Enabled</span>
	 * We may want to export just: Enabled </p>
	 * 
	 * @param result
	 */
	public IModel<String> getCellAsString(T result) {
		return getLabelModel(result);
	}

	public String getCssClass(T object) {
		return this.getCssClass();
	}

	
	
	public void setDefaultWidth(int w) {
		this.defaultWidth=w;
	}
	
	public int getDefaultWidth() {
		return defaultWidth;
	}
	
	

	
	public boolean isVisible() {
		return this.is_visible;
	}
	
	public void setVisible(boolean b) {
		is_visible=b;
		
	}

	public int getHeaderWidth() {
		return getWidth();
	}
	
	
	/**
	 * <p>
	 *  true: xls, csv export will erxport this column
	 * false: xls, csv export will not consider this column
	 * <p>
	 * 
	 */
	public boolean isExportable() { 
		return true; 
	}

	
	public void setEnabled(boolean b) {
		this.is_enabled=b;
	}
	
	public boolean isEnabled() {
		return this.is_enabled;
	}


	
	public void setHeaderMenu(boolean b) {
		this.is_header_menu=b;
	}
	
	/**
	 * Publishes the hamburger menu on the col header
	 * 
	 * @return
	 */
	public boolean isHeaderMenu() {
		return this.is_header_menu;
	}
	

}

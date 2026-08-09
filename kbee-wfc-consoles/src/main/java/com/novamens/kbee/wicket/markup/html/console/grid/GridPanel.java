package com.novamens.kbee.wicket.markup.html.console.grid;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import java.util.StringTokenizer;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.value.IValueMap;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.interaction.resizable.IResizableListener;
import com.googlecode.wicket.jquery.ui.interaction.resizable.ResizableBehavior;
import com.novamens.content.base.Content;
import com.novamens.content.model.LabelMember;
import com.novamens.content.userlist.UserListItem;

import com.novamens.datetime.DateTimeService;
import com.novamens.event.Event;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.Toolbar;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.event.QueryChangeEvent;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.actions.TwoButtonsMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Field;

/**
 * <p>
 * Selector
 * Menu
 * Fixed 1..N de la Console
 * <p>
 * "hasselector"
 * "noselector"
 * <p>
 * isSelectionEnabled()
 * Selector w:138px
 * <p>
 * isMenuEnabled()
 * hasExpander()
 *
 * </p>
 */
@SuppressWarnings("serial")
public abstract class GridPanel<T> extends DataViewPanel<T> {
    
	private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridPanel.class.getName());

    public final static int TITLE_DEFAULT_WIDTH = 480;
    						
    public final static int ICON_COL_WIDTH = 34;
    

    private RowPanel selected;
    private List<GridColumn<SearchResult, String>> columns, visibleColumns, preferredColumns, fixedColumns;

    private GridDisplayMode display_mode = GridDisplayMode.COMPACT_GRID_NO_BCK;
    private GridDisplayMode defaultGridDisplayMode = GridDisplayMode.COMPACT_GRID_NO_BCK;

    private List<GridColumn<SearchResult, String>> not_visible_cols = null;
    
    private boolean show_title_expanded = true;
    private boolean selectall;
    private int system_cols_width = 0;
    private int total_width = 0;

    private boolean show_null_rows = true;


    /** -----------------------------------------
     * 
     *
     */

    private class SelectionFragment extends Fragment {
        public SelectionFragment(String id, IModel<Boolean> model) {
            super(id, "selector-fragment", GridPanel.this);
            setOutputMarkupId(true);
            CheckBox selected = new AjaxCheckBox("selector", model) {
                protected void onUpdate(AjaxRequestTarget target) {
                    SelectionFragment.this.onUpdate(target);
                }

                @Override
                public boolean isEnabled() {
                    return isSelectionEnabled();
                }
            };
            add(selected);
        }

        protected void onUpdate(AjaxRequestTarget target) {
        }
    }



    /** -----------------------------------------
     * 
     *
     */

    private class MenuFragment extends Fragment {

        private IModel<T> model;
        Panel menupanel;
        WebMarkupContainer dropdown;

        public MenuFragment(String id, IModel<T> model) {
            super(id, "menu-fragment", GridPanel.this);
            this.model = model;
            Serializable objid = GridPanel.this.getId(model.getObject());


            dropdown = new WebMarkupContainer("dropdown");
            dropdown.setOutputMarkupId(true);
            WebMarkupContainer menulink = new WebMarkupContainer("menulink");
            menulink.add(new AttributeModifier("id", String.valueOf(objid)));
            dropdown.add(menulink);
            add(dropdown);

            menupanel = getMenu(getModel());
            
            if (menupanel != null) {
                menupanel.setOutputMarkupId(true);
                menupanel.add(new AttributeModifier("aria-labelledby", String.valueOf(objid)));
                dropdown.add(menupanel);
            } else {
                menulink.setVisible(false);
                dropdown.add((new Label("menu")).setVisible(false));
            }
            
        }

        public IModel<T> getModel() {
            return model;
        }

        @Override
        public void onDetach() {
            this.model.detach();
            super.onDetach();
        }
    }


    /** -----------------------------------------
     * 
     *
     */

    private class HeaderMenuFragment extends Fragment {

        private Serializable cid = null;
        private String zname = null;

        public HeaderMenuFragment(String id, GridColumn<?, ?> column) {
            super(id, "header-menu-fragment", GridPanel.this);

            this.cid = column.getId();
            this.zname = column.getDisplayModel().getObject();

            ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>("col-menu", null);


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new AjaxMenuItemPanelV5<Void>(id) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            try {

                                GridColumn<SearchResult, String> coltoremove = null;
                                for (GridColumn<SearchResult, String> col : getPreferredColumns()) {
                                    if (col.getId().equals(cid)) {
                                        coltoremove = col;
                                        break;
                                    }
                                }

                                if (coltoremove != null) {
                                    List<GridColumn<SearchResult, String>> list = getPreferredColumns();
                                    list.remove(coltoremove);
                                    setPreferredColumns(list);
                                    visibleColumns = null;
                                    not_visible_cols = null;
                                    fire(new RefreshClickEvent(target));
                                }

                                FeedbackHelper.showInfoToast(getLabel(), zname);

                            } catch (Exception e) {

                                logger.error(e);
                                FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                            }
                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("hide", GridPanel.this, null).getObject();
                        }
                    };
                }
            });


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new SeparatorMenuItemPanelV5<Void>(id) {
                        @Override
                        public boolean isVisible() {
                            return true;
                        }

                        @Override
                        public String getCssClass() {
                            return "divider";
                        }
                    };
                }
            });


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {

                    SubmenuAjaxItemPanelV5<Void> submenu = new SubmenuAjaxItemPanelV5<Void>(id) {

                        @Override
                        protected String getMenuStyle() {
                            return "border-bottom: none";
                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("insert-column-right", GridPanel.this, null).getObject();
                        }

                        protected void addItems() {
                            for (GridColumn<SearchResult, String> x_col : getAvailableColumns()) {

                                if (!x_col.getId().equals(HeaderMenuFragment.this.cid)) {

                                    final String sna = x_col.getDisplayModel().getObject();
                                    final String sid = x_col.getId();


                                    addItem(new MenuItemFactory<Void>() {
                                        @Override
                                        public AbstractMenuItemPanelV5<Void> getItem(String id) {
                                            return new AjaxMenuItemPanelV5<Void>(id) {
                                                public void onClick(AjaxRequestTarget target) {

                                                    List<GridColumn<SearchResult, String>> list = getPreferredColumns();
                                                    List<GridColumn<SearchResult, String>> n_list = new ArrayList<GridColumn<SearchResult, String>>();

                                                    for (GridColumn<SearchResult, String> c : list) {
                                                        n_list.add(c);
                                                        if (c.getId().equals(HeaderMenuFragment.this.cid))
                                                            n_list.add(x_col);
                                                    }

                                                    setPreferredColumns(n_list);

                                                    visibleColumns = null;
                                                    not_visible_cols = null;


                                                    FeedbackHelper.showInfoToast(new StringResourceModel("insert-column-right", GridPanel.this, null).getObject(), sna);
                                                    fire(new RefreshClickEvent(target));


                                                }

                                                @Override
                                                public String getLabel() {
                                                    return sna;
                                                }
                                            };
                                        }
                                    });
                                }
                            }
                        }
                    };

                    return submenu;
                }
            });


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new SeparatorMenuItemPanelV5<Void>(id) {
                        @Override
                        public boolean isVisible() {
                            return true;
                        }

                        @Override
                        public String getCssClass() {
                            return "divider";
                        }
                    };
                }
            });


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {

                    return new TwoButtonsMenuItemPanelV5<Void>(id, "fal fa-chevron-left", "fal fa-chevron-right") {
                        @Override
                        public void onRightClick(AjaxRequestTarget target) {
                            try {
                                GridColumn<SearchResult, String> coltoup = null;
                                for (GridColumn<SearchResult, String> col : getPreferredColumns()) {
                                    if (col.getId().equals(cid)) {
                                        coltoup = col;
                                        break;
                                    }
                                }

                                if (coltoup != null) {
                                    int i = 0;
                                    List<GridColumn<SearchResult, String>> list = getPreferredColumns();
                                    for (GridColumn<SearchResult, String> column : list) {
                                        if (coltoup.getId().equals(column.getId())) {
                                            if (i < list.size() - 1) {
                                                GridColumn<SearchResult, String> nextcolumn = list.get(i + 1);
                                                list.set(i + 1, coltoup);
                                                list.set(i, nextcolumn);
                                                break;
                                            }
                                        } else {
                                            i++;
                                        }
                                    }
                                    setPreferredColumns(list);
                                    FeedbackHelper.showInfoToast(getLabel(), zname);
                                    fire(new RefreshClickEvent(target));
                                }

                            } catch (Exception e) {
                                logger.error(e);
                                FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                            }
                        }


                        @Override
                        public void onLeftClick(AjaxRequestTarget target) {
                            try {

                                GridColumn<SearchResult, String> coltoup = null;
                                for (GridColumn<SearchResult, String> col : getPreferredColumns()) {
                                    if (col.getId().equals(cid)) {
                                        coltoup = col;
                                        break;
                                    }
                                }

                                if (coltoup != null) {
                                    int i = 0;
                                    List<GridColumn<SearchResult, String>> list = getPreferredColumns();
                                    for (GridColumn<SearchResult, String> column : list) {
                                        if (coltoup.getId().equals(column.getId())) {
                                            if (i > 0) {
                                                GridColumn<SearchResult, String> previouscolumn = list.get(i - 1);
                                                list.set(i - 1, coltoup);
                                                list.set(i, previouscolumn);
                                                break;
                                            }
                                        } else {
                                            i++;
                                        }
                                    }
                                    setPreferredColumns(list);
                                    FeedbackHelper.showInfoToast(getLabel(), zname);
                                    fire(new RefreshClickEvent(target));
                                }


                            } catch (Exception e) {
                                logger.error(e);
                                FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                            }
                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("move", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getLeftLabel() {
                            return null;
                            //return new StringResourceModel("move-left", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getRightLabel() {
                            return null;
                            //return new StringResourceModel("move-right", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getLeftTitleAttribute() {
                            return new StringResourceModel("move-left", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getRightTitleAttribute() {
                            return new StringResourceModel("move-right", GridPanel.this, null).getObject();
                        }
                    };
                }
            });


            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {
                    return new SeparatorMenuItemPanelV5<Void>(id) {
                        @Override
                        public boolean isVisible() {
                            return true;
                        }

                        @Override
                        public String getCssClass() {
                            return "divider";
                        }
                    };
                }
            });

            menu.addItem(new MenuItemFactory<Void>() {
                @Override
                public AbstractMenuItemPanelV5<Void> getItem(String id) {

                    return new TwoButtonsMenuItemPanelV5<Void>(id, "fal fa-plus", "fal fa-minus") {

                        // return new TwoButtonsMenuItemPanelV5<Void>(id) {
                        //protected boolean isBorderTop() 				{return false;	}
                        //protected boolean isBorderBottom()	 			{return false;	}
                        //protected boolean isBorderButtonSeparator() 	{return false;	}


                        @Override
                        public void onRightClick(AjaxRequestTarget target) {
                            try {
                                GridColumn<SearchResult, String> coltoup = null;
                                for (GridColumn<SearchResult, String> col : getPreferredColumns()) {
                                    if (col.getId().equals(cid)) {
                                        coltoup = col;
                                        break;
                                    }
                                }

                                if (coltoup != null) {
                                    coltoup.onResize(Double.valueOf(coltoup.getWidth() * 0.80).intValue());
                                    FeedbackHelper.showInfoToast(getLabel(), zname);
                                    fire(new RefreshClickEvent(target));
                                }

                            } catch (Exception e) {
                                FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                                logger.error(e);
                            }
                        }


                        @Override
                        public void onLeftClick(AjaxRequestTarget target) {
                            try {
                                GridColumn<SearchResult, String> coltoup = null;
                                for (GridColumn<SearchResult, String> col : getPreferredColumns()) {
                                    if (col.getId().equals(cid)) {
                                        coltoup = col;
                                        break;
                                    }
                                }

                                if (coltoup != null) {
                                    coltoup.onResize(Double.valueOf(coltoup.getWidth() * 1.20).intValue());
                                    FeedbackHelper.showInfoToast(getLabel(), zname);
                                    fire(new RefreshClickEvent(target));
                                }
                            } catch (Exception e) {
                                logger.error(e);
                                FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                            }
                        }

                        @Override
                        public String getLeftTitleAttribute() {
                            return new StringResourceModel("increase-10", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getRightTitleAttribute() {
                            return new StringResourceModel("reduce-10", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getLabel() {
                            return new StringResourceModel("width", GridPanel.this, null).getObject();
                        }


                        @Override
                        public String getLeftLabel() {
                            return null;
                            //return new StringResourceModel("increase-10", GridPanel.this, null).getObject();
                        }

                        @Override
                        public String getRightLabel() {
                            return null;
                            //return new StringResourceModel("reduce-10", GridPanel.this, null).getObject();
                        }


                    };
                }
            });

            add(menu);
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }
    }


    /**
     *
     */
    private class ExpanderFragment extends Fragment {
        @SuppressWarnings("unchecked")
        public ExpanderFragment(String id) {
            super(id, "expander-fragment", GridPanel.this);
            add(new WebMarkupContainer("expander"));
            get("expander").add(new AjaxEventBehavior("click") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    boolean expanded = false;
                    RowPanel row = (RowPanel) ExpanderFragment.this.getParent().getParent().getParent();
                    if (selected != null && selected.getRowId().equals(row.getRowId())) {
                        expanded = false;
                        selected = null;
                    } else if (selected == null)
                        expanded = true;
                    if (selected != null && !selected.getRowId().equals(row.getRowId())) {
                        selected.setExpanded(false);
                        target.add(selected);
                        expanded = true;
                    }
                    row.setExpanded(expanded);
                    target.add(row);
                    if (expanded) {
                        selected = row;
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onBeforeRender() {
            super.onBeforeRender();
            RowPanel row = (RowPanel) ExpanderFragment.this.getParent().getParent().getParent();
            if (selected != null && selected.getRowId().equals(row.getRowId())) {
                get("expander").add(new AttributeModifier("class", "far fa-angle-down"));
            } else {
                get("expander").add(new AttributeModifier("class", "far fa-angle-up"));
            }
        }
    }


    /**
     * Selector
     */
    public class SelectorColumn extends GridColumn<SearchResult, String> {
        public SelectorColumn() {
            super("selection", new StringResourceModel("action", GridPanel.this, null), "");
            setHeaderMenu(false);
        }

        public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> model) {
            cellItem.add(new SelectionFragment(componentId, new SelectionModel(model.getObject())) {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    fire(new SelectionEvent(target));
                }
            });
        }

        @Override
        public boolean isFixed() {
            return true;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        public String getCssClass() {
            return "selector cell-container";
        }

        @Override
        public int getWidth() {
            return 42;
        }

        
        		
        @Override
        public int getHeaderWidth() {
            return hasExpander() ? HEADER_WIDTH_HAS_EXPANDER  : HEADER_WIDTH_SIMPLE;
        }

        @Override
        public int getXPadding() {
            return 0;
        }

        @Override
        public boolean isExportable() {
            return false;
        }
    }
    
    
    static final int HEADER_WIDTH_HAS_EXPANDER =  128;
    static final int HEADER_WIDTH_SIMPLE = 86;

    /**
     * Menu
     */
    public class MenuColumn extends GridColumn<SearchResult, String> {
        public MenuColumn() {
            super("menu", new Model<String>(""), "");
            setHeaderMenu(false);
        }

        @SuppressWarnings("unchecked")
        public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
        	try {
	            Object object = resultmodel.getObject().getObject();
	            if (object != null) {
	                IModel<T> objectmodel = getModel((T) object);
	                cellItem.add(new MenuFragment(componentId, objectmodel));
	            } else {
	                cellItem.add(new Label(componentId, "null"));
	            }
        	} catch (Exception e) {
        		logger.error(e);
        		cellItem.add(new Label(componentId, e.getClass().getName()));
        	}
        }

        @Override
        public boolean isFixed() {
            return true;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        public String getCssClass() {
            return "menu cell-container";
        }

        @Override
        public int getWidth() {
            return 44;
        }

        @Override
        public int getHeaderWidth() {
            return isSelectionEnabled() ? 0 : getWidth();
        }

        @Override
        public int getXPadding() {
            return 0;
        }

        @Override
        public boolean isExportable() {
            return false;
        }
    }


    /**
     *
     */
    public class ExpanderColumn extends GridColumn<SearchResult, String> {

        public ExpanderColumn() {
            super("expander", new Model<String>(""), "");
            setHeaderMenu(false);
        }

        public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
            cellItem.add(new ExpanderFragment(componentId));
        }

        @Override
        public boolean isFixed() {
            return true;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        public String getCssClass() {
            return "expander cell-container";
        }

        @Override
        public int getWidth() {
            return 42;
        }

        @Override
        public int getHeaderWidth() {
            return isSelectionEnabled() ? 0 : getWidth();
        }

        @Override
        public int getXPadding() {
            return 0;
        }

        @Override
        public boolean isExportable() {
            return false;
        }
    }

    /**
     *
     */

    private class ColumnsProvider implements IDataProvider<ICellPopulator<SearchResult>> {
        @Override
        public Iterator<ICellPopulator<SearchResult>> iterator(long first, long count) {
            List<ICellPopulator<SearchResult>> populators = new ArrayList<ICellPopulator<SearchResult>>((int) count);
            for (GridColumn<SearchResult, String> column : getVisibleColumns()) {
                populators.add(column);
            }
            return populators.iterator();
        }

        @Override
        public long size() {
            return getVisibleColumns().size();
        }

        public IModel<ICellPopulator<SearchResult>> model(ICellPopulator<SearchResult> object) {
            return new Model<ICellPopulator<SearchResult>>(object);
        }

        @Override
        public void detach() {
        }
    }

    ;

	public class ErrorRowPanel extends Fragment {
		public ErrorRowPanel(String errorMsg) {
			super("row-container", "error-row-fragment", GridPanel.this);
			add( (new Label("msg", errorMsg)).setEscapeModelStrings(false));
		}
		public ErrorRowPanel(Exception e) {
			super("row-container", "error-row-fragment", GridPanel.this);
			add( (new Label("msg", e.getClass().getName() + " | " + e.getMessage())).setEscapeModelStrings(false));
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
    public class RowPanel extends Fragment {

        private boolean expanded = false;
        private String id;
        IModel<SearchResult> rowmodel;

        protected void onNullModelObject(IModel<SearchResult> rowmodel) {
            add(new DataView<ICellPopulator<SearchResult>>("td", new ColumnsProvider(), getVisibleColumns().size()) {

                public void populateItem(Item<ICellPopulator<SearchResult>> item) {
                    item.add((new Label("td-container", "search index error")).setVisible((item.getIndex() == 0)));
                    fire(new GridPanelNullObjectEvent<SearchResult>(rowmodel));
                    if (expanded) {
                        setVisible(false);
                    }
                }

                public boolean isVisible() {
                    return GridPanel.this.isShowNullItems();
                }
            });

            add(new WebMarkupContainer("expanded-row") {
                public boolean isVisible() {
                    return false;
                }
            });

            add(new AttributeModifier("class", new Model<String>() {
                public String getObject() {
                    return "row-container";
                }
            }));
        }


        @SuppressWarnings("unchecked")
        public RowPanel(final IModel<SearchResult> rowmodel) {
            super("row-container", "row-fragment", GridPanel.this);

            setOutputMarkupId(true);

            if (rowmodel == null) {
                logger.error("rowmodel is null. Try Reindexing");
                onNullModelObject(rowmodel);
                setVisible(GridPanel.this.isShowNullItems());
                return;
            } else if (rowmodel.getObject() == null) {
                logger.error("rowmodel.getObject() is null. Try Reindexing");
                onNullModelObject(rowmodel);
                setVisible(GridPanel.this.isShowNullItems());
                return;
            } else if (rowmodel.getObject().getObject() == null) {
                logger.error("rowmodel.getObject().getObject() is null. Try Reindexing");
                onNullModelObject(rowmodel);
                setVisible(GridPanel.this.isShowNullItems());
                return;
            }

            this.rowmodel = rowmodel;

            setRowId(String.valueOf(((T) rowmodel.getObject().getObject()).hashCode()));

            int visible_cols = getVisibleColumns().size();

            if (visible_cols == 0) {
                logger.error("getVisibleColumns() has zero columns. Trying to display 1 column.");
                visible_cols++;
            }

            add(new DataView<ICellPopulator<SearchResult>>("td", new ColumnsProvider(), visible_cols) {

                public void populateItem(Item<ICellPopulator<SearchResult>> item) {

                    GridColumn<SearchResult, ?> column = (GridColumn<SearchResult, ?>) item.getModelObject();

                    column.setExpaned(expanded);

                    if (column instanceof DateColumn) {
                        ((DateColumn<?>) column).setUserVisible(
                                GridPanel.this.getGridDisplayMode() == GridDisplayMode.COMFORTABLE ||
                                        GridPanel.this.getGridDisplayMode() == GridDisplayMode.COMFORTABLE_GRID_NO_BCK ||
                                        GridPanel.this.getGridDisplayMode() == GridDisplayMode.COMFORTABLE_NO_BCK ||
                                        GridPanel.this.getGridDisplayMode() == GridDisplayMode.COMFORTABLE_GRID);
                    }
                    try {
                        item.getModelObject().populateItem(item, "td-container", rowmodel);
                    } catch (Exception e) {
                        logger.error(e);
                        item.add(new CellErrorPanel("td-container", e.getClass().getSimpleName() + " | " + e.getMessage()));
                    }

                    StringBuilder str = new StringBuilder();

                    String css = null;


                    if (rowmodel.getObject() != null && rowmodel.getObject().getObject() != null) {

                        try {
                            //Object o = rowmodel.getObject().getObject();
                            //T t = (T) o;
                            if (column.getCssClass(rowmodel.getObject()) != null)
                                css = column.getCssClass(rowmodel.getObject());
                        } catch (Exception e) {
                            logger.error(e);
                        }

                    }

                    if (css == null && column.getCssClass() != null)
                        css = column.getCssClass();
                    if (css != null)
                        str.append(css);


                    if (column.getRowCssClass() != null)
                        str.append(" " + column.getRowCssClass());

                    if (str.length() > 0)
                        item.add(new AttributeModifier("class", str.toString()));

                    if (column.isEscapeModelString())
                        item.setEscapeModelStrings(true);

                    if (column.getWidth() > 0)
                        item.add(new AttributeModifier("style", "width:" + String.valueOf(column.getWidth()) + "px;"));

                    if (expanded) {
                        if (column.getId().equals("title")) {
                            item.setVisible(GridPanel.this.isShowTitleExpanded());
                            // ------------------------------------------------
                            // aca agrego la botonera en la columna title ??
                            // ------------------------------------------------
                        } else if (!column.isFixed())
                            item.setVisible(false);
                    }
                }
            });

            add(new WebMarkupContainer("expanded-row") {
                public boolean isVisible() {
                    return expanded;
                }
            });

            final String rccss = getRowContainerCss(rowmodel);
            add(new AttributeModifier("class", new Model<String>() {
                public String getObject() {
                    String css = (rccss != null ? rccss : "");
                    return css + (expanded ? " row-container expanded " : " row-container");
                }
            }));
        }

        public String getRowId() {
            return id;
        }

        public void setRowId(String id) {
            this.id = id;
        }

        public void setExpanded(boolean value) {
            expanded = value;
            if (expanded) {
                Panel panel = getPanel(rowmodel.getObject());
                if (panel != null) {
                    panel.add(new AttributeModifier("style", "width: fit-content; margin-left: " + String.valueOf(getSystemColsWidth()) + "px;"));
                    ((WebMarkupContainer) get("expanded-row")).addOrReplace(panel);
                } else {
                    logger.error("panel is null");
                }
            }
        }

        public boolean expanded() {
            return expanded;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (get("td") instanceof DataView<?>) {
                DataView<?> view = (DataView<?>) get("td");
                Iterator<?> items = view.getItems();
                while (items.hasNext())
                    ((Item<?>) items.next()).detach();
            }
        }
    }

    /** -----------------------------------------------------------
     * 
     * 
     * 
     * @param id
     * @param query
     * @param columns
     */
    public GridPanel(String id, Query query, List<GridColumn<SearchResult, String>> columns) {
        super(id, query);
        setOutputMarkupId(true);
        setColumns(columns);
        setQuery(query);
    }


    /**
     * @param rowmodel
     * @return
     */
    protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
        return null;
    }

    public boolean isShowTitleExpanded() {
        return this.show_title_expanded;
    }

    public void setShowTitleExpanded(boolean b) {
        this.show_title_expanded = b;
    }

//	public void selectAll(boolean value) {
//		Iterator<Item<SearchResult>> items = ((DataView<SearchResult>)get("container:row")).getItems();
//		this.selection.clear();
//		if (value)
//			while (items.hasNext()) {
//				Item<SearchResult> item = items.next();
//				this.selection.put(getId((T)item.getModelObject().getObject()), item.getModelObject());
//			}
//	}

    public List<GridColumn<SearchResult, String>> getColumns() {
        return this.columns;
    }


    

    public List<GridColumn<SearchResult, String>> getAvailableColumns() {
        if (not_visible_cols != null)
            return not_visible_cols;

        not_visible_cols = new ArrayList<GridColumn<SearchResult, String>>();

        for (GridColumn<SearchResult, String> col : getColumns()) {
            if (!col.isVisible() && !col.isFixed() && !col.isOnlyForExpandedHitPanel()) {
                not_visible_cols.add(col);
            }
        }

        not_visible_cols.sort(new Comparator<GridColumn<SearchResult, String>>() {
            @Override
            public int compare(GridColumn<SearchResult, String> a, GridColumn<SearchResult, String> b) {
                String av = (a != null && a.getDisplayModel() != null && a.getDisplayModel().getObject() != null) ? a.getDisplayModel().getObject() : "";
                String bv = (b != null && b.getDisplayModel() != null && b.getDisplayModel().getObject() != null) ? b.getDisplayModel().getObject() : "";
                return av.compareToIgnoreCase(bv);
            }
        });

        return not_visible_cols;


    }


    public List<GridColumn<SearchResult, String>> getFixedColumns() {
        if (this.fixedColumns != null)
            return this.fixedColumns;
        this.fixedColumns = new ArrayList<GridColumn<SearchResult, String>>();
        for (GridColumn<SearchResult, String> col : getColumns()) {
            if (col.isFixed())
                this.fixedColumns.add(col);
        }
        return this.fixedColumns;
    }


    /**
     * Preferred columns that are not fixed (fixed columns where added before)
     **/
    public List<GridColumn<SearchResult, String>> getPreferredColumns() {

        if (this.preferredColumns != null)
            return this.preferredColumns;

        this.preferredColumns = new ArrayList<GridColumn<SearchResult, String>>();
        String preferences = getUserPreference("columns");

        if (preferences == null) {
            preferences = getDefaultUserPreference("columns");
            logger.debug("default preferences: " + preferences);
        }

        //if (logger.isDebugEnabled()) {
        //	for (GridColumn<SearchResult, String> column : getColumns()) {
        //		logger.debug(column.getId()+ " " + column.isPreferred());
        //	}
        //}

        if (preferences == null) {
            for (GridColumn<SearchResult, String> column : getColumns()) {
                if (column.isPreferred() && !column.isFixed() && column.isEnabled() && preferredColumns.size() < 15) {
                    this.preferredColumns.add(column);
                }
            }
        } else {
            StringTokenizer tokenizer = new StringTokenizer(preferences, ";");

            while (tokenizer.hasMoreTokens()) {
                String columnid = tokenizer.nextToken();
                for (GridColumn<SearchResult, String> column : getColumns()) {
                    if (column.getId().equals(columnid) && column.isEnabled()) {
                        this.preferredColumns.add(column);
                    }
                }
            }
        }
        return this.preferredColumns;
    }


    public void setPreferredColumns(List<GridColumn<SearchResult, String>> columns) {

        StringBuilder preferences = new StringBuilder();

        for (GridColumn<SearchResult, String> column : columns) {
            if (preferences.length() > 0)
                preferences.append(";");
            preferences.append(column.getId());
        }
        setUserPreference("columns", preferences.toString());

        this.preferredColumns = null;
        this.visibleColumns = null;
    }

    public void resetColumns() {
        this.visibleColumns = null;
        this.preferredColumns = null;
        this.fixedColumns = null;
    }

    public List<GridColumn<SearchResult, String>> getVisibleColumns() {

        if (this.visibleColumns == null) {
            this.visibleColumns = new ArrayList<GridColumn<SearchResult, String>>();

            this.visibleColumns.addAll(getFixedColumns());
            this.visibleColumns.addAll(getPreferredColumns());

            int n = 0;

            if (this.isSelectionEnabled())
                this.visibleColumns.add(n++, new SelectorColumn());

            if (this.isMenuEnabled())
                this.visibleColumns.add(n++, new MenuColumn());

            if (hasExpander())
                this.visibleColumns.add(n, new ExpanderColumn());


            if (this.visibleColumns.size() == 0)
                logger.error("Visible columns can not be 0" + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());


            for (GridColumn<SearchResult, String> col : this.visibleColumns) {
                if (col.isEnabled())
                    col.setVisible(true);
                else
                    col.setVisible(false);
                col.setGridDateFormat(getDateFormat());
            }
        }
        return this.visibleColumns;
    }

    public void setColumns(List<GridColumn<SearchResult, String>> columns) {
        this.columns = columns;
        this.visibleColumns = null;
        this.preferredColumns = null;
        this.fixedColumns = null;
    }

    public GridDisplayMode getGridDisplayMode() {
        return this.display_mode;
    }

    public void setGridDisplayMode(GridDisplayMode mode) {
        this.display_mode = mode;
        getUser().getService(PreferencesService.class).setValue(getConsoleKey() + "/" + GridPanel.class.getSimpleName(), "displaymode", mode.getRsLabel());
    }

    public void setSelectall(boolean sall) {
        this.selectall = sall;
    }

    public boolean getSelectall() {
        return this.selectall;
    }

    @Override
    public boolean isShowNullItems() {
        return this.show_null_rows;
    }

    @Override
    public void setShowNullRows(boolean b) {
        this.show_null_rows = b;
    }

    public void setDefaultGridDisplayMode(GridDisplayMode mode) {
        defaultGridDisplayMode = mode;
    }

    public GridDisplayMode getDefaultGridDisplayMode() {
        return defaultGridDisplayMode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataView<SearchResult> getDataView() {
        return (DataView<SearchResult>) get("container:row");
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Field.class, "jquery.scrollbar.js")));

        String s = "jQuery('.scrollbar-external').scrollbar({" +
                " \"autoScrollSize\": false," +
                " \"scrollx\": $('.external-scroll_x')," +
                "});";

        response.render(OnDomReadyHeaderItem.forScript(s));
    }

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

        if (get("container") == null) {
            addContainer();
            addHeader();
            addGrid();
        }

        for (GridColumn<SearchResult, String> column : getColumns()) {
            column.setGridDateFormat(getDateFormat());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (get("container:row") != null && (get("container:row") instanceof DataView)) {
            @SuppressWarnings("unchecked")
            DataView<SearchResult> view = (DataView<SearchResult>) get("container:row");

            if (view != null) {
                Iterator<Item<SearchResult>> items = view.getItems();
                while (items.hasNext()) {
                    items.next().detach();
                }
                view.detach();
            }
        }
        for (GridColumn<SearchResult, String> column : columns)
            column.detach();
    }

    protected String getConsoleKey() {
        return "";
    }

    protected String getConsoleDisplayName() {
        return "";
    }

    protected void addContainer() {
        WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        container.add(new AttributeModifier("class", new Model<String>() {
            @Override
            public String getObject() {
                return "container " + GridPanel.this.getGridDisplayMode().getCss();
            }
        }));

        add(container);
    }


    protected WebMarkupContainer getColumnMenu(GridColumn<?, ?> column) {

        try {

            // hide
            // move left
            // move right


            if (!column.isHeaderMenu())
                return new InvisiblePanel("col-menu");

            if (column.getId().equals("selection"))
                return new InvisiblePanel("col-menu");

            if (column.getId().equals("menu"))
                return new InvisiblePanel("col-menu");

            if (column.getId().equals("expander"))
                return new InvisiblePanel("col-menu");

            //if (column.getId().equals("unread"))
            //	return new InvisiblePanel("col-menu");


            //boolean isSortIcon = false;

            boolean isSortIcon = column.getSortProperty() != null && !"".equals(column.getSortProperty()) &&
                    (column.getSortProperty().toString().equals(getQuery().getParameters().get("sort")) ||
                            (getQuery().getParameters().get("sort") != null && ((String) getQuery().getParameters().get("sort")).startsWith(column.getSortProperty().toString())));


            HeaderMenuFragment header = new HeaderMenuFragment("col-menu", column);
            header.add(new AttributeModifier("class", "header-col-menu " + (isSortIcon ? " issort-icon" : "")));

            return header;


        } catch (Exception e) {
            logger.error(e);
        }

        return new InvisiblePanel("col-menu");


    }

    /**
     * Header
     */
    protected void addHeader() {

        initPreferences();

        ((WebMarkupContainer) get("container")).add(new AttributeModifier("class",
                new Model<String>() {
                    public String getObject() {
                        return "container " + getGridDisplayMode().getCss() + " " + (GridPanel.this.isSelectionEnabled() ? " hasselector" : " noselector");
                    }
                }));

        ((WebMarkupContainer) get("container")).add(new ListView<GridColumn<?, ?>>("header", new PropertyModel<List<GridColumn<?, ?>>>(this, "visibleColumns")) {

            @Override
            public void onBeforeRender() {
                GridPanel.this.system_cols_width = 0;
                GridPanel.this.total_width = 0;
                super.onBeforeRender();
            }

            public void populateItem(ListItem<GridColumn<?, ?>> item) {

                final int index = item.getIndex();
                final GridColumn<?, ?> column = item.getModelObject();

                if (column instanceof ImageColumn && getGridDisplayMode() == GridDisplayMode.COMPACT) {
                    item.setVisible(false);
                    return;
                }


                item.add(getColumnMenu(column));


                // Selection -------------------------------------------------------------
                //
                if (column.getId().equals("selection")) {


                    WebMarkupContainer sall = new WebMarkupContainer("selectall") {
                        @Override
                        public boolean isVisible() {
                            return GridPanel.this.isSelectionEnabled();
                        }
                    };
                    item.add(sall);
                    sall.add(new AjaxCheckBox("check", new PropertyModel<Boolean>(GridPanel.this, "selectall")) {
                        protected void onUpdate(AjaxRequestTarget target) {
                            GridPanel.this.selectAll(GridPanel.this.getSelectall());
                            onSelectAll(target);
                        }

                        @Override
                        public boolean isEnabled() {
                            return GridPanel.this.isSelectionEnabled();
                        }
                    });

                    List<ToolbarItem> list = getSelectionToolbarItems();

                    WebMarkupContainer sactions = new WebMarkupContainer("selection-actions") {
                        @Override
                        public boolean isVisible() {
                            return GridPanel.this.isSelectionEnabled();
                        }
                    };

                    sactions.add(new Label("selection-actions-label", column.getDisplayModel()) {
                        @Override
                        public boolean isVisible() {
                            return GridPanel.this.hasExpander();
                        }

                    });

                    item.add(sactions);

                    if (list != null) {
                        sactions.add(new Toolbar("menu", list));
                    } else
                        sactions.add(new InvisiblePanel("menu"));
                }
                // End Selection -------------------------------------------------------------
                else {

                    item.add(new Panel("selectall") {
                        public boolean isVisible() {
                            return false;
                        }
                    });

                    item.add(new Panel("selection-actions") {
                        public boolean isVisible() {
                            return false;
                        }
                    });
                }


                // -----------------
                //
                // GridColumn<?,?>
                //
                WorkingIndicatorAjaxLinkV5<Void> titlecol = new WorkingIndicatorAjaxLinkV5<Void>("column-title") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean isVisible() {
                        return !column.getId().equals("selection");
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            setSort(column.getSortProperty().toString());
                            fire(new QueryChangeEvent(target, getQuery()));
                            target.add(GridPanel.this);

                        } catch (Exception e) {
                            logger.error(e, "click on title column");

                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        return (column.getSortProperty() != null);
                    }

                    @Override
                    protected String getWorkingLabel() {
                        return column.getDisplayModel().getObject();
                    }
                };

                titlecol.add(new Label("label", column.getDisplayModel()));

                item.add(titlecol);

                item.get("column-title:label").setEscapeModelStrings(false);
                item.setMarkupId(column.getId());
                StringBuilder style = new StringBuilder();

                StringBuilder str = new StringBuilder("header headerWithMenu");

                if (column.getHeaderCssClass() != null)
                    str.append(column.getHeaderCssClass());

                if (column.getCssClass() != null)
                    str.append(" " + column.getCssClass());

                if (column.getSortProperty() == null)
                    str.append(" " + " no-sorting");

                item.add(new AttributeModifier("class", str.toString()));

                if (column.getHeaderWidth() > 0)
                    style.append("width:" + String.valueOf(column.getHeaderWidth()) + "px;");

                if (style.length() > 0)
                    item.add(new AttributeModifier("style", style.toString()));

                addTotalWidth(column.getWidth() + column.getXPadding());

                if (column.isFixed())
                    addSystemColsWidth(column.getWidth() + column.getXPadding());

                titlecol.add(new WebMarkupContainer("sorted-icon") {
                    @Override
                    public boolean isVisible() {
                        return column.getSortProperty() != null && !"".equals(column.getSortProperty()) &&
                                (column.getSortProperty().toString().equals(getQuery().getParameters().get("sort")) ||
                                        (getQuery().getParameters().get("sort") != null && ((String) getQuery().getParameters().get("sort")).startsWith(column.getSortProperty().toString())));
                    }

                    @Override
                    protected void onComponentTag(final ComponentTag tag) {
                        IValueMap attributes = tag.getAttributes();
                        String ascending = (String) getQuery().getParameters().get("ascending");
                        if ("true".equals(ascending)) {
                            attributes.put("class", "far fa-angle-down caret-sort");
                        } else {
                            attributes.put("class", "far fa-angle-up caret-sort");
                        }
                        super.onComponentTag(tag);
                    }
                });

                
                AbstractDefaultAjaxBehavior callback = new AbstractDefaultAjaxBehavior() {
                    @Override
                    protected void respond(AjaxRequestTarget target) {
                        int width = RequestCycle.get()
                            .getRequest()
                            .getRequestParameters()
                            .getParameterValue("w")
                            .toInt();

                        column.onResize(width);
                    }
                };

                item.add(callback);
                
                if (column.isResizable()) {
                    Options options = new Options();
                    options.set("minWidth", 42);
                    options.set("maxWidth", 1920);
                    options.set("minHeight", 20);
                    options.set("maxHeight", 20);
                    IResizableListener resizelistener = new IResizableListener() {
                        @Override
                        public void onResizeStop(AjaxRequestTarget target, int top, int left, int width, int height) {
                            //column.onResize(width);
                        	String js = String.format(
                        		    "var w = Math.round($('#%s').outerWidth());" +
                        		    "Wicket.Ajax.get({u: '%s&w=' + w});" +
                        		    "$('.grid-row').each(function(i, obj) {" +
                        		    "  $(obj).children().eq(%d).css('width', w + 'px');" +
                        		    "});",
                        		    item.getMarkupId(),
                        		    callback.getCallbackUrl(),
                        		    index
                        		);
                            target.appendJavaScript(js);
                        }

                        @Override
                        public void onResizeStart(AjaxRequestTarget target, int top, int left, int width, int height) {
                        }

                        @Override
                        public boolean isResizeStopEventEnabled() {
                            return true;
                        }

                        @Override
                        public boolean isResizeStartEventEnabled() {
                            return false;
                        }
                    };
                    
                    ResizableBehavior behavior = new ResizableBehavior("#" + item.getModelObject().getId(), options, resizelistener);
                    item.add(behavior);
                    

                }
            }
        });
    }


    /**
     * If the SearchResult or the SearchResult.getObject() is null
     * then it add a row with the Label "err" on the 1st column.
     */
    protected void addGrid() {
        try {
            DataView<SearchResult> dv = new DataView<SearchResult>("row", getSearcher(), getPageSize()) {
                @Override
                protected void populateItem(Item<SearchResult> item) {
                	try {
                		item.add(new RowPanel(item.getModel()));
                	} catch (Throwable e) {
                		logger.error(e);
                		item.add(new ErrorRowPanel(e.getClass().getName()));
                		
                	}
                }
            };
            
            ((WebMarkupContainer) get("container")).add(dv);
            
        } catch (Exception e) {
            logger.error(e, "The grid can not be rendered due to an Application error please call support ");
       
            showError(new StringResourceModel("error-msg", GridPanel.this, null).getString() +
                    e.getClass().getCanonicalName() + "<br />" + e.getMessage() + (e.getCause() != null ? (" | " + e.getCause()) : "") + "</p>");
        }
    }

    /**
     * @param item
     */

    protected void setSort(String sortProperty) {
        Query query = null;
        try {
            query = getSearcher().getQuery();
            query.getParameters().put("sort", sortProperty);
            String ascending = (String) query.getParameters().get("ascending");
            if ("true".equals(ascending)) {
                query.getParameters().put("ascending", "false");
            } else {
                query.getParameters().put("ascending", "true");
            }
        } catch (Exception e) {
            logger.error(e);
            if (query != null)
                query.getParameters().remove("sort");
        }
    }

    protected Panel getPanel(IModel<T> object) {
        return null;
    }

    protected IModel<T> getModel(T object) {
        return null;
    }


    /**
     * Scans Page and all its components
     * The first Component that listens to this event will handle it
     **/
    @SuppressWarnings("unchecked")
    public void fire(Event event) {

        logger.debug("Fire " + event.getClass().getSimpleName());

        boolean handled = false;
        for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
            if (listener.handle(event)) {
                listener.onEvent(event);
                handled = true;
                break;
            }
        }
        if (!handled) {
            fire(event, getPage().iterator(), false);
        }
    }

    protected void onSelectAll(AjaxRequestTarget target) {
    }

    @Override
    protected String getContextKey() {
        return "grid/";
    }

    private void addSystemColsWidth(int colwidth) {
        this.system_cols_width += colwidth;
    }

    private void addTotalWidth(int colwidth) {
        this.total_width += colwidth;
    }

    private void initPreferences() {

        String dm = getUser().getService(PreferencesService.class).getValue(getConsoleKey() + "/" + GridPanel.class.getSimpleName(), "displaymode", getDefaultGridDisplayMode().getRsLabel());

        if 		(dm.equals(GridDisplayMode.COMPACT.getRsLabel())) 				this.display_mode = GridDisplayMode.COMPACT;
        else if (dm.equals(GridDisplayMode.COMPACT_NO_BCK.getRsLabel())) 		this.display_mode = GridDisplayMode.COMPACT_NO_BCK;
        else if (dm.equals(GridDisplayMode.COMPACT_GRID.getRsLabel())) 			this.display_mode = GridDisplayMode.COMPACT_GRID;
        else if (dm.equals(GridDisplayMode.COMPACT_GRID_NO_BCK.getRsLabel())) 	this.display_mode = GridDisplayMode.COMPACT_GRID_NO_BCK;

        else if (dm.equals(GridDisplayMode.COMFORTABLE.getRsLabel())) 				this.display_mode = GridDisplayMode.COMFORTABLE;
        else if (dm.equals(GridDisplayMode.COMFORTABLE_NO_BCK.getRsLabel())) 		this.display_mode = GridDisplayMode.COMFORTABLE_NO_BCK;
        else if (dm.equals(GridDisplayMode.COMFORTABLE_GRID.getRsLabel())) 			this.display_mode = GridDisplayMode.COMFORTABLE_GRID;
        else if (dm.equals(GridDisplayMode.COMFORTABLE_GRID_NO_BCK.getRsLabel())) 	this.display_mode = GridDisplayMode.COMFORTABLE_GRID_NO_BCK;

        else this.display_mode = getDefaultGridDisplayMode();

        /**
         * do not call setDateFormat() here because it will save the value to the Database, which is redundant
         **/
        String df = this.getUserPreference("date-format");
        setDateFormat(df != null ? df : DateTimeService.COLlOQUIAL_AGO_LABEL);

        int vm = getIntUserPreference("view-mode", ViewMode.ICON.getId());

        if (ViewMode.ICON.getId() == vm) {
            setViewMode(ViewMode.ICON);
        } else if (ViewMode.THUMBNAIL.getId() == vm) {
            setViewMode(ViewMode.THUMBNAIL);
        } else if (ViewMode.THUMBNAIL_LARGE.getId() == vm) {
            setViewMode(ViewMode.THUMBNAIL_LARGE);
        } else {
            setViewMode(ViewMode.NOIMAGE);
        }

        setPageSize(getIntUserPreference("page-size", PAGE_SIZE));
    }

    private int getSystemColsWidth() {
        return this.system_cols_width;
    }


}
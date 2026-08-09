package com.novamens.kbee.wicket.markup.html.console.list;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

@SuppressWarnings("serial")
public abstract class ListPanel<T> extends DataViewPanel<T> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListPanel.class.getName());
	
	private ListDisplayMode	display_mode = ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK;

	/**
	 * 
	 */
	public class ItemPanel extends Fragment {

		private boolean expanded = false;
		private String id;
		private int index;
		private boolean hasmenu;
		private	IModel<T> itemmodel;
		
		public ItemPanel(IModel<SearchResult> model, int index) {
			super("row-container", "row-fragment", ListPanel.this);
			
			setOutputMarkupId(true);
			
			this.index = index;
			
			if (model==null) {
				logger.error("model is null. Try Reindexing");
				onNullModelObject();
				setVisible(ListPanel.this.isShowNullItems());		 		
				return;
			}
			else if (model.getObject()==null) {
				logger.error("model.getObject() is null. Try Reindexing");
				onNullModelObject();
				setVisible(ListPanel.this.isShowNullItems());
				return;
			}
			else if(model.getObject().getObject()==null) {
				logger.error("rowmodel.getObject().getObject() is null. Try Reindexing");
				onNullModelObject();
				setVisible(ListPanel.this.isShowNullItems());
				return;
			}
			
			setModel(model);
			addMenu();
			addExpander();
			addIcon();
			addItem();
			
			try {
				if (expanded())
					addExtendedPanel();
				else
					addOrReplace( new InvisiblePanel("editor"));
				
			} catch (Exception e) {
				logger.error(e);
				addOrReplace(new kbee.web.error.ErrorPanel("editor", e));
			}
		}
		
		
		private void addItem() {
			try {
				Panel pa=getItemListPanel(getModel(), ItemPanel.this.index);
				
				
				if (hasIcon()) {
					pa.add( new AttributeModifier("style", "width:calc(100% - 125px); float:left;"));	
				}
				else {
					pa.add( new AttributeModifier("style", "width:calc(100% - 125px); margin-left:" + (isIconSupported() ? "30px" : "0px") + "; float:left;"));
				}
				addOrReplace(pa);
			} catch (Exception e) {
				logger.error(e);
				addOrReplace( new kbee.web.error.ErrorPanel("item", e));
			}
		}


		@SuppressWarnings("unchecked")
		public void setModel(IModel<SearchResult> model) {
			T object = (T)model.getObject().getObject();
			this.itemmodel = ListPanel.this.getModel(object);
		}
		public IModel<T> getModel() {
			return this.itemmodel;
		}
		public String getItemId() {
			return id;
		}
		public void setItemId(String id) {
			this.id = id;
		}
		public void setExpanded(boolean value) {
			expanded = value;
		}
		public boolean expanded() {
			return expanded;
		}
		@Override
		public void onDetach() {
			super.onDetach();
			if (getModel()!=null)
				getModel().detach();
		}
		
		protected void addMenu() {
			WebMarkupContainer container;
			try {
					Panel menu = isMenuEnabled() ? ListPanel.this.getMenu(getModel()) : null;
					hasmenu = menu!=null;
					container = new WebMarkupContainer("menu-container") {
						public boolean isVisible() {
							return hasmenu;
						}
					};
					
					if (menu!=null) {
						WebMarkupContainer link = new WebMarkupContainer("dmenu");
						link.setOutputMarkupId(true);
						container.add(link);
						container.add(menu);
					}
					else {
						WebMarkupContainer link = new WebMarkupContainer("dmenu");
						container.add(link);
						container.add(new InvisiblePanel("menu"));
					}
					addOrReplace(container);

			} catch (Exception e) {
				logger.error(e);
				container = new WebMarkupContainer("menu-container");
				container.addOrReplace(new ErrorPanel("menu"));
				addOrReplace(container);
				
			}
		}
		protected void addExpander() {
			 
			AjaxLink<?> expanderlink = new AjaxLink<Void>("expander") {
				
				@Override
				public boolean isVisible() {
					return hasExpander();
				}

				@Override
				public void onClick(AjaxRequestTarget target) {

					setExpanded(!expanded());
					
					if (expanded()) {
						ItemPanel.this.addExtendedPanel();
					}
					else {
						ItemPanel.this.addOrReplace(new InvisiblePanel("editor"));
					}
					target.add(ItemPanel.this);
				}
			};
			
			WebMarkupContainer expandericon = new WebMarkupContainer("icon");
			expandericon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return expanded ? "far fa-angle-down" : "far fa-angle-up";
				}
			}));
			expanderlink.add(expandericon);
			add(expanderlink);
		}
		
		
		protected void addIcon() {
			 
			AjaxLink<T> expanderlink = new AjaxLink<T>("icon-container", ItemPanel.this.getModel()) {
				@Override
				public boolean isVisible() {
					return ListPanel.this.hasIcon(getModel());
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					target.add(ItemPanel.this);
				}
			};
			
			WebMarkupContainer cicon = new WebMarkupContainer("icon");
			cicon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return ListPanel.this.getIcon( getModel() );
				}
			}));
			expanderlink.add(cicon);
			add(expanderlink);
		}
		
		
		protected boolean hasIcon() {
			return ListPanel.this.hasIcon( getModel() );
		}

		
		protected boolean isFolder() {
			return false;
		}

		protected boolean isLocked() {
			return true;
		}
		
		protected void addExtendedPanel() {
			try {
				Panel panel=ListPanel.this.getPanel(getModel(), index, expanded());
				if (panel!=null)
					ItemPanel.this.addOrReplace(panel);
				else
					ItemPanel.this.addOrReplace(new InvisiblePanel("editor"));
			
			} catch (Exception e) {
				logger.error(e);
				ItemPanel.this.addOrReplace(new kbee.web.error.ErrorPanel("editor", new Model<String>(e.getClass().getName())));
			}
		}
		
		protected void onNullModelObject() {
	 		add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						return "row-container";
					}
			}));
	 	}
	}

	/** --------------------------------------------------------------------
	 *
	 *
	 */
	public ListPanel(String id, Query query) {
		super(id, query);
		setOutputMarkupId(true);
		setQuery(query);
	}
	

	public boolean isIconSupported() {
		return true;
	}


	protected abstract Panel getItemListPanel(IModel<T> model, int index);

	
	public ListDisplayMode getListDisplayMode() {
		return this.display_mode;
	}
	
	public void setListDisplayMode(ListDisplayMode mode) {
		this.display_mode = mode;
		setUserPreference("listdisplaymode", mode.getRsLabel());
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("container")==null) {
			initPreferences();
			addContainer();
			addList();
		}
	}
	
	protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public DataView<SearchResult> getDataView() {
		return (DataView<SearchResult>) get("container:row");
	}
	


	@Override
	protected String getContextKey() {
		return "list/";
	}

	protected void addContainer() {
		WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		container.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				 return "container list " + ListPanel.this.getListDisplayMode().getCss(); 
			}}));
		add(container);
	}
	
	/**
	 * If the SearchResult or the SearchResult.getObject() is null
	 * then it add a row with the Label "err" on the 1st column.
	 * 
	 */
	protected void addList() {
		try {
			
			IDataProvider<SearchResult> searcher = getSearcher();
			
			DataView<SearchResult> dataview = new DataView<SearchResult>("row", searcher, getPageSize()) {
				@Override
				protected void populateItem(Item<SearchResult> item) {
					try {
					item.add(new ItemPanel(item.getModel(), item.getIndex()));
					} catch (Exception e) {
						logger.error(e);
						item.add(new kbee.web.error.ErrorPanel("row-container", e));
					}
				}
			};
			((WebMarkupContainer) get("container")).add(dataview);
		} 
		catch (Exception e) {

			logger.error(e);
			
			logger.error(e, "The grid can not be rendered due to an Application error please call support ");
			showError(new StringResourceModel("error-msg", ListPanel.this, null).getString() + 
				 e.getClass().getCanonicalName() + "<br />" + e.getMessage());
		}	
	}
	
	
	/**
	 * 
	 */
	private void initPreferences() {
		
		String dm = getUserPreference("listdisplaymode", ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK.getRsLabel());
		
			 if (dm.equals(ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK.getRsLabel()))	this.display_mode=ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMPACT_LIST_BORDER_NOBCK.getRsLabel()))		this.display_mode=ListDisplayMode.COMPACT_LIST_BORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK.getRsLabel()))	this.display_mode=ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMPACT_LIST_BORDER_BCK.getRsLabel()))		this.display_mode=ListDisplayMode.COMPACT_LIST_BORDER_BCK;
		
		else if (dm.equals(ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK.getRsLabel()))	this.display_mode=ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMFORTABLE_LIST_BORDER_NOBCK.getRsLabel()))		this.display_mode=ListDisplayMode.COMFORTABLE_LIST_BORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK.getRsLabel()))	this.display_mode=ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK;
		else if (dm.equals(ListDisplayMode.COMFORTABLE_LIST_BORDER_BCK.getRsLabel()))		this.display_mode=ListDisplayMode.COMFORTABLE_LIST_BORDER_BCK;
		
		else this.display_mode = ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK;

			 
		// logger.debug(this.display_mode.getRsLabel());
			 
		/** 
		 * do not call setDateFormat() here because it will save the value to the Database, which is redundant
		 **/

		setPageSize(getIntUserPreference("page-size", PAGE_SIZE));
	}

	public void setSearcher(Searcher seacher) {
		super.setSearcher(seacher);
	}
	
	protected boolean hasIcon(IModel<T> model) {
		return false;
	}

	protected String getIcon(IModel<T> model) {
		return null;
	}

}
package com.novamens.kbee.wicket.markup.html.areainfo;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.Tuple;


@SuppressWarnings("serial")
public class GridInfoPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private List<Tuple> list;
	private List<String> headers;
	private List<Integer> colw;
	
	private String label_css = "col-xs-6";
	private String value_css = "col-xs-6";
	
	private IModel<String> title_model;
	
	private boolean is_expanded = false;
	private boolean is_created = false;
	
	private boolean isHeader = false;

	String panel_link_str = null;
	IModel<String> panel_link_label = null;
	
	
	public void setPanelLinkLabel(IModel<String> s) {
		panel_link_label=s;
	}
	
	public IModel<String> getPanelLinkLabel() {
		return panel_link_label;
	}
	

	
	
	public void setPanelLink(String s) {
		panel_link_str=s;
	}
	
	public String getPanelLink() {
		return panel_link_str;
	}
	
	
	public void setHeader( boolean b) {
		isHeader=b;
	}
	
	public boolean isHeader() {
		return this.isHeader;
	}
	/**
	 * 
	 * 
	 */
	public class TupleProvider extends SortableDataProvider<Tuple, String> {
		private static final long serialVersionUID = 1L;
		
		public Iterator<Tuple> iterator(long first, long count) {
			ArrayList<Tuple> iteration = new ArrayList<Tuple>();
			Iterator<Tuple> iterator = getList().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}
		
		public IModel<Tuple> model(Tuple object) {
			return new Model<Tuple>(object);
		}
		
		public long size() {
			return getList().size();
		}
	}
	
	
	/**
	 * 
	 * 
	 */
	public GridInfoPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public GridInfoPanel(String id, List<Tuple> list, IModel<String> title) {
		this(id, list, title, null, false);
	}
	
	public GridInfoPanel(String id, List<Tuple> list, IModel<String> title, boolean is_expanded) {
		this(id, list, title, null, is_expanded);
	}
	
	
	public GridInfoPanel(String id, List<Tuple> list, IModel<String> title, List<String> titles) {
			this(id, list, title, titles, false);
	}
	
	public GridInfoPanel(String id, List<Tuple> list, IModel<String> title, List<String> titles, boolean is_expanded) {
		super(id);
		setTitle(title);
		
		setList(list);
		setHeaders(titles);
		setOutputMarkupId(true);
		setExpanded(is_expanded);
	}
	
	
	
	
	
	public GridInfoPanel(String id, List<Tuple> list) {
		super(id);
		setList(list);
		setOutputMarkupId(true);
	}
	
	
	
	public void setColWidth(List<Integer> l) {
		this.colw=l;
	}
	
	public List<Integer> getColWidth() {
		return this.colw;
	}
	
	public List<Tuple> getList() {
		return list;
	}
	
	public void setList(List<Tuple> list) {
		this.list=list;
	}

	public List<String> getHeaders() {
		if (this.headers==null)
			this.headers = new ArrayList<String>();
		return this.headers;
	}
	
	public void setHeaders(List<String> list) {
		this.headers=list;
	}

	
	public IModel<String> getTitle() {
		return title_model;
	}
	
	public void setTitle(IModel<String> title) {
		this.title_model=title;
	}

	public boolean helpInfo() {
		return false;
	}
	

	protected void onHelp(AjaxRequestTarget target) {}
	
	
	
	private void addPanel() {
		
			is_created = true;
			WebMarkupContainer collapsable_panel = new WebMarkupContainer("collapsable_panel");
			addOrReplace(collapsable_panel);
			collapsable_panel.setVisible(false);
			DataTable<Tuple, String> table = new DataTable<Tuple, String>("grid", getColumns(), new TupleProvider(), 100);
			table.setOutputMarkupId(true);
			
			
			if (isHeader())
				table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (TupleProvider) table.getDataProvider()));
			
			
			collapsable_panel.addOrReplace(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table));
			collapsable_panel.addOrReplace(table);
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getList()==null)
			setList(new ArrayList<Tuple>());

		WebMarkupContainer title_container = new WebMarkupContainer("title-container") {
			private static final long serialVersionUID = 1L;
						public boolean isVisible() {
							return getTitle()!=null;
						}
	};
		
	add(title_container);
	
	
	Label p_l_la=new Label("panel-link-label", getPanelLinkLabel());
	p_l_la.setEscapeModelStrings(false);
	
	WebMarkupContainer p_l_panel = new WebMarkupContainer("panel-link-container");
	p_l_panel.setVisible(panel_link_str!=null);
	add(p_l_panel);
	
	
	Link<Void> p_l = new Link<Void>("panel-link") {
		public boolean isVisible() {
			return panel_link_str!=null;
		}

		@Override
		public void onClick() {
			setResponsePage( new RedirectPage(panel_link_str));
		}
	};
	p_l_panel.add(p_l);
	p_l.add(p_l_la);
		
	
	AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
		public boolean isVisible() {
			return helpInfo();
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			onHelp(target);
		}
	};

	
		
	AjaxLink<?> expander= new AjaxLink<Void>("expander") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				if (!is_created)
					addPanel();
				
				boolean isv=GridInfoPanel.this.get("collapsable_panel").isVisible();
				GridInfoPanel.this.get("collapsable_panel").setVisible(!isv);
				
				target.add(GridInfoPanel.this);
			}
	};
	
	title_container.add(expander);
	
	expander.add(helpLink);
		
	if (getTitle()!=null)
		expander.add((new Label("title", getTitle())).setEscapeModelStrings(false));
	else
		expander.add(new Label("title", ""));
		
						
	WebMarkupContainer toggle = new WebMarkupContainer("toggle") {
			public boolean isVisible() {
				return  GridInfoPanel.this.isToggle();
			}
	};

	 toggle.add(new AttributeModifier("class", new Model<String>() {
		 public String getObject() {
				if (GridInfoPanel.this.get("collapsable_panel")!=null &&
					GridInfoPanel.this.get("collapsable_panel").isVisible())
					return "far fa-angle-down";
				return "far fa-angle-up";
		 }
	 }));
	 
	
	expander.add(toggle);

	if (isExpanded()) { 
		addPanel();
		get("collapsable_panel").setVisible(true);
	}
	else
		add( new InvisiblePanel("collapsable_panel"));
		

	
	
	
	}


	
	boolean is_toggle = true;
	
	public void setIsToggle( boolean b) {
		this.is_toggle=b;
	}
	protected boolean isToggle() {
 
		return false;
	}

	public boolean isExpanded() {
		return this.is_expanded;
	}
	
	public void setExpanded(boolean b) {
		is_expanded=b;
	}

	
	
	
	
	/**
	 * 
	 */
	private List<IColumn<Tuple, String>> getColumns() {

		List<IColumn<Tuple, String>> columns = new ArrayList<IColumn<Tuple, String>>();

		columns.add(new PropertyColumn<Tuple, String>(new StringResourceModel("label",this, null), "label") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getCssClass() {
				return getLabelCss();
			}
			@Override
			public void populateItem(final Item<ICellPopulator<Tuple>> item, final String componentId, final IModel<Tuple> rowModel)
			{
				item.add((new Label(componentId, createLabelModel(rowModel)).setEscapeModelStrings(false)));
			}
			protected IModel<?> createLabelModel(final IModel<Tuple> rowModel) {
				return getDataModel(rowModel);
			}
		});
		
		
		
		
		if (getList()!=null && getList().size()>0  && getList().get(0) !=null && getList().get(0).arr!=null && getList().get(0).arr.length>0)  {
			
			Double cols =  Double.valueOf( (Integer.valueOf(9)) / (Integer.valueOf(getList().get(0).arr.length)));
			 	
			final int total_cols = getList().get(0).arr.length;
			final int col_w = cols.intValue();

			if (getColWidth()==null) {
				this.colw = new ArrayList<Integer>();
				for (int n=0; n<total_cols; n++) 
					colw.add(Integer.valueOf(col_w));
			}
			
			if (this.colw.size()<total_cols) {
				for (int n=this.colw.size(); n<total_cols;n++)
					colw.add(Integer.valueOf(1));
			}

			for (int n=0; n<total_cols; n++) {
				String title = (getHeaders().get(n) !=null ? getHeaders().get(n) : ("col"+ String.valueOf(n)));
	
				final int c_width  = colw.get(n).intValue();
				
				columns.add(new PropertyColumn<Tuple, String>( new Model<String>(title), "value" + String.valueOf(n)) {
					@Override
					public void populateItem(final Item<ICellPopulator<Tuple>> item, final String componentId, final IModel<Tuple> rowModel) {
						Label la = new Label(componentId, createLabelModel(rowModel));
						la.setEscapeModelStrings(false);
						item.add(la);
					}
					@Override
					public String getCssClass() {
						String str;
						str = "col-xs-"+String.valueOf(c_width) + " " +
							"col-md-"+String.valueOf(c_width) + " " +
							"col-lg-"+String.valueOf(c_width);
						return str;
					}
					protected IModel<?> createLabelModel(IModel<Tuple> rowModel) {
						return getDataModel(rowModel);
					}
				});
			}
		}
		else {
		columns.add(new PropertyColumn<Tuple, String>(new StringResourceModel("value",this, null), "value") {
			@Override
			public String getCssClass() {
				return getValueCss();
			}
			@Override
			public void populateItem(final Item<ICellPopulator<Tuple>> item, final String componentId, final IModel<Tuple> rowModel) {
				item.add( (new Label(componentId, createLabelModel(rowModel)).setEscapeModelStrings(false)));
			}
			protected IModel<?> createLabelModel(final IModel<Tuple> rowModel) {
				return getDataModel(rowModel);
			}
		});
		}

		return columns;
	}
	
	protected int getCols(int col_order) {
		return 0;
	}
	public void setLabelCss(String c) {
		this.label_css=c;
	}
	
	public String getLabelCss() {
		return this.label_css;
	}
	
	public void setValueCss(String c) {
		this.value_css=c;
	}
	
	public String getValueCss() {
		return this.value_css;
	}
}


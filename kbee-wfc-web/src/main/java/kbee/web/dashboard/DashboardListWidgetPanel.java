package kbee.web.dashboard;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.properties.PropertyService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.markup.html.tabs.ITabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.console.grid.LabelSetPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.panel.ListSimplePanel;

/**
 * @param <T>
 */
 
public class DashboardListWidgetPanel<T> extends  DashboardWidgetBasePanel {
	private static final long serialVersionUID = 1L;

	static String PROPERTY_HAS_TAGS ="tags";
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardListWidgetPanel.class.getName());

	private ListSimplePanel<T> simple_panel;
	
	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	
	private List<IModel<T>> list;
	private List<ITabKB> _tabs;
	private int initial_selected = 0;
	
	private NumberFormat integer_nf = null;
	private String sort_criteria;
	
	private String viewModeCriteria = "standard";

	public DashboardListWidgetPanel(String id) {
		super(id, "null");
	}
	
	public DashboardListWidgetPanel(String id, String preferences_key) {
		this(id, null, null, preferences_key);
	}

	public DashboardListWidgetPanel(String id, List<IModel<T>> list, String preferences_key) {
		this(id, list, null, preferences_key);	
	}

	public DashboardListWidgetPanel(String id, List<IModel<T>> list, IModel<String> title, String preferences_key) {
		super(id, preferences_key);
		setItems(list);
		setTitle(title);

		sort_criteria = "title";
		integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
		integer_nf.setMinimumFractionDigits(0);
		integer_nf.setMaximumFractionDigits(0);
		integer_nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	public String getSortCriteria() {
		return this.sort_criteria;
	}
	
	public void setSortCriteria(String s) {
		this.sort_criteria=s;
	}
	
	public void setItems(List<IModel<T>> l) {
		this.list=l;
	}
	
	public List<IModel<T>> getItems() {
		if (list!=null)
			return list;
		this.list = new ArrayList<IModel<T>>();
		return list;
	}

	public void toogleHelp(AjaxRequestTarget target) {
		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			main_container.get("tabs").setVisible(!main_container.get("tabs").isVisible());
			target.add(this.main_container);
		}
	}

	public PopupSettings getPopupSettings() {
		return null;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (list!=null)
			list.forEach(item -> item.detach());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		this.main_container = new WebMarkupContainer("body");
		this.main_container.setVisible(!isCollapsed());
		
		if ( getBodyStyle()!=null)
			this.main_container.add( new AttributeModifier("style", getBodyStyle()));
		
		this.main_container.setOutputMarkupId(true);
		add(this.main_container);

		this.main_container.add(new InvisiblePanel("help"));
		
		setBottomPanel(
				new DBoardListBottomPanel("base-bottom") {
					@Override
					protected IModel<String> getViewingString() {
						return DashboardListWidgetPanel.this.getViewingString();
					}
					@Override
					protected IModel<String> getAllString() {
						return DashboardListWidgetPanel.this.getAllString();
					}
					@Override
					protected void onClickAll() {
						DashboardListWidgetPanel.this.onClickAll();
					}
				}
		);
		addTabs();
	}

	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}
	
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		this.main_container.setVisible(!this.main_container.isVisible());
		refresh(target);
	}

	protected String getBodyStyle() {
		return null;
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		return new DummyBlockPanel("help");
	}

	@Override
	protected void refresh(AjaxRequestTarget target) {
		this.onDetach();
		if (simple_panel != null) {
			simple_panel.detach();
			simple_panel.setItems(getItems());
		}
		target.add(this.main_container);
		target.add(getBottomPanel());
		super.refresh(target);
	}
	
	protected ListSimplePanel<T> getListPanel(String panelId) {
		
		ListSimplePanel<T> s = new ListSimplePanel<T>(panelId, DashboardListWidgetPanel.this.getName(), getItems()) {

			private static final long serialVersionUID = 1L;

			protected String getListContainerCss() {
				return DashboardListWidgetPanel.this.getListContainerCss();
			}

			@Override
			public IModel<String> getLabelContainerCss() {
				return DashboardListWidgetPanel.this.getLabelContainerCss(); 
			}

			protected String getTitleMeta() {
				return DashboardListWidgetPanel.this.getTitleMeta();
			}
			
			public PopupSettings getPopupSettings() {
				return DashboardListWidgetPanel.this.getPopupSettings();
			}

			protected void onClick(IModel<T> modelObject, int index) {
				DashboardListWidgetPanel.this.onClick(modelObject, index);
			}
			
			@Override
			public IModel<String> getIconCss(IModel<T> model) {
				 return DashboardListWidgetPanel.this.getIconCss(model);
			}
			
			@Override
			public IModel<String> getIconCssTitle(IModel<T> model) {
				 return DashboardListWidgetPanel.this.getIconCssTitle(model);
			}
			
			
			@Override
			protected IModel<String> getItemAbstract(IModel<T> modelObject) {
				return DashboardListWidgetPanel.this.getItemAbstract(modelObject);	
			}
			
			@Override
			protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
				return DashboardListWidgetPanel.this.getItemLabelMeta(modelObject);	
			}
			
			@Override
			protected WebMarkupContainer getItemTags(IModel<T> modelObject)  {
				return DashboardListWidgetPanel.this.getItemTags(modelObject);
			}
			
			@Override
			protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
				return DashboardListWidgetPanel.this.getMoreInfoPanel(modelObject);
			}
			
			@Override
			public boolean isExpand() {
				return DashboardListWidgetPanel.this.isExpandVisible();
			}
			
			@Override
			public boolean isMenu() {
				return DashboardListWidgetPanel.this.isMenuVisible();
			}
			
			public WebMarkupContainer getExpandedPanel(String id, IModel<T> model) {
				WebMarkupContainer panel=DashboardListWidgetPanel.this.getExpandedPanel(id,  model);
				 if (panel !=null)
					 return panel;
				 else
					return super.getExpandedPanel(id,  model);
			}
			
			protected Panel getMenu(IModel<T> model, int index) {
				return DashboardListWidgetPanel.this.getMenu(model, index);
			}
		};
		
		s.setMenu(isMenuVisible());
		s.setExpand(isExpandVisible());
		s.setIcon(isIconVisible());
		s.setTitle(null);
		
		return s;
	}
	

	
	protected IModel<String> getIconCss(IModel<T> model) {return null;}
	protected IModel<String> getIconCssTitle(IModel<T> model) {return null;}

	
	/**
	 * 
	 */
	protected void addTabsLists() {
		
		List<ITab> tabs = new ArrayList<ITab>();

		try {
				tabs.add(new AbstractTabKB(getListTitle(), "items") {
					public Panel getPanel(String panelId) {
						return getListPanel(panelId);
					}
				});
				
				List<ITabKB> l=getTabs();
				
				if (l!=null) {
					for( ITabKB itab: l)
						tabs.add(itab);
				}
				
				AjaxTabbedPanel<ITab> xtabs= new AjaxTabbedPanel<ITab>("tabs", tabs) {
					@Override
					protected String  getNavCss() {
						return "nav nav-buttons";
					}
					@Override
					protected void onAjaxUpdate(AjaxRequestTarget target) {
						 setInitialSelectedTab(getSelectedTab());
					}
					
				};
				xtabs.setSelectedTab(getInitialSelectedTab());
				
				
				this.main_container.addOrReplace(xtabs);

		} catch (Exception e) {
			logger.error(e);
			main_container.addOrReplace( new ErrorPanel("tabs", e));
		}
	}
	
	protected void addTabs() {
		if (getTabs()!=null && getTabs().size()>0)
			addTabsLists();
		else
			addSingleList();
	}

	protected void setHelpPanel(WebMarkupContainer help) {
		if (this.help!=null) {
				addOrReplace(help);
		}
		else
			this.help=help;
	}

	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs", new StringResourceModel("no-items", this,null), "");	
	}
	
	protected void addSingleList() {
		try {
			if (getItems()==null || getItems().size()==0) {
				main_container.addOrReplace(addVoidPanel("tabs"));
			}
			else {
				simple_panel = (ListSimplePanel<T>) getListPanel("tabs");
				simple_panel.setTitle(null);
				main_container.addOrReplace(simple_panel);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			
		}
	}
	
	protected void setInitialSelectedTab(int selectedTab) {
		initial_selected = selectedTab;
	}

	protected int getInitialSelectedTab() {
		return initial_selected;
	}

	protected  WebMarkupContainer getExpandedPanel(String id, IModel<T> model) {
		return null;
	}
	
	protected Panel getMenu(IModel<T> model, int index) {
		return new InvisiblePanel("menu");
	}
	
	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable") ?"cozy" : "standard");
	}

	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}

	protected IModel<String> getListTitle() {
		return getTitle();
	}

	protected String getName() {
		return getClass().getName();
	}

	
	
	protected boolean isMenuVisible() 	{return true;}
	protected boolean isExpandVisible() {return false;}
	protected boolean isIconVisible() 	{return true;}
	protected boolean isSort() 			{return false;}
	

	protected IModel<String> getAllString() {return new Model<String>("");	}

	protected IModel<String> getViewingString() {
		return new Model<String>("");
	}

	protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
		
		try {
			if (modelObject.getObject() instanceof Content) {
				return new Model<String>(((Content) modelObject.getObject()).getContentTypeClassificationAsString());
			}
			else
				return null;
		} catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName());
		}
	}
	
	protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
		try {
			if (modelObject.getObject() instanceof Content) {
				 Content c=(Content) modelObject.getObject();
				 String nr = (String) c.getService(PropertyService.class).getProperty(PropertyService.PROPERTY_HAS_TAGS);
				 if (nr==null || nr.equals("0"))
					 return new InvisiblePanel("labels");
					 
				 
				return new LabelSetPanel<Content>("labels", new ObjectModel<Content>(c), false, true, false);
			}
			else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("labels", e);
		}
	}
									
	
	protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
			return null;
	}
	
	@Override
	protected void onTitleClick() {
		onClickAll();
	}

	protected void onClickAll() {}
	
	
	protected NumberFormat getIntegerNumberFormat() {
		return integer_nf;
	}
	
	protected IModel<String> getItemAbstract(IModel<T> modelObject) {
		try {
			if (modelObject.getObject() instanceof Content) {
				return new Model<String>(((Content) modelObject.getObject()).getTitle());
			}
			else
				return null;
		} catch (Exception e) {
			logger.error(e);
			return  new Model<String>(e.getClass().getName());
		}
	}

	protected String getTitleMeta() {
		return null;
	}
	
	protected void onClick(IModel<T> modelObject, int index) {
	}
	
	protected void setTabs(List<ITabKB> t) {
		 _tabs = t;
	}
	
	protected List<ITabKB> getTabs() {
		 return _tabs;
	}
	
	protected String getViewModeCriteria() {
		return viewModeCriteria;
	}

	protected void setViewModeCriteria(String s) {
		viewModeCriteria=s;
	}

	
}

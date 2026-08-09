package com.novamens.kbee.wicket.markup.html.console.panel;



import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabWithIcon;
import com.novamens.wicket.markup.html.tabs.ITabKB;

/**
 * 
 * tabs-container: nav
 * 
 */

@Deprecated
@SuppressWarnings("serial")
public class VerticalAjaxTabbedPanel<T extends ITab> extends KBPanel {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(VerticalAjaxTabbedPanel.class.getName());
	
	static public int HORIZONTAL = 0;
	static public int VERTICAL = 1;

	
	public static final String TAB_PANEL_ID = "panel";  	/** id used for child panels */

	private int currentTab = -1;

	private transient VisibilityCache visibilityCache;

	private String key;
	// header
	private Panel top_v_header_panel;

	private WebMarkupContainer headerContainer;
	private WebMarkupContainer headerTabsContainer;
	
	private Panel bottom_v_header_panel;
		
	// content
	
	private WebMarkupContainer tabContent;
	private final List<T> tabs; 
	private List<Component> panels;

	private WebMarkupContainer contentContainer;
	private WebMarkupContainer contentMarkup; 
	private Panel top_v_content_panel;
	private Panel bottom_v_content_panel;
	
	boolean isVertical;
	boolean isRightMenu;
	boolean expandDown = true;

	boolean isTwoPanels = true;
	
	private IModel<String> title;

	private List<MenuItemFactory<Panel>> menu_item_factory_list;
	
	private WebMarkupContainer onePanelMenuContainer;
	String content_panel_css;
	
	
	private class PanelClassModifier extends AttributeModifier {
		private static final long serialVersionUID = 1L;
		public PanelClassModifier(final int panelindex) {
			super("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				public String getObject() {
					String tab_content_css = getTabPanelCss();
					return  (currentTab==panelindex ? "tab-pane fade in active " : "tab-pane fade ")+( tab_content_css!=null? tab_content_css:"") ;
				}
			}); 
		}
	}

	// tab panel
	protected String getTabPanelCss() {
		return "tab-panel";
	}

	protected Panel getOnePanelMenu() {
		return getMenu("menuop");
	}
	
	public void setExpandDown( boolean b) {
		this.expandDown=b;
	}
	
	public boolean isExpandDown() {
		return this.expandDown;
	}

	
	/** -----------------------------------------------------
	 * 
	 * @param id
	 * @param tabs
	 */
			
	public VerticalAjaxTabbedPanel(final String id, final String key, final List<T> tabs)	{
		this(id, key, tabs, HORIZONTAL);
	}

	
	public VerticalAjaxTabbedPanel(final String id, final String key,   final List<T> tabs, int orientation) {
				this(id, key, tabs, orientation, true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 * 
	 */
	public VerticalAjaxTabbedPanel(final String id, String key, final List<T> tabs, int orientation, boolean isRightMenu) {
		super(id);

		setOutputMarkupId(true);
		
		this.key=key;
		this.tabs = Args.notNull(tabs, "tabs");
		
		this.isRightMenu=isRightMenu;
		isVertical=true;
		isRightMenu = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue(getKey()+"-VerticalTab", "isrightmenu", "yes").equals("yes");
		setOutputMarkupId(true);
		
		this.panels = new ArrayList<Component>(tabs.size());
		for (int i=0; i<tabs.size(); i++) 
			this.panels.add(i, null);
 
	}

	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		onePanelMenuContainer =  new WebMarkupContainer("one-panel-menu-container");
		onePanelMenuContainer.add(getOnePanelMenu());
		onePanelMenuContainer.setOutputMarkupId(true);
		onePanelMenuContainer.setVisible(false);

		if (!isExpandDown()) {
			onePanelMenuContainer.add(new AttributeModifier("class", "notexpanddown"));	
		}
		else
			onePanelMenuContainer.add(new AttributeModifier("class", "expanddown"));
		
		add(onePanelMenuContainer);
		
		
		
		headerTabsContainer = newTabsContainer("header-tabs-container");
		headerTabsContainer.setOutputMarkupId(true);
		
		final IModel<Integer> tabCount = new IModel<Integer>() {
			@Override
			public Integer getObject() {
				return VerticalAjaxTabbedPanel.this.tabs.size();
			}
		};


		// Header	-----------------
		//
		headerContainer =  new WebMarkupContainer("header-container");
		headerContainer.setOutputMarkupId(true);
		add(headerContainer);

		headerContainer.add(getMenu());
		headerContainer.add(new AttributeModifier("class", "col-lg-2 col-md-3 col-xs-12" + (isRightMenu() ? " head-right " : " head-left ")));
		
		if (getTitle()!=null)
			headerContainer.add(new Label("title", getTitle()));
		else
			headerContainer.add( (new Label("title", "")).setVisible(false));

		
		if (top_v_header_panel!=null) 
			headerContainer.addOrReplace(top_v_header_panel);
		else 	
			headerContainer.addOrReplace(new InvisiblePanel("header-top-panel"));

		if (bottom_v_header_panel!=null) 
			headerContainer.addOrReplace(bottom_v_header_panel);
		else 	
			headerContainer.addOrReplace(new InvisiblePanel("header-bottom-panel"));

		headerContainer.add(headerTabsContainer);

		
		

		// add the loop used to generate tab names
		//
		headerTabsContainer.add(new Loop("tabs", tabCount) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final LoopItem item) {
				final int index = item.getIndex();
				final T tab = VerticalAjaxTabbedPanel.this.tabs.get(index);

				final WebMarkupContainer titleLink = newLink("link", index);
										
				if (VerticalAjaxTabbedPanel.this.tabs.get(index) instanceof AbstractTabWithIcon) {
					String iconclass = ( (AbstractTabWithIcon) VerticalAjaxTabbedPanel.this.tabs.get(index)).getIconClass();
						WebMarkupContainer icon = new WebMarkupContainer("icon");
						if (iconclass!=null) {
							
							icon.add(new AttributeModifier("class", iconclass));
							titleLink.add(icon);
							
							String icontitle = ( (AbstractTabWithIcon) VerticalAjaxTabbedPanel.this.tabs.get(index)).getIconTitle();
							if (icontitle!=null) {
								icon.add(new AttributeModifier("title",icontitle ));
								icon.add(new AttributeModifier("alt", icontitle ));
							}
						} 
						else
							titleLink.add((new WebMarkupContainer("icon")).setVisible(false));
									
				}
				
				else {
					titleLink.add((new WebMarkupContainer("icon")).setVisible(false));
				}
				
				titleLink.add(newTitle("title", getTabTitle(tab), index));
				item.add(titleLink);
			}

			@Override
			protected LoopItem newItem(final int iteration)	{
				return newTabContainer(iteration);
			}
		});

		
		
		
		

		// Content	-----------------

		
		contentContainer = new WebMarkupContainer("content-container");
		contentContainer.setOutputMarkupId(true);
		add(contentContainer);
		
		isVertical=true;
		isRightMenu = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue(getKey()+"-VerticalTab", "isrightmenu", "yes").equals("yes");
		setOutputMarkupId(true);
		

		contentContainer.add(new AttributeModifier("class", "col-lg-10 col-md-9 col-xs-12 " + (isRightMenu() ? "content-left " : " content-right")));
		
		contentMarkup = new WebMarkupContainer("content-panel");
		
		contentMarkup.setOutputMarkupId(true);
		if (getContentPanelCss()!=null)
				 contentMarkup.add(new AttributeModifier("class", getContentPanelCss()));
		
		if (top_v_content_panel!=null) 
			contentMarkup.addOrReplace(top_v_content_panel);
		else 	
			contentMarkup.addOrReplace(new InvisiblePanel("content-top-panel"));
		if (bottom_v_content_panel!=null) 
			contentMarkup.addOrReplace(bottom_v_content_panel);
		else 	
			contentMarkup.addOrReplace(new InvisiblePanel("content-bottom-panel"));
		
		
		this.tabContent = new WebMarkupContainer("tab-content");
		this.tabContent.setOutputMarkupId(true);
		this.tabContent.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				 if (getTabPanelContainerCss()!=null)
					 return getTabPanelContainerCss();
				 return"";
			}
		}));
		tabContent.add(newPanel());

		contentMarkup.add(tabContent);
		contentContainer.add(contentMarkup);

	}


	

	
	public void setContentTopPanel(Panel panel) {
		
		if (!panel.getId().equals("content-top-panel")) 
			throw new KbeeRuntimeException("must have id = content-top-panel");
		
		top_v_content_panel=panel;
		
		if (this.isInitialized())
			contentMarkup.addOrReplace(panel);
	}
	

	public void setContentBottomPanel(Panel panel) {
									
		if (!panel.getId().equals("content-bottom-panel")) 
			throw new KbeeRuntimeException("must have id = content-bottom-panel");
		
		bottom_v_content_panel=panel;
		
		if (this.isInitialized()) {
			try {
				WebMarkupContainer w = ((WebMarkupContainer) get("content-container:content-panel"));
				if (w!=null)
					((WebMarkupContainer) get("content-container:content-panel")).addOrReplace(bottom_v_content_panel);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	

	
	
	public void setHeaderTopPanel(Panel panel) {
		if (!panel.getId().equals("header-top-panel")) 
			throw new KbeeRuntimeException("must have id=header-top-panel");
		top_v_header_panel=panel;
	}
	
	public void setHeaderBottomPanel(Panel panel) {
		if (!panel.getId().equals("header-bottom-panel")) 
			throw new KbeeRuntimeException("must have id=header-bottom-panel");
		
		bottom_v_header_panel=panel;
		
		if (this.isInitialized()) {
				try {
					WebMarkupContainer w = ((WebMarkupContainer) get("header-container:header-bottom-panel"));
					if (w!=null)
						((WebMarkupContainer) get("header-container")).addOrReplace(bottom_v_content_panel);
					
				} catch (Exception e) {
					logger.error(e);
				}
		}
	}
	
	

	protected IModel<String> getTabTitle(T tab) {
		return tab.getTitle();
	}

	/**
	 * @return index of the selected tab
	 */
	public final int getSelectedTab() {
		return (Integer)getDefaultModelObject();
	}
	
	public Component getTab(int index) {
		Component component = panels.get(index);
		return component;
	}

	/**
	 * Override of the default initModel behaviour. This component <strong>will not</strong> use any
	 * compound model of a parent.
	 * 
	 * @see org.apache.wicket.Component#initModel()
	 */
	@Override
	protected IModel<?> initModel()	{
		return new Model<Integer>(-1);
	}

	/**
	 * Generates the container for all tabs. The default container automatically adds the css
	 * <code>class</code> attribute based on the return value of {@link #getTabContainerCssClass()}
	 * 
	 * @param id
	 *            container id
	 * @return container
	 */
	protected WebMarkupContainer newTabsContainer(final String id) {
		return new WebMarkupContainer(id) {
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("class", getNavCss());
			}
		};
	}

	/**
	 * Generates a loop item used to represent a specific tab's <code>li</code> element.
	 * 
	 * @param tabIndex
	 * @return new loop item
	 */
	protected LoopItem newTabContainer(final int tabIndex) {
		return new LoopItem(tabIndex){
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(getVisiblityCache().isVisible(tabIndex));
			}

			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);

				String cssClass = tag.getAttribute("class");
				if (cssClass == null) {
					cssClass = " ";
				}
				
				if (getIndex() == getSelectedTab())	{
					cssClass += ' ' + getSelectedTabCssClass();
				}
				if (getVisiblityCache().getLastVisible() == getIndex())	{
					cssClass += ' ' + getLastTabCssClass();
				}
				
				if ("".equals(cssClass.trim()))
					tag.remove("class");
				else
					tag.put("class", cssClass.trim());
			}
		};
	}
	
	
	/**
	 * nav nav-pills nav-horizontal
	 * nav nav-pills nav-justified 
	 * nav nav-pills nav-stacked 
	 * 
	 * @return
	 */
	// Nav
	protected String getNavCss() {
		return isVertical() ? "nav nav-pills nav-stacked" : "nav nav-pills nav-horizontal";
	}

	
	protected boolean isVertical() {
		return this.isVertical;
	}


	// tab panel container
	protected String getTabPanelContainerCss() {
		return "tab-panel-container";
	}

	protected Component getHeaderTabsContainer() {
		return headerTabsContainer.add(new AttributeModifier("class",getNavCss()));

	}

	
	
	
	@Override
	protected void onBeforeRender()	{
		super.onBeforeRender();
		
		// getCss is for nav: tab pills
		if (getNavCss()!=null) {
			getHeaderTabsContainer().add(new AttributeModifier("class",getNavCss()));;
		}
		
		int index = getSelectedTab();

		if ((index == -1) || (getVisiblityCache().isVisible(index) == false))
		{
			// find first visible tab
			index = -1;
			for (int i = 0; i < tabs.size(); i++)
			{
				if (getVisiblityCache().isVisible(i))
				{
					index = i;
					break;
				}
			}

			if (index != -1)
			{
				// found a visible tab, so select it
				setSelectedTab(index);
			}
		}

		setCurrentTab(index);
	}

	/**
	 * @return the value of css class attribute that will be added to a div containing the tabs. The
	 *         default value is <code>tab-row</code>
	 */
	//protected String getTabContainerCssClass() {
	//	return "nav nav-pills nav-justified";
	//}


	/**
	 * @return the value of css class attribute that will be added to last tab. The default value is
	 *         <code>last</code>
	 */
	protected String getLastTabCssClass() {
		return "";
	}

	/**
	 * @return the value of css class attribute that will be added to selected tab. The default
	 *         value is <code>selected</code>
	 */
	protected String getSelectedTabCssClass() {
		return "active";
	}

	/**
	 * @return list of tabs that can be used by the user to add/remove/reorder tabs in the panel
	 */
	public final List<T> getTabs() {
		return tabs;
	}

	/**
	 * Factory method for tab titles. Returned component can be anything that can attach to span
	 * tags such as a fragment, panel, or a label
	 * 
	 * @param titleId
	 *            id of title component
	 * @param titleModel
	 *            model containing tab title
	 * @param index
	 *            index of tab
	 * @return title component
	 */
	protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index) {
		
		Label label=new Label(titleId, titleModel);
		label.setEscapeModelStrings(false);
		return label;
	}

	/**
	 * Factory method for links used to switch between tabs.
	 * 
	 * The created component is attached to the following markup. Label component with id: title
	 * will be added for you by the tabbed panel.
	 * 
	 * <pre>
	 * &lt;a href=&quot;#&quot; wicket:id=&quot;link&quot;&gt;&lt;span wicket:id=&quot;title&quot;&gt;[[tab title]]&lt;/span&gt;&lt;/a&gt;
	 * </pre>
	 * 
	 * Example implementation:
	 * 
	 * <pre>
	 * protected WebMarkupContainer newLink(String linkId, final int index)
	 * {
	 * 	return new Link(linkId)
	 * 	{
	 * 		private static final long serialVersionUID = 1L;
	 * 
	 * 		public void onClick()
	 * 		{
	 * 			setSelectedTab(index);
	 * 		}
	 * 	};
	 * }
	 * </pre>
	 * 
	 * @param linkId
	 *            component id with which the link should be created
	 * @param index
	 *            index of the tab that should be activated when this link is clicked. See
	 *            {@link #setSelectedTab(int)}.
	 * @return created link component
	 */
	protected WebMarkupContainer newLink(final String linkId, final int index) {
		return new AjaxFallbackLink<Void>(linkId) {
			@Override
			public void onClick(final Optional<AjaxRequestTarget> target) 	{
				setSelectedTab(index);
				if (target.isPresent()) {
					target.get().add(VerticalAjaxTabbedPanel.this);
					onAjaxUpdate(target.get());
				}
			}
		};
	}

	protected void onAjaxUpdate(AjaxRequestTarget target) {
		
	}

	/**
	 * sets the selected tab
	 * 
	 * @param index
	 *            index of the tab to select
	 * @return this for chaining
	 * @throws IndexOutOfBoundsException
	 *             if index is not in the range of available tabs
	 */
	public VerticalAjaxTabbedPanel<T> setSelectedTab(final int index)
	{
		if ((index < 0) || (index >= tabs.size()))
		{
			logger.error("index out of bound");
			throw new IndexOutOfBoundsException();
		}

		setDefaultModelObject(index);

		// force the tab's component to be aquired again if already the current tab
		currentTab = -1;
		setCurrentTab(index);

		return this;
	}

	private void setCurrentTab(int index)
	{
		if (this.currentTab == index) {
			// already current
			return;
		}
		
		this.currentTab = index;

		Component component;

		if (currentTab == -1 || (tabs.size() == 0) || !getVisiblityCache().isVisible(currentTab)) {
			// no tabs or the current tab is not visible
			component = newPanel();
		}
		else {
			component = panels.size()>=currentTab+1 ? panels.get(currentTab) : null;
			if (component == null) {
				// show panel from selected tab
				T tab = tabs.get(currentTab);
				component = tab.getPanel(TAB_PANEL_ID);
				if (component != null) {
					component.add(new PanelClassModifier(currentTab));
					panels.add(currentTab, component);
				}
				else {
					throw new WicketRuntimeException("ITab.getPanel() returned null. TabbedPanel [" +
							getPath() + "] ITab index [" + currentTab + "]");
				}
			}
		}

		if (!component.getId().equals(TAB_PANEL_ID)) {
			throw new WicketRuntimeException(
				"ITab.getPanel() returned a panel with invalid id [" +
					component.getId() +
					"]. You must always return a panel with id equal to the provided panelId parameter. TabbedPanel [" +
					getPath() + "] ITab index [" + currentTab + "]");
		}

		getTabContent().addOrReplace(component);
	}

	
	protected WebMarkupContainer getTabContent() {
		return tabContent;
		// return ((WebMarkupContainer) get("tab-content"));
	}
	
	
	
	private WebMarkupContainer newPanel()
	{
		return new WebMarkupContainer(TAB_PANEL_ID);
	}

	@Override
	public void onDetach() {
		visibilityCache = null;
		super.onDetach();
	}

	private VisibilityCache getVisiblityCache()	{
		if (visibilityCache == null) {
			visibilityCache = new VisibilityCache();
		}
		return visibilityCache;
	}

	public void setTabMenuVisibility(boolean visible){
		this.get("tabs-container").setVisible(visible);
	}

	/**
	 * A cache for visibilities of {@link ITab}s.
	 */
	private class VisibilityCache
	{

		/**
		 * Visibility for each tab.
		 */
		private Boolean[] visibilities;

		/**
		 * Last visible tab.
		 */
		private int lastVisible = -1;

		public VisibilityCache()
		{
			visibilities = new Boolean[tabs.size()];
		}

		public int getLastVisible()
		{
			if (lastVisible == -1)
			{
				for (int t = 0; t < tabs.size(); t++)
				{
					if (isVisible(t))
					{
						lastVisible = t;
					}
				}
			}

			return lastVisible;
		}

		public boolean isVisible(int index) {
			if (visibilities.length < index + 1) {
				Boolean[] resized = new Boolean[index + 1];
				System.arraycopy(visibilities, 0, resized, 0, visibilities.length);
				visibilities = resized;
			}

			if (visibilities.length > 0) {
				Boolean visible = visibilities[index];
				if (visible == null)
				{
					visible = tabs.get(index).isVisible();
					visibilities[index] = visible;
				}
				return visible;
			}
			else {
				return false;
			}
		}


	}



	public String getContentPanelCss() {
		return this.content_panel_css;
	}
	public void setContentPanelCss(String string) {
		content_panel_css = string;
		if (this.isInitialized()) {
			WebMarkupContainer w = ((WebMarkupContainer) get("content-container:content-panel"));
			if (w!=null)
				w.add(new AttributeModifier("class", content_panel_css));
		}
	}
	
	
	
	
	
	
	public void setMenuItemFactory(List<MenuItemFactory<Panel>> list) {
		this.menu_item_factory_list=list;
	}
	
	protected List<MenuItemFactory<Panel>> getMenuItems() {
		return menu_item_factory_list;
	}
	
	
	
	
	protected Panel getMenu() {
		return getMenu("menu");
	}
	
	/**
	 * @return
	 */
	protected Panel getMenu(String mid) {

		try {
				ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>(mid, new Model<Panel>(this));
				menu.add(new AttributeModifier("class", "dropdown-menu " + (isRightMenu() ? " dropdown-menu-right" : " dropdown-menu-left")));
				menu.setOutputMarkupId(true);
				
				if ( getMenuItems()!=null && getMenuItems().size()>0) {
					for (MenuItemFactory<Panel> item: getMenuItems()) {
						menu.addItem(item);
					}
				}
				
				menu.addItem(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new SeparatorMenuItemPanelV5<Panel>(id) {
							@Override
							public String getCssClass() {
								return "divider";
							}
							@Override
							public boolean isVisible() {
									return true;
							}
						};
					}
				});
				
				menu.addItem(new MenuItemFactory<Panel>() {
				
					
					
					
					
					
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new AjaxMenuItemPanelV5<Panel>(id) {
							
							@Override
							public boolean isVisible() {
								return VerticalAjaxTabbedPanel.this.isTwoPanels;
							}
							
							@Override 
							public String getLabel() {
								return  VerticalAjaxTabbedPanel.this.getLabel("switch-sides").getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									VerticalAjaxTabbedPanel.this.isRightMenu=!VerticalAjaxTabbedPanel.this.isRightMenu;
									 ((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(getKey()+"-VerticalTab", "isrightmenu", (isRightMenu() ? "yes" : "no"));
									 VerticalAjaxTabbedPanel.this.headerContainer.add(new AttributeModifier("class", "col-lg-2 col-md-3 col-xs-12" + (isRightMenu() ? " head-right " : " head-left ")));
									VerticalAjaxTabbedPanel.this.contentContainer.add(new AttributeModifier("class", "col-lg-10 col-md-9 col-xs-12 " + (isRightMenu() ? "content-left " : " content-right")));
									((Panel) VerticalAjaxTabbedPanel.this.headerContainer.get("menu")).add(new AttributeModifier("class", "dropdown-menu " + (isRightMenu() ? " dropdown-menu-right" : " dropdown-menu-left")));
									target.add(VerticalAjaxTabbedPanel.this);
									
								} 
								catch (Exception e) {
									logger.error(e);	
								}
							}
						};
					}
				});
				
				
				
				menu.addItem((id) ->
				new AjaxMenuItemPanelV5<Panel>(id) {
					@Override 
					public String getLabel() {
						return  "Hide/Show panel";
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try {
				
							VerticalAjaxTabbedPanel.this.isTwoPanels=!VerticalAjaxTabbedPanel.this.isTwoPanels;
							 
							 
							((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(getKey()+"-VerticalTab", "twopanels", (VerticalAjaxTabbedPanel.this.isTwoPanels ? "yes" : "no"));


							if (!VerticalAjaxTabbedPanel.this.isTwoPanels) {
								VerticalAjaxTabbedPanel.this.headerContainer.setVisible(false);
								VerticalAjaxTabbedPanel.this.contentContainer.add(new AttributeModifier("class", "col-lg-12 col-md-12 col-xs-12 " + (isRightMenu() ? "content-left " : " content-right")));
								VerticalAjaxTabbedPanel.this.onePanelMenuContainer.setVisible(true);
							}
							else {
								VerticalAjaxTabbedPanel.this.headerContainer.setVisible(true);
								VerticalAjaxTabbedPanel.this.contentContainer.add(new AttributeModifier("class", "col-lg-10 col-md-9 col-xs-12 " + (isRightMenu() ? "content-left " : " content-right")));
								VerticalAjaxTabbedPanel.this.onePanelMenuContainer.setVisible(false);
							}

							target.add(VerticalAjaxTabbedPanel.this);
						} 
						catch (Exception e) {
							logger.error(e);	
						}
					}
				}	
			);
				
			return menu;
				
			} catch (Exception e) {
				logger.error(e);
				return new InvisiblePanel("menu");
			}
	}
		
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	public void setTitle(IModel<String> t) {
		this.title=t;
	}
	
	
	
	public String getKey() {
		return this.key;
	}
	
	
	public IModel<String> getTitle() {
		return this.title;
	}
	
	
	public boolean isRightMenu() {
		return this.isRightMenu;
	}

	
	
	public void setSelectedTab(String a) {
		if (a==null)
			return;
		int index = 0;
		for (ITab it:getTabs()) {
			if (it instanceof ITabKB) {
				if (a.contentEquals(((ITabKB) it).getKey())) {
					setSelectedTab(index);
					return;
				}
			}
			index++;
		}
		logger.debug("No tab with key -> " + a);
		
		try {
			
			Integer in=Integer.valueOf(a)-1;
			if (in>=0 && in < getTabs().size()) {
				setSelectedTab(in.intValue());
				return;
			}
			else
				logger.debug("No tab with index -> " + in);
			
		} catch (Exception e) {
			logger.warn(e);
		}
	}
	

	
}
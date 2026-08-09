package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.AbstractTabWithIcon;
import com.novamens.wicket.markup.html.tabs.ITabKB;

@SuppressWarnings("serial")
public class VerticalLayout<T extends ITab> extends KBPanel {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(VerticalLayout.class.getName());
	
	public static final String TAB_PANEL_ID = "panel";  	/** id used for child panels */

	public static final int COLS_10X2 = 1;
	public static final int COLS_9X3  = 2;
	public static final int COLS_8X4  = 3;
	public static final int COLS_7X5  = 4;
	public static final int COLS_6X6  = 5;
	
	static public int HORIZONTAL = 0;
	static public int VERTICAL = 1;

	private String key;
	
	// top bar
	private Panel top_bar;
	
	// header
	private Panel top_v_header_panel;
	private WebMarkupContainer headerContainer;
	private Panel bottom_v_header_panel;
		
	// content
	private WebMarkupContainer tabContent;
	private final List<T> tabs; 

	private WebMarkupContainer contentContainer;
	private WebMarkupContainer contentMarkup; 
	private Panel content_toolbar = new InvisiblePanel("content-toolbar");
	private Panel top_v_content_panel;
	private Panel bottom_v_content_panel;
	
	private boolean isRightMenu;
	private boolean isTwoPanels = true;

	private IModel<String> title;
	private List<MenuItemFactory<Panel>> menu_item_factory_list;
	
	private int currentTab = -1;
	private boolean isVertical;
	private List<Component> panels;
	private WebMarkupContainer onePanelMenuContainer;
	private boolean expandDown = true;
	private WebMarkupContainer headerTabsContainer;
	private String content_panel_css;
	
	private String navigation_width_css;
	private String main_panel_width_css;
	
	private transient VisibilityCache visibilityCache;
	private  boolean isColumnsView = true;
	
	/** ---------------------------------------------------------
	 * 
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
	/** ---------------------------------------------------------
	 * 
	 */
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
	 
	/** -----------------------------------------------
	 * 
	 * */
	public VerticalLayout(final String id, final String key, final List<T> tabs)	{
		this(id, key, tabs, HORIZONTAL);
	}
	
	public VerticalLayout(final String id, final String key,   final List<T> tabs, int orientation) {
		this(id, key, tabs, orientation, true);
	}
	
	public VerticalLayout(final String id, String key, final List<T> tabs, int orientation, boolean isRightMenu) {
		super(id);

		setOutputMarkupId(true);
		
		setSections(VerticalLayout.COLS_10X2);
		
		this.key=key;
		this.tabs = Args.notNull(tabs, "tabs");
		
		this.isRightMenu=isRightMenu;
		isVertical=true;
		
		
		setOutputMarkupId(true);
		
		this.panels = new ArrayList<Component>(tabs.size());
		for (int i=0; i<tabs.size(); i++) 
			this.panels.add(i, null);
		
	}
	
	
	/**
	 * 
	 */
	private WebMarkupContainer headerInfoPanel;
	public WebMarkupContainer getHeaderInfoPanel() {
		if (headerInfoPanel==null)
			return new InvisiblePanel("header-info-panel");
		return headerInfoPanel;
	}
	public void setHeaderInfoPanel(WebMarkupContainer panel) {
		if (!panel.getId().equals("header-info-panel")) 
			throw new IllegalArgumentException("must have id = header-info-panel");
		headerInfoPanel=panel;
		if (this.isInitialized())
			headerContainer.addOrReplace(panel);
	}
	
	
	
	/**
	 * 
	 */
	private WebMarkupContainer headerNavTitlePanel;
	public WebMarkupContainer getHeaderNavTitlePanel() {
		if (headerNavTitlePanel==null)
			return new InvisiblePanel("header-nav-title");
		return headerNavTitlePanel;
	}
	public void setHeaderNavTitlePanel(WebMarkupContainer panel) {
		if (!panel.getId().equals("header-nav-title")) 
			throw new IllegalArgumentException("must have id = header-nav-title");
		headerNavTitlePanel=panel;
		if (this.isInitialized())
			headerContainer.addOrReplace(panel);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

			// -----------
			// toolbar
			//
			//
			if (top_bar!=null)
				add(top_bar);
			else 
				add(new InvisiblePanel("toolbar"));
				
			onePanelMenuContainer =  new WebMarkupContainer("one-panel-menu-container");
			onePanelMenuContainer.add(getOnePanelMenu());
			onePanelMenuContainer.add(getColumnsView());
			onePanelMenuContainer.setOutputMarkupId(true);
			onePanelMenuContainer.setVisible(false);

			if (!isExpandDown()) {
				onePanelMenuContainer.add(new AttributeModifier("class", "notexpanddown"));	
			}
			else {
				onePanelMenuContainer.add(new AttributeModifier("class", "expanddown"));
			}
			add(onePanelMenuContainer);
			
			headerTabsContainer = newTabsContainer("header-tabs-container");
			headerTabsContainer.setOutputMarkupId(true);
			
			final IModel<Integer> tabCount = new IModel<Integer>() {
				@Override
				public Integer getObject() {
					return VerticalLayout.this.tabs.size();
				}
			};

			// Header	-----------------
			//
			headerContainer =  new WebMarkupContainer("header-container");
			headerContainer.setOutputMarkupId(true);
			add(headerContainer);
			
			headerContainer.addOrReplace(getHeaderInfoPanel());
			headerContainer.addOrReplace(getHeaderNavTitlePanel());
			

			headerContainer.addOrReplace(getMenu());
			headerContainer.add(getColumnsView());
			
			headerContainer.add(new AttributeModifier("class", getNavigationWidthCss() + (isRightMenu() ? " head-right " : " head-left ")));
			
			if (getTitle()!=null) {
			
				Label l_title = new Label("title", getTitle());
				headerContainer.add(l_title);
			}
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
					final T tab = VerticalLayout.this.tabs.get(index);

					boolean isLink = ((tab instanceof ITabKB) ? ((ITabKB) tab).isLink() : true);
					
					// no titlelink ----------
					
					WebMarkupContainer titleNoLink = new WebMarkupContainer("title-nolink");
					Component ctitleNolink = newTitle("title", getTabTitle(tab), index,((tab instanceof ITabKB) ? ((ITabKB) tab).getCss() : null), ((tab instanceof ITabKB) ? ((ITabKB) tab).getStyle() : null));
					titleNoLink.add(ctitleNolink);
					titleNoLink.setVisible(!isLink);
					item.add(titleNoLink);
					final WebMarkupContainer titleLink = newLink("link", index);
					if (VerticalLayout.this.tabs.get(index) instanceof AbstractTabWithIcon) {
						String iconclass = ( (AbstractTabWithIcon) VerticalLayout.this.tabs.get(index)).getIconClass();
							WebMarkupContainer icon = new WebMarkupContainer("icon");
							if (iconclass!=null) {
								icon.add(new AttributeModifier("class", iconclass));
								titleLink.add(icon);
								String icontitle = ( (AbstractTabWithIcon) VerticalLayout.this.tabs.get(index)).getIconTitle();
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
					
					
					// titlelink ---------------
					//
					titleLink.setVisible(isLink);
					titleLink.add(newTitle("title", getTabTitle(tab), index, 
							((tab instanceof ITabKB) ? ((ITabKB) tab).getCss() : null),
							((tab instanceof ITabKB) ? ((ITabKB) tab).getStyle() : null)));
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
			
			if (getSessionUser()!=null) {
				this.isRightMenu = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue(getKey()+"-VerticalTab", "isrightmenu", "yes").equals("yes");
				this.isTwoPanels = ((KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue(getKey()+"-VerticalTab", "twopanels", "yes").equals("yes");
			}

			setOutputMarkupId(true);
			
			StringBuilder str = new StringBuilder();
			str.append((isRightMenu() ? " content-left " : " content-right"));
			str.append((isExpandDown() ? " is-expanddown " : ""));

			contentContainer.add(new AttributeModifier("class", getMainPanelWidthCss() + str.toString()));
			
			contentMarkup = new WebMarkupContainer("content-panel");
			
			contentMarkup.add(content_toolbar);
			
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
			
			setUpPanels();
			
			// getCss is for nav: tab pills
			if (getNavCss()!=null) {
				getHeaderTabsContainer().add(new AttributeModifier("class",getNavCss()));;
			}
			
			
			
			add(new WicketEventListener<AbstractWicketAjaxEvent>() {
				@Override
				public void onEvent(AbstractWicketAjaxEvent event) {
					for (int t=0; t<panels.size(); t++) {
						Object panel = panels.get(t);
						if (panel instanceof KBPanel && t!=currentTab) {
							List<Component> panels = new ArrayList<>();
							panels.add((KBPanel)panel);
							((KBPanel)panel).fire(event, panels.iterator());
							((KBPanel)panel).fire(event, ((KBPanel)panel).iterator());
						}
					}
				}
			});
	}
	
	public Panel setSelectedTab(T selectedtab) {
		int i = 0;
		for (T tab : getTabs()) {
			if (tab==selectedtab) {
				return setSelectedTab(i);
			}
			else {
				i++;
			}
		}
		return null;
	}
					
	public void setToolbarPanel(Panel panel) {
		if (!panel.getId().equals("toolbar")) 
			throw new IllegalArgumentException("must have id = toolbar");
		top_bar=panel;
		if (this.isInitialized())
			addOrReplace(panel);
	}


	
	public void setContentTopPanel(Panel panel) {
		if (!panel.getId().equals("content-top-panel")) 
			throw new IllegalArgumentException("must have id = content-top-panel");
		top_v_content_panel=panel;
		if (this.isInitialized())
			contentMarkup.addOrReplace(panel);
	}
	
	public void setContentToolbar(Panel panel) {
		if (!panel.getId().equals("content-toolbar")) 
			throw new IllegalArgumentException("must have id = content-toolbar");
		content_toolbar=panel;
		if (this.isInitialized())
			contentMarkup.addOrReplace(panel);
	}

	public void setContentBottomPanel(Panel panel) {
									
		if (!panel.getId().equals("content-bottom-panel")) 
			throw new IllegalArgumentException("must have id = content-bottom-panel");
		
		bottom_v_content_panel=panel;
		
		if (this.isInitialized()) {
			try {
				WebMarkupContainer w = ((WebMarkupContainer) get("content-container:content-panel"));
				if (w!=null)
					((WebMarkupContainer) get("content-container:content-panel")).addOrReplace(bottom_v_content_panel);
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public void setHeaderTopPanel(Panel panel) {
		if (!panel.getId().equals("header-top-panel")) 
			throw new IllegalArgumentException("must have id=header-top-panel");
		top_v_header_panel=panel;
	}

	public void setHeaderBottomPanel(Panel panel) {
		if (!panel.getId().equals("header-bottom-panel")) 
			throw new IllegalArgumentException("must have id=header-bottom-panel");
		
		bottom_v_header_panel=panel;
		
		if (this.isInitialized()) {
			try {
				WebMarkupContainer w = ((WebMarkupContainer) get("header-container:header-bottom-panel"));
				if (w!=null)
					((WebMarkupContainer) get("header-container")).addOrReplace(bottom_v_content_panel);
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}

	
	
	public <P extends WebMarkupContainer> List<T> getTabs(Class<P> panelclass) {
		List<T> tabs = new ArrayList<T>();
		for (T tab : getTabs()) {
			if (panelclass.isAssignableFrom(tab.getPanel("panel").getClass())) {
				tabs.add(tab);
			}
		}
		return tabs;
	}

	
	public void setExpandDown( boolean b) {
		this.expandDown=b;
	}
	
	public boolean isExpandDown() {
		return this.expandDown;
	}
	

	public void setTabMenuVisibility(boolean visible){
		this.get("tabs-container").setVisible(visible);
	}
	 
	public void setMenuItemFactory(List<MenuItemFactory<Panel>> list) {
		this.menu_item_factory_list=list;
	}

	@Override
	public void onDetach() {
		visibilityCache = null;
//		for (Component panel : panels) {
//			panel.detach();
//		}
		super.onDetach();
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
	
	

	/**
	 * @return list of tabs that can be used by the user to add/remove/reorder tabs in the panel
	 */
	public final List<T> getTabs() {
		return tabs;
	}
	/**
	 * sets the selected tab
	 * @param index
	 *            index of the tab to select
	 * @return this for chaining
	 * @throws IndexOutOfBoundsException
	 *             if index is not in the range of available tabs
	 */
	public VerticalLayout<T> setSelectedTab(final int index)
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
					target.get().add(VerticalLayout.this);
					onAjaxUpdate(target.get());
				}
			}
		};
	}

	
	protected IModel<String> getTabTitle(T tab) {
		return tab.getTitle();
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
	protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index, String css) {
		return newTitle(titleId, titleModel, index,  css, null);
	}
	
	
	protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index, String css, String style) {
		
		Label label=new Label(titleId, titleModel);
		label.setEscapeModelStrings(false);
		if (css!=null)
			label.add( new AttributeModifier("class", css));
		
		if (style!=null)
			label.add( new AttributeModifier("style", style));
		
		return label;
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

	protected Panel getMenu() {
		return getMenu("menu");
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
	 * Override of the default initModel behaviour. This component <strong>will not</strong> use any
	 * compound model of a parent.
	 * 
	 * @see org.apache.wicket.Component#initModel()
	 */
	@Override
	protected IModel<?> initModel()	{
		return new Model<Integer>(-1);
	}
	
	
	
	// tab panel container
	protected String getTabPanelContainerCss() {
		return "tab-panel-container";
	}

	public Component getHeaderTabsContainer() {
		return headerTabsContainer;// .add(new AttributeModifier("class",getNavCss()));

	}

	
	@Override
	protected void onBeforeRender()	{
		super.onBeforeRender();
		
		
		int index = getSelectedTab();

		
		if (	(index == -1) || 
				(getVisiblityCache().isVisible(index) == false) ||
				(!hasPanel(index))
			)
		{
			// find first visible tab
			index = -1;
			for (int i = 0; i < tabs.size(); i++)
			{
				if (getVisiblityCache().isVisible(i) &&  hasPanel(i))
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

	private boolean hasPanel(int index) {
		if (tabs.get(index) instanceof AbstractTabKB) {
			return (((AbstractTabKB) tabs.get(index)).isLink());  
		}
		return true;
	}

	protected WebMarkupContainer getTabContent() {
		return tabContent;
		// return ((WebMarkupContainer) get("tab-content"));
	}
	
	
	
	
	
	protected List<MenuItemFactory<Panel>> getMenuItems() {
		return menu_item_factory_list;
	}
	
	
	protected void onAjaxUpdate(AjaxRequestTarget target) {
		
	}
	
	
	
	
	public boolean isColumnsView() {
		return isColumnsView;
	}
	
	
	protected WebMarkupContainer getColumnsView() {
	
		if (!isColumnsView())
			return new InvisiblePanel("columns-view");
		
		AjaxLink<Void> a = new AjaxLink<Void> ("columns-view") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onChangeView(target);
			}
		};
		
		WebMarkupContainer w = new WebMarkupContainer("col-icon");
		w.add( new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (!VerticalLayout.this.isTwoPanels)
					return "fal fa-table-columns";
				else
					return "fal fa-rectangle";
			}
		}));
				
		a.add(w);
		return a;
	}
	
	
	private void setUpPanels() {
		StringBuilder str = new StringBuilder();
		str.append((isExpandDown() ? " is-expanddown " : ""));
		str.append((isRightMenu() ? " content-left " : " content-right"));
		str.append((VerticalLayout.this.isTwoPanels ? " two-panels " : " one-panel "));
		if (!VerticalLayout.this.isTwoPanels) {
			VerticalLayout.this.headerContainer.setVisible(false);
			VerticalLayout.this.contentContainer.add(new AttributeModifier("class", "col-lg-12 col-md-12 col-xs-12 " + str.toString()));
			VerticalLayout.this.onePanelMenuContainer.setVisible(true);
		}
		else {
			VerticalLayout.this.headerContainer.setVisible(true);
			VerticalLayout.this.contentContainer.add(new AttributeModifier("class", getMainPanelWidthCss() + str.toString()));
			VerticalLayout.this.onePanelMenuContainer.setVisible(false);
		}
	}
		
	
	protected void onChangeView(AjaxRequestTarget target) {
		
		VerticalLayout.this.isTwoPanels=!VerticalLayout.this.isTwoPanels;
		if (getSessionUser()!=null)
			((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(getKey()+"-VerticalTab", "twopanels", (VerticalLayout.this.isTwoPanels ? "yes" : "no"));
		setUpPanels();
		target.add(VerticalLayout.this);
	}
	
	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	protected Panel getMenu(String mid) {

		try {
				ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>(mid, new Model<Panel>(this));
				menu.add(new AttributeModifier("class", "dropdown-menu " + (isRightMenu() ? " dropdown-menu-right" : " dropdown-menu-left")));
				menu.setOutputMarkupId(true);
				
				if (getTitle()!=null) {
					menu.addItem(id ->
					new HeaderMenuItemPanelV5<Panel>(id) {
						@Override
						public String getLabel() {
							return VerticalLayout.this.getTitle().getObject();
						}
						}
					);
				}
				
				
					
					int index = 0;
					for(T tab:getTabs()) {
						
						final int TAB = index++;
						
						
							menu.addItem((id) ->
							new AjaxMenuItemPanelV5<Panel>(id) {
								@Override 
								public String getLabel() {
									if (getTabs().get(TAB).getTitle()!=null)
										return  getTabs().get(TAB).getTitle().getObject();
									return "";
								}
								@Override
								public void onClick(AjaxRequestTarget target) throws Exception {
									try {
										VerticalLayout.this.setSelectedTab(TAB);
										target.add(VerticalLayout.this);
									} 
									catch (Exception e) {
										logger.error(e);	
									}
								}
							}	
						);
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
					
					
					/**	
					 * Usuaurio
					 * 
					 * 
					 */
					
				/**	
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
				}*/
				
				
				menu.addItem(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new AjaxMenuItemPanelV5<Panel>(id) {
							
							@Override
							public boolean isVisible() {
								return true;
							}
							
							@Override 
							public String getLabel() {
								return  VerticalLayout.this.getLabel("switch-sides").getObject();
							}

							@Override
							public void onClick(AjaxRequestTarget target) throws Exception {
								try {
									VerticalLayout.this.isRightMenu=!VerticalLayout.this.isRightMenu;
									
									 ((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(getKey()+"-VerticalTab", "isrightmenu", (isRightMenu() ? "yes" : "no"));
									 VerticalLayout.this.headerContainer.add(new AttributeModifier("class", getNavigationWidthCss() + (isRightMenu() ? " head-right " : " head-left ")));
									 
									
									 
									StringBuilder str = new StringBuilder();
									str.append((isRightMenu() ? " content-left " : " content-right"));
									str.append((isExpandDown() ? " is-expanddown " : ""));
									 
									 VerticalLayout.this.contentContainer.add(new AttributeModifier("class", getMainPanelWidthCss() + str.toString()));
									((Panel) VerticalLayout.this.headerContainer.get("menu")).add(new AttributeModifier("class", "dropdown-menu " + (isRightMenu() ? " dropdown-menu-right" : " dropdown-menu-left")));
									target.add(VerticalLayout.this);
									
								} 
								catch (Exception e) {
									logger.error(e);	
								}
							}
						};
					}
				});
				
				
				if ( getMenuItems()!=null && getMenuItems().size()>0) {
					
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

					
					
					for (MenuItemFactory<Panel> item: getMenuItems()) {
						menu.addItem(item);
					}
				}
				
				

				/**
				menu.addItem((id) ->
				new AjaxMenuItemPanelV5<Panel>(id) {
					@Override 
					public String getLabel() {
						return  new StringResourceModel("hide-show", VerticalLayout.this, null).getObject();
					}
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						try {
							onChangeView(target);
						} 
						catch (Exception e) {
							logger.error(e);	
						}
					}
				}	
			);**/
				
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

	public void setSections(int sec) {
		
		if (sec==COLS_9X3) {
			main_panel_width_css = "col-lg-9 col-md-8 col-xs-12";
			navigation_width_css = "col-lg-3 col-md-4 col-xs-12";
			
		}
		else if (sec==COLS_10X2) {
			main_panel_width_css = "col-lg-10 col-md-9 col-xs-12";
			navigation_width_css = "col-lg-2 col-md-3 col-xs-12";

		}
		else if (sec==COLS_8X4) {
			main_panel_width_css = "col-lg-8 col-md-7 col-xs-12";
			navigation_width_css = "col-lg-4 col-md-5 col-xs-12";

		}
		else if (sec==COLS_7X5) {
			main_panel_width_css = "col-lg-7 col-md-12 col-xs-12";
			navigation_width_css = "col-lg-5 col-md-12 col-xs-12";

		}
		else if (sec==COLS_6X6) {
			main_panel_width_css = "col-lg-6 col-md-12 col-xs-12";
			navigation_width_css = "col-lg-6 col-md-12 col-xs-12";
		}
	}

	public void setNavigationWidthXss(String navigation_width_css) {
		this.navigation_width_css = navigation_width_css;
	}

	public void setMainPanelWidthCss(String main_panel_width_css) {
		this.main_panel_width_css = main_panel_width_css;
	}

	
	public String getMainPanelWidthCss() {
		return main_panel_width_css;
	}
	
	public String getNavigationWidthCss() {
		return navigation_width_css;
	}

	// tab panel
	protected String getTabPanelCss() {
		return "tab-panel";
	}
	
	protected Panel getOnePanelMenu() {
		return getMenu("menuop");
	}
	
	private VisibilityCache getVisiblityCache()	{
		if (visibilityCache == null) {
			visibilityCache = new VisibilityCache();
		}
		return visibilityCache;
	}

	private WebMarkupContainer newPanel()
	{
		return new WebMarkupContainer(TAB_PANEL_ID);
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

	


	
}
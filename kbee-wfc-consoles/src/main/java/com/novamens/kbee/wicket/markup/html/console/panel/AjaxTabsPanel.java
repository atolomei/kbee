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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;

import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabWithIcon;
import com.novamens.wicket.markup.html.tabs.ITabKB;

/**
 * 
 * tabs-container: nav
 * 
 */
@SuppressWarnings("serial")
public class AjaxTabsPanel<T extends ITab> extends KBPanel {
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AjaxTabsPanel.class.getName());
	

	public static final String TAB_PANEL_ID = "panel";  	/** id used for child panels */

	private int currentTab = -1;

	private transient VisibilityCache visibilityCache;

	private String key;

	//private WebMarkupContainer headerContainer;
	private WebMarkupContainer headerTabsContainer;
	
	//private Panel bottom_v_header_panel;
		
	// content
	
	private WebMarkupContainer tabContent;
	private final List<T> tabs; 
	private List<Component> panels;

	boolean isVertical;

	static public int HORIZONTAL = 0;
	static public int VERTICAL = 1;

	// Component tabs_component;
	
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
	
	public AjaxTabsPanel(final String id, final String key, final List<T> tabs)	{
		this(id, key, tabs, HORIZONTAL);
	}
	
	public AjaxTabsPanel(final String id, final String key,   final List<T> tabs, int orientation) {
		this(id, key, tabs, orientation, true);
	}
	
	public AjaxTabsPanel(final String id, String key, final List<T> tabs, int orientation, boolean isRightMenu) {
		super(id);

		setOutputMarkupId(true);
		
		this.key=key;
		this.tabs = Args.notNull(tabs, "tabs");
		
		isVertical=true;
		
		this.panels = new ArrayList<Component>(tabs.size());
		for (int i=0; i<tabs.size(); i++) 
			this.panels.add(i, null);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		headerTabsContainer = newTabsContainer("header-tabs-container");
		headerTabsContainer.setOutputMarkupId(true);
		
		final IModel<Integer> tabCount = new IModel<Integer>() {
			@Override
			public Integer getObject() {
				return AjaxTabsPanel.this.tabs.size();
			}
		};

		// add the loop used to generate tab names
		//
		headerTabsContainer.add(new Loop("tabs", tabCount) {
			@Override
			protected void populateItem(final LoopItem item) {
				final int index = item.getIndex();
				final T tab = AjaxTabsPanel.this.tabs.get(index);

				final WebMarkupContainer titleLink = newLink("link", index);
										
				if (AjaxTabsPanel.this.tabs.get(index) instanceof AbstractTabWithIcon) {
					String iconclass = ( (AbstractTabWithIcon) AjaxTabsPanel.this.tabs.get(index)).getIconClass();
						WebMarkupContainer icon = new WebMarkupContainer("icon");
						if (iconclass!=null) {
							icon.add(new AttributeModifier("class", iconclass));
							titleLink.add(icon);
							String icontitle = ( (AbstractTabWithIcon) AjaxTabsPanel.this.tabs.get(index)).getIconTitle();
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
		
		add(headerTabsContainer);
	}

	public final int getSelectedTab() {
		return (Integer)getDefaultModelObject();
	}
	
	public Component getTab(int index) {
		Component component = panels.get(index);
		return component;
	}
	
	public AjaxTabsPanel<T> setSelectedTab(final int index)	{
		if ((index < 0) || (index >= tabs.size()))	{
			logger.error("index out of bound");
			throw new IndexOutOfBoundsException();
		}

		setDefaultModelObject(index);

		// force the tab's component to be aquired again if already the current tab
		currentTab = -1;
		setCurrentTab(index);

		return this;
	}
	
	protected IModel<String> getTabTitle(T tab) {
		return tab.getTitle();
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
	
	protected String getNavCss() {
		return isVertical() ? "nav nav-pills nav-stacked" : "nav nav-pills nav-horizontal";
	}
	
	protected boolean isVertical() {
		return this.isVertical;
	}

	protected String getTabPanelContainerCss() {
		return "tab-panel-container";
	}

	protected Component getHeaderTabsContainer() {
		return headerTabsContainer.add(new AttributeModifier("class",getNavCss()));
	}

	protected String getTabPanelCss() {
		return "tab-panel";
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

	protected WebMarkupContainer newLink(final String linkId, final int index) {
		return new AjaxFallbackLink<Void>(linkId) {
			@Override
			public void onClick(final Optional<AjaxRequestTarget> target) 	{
				setSelectedTab(index);
				if (target.isPresent()) {
					target.get().add(AjaxTabsPanel.this);
					onAjaxUpdate(target.get());
				}
			}
		};
	}

	protected void onAjaxUpdate(AjaxRequestTarget target) {
		
	}

	private void setCurrentTab(int index) {
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
	}
	
	private WebMarkupContainer newPanel() {
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
	private class VisibilityCache {

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
	
	public String getKey() {
		return this.key;
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
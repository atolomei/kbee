package com.novamens.wicket.markup.html.tabs;

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

/**
 * 
 * tabs-container: nav
 * 
 */
@SuppressWarnings("serial")
public class AjaxTabbedPanel<T extends ITab> extends Panel {
	private static final long serialVersionUID = 1L;

	/** id used for child panels */
	public static final String TAB_PANEL_ID = "panel";

	private final List<T> tabs;

	/** the current tab */
	private int currentTab = -1;
	
	private List<Component> panels;

	private transient VisibilityCache visibilityCache;
		
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

	public AjaxTabbedPanel(final String id, final List<T> tabs)	{
		this(id, tabs, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param tabs
	 *            list of ITab objects used to represent tabs
	 * @param model
	 *            model holding the index of the selected tab
	 */
	public AjaxTabbedPanel(final String id, final List<T> tabs, IModel<Integer> model) {
		super(id, model);

		setOutputMarkupId(true);
		
		this.tabs = Args.notNull(tabs, "tabs");
		this.panels = new ArrayList<Component>(tabs.size());
		for (int i=0; i<tabs.size(); i++) { this.panels.add(i, null);}

		final IModel<Integer> tabCount = new IModel<Integer>() {
			@Override
			public Integer getObject() {
				return AjaxTabbedPanel.this.tabs.size();
			}
		};

		WebMarkupContainer tabsContainer = newTabsContainer("tabs-container");
		add(tabsContainer);

		// add the loop used to generate tab names
		tabsContainer.add(new Loop("tabs", tabCount){
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final LoopItem item) {
				final int index = item.getIndex();
				final T tab = AjaxTabbedPanel.this.tabs.get(index);

				final WebMarkupContainer titleLink = newLink("link", index);
										
				if (AjaxTabbedPanel.this.tabs.get(index) instanceof AbstractTabWithIcon) {
					String iconclass = ( (AbstractTabWithIcon) AjaxTabbedPanel.this.tabs.get(index)).getIconClass();
						WebMarkupContainer icon = new WebMarkupContainer("icon");
						if (iconclass!=null) {
							
							icon.add(new AttributeModifier("class", iconclass));
							titleLink.add(icon);
							
							String icontitle = ( (AbstractTabWithIcon) AjaxTabbedPanel.this.tabs.get(index)).getIconTitle();
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
				
				titleLink.add( (newTitle("title", getTabTitle(tab), index)).setEscapeModelStrings(false));
				
				
				// tab.getTitle()
				
				item.add(titleLink);
			}

			@Override
			protected LoopItem newItem(final int iteration)	{
				return newTabContainer(iteration);
			}
		});

		
		WebMarkupContainer tabc = new WebMarkupContainer("tab-content");
		
		tabc.setOutputMarkupId(true);
		
		tabc.add(new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				 if (getTabPanelContainerCss()!=null)
					 return getTabPanelContainerCss();
				 return"";
			}
		}));
		
		add(tabc);
		tabc.add(newPanel());
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
	
	//protected String getCss() {
	//	return null;
	//}

	
	
	
	/**
	 * nav nav-pills nav-horizontal
	 * nav nav-pills nav-justified 
	 * nav nav-pills nav-stacked 
	 * 
	 * @return
	 */
	// Nav
	protected String getNavCss() {
		return "nav nav-pills nav-horizontal";
	}

	
	// tab panel container
	protected String getTabPanelContainerCss() {
		return "tab-panel-container";
	}

	
	// tab panel
	protected String getTabPanelCss() {
		return "tab-panel";
	}
	
	
	
	@Override
	protected void onBeforeRender()	{
		super.onBeforeRender();
		
		// getCss is for nav: tab pills
		if (getNavCss()!=null)
			get("tabs-container").add(new AttributeModifier("class",getNavCss()));
		
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
		return new Label(titleId, titleModel);
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
					target.get().add(AjaxTabbedPanel.this);
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
	public AjaxTabbedPanel<T> setSelectedTab(final int index)
	{
		if ((index < 0) || (index >= tabs.size()))
		{
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

		((WebMarkupContainer) get("tab-content")).addOrReplace(component);
	}

	private WebMarkupContainer newPanel()
	{
		return new WebMarkupContainer(TAB_PANEL_ID);
	}

	@Override
	protected void onDetach() {
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
}
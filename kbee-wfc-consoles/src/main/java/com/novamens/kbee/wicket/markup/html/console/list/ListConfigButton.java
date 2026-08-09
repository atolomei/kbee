package com.novamens.kbee.wicket.markup.html.console.list;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.GridConfigButton;
import com.novamens.kbee.wicket.markup.html.console.grid.GridConfigPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.modal.EmptyDialog;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class ListConfigButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListConfigButton.class.getName());

	boolean include_grid_browser_switcher = false;
	
	public ListConfigButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
			add(menu);

			menu.addItem(id ->
			new HeaderMenuItemPanelV5<Void>(id) {
				@Override
				public String getLabel() {
					return getLabelString("settings");
				}
			}
		);
			
		menu.addItem(id -> 
		new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
			}
		);
			
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					SubmenuAjaxItemPanelV5<Void> submenu = new SubmenuAjaxItemPanelV5<Void>(id, null, "far fa-angle-down") {
						@Override
						public String getLabel() {
							return new StringResourceModel("page-size", ListConfigButton.this, null).getString();
						}
					};
					submenu.addItem(subitemid ->
						new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									ListConfigButton.this.onClick(target, 20);
								}
							}
							@Override
							public String getLabel() {
								return "20";
							}
							@Override
							public boolean isIconVisible() {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									return (panel.getPageSize()==20);
								}
								return false;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", ListConfigButton.this, null).getString();
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
						}
					);
					submenu.addItem(subitemid ->
						new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									ListConfigButton.this.onClick(target, 40);
								}
							}
							@Override
							public String getLabel() {
								return "40";
							}
							@Override
							public boolean isIconVisible() {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									return (panel.getPageSize()==40);
								}
								return false;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", ListConfigButton.this, null).getString();
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
						}
					);
					submenu.addItem(subitemid ->
						new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									ListConfigButton.this.onClick(target, 60);
								}
							}
							@Override
							public String getLabel() {
								return "60";
							}
							@Override
							public boolean isIconVisible() {
								ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
								if (panel!=null) {
									return (panel.getPageSize()==60);
								}
								return false;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", ListConfigButton.this, null).getString();
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
						}
					);	
					
					
					
					submenu.addItem(subitemid ->
					new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
							if (panel!=null) {
								ListConfigButton.this.onClick(target, 120);
							}
						}
						@Override
						public String getLabel() {
							return "120";
						}
						@Override
						public boolean isIconVisible() {
							ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
							if (panel!=null) {
								return (panel.getPageSize()==120);
							}
							return false;
						}
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", ListConfigButton.this, null).getString();
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
					}
				);	
					

				submenu.addItem(subitemid ->
					new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
							if (panel!=null) {
								ListConfigButton.this.onClick(target, 240);
							}
						}
						@Override
						public String getLabel() {
							return "240";
						}
						@Override
						public boolean isIconVisible() {
							ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
							if (panel!=null) {
								return (panel.getPageSize()==240);
							}
							return false;
						}
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", ListConfigButton.this, null).getString();
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
					}
				);	

				
				submenu.addItem(subitemid ->
				new AjaxCheckMenuItemPanelV5<Void>(subitemid) {
					@Override
					public void onCheckClick(AjaxRequestTarget target) {
						ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
						if (panel!=null) {
							ListConfigButton.this.onClick(target, 480);
						}
					}
					@Override
					public String getLabel() {
						return "480";
					}
					@Override
					public boolean isIconVisible() {
						ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
						if (panel!=null) {
							return (panel.getPageSize()==480);
						}
						return false;
					}
					@Override 
					public String getWorkingLabel() {
						return new StringResourceModel("working", ListConfigButton.this, null).getString();
					}
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						attributes.setEventPropagation(EventPropagation.STOP); 
					}
				}
			);

					
					return submenu;
				}
			});
			

			menu.addItem(id -> 
				new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);
			
			menu.addItem(id ->
			new AjaxCheckMenuItemPanelV5<Void>(id) {
				@Override
				public void onCheckClick(AjaxRequestTarget target) {
					ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
					if (panel!=null) {
						if 			(panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_NOBORDER_BCK) { 
							ListConfigButton.this.onClick(target,ListDisplayMode.COMPACT_LIST_NOBORDER_BCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_BORDER_BCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMPACT_LIST_BORDER_BCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_BORDER_NOBCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMPACT_LIST_BORDER_BCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK);
						}
					}
				}
				@Override
				public String getLabel() {
					return new StringResourceModel("compact-view", ListConfigButton.this, null).getString();
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					attributes.setEventPropagation(EventPropagation.STOP); 
				}
				@Override
				public boolean isIconVisible() {
					ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
					if (panel!=null) {											
						if (panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_BORDER_BCK   || 
							panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_NOBORDER_BCK ||
							panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_BORDER_NOBCK ||
							panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK 
						)
							return true;
					}
					return false;
				}
				@Override 
				public String getWorkingLabel() {
					return new StringResourceModel("working", ListConfigButton.this, null).getString();
				}
			}
		);

			
			menu.addItem(id ->
			new AjaxCheckMenuItemPanelV5<Void>(id) {
				@Override
				public void onCheckClick(AjaxRequestTarget target) {
					ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
						if 			(panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_NOBORDER_BCK) { 
							ListConfigButton.this.onClick(target,ListDisplayMode.COMFORTABLE_LIST_NOBORDER_BCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_BORDER_BCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMFORTABLE_LIST_BORDER_BCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_BORDER_NOBCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMFORTABLE_LIST_BORDER_NOBCK);
						}
						else if 	(panel.getListDisplayMode()==ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK) {
							ListConfigButton.this.onClick(target,ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK);
						}
					}
				@Override
				public String getLabel() {
					return new StringResourceModel("comfortable-view", ListConfigButton.this, null).getString();
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					attributes.setEventPropagation(EventPropagation.STOP); 
				}
				@Override
				public boolean isIconVisible() {
					ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
					if (panel!=null) {											
						if 	   (panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_BORDER_BCK       || 
								panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_NOBORDER_BCK	  ||
								panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_BORDER_NOBCK 	  ||
								panel.getListDisplayMode()==ListDisplayMode.COMFORTABLE_LIST_NOBORDER_NOBCK 
							)
							return true;
					}
					return false;
				}
				@Override 
				public String getWorkingLabel() {
					return new StringResourceModel("working", ListConfigButton.this, null).getString();
				}
			}
		);
			menu.addItem(id -> 
				new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);

			
			if (include_grid_browser_switcher) {
		
				menu.addItem(id -> 
					new SeparatorMenuItemPanelV5<Void>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
					}
				);
							
				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<Void>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							fire (new GeneralAjaxWicketEvent(target, "list-browser")); 
						}
						@Override
						public String getLabel() {
							return getLabelString("list");
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						@Override
						public boolean isIconVisible() {
							return (getBrowser().getBrowserType().equals("list")) ||
								(getBrowser().getBrowserType().equals("treelist"));
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);
							
				String grid_label = getBrowser().getBrowserType();
				
				
				if (!getBrowser().getBrowserType().startsWith("tree")) {
					
					
					
					
					menu.addItem(id ->
						new AjaxCheckMenuItemPanelV5<Void>(id) {
						
							
							public boolean isVisible() {
								return (!getBrowser().getBrowserType().startsWith("tree"));
							}
							
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								onSetGridView(target);
							}
							@Override
							public String getLabel() {
								return getLabelString("grid");
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
							@Override
							public boolean isIconVisible() {
								return (getBrowser().getBrowserType().equals("grid")) ||
									(getBrowser().getBrowserType().equals("tree"));
							}
							@Override 
							public String getWorkingLabel() {
								return getLabelString("working");
							}
						}
					);
				}
		
			}			
			
			add(new EmptyDialog("config-modal"));
	}
	
	public void onClick(AjaxRequestTarget target, ListDisplayMode mode) {
		ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
		if (panel!=null) {
			panel.setListDisplayMode(mode);
			target.add(getBrowser());
		}
	}
	
	public void onClick(AjaxRequestTarget target, int size) {
		try {
			ListPanel<?> panel = (ListPanel<?>) getBrowser().getPanel(ListPanel.class);
			if (panel!=null) {
				if (panel.getPageSize()!=size) {
					panel.setPageSize(size);
					target.add(getBrowser());
				}	
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	public void setGridSwitcher(boolean include_grid_browser_switcher) {
		 this.include_grid_browser_switcher  = include_grid_browser_switcher;
	}
	
	protected void onSetGridView(AjaxRequestTarget target) {
		fire (new GeneralAjaxWicketEvent(target, "grid-browser"));
	}
}

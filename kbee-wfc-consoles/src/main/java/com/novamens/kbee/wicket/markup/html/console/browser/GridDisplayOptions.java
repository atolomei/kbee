package com.novamens.kbee.wicket.markup.html.console.browser;

 

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.console.BaseBrowser;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

						
public class GridDisplayOptions extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isEnabled() {
		return true;
	}

	public void onClick(AjaxRequestTarget target, GridDisplayMode mode) {
		GridPanel<?> panel = (GridPanel<?>) GridDisplayOptions.this.getBrowser().getPanel(GridPanel.class);
		if (panel!=null) {
			if (panel.getGridDisplayMode()!=mode) {
				panel.setGridDisplayMode(mode);
				target.add(getBrowser());
			}	
		}
	};


	public GridDisplayOptions(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
	setOutputMarkupId(true);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		add(menu);

		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					
					@Override 
					public String getWorkingLabel() {
						return "working";
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						 GridDisplayOptions.this.onClick(target, GridDisplayMode.COMPACT);
					}
					@Override
					public String getLabel() {
						return GridDisplayMode.COMPACT.getLabel(getLocale());
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		

		
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						 GridDisplayOptions.this.onClick(target, GridDisplayMode.COMPACT_GRID);
					}
					@Override
					public String getLabel() {
						return GridDisplayMode.COMPACT_GRID.getLabel(getLocale());
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override 
					public String getWorkingLabel() {
						return "working";
					}
				};
			}
		});

	
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						 GridDisplayOptions.this.onClick(target, GridDisplayMode.COMFORTABLE);
						
					}
					@Override
					public String getLabel() {
						return GridDisplayMode.COMFORTABLE.getLabel(getLocale());
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override 
					public String getWorkingLabel() {
						return "working";
					}
				};
			}
		});


		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget target) {
						 GridDisplayOptions.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID);
					}
					@Override
					public String getLabel() {
						return GridDisplayMode.COMFORTABLE_GRID.getLabel(getLocale());
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override 
					public String getWorkingLabel() {
						return "working";
					}
				};
			}
		});
	}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	

	

}


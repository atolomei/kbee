package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class GridSettingsButton<T> extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public GridSettingsButton(BaseBrowser<T> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
			
					}
					@Override
					public String getLabel() {
						return GridSettingsButton.this.getLabel("extended-mode").getObject();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
			
					}
					@Override
					public String getLabel() {
						return GridSettingsButton.this.getLabel("compact-mode").getObject();
						//return "Display Mode Compact";
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		
		
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});


		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
			
					}
					@Override
					public String getLabel() {					
						return GridSettingsButton.this.getLabel("row-bck").getObject();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
					}
					@Override
					public String getLabel() {
						return "Config";
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		
		
		add(menu);
		
		//add(new EventListener2<SelectionEvent>() {
		//	public void onEvent(SelectionEvent event) {
		//		event.getRequestTarget().add(WorkspaceTools.this);
		//	}
		//});
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}

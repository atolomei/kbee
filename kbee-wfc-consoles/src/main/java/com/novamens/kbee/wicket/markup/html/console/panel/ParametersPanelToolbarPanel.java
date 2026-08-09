package com.novamens.kbee.wicket.markup.html.console.panel;


import org.apache.wicket.ajax.AjaxRequestTarget;


import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.error.ErrorPanel;

@SuppressWarnings("serial")
public class ParametersPanelToolbarPanel extends KBPanel {
			
	private static final long serialVersionUID = 1L;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ParametersPanelToolbarPanel.class.getName());
	
	private boolean is_saved_query = true;
	private boolean is_saved_dashboard = true;
	
	public ParametersPanelToolbarPanel(String id) {
		super(id);
		
		add(new WorkingIndicatorAjaxLinkV5<Void>("clear-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
					onClearAll(target);
			}
			
			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", ParametersPanelToolbarPanel.this, null).getString();
			}
		});
		
		
		add(getMenu());
	}
	
	protected Panel getMenu() {
		try {
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
			menu.setOutputMarkupId(true);
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new AjaxMenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override 
						public String getLabel() {
							return ParametersPanelToolbarPanel.this.getLabel("edit").getObject();
						}

						@Override
						public boolean isVisible() {
							return isSaveQuerySupported();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								onSaveQuery(target);
							} 
							catch (ContentMgmtException e) {
								logger.error(e);	
							}
						}
					};
				}
			});
	

			
			menu.addItem(new MenuItemFactory<Void>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new AjaxMenuItemPanelV5<Void>(id) {
						private static final long serialVersionUID = 1L;
						@Override 
						public String getLabel() {
							return ParametersPanelToolbarPanel.this.getLabel("save-dashboard").getObject();
						}

						@Override
						public boolean isVisible() {
							return isSaveDashboardSupported();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								onSaveDashboardQuery(target);
							} 
							catch (ContentMgmtException e) {
								logger.error(e);	
							}
						}

					};
				}
			});

			return menu;
				
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("menu", e);
		}
	}
		
	protected void onSaveDashboardQuery(AjaxRequestTarget target) {}
	protected void onSaveQuery(AjaxRequestTarget target)  {}
	protected void onClearAll(AjaxRequestTarget target)   {}
						
	public boolean isSaveDashboardSupported() {
		return this.is_saved_dashboard;
	}
	
	public void setSaveDashboardSupported(boolean b) {
		this.is_saved_dashboard=b;
	}
	
	
	public boolean isSaveQuerySupported() {
		return this.is_saved_query;
	}
	
	public void setSaveQuerySupported(boolean b) {
		this.is_saved_query=b;
	}
	
	
	protected boolean isSaveQueryEnabled() {
		return true;
	}
	
	protected boolean isClearEnabled() {
		return true;
	}
}
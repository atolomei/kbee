package com.novamens.kbee.wicket.markup.html.console.grid;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.EmptyDialog;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class GridConfigButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( GridConfigButton.class.getName());

	static private List<Integer> PAGE_SIZES  = new ArrayList<Integer>();
	
	static  {
		PAGE_SIZES.add(Integer.valueOf(10));
		PAGE_SIZES.add(Integer.valueOf(20));
		PAGE_SIZES.add(Integer.valueOf(25));
		PAGE_SIZES.add(Integer.valueOf(30));
		PAGE_SIZES.add(Integer.valueOf(40));
		PAGE_SIZES.add(Integer.valueOf(50));
		PAGE_SIZES.add(Integer.valueOf(80));
		PAGE_SIZES.add(Integer.valueOf(120));
		PAGE_SIZES.add(Integer.valueOf(240));
		PAGE_SIZES.add(Integer.valueOf(480));
		PAGE_SIZES.add(Integer.valueOf(600));
		PAGE_SIZES.add(Integer.valueOf(1200));
	 }
	
	static private List<String> DATE_MODES  = new ArrayList<String>();
	
	static  {
		DATE_MODES.add(DateTimeService.COLlOQUIAL_AGO_LABEL);
		DATE_MODES.add(DateTimeService.COLlOQUIAL_LABEL);
		DATE_MODES.add(DateTimeService.MONTH_DAY_YEAR_LABEL);
		DATE_MODES.add(DateTimeService.FULL_LABEL);
		DATE_MODES.add(DateTimeService.TIMESTAMP_LABEL);
	 }
	
	private boolean is_created = false;
	private boolean isRememberQuery = false;
	private boolean include_list_browser_switcher = false;
	private boolean include_tree_browser_switcher = false;
	
	
	public GridConfigButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
		setRememberQuery( browser.isRememberQuery());
	}
	
	/**
	 * Grid Columns
	 * -------------------------
	 * Grid cells          check
	 * -------------------------
	 * Background odd rows check
	 * -------------------------
	 * Compact             check 
	 * Expanded
	 * -------------------------
	 * save as default 
	 * -------------------------
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
			ContextMenuPanel<ToolbarItem> menu = new ContextMenuPanel< ToolbarItem>( new Model< ToolbarItem>(this));
			add(menu);
			

			menu.addItem(id ->
					new com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5<ToolbarItem>(id) {
						@Override
						public String getLabel() {
							return getLabelString("settings");
						}
					}
				);

			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
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
			
			
			menu.addItem(id -> new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						if (!is_created) 
							addModal();
							
							((Modal)GridConfigButton.this.get("config-modal")).open(target, new Modal.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, com.novamens.wicket.markup.html.modal.Modal.Button button) {
									if (!button.isCancel()) {
										GridConfigPanel configpanel = (GridConfigPanel)((Modal)GridConfigButton.this.get("config-modal")).getBody();
										GridPanel<?> grid = (GridPanel<?>)getBrowser().getPanel(DataViewPanel.class);
										grid.setPageSize(configpanel.getPageSize());
										grid.setPreferredColumns(configpanel.getPreferredColumns());
										grid.setDateFormat(configpanel.getDateFormat());
										ViewMode mode = configpanel.getIconSize();
										grid.setViewMode(mode);
										FeedbackHelper.showInfoToast("Grid configuration ok");
										getBrowser().refresh(target);
									}
								}
							}, "");
					}
					@Override
					public String getLabel() {
						return getLabelString("grid-settings");
					}
				}
			);

			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
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

			
			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							setRememberQuery(!isRememberQuery());
							KbeeUser user = ((KbeeUser) getSessionUser());
							if (user!=null) {
								user.getService(PreferencesService.class).setValue(getBrowser().getConsoleKey(), "rememberQuery", isRememberQuery()?"yes":"no");
								logger.debug("rememberQuery -> " + (isRememberQuery() ? "yes":"no"));
								Map<String, Serializable> map = new HashMap<String, Serializable>();
								map.put("rememberQuery", isRememberQuery()?"yes":"no");
								fire (new GeneralWicketEvent("rememberQuery", map));
								FeedbackHelper.showInfoToast(getLabel());
								target.add(GridConfigButton.this);
							}
							
							
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("remember-query", GridConfigButton.this, null).getString();
						}
						@Override
						public boolean isEnabled() {
								return true;
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						
						@Override
						public boolean isVisible() {
								return true;
						}
						
						
						@Override
						public boolean isIconVisible() {
							return isRememberQuery();
						}
						
						
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", GridConfigButton.this, null).getString();
						}
					};
				}
			});

			
			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
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


			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							
							if (panel!=null) {
								
								if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID) { 
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE);		
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);
								}
								
								FeedbackHelper.showInfoToast(getLabel());
							}
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("cell-borders", GridConfigButton.this, null).getString();
						}
						@Override
						public boolean isEnabled() {
								return true;
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						
						@Override
						public boolean isVisible() {
								return true;
						}
						
						
						@Override
						public boolean isIconVisible() {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {											
									if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID || 
										panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID ||
										panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK ||
										panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK)
										return true;
							}
							return false;
						}
						
						
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", GridConfigButton.this, null).getString();
						}
					};
				}
			});


			menu.addItem(id ->
	        	new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
	        		@Override
	        		public String getCssClass() {
	        			return "divider";
	        		}
	        	}	
			);
			
			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				private static final long serialVersionUID = 1L;
				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {
								if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID) { 
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);		
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE);
								}
								FeedbackHelper.showInfoToast(getLabel());
							}
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("background-odd", GridConfigButton.this, null).getString();
						}
						@Override
						public boolean isIconVisible() {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {											
									if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID || 
										panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID ||
										panel.getGridDisplayMode()==GridDisplayMode.COMPACT ||
										panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE
										)
										return true;
							}
							return false;
						}
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", GridConfigButton.this, null).getString();
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
					};
				}
			});

			menu.addItem(id ->
            	new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
            		@Override
	        		public String getCssClass() {
	        			return "divider";
            		}
            	}	
            );

			menu.addItem(new MenuItemFactory<ToolbarItem>() {
				@Override
				public AbstractMenuItemPanelV5<ToolbarItem> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							
							if (panel!=null) {
								
								if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID) { 
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_NO_BCK) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID);		
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
							}

							FeedbackHelper.showInfoToast(getLabel());
							
							
						}
						@Override
						public String getLabel() {
							return new StringResourceModel("compact-view", GridConfigButton.this, null).getString();
						}	
						@Override
						public boolean isEnabled() {
								return true;
						}
						@Override
						public boolean isVisible() {
								return true;
						}
						
						@Override
						public boolean isIconVisible() {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {
									if (	panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID || 
											panel.getGridDisplayMode()==GridDisplayMode.COMPACT ||
											panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK ||
											panel.getGridDisplayMode()==GridDisplayMode.COMPACT_NO_BCK)
										return true;
							}
							return false;
						}

						
						@Override 
						public String getWorkingLabel() {
							return new StringResourceModel("working", GridConfigButton.this, null).getString();
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}

					};
				}
			});
			

			menu.addItem(id ->
				new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {
								if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID) { 
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_GRID_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMPACT_NO_BCK) {
									GridConfigButton.this.onClick(target, GridDisplayMode.COMFORTABLE_GRID_NO_BCK);
								}
								
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID);		
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_GRID_NO_BCK);
								}
								else if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_NO_BCK) {
									//GridConfigButton.this.onClick(target, GridDisplayMode.COMPACT_NO_BCK);
								}
							}
							FeedbackHelper.showInfoToast(getLabel());
						}
						@Override
						public boolean isIconVisible() {
							GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
							if (panel!=null) {
									if (panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID || 
									    panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE  	 ||
									    panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_NO_BCK ||
									   	panel.getGridDisplayMode()==GridDisplayMode.COMFORTABLE_GRID_NO_BCK )
										return true;
							}
							return false;
						}
						@Override
						public String getLabel() {
							return getLabelString("comfortable-view");
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
					}
			);

			
			if (include_list_browser_switcher) {
			
				menu.addItem(id -> 
					new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
					}
				);
				
				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							onSetListView(target);
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
							return isListVisible();
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);
						
				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
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
							return isGridVisible();
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);

				if (include_tree_browser_switcher)
					menu.addItem(id ->
						new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								fire (new GeneralAjaxWicketEvent(target, "tree-browser"));
							}
							@Override
							public String getLabel() {
								return new StringResourceModel("tree", GridConfigButton.this, null).getString();
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
							@Override
							public boolean isIconVisible() {
								return (getBrowser().getBrowserType().equals("tree"));
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", GridConfigButton.this, null).getString();
							}
						}
					);
			}			
			
			menu.addItem(id -> 
				new SeparatorMenuItemPanelV5<ToolbarItem>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxCheckMenuItemPanelV5<ToolbarItem>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						((KbeeUser) getSessionUser()).getService(PreferencesService.class).deleteAllPreferences(getBrowser().getConsoleKey());
						getBrowser().reload(target);
						FeedbackHelper.showInfoToast(getLabel());
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("grid-reset-defaults", GridConfigButton.this, null).getString();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					public boolean isVisible() {
							return true;
					}
				}
			);
			
			add(new EmptyDialog("config-modal"));
	}
	
	public void onClick(AjaxRequestTarget target, GridDisplayMode mode) {
		GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
		if (panel!=null) {
			if (panel.getGridDisplayMode()!=mode) {
				panel.setGridDisplayMode(mode);
				target.add(getBrowser());
			}	
		}
	}

	public void setRememberQuery(boolean b) {
		isRememberQuery=b;
	}
	
	public boolean isRememberQuery() {
		return this.isRememberQuery;
	}
	
	public void setGridSwitcher(boolean include_list_browser_switcher) {
		 this.include_list_browser_switcher  = include_list_browser_switcher;
	}
	
	public void setTreeSwitcher(boolean include_tree_browser_switcher) {
		 this.include_tree_browser_switcher  = include_tree_browser_switcher;
	}
	
	public List<Integer> getItemRows() {
		return PAGE_SIZES;
	}
	
	public List<String> getDateFormats() {
		return DATE_MODES;
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isGridVisible() {
		return getBrowser().getBrowserType().equals("grid") || getBrowser().getBrowserType().equals("tree");
	}
	
	protected boolean isListVisible() {
		return getBrowser().getBrowserType().equals("list") || getBrowser().getBrowserType().equals("treelist");
	}
	
	protected void onSetListView(AjaxRequestTarget target) {
		fire (new GeneralAjaxWicketEvent(target, "list-browser")); 
	}
	
	protected void onSetGridView(AjaxRequestTarget target) {
		fire (new GeneralAjaxWicketEvent(target, "grid-browser")); 
	}
	
	private void addModal() {
		
		Modal modal = new Modal("config-modal", 
			"modal.grid-config.title", 
			new GridConfigPanel("body", (GridPanel<?>)getBrowser().getPanel(DataViewPanel.class)), Modal.Cancel, Modal.Save) {
			@Override
			protected IModel<String> getFooterCss() {
				return new Model<String>("modal-footer center");
			}
		};
		
		modal.setModalType(Modal.MODAL_CENTER);
		modal.setParameters(getBrowser().getConsoleDisplayName());
		addOrReplace(modal);
		
		is_created=true;
	}
}
package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.event.PortalAjaxShowPayloadEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowControllerEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowDeletedEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowArchivedEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowHierarchyEvent;
import kbee.web.portal6.panel.PortalIWebPanel;
import kbee.web.portal6.panel.PortalPanel;
import kbee.web.service.PortalPanelService;

public class PortalPageStructureToolbar extends PortalPanel<Page> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPageStructureToolbar.class.getName());

	private static final long serialVersionUID = 1L;
	
	//int edit_mode = PortalAjaxEvent.EDIT_MODE;
	
	int view_flat_hierarchy = PortalAjaxEvent.VIEW_HIERARCHY_YES;
	
	int archived_visible = PortalAjaxEvent.SHOW_ARCHIVED_YES;
	int deleted_visible = PortalAjaxEvent.SHOW_DELETED_NO;
	int controller_visible = PortalAjaxEvent.SHOW_CONTROLLER_YES;
	int portal_editor_disposition = PortalAjaxEvent.EDITOR_DISPOSITION_LEFT;
	
	
	int payload_visible = PortalAjaxEvent.SHOW_PAYLOAD_NO;
	
	int pin_editor = PortalAjaxEvent.PIN_EDITOR_YES;
	
	WebMarkupContainer i1 = new WebMarkupContainer("icon"); 
	WebMarkupContainer i2 = new WebMarkupContainer("icon");
	WebMarkupContainer i3 = new WebMarkupContainer("icon");
	WebMarkupContainer i4 = new WebMarkupContainer("icon");
	WebMarkupContainer i5 = new WebMarkupContainer("icon");
	
	
	String SON  = "far fa-toggle-on";
	String SOFF = "far fa-toggle-off";


	public PortalPageStructureToolbar(String id, IModel<Page> model, Map<String, String> parameters) {
		super(id, model, parameters);

		// for EDIT MODE
		this.view_flat_hierarchy	 		= getIntPreference(PortalAjaxEvent.STRUCTURE_VIEW, 		PortalAjaxEvent.VIEW_HIERARCHY_YES); 	
		this.archived_visible 				= getIntPreference(PortalAjaxEvent.ARCHIVED_VISIBLE,	PortalAjaxEvent.SHOW_ARCHIVED_NO); 
		this.deleted_visible 				= getIntPreference(PortalAjaxEvent.DELETED_VISIBLE, 	PortalAjaxEvent.SHOW_DELETED_NO);
		this.controller_visible 			= getIntPreference(PortalAjaxEvent.CONTROLLER_VISIBLE, 	PortalAjaxEvent.SHOW_CONTROLLER_YES);
		this.pin_editor  					= getIntPreference(PortalAjaxEvent.PIN_EDITOR, 			PortalAjaxEvent.PIN_EDITOR_YES);
		this.portal_editor_disposition 		= getIntPreference(PortalAjaxEvent.EDITOR_DISPOSITION, 	PortalAjaxEvent.EDITOR_DISPOSITION_LEFT);
		this.payload_visible 				= getIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, 	PortalAjaxEvent.SHOW_PAYLOAD_NO);
		
		// this.edit_mode						= getIntPreference("edit-mode", PortalAjaxEvent.EDIT_MODE);
	}

	public void setPreference(String key, String value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("portal-page-"+getModel().getObject().getId().toString(), key, value);
	}
	
	public void setIntPreference(String key, int value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue("portal-page-"+getModel().getObject().getId().toString(), key, value);
	}
	
	
	public String getPreference(String key, String defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("portal-page-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

	public int getIntPreference(String key, int defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue("portal-page-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

	
	Boolean yes = Boolean.valueOf(true);
	public Boolean getYes() {
		return yes;
	}
	
	public void setYes(Boolean b) {
		yes=b;
	}
	
	/**
	 *
	 * 
	 * 					reset=Reset Default Settings
					editor-left-right=Editor Left/Right
					pin=Pin/Unpin info panel



	 * @return
	 */
protected List<MenuItemFactory<Page>> getMenuItems() {
		
		List<MenuItemFactory<Page>> list = new ArrayList<MenuItemFactory<Page>>();
		
		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 10;
			}

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							if (payload_visible ==PortalAjaxEvent.SHOW_PAYLOAD_NO)
								payload_visible =PortalAjaxEvent.SHOW_PAYLOAD_YES;
							else
								payload_visible =PortalAjaxEvent.SHOW_PAYLOAD_NO;

							setIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, payload_visible );
							
							fire (new PortalAjaxShowPayloadEvent<Page>(target, getModel(), payload_visible ));
							
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<Page>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return  getLabelString("view-payload");
					}
					
					
							
							
							
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});
		

		
		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 10;
			}

			
			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							if (portal_editor_disposition==PortalAjaxEvent.EDITOR_DISPOSITION_LEFT)
								portal_editor_disposition=PortalAjaxEvent.EDITOR_DISPOSITION_RIGHT;
							else
								portal_editor_disposition=PortalAjaxEditorDispositionEvent.EDITOR_DISPOSITION_LEFT;
							fire (new PortalAjaxEditorDispositionEvent<Page>(target, getModel(), portal_editor_disposition));
							
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<Page>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getLabelString("editor-left-right");
					}
					
					
					
					
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});

		
		
		
		
		
		
		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 10;
			}

			
			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							if (portal_editor_disposition==PortalAjaxEvent.EDITOR_DISPOSITION_LEFT)
								portal_editor_disposition=PortalAjaxEvent.EDITOR_DISPOSITION_RIGHT;
							else
								portal_editor_disposition=PortalAjaxEditorDispositionEvent.EDITOR_DISPOSITION_LEFT;
							fire (new PortalAjaxEditorDispositionEvent<Page>(target, getModel(), portal_editor_disposition));
							
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<Page>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getLabelString("editor-left-right");
					}
					
					
					
					
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});

		
		
		
		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 100;
			}

			
			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							
							setIntPreference(PortalAjaxEvent.STRUCTURE_VIEW, PortalAjaxEvent.VIEW_HIERARCHY_YES); 	
							setIntPreference(PortalAjaxEvent.ARCHIVED_VISIBLE,PortalAjaxEvent.SHOW_ARCHIVED_NO); 
							setIntPreference(PortalAjaxEvent.DELETED_VISIBLE, PortalAjaxEvent.SHOW_DELETED_NO);
							setIntPreference(PortalAjaxEvent.CONTROLLER_VISIBLE, PortalAjaxEvent.SHOW_CONTROLLER_YES);
							setIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, PortalAjaxEvent.SHOW_PAYLOAD_NO);
							
							fire (new PortalAjaxRefreshEvent<Page>(target, getModel()));
							
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<Page>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return  getLabelString("reset");
					}
					
					
					//"Reset Default Settings";

					
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});
		
		
		
		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 10;
			}

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new AjaxMenuItemPanelV5<Page>(id, getModel()) {
					private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
						try {
							if (payload_visible ==PortalAjaxEvent.SHOW_PAYLOAD_NO)
								payload_visible =PortalAjaxEvent.SHOW_PAYLOAD_YES;
							else
								payload_visible =PortalAjaxEvent.SHOW_PAYLOAD_NO;

							setIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, payload_visible );
							
							fire (new PortalAjaxShowPayloadEvent<Page>(target, getModel(), payload_visible ));
							
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<Page>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return  getLabelString("view-payload");
					}
					
							
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});
		

		
		 
		

		
		list.add(new MenuItemFactory<Page>() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getOrder() {
				return 10;
			}

			@Override
			public AbstractMenuItemPanelV5<Page> getItem(String id) {
				return new LinkMenuItemPanel<Page>(id) {
					private static final long serialVersionUID = 1L;
						public void onClick() {
							try {
 								WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject().getSite());
 								setResponsePage(page);
 							} catch (Exception e) {
 								logger.error(e);
 								setResponsePage(new ApplicationErrorPage<>(e));
 							}
					}
					

 					public String getTarget() {
 							return "_blank";
 					}
 						
					@Override 
					public String getLabel() {
						return  getLabelString("open-portal");
					}
				};
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		return list;
		
}

	
	
	protected Panel getMenu() {
		
		ContextMenuPanel<Page> menu = new ContextMenuPanel<Page>(getModel());
						
		menu.setOutputMarkupId(true);
		
		List<MenuItemFactory<Page>> list = getMenuItems();
		
		
		
		/**
		list.sort(new Comparator<MenuItemFactory<T>>() {

			@Override
			public int compare(MenuItemFactory<T> o1, MenuItemFactory<T> o2) {
				if (o1.getOrder()<o2.getOrder())
					return -1;
				
				if (o1.getOrder()>o2.getOrder())
					return 1;
				
				return 0;
			}
		});
		**/
		
		for (MenuItemFactory<Page> f: list)
			menu.addItem(f);
		return menu;
		
	}

	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		AjaxCheckMenuItemPanelV5<Page> tp = new AjaxCheckMenuItemPanelV5<Page>("show-two-panels" , getModel()) {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				
				try {
					
					if (pin_editor==PortalAjaxEvent.PIN_EDITOR_NO)
						pin_editor=PortalAjaxEvent.PIN_EDITOR_YES;
					else
						pin_editor=PortalAjaxEvent.PIN_EDITOR_NO;
					
					setIntPreference(PortalAjaxEvent.PIN_EDITOR, pin_editor);
					
					fire (new PortalAjaxPinEditorEvent<Page>(target, getModel(), pin_editor));
				} 
				catch (Exception e) {
					logger.error(e);
					fire(new ErrorEvent<Page>(target, getModel(), e));
					
				}
			}
			@Override 
			public String getLabel() {
				return "Info";
			}
			
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			
			
		}; 
		
		add(tp);
		
		
		add(getMenu());

		i1.add(new AttributeModifier("class", (view_flat_hierarchy==PortalAjaxEvent.VIEW_HIERARCHY_YES) ? SON : SOFF));
		i2.add(new AttributeModifier("class", (archived_visible == PortalAjaxEvent.SHOW_ARCHIVED_YES) ? SON : SOFF));
		i3.add(new AttributeModifier("class", (deleted_visible == PortalAjaxEvent.SHOW_DELETED_YES) ? SON : SOFF));
		i4.add(new AttributeModifier("class", (controller_visible==PortalAjaxEvent.SHOW_CONTROLLER_YES) ? SON : SOFF));
		i5.add(new AttributeModifier("class", (payload_visible==PortalAjaxEvent.SHOW_PAYLOAD_YES) ? SON : SOFF));
		
		 
		//BooleanSwitchField yes=new BooleanSwitchField("yes", new PropertyModel<Boolean>(this, "yes"));
		//add(yes);
		
		
		AjaxLink<Page> refresh = new AjaxLink<Page>("refresh", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire (new PortalAjaxRefreshEvent<Page>(target, getModel()));
			}
		};
		add(refresh);
		
		
		
		AjaxLink<Page> flat = new AjaxLink<Page>("flat-view", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (view_flat_hierarchy==PortalAjaxEvent.VIEW_HIERARCHY_YES)
					view_flat_hierarchy=PortalAjaxEvent.VIEW_HIERARCHY_NO;
				else
					view_flat_hierarchy=PortalAjaxEvent.VIEW_HIERARCHY_YES;
				
				setIntPreference(PortalAjaxEvent.STRUCTURE_VIEW, view_flat_hierarchy);
				
				i1.add(new AttributeModifier("class", (view_flat_hierarchy==PortalAjaxEvent.VIEW_HIERARCHY_YES) ? SON : SOFF));
				fire (new PortalAjaxStructureShowHierarchyEvent<Page>(target, getModel(),  view_flat_hierarchy));
			}
		};
		flat.add(i1);
		add(flat);
		
		
		
		
		AjaxLink<Page> sp = new AjaxLink<Page>("show-preview", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
			
				if (payload_visible==PortalAjaxEvent.SHOW_PAYLOAD_NO)
					payload_visible=PortalAjaxEvent.SHOW_PAYLOAD_YES;
				else
					payload_visible=PortalAjaxEvent.SHOW_PAYLOAD_NO;
				
				setIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, payload_visible);
				
				i5.add(new AttributeModifier("class", (payload_visible==PortalAjaxEvent.SHOW_PAYLOAD_YES) ? SON : SOFF));
				
				fire (new PortalAjaxShowPayloadEvent<Page>(target, getModel(), payload_visible ));
				
			}
		};
		sp.add(i5);
		add(sp);
				
		 
		
		
		AjaxLink<Page> arch= new AjaxLink<Page>("show-archived", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				if (archived_visible == PortalAjaxEvent.SHOW_ARCHIVED_YES)
					archived_visible = PortalAjaxEvent.SHOW_ARCHIVED_NO;
				else
					archived_visible = PortalAjaxEvent.SHOW_ARCHIVED_YES;
				
				setIntPreference(PortalAjaxEvent.ARCHIVED_VISIBLE, archived_visible);
				
				i2.add(new AttributeModifier("class", (archived_visible == PortalAjaxEvent.SHOW_ARCHIVED_YES) ? SON : SOFF));
				fire (new PortalAjaxStructureShowArchivedEvent<Page>(target, getModel(), archived_visible));
			}
		};
		
		arch.add(i2);
		add(arch);
		
		
		
		AjaxLink<Page> sd= new AjaxLink<Page>("show-deleted", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				if (deleted_visible == PortalAjaxEvent.SHOW_DELETED_NO)
					deleted_visible = PortalAjaxEvent.SHOW_DELETED_YES;
				else
					deleted_visible = PortalAjaxEvent.SHOW_DELETED_NO;
													
				setIntPreference(PortalAjaxEvent.DELETED_VISIBLE, deleted_visible);
				
				i3.add(new AttributeModifier("class", (deleted_visible == PortalAjaxEvent.SHOW_DELETED_YES) ? SON : SOFF));
				fire (new PortalAjaxStructureShowDeletedEvent<Page>(target, getModel(), deleted_visible));
			}
		};
		
		sd.add(i3);
		add(sd);
		
		
		
		
						
		AjaxLink<Page> release = new AjaxLink<Page>("show-controller", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				if (controller_visible == PortalAjaxEvent.SHOW_CONTROLLER_YES)
					controller_visible = PortalAjaxEvent.SHOW_CONTROLLER_NO;
				else
					controller_visible=PortalAjaxEvent.SHOW_CONTROLLER_YES;
				
				setIntPreference(PortalAjaxEvent.CONTROLLER_VISIBLE, controller_visible);
				
				i4.add(new AttributeModifier("class", (controller_visible==PortalAjaxEvent.SHOW_CONTROLLER_YES) ? SON : SOFF));
				
				fire (new PortalAjaxStructureShowControllerEvent<Page>(target, getModel(), controller_visible));

			}
		};
		release.add(i4);
		
		
		
		add(flat);
		add(arch);
		add(release);
		
	}
	
		
	

}

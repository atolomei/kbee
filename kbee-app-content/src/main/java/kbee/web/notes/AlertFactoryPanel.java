package kbee.web.notes;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.notes.Billboard;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;


public abstract class AlertFactoryPanel extends Panel {
			
	private static final long serialVersionUID = 1L;

	final boolean is_admin   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 


	public AlertFactoryPanel() {
		this("factory");
	}
		
	public AlertFactoryPanel(String id) {
		super(id);
		
		ContextMenuPanel<Billboard> menu = new ContextMenuPanel<Billboard>(null);
		
		WebMarkupContainer newm = new WebMarkupContainer ("new-multiple-button");
		newm.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
		
		menu.addItem(new MenuItemFactory<Billboard>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new MenuItemPanelV5<Billboard>(id) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						onCreate("alert");
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("alert", AlertFactoryPanel.this, null).getObject();
					}
					//@Override
					//public String getTarget() {
					//	return "_blank";
					//}
					
					@Override
					public boolean isEnabled() {
						return false;
						// return is_root || is_admin;
					}
				};
			}
		});
		

		menu.addItem(new MenuItemFactory<Billboard>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Billboard> getItem(String id) {
				return new MenuItemPanelV5<Billboard>(id) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						onCreate("billboard");
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("billboard", AlertFactoryPanel.this, null).getObject();
					}
					@Override
					public boolean isEnabled() {
						return is_root || is_admin;
					}
					
					@Override
					public boolean getOpener() {
						return true;
					}
				};
			}
		});

		
				
		add(menu);
	}

	protected abstract void onCreate(String type);


}

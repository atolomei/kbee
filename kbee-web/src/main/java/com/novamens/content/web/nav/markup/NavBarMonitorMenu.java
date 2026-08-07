 package com.novamens.content.web.nav.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

/** -------------------------------------------------------------------------
 *
 */
@SuppressWarnings("serial")
public class NavBarMonitorMenu extends Panel {

	private static final long serialVersionUID = 1L;
								
	final boolean role_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model 	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_root		= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_support 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
										
	public NavBarMonitorMenu(String id) {
		super(id);
		
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
						return (new StringResourceModel("monitor.tool.take", NavBarMonitorMenu.this, null)).getObject();
					}
					@Override
					public boolean isEnabled() {
						return role_admin || role_support;
					}
				};
			}
		});
		
		add(menu);
	}
}

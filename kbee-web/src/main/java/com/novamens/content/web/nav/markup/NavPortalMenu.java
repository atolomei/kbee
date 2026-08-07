package com.novamens.content.web.nav.markup;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;


/**
 * User Favorites
 * 
 */
public class NavPortalMenu extends Panel {
			
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(NavPortalMenu.class.getName());
	
	public NavPortalMenu(String id) {
		super(id);

		try {
			/*
			/** DirectoryFavoritesHeaderPanel
			 */ 
			Panel panel = (Panel) ServiceLocator.getService(BeansService.class).getBean("user-directory", "menu");
			if (panel!=null) { 
				panel.add(new AttributeModifier("style", "padding: 0;"));
				add(panel);
			}
			else {
				logger.error("Spring error user-directory is null.");
				add(new Panel("menu") {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
					return false;
					}
				});
			}
			
			
		
		} catch (Exception e) {
			
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getMessage());
			add(new Panel("menu") {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
					return false;
					}
				});
		}
    }

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}



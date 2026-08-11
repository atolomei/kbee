package kbee.web.dataset;

import java.io.File;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.console.BaseBrowser;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;


public abstract class DataSetMemberTools<T extends DataSetMember> extends ToolbarItem {
	private static final long serialVersionUID = 1L;
			
	public static final int MAX_FILES_EXPORT_ROOT = 160000;
	public static final int MAX_FILES_EXPORT_ADMIN = 60000;
	public static final int MAX_FILES_EXPORT_REGULAR_USER = 2000;
	public static final int MAX_GRID_EXPORT_ROOT = 3000000;
	public static final int MAX_GRID_EXPORT_ADMIN = 2000000;
	public static final int MAX_GRID_EXPORT_REGULAR_USER = 200000;

	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public DataSetMemberTools(BaseBrowser<T> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		UserProfile profile = getContentDao().findUserProfileByUser(getSessionUser());
		
		final boolean is_download= profile.isSendFilesEmail();

		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getLabel() {
						return DataSetMemberTools.this.getLabel("tools.grid.export.xls").getObject();
					}
					@Override
					public boolean isEnabled() {
						if (is_root || is_domain_admin)
							return true;
						if (is_support)
							return false;
						return is_download;
					}
					@Override
					protected File getFile() {
						return getGridExport("xls");
					}
					
					
				};
			}
		});

		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getLabel() {
						return DataSetMemberTools.this.getLabel("tools.grid.export.csv").getObject();
					}
					@Override
					public boolean isEnabled() {
						if (is_root || is_domain_admin)
							return true;
						if (is_support)
							return false;
						return is_download;
					}
					@Override
					protected File getFile() {
						return getGridExport("csv");
					}
				};
			}
		});
		
		
		/*
		menu.addItem(new MenuItemFactory<Void>() {
			private static final long serialVersionUID = 1L;
			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new SeparatorMenuItemPanel<Void>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  isFilesExport();
					}
				};
			}
		});
		*/

		
		add(menu);
	}
		
	
	/**
	 * @return
	 */
	protected int getMaxGridExport() {
		if (is_root)			return MAX_GRID_EXPORT_ROOT;
		if (is_domain_admin)	return MAX_GRID_EXPORT_ADMIN;
		if (is_support)			return 0;
		return MAX_GRID_EXPORT_REGULAR_USER;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	// protected abstract void onGridExport(AjaxRequestTarget target, String format);
	
	protected abstract File getGridExport(String format);
	

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isFilesExport() {
		return true;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}

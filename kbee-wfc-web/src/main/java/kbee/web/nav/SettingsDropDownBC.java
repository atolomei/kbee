package kbee.web.nav;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

/**
 * DataSet Values
 * General Settings
 * Email Templates
 * Libraries
 * Information Model
 */
@SuppressWarnings("serial")
public class SettingsDropDownBC extends DropDownMenuBC<Void> {
	
	private static final long serialVersionUID = 1L;
		
	final boolean is_root 				= ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin 			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	
	public SettingsDropDownBC() {
	
		addElement(new SettingsBC());
		
		addElement(new BCElement("bc.dataset.members") {
			public void onClick() {
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-home-page"));
			}
		});
		
		if (role_admin || is_root || role_support) {
			addElement( new BCElement("bc.informationmodel") {
				public void onClick() {
					setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-home-page"));
				}
			});
		}
		
		addElement(new GeneralSettingsBC());
		
		//addElement(new EmailNotificationsBC());
		//addElement(new BillboardBC()); // Billboard and Manual Alerts
		
		addElement(new EmailTemplatesBC());
		addElement(new LibrariesBC());
		addElement(new SourcesBC());
		addElement(new FacetsBC());

		
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
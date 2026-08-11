package kbee.web.page;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.web.security.login.LoginSimplePage;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.application.PageBeanResolver;
import kbee.web.dashboard.DashboardFactoryHomePage;
import kbee.web.dashboard.DashboardHomePage;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.service.PortalPanelService;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;
	
	protected WebPage getLoginPage(PageParameters par) {
		return (new PageBeanResolver(
			"login-page", 
			LoginSimplePage.class)).getPage();
	}

	
	@Override
	protected void onAfterRender()	{
		super.onAfterRender();
		
		if (getSessionUser()==null) {
			getRequestCycle().setResponsePage( getLoginPage(null));
			return;
		}
		
		if (!isUserEnabled()) {
			PageParameters par = new PageParameters();
			par.add("login_error", "5");
			getRequestCycle().setResponsePage(getLoginPage(par));
			return;
		}
		
		// ------------------------------------------
		// Domain kbee 
		//
		if (isDomainKbee()) {
			getRequestCycle().setResponsePage(new DashboardFactoryHomePage());
			return;
		}
		
		// ------------------------------------------
		// Regular Domains 
		//
		if (!isDomainEnabled()) {
			if ((isDomainDraft() || isDomainArchived()) && isRoot()) {
				getRequestCycle().setResponsePage( new DashboardHomePage());
			}
			else {
					PageParameters par = new PageParameters();
					par.add("login_error", "2"); 
					getRequestCycle().setResponsePage(getLoginPage(par));
			}
		}
		
		getRequestCycle().setResponsePage(ServiceLocator.getService(PortalPanelService.class).getStartPage(getUserProfile()));
	}
	
	/**
	 * @param par
	 * @return
	 */
	
	

	private boolean isDomainArchived() {
			if (getPerson().getDomain().getState()==ObjectState.ARCHIVED) 
				return true;
			return false;
	}

		
	private boolean isDomainDraft() {
		if (getPerson().getDomain().getState()==ObjectState.DRAFT) 
			return true;
		return false;
	}
	
	private boolean isDomainEnabled() {
		if (getPerson().getDomain().getState()==ObjectState.ENABLED) 
				return true;
		return false;
	}
	
	private boolean isUserEnabled() {
		User u= getSessionUser();
		if (u==null)
			return false;
		if (u.getUserName().equals("root@kbee"))
			return true;
		return u.isEnabled();
	}
	
	
	private boolean isRoot() {
		User user = getSessionUser();
		return (user!=null && user.getUserName().startsWith("root@"));
	}
		
	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			 
			return false;
		}
	}
	
	private Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			 
			return null;
		}
	}
	
	
	private UserProfile getUserProfile() {
		return getContentDao().findUserProfileByUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected Page getPage( String pagekey ) {
		return ServiceLocator.getService(ApplicationSiteMapService.class).getPage(pagekey);
	}
}

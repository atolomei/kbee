package kbee.web.notes;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.HomeBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

public class UserNotesPage extends ApplicationPage<Person> {
			
	private static final long serialVersionUID = 1L;
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserNotesPage.class.getName());
	
	IModel<Person> model;
	
	public UserNotesPage(PageParameters parameters) {
		try {
			User user = getUser(parameters);
			if (user!=null && user.getId().equals(getSessionUser().getId())) {
				Person person = getContentDao().findUserProfileByUser(user).getPerson();
				setModel(new ObjectModel<Person>(person));
				addComponents();
			}
			else throw new KbeeRuntimeException("User is null or not session user");
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace(new InvisiblePanel("navigation"));
			addOrReplace(new InvisiblePanel("user-notes"));
			addOrReplace(new InvisiblePanel("page-error-dialog"));
		}
	}

	
	public UserNotesPage() {
		try {
			User user = getSessionUser();
			Person person = getContentDao().findUserProfileByUser(user).getPerson();
			setModel(new ObjectModel<Person>(person));
			getPageParameters().set("id", user.getId().toString());
			addComponents();
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace(new InvisiblePanel("navigation"));
			addOrReplace(new InvisiblePanel("user-notes"));
			addOrReplace(new InvisiblePanel("page-error-dialog"));
		}
	}

	
	public UserNotesPage(IModel<Person> model) {	
		setModel(model);
		getPageParameters().set("id", model.getObject().getProfile(UserProfile.class).getUser().getId().toString());
		addComponents();
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("page-confirmation-dialog");
	}
	

	@Override
	protected void addModals() 	 {
		addOrReplace(new ConfirmationDialog("page-confirmation-dialog"));
		addOrReplace(new InvisiblePanel("page-error-dialog"));
	}


	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}


	@Override
	public boolean hasPermissions() {
		return true;
	}
	
	public IModel<Person> getModel() {
		return model;
	}
	

	private void addComponents() {

		setPageTitle(new StringResourceModel("page-title", this, null));
		
		if (hasPermissions()) {
				
			
			MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
			bc.addElement( new HomeBC());
			bc.addElement( new AccountDropDownBC());			
			bc.addElement(new BCElement( new StringResourceModel("page-title", this, null)));
			add(bc);

			
			
			// setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setTopNavigation(getMainTopbar());
			
			// setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
			setMenu(getMainLaternalMenu(getApplicationMenuSection().getKey()));
			
			
				add( new UserNotesPanel("user-notes", false)  {
					private static final long serialVersionUID = 1L;
					protected ConfirmationDialog getConfirmationDialog() {
						return UserNotesPage.this.getConfirmationDialog();
					}
				});
		} else {
			add(new ErrorNotAuthorizedPanel<>("user-notes"));
			add(new InvisiblePanel("breadcrumb"));
		}
	}
	
	/**
	 * @param parameters
	 * @return
	 */
	private User getUser(PageParameters parameters) {
		if (parameters.get("id")!=null && !"".equals(parameters.get("id").toString())) {
			try {
				String id = parameters.get("id").toString();
				return (User) ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(id));
			} catch (Exception e) {
				logger.error(e);
				return null;
			}
		}	
		return null;
	}

	

	
}

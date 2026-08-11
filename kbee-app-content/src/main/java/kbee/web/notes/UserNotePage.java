package kbee.web.notes;

import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.notes.UserNote;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

public class UserNotePage extends ApplicationPage<UserNote> {
			
private static final long serialVersionUID = 1L;

final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	  
	public UserNotePage() {
		// String title = new StringResourceModel("new", this, null).getObject();
		// setModel(new Model<NewWorkNoteData>(new NewWorkNoteData(title)));
	}
	
	protected boolean hasPermissions() {
		return true;
		// return role_admin;
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setPageTitle(new StringResourceModel("new", UserNotePage.this, null));
		setPageDescription(getPageTitle());

		if (hasPermissions()) {
			setTopNavigation(super.getMainTopbar());
			setMenu(getMainLaternalMenu(getApplicationMenuSection().getKey()));
		}
	}
}
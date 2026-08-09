package kbee.web.notification;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

@SuppressWarnings("serial")
public class UserNotificationsPage extends ApplicationPage<Person> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(UserNotificationsPage.class.getName());
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			UserNotificationsPage.this.refresh(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refresh() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refresh"));
		}
	}

	IModel<Person> model;
	
	public UserNotificationsPage() {
		try {
			User user = getSessionUser();
			Person person = getContentDao().findUserProfileByUser(user).getPerson();
			setModel(new ObjectModel<Person>(person));
			addComponents();
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace(new InvisiblePanel("navigation"));
			addOrReplace(new InvisiblePanel("user-notification"));
			addOrReplace(new InvisiblePanel("page-error-dialog"));
		}
	}

	public UserNotificationsPage(IModel<Person> model) {	
		setModel(model);
		addComponents();
	}
	
	public void refresh(AjaxRequestTarget target) {
		target.add(get("user-notifications")); 
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("page-confirmation-dialog");
	}

	@Override
	protected void addModals() 	 {
		add(new ConfirmationDialog("page-confirmation-dialog"));
		add(new InvisiblePanel("page-error-dialog"));
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
		add(new RefreshBehavior());
		setPageTitle(new StringResourceModel("page-title", this, null));
		if (hasPermissions()) {
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());  
			add(new UserNotificationsPanel("user-notifications")  {
				protected ConfirmationDialog getConfirmationDialog() {
					return UserNotificationsPage.this.getConfirmationDialog();
				}
			});
			MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
			bc.addElement( new HomeBC());
			bc.addElement( new AccountDropDownBC());			
			bc.addElement(new BCElement( new StringResourceModel("page-title", this, null)));
			add(bc);
		} 
		else {
			
			add(new ErrorNotAuthorizedPanel<>("user-notifications"));
			add(new InvisiblePanel("breadcrumb"));
		}
	}
}

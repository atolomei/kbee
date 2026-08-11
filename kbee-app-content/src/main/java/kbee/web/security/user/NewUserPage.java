package kbee.web.security.user;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.nav.UsersBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.ConsoleObjectPage;
import kbee.web.page.PageContentHeaderPanel;


@SuppressWarnings("serial")
public class NewUserPage extends ConsoleObjectPage<NewUserData> {
	
	final boolean is_support		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	
	
	
	private static final long serialVersionUID = 1L;

	
	private IModel<User> base_user;
	
	public  IModel<User> getBaseUser() {
		return base_user;
	}
	
	public  void  setBaseUser(IModel<User> b) {
		this.base_user=b;
	}

	public NewUserPage(IModel<User> b) {
		setBaseUser(b);
		setModel(new Model<NewUserData>(new NewUserData()));
		addComponents(getModel());
	}
	
	public NewUserPage() {
		setModel(new Model<NewUserData>(new NewUserData()));
		addComponents(getModel());
	}
	
	
	public NewUserPage(IModel<NewUserData> model, IModel<User> b) {
		super(model);
		setBaseUser(b);
		addComponents(model);
	}
	
	
	public void onEdit(IModel<Person> model) {
		
	}
	
	    
	protected boolean hasPermissions() {
		return is_domain_admin || is_root || is_support || is_security; 
	}
	
	private void addComponents(IModel<NewUserData> model) {
			
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		if (hasPermissions()) {

			setPageTitle(new StringResourceModel("new-user", NewUserPage.this, null));
			setPageDescription(getPageTitle());

			PageContentHeaderPanel<NewUserData> panel=new PageContentHeaderPanel<NewUserData>(null);
			panel.setTitle( new StringResourceModel("new", NewUserPage.this, null));
			setSearchPanel(false);
			setClearAllSearch(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement( new SecurityDropDownMenuBC());
			bc.addElement( new UsersBC());
			bc.addElement(new BCElement(new StringResourceModel("new", NewUserPage.this, null)));
			panel.setBreadcrumbPanel(bc);
			
			add(new NewUserEditor("editor", model, this.getBaseUser()) {
				@Override
				public void onEdit(IModel<Person> model) {
					Page page = new UserPage(model);
					setResponsePage(page);
				}
				@Override
				public void onCancel(AjaxRequestTarget target) {
					setResponsePage( new UsersPage());
				}
			});
		}
		else {
			add(new ErrorNotAuthorizedPanel<>("editor"));
		}
	}
}

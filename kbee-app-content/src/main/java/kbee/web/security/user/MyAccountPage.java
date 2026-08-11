package kbee.web.security.user;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.HomeBC;
import kbee.web.notification.AccountDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.user.UserAvatarPanel;


public class MyAccountPage extends ApplicationPage<Person> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyAccountPage.class.getName());
	
	boolean gotoLast = false;
	
	public MyAccountPage() {
		this (false);
	}
	
	public MyAccountPage(PageParameters parameters) {
		this (false);
	}
	
	public MyAccountPage(final boolean goto_last_selected_tab) {

		setModel(getUserModel());
		gotoLast=goto_last_selected_tab;
	} 
	

	public IModel<Person> getUserModel() {
		try {
			UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			Person person = (Person)profile.getEntity();
			IModel<Person> model = new ObjectModel<Person>(person);
			return model;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getModel()==null || getModel().getObject()==null) {
			setResponsePage( new ApplicationErrorPage<>(new Model<String>("User is null")));			
			return;
		}
		  
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		setPageTitle(new Model<String>(getModel().getObject().getFirstLastName()));
		setPageDescription(new Model<String>(getModel().getObject().getFirstLastName()));
		add(getMainPanel(gotoLast));

		PageContentHeaderPanel<Person> panel=new PageContentHeaderPanel<Person>(getModel());
		
		panel.setTitle(getModel().getObject().getFirstLastName());
		User user=getModel().getObject().getProfile(UserProfile.class).getUser();
		panel.setAvatarPanel(new UserAvatarPanel("avatar", new ObjectModel<User>(user)));
		panel.setSubLine(new Model<String>(getModel().getObject().getWorkPosition()));

		
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		
		setPageContentHeader(panel);
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}
	
	protected Panel getMainPanel(final boolean goto_last_selected_tab) {
		return new UserMainPanel("editor", getModel(), true, false, goto_last_selected_tab);
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<Void>();
		bc.addElement( new HomeBC());
		bc.addElement( new AccountDropDownBC());			
		bc.addElement(new BCElement("bc.myaccount"));
		return bc;
	}
	
	
	protected IModel<DataSetMember> getMemberModel(IModel<Person> model) { 
		
		DataSetMember member = null;
		
		if (model.getObject() instanceof PersonMember) {
			member = (DataSetMember)getModel().getObject();
		}
			
		List<DataSetMember> members = getContentDao().findMembersByEntity(getModel().getObject());
				
		if (members!=null &&  !members.isEmpty()) {
			member =  (DataSetMember) getContentDao().reload(members.get(0));
		}
				
		return member!=null? new ObjectModel<DataSetMember>(member) : null;
	}

}

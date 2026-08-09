package kbee.web.searcher.page;



import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.page.ApplicationMenuSection;


public class SearcherUserNotesPage extends AbstractSearcherPage<Person> {
	
private static final long serialVersionUID = 1L;
		
	IModel<Person> model;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherUserNotesPage.class.getName());
	
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}

	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	
	
	public SearcherUserNotesPage(PageParameters parameters) {
		try {
			User user = getSessionUser();
			com.novamens.content.entity.Person person = getContentDao().findUserProfileByUser(user).getPerson();
			setModel(new ObjectModel<Person>(person));
			Site site =  getSite(parameters);
			if (site!=null) 
				setSiteModel(new ObjectModel<Site>(site));
			addComponents();
		} 
		catch (Exception e) {
			add(new InvisiblePanel("navigation"));
			add(new InvisiblePanel("user-notes"));
			logger.error(e);
		}
	}

	public SearcherUserNotesPage(IModel<Person> model, IModel<Site> siteModel) {	
		setModel(model);
		setSiteModel(siteModel);
		addComponents();
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("page-confirmation-dialog");
	}
	

	@Override
	protected void addModals() 	 {
		addOrReplace(new ConfirmationDialog("page-confirmation-dialog"));
	}


	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SEARCHER;
	}


	@Override
	public boolean hasPermissions() {
		return true;
	}
	
	
	@Override
	protected boolean isSearchForm() {
		return true;
	}
	
	
	public IModel<Person> getModel() {
		return model;
	}
	
	private void addComponents() {
		setPageTitle(new StringResourceModel("page", this, null));
		getPageParameters().set("siteurl", getSiteModel()!=null?getSiteModel().getObject().getUrl().toString():"");
	}
	

	

	
}

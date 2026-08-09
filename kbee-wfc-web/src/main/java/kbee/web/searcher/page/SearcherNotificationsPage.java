package kbee.web.searcher.page;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.Notification;
// import com.novamens.content.web.notification.UserNotificationsPanel;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.page.ApplicationMenuSection;

@SuppressWarnings("serial")
public class SearcherNotificationsPage extends AbstractSearcherPage<Person> {
	private static final long serialVersionUID = 1L;
	
	IModel<Person> model;
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}

	@Override
	protected boolean isExplorerOn() {
		return false;
	}
	
	public SearcherNotificationsPage() {
			this(new PageParameters());
	}
	
	public SearcherNotificationsPage(PageParameters pageparameters) {
		try {
			User user = getSessionUser();
			com.novamens.content.entity.Person person = getContentDao().findUserProfileByUser(user).getPerson();
			setModel(new ObjectModel<Person>(person));
			Site site = getSite(pageparameters);
			
			if (site!=null) {
				setSiteModel(new ObjectModel<Site>(site));
				
				if (site.getDomain().getId().equals(getDomain().getId()))
					addComponents();
				else {
					add(new InvisiblePanel("navigation"));
					add(new InvisiblePanel("user-notes"));
				}
			}
		} 
		catch (Exception e) {
			add(new InvisiblePanel("navigation"));
			add(new InvisiblePanel("user-notes"));
		}
	}
	
	
	public SearcherNotificationsPage(IModel<Site> siteModel) {
		try {
			User user = getSessionUser();
			com.novamens.content.entity.Person person = getContentDao().findUserProfileByUser(user).getPerson();
			setModel(new ObjectModel<Person>(person));
			setSiteModel(siteModel);
			getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
			addComponents();
		} 
		catch (Exception e) {
			add(new InvisiblePanel("navigation"));
			add(new InvisiblePanel("user-notes"));
		}
	}

	public SearcherNotificationsPage(IModel<Person> model, IModel<Site> siteModel) {	
		setModel(model);
		setSiteModel(siteModel);
		getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
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
		if (hasPermissions()) {
					// TODO AT
					throw new KbeeRuntimeException("new UserNotificationsPanel(");
				/**
				add( new UserNotificationsPanel("user-notifications")  {
					@Override
					protected void onTitleClick(IModel<Notification> model) {
						try {
						if (model.getObject() instanceof ContentNotification) {
								Content content = ((ContentNotification) model.getObject()).getContent();
								if (content !=null && content.getState()==ObjectState.ENABLED) {
									Site site = getSiteModel().getObject();
									String url = ServiceLocator.getService(PortalUrlService.class).getDetailUrl(content, site);
									setResponsePage(new RedirectPage(url));
								}
								else {
									getLabel("");
									setResponsePage(new ErrorPage<Content>(getLabel("not-found-message", model.getObject().getTitle()), new Model<String>("")));
									//setResponsePage(new ErrorPage<Content>(new Model<String>(model.getObject().getTitle() + " " + "not-in-library"), new Model<String>("")));
								}	
							}
							else {										
								setResponsePage(new ErrorPage<Content>( new Model<String>("ContentPublishNotification"), new Model<String>("not ContentPublishNotification")));
							}	
						} 
						catch (Exception e) {											
							setResponsePage(new ErrorPage<Content>( new Model<String>(e.getClass().getName()), new Model<String>(e.getMessage())));
						}
					}
					protected ConfirmationDialog getConfirmationDialog() {
						return SearcherNotificationsPage.this.getConfirmationDialog();
					}
				});
				**/
		}
		else {
			add(new InvisiblePanel("user-notes"));
		}	
	}
}

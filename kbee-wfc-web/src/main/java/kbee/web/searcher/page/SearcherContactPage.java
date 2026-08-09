package kbee.web.searcher.page;




import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.library.ContactUsPanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.portal6.DomainSearcherPortalService;

public class SearcherContactPage extends AbstractSearcherPage<Void> {
			
	private static final long serialVersionUID = 1L;
	
	public  SearcherContactPage(PageParameters parameters) {
		setOutputMarkupId(true);
		setPageTitle(new StringResourceModel("about",  SearcherContactPage.this, null));
		Site site =  getSite(parameters);
		if (site!=null) 
			setSiteModel(new ObjectModel<Site>(site));
	}
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}
	
	
	
	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	@Override
	protected boolean isSearchForm() {
		return true;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getPageParameters().set("siteurl", getSiteModel()!=null?getSiteModel().getObject().getUrl().toString():"");
		
		WebMarkupContainer mains=new WebMarkupContainer("main-searcher");
		add(mains);
		
		if (!hasPermissions()) {
			mains.setVisible(false);
			return;
		}
		
		// String email=getDomain().getService(DomainSearcherPortalService.class).getContactEmail();
		//String email=getSiteModel().getObject().getEmailContact();
		
		//mains.add(new Label("title", getDomain().getService(DomainSearcherPortalService.class).getContactTitle()));
		//mains.add(new Label("abstract", getDomain().getService(DomainSearcherPortalService.class).getContactAbstract()).setEscapeModelStrings(false));
		//mains.add(new Label("text", getDomain().getService(DomainSearcherPortalService.class).getContactText()).setEscapeModelStrings(false));
		
		String s_text     = "";
		String s_title    = "Contact Us";
		String s_abstract = "Get the support you need, anytime, anywhere. Just fill out the form and we will respond as soon as possible.";
		
		mains.add(new Label("title",  s_title ));
		mains.add(new Label("abstract", s_abstract));
		mains.add(new Label("text", s_text));

		
		ContactUsPanel cp = new ContactUsPanel("contact-us-panel", email);
		cp.setClose(false);
		mains.add(cp);

	}

	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}

	String name;
	String email;
	String message;
	
	
	
	
	
	

	
	@Override
	protected boolean isInstitutional() {
		return true;
	}
	
	
	@Override
	protected boolean isHome() {
		return false;
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

	/** 
	 * Reports										 
	 **/											
	protected String getPageType()     {return "search-contact";} 												// con | det  
	protected String getContentTitle() {return null;} 													// content title or user title, ...
										
	protected String getStatsPageTitle() {return "search contact";} 										// for console page, it is the name of the console 
	protected Long getStatsPageId() {return new Long(0);} 								                // for console page, it is the name of the console
													
	protected String getObjectId()  {return null;} 												   		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content

	
}

package kbee.web.searcher.page;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.portal6.DomainSearcherPortalService;
import kbee.web.searcher.editor.SearcherAboutEditor;


public class SearcherAboutPage extends AbstractSearcherPage<Void> {
			
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherAboutPage.class.getName());
	
	
	@Override
	protected boolean isEditableOn() {
		return false;
	}
	
	
	
	public SearcherAboutPage(PageParameters parameters) {
		setOutputMarkupId(true);
		setPageTitle(new StringResourceModel("about", SearcherAboutPage.this, null));
		Site site =  getSite(parameters);
		if (site!=null) 
			setSiteModel(new ObjectModel<Site>(site));
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

		if (hasPermissions()) {
			
			String ti=(String) getSiteModel().getObject().getCustomValuesJson().get("title");
			
			logger.debug((String) getSiteModel().getObject().getCustomValuesJson().get("about-title"));
			logger.debug((String) getSiteModel().getObject().getCustomValuesJson().get("about-abstract"));
			logger.debug((String) getSiteModel().getObject().getCustomValuesJson().get("about-text"));
			
			
			mains.add(new Label("title", (String) getSiteModel().getObject().getCustomValuesJson().get("about-title")));
			mains.add((new Label("abstract", (String) getSiteModel().getObject().getCustomValuesJson().get("about-abstract"))).setEscapeModelStrings(false));
			mains.add( (new Label("text", (String) getSiteModel().getObject().getCustomValuesJson().get("about-text"))).setEscapeModelStrings(false));
			
			// mains.add(new Label("title", getDomain().getService(DomainSearcherPortalService.class).getAboutTitle()));
			// mains.add(new Label("abstract", getDomain().getService(DomainSearcherPortalService.class).getAboutAbstract()).setEscapeModelStrings(false));
			// mains.add(new Label("text", getDomain().getService(DomainSearcherPortalService.class).getAboutText()).setEscapeModelStrings(false));
		} else {
			mains.setVisible(false);
		}
	}
	
	
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
	protected String getPageType()     {return "search-about";} 										// con | det  
	protected String getContentTitle() {return null;} 													// content title or user title, ...
											
	protected String getStatsPageTitle() 	{return "search about";} 									// for console page, it is the name of the console 
	protected Long getStatsPageId() 		{return new Long(0);} 						                // for console page, it is the name of the console
													
	protected String getObjectId()  {return null;} 												   		// for user, domain, ...
	protected String getContentId() {return null;}	  													// for content

	
}


//PackageResourceReference res=ServiceLocator.getService(BrandingWebService.class).getSearchLibraryBckImage();
//if (res!=null) {
//	String imagehref = RequestCycle.get().urlFor(res, null).toString();
//	+mains.add(new AttributeModifier("style", "background: url(" + imagehref + ") no-repeat 0 0 scroll; background-size: cover;"));
//}



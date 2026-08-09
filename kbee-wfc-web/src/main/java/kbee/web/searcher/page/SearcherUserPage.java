package kbee.web.searcher.page;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

import kbee.web.service.ApplicationSiteMapService;

public class SearcherUserPage<T> extends AbstractSearcherPage<T> {
	private static final long serialVersionUID = 1L;

	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	@Override
	protected boolean isEditableOn() {
		return false;
	}

	
	
	public SearcherUserPage(IModel<Site> model_site) {
		setSiteModel(model_site);
		setPageTitle(new Model<String>(getSessionUser().getDisplayName()));
		addModals();
		
		// add(new UserMainPanel("user", new ObjectModel<Person>(getPerson()), true, true, true));
		PageParameters p=new PageParameters();
		p.add("person", getPerson().getId().toString());
		add(ServiceLocator.getService(ApplicationSiteMapService.class).getPanel("user", "user-main-panel", getPageParameters()));
		
		
		

	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	protected void addListeners() {
		super.addListeners();
	}
	
	@Override
	protected boolean isSearchForm() {
		return false;
	}
	
	protected String getPageType() {
		return "search-det";
	}
	
	protected Long getStatsPageId() 		{
		return Long.valueOf(0);
	} 								               	// 
}

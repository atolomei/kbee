package kbee.web.searcher;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

import kbee.web.portal6.DomainSearcherPortalService;

public class SearcherLinksQuery extends SearcherSiteQuery {

	private static final long serialVersionUID = 1L;

	
	public SearcherLinksQuery(Site site, Index index) {
		super(site, index);
		
		setIncludeFacets(false);
		
		getParameters().put("sort", "title_sort");
		getParameters().put("ascending", "false");
		
		DomainSearcherPortalService.HomeBlock hb = getDomain().getService(DomainSearcherPortalService.class).getHomeBlock("links");
		String iql = hb!=null?hb.iql:null;
		if (iql!=null) {
			String baseiql = (String) getParameters().get("iql");
			if (baseiql!=null && baseiql.length()>0)
				iql = "("+baseiql+") and " + iql;
			getParameters().put("iql", iql);
		}
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}

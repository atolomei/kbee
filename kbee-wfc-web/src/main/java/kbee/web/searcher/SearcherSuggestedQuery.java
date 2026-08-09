package kbee.web.searcher;

import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.portal6.model.Site;

import kbee.web.portal6.DomainSearcherPortalService;

public class SearcherSuggestedQuery extends SearcherSiteQuery {

	private static final long serialVersionUID = 1L;

	public SearcherSuggestedQuery(Site site, Index index) {
		super(site, index);
		setIncludeFacets(false);
		getParameters().put("sort", "title_sort");
		
		DomainSearcherPortalService.HomeBlock hb = getDomain().getService(DomainSearcherPortalService.class).getHomeBlock("queries");
		String iql = hb!=null?hb.iql:null;
		if (iql!=null) {
			String baseiql = (String) getParameters().get("iql");
			if (baseiql!=null && baseiql.length()>0)
				iql = "("+baseiql+") and " + iql;
			getParameters().put("iql", iql);
		}

		
	}

}

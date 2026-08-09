package kbee.web.searcher;

import com.novamens.indexer.service.Index;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;

public class SearcherHomeBlockQuery extends SearcherSiteQuery {
			
	private static final long serialVersionUID = 1L;

	public SearcherHomeBlockQuery(Site site, Block block, Index index) {
		super(site, index);
		setIncludeFacets(false);
		
		String sort = block.getCustomValuesJson().getString("sort");

		if (sort!=null) {
			if (sort.equals("title")) {
				getParameters().put("sort", "title_sort");
				getParameters().put("ascending", "true");
				setSortField("title_sort");
				setAscending(true);
			}
			else if (sort.equals("modified")) {
				getParameters().put("sort", "modified");
				setSortField("modified");
				setAscending(false);
			}
		}
		else {
			getParameters().put("sort", "modified");
			setSortField("modified");
			setAscending(false);
		}	
		
		String iql = block.getCustomValuesJson().getString("iql");
		
		if (iql!=null) {
			String baseiql = (String) getParameters().get("iql");
			if (baseiql!=null && baseiql.length()>0)
				iql = "("+baseiql+") and " + iql;
			getParameters().put("iql", iql);
		}
		
		String orderset = block.getCustomValuesJson().getString("order-set");
		if (orderset!=null)
			setOrderSet(orderset);
		else
			setOrderSet("modified");
			
	}
}

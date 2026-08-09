package kbee.web.query;


import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.solr.indexer.query.SolrParametersQuery;
 
public class DomainsQuery extends  SolrParametersQuery {
			
	private static final long serialVersionUID = 1L;

	static final private String ENA=String.valueOf(ObjectState.ENABLED.getId());
	static final private String AR=String.valueOf(ObjectState.ARCHIVED.getId());
	static final private String STATE="["+  ENA +", "+ AR + "]";
	
	public DomainsQuery(Index index) {
		super(index);
		
		getParameters().put("type", "domain");
		getParameters().put("sort", "title_sort");
		getParameters().put("ascending", "true");
		getParameters().put("state", STATE);
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
	
	@Override
	public boolean includeFacets() {
		return true;
	}
	

	

}

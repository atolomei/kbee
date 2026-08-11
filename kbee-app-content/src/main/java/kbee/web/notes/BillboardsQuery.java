package kbee.web.notes;

import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;

import kbee.web.query.DomainSolrQuery2;



public class BillboardsQuery extends DomainSolrQuery2  {
	private static final long serialVersionUID = 1L;

	public BillboardsQuery(Index index) {
		super(index);
		Domain domain = getDomain();
		getParameters().put("type", "[billboard]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("domain", String.valueOf(domain.getId()));

	}
}

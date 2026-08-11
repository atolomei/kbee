package kbee.web.domain;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

import kbee.web.query.DomainsQuery;

public class DomainsRecycleBinQuery extends DomainsQuery {
	private static final long serialVersionUID = 1L;
	
	public DomainsRecycleBinQuery(Index index) {
		super(index);
		getParameters().put("state", "["+ String.valueOf(ObjectState.DELETED.getId())+ "]");
	}
}

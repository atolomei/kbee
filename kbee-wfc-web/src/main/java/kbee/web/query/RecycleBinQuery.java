package kbee.web.query;

import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;

public class RecycleBinQuery extends DomainSolrQuery {
	private static final long serialVersionUID = 1L;

	public RecycleBinQuery(Index index, User user) {
		super(index);
		Domain domain = getDomain();
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "title");
		getParameters().put("state", String.valueOf(ObjectState.DELETED.getId()));
		getParameters().put("domain", String.valueOf(domain.getId()));
		getParameters().put("lastmodifieduser", String.valueOf(user.getId()));  // VER [AF]
		getParameters().put("ascending", "true");
	}
}

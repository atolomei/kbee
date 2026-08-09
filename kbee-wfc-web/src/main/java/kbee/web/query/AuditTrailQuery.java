package kbee.web.query;

import com.novamens.content.base.Content;
import com.novamens.indexer.service.Index;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class AuditTrailQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public AuditTrailQuery(Index index, Content content) {
		super(index);
		//User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		getParameters().put("type", "logevent");
		getParameters().put("sort", "date");
		getParameters().put("ascending", "false");
//		getParameters().put("workspace", String.valueOf(user.getId()));
	}
}

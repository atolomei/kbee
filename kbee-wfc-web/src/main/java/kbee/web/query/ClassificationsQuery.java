package kbee.web.query;

import com.novamens.content.model.DataSet;
import com.novamens.indexer.service.Index;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class ClassificationsQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public ClassificationsQuery(Index index, DataSet dataset) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "name");
		getParameters().put("ascending", "false");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
	}
}

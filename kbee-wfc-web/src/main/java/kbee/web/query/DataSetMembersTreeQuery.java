package kbee.web.query;

import com.novamens.content.model.DataSet;
import com.novamens.indexer.service.Index;

public class DataSetMembersTreeQuery extends DataSetMembersQuery {
	private static final long serialVersionUID = 1L;

	public DataSetMembersTreeQuery(Index index, DataSet dataset) {
		this(index, dataset, false);
	}

	public DataSetMembersTreeQuery(Index index, DataSet dataset, String sort) {
		super(index, dataset, sort, false);
	}
	
	public DataSetMembersTreeQuery(Index index, DataSet dataset, boolean deleted_visible) {
		super(index, dataset, deleted_visible);
	}
	
	@Override
	public String getStatement() {
		String statement = super.getStatement();
		if (!getParameters().containsKey("parent")) {
			statement  = "("+statement+") AND NOT parent:*";
		}
		return statement;
	}
}

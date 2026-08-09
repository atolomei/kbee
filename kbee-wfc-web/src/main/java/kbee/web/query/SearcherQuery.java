package kbee.web.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

public class SearcherQuery extends ContentQuery {
	
	private static final long serialVersionUID = 1L;

	public SearcherQuery(Index index) {
		super(index);
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("head", "true");
		
	}
}

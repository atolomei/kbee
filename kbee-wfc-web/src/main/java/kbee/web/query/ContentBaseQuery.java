package kbee.web.query;


import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;


public class ContentBaseQuery extends ContentQuery {
	private static final long serialVersionUID = 1L;

	public ContentBaseQuery(Index index) {
		super(index);
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getFilterParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getFilterParameters().put("head", "true");
	}
}

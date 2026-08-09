package kbee.web.cursor;

import java.util.List;
import java.util.Map;


import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.SearchResult;

public class ListModelSearchResult<T> implements SearchResult {

	private static final long serialVersionUID = 1L;

	IModel<T> object;
	
	public ListModelSearchResult(IModel<T> object) {
		this.object=object;
	}
	
	@Override
	public void detach() {
		if (object!=null)
			this.object.detach();
	}

	@Override
	public Object getObject() {
		if (this.object!=null)
			return this.object.getObject();
		return null;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public Map<String, Object> getParameters() {
		return null;
	}

	@Override
	public float getScore() {
		return 0;
	}

	@Override
	public List<String> getSnippets() {
		return null;
	}

}

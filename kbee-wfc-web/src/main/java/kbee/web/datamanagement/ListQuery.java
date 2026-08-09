package kbee.web.datamanagement;

import com.novamens.indexer.query.*;
import com.novamens.kbee.content.command.ListResultSet;
import org.apache.wicket.model.IModel;

import java.util.*;

public class ListQuery<T> implements Query {

	private static final long serialVersionUID = 1L;
	private List<IModel<T>> list;

	public ListQuery(List<IModel<T>> list) {
		this.list = list;
	}

	@Override
	public ResultSet execute() {
		return new ListResultSet<IModel<T>>(list){
			@Override
			public SearchResult next() {
				final IModel<T> obj = getIterator().next();
				return new SearchResult(){
					private static final long serialVersionUID = 1L;
					@Override
					public void detach() {
						obj.detach();
					}

					@Override
					public Object getObject() {
						return obj.getObject();
					}

					@Override
					public String getText() {
						return obj.getObject().toString();
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
				};
			}
		};
	}

	@Override
	public QueryBuilder getBuilder() {
		return null;
	}

	
	@Override
	public Map<String, Object> getParameters() {
		return new HashMap<String, Object>();
	}


	@Override
	public void setParameters(Map<String, Object> parameters) {
	}
	
	@Override
	public void setParameter(String name, Object value) {
	}

	@Override
	public void setOptions(Map<String, FacetOptions> options) {
	}

	@Override
	public String getTitle() {
		return null;
	}
	
	@Override
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}

}

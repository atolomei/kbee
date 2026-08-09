package kbee.web.query;

import java.util.Map;

import com.novamens.content.library.Library;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Criteria;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;


public class LibraryQuery extends ContentQuery {
	private static final long serialVersionUID = 1L;

	public LibraryQuery(Index index) {
		super(index);
		getFilterParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getFilterParameters().put("head", "true");
		getFilterParameters().put("type", "[idoc]");
	}
	
	public LibraryQuery(Index index, Library lib) {
		super(index);
		setLibrary(lib);
		getFilterParameters().put("type", "[idoc]");
	}
	
	public void setLibrary(Library lib) {
		if (lib==null)
			return;
		Criteria criteria = lib.getCriteria();
		if (criteria!=null) {
			Map<String, Object> parameters = criteria.getParameters();
			for (String parametername : parameters.keySet()) {
				getFilterParameters().put(parametername, parameters.get(parametername));
			}
		}
	}
	
	@Override
	public void setParameter(String name, Object value) {
		super.setParameter(name, value);
		if (getParameters().containsKey("userlist")) {
			getFilterParameters().put("type", "[idoc, useritem]");
		}
		else {
			getFilterParameters().put("type", "[idoc]");
		}
	}
	
	public void setParameters(Map<String, Object> parameters) {
		super.setParameters(parameters);
		if (parameters.containsKey("userlist")) {
			getFilterParameters().put("type", "[idoc, useritem]");
		}
		else {
			getFilterParameters().put("type", "[idoc]");
		}
	}
	
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute());
	}
}
	
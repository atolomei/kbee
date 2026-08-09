package kbee.web.console;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;

import kbee.web.query.ConsoleQuery;

public class MyBoxQuery extends ConsoleQuery {

	private static final long serialVersionUID = 1L;

	
	public MyBoxQuery(User workspace, Index index) {
		super(index);
		User user = workspace;
		getParameters().put("type", "[idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.DRAFT.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
		
	}
	public MyBoxQuery(Index index) {
		super(index);
		User user = getSessionUser();
		getParameters().put("type", "[idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.DRAFT.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
	
	public MyBoxQuery(Index index, User user) {
		super(index);
		getParameters().put("type", "[idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.DRAFT.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
}

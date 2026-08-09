package kbee.web.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;

public class WorkspaceQuery extends ConsoleQuery {
	
	private static final long serialVersionUID = 1L;

	public WorkspaceQuery(Index index) {
		super(index);
		User user = getSessionUser();
		getParameters().put("type", "[text, idoc]");
		
		//getParameters().put("sort", "modified");
		
		getParameters().put("sort", "title_sort");
		
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
	
	public WorkspaceQuery(Index index, User user) {
		super(index);
		getParameters().put("type", "[text, idoc]");
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		
		
		
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
}
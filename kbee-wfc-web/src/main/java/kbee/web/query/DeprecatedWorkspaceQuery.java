package kbee.web.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;

@Deprecated
public class DeprecatedWorkspaceQuery extends ConsoleQuery {
	private static final long serialVersionUID = 1L;

	public DeprecatedWorkspaceQuery(Index index) {
		super(index);
		User user = getSessionUser();
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("workspace", String.valueOf(user.getId()));
	}
	
	public DeprecatedWorkspaceQuery(Index index, User user) {
		super(index);
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("workspace", String.valueOf(user.getId()));
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		
	}
}
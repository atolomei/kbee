package kbee.web.query;

import com.novamens.content.userlist.UserList;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;

/**
 * 
 *  {@code MonitorUserListQuery}
 *  {@code WorkspaceUserListQuery}
 *  
 */
public class WorkspaceUserListQuery extends WorkspaceQuery {
	private static final long serialVersionUID = 1L;

	public WorkspaceUserListQuery(UserList userList, Index index) {
		super(index);
		
		getParameters().put("type", "[text, idoc, useritem]");
		getParameters().put("userlist", String.valueOf(((KbeeUserList)userList).getId()));
	}
	
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute());
	}
}

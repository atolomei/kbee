package kbee.web.query;

import com.novamens.content.userlist.UserList;

import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;

public class MonitorUserListQuery extends MonitorQuery {
	private static final long serialVersionUID = 1L;

	public MonitorUserListQuery(UserList userList, Index index) {
		super(index);
		getFilterParameters().put("type", "[text, idoc, useritem]");
		getParameters().put("userlist", String.valueOf(((KbeeUserList)userList).getId()));
	}
	
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute());
	}

}

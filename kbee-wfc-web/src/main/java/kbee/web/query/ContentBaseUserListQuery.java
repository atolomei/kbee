package kbee.web.query;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.userlist.UserList;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;

public class ContentBaseUserListQuery extends ContentBaseQuery {
	private static final long serialVersionUID = 1L;
	
	public ContentBaseUserListQuery(UserList userList,Index index) {
		super(index);
		getParameters().put("userlist", String.valueOf(((KbeeUserList)userList).getId()));
	}

	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute());
	}
	
	public Map<String, Object> getFilterParameters() {
		Map<String, Object> filterparameters = new HashMap<String, Object>();
		filterparameters.put("state", String.valueOf(ObjectState.ENABLED.getId()));
		filterparameters.put("head", "true");
		String types = getParameters().get("userlist")!=null ? "[text, idoc, useritem]" : "[text, idoc]";
		filterparameters.put("type", types);
		return filterparameters;
	}
}

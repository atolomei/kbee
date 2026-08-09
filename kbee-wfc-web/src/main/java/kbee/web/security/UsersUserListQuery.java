package kbee.web.security;

import java.util.Map;

import com.novamens.content.model.DataSet;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.KbeeUserList;

public class UsersUserListQuery extends UsersQuery {
	private static final long serialVersionUID = 1L;

	public UsersUserListQuery(UserList list, Index index, DataSet dataset) {
		super(index, dataset);
		addParam(list);
	}
	
	
	public UsersUserListQuery(UserList list, Map<String, String> parameters, Index index, DataSet dataset) {
			super(parameters, index, dataset);
			addParam(list);
	}
	public UsersUserListQuery(UserList list, Index index, DataSet dataset, boolean deleted_visible) {
		super( index, dataset, deleted_visible);
		addParam(list);
	}
	
	public UsersUserListQuery(UserList list, Map<String, String> parameters, Index index, DataSet dataset, boolean deleted_visible) {
		super( parameters, index, dataset, deleted_visible);
		addParam(list);
		
	}
	
//	@Override
//	public ResultSet execute() {
//		return new UserListResultSetWrapper(super.execute()) {
//			@Override
//			public SearchResult next() {
//				SearchResult result = getResultSet().next();
//				Object object = result.getObject();
//				if (object instanceof UserListItem) {
//					object = ((UserListItem)object).getObject();
//					object = getContentDao().reload(object);
//					result = new SolrSearchResult(object);
//				}
//				return result;
//			}
//		};
//	}
	
	private void addParam(UserList list) {
		getParameters().put("type", "[datesetmember, useritem]");
		getParameters().put("userlist", String.valueOf(((KbeeUserList) list).getId()));		
	}
}

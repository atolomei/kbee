package kbee.web.query;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.service.ServiceLocator;

public class DatasetMemebersUserListQuery extends DataSetMembersQuery {
				
	
	private static final long serialVersionUID = 1L;

	public DatasetMemebersUserListQuery(UserList userList, Index index, DataSet dataset) {
			super(index, dataset);
			addParam( userList);
	}
	
	public DatasetMemebersUserListQuery(UserList userList, Index index, DataSet dataset, boolean deleted_visible) {
		super(index, dataset, deleted_visible);
		addParam( userList);
	}
	
	public DatasetMemebersUserListQuery(UserList userList, Index index, DataSet dataset, String sort, boolean deleted_visible) {
		super(index, dataset, sort, deleted_visible);
		addParam( userList);
	}
	

/*	@Override
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute()) {
			@Override
			public SearchResult next() {
				SearchResult result = getResultSet().next();
				Object object = result.getObject();
				if (object instanceof UserListItem) {
					object = ((UserListItem)object).getObject();
					object = getContentDao().reload(object);
					result = new SolrSearchResult(object);
				}
				return result;
			}
		};

	}*/

	
	
	
	// return new UserListResultSetWrapper(super.execute());
	
	
//protected ResultSet getResultSet() {
//		return resultset;
//	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	private void addParam(UserList userList) {
		getParameters().put("type", "[datasetmember, useritem]");
		getParameters().put("userlist", String.valueOf(((KbeeUserList) userList).getId()));
	}
	
}

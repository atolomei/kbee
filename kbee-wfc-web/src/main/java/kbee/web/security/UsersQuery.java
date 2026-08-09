package kbee.web.security;

import java.util.Map;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.solr.indexer.query.SolrSearchResult;

public class UsersQuery extends SolrParametersQuery {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UsersQuery.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean role_federated_security	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());

	/**
	 * 
	 * DataSet is the UserSet
	 * 
	 *  UserSet instance has a relationship to  
	 *  
	 *  PersonMember (DataSet)
	 *  UserProfile
	 * 
	 * @param index
	 * @param dataset
	 */
	public UsersQuery(Index index, DataSet dataset) {
		this( index, dataset, false);
	}

	public UsersQuery(Map<String, String> parameters, Index index, DataSet dataset) {
		this(parameters, index, dataset, false);
	}
	
	public UsersQuery(Index index, DataSet dataset, boolean deleted_visible) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
		getParameters().put("state", getStates(deleted_visible));
		
		if (!role_security && role_federated_security) {
			getParameters().put("solrclause", getUserAdminClause());
		}
	}

	public UsersQuery(Map<String, String> parameters, Index index, DataSet dataset, boolean deleted_visible) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
		getParameters().put("state", getStates(deleted_visible));

		if (parameters.get("id")!=null)
			getParameters().put("id", parameters.get("id"));

		if (parameters.get("username")!=null)
			getParameters().put("username", parameters.get("username"));
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
	
	@Override
	public boolean includeFacets() {
		return true;
	}
	
	@Override
	public IqlService getIqlService() {
		return ServiceLocator.getService(UserService.class).getDomain().getService(IqlService.class);
	}
	
	@Override
	public ResultSet execute() {
		return new UserListResultSetWrapper(super.execute()) {
			@Override
			public SearchResult next() {
				SearchResult result = getResultSet().next();
				Object object = result.getObject();
				if (object instanceof UserListItem) {
					object = ((UserListItem)object).getObject();
				}
				if (object instanceof HibernateProxy) {
					HibernateProxy proxy = (HibernateProxy)object;
					LazyInitializer initializer = proxy.getHibernateLazyInitializer();
					object = initializer.getImplementation();
				}
				result = new SolrSearchResult(object);
				return result;
			}
		};
	}
	
	protected String getStates(boolean deleted_visible) {
		return "["+String.valueOf(ObjectState.ENABLED.getId())+
		", "+String.valueOf(ObjectState.ARCHIVED.getId()) + 
		(deleted_visible ? (", "+String.valueOf(ObjectState.DELETED.getId())):"")+ "]";
	}
	
	protected String getUserAdminClause() {
		
		UserProfile sessionuserprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		String iqlclause = "";
		UserSet userSet = getContentDao().getUserSet();
		for (UserRole userrole : sessionuserprofile.getRoles()) {
			
			if (userrole.getEntity()!=null) {
				EntityRole role = (EntityRole)reload(userrole.getRole());
				if (role.manage(userSet)) {
					if (!"".equals(iqlclause)) {
						iqlclause += " OR ";
					}
					iqlclause += role.getClassifier().getPredicate() + "(";
					iqlclause += String.valueOf(userrole.getEntity().getId());
					iqlclause += ")";
				}
			}
		}
		
		IqlQuery query = getIqlService().getNewQuery(iqlclause);
		String solrclause = ((SolrIqlQuery)query).getSolrStatement();
		solrclause = "(("+solrclause+") OR lastmodifieduser:"+String.valueOf(sessionuserprofile.getUser().getId()) + ")";
		return solrclause;
	}
	
	protected Role reload(Role role) {
		return (Role)getContentDao().reload(role);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

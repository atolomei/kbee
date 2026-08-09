package kbee.web.query;

import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class SitesQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	//private static Logger logger = Logger.getLogger(SitesQuery.class.getName());
	
	public SitesQuery(Map<String, String> parameters, Index index, boolean deletedVisible) {
		super(index);
		getParameters().put("type", "site");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("state", getStates(deletedVisible));
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
	
	protected String getStates(boolean deleted_visible) {
		return "["+String.valueOf(ObjectState.ENABLED.getId())+
		", "+String.valueOf(ObjectState.ARCHIVED.getId()) + 
		(deleted_visible ? (", "+String.valueOf(ObjectState.DELETED.getId())):"")+ "]";
	}
	
	
	protected String getUserAdminClause() {
		
		UserProfile sessionuserprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		String iqlclause = "";
		
		for (UserRole userrole : sessionuserprofile.getRoles()) {
			if (userrole.getEntity()!=null) {
				EntityRole role = (EntityRole)reload(userrole.getRole());
				if (role.enableUserAdmin()) {
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

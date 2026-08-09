package kbee.web.query;

import java.io.Serializable;

import org.hibernate.proxy.HibernateProxy;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.SecuredSet;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.userlist.UserListResultSetWrapper;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.solr.indexer.query.SolrSearchResult;

import kbee.query.QueryHelpher;
import kbee.web.dataset.DataSetNode;

public class DataSetMembersQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	
	private boolean secured = false;
	private String managedTerm = "";

	public DataSetMembersQuery(Index index, DataSet dataset) {
		this(index, dataset, false);
	}

	public DataSetMembersQuery(Index index, DataSet dataset, String sort) {
		this(index, dataset, sort, false);
	}
	
	public DataSetMembersQuery(Index index, DataSet dataset, boolean deleted_visible) {
		this(index, dataset, "modified", deleted_visible);
	}
	
	public DataSetMembersQuery(Index index, DataSet dataset, String sort, boolean deleted_visible) {
		super(index);
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", sort);
		getParameters().put("state", 	"["+String.valueOf(ObjectState.ENABLED.getId())+
				", "+String.valueOf(ObjectState.ARCHIVED.getId()) + 
				(deleted_visible ? (", "+String.valueOf(ObjectState.DELETED.getId())):"")+ "]");
		getParameters().put("ascending", "true");
		getParameters().put("dataset", String.valueOf(dataset.getId()));
		if (dataset instanceof SecuredSet) {
			secured = true;
		}
		if (dataset instanceof EntitySet) {
			managedTerm = QueryHelpher.buildManegdTerm(dataset);
		}
	}
	
	@Override
	public void setParameter(String name, Object value) {
		if (value==null) {
			if ("node".equals(name)) {
				getParameters().remove("parent");
			}
			else {
				getFilterParameters().remove(name);
				getParameters().remove(name);
			}
		}
		else {
			if ("node".equals(name))
				super.setParameter("parent", String.valueOf(((DataSetNode)value).getObject().getId()));
			else
				super.setParameter(name, value);
		}
	}
	
	@Override
	public boolean includeFacets() {
		return true;
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
					object = Proxy.Unproxy(object);
				}
				result = new SolrSearchResult(object);
				return result;
			}
		};
	}
	
	@Override
	public String getSolrFilterStatement() {
		String ss = super.getSolrFilterStatement();
		String statement = "";
		if (secured) {
			statement = QueryHelpher.buildSecurityTerm(KbeePermission.READ);
		}
		statement += managedTerm;
		Serializable userId = ServiceLocator
			.getService(SecurityService.class)
			.getSessionUser()
			.getId();
		if (!"".equals(statement)) {
			statement = "(" + statement +") OR "; 
			statement += "lastmodifieduser:" + userId; 
		}
		ss = ss!=null  
			? ss = (!"".equals(statement) ? ss + " AND ("+statement+")" : ss)
			: statement;		
		return ss;
	}
}

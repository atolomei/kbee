package kbee.web.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class SitesSolrQuery extends SolrParametersQuery {
			
	private static final long serialVersionUID = 1L;

	public SitesSolrQuery(Index index) {
		this(index, false);
		
	}
	
	
	public SitesSolrQuery(Index index, boolean deletedVisible) {
		super(index);
		getParameters().put("type", "[site]");
		getParameters().put("sort", "modified");
		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("ascending", "false");
		getParameters().put("state", getStates(deletedVisible));
	}
	
	@Override
	public boolean includeFacets() {
		return true;
	}
	
	protected Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
	
	
	protected String getStates(boolean deleted_visible) {
		return "["+String.valueOf(ObjectState.ENABLED.getId())+
		", "+String.valueOf(ObjectState.ARCHIVED.getId()) + 
		(deleted_visible ? (", "+String.valueOf(ObjectState.DELETED.getId())):"")+ "]";
	}
	
	
}

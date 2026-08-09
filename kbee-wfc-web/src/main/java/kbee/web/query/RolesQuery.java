package kbee.web.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class RolesQuery extends  SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	
	public RolesQuery(Index index) {
		this(index, null);
	}
	
	public RolesQuery(Index index, String sort) {
		super(index);
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		getParameters().put("domain", String.valueOf(domain.getId()));
		getParameters().put("type", "role");
		if (sort!=null)
		getParameters().put("sort", sort);
		getParameters().put("state", 	"["+String.valueOf(ObjectState.ENABLED.getId())+", "+String.valueOf(ObjectState.ARCHIVED.getId()) + "]");
		getParameters().put("ascending", "true");
	}
	
	@Override
	public boolean includeScore() {
		return true;
	}
	
	@Override
	public boolean includeFacets() {
		return true;
	}
}

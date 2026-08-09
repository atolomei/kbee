package kbee.web.query;


import java.time.OffsetDateTime;
import java.util.Map;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class AuditResourcesQuery extends SolrParametersQuery  {
	
	private static final long serialVersionUID = 1L;

	
/**	
	public AuditResourcesQuery(Index index) {
		thie(index, getDomainKbee())
		
		getParameters().put("type", "kbfile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		if (!isDomainKbee())
			getParameters().put("domain", String.valueOf(getDomain().getId()));
 	}
	
	
	public AuditResourcesQuery(Index index, Domain domain) {
		super(index);
		getParameters().put("type", "kbfile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("domain", String.valueOf(domain.getId()));
	}
	
	public AuditResourcesQuery(Index index, boolean allDomains) {
		super(index);
		getParameters().put("type", "kbfile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
	 	if (!allDomains)
			getParameters().put("domain", String.valueOf(getDomain().getId()));
  	}
 	**/
	
	public AuditResourcesQuery(Index index, Domain domain, Map<String, Object> filters, boolean allDomains) {
		super(index);
		
		getParameters().put("type", "kbfile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("type", "kbfile");
		
		if (filters!=null) {
			for (String filter: filters.keySet()) {
				getParameters().put(filter, filters.get(filter));
			}
		}
		
		if (!allDomains) {
			//getParameters().put("domain", String.valueOf(getDomain().getId()));
			getParameters().put("domain", String.valueOf(domain.getId()));
		}
		
		
		
		
  	}

	
	
	
		
	
	

	private Boolean is_domain_kbee = null;
	
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

	protected Domain getDomain() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}

	
	
}

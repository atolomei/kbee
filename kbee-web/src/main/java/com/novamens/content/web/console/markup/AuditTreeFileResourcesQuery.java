package com.novamens.content.web.console.markup;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class AuditTreeFileResourcesQuery extends SolrParametersQuery implements Query {
				
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuditTreeFileResourcesQuery(Index index, boolean all_domains) {
		super(index);
		getParameters().put("type", "treefile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
	 	if (!all_domains)
			getParameters().put("domain", String.valueOf(getDomain().getId()));
  	}
	
	
	public AuditTreeFileResourcesQuery(Index index) {
		super(index);
		getParameters().put("type", "treefile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("domain", String.valueOf(getDomain().getId()));
 	}
	
		
	public AuditTreeFileResourcesQuery(Index index, Domain domain) {
		super(index);
		getParameters().put("type", "treefile");
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("domain", String.valueOf(domain.getId()));
	}
	
	protected Domain getDomain() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}


}

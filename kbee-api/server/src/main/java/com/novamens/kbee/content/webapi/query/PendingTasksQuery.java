package com.novamens.kbee.content.webapi.query;


import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class PendingTasksQuery extends SolrParametersQuery  {
	
	private static final long serialVersionUID = 1L;
	
	private static final String ROLE_DOMAIN_ADMIN = KbeeGlobalRole.DOMAIN_ADMIN.getId();

	
	public PendingTasksQuery(Index index, User user, boolean facets) {
		super(index);
		
		setIncludeFacets(facets);
		
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("workspace", getWorkflowUser());
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("ascending", "false");
				
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin = service.isMember(user, ROLE_DOMAIN_ADMIN);
		
		if (!service.isRoot() && !admin) {
			String takers = String.valueOf(user.getId());
			for (Group group : user.getGroups()) {
				takers = getTakers(group, takers);
			}
			if (!" ".equals(takers)) {
				getParameters().put("taker", "["+takers.trim()+"]");
			}
		}
	}

	public String getWorkflowUser() {
		return String.valueOf(getDomain().getService(DomainService.class).getWorkflowUser().getId());
	}
	
	/**
	 * 
	 * @param group
	 * @param writers
	 * @return
	 */
	protected String getTakers(Group group, String takers) {
		StringBuilder str =new StringBuilder();
		String id = ((KbeeGroup)group).getId().toString();
		if (takers.contains(" "+id)) 
			return takers;
		str.append(takers);
		if (!" ".equals(takers)) 
			str.append(", ");
		str.append(((KbeeGroup)group).getId());
		String ret = str.toString();
		for (Group parent : ((KbeeGroup)group).getGroups()) 
			ret = getTakers(parent, ret);
		return ret;
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	

}
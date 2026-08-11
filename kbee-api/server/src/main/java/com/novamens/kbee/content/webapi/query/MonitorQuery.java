package com.novamens.kbee.content.webapi.query;


import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class MonitorQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public MonitorQuery(Index index, User user, boolean facets) {
		super(index);

		setIncludeFacets(facets);
		
		getParameters().put("type", "[text, idoc]");
		
		getParameters().put("domain", String.valueOf(((KbeeUser)user).getDomain().getId()));
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		getParameters().put("inworkspace", "true");
		
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		//getParameters().put("workspace", String.valueOf(user.getId()));
	
	
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId()); 
	
		if (!service.isRoot() && !admin && !support) {
			String readers = String.valueOf(user.getId());
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			getParameters().put("reader", "["+readers.trim()+"]");
		}
	}

	protected String getReaders(Group group, String readers) {
	
		StringBuilder str =new StringBuilder();
		
		String id = ((KbeeGroup)group).getId().toString();
		
		if (readers.contains(" "+id)) 
			return readers;
		
		str.append(readers);
		
		if (!" ".equals(readers)) 
			str.append(", ");
		
		str.append(((KbeeGroup)group).getId());
		String ret = str.toString();
		
		for (Group parent : ((KbeeGroup)group).getGroups()) {
			ret = getReaders(parent, ret);
		}
		return ret;
	}
}
package com.novamens.kbee.content.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;


public class MonitorQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

	public MonitorQuery(Index index, String iql) {
		super(index);
		
		User user = getSessionUser();
		
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");


		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("ascending", "false");
		getParameters().put("inworkspace", "true");

		getParameters().put("iql", iql);
				
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
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
	
	public User getSessionUser() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
	@Override
	public IqlService getIqlService() {
		return getDomain().getService(IqlService.class);
	}
	
	@Override
	public String[] fields() {
		String fields[] = { "id", "title", "score" };
		return fields;
	}
}

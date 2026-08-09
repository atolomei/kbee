package kbee.web.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;

public class ProgressNotesQuery2 extends SolrQuery {
	private static final long serialVersionUID = 1L;
	
	//private static final String ROLE_DOMAIN_ADMIN = KbeeGlobalRole.DOMAIN_ADMIN.getId();

	public ProgressNotesQuery2(Index index) {
		super(index);
		
		setSortField("lastmodifiedtime");
		setAscending(false);
		
		getParameters().put("sort", "lastmodifiedtime");
		getParameters().put("ascending", "false");
	}
	
	public String getStatement() {
		return getSolrStatement();
	}
	
	public String getSolrStatement() {
		//String statement = "{!join  from=objectid to=objectid}";
		//clsf01member:150475

		String statement = "";
		
		User user = getSessionUser();

		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin 	= service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId()); 
		
		if (!service.isRoot() && !admin && !support) {
			statement = "{!join  from=id to=contentid}";
			String readers = String.valueOf(user.getId());
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			statement += "reader:("+readers+") AND state:1 AND head:false";
		}
		else {
			statement = "{!join  from=id to=contentid}";
			statement += "state:1 AND head:false";
		}
		
		return statement;
	}
	
	public String getSolrFilterStatement() {
		String statement = "type:progressnote AND state:1 AND domain:"+getDomain().getId();
		return statement;
	}
	
	private User getSessionUser() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		return profile.getUser();
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private String getReaders(Group group, String readers) {
		
		StringBuilder str =new StringBuilder();
		
		String id = ((KbeeGroup)group).getId().toString();
		
		if (readers.contains(" "+id)) 
			return readers;
		
		str.append(readers);
		
		if (!"".equals(readers)) 
			str.append(" OR ");
		
		str.append(((KbeeGroup)group).getId());
		String ret = str.toString();
		
		for (Group parent : ((KbeeGroup)group).getGroups()) {
			ret = getReaders(parent, ret);
		}
		return ret;
	}
}
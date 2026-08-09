package kbee.web.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ArchiveQuery extends ConsoleQuery {
			
	private static final long serialVersionUID = 1L;

	public ArchiveQuery(Index index) {
		super(index);
		
		User user = getSessionUser();
		
		getParameters().put("type", "[text, idoc]");
		getParameters().put("sort", "modified");
		getParameters().put("head", "true");
		getParameters().put("-istemplate", "true");
		
		getParameters().put("state", String.valueOf(ObjectState.ARCHIVED.getId()));
		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("ascending", "false");
				
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
}

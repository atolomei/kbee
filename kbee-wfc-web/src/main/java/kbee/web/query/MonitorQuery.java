package kbee.web.query;


import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class MonitorQuery extends ContentQuery {
			
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MonitorQuery.class.getName());
	
	private static final long serialVersionUID = 1L;

	public MonitorQuery(Index index) {
		this(index, null);
	}
	
	public MonitorQuery(Index index, User filterByWorkspace) {
		super(index);
		
		User user = getSessionUser();
		Domain domain = getDomain();
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
		
		//getFilterParameters().put("type", "[idocproxy]");
		getFilterParameters().put("type", "[text, idoc, idocproxy]");
		getFilterParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getFilterParameters().put("inworkspace", "true");
		getFilterParameters().put("domain", String.valueOf(domain.getId()));
		
		if (filterByWorkspace!=null) {
			getFilterParameters().put("workspace", filterByWorkspace.getId().toString());
		}
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId()); 
		
		if (!service.isRoot() && !admin && !support) {
			String readers = " ";
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			if (!"".equals(readers)) {
				readers += ", "; 
			}
			readers += String.valueOf(getSessionUser().getId()); 
			//logger.debug(readers);
			if (!" ".equals(readers)) {
				getParameters().put("reader", "["+readers.trim()+"]");
			}
		}
	}
}

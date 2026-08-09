package kbee.web.query;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

@Deprecated
public class AuditContentQuery extends ConsoleQuery {
			
	private static final long serialVersionUID = 1L;

	public AuditContentQuery(Index index) {
		super(index);
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		getParameters().put("type", "logevent");	
		
		// getParameters().put("-eventtype", "SendEmail");
		getParameters().put("auditset", String.valueOf(AuditSet.CONTENT.getId()));
		
		getParameters().put("domain", String.valueOf(domain.getId()));
		
		getParameters().put("sort", "executed");
		getParameters().put("ascending", "false");
		
	}
}

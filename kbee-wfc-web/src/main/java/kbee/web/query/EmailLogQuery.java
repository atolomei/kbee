package kbee.web.query;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;


@Deprecated
public class EmailLogQuery extends ConsoleQuery {
			
	static Logger logger = LogManager.getLogger(EmailLogQuery.class.getName());
	
	private static final long serialVersionUID = 1L;

	public EmailLogQuery(Index index) {
			this(index, false);
	}
							
	public EmailLogQuery(Map<String, String> parameters, Index index, final boolean all_domains) {
		super(index);
		
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		
		// From
		// To
		// Text
		if (parameters.get("title")!=null)
			getParameters().put("title", parameters.get("title"));

		// DateRange
		if (parameters.get("executed")!=null)
			getParameters().put("member", parameters.get("executed"));
		
		getParameters().put("type", "logevent");
		getParameters().put("eventtype", "SendEmail");
		
		if (!all_domains)
			getParameters().put("domain", String.valueOf(domain.getId()));
		
		
		if (parameters.get("sort")!=null)
			getParameters().put("sort", parameters.get("sort"));
		else
			getParameters().put("sort", "executed");
		
		if (parameters.get("ascending")!=null)
			getParameters().put("ascending", "false");
		else
			getParameters().put("ascending", parameters.get("ascending"));
		
		logger.debug(getParameters());
	}
	
	
	public EmailLogQuery(Index index, final boolean all_domains) {
		super(index);
		
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		Domain domain = profile.getDomain();
		getParameters().put("type", "logevent");
		getParameters().put("eventtype", "SendEmail");
		
		if (!all_domains)
			getParameters().put("domain", String.valueOf(domain.getId()));
		
		getParameters().put("sort", "executed");
		getParameters().put("ascending", "false");
	}
}

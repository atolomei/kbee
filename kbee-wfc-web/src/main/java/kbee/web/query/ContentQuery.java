package kbee.web.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/** 
 * type: Question 
 * domain: domain
 * head: true
 * state: enabled
 * ascending: false
 * tag: tag1, tag2, tag3
 * 
 */
public class ContentQuery extends ConsoleQuery {
	
	private static final long serialVersionUID = 1L;
	
	public ContentQuery(Index index) {
		super(index);
		
		User user = getSessionUser();
		
		getFilterParameters().put("domain", String.valueOf(getDomain().getId()));
		getFilterParameters().put("type", "[text, idoc]");
		
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "false");
				
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin 	= service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId()); 
		
		if (!service.isRoot() && !admin && !support) {
			 String readers = String.valueOf(user.getId());
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			StringBuilder xr = new StringBuilder();
			xr.append(readers);
			
			if (xr.length()>0)
				xr.append(", ");
			xr.append(String.valueOf(getSessionUser().getId()));
			getParameters().put("reader", "["+xr.toString().trim()+"]");
		}
	}
	
	protected String getReadersStatement() {
		
		String statement = "";
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		User user = service.getSessionUser();
		
		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
		
		if (!service.isRoot() && !admin && !support) {
			List<String> readers = new ArrayList<>();
			readers.add(String.valueOf(user.getId()));
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			for (String principal : readers) {
				if ("".equals(statement))
					statement += "reader:(";
				else
					statement += " OR ";
				statement += principal;
			}
			statement +=")";
		}	
		
		return statement;
	}
	
	protected List<String> getReaders(Group group, List<String> readers) {
		
		String id = ((KbeeGroup)group).getId().toString();
	
		if (readers.contains(id)) 
			return readers;
		
		readers.add(id);
	
		for (Group parent : ((KbeeGroup)group).getGroups()) {
			readers = getReaders(parent, readers);
		}
		
		return readers;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}

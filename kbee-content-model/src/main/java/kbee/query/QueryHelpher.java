package kbee.query;

import java.util.stream.Collectors;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class QueryHelpher {
	
	public static String buildSecurityTerm(Permission permission) {
		return buildSecurityTerm(permission.toString());
	}
	
	public static String buildSecurityTerm(String permission) {
		String statement = "";
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
		
		if (!service.isRoot() && !admin && !support) {
			statement += ServiceLocator.getService(UserService.class)
				.getSessionUserPrincipals().stream()
				.map(principal -> String.valueOf(principal.getId()))
				.collect(Collectors.joining(" OR ", getField(permission)+":(", ")"));
		}
		
		return statement;
	}
	
	public static String buildManegdTerm(DataSet ds) {
		String managed = "";
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		if (service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
			service.isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId()) ||
			service.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId())) {
			return managed;
		}; 
		
		UserProfile usersessionprofile = ServiceLocator
			.getService(UserService.class)
			.getSessionUserProfile();
		
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)Proxy.Unproxy(role); 
				if (entityrole.getClassifier().getDataSet().equals(ds) && entityrole.manage(ds)) {
					if (!"".equals(managed)) managed += " ";
					String id = (new ObjectId(userrole.getEntity())).toString();
					managed += id;
				}
			}
		}
		
		if (!"".equals(managed)) {
			managed = "id:(" + managed + ")";
		}	
		
		return managed;	
	}
	
	private static String getField(String permission) {
		if ("write".equals(permission)) {
			return "writer";
		}
		if ("childs".equals(permission)) {
			return "childwriter";
		}
		if ("read".equals(permission)) {
			return "reader";
		}
		return null;
	}
}
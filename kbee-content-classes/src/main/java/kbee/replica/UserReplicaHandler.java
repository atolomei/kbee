package kbee.replica;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserRole;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiValue;
import kbee.api.model.IPerson;
import kbee.api.model.IRole;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserRole;

public class UserReplicaHandler extends ClassificableReplicaHandler<ApiUser, KbeePersonMember> {

	public UserReplicaHandler(Replica replica, ApiUser iuser) {
		super(replica, iuser);
	}
	
	@Override
	protected void replicateIn(KbeePersonMember local) throws ReplicaException {
		ApiUser remote = getObject();
		
		local.setLastName(remote.getLastName());
		local.setFirstName(remote.getFirstName());
		local.setEmail(remote.getEmail());
		
		UserProfile userprofile = local.getProfile(UserProfile.class);
		KbeeUser user = (KbeeUser)userprofile.getUser();
		
		String remotename = remote.getName();
		int i = remotename.indexOf("@");
		String localname = remotename.substring(0, i) + "@" + local.getDomain().getName();
		
		user.setUserName(localname);
		
		if (remote.isEnabled())
			user.setStateEnabled();
		else
			user.setStateArchived();
		user.setLocale(remote.getLocale());
		
		if (remote.getPassword()!=null) {
			user.setEncodedPassword(remote.getPassword());
		}
		
		if (remote.getRoles()!=null) {
			List<UserRole> userRoles = new ArrayList<>();
			for (IUserRole iUserRole : remote.getRoles()) {
				IRole irole = getReplicaApi().getRole(iUserRole.getRole().getId());
				KbeeAbstractRole role = replicated(KbeeAbstractRole.class, irole); 
				ApiValue ientity = iUserRole.getEntity()!=null ?
					getReplicaApi().get(ApiValue.class, iUserRole.getEntity().getHRef()) :
					null;
				KbeeUserRole userRole = new KbeeUserRole();
				userRole.setRole(role);
				if (ientity!=null) {
					KbeeEntityMember entity = replicated(KbeeEntityMember.class, ientity);
					userRole.setEntity(entity); 
				}		
				userRole.setUserProfile(userprofile);
				userRole.setUser(user);
				userRoles.add(userRole);
			}
			userprofile.setRoles(userRoles);
		}
			
		//syncClassifiers(remote, getMember(local), getUserSet(local.getDomain()).getClassifiers());
		
 		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}

	@Override
	protected KbeePersonMember createLocal() throws ReplicaException {
		
		ApiUser remote = getObject();
		
		// Creates PersonMember -----------------------------------------------------------
		Person member = (Person) getUserSet().createMember();
		
		IPerson iperson = getReplicaApi().getPerson(remote.getPerson().getId());
		KbeePerson person = replicated(KbeePerson.class, iperson); 
		
		((KbeePersonMember)member).setPerson(person);
		
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save(member);
				
		// Creates User -----------------------------------------------------------------
		
		KbeeUser user = new KbeeUser();
		
		String remotename = remote.getName();
		int i = remotename.indexOf("@");
		String localname = remotename.substring(0, i) + "@" + getSessionDomain().getName();
		user.setUserName(localname);
		
		user.addGroup(getGroup(KbeeGlobalRole.USER));
		
		user.setLocale(getSessionDomain().getLocale());
		user.setTimeZone(getSessionDomain().getTimeZone());
		user.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		if (getSessionDomain().getDefaultPassword()!=null)
			user.setPassword(getSessionDomain().getDefaultPassword());
		
		getContentDao().save(user);
		getContentDao().flush();

		
		// Completes UserProfile -----------------------------------------------------------------
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(user);
		userProfile.setLastModifiedUser(getSessionUser());
		userProfile.setDomain(getSessionDomain());
		userProfile.setCreationOffsetDateTime(OffsetDateTime.now());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage("home");
		userProfile.setEmailNotifications(true);
		userProfile.setTipOfTheDay(false);
		userProfile.setEditPersonEnabled(true);
		userProfile.setType(UserProfileType.EMPLOYEE);

		return (KbeePersonMember)member;
	}
	
	private Group getGroup(KbeeGlobalRole role) {
		return getContentSecurityDao().findGroupByName(role.getId(), getSessionDomain());
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}

}
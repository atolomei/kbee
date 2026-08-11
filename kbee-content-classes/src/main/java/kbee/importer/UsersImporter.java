package kbee.importer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;

 import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

import kbee.api.model.ApiProxy;
import kbee.api.model.IGroup;
import kbee.api.model.IResultSet;
import kbee.api.model.ApiUser;

@Deprecated
public class UsersImporter extends ClassificablesImporter {

	private int total = 0;
	private int updated = 0;
	
	UsersImporter(KbeeApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute() throws ContentMgmtException {
		int i=0;
		Transaction transaction = null;
		try {
			IResultSet<ApiProxy> users = getServer().getUsers();
			transaction = beginTransaction();
			while (users.hasNext()) {
				ApiProxy proxy = users.next();
//				if (!proxy.getHRef().startsWith(getDomain().getName())){
//					String href = proxy.getHRef();
//					String[] tokens = href.split("/");
//					tokens[0] = getDomain().getName();
//					href = ""; 
//					for (int t=0; t<tokens.length; t++) {
//						href += tokens[t];
//						if (t<tokens.length-1) href +="/";
//					}
//					proxy.setHRef(href);
//				}
//				if (proxy.getHRef().startsWith(getDomain().getName())){
					ApiUser remote = getServer().get(ApiUser.class, proxy.getHRef());
					Person local = getLocal(KbeePersonMember.class, remote);
					if ((local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime())) && remote.getName()!=null) {
						if (local == null) {
							local = getLocalUser(remote);
							if (local==null) {
								local = createUser(remote.getName());
							}
							setLocal(remote, local);
						}	
						syncUser(remote, local);
						update(local);
						updated++;
					 	logger.info("User "+local.getDisplayName() + " "+ local.getId());
					}
//				}
				//if (remote.getName()!=null)
				//	importAudit(remote, local);
				if (i++%2==0) {
					transaction.commit();
					transaction = beginTransaction();
				}
				setProgress(i);
			}
			transaction.commit();
		}
		catch (Throwable e) {
			e.printStackTrace();
			transaction.rollback();
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			IResultSet<ApiProxy> users = getServer().getUsers();
			total = (int)users.getSize();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" users processed. ";
		result += String.valueOf(updated)+" users updated</p>";
		return result;
	}
	
	private Person getLocalUser(ApiUser remote) {
		String remotename = remote.getName();
		int i = remotename.indexOf("@");
		String localname = remotename.substring(0, i) + "@" + getDomain().getName();
		User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername(localname);
		if (user!=null) {
 			UserProfile profile = getContentDao().findUserProfileByUser(user);
			List<DataSetMember> members = getContentDao().findMembersByEntity(profile.getEntity());
			if (members.size()!=1) return null;
			PersonMember member = (PersonMember)members.get(0);
			return member;
		}
		return null;
	}
	
	private void syncUser(ApiUser remote, Person local) {
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
		
		Set<Group> groups = new HashSet<Group>();
		
		for (ApiProxy proxy : remote.getGroups()) {
			IGroup remotegroup = getServer().get(IGroup.class, proxy.getHRef());
			KbeeGroup localgroup = getLocal(KbeeGroup.class, remotegroup);
			
			if (localgroup!=null) {
				groups.add(localgroup);
			}
		}
		
		user.setGroups(groups);
			
		syncClassifiers(remote, getMember(local), getUserSet(local.getDomain()).getClassifiers());
		
 		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	private DataSetMember getMember(Person person) {
		if (person instanceof DataSetMember) {
			return (DataSetMember)person;
		}
		else {
			List<DataSetMember> members = getContentDao().findMembersByEntity(person);
			if (members.size()!=1) return null;
			return members.get(0);
		}
	}
	
	private Person createUser(String username) throws ContentMgmtException {
		return (Person)ServiceLocator.getService(ObjectFactoryService.class).createUser(username);
	}
	
	private UserSet getUserSet(Domain domain) {
		UserSet userset= null;
		for (DataSet dataset : getContentDao().getDataSets(domain)) {
			dataset = (DataSet)getContentDao().reload(dataset);
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				userset = (UserSet)dataset;
				break;
			}
		}
		Assert.isTrue(userset!=null, "user set not found");
		return userset;
	}
	
//	private void importAudit(IUser remote, Person local) throws IOException {
//		(new LogEventsImporter(getServer(), getDomain())).execute(remote, local);
//	}
}

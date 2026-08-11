package kbee.replica;

import java.time.OffsetDateTime;

import com.novamens.content.library.Library;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IGroup;
import kbee.api.model.ILibrary;

public class LibraryReplicaHandler extends AbstractReplicaHandler<ILibrary, KbeeLibrary> {

	public LibraryReplicaHandler(Replica replica, ILibrary ilibrary) {
		super(replica, ilibrary);
	}
	
	@Override
	protected void replicateIn(KbeeLibrary local) throws ReplicaException {
		ILibrary remote = getObject();
		local.setName(remote.getDisplayName());
		local.setDisplayName(remote.getDisplayName());
		local.setKey(remote.getName());
		local.setCriteria(remote.getCriteria());
		local.setCanonical(remote.isCanonical()) ;
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		if (remote.getReaders()!=null) {
			IGroup igroup = getReplicaApi().getGroup(remote.getReaders().getId());
			Group group = getLocalGroup(igroup);
			local.setReaders(group);
		}
	}
	
	@Override
	protected KbeeLibrary createLocal() {
	UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
	
		String name = "new library";
		
		KbeeLibrary library = new KbeeLibrary();
		library.setName(name);
	
		String key = name;
		
		if (key.length()>24)
			key=key.substring(0, 23);
		
		library.setKey(key.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "-"));
		
		library.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		library.setCreationOffsetDateTime(OffsetDateTime.now());
		library.setLastModifiedUser(userProfile.getUser());
		library.setState(ObjectState.ENABLED);
		library.setDomain(getSessionDomain());
		library.setCriteria("ishead(true)");
		library.setOrder(getContentDao().getLibraries(getSessionDomain()).size());
		
		getContentDao().save((Library)library);
		
		return library;
	}
}
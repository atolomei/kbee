package kbee.replica;

import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.user.KbeeUserProfile;

import kbee.api.model.IPerson;

public class PersonReplicaHandler extends AbstractReplicaHandler<IPerson, KbeePerson> {

	public PersonReplicaHandler(Replica replica, IPerson iuser) {
		super(replica, iuser);
	}
	
	@Override
	protected void replicateIn(KbeePerson local) throws ReplicaException {
		IPerson remote = getObject();
		
 		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}

	@Override
	protected KbeePerson createLocal() {
		KbeePerson person = new KbeePerson(); 
		KbeeUserProfile userProfile = new KbeeUserProfile();
		person.addProfile(userProfile);
		getContentDao().save(person);
		return person;
	}
}
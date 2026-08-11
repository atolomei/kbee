package kbee.replica;

import java.time.OffsetDateTime;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeePersonMember;

import kbee.api.model.ApiValue;
import kbee.api.model.IPerson;

public class DataSetMemberReplicaHandler extends ClassificableReplicaHandler<ApiValue, KbeeDataSetMember> {

	public DataSetMemberReplicaHandler(Replica replica, ApiValue value) {
		super(replica, value);
	}
	
	@Override
	protected void replicateIn(KbeeDataSetMember local) throws ReplicaException {
		ApiValue remote = getObject();
		local.setStrValue(remote.getDisplayName());
		
		if (local instanceof PersonMember) {
			
			IPerson iperson = getReplicaApi().getPerson(remote.getPerson().getId());
			KbeePerson person = (KbeePerson)replicated(KbeePerson.class, iperson);
						
			((KbeePersonMember)local).setPerson(person);

			((KbeePersonMember)local).setLastName(remote.getLastName());
			((KbeePersonMember)local).setFirstName(remote.getFirstName());
			((KbeePersonMember)local).setEmail(remote.getEmail());
			
			update(((KbeePersonMember)local).getPerson());
		}
		
		if (remote.getAttributes()!=null) {
			syncClassifiers(remote, local, local.getDataSet().getClassifiers());
			syncAttributes(remote, local, local.getDataSet().getAttributes());
		}
		
		if (remote.getParent()!=null) {
			ApiValue iparent = new ApiValue();
			iparent.setId(remote.getParent().getId());
			iparent.setDomain(remote.getDomain());
			
//			DataSetMember parent = getLocal(KbeeDataSetMember.class, iparent);
//			if (parent!=null) {
//				//local.setParent(parent);
//			}
		}
		
		if (remote.getState()!=null)
			local.setState(ObjectState.valueOf(remote.getState()));
		else
			local.setState(ObjectState.ENABLED);
		
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		
		update(local);
	}

	@Override
	protected KbeeDataSetMember createLocal() throws ReplicaException {
		ApiValue remote = getObject();
		DataSet dataset = getDataSet(getObject().getDataSet());
		DataSetMember member = dataset.createMember();
		member.setDomain(dataset.getDomain());
		member.setLastModifiedUser(dataset.getLastModifiedUser());
		member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		if (member instanceof PersonMember) {
			IPerson iperson = getReplicaApi().getPerson(remote.getPerson().getId());
			KbeePerson person = (KbeePerson)replicated(KbeePerson.class, iperson);
			((KbeePersonMember)member).setPerson(person);
		}
		update(member);
		return (KbeeDataSetMember)member;
	}
}
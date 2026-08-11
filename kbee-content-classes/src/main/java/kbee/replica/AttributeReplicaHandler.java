package kbee.replica;

import com.novamens.content.model.AttributeType;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IModelAttribute;

public class AttributeReplicaHandler extends AbstractReplicaHandler<IModelAttribute, KbeeAttribute> {

	public AttributeReplicaHandler(Replica replica, IModelAttribute attribute) {
		super(replica, attribute);
	}
	
	@Override
	protected void replicateIn(KbeeAttribute local) {
		IModelAttribute remote = getObject();
		local.setName(remote.getDisplayName());
		local.setAlias(remote.getAlias());
		((KbeeAttribute)local).setType(AttributeType.valueOf(remote.getType()));
		((KbeeAttribute)local).setPredicate(remote.getPredicate());
		local.setState(ObjectState.valueOf(remote.getState()));
		((KbeeAttribute)local).setUniqueName(remote.getUniqueName());
		((KbeeAttribute)local).setFilterable(remote.isFilterable());
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	@Override
	protected KbeeAttribute createLocal() {
		return (KbeeAttribute)ServiceLocator.getService(ObjectFactoryService.class).createAttribute();
	}
}
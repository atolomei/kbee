package kbee.replica;

import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IResourceTag;

public class ResourceTagReplicaHandler extends AbstractReplicaHandler<IResourceTag, KbeeResourceTag> {

	public ResourceTagReplicaHandler(Replica replica, IResourceTag itag) {
		super(replica, itag);
	}
	
	@Override
	protected void replicateIn(KbeeResourceTag local) {
		IResourceTag remote = getObject();
		local.setAlias(remote.getName());
		local.setName(remote.getDisplayName());
		local.setMultiple(remote.isMultiple());
	}
	
	@Override
	protected KbeeResourceTag createLocal() {
		return (KbeeResourceTag)ServiceLocator.getService(ObjectFactoryService.class).createResourceTag();
	}
}
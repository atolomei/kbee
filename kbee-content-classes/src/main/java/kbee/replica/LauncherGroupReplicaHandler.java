package kbee.replica;

import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ILauncherGroup;

public class LauncherGroupReplicaHandler extends AbstractReplicaHandler<ILauncherGroup, KbeeLauncherGroup> {

	public LauncherGroupReplicaHandler(Replica replica, ILauncherGroup igroup) {
		super(replica, igroup);
	}
	
	@Override
	protected void replicateIn(KbeeLauncherGroup local) {
		ILauncherGroup remote = getObject();
		local.setAlias(remote.getName());
		local.setName(remote.getDisplayName());
	}
	
	@Override
	protected KbeeLauncherGroup createLocal() {
		return (KbeeLauncherGroup)ServiceLocator.getService(ObjectFactoryService.class).createLauncherGroup("");
	}
}
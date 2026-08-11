package kbee.replica;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IGroup;

public class GroupReplicaHandler extends AbstractReplicaHandler<IGroup, KbeeGroup> {

	public GroupReplicaHandler(Replica replica, IGroup igroup) {
		super(replica, igroup);
	}
	
	@Override
	protected void replicateIn(KbeeGroup local) {
		IGroup remote = getObject();
		local.setName(remote.getName());
		((KbeeGroup)local).setCanonical(remote.isCanonical());
		((KbeeGroup)local).setAreaCode(remote.getArea());
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	@Override
	protected KbeeGroup createLocal() {
		KbeeGroup local = null;
		IGroup remote = getObject();
		if (remote.isCanonical()) {
			local = (KbeeGroup)getCanonical(remote.getName());
		}
		if (local == null) {
			local = (KbeeGroup)createGroup();
		}
		return local;
	}
	
	private Group getCanonical(String groupname) {
		if ("Domain Admin".equals(groupname))
			groupname = "domain-admin";
		if ("Content Base".equals(groupname))
			groupname = "library";
		if ("Information Model".equals(groupname))
			groupname = "information-model";
		if ("Datasets Members".equals(groupname))
			groupname = "dataset-values";
		for (Group group : getContentSecurityDao().getGroups(getSessionDomain())) {
			if (group.isCanonical() && groupname.toLowerCase().equals(group.getName().toLowerCase())) {
				return group;
			}
		}
		return null;
	}
	
	private Group createGroup() throws ContentCreationException {
		return ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
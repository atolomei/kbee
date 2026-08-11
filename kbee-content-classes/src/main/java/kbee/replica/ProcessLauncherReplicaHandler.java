package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.IAcl;
import kbee.api.model.IAclEntry;
import kbee.api.model.IGroup;
import kbee.api.model.ILauncher;
import kbee.api.model.ILauncherGroup;

public class ProcessLauncherReplicaHandler extends AbstractReplicaHandler<ILauncher, KbeeProcessLauncher> {

	public ProcessLauncherReplicaHandler(Replica replica, ILauncher ilauncher) {
		super(replica, ilauncher);
	}
	
	@Override
	protected void replicateIn(KbeeProcessLauncher local) throws ReplicaException {
		ILauncher remote = getObject();
		local.setLabel(remote.getDisplayName());
		local.setEnabled(remote.isNewDocumentEnabled());
		local.setDescription(remote.getDescription());
		local.setLibrary(remote.isLibraryEnabled());
		Acl acl = local.getAcl();
		replicateAcl(remote.getAcl(), acl);
		if (remote.getGroup()!=null) {
			local.setLauncherGroup(getLauncherGroup(remote.getGroup()));
		}
	}

	@Override
	protected KbeeProcessLauncher createLocal() {
		//ITemplate itemplate = getReplicaApi().getTemplate(getObject().getTemplate().getId());
		//ContentTemplate localtemplate = getLocal(KbeeContentTemplate.class, itemplate);
		return (KbeeProcessLauncher)ServiceLocator.getService(ObjectFactoryService.class).createLauncher(null);
	}
	
	private void replicateAcl(IAcl remote, Acl local) throws ReplicaException {
		try {
			List<AclEntry> entries = ((KbeeAcl)local).getEntries(); 
			while (!entries.isEmpty()) {
				for (AclEntry entry : entries) {
					local.removeEntry(getSessionUser(), (KbeeAclEntry)entry);
					break;
				}
				entries = ((KbeeAcl)local).getEntries(); 
			}
			if (remote.getEntries()!=null)
			for (IAclEntry entry : remote.getEntries()) {
				KbeeAclEntry localentry = new KbeeAclEntry();
				IGroup igroup = getReplicaApi().getGroup(entry.getPrincipal().getId());
				Group group = getLocalGroup(igroup);
				if (group!=null) {
					List<Permission> permissions = new ArrayList<Permission>();
					for (String permissionvalue : entry.getPermissions()) {
						Permission permission = KbeePermission.valueOf(permissionvalue);
						permissions.add(permission);
					}
					localentry.setPrincipal(group);
					localentry.setPermissions(permissions);
					local.addEntry(getSessionUser(), localentry);
				}
			}
		}
		catch (Exception e) {
			
		}
	}
	
	private LauncherGroup getLauncherGroup(ApiProxy proxy) {
		ILauncherGroup remote = getReplicaApi().getLauncherGroup(proxy.getId());
		LauncherGroup local = getLocal(KbeeLauncherGroup.class, remote);
		return local;
	}
}
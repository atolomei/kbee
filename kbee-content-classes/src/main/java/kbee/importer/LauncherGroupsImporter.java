package kbee.importer;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ILauncherGroup;
import kbee.api.service.ApiService;

@Deprecated
public class LauncherGroupsImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public LauncherGroupsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			for (ILauncherGroup remote : getRemoteLaunchersGroups()) {
				KbeeLauncherGroup local = getLocal(KbeeLauncherGroup.class, remote);
				if (local==null || forceUpdate()) {
					if (local == null) {
						local = createLauncherGroup();
						setLocal(remote, local);
					}
					syncLauncherGroup(remote, local);
					update(local);
					updated++;
					logger.info("Launcher Group "+local.getDisplayName());
				}
				else {
					logger.info("Launcher Group "+local.getDisplayName() + " not modified");
				}
				setProgress(++i);
			}
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteLaunchersGroups().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" LauncherGroup processed. ";
		result += String.valueOf(updated)+" LauncherGroup updated</p>";
		return result;
	}
	
	private void syncLauncherGroup(ILauncherGroup remote, KbeeLauncherGroup local) throws ContentMgmtException {
		local.setAlias(remote.getName());
		local.setName(remote.getDisplayName());
	}
	
	private KbeeLauncherGroup createLauncherGroup() throws ContentCreationException {
		return (KbeeLauncherGroup)ServiceLocator.getService(ObjectFactoryService.class).createLauncherGroup("");
	}
	
	private List<ILauncherGroup> getRemoteLaunchersGroups() {
		return getServer().getLauncherGroups();
	}
}
package kbee.importer;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;

import kbee.api.model.ISettings;
import kbee.api.service.ApiService;

public class SettingsImporter extends Importer {
	
	private int total = 0;

	public SettingsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute() throws ContentMgmtException  {
		try {
			ISettings settings = getRemoteSettings();
			Domain local =  getSessionDomain();
			local = (Domain) getContentDao().reload(local);
			syncSettings(settings, local);
			update(local);
		}
		catch (Throwable e) {
			e.printStackTrace();
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	@Override
	public int getTotal() {
		return total;
	}

	@Override
	public String getResult() {
		String result = "settings updated";
		return result;
	}
	
	private void syncSettings(ISettings remote, Domain local) {
		local.setQuota(remote.getQuota());
		local.setMaxUsers(remote.getMaxUsers());
	}
	
	private ISettings getRemoteSettings() {
		return getServer().getSettings();
	}
}
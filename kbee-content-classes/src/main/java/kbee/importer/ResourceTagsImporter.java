package kbee.importer;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IResourceTag;
import kbee.api.service.ApiService;

public class ResourceTagsImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public ResourceTagsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			for (IResourceTag remote : getRemoteResourceTags()) {
				KbeeResourceTag local = getLocal(KbeeResourceTag.class, remote);
				if (local==null || forceUpdate()) {
					if (local == null) {
						local = createResourceTag();
						setLocal(remote, local);
					}
					syncResourceTag(remote, local);
					update(local);
					updated++;
					logger.info("Resource Tag "+local.getDisplayName());
				}
				else {
					logger.info("Resource Tag "+local.getDisplayName() + " not modified");
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
			total = getRemoteResourceTags().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" ResourceTags processed. ";
		result += String.valueOf(updated)+" ResourceTags updated</p>";
		return result;
	}
	
	private void syncResourceTag(IResourceTag remote, KbeeResourceTag local) throws ContentMgmtException {
		local.setAlias(remote.getName());
		local.setName(remote.getDisplayName());
		local.setMultiple(remote.isMultiple());
	}
	
	private KbeeResourceTag createResourceTag() throws ContentCreationException {
		return (KbeeResourceTag)ServiceLocator.getService(ObjectFactoryService.class).createResourceTag();
	}
	
	private List<IResourceTag> getRemoteResourceTags() {
		return getServer().getResourceTags();
	}
}
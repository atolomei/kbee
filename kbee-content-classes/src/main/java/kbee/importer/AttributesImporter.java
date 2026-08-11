package kbee.importer;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IModelAttribute;
import kbee.api.service.ApiService;

public class AttributesImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public AttributesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
 			for (IModelAttribute remote : getRemoteAttributes()) {
				Attribute local = getLocal(KbeeAttribute.class, remote);
				if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
					if (local == null) {
						local = createAttribute();
						setLocal(remote, local);
					}
					syncAttribute(remote, local);
					update(local);
					updated++;
					logger.info("Attribute "+local.getDisplayName());
				}
				else {
					logger.info("Attribute "+local.getDisplayName() + " not modified");
				}
				setProgress(++i);
			}
			getContentDao().flush();
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteAttributes().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" attributes processed. ";
		result += String.valueOf(updated)+" attributes updated</p>";
		return result;
	}
	
	private void syncAttribute(IModelAttribute remote, Attribute local) throws ContentMgmtException {
		local.setName(remote.getDisplayName());
		local.setAlias(remote.getAlias());
		((KbeeAttribute)local).setType(AttributeType.valueOf(remote.getType()));
		((KbeeAttribute)local).setPredicate(remote.getPredicate());
		local.setState(ObjectState.valueOf(remote.getState()));
		((KbeeAttribute)local).setUniqueName(remote.getUniqueName());
		((KbeeAttribute)local).setFilterable(remote.isFilterable());
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	private Attribute createAttribute() throws ContentCreationException {
		return (Attribute)ServiceLocator.getService(ObjectFactoryService.class).createAttribute();
	}
	
	private List<IModelAttribute> getRemoteAttributes() {
		return getServer().getAttributes();
	}
}

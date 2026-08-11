package kbee.importer;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExternalSet;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiDataSet;
import kbee.api.service.ApiService;

public class DataSetsImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;

	public DataSetsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute() throws ContentMgmtException  {
		int i = 0;
		try {
			for (ApiDataSet remote : getRemoteDataSets()) {
				DataSet local = getLocal(KbeeDataSet.class, remote);
				if ((local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) 
						&& !"USER".equals(remote.getType())) {
					if (local == null) {
						local = createDataSet(DataSetType.valueOf(remote.getType()));
						setLocal(remote, local);
					}
					syncDataSet(remote, local);
					update(local);
					updated++;
					setProgress(i);
					logger.info("DataSet "+local.getDisplayName());
				}
				else {
					logger.info("DataSet "+remote.getDisplayName() + " not modified");
				}
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
			total = getRemoteDataSets().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" datasets processed. ";
		result += String.valueOf(updated)+" datasets updated</p>";
		return result;
	}
	
	private DataSet createDataSet(DataSetType type) throws ContentCreationException {
		return (DataSet)ServiceLocator.getService(ObjectFactoryService.class).createDataSet(type);
	}
	
	private void syncDataSet(ApiDataSet remote, DataSet local) {
		local.setName(remote.getDisplayName()); 
		((KbeeDataSet)local).setAlias(remote.getAlias()); 
		local.setState(ObjectState.valueOf(remote.getState()));
		((KbeeDataSet)local).setDisplayNameEditable(remote.isDisplayNameEditable());
		((KbeeDataSet)local).setDisplayNameTemplate(remote.getDisplayNameRule());
		((KbeeDataSet)local).setSublineTemplate(remote.getSublineRule());
		((KbeeDataSet)local).setHierachical(remote.isHierachical());
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		if (local instanceof ExternalSet) {
			if (remote.getSubtype()!=null) {
				((KbeeExternalSet)local).setExternalSubtype(Integer.valueOf(remote.getSubtype()));
			}
		}
	}
	
	private List<ApiDataSet> getRemoteDataSets() {
		return getServer().getDataSets();
	}
}
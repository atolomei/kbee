package kbee.importer;


import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeSecuredSet;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.service.ApiService;

public class ClassifiersImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public ClassifiersImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			for (ApiClassifier remote : getRemoteClassifiers()) {
				Classifier local = getLocal(KbeeClassifier.class, remote);
				DataSet localdataset = remote.getDataSet()!=null ? getDataSet(remote.getDataSet()) : null;
				if (localdataset!=null && (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate())) {
					if (local == null) {
						local = createClassifier();
						setLocal(remote, local);
					}
					syncClassifier(remote, local);
					update(local);
					updated++;
					logger.info("Classifier "+local.getDisplayName());
				}
				else { 
					if (local!=null)
					logger.info("Classifier "+local.getDisplayName() + " not modified");
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
			total = getRemoteClassifiers().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" classifiers processed. ";
		result += String.valueOf(updated)+" classifiers updated</p>";
		return result;
	}
	
	private void syncClassifier(ApiClassifier remote, Classifier local) throws ContentMgmtException {
		KbeeClassifier kblocal = (KbeeClassifier)local;
		local.setName(remote.getDisplayName());
		local.setAlias(remote.getAlias());
		kblocal.setMultiplicity(Multiplicity.valueOf(remote.getMultiplicity()));
		kblocal.setUniqueName(remote.getUniqueName());
		if (local.getDataSet()==null)
		local.addDataSet(getDataSet(remote.getDataSet()));
		if (remote.getDataSet2()!=null) {
			((KbeeClassifier)local).setDataSet2(getDataSet(remote.getDataSet2()));
		}
		kblocal.setPredicate(remote.getPredicate());
		kblocal.setRuleCondition(remote.isRules());
		kblocal.setContentType(remote.isContentType());
		kblocal.setSearchable(remote.isSearchable());
		
		if (DataSetType.SECURED.equals(local.getDataSet().getDataSetType())) {
			((KbeeSecuredSet)local.getDataSet()).setClassifier(local);
			update(local.getDataSet());
		}
		
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	private DataSet getDataSet(ApiProxy proxy) {
		ApiDataSet remote = getServer().getDataSet(proxy.getId());
		DataSet local = getLocal(KbeeDataSet.class, remote);
		return local;
	}
	
	private Classifier createClassifier() throws ContentCreationException {
		return (Classifier)ServiceLocator.getService(ObjectFactoryService.class).createClassifier();
	}
	
	private List<ApiClassifier> getRemoteClassifiers() {
		return getServer().getClassifiers();
	}
}

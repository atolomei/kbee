package kbee.replica;

import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeSecuredSet;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiClassifier;

public class ClassifierReplicaHandler extends AbstractReplicaHandler<ApiClassifier, KbeeClassifier> {

	public ClassifierReplicaHandler(Replica replica, ApiClassifier iclassifier) {
		super(replica, iclassifier);
	}
	
	@Override
	protected void replicateIn(KbeeClassifier local) throws ReplicaException {
		ApiClassifier remote = getObject();
		local.setName(remote.getDisplayName());
		local.setAlias(remote.getAlias());
		local.setMultiplicity(Multiplicity.valueOf(remote.getMultiplicity()));
		local.setUniqueName(remote.getUniqueName());
		if (local.getDataSet()==null)
		local.addDataSet(getDataSet(remote.getDataSet()));
		if (remote.getDataSet2()!=null) {
			((KbeeClassifier)local).setDataSet2(getDataSet(remote.getDataSet2()));
		}
		local.setPredicate(remote.getPredicate());
		local.setRuleCondition(remote.isRules());
		local.setContentType(remote.isContentType());
		local.setSearchable(remote.isSearchable());
		if (DataSetType.SECURED.equals(local.getDataSet().getDataSetType())) {
			((KbeeSecuredSet)local.getDataSet()).setClassifier(local);
			update(local.getDataSet());
		}
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());		
	}
	
	@Override
	protected KbeeClassifier createLocal() {
		return (KbeeClassifier)ServiceLocator.getService(ObjectFactoryService.class).createClassifier();
	}
}
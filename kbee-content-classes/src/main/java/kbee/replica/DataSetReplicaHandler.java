package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExternalSet;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiDataSet;
import kbee.api.model.IModelElement;

public class DataSetReplicaHandler extends AbstractReplicaHandler<ApiDataSet, KbeeDataSet> {

	public DataSetReplicaHandler(Replica replica, ApiDataSet idataset) {
		super(replica, idataset);
	}
	
	@Override
	protected void replicateIn(KbeeDataSet local) throws ReplicaException {
		ApiDataSet remote = getObject();
		local.setName(remote.getDisplayName()); 
		local.setAlias(remote.getAlias()); 
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
		
		List<DataSetElementTemplate> structure = new ArrayList<>();
		for (IModelElement itemplate : remote.getStructure()) {
			ModelElement element = null;
			KbeeDataSetElementTemplate template = new KbeeDataSetElementTemplate();
			if (CLASSIFIER_REL.equals(itemplate.getAttribute().getRel())) {
				element = getClassifier(itemplate.getAttribute());
				if (element!=null) {
					template.setClassifier((Classifier)element);
				}	
			}
			else {
				element = getAttribute(itemplate.getAttribute());
				if (element!=null) {
					template.setAttribute((Attribute)element);
				}	
			}
			if (element!=null) {
				template.setMultiplicity(Multiplicity.valueOf(itemplate.getMutiplicity()));
				structure.add(template);
			}
			else {
				logger.warn(itemplate.getAttribute().getName() + " not found");
			}
		}
		local.setStructure(structure);
	}

	@Override
	protected KbeeDataSet createLocal() {
		return (KbeeDataSet)ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.valueOf(getObject().getType()));
	}
	
	@Override
	protected boolean replicable (KbeeDataSet local) {
		return !"USER".equals(getObject().getType()) && super.replicable(local);
	}
}

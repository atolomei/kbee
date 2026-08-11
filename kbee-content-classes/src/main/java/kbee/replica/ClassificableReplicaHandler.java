package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiClassificable;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IModelAttribute;
 
public class ClassificableReplicaHandler<T extends ApiClassificable, L extends Classificable> extends AbstractReplicaHandler<T, L> {

	public ClassificableReplicaHandler(Replica replica, T iclassificable) {
		super(replica, iclassificable);
	}

	protected void syncClassifiers(ApiClassificable remote, Classificable local, List<Classifier> classifiers) throws ReplicaException {
		if (remote.getAttributes()!=null)
		for (IAttributeValues values : remote.getAttributes()) {
			Classifier classifier = getClassifier(values.getAttribute());
			if (classifier!=null) {
				List<DataSetMember> members = new ArrayList<DataSetMember>();
				for (ApiValue remotevalue : values.getValues()) {
					remotevalue.setDomain(remote.getDomain());
					DataSetMember localvalue = getLocal(KbeeDataSetMember.class, remotevalue);
					if (localvalue!=null) {
						members.add(localvalue);
					}
					else {
						localvalue = replicated(KbeeDataSetMember.class, remotevalue);
						if (localvalue!=null) {
							members.add(localvalue);
						}
						else {
							logger.warn("value " +remotevalue.getName() + " not found");
						}
					}
				}
				if (!members.isEmpty()) {
					local.setClassification(classifier, members);
					update(local);
				}	
			}
			else {
				logger.warn("attribute "+ values.getAttribute().getId() + " not found");
			}
		}
	}
	
	protected void syncAttributes(ApiClassificable remote, Classificable local, List<AttributeTemplate> attributes) throws ReplicaException {
		if (remote.getAttributes()!=null)
		for (IAttributeValues values : remote.getAttributes()) {
			Attribute attribute = getAttribute(values.getAttribute());
			if (attribute!=null) {
				List<String> stringvalues = new ArrayList<String>();
				for (ApiValue remotevalue : values.getValues()) {
					stringvalues.add(remotevalue.getDisplayName());
				}
				local.setAttributeValues(attribute, stringvalues);
			}
			else {
				logger.warn("attribute "+ values.getAttribute().getId() + " not found");
			}
		}
	}
	
	private Classifier getClassifier(ApiAttributeProxy attribute) throws ReplicaException {
		if (CLASSIFIER_REL.equals(attribute.getRel())) {
			ApiClassifier remoteclassifier = getReplicaApi().getClassifier(attribute.getId());
			if (remoteclassifier!=null) {
				Classifier local = getLocal(KbeeClassifier.class, remoteclassifier);
				if (local == null) {
					local = replicated(KbeeClassifier.class, remoteclassifier);
				}
				return local;
			}
		}
		return null;
	}
	
	private Attribute getAttribute(ApiAttributeProxy attribute) throws ReplicaException {
		if (ATTRIBUTE_REL.equals(attribute.getRel())) {
			IModelAttribute remoteattribute = getReplicaApi().getAttribute(attribute.getId());
			if (remoteattribute!=null) {
				 Attribute local = getLocal(KbeeAttribute.class, remoteattribute);
				if (local == null) {
					local = replicated(KbeeAttribute.class, remoteattribute);
				}
				return local;
			}
		}
		return null;
	} 
}
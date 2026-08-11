package com.novamens.kbee.content.webapi.type;

import com.novamens.content.model.Classifier;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiClassifier;

public class IClassifierAdapter implements Adapter<Classifier, ApiClassifier> {
	
	public IClassifierAdapter() {
	}
	
	public ApiClassifier adapt(Classifier classifier) {
		ApiClassifier iclassifier = new ApiClassifier();
		iclassifier.setDisplayName(classifier.getDisplayName());
		iclassifier.setAlias(classifier.getAlias());
		iclassifier.setDomain(classifier.getDomain().getName());
		iclassifier.setState(classifier.getState().name());
		iclassifier.setId(String.valueOf(classifier.getId()));
		iclassifier.setDataSet(new ApiProxy(String.valueOf(classifier.getDataSet().getId()), classifier.getDataSet().getDisplayName(), UriHelper.getUri(classifier.getDataSet()), "dataset"));
		iclassifier.setMultiplicity(String.valueOf(classifier.getMultiplicity().name()));
		iclassifier.setUniqueName(classifier.getUniqueName());
		iclassifier.setPredicate(classifier.getPredicate());
		iclassifier.setRules(classifier.isRuleCondition());
		iclassifier.setContentType(classifier.isContentType());
		iclassifier.setSearchable(classifier.isSearchable());
		iclassifier.setLastModifiedDate(classifier.getLastModifiedOffsetDateTime());
		iclassifier.setLastModifiedUser(new ApiUserProxy(classifier.getLastModifiedUser()));
		if (classifier.getDataSet2()!=null) {
			iclassifier.setDataSet2(new ApiProxy(String.valueOf(classifier.getDataSet2().getId()), classifier.getDataSet2().getDisplayName(), UriHelper.getUri(classifier.getDataSet2()), "dataset"));
		}
		return iclassifier;	
	}
}
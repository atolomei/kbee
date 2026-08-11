package com.novamens.kbee.portal.service;

import java.util.Map;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.security.acl.KbeePermission;

import kbee.query.QueryHelpher;

public class MonitorSearchSuggestionService extends SearchSuggestionService {
	
	public MonitorSearchSuggestionService() {
	}
	
	public MonitorSearchSuggestionService(Domain domain) {
		super(domain);
	}

	@Override
	protected String getStatement(String pattern, Map<String, Object> parameters) {
		return getStatement(pattern);
	}

	@Override
	protected String getStatement(String pattern) {
		String solrstatement;
		String securityStatement = QueryHelpher.buildSecurityTerm(KbeePermission.READ);
		if ("".equals(pattern)) {
			solrstatement = "";
			if (!"".equals(securityStatement)) solrstatement += securityStatement + " AND ";
		}
		else {
			solrstatement = "("+ pattern + " OR " + pattern+"*)" + " AND (((type:idoc OR type:text) AND inworkspace:true";
			if (!"".equals(securityStatement)) solrstatement += " AND " + securityStatement;
			solrstatement += ") OR type:datasetmember^4) AND ";
		}
		solrstatement += " domain:" +String.valueOf(getDomain().getId());
		return solrstatement;
	}
	
	@Override
	protected boolean isVisible(DataSet dataset) {
		return dataset.isSuggester() || dataset.getDataSetType().equals(DataSetType.USER);
	}
	
	@Override
	protected boolean isVisible(Facet facet) {
		if (facet instanceof ClassifierFacet) {
			ClassifierFacet  classifierfacet = (ClassifierFacet)facet;
			Classifier classifier = classifierfacet.getClassifier();
			if (classifier!=null) {
				return classifier.isVisible("monitor");
			}
			else
				return true;
		}
		else
		if (facet instanceof ClassifierHierarchicalFacet) {
			ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet;
			Classifier classifier = classifierfacet.getClassifier();
			if (classifier!=null) {
				return classifier.isVisible("monitor");
			}
			else
				return true;
		}
		return true;
	}
	
	@Override
	protected String getLabel(DataSet dataset) {
		return "Workspace";
	}
}

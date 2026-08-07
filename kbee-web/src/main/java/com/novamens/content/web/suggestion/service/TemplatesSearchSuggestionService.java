package com.novamens.content.web.suggestion.service;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.dom.Domain;
import com.novamens.kbee.portal.service.SearchSuggestionService;

public class TemplatesSearchSuggestionService extends SearchSuggestionService {
	
	public TemplatesSearchSuggestionService() {
	}
	
	public TemplatesSearchSuggestionService(Domain domain) {
		super(domain);
	}

	@Override
	protected String getStatement(String pattern) {
		String solrstatement;
		if ("".equals(pattern)) {
			solrstatement = "";
		}
		else {
			solrstatement = "("+ pattern + " OR " + pattern+"*)" + " AND (((type:idoc OR type:text) AND istemplate:true AND head:true AND state:1) OR type:datasetmember^4) AND ";
		}
		solrstatement += " domain:" +String.valueOf(getDomain().getId());
		return solrstatement;
	}
	
	@Override
	protected boolean isVisible(DataSet dataset) {
		return dataset.isSuggester() || dataset.getDataSetType().equals(DataSetType.USER);
	}
	
	@Override
	protected String getLabel(DataSet dataset) {
		return "Templates";
	}
}

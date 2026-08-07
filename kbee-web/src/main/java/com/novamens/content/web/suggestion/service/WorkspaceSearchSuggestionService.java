package com.novamens.content.web.suggestion.service;

import java.util.Map;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.dom.Domain;
import com.novamens.kbee.portal.service.SearchSuggestionService;

public class WorkspaceSearchSuggestionService extends SearchSuggestionService {
	
	public WorkspaceSearchSuggestionService() {
	}
	
	public WorkspaceSearchSuggestionService(Domain domain) {
		super(domain);
	}

	@Override
	protected String getStatement(String pattern, Map<String, Object> parameters) {
		String solrstatement;
		String userid = (String)parameters.get("user");
		if ("".equals(pattern)) {
			solrstatement = "";
		}
		else {
			solrstatement = "("+ pattern + " OR " + pattern+"*)" + " AND (((type:idoc OR type:text) AND workspace:"+userid+") OR type:datasetmember^4) AND ";
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
		return "Workspace";
	}
}

package kbee.web.search.service;

import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.kbee.portal.service.SearchSuggestionService;

public class ParametricSearchSuggestionService extends SearchSuggestionService {
	
	public ParametricSearchSuggestionService() {
	}
	
	public ParametricSearchSuggestionService(Domain domain) {
		super(domain);
	}
	
	@Override
	protected String getStatement(String pattern, Map<String, Object> parameters) {
		String statement = super.getStatement(pattern, parameters);
		if (parameters!=null)
		for (String key : parameters.keySet()) {
			String value = (String)parameters.get(key);
			String term = "";
			if (value.startsWith("[")) {
				value = value.substring(1, value.length()-1);
				String values[] =value.split(",");
				term ="(";
				int i = 0;
				for (String option : values) {
					if (i>0) 
						term += " OR ";
					term += key+":"+option.trim();
					i++;
				}
				term += ")";
				statement += " AND " + term;
			}
			else {
				statement += " AND " + key +":" + value;
			}
		}
		return statement;
	}
}

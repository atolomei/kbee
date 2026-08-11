package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.query.Suggestion;

import kbee.api.model.ISuggestion;

public class ISuggestionAdapter implements Adapter<Suggestion, ISuggestion> {
	
	//private static Logger logger = Logger.getLogger(IPersonAdapter.class.getName());
	
	public ISuggestionAdapter() {
	}

	public ISuggestion adapt(Suggestion suggestion) {
		
		ISuggestion isuggestion = new ISuggestion();
		
		if (suggestion.getObject() instanceof Content) {
			Content content = (Content)suggestion.getObject();
			String displayNane = content.getTitle();
			String subline = content.getService(ContentService.class).getConsoleSubtitle();
			String id = String.valueOf(content.getId());
			String href = UriHelper.getUri(content, true);
			isuggestion.setDisplayName(displayNane);
			isuggestion.setId(id);
			isuggestion.setSubline(subline);
			isuggestion.setHref(href);
		}
		
		return isuggestion;	
	}
	
}

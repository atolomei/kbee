package com.novamens.kbee.content.webapi.service;

import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.portal.service.KbeeSuggestion;
import com.novamens.portal6.model.Site;

public class SiteSearchSuggestionService extends com.novamens.kbee.portal.service.SiteSearchSuggestionService {
	
	public SiteSearchSuggestionService() {
	}
	
	public SiteSearchSuggestionService(Site site) {
		super(site);
	}
	
	@Override
	protected Suggestion createSuggestion(Object object, String label, String facet, float score) {
		return new KbeeSuggestion(object, label, score, false);
	}
}	
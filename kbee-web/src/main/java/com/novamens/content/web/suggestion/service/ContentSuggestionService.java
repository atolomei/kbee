package com.novamens.content.web.suggestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.model.ObjectModel;

public class ContentSuggestionService implements SuggestionService {
			
	private Domain domain;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentSuggestionService.class.getName());
	
	public static String DOCUMENT_ID_FIELD = "id";

	public ContentSuggestionService() {
	}
	
	public ContentSuggestionService(Domain domain) {
		this.domain = domain;
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}

	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		try {
			String solrfilterstatement;
			String solrstatement;
			String sortfield;
			
			if ("".equals(pattern)) {
				solrstatement = "";
				sortfield = "title";
			}
			else {
				solrstatement = "("+ pattern + " OR " + pattern+"*)" + " AND (type:idoc OR type:text) ";
				sortfield = null;
			}
			solrfilterstatement = " domain:" +String.valueOf(getDomain().getId());
			solrfilterstatement += " AND head:true AND state:1";
			QueryResponse response = ((SolrIndex)getIndex()).select(solrstatement, solrfilterstatement, sortfield, true, 0, 25, false, false, null);
			SolrDocumentList results = response.getResults();
			for (int i=0; i<results.size(); i++) {
				SolrDocument solrdocument = results.get(i);
				IndexerDocument document = new IndexerDocument();
				Object documentId = solrdocument.getFieldValue(DOCUMENT_ID_FIELD);
				document.setId(documentId.toString());
				for (String field : solrdocument.getFieldNames()) {
					document.addField(field, solrdocument.getFieldValue(field).toString());
				}
				IModel<Content> model = new ObjectModel<Content>((Content)((SolrIndex)getIndex()).getObjectBuilder().build(document));
				WebSuggestion suggestion = new WebSuggestion(model, model.getObject().getTitle(), 0, false);
				suggestions.add(suggestion);
			}
		}
		catch (IndexerException e) {
			logger.error(e);
		}
		
		return suggestions;
	}
	
	public Index getIndex() {
		Index index = getDomain().getService(JavaIndexerService.class).getIndex();
		if (index instanceof IndexProxy) index = ((IndexProxy)index).getIndex();
		return index;
	}
	
	public Domain getDomain() {
		return domain;
	}
}

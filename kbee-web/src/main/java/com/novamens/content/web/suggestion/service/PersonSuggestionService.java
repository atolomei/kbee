package com.novamens.content.web.suggestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.SystemSuggestionService;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.WebSuggestion;

public class PersonSuggestionService implements SystemSuggestionService {
	
	public class ProxyModel implements IModel<Person> {
		private static final long serialVersionUID = 1L;
		private PersonProxy proxy;
		public ProxyModel(PersonProxy proxy) {
			this.proxy = proxy;
		}
		public Person getObject() {
			return proxy.getObject();
		}
		public PersonProxy getProxy() {
			return proxy;
		}
		public void setObject(Person principal) {
		}
		public void detach() {
		}
	}
	
	public PersonSuggestionService() {
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}
	
	@SuppressWarnings("unchecked")
	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		List<Group> groups = null;
		
		
		if (parameters!=null && parameters.get("groups")!=null && parameters.get("groups") instanceof List<?>) {
			List<?> list = (List<?>)parameters.get("groups");
			if (!list.isEmpty() && list.get(0) instanceof Group) {
				groups = (List<Group>)list;
			}
		}
		
		boolean active = parameters!=null && parameters.get("active")!=null && "true".equals(parameters.get("active"));
		
		QueryResponse response = null;
		try {
			TextQuery query = new TextQuery(getStatement(pattern, groups, active));
			query.setFaceted(false);
			if ("".equals(pattern)) {
				query.setPageSize(400);
				query.setSortField("title_sort");
				query.setAscending(true);
			}
			else {
				query.setPageSize(50);
			}
			response = (QueryResponse)getIndex().execute(query);
		}
		catch (IndexerException e) {
			throw new RuntimeException(e);
		}
		
		SolrDocumentList results = response.getResults();
		
		for (int r=0; r<results.size(); r++) {
			SolrDocument solrdocument = results.get(r);
			float score = Float.valueOf(solrdocument.getFieldValue("score").toString());
			
			WebSuggestion newsuggestion = null;
			
			Person person = new PersonProxy(new ObjectId(solrdocument.getFieldValue("id")));
			((PersonProxy)person).setDisplayName(solrdocument.getFieldValue("title").toString());
			
			String suggestionlabel = person.getDisplayName();
			
			boolean outstanding = score > 3.5 ? true : false;
			
			if (person!=null) {
				newsuggestion = new WebSuggestion(new ProxyModel((PersonProxy)person), suggestionlabel, score, outstanding);
				suggestions.add(newsuggestion);		
			}
		}
		if (!suggestions.isEmpty()) {
			boolean os = false;
			for (Suggestion s : suggestions) {
				if (s.isOutstanding())
					os = true;
				else {
					if (!s.isOutstanding() && os) {
						((WebSuggestion)s).setCssClass("tope");
						break;
					}
				}
			}
		}	
		return suggestions;
	}
	
	private DataSet getUserSet() {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				return dataset;
			}
		}
		return null;
	}
	
	private String getStatement(String pattern, List<Group> groups, boolean active) {
		String solrstatment = "";
		
		if (!"".equals(pattern) && pattern!=null)
			solrstatment += "("+ pattern + " OR " + pattern+"*) AND ";
		solrstatment += "(type:datasetmember AND ";
		solrstatment += "(dataset:" + String.valueOf(getUserSet().getId()) + ")) AND ";
		solrstatment += "state:"+String.valueOf(ObjectState.ENABLED.getId())+" AND ";
		if (active)	solrstatment += "active:true AND ";
		solrstatment += "domain:"+String.valueOf(getDomain().getId());
		
		if (groups!=null && !groups.isEmpty()) {
			solrstatment += " AND (";
			int g = 0;
			for (Group group : groups) {
				if (g++>0)
				solrstatment += " OR ";
				solrstatment += " groupmember:"+String.valueOf(group.getId());
			}
			solrstatment += " )";
		}
		
		return solrstatment;
	}
	
	private JavaIndex getIndex() {
		return (JavaIndex)getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

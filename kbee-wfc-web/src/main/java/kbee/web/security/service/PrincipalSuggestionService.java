package kbee.web.security.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.SystemSuggestionService;
import com.novamens.kbee.content.security.PrincipalProxy;
import com.novamens.security.Principal;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.WebSuggestion;

public class PrincipalSuggestionService implements SystemSuggestionService {
	
	public class ProxyModel implements IModel<Principal> {
		private static final long serialVersionUID = 1L;
		private PrincipalProxy proxy;
		public ProxyModel(PrincipalProxy proxy) {
			this.proxy = proxy;
		}
		public Principal getObject() {
			return proxy.getObject();
		}
		public void setObject(Principal principal) {
		}
		public void detach() {
		}
	}

	public PrincipalSuggestionService() {
	}

	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		return null;
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		
		QueryResponse response = null;
		try {
			TextQuery query = new TextQuery(getStatement(pattern));
			query.setFaceted(false);
			if ("".equals(pattern)) {
				query.setPageSize(400);
				query.setSortField("title");
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
		
		//int s = 0;
		for (int r=0; r<results.size(); r++) {

			
			SolrDocument solrdocument = results.get(r);
			float score = Float.valueOf(solrdocument.getFieldValue("score").toString());
			//if (score>0.1) {
				//s++;
				WebSuggestion newsuggestion = null;
				
				String type = solrdocument.getFieldValue("type").toString();
				Principal principal = null;
				String suggestionlabel = null;
			
				if (type.equals("group")) {
					//principal = (Principal)getContentDao().findObjectById(new ObjectId(solrdocument.getFieldValue("id"))) ;
					principal = new PrincipalProxy(new ObjectId(solrdocument.getFieldValue("id")));
					((PrincipalProxy)principal).setDisplayName(solrdocument.getFieldValue("title").toString());
					((PrincipalProxy)principal).setType(type);
					suggestionlabel = principal !=null ? principal.getDisplayName() + " (group)" : "err";
				}
				else {
					//Person person = (Person)getContentDao().findObjectById(new ObjectId(solrdocument.getFieldValue("id"))) ;
					//principal = person.getProfile(UserProfile.class).getUser();
					principal = new PrincipalProxy(new ObjectId(solrdocument.getFieldValue("id")));
					((PrincipalProxy)principal).setDisplayName(solrdocument.getFieldValue("title")!=null ? solrdocument.getFieldValue("title").toString() : "");
					((PrincipalProxy)principal).setType(type);
					suggestionlabel = principal!=null ? principal.getDisplayName() + " (user)" : "- (user)";
				} 

				
				boolean outstanding = score > 3.5 ? true : false;
				if (principal!=null) {
					//newsuggestion = new WebSuggestion(new SerializableModel<Principal>(principal), suggestionlabel, score, outstanding);
					newsuggestion = new WebSuggestion(new ProxyModel((PrincipalProxy)principal), suggestionlabel, score, outstanding);
					suggestions.add(newsuggestion);		
					//addOrdered(newsuggestion, suggestions);
				}
			//}
		}
		if (!suggestions.isEmpty()) {
			boolean os = false;
			for (Suggestion suggestion : suggestions) {
				if (suggestion.isOutstanding())
					os = true;
				else {
					if (!suggestion.isOutstanding() && os) {
						((WebSuggestion)suggestion).setCssClass("tope");
						break;
					}
				}
			}
		}	
 		return suggestions;
	}
	
	public Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
	
	public DataSet getUserSet() {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				return dataset;
			}
		}
		return null;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getStatement(String pattern) {
		String solrstatment = "";
		
		pattern = pattern.replace("(group)", ""); 
		pattern = pattern.replace("(user)", ""); 
		
		if (!"".equals(pattern) && pattern!=null) {
			if (pattern.startsWith("\"")) {
				solrstatment += pattern+ " AND ";
			}
			else {
				solrstatment += "("+ pattern + " OR " + pattern+"*) AND ";
			}
		}
		solrstatment += "(type:group OR ";
		solrstatment += "(type:datasetmember AND dataset:" + String.valueOf(getUserSet().getId()) + ")) AND ";
		solrstatment += "domain:"+String.valueOf(getDomain().getId());
		
		return solrstatment;
	}
	
//	@SuppressWarnings("unchecked")
//	private void addOrdered(WebSuggestion newsuggestion, List<Suggestion> suggestions) {
//		int i = 0;
//		for (Suggestion suggestion : suggestions) {
//			if (suggestion.isOutstanding() && newsuggestion.isOutstanding()) {
//				if (suggestion.getScore()>newsuggestion.getScore())
//					i++;
//				else
//					break;
//			}
//			else {
//				if (suggestion.isOutstanding() && !newsuggestion.isOutstanding()) {
//					i++;
//				}
//				else {
//					if (!suggestion.isOutstanding() && newsuggestion.isOutstanding()) {
//						break;
//					}
//					else {
//						Principal principal1 = ((ObjectModel<Principal>)suggestion.getObject()).getObject();
//						Principal principal2 = ((ObjectModel<Principal>)newsuggestion.getObject()).getObject();
//						if (principal1.getDisplayName().toLowerCase().compareTo(principal2.getDisplayName().toLowerCase())<0)
//							i++;
//						else
//							break;
//					}
//				}
//			}
//		}	
//		suggestions.add(i, newsuggestion);		
//	}
}

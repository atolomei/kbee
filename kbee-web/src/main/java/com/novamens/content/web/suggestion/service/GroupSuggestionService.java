package com.novamens.content.web.suggestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.indexer.service.SystemSuggestionService;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.query.GroupsQuery;

public class GroupSuggestionService implements SystemSuggestionService {

	public GroupSuggestionService() {
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}
	
	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		Query query = new GroupsQuery(getIndex());
		if (pattern!=null && !"".equals(pattern)) { 
			query.getParameters().put("metainfo", "["+pattern+"*,"+pattern+"]");
			query.getParameters().put("sort", "relevance");
		}
		
		
		if (parameters==null || parameters.get("canonical")==null)
			query.getParameters().put("canonical", "false");
		
		ResultSet resultSet = query.execute();
		
		while (resultSet.hasNext() )  {
			SearchResult result = resultSet.next();
			Group group = (Group)result.getObject(); 
			if (group!=null) {
 				boolean outstanding = result.getScore() > 3.5 ? true : false;
				WebSuggestion suggestion = new WebSuggestion(new ObjectModel<Group>(group), group.getName()!=null? group.getName():"na", result.getScore(), outstanding);
				suggestions.add(suggestion);
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
	
	public JavaIndex getIndex() {
		return (JavaIndex)getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
}
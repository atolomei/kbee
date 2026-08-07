package com.novamens.content.web.suggestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.SystemSuggestionService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.WebSuggestion;

public class UserSuggestionService implements SystemSuggestionService {
	
	public class ProxyModel implements IModel<User> {
		private static final long serialVersionUID = 1L;
		private IModel<Person> model;
		public ProxyModel(IModel<Person> model) {
			this.model = model;
		}
		public User getObject() {
			Person person = model.getObject();
			if (person!=null) {
				User user = person.getProfile(UserProfile.class).getUser();
				return user;
			}
			else {
				return null;
			}
		}
		public void setObject(User principal) {
		}
		public void detach() {
		}
	}
	
	public UserSuggestionService() {
	}

	public List<Suggestion> getSuggestions(String pattern) {
		return getSuggestions(pattern, null);
	}
	
	@SuppressWarnings("unchecked")
	public List<Suggestion> getSuggestions(String pattern, Map<String, Object> parameters) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		
		List<Suggestion> personssuggestions = ServiceLocator.getService(PersonSuggestionService.class).getSuggestions(pattern, parameters);
		
		for (Suggestion personsuggestion : personssuggestions) {
			IModel<Person> personmodel = (IModel<Person>)personsuggestion.getObject();
			IModel<User> usermodel = new ProxyModel(personmodel);
			WebSuggestion suggestion = new WebSuggestion(usermodel, personsuggestion.getText(), personsuggestion.getScore(), personsuggestion.isOutstanding());
			suggestions.add(suggestion);
		}
		return suggestions;
	}
}

package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.web.suggestion.service.GroupSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class GroupsEditor extends RelationEditor<Group, Group> {
	private static final long serialVersionUID = 1L;

	public GroupsEditor() {
		super("groups");
	}
	
	protected Property<?> getKey() {
		return new Property<Group>() {
			@Override
			public String getName() {
				return "group";
			}
			@Override
			public boolean isAutocomplete() {
				return true;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return GroupsEditor.this.getSuggestions(pattern);
			}
			@Override
			public boolean isValid(IModel<Group> model) {
				return !isDescendant(model);
			}
		};
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		for (Suggestion suggestion :ServiceLocator.getService(GroupSuggestionService.class).getSuggestions(pattern)) {
			if (!isDescendant(suggestion)) {
				suggestions.add(suggestion);
			}
		};
		return suggestions;
	}
	
	@Override
	public boolean ordered() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isDescendant(Suggestion suggestion) {
		Group group = getModelObject();
		Group suggested = ((ObjectModel<Group>)suggestion.getObject()).getObject();
		return isDescendant(group, suggested);
	}
	
	private boolean isDescendant(IModel<Group> model) {
		Group group = getModelObject();
		Group suggested = model.getObject();
		return isDescendant(group, suggested);
	}
	
	private boolean isDescendant(Group group1, Group group2) {
		if (group1.equals(group2))
			return true;
		for (Group parent : group2.getGroups()) {
			if (!parent.equals(group2) && isDescendant(group1, parent)) {
				return true;
			}
		}
		return false;
	}
}

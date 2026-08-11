package kbee.web.security.user;

import java.util.List;

import com.novamens.content.web.suggestion.service.GroupSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class NewUserGroupsEditor extends RelationEditor<NewUserData, Group> {
	private static final long serialVersionUID = 1L;

	public NewUserGroupsEditor() {
		super("groups");
	}
	
	protected Property<?> getKey() {
		return new Property<Group>() {
			public String getName() {
				return "groups";
			}
			public boolean isAutocomplete() {
				return true;
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return NewUserGroupsEditor.this.getSuggestions(pattern);
			}
		};
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		return ServiceLocator.getService(GroupSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	public boolean ordered() {
		return false;
	}
}

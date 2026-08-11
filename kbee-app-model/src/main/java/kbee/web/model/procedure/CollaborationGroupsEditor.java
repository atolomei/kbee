package kbee.web.model.procedure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.web.suggestion.service.GroupSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class CollaborationGroupsEditor extends RelationEditor<ManualEndCondition, Group> {
				
	static Logger logger = LogManager.getLogger(CollaborationGroupsEditor.class.getName());
	
	private static final long serialVersionUID = 1L;

	public CollaborationGroupsEditor() {
		super("collaborationGroups");
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("canonical", "true");
		return ServiceLocator.getService(GroupSuggestionService.class).getSuggestions(pattern, parameters);
	}
	
	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected Property<?> getKey() {
		return new Property<Group>() {
			public String getName() {
				return "groups";
			}
			public boolean isAutocomplete() {
				return true;
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return CollaborationGroupsEditor.this.getSuggestions(pattern);
			}
		};
	}
}

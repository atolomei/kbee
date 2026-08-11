package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.web.suggestion.service.GroupSuggestionService;
import com.novamens.dao.SecurityDao;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.security.GroupProxy;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class EnabledGroupsEditor extends RelationEditor<Task, Group> {
	private static final long serialVersionUID = 1L;
				
	static Logger logger = LogManager.getLogger(EnabledGroupsEditor.class.getName());

	public EnabledGroupsEditor() {
		super("enabledGroups");
		setPropertyModel(new IModel<Collection<Group>>() {
			@Override
			public void detach() {
			}
			@Override
			public void setObject(Collection<Group> groups) {
				List<Group> proxies = new ArrayList<Group>();
				for (Group group : groups) {
					proxies.add(new GroupProxy(group));
				}
				((KbeeTask)getModelObject()).setEnabledGroups(proxies);
			}
			@Override
			public List<Group> getObject() {
				List<Group> groups = new ArrayList<Group>();
				for (Group group : ((KbeeTask)getModelObject()).getEnabledGroups()) {
					if (group instanceof GroupProxy) {
						group = getSecurityDao().findGroupById(group.getId());
						if (group!=null)
						groups.add(group);
					}
				}
				return groups;
			}
		});
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
				return EnabledGroupsEditor.this.getSuggestions(pattern);
			}
		};
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
}

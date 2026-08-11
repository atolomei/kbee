package kbee.web.security.user;


import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.web.security.markup.GroupStandAlonePage;
import com.novamens.content.web.suggestion.service.GroupSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class GroupsEditor extends RelationEditor<User, Group> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GroupsEditor.class.getName());
	
	private static final long serialVersionUID = 1L;

	public GroupsEditor() {
		super("standardGroups");
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		return ServiceLocator.getService(GroupSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected Property<?> getKey() {
		return new Property<Group>() {
			public String getName() {
				return "standardGroups";
			}
			public boolean isAutocomplete() {
				return true;
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return GroupsEditor.this.getSuggestions(pattern);
			}
		};
	}

	@Override
	protected int compare(IModel<Group> a, IModel<Group> b) {
		try {
		if (a.getObject().getName()==null)
			return (b.getObject().getName()!=null?1:0);
		else if(b.getObject().getName()==null)
			return -1;
		return a.getObject().getName().compareToIgnoreCase(b.getObject().getName());
		} catch (Exception e) {
			logger.error(e);
			return 0;
		}
	}
	
	@Override
	protected void onValueClick(IModel<Group> model) {
	
		final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
		
		if (role_security)
			setResponsePage(new GroupStandAlonePage(model));
		else  {
			setResponsePage(new ApplicationErrorPage<Object>( 
				new Model<String>("Your user account doesn't have rights to read Group " + model.getObject().getName()), 
				new Model<String>("Groups")));
			// TODO Alert Window
			//
		}
	}
//	
//	@Override
//	protected boolean deleteEnabled(Group value) {
//		return !value.isDerived();
//	}
}
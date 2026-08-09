package kbee.web.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.workflow.WorkflowContext;

import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class PersonCollaboratorSelector extends ObjectEditorPanel<WorkflowContext >{
	private static final long serialVersionUID = 1L;
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin = is_root ||  ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean role_dataset_read = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final boolean role_dataset_write = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	private IModel<ManualEndCondition> conditionmodel;
	private IModel<DataSet> collaborationsetmodel;
	private IModel<Person> personmodel;
	
	public PersonCollaboratorSelector(
			IModel<ManualEndCondition> conditionmodel, 
			IModel<Person> personmodel, 
			IModel<DataSet> collaborationmodel) {
		super("collaborator");
		setOutputMarkupId(true);
		setCondition(conditionmodel);
		setPersonModel(personmodel);
		setCollaborationSet(collaborationmodel);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		add(new AutoCompleteFieldV5<Person>("collaborator", getPersonModel(), true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				String qf = getCollaborationSet() instanceof UserSet || !getCondition().getCollaborationGroups().isEmpty() ? "isactive(true)" : "";
				if (getCondition().getCollaborationGroups()!=null && !getCondition().getCollaborationGroups().isEmpty()) {
					if (!"".equals(qf)) qf += " AND ";
					qf += getFilter(getCondition().getCollaborationGroups());
				}
				if (!"".equals(qf))
				parameters.put("qf", qf);
				return getCollaborationSet().getService(DataAccessService.class).getSuggestions(pattern, null, parameters);
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				target.add(PersonCollaboratorSelector.this);
			}	
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override 
			public boolean isEnabledAdvancedOptions(){
				return true;
			}
			@Override 
			public String getHistoryKey() {
				return getCondition().getEvent()+"-collaborator"; 
			}
			@Override 
			protected String getInfo(Suggestion suggestion) {
				return PersonCollaboratorSelector.this.getInfo ((PersonMember)((IModel<?>)suggestion.getObject()).getObject());
			}
			@Override 
			protected String getTemplate() {
				return "function(data) {  return '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value + '</span> - <span class=\"list-group-item-text\" >' + data.info + '</span></div>'; }";
			}
			protected String getFilter(List<Group> groups) {
				String statement = "";
				if (groups!=null && !groups.isEmpty()) {
					statement += "(";
					int g = 0;
					for (Group group : groups) {
						if (g++>0)
						statement += " OR ";
						statement += " member("+String.valueOf(group.getId())+")";
					}
					statement += " )";
				}
				return statement;
			}
			@Override 
			protected IModel<String> getHelpText() {
				return PersonCollaboratorSelector.this.getHelpText();
			}

		});
		
		//((Field<?>)get("collaborator")).setAutoFocus(true);
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container");
		menu.add(getMenu());
		add(menu);
	}
	
	public boolean isVisible() {
		return getCondition()!=null && getCondition().getCollaboration(); 
	}

	public ManualEndCondition getCondition() {
		return conditionmodel.getObject();
	}

	public void setCondition(IModel<ManualEndCondition> model) {
		this.conditionmodel = model;
	}
		
	public void setPersonModel(IModel<Person> model) {
		this.personmodel = model;
	}
	
	public IModel<Person> getPersonModel() {
		return personmodel;
	}

	public void setCollaborationSet(IModel<DataSet> model) {
		this.collaborationsetmodel = model;
	}
	
	public DataSet getCollaborationSet() {
		return collaborationsetmodel.getObject();
	}
	
	@SuppressWarnings("unchecked")
	public Component getFocusField() {
		((Field<?>)get("collaborator")).onBeforeRender();
		return ((Field<String>)get("collaborator")).getInput();
	}
	
	protected String getCollaboratorsUrl() {
		String url = getCollaborationSet() instanceof UserSet ? "/security/users" : "/dataset/"+getCollaborationSet().getId();
		return url; 
	}
	
	protected String getCollaboratorUrl() {
		Person person = getPersonModel().getObject();
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile!=null ? userprofile.getUser() : null;
		String url = getCollaborationSet() instanceof UserSet ? 
			"/security/users/" + user.getId() : 
			"/dataset/"+getCollaborationSet().getId()+"/"+person.getId();
		return url; 
	}
	
	protected String getInfo(PersonMember person) {
		ExtractionRule macro = person.getDataSet().getSublineRule();
		String info = macro!=null ? (String)macro.extract(person) : null;
		return info;
	}
	
	protected Panel getMenu() {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new LinkMenuItemPanel<Void>(id) {
				@Override
				public void onClick() {
					setResponsePage(new RedirectPage(getCollaboratorsUrl()));
				}	
				@Override
				public boolean isEnabled() {
					return getCollaborationSet() instanceof UserSet ?
						role_security :
						role_dataset_read;
				}
				@Override
				public String getLabel() {	
					return PersonCollaboratorSelector.this.getLabelString("collaborators", getCollaborationSet().getDisplayName());
				}
				@Override
				public String getTarget() {
					return "_blank";
				}
		});
		
		menu.addItem(id ->
			new LinkMenuItemPanel<Void>(id) {
				@Override
				public void onClick() {
					setResponsePage(new RedirectPage(getCollaboratorUrl()));
				}	
				@Override
				public boolean isEnabled() {
					if (getPersonModel().getObject()==null) return false;
					return getCollaborationSet() instanceof UserSet ?
						role_security :
						role_dataset_write;
				}
				@Override
				public String getLabel() {	
					return PersonCollaboratorSelector.this.getLabelString("edit.label");
				}
				@Override
				public String getTarget() {
					return "_blank";
				}
		});
		
		return menu;
	}
	
	protected IModel<String> getHelpText() {
		if (getCollaborationSet() instanceof UserSet) { 
			if (getCondition().getCollaborationGroups()!=null && 
				!getCondition().getCollaborationGroups().isEmpty()) {
				int g = 0;
				String orlabel = getLabelString("orlabel");
				int numberofgroups = getCondition().getCollaborationGroups().size();
				String groupslabel = "";
				for (Group group : getCondition().getCollaborationGroups()) {
					groupslabel += g>0 ? (g==numberofgroups-1 ? " "+orlabel+" " : ", ") : "";
					groupslabel += group.getDisplayName();
					g++;
				}
				if (numberofgroups>1)
					return getLabel("help1", groupslabel);
				else
					return getLabel("help2", groupslabel);
			}
			else {
				return null;
			}
		}
		else {
			return getLabel("personset.help", getCollaborationSet().getDisplayName());
		}
	}	
}

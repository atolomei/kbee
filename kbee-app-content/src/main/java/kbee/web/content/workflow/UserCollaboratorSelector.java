package kbee.web.content.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.DataAccessService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.security.acl.Group;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class UserCollaboratorSelector extends ObjectEditorPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;
	
	private IModel<ManualEndCondition> condition;
	private IModel<Person> collaboratorModel;
	
	public UserCollaboratorSelector(IModel<ManualEndCondition> conditionmodel,	IModel<Person> collaboratormodel) {
		super("collaborator");
		setCondition(conditionmodel);
		setCollaboratorModel(collaboratormodel);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		add(new AutoCompleteFieldV5<Person>("collaborator", getCollaboratorModel(), true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				String qf = "isactive(true)";
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
				target.add(UserCollaboratorSelector.this);
			}	
			@Override 
			public boolean isEnabledAdvancedOptions(){
				return true;
			}
			@Override 
			public String getHistoryKey() {
				return "collaborator"; 
			}
			@Override 
			protected String getInfo(Suggestion suggestion) {
				return UserCollaboratorSelector.this.getInfo ((PersonMember)((IModel<?>)suggestion.getObject()).getObject());
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
				return UserCollaboratorSelector.this.getHelpText();
			}

		});
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container");
		menu.add(getMenu());
		add(menu);
	}
	
	public boolean isVisible() {
		return getCondition()!=null && getCondition().getCollaboration(); 
	}

	public ManualEndCondition getCondition() {
		return condition.getObject();
	}

	public void setCondition(IModel<ManualEndCondition> model) {
		this.condition = model;
	}
	
	public IModel<Person> getCollaboratorModel() {
		return collaboratorModel;
	}

	public void setCollaboratorModel(IModel<Person> collaboratorModel) {
		this.collaboratorModel = collaboratorModel;
	}
	
	public DataSet getCollaborationSet() {
		return null;
	}
	
	protected String getInfo(PersonMember person) {
		ExtractionRule macro = person.getDataSet().getSublineRule();
		String info = (String)macro.extract(person);
		return info;
	}

	protected IModel<String> getHelpText() {
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
				return getLabel("help2", groupslabel);
			else
				return getLabel("help3", groupslabel);
		}
		else {
			return getLabel("help1");
		}
	}	
	
	protected Panel getMenu() {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new LinkMenuItemPanel<Void>(id) {
				@Override
				public void onClick() {
					setResponsePage(new RedirectPage("/new"));
				}	
				@Override
				public boolean isEnabled() {
					// permisos??
					return true;
				}
				@Override
				public String getLabel() {	
					return UserCollaboratorSelector.this.getLabel("new-user").getObject();
				}
				@Override
				public String getTarget() {
					return "_blank";
				}
		});
		
		return menu;
	}
}

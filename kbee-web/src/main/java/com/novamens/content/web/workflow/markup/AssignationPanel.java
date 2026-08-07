package com.novamens.content.web.workflow.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dao.SecurityDao;
import com.novamens.indexer.query.Suggestion;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.panel.AlertPanel;

/** 
 * @param <T>
 */
@SuppressWarnings("serial")
public class AssignationPanel<T extends Content> extends ObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	private String note;
	private IModel<User> usermodel;
	private List<IModel<Group>> enabledGroups;
	
	protected final boolean root  = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean role_admin  = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	public AssignationPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public AssignationPanel(String id, IModel<T> model) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
	}

	public void setEnabledGroups(List<Group> groups) {
		this.enabledGroups = new ArrayList<IModel<Group>>();
		for (Group group : groups) {
			this.enabledGroups.add(new ObjectModel<Group>(group));
		}
	}
	
	public List<Group> getEnabledGroups() {
		List<Group> groups = new ArrayList<Group>();
		for (IModel<Group> model : this.enabledGroups) {
			groups.add(model.getObject());
		}
		return groups;
	}
	
	public void onAssign(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public Content getContent() {
		return getModel().getObject();
	}

	public String getTitle() {
		return getContent().getTitle();
	}

	public String getTask() {
		Task task =  getContent().getService(WorkflowService.class).getTask();
		String taskname = task!=null ? task.getName() : "-";
		return taskname;
	}

	public void setUser(User user) {
		usermodel = new ObjectModel<User>(user);
	}

	public User getUser() {
		return usermodel!=null ? usermodel.getObject() : null;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public void update(AjaxRequestTarget target) {
		getContent().getService(WorkflowService.class).reassign(getUser(), getNote());
	}

	
	Form<?> form; 
	@Override
	
	public Form<?> getForm() {
		return form; // (Form<?>)get("aform");
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (form==null) {
			addForm();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onAfterRender() {
		super.onAfterRender();
		if (form!=null) {
		((AutoCompleteFieldV5<User>) form.get("user")).clearCache(getResponse());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (usermodel!=null) 
			usermodel.detach();
	}

	 
	 private SecurityDao getSecurityDao() {
	        return (SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	   }

	protected void addForm() {
		
		form = new Form<Void>("aform", Disposition.VERTICAL);
		
		
		AlertPanel<Void> pa=new AlertPanel<Void>("user-help",AlertPanel.INFO,  null, 
				null, 
				getLabel("user.help"));
		pa.setIcon(AlertPanel.HELP_INFO);
		form.add(pa);
		
		
		
		form.add(new StaticField<String>("title", new PropertyModel<String>(this, "title")));
		form.add(new StaticField<String>("task", new PropertyModel<String>(this, "task")));
						
		form.add(new StaticField<String>("formeruser", new Model<String>(getSecurityDao().findUserById( getModel().getObject().getWorkspace()).getDisplayName())));

		form.add(new AutoCompleteFieldV5<User>("user", new PropertyModel<User>(this, "user"), true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				if (!role_admin && getEnabledGroups()!=null && !getEnabledGroups().isEmpty()) {
					parameters.put("groups", getEnabledGroups());
				}
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern, parameters);
			}
			@Override 
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				target.focusComponent(((TextAreaField<?>)AssignationPanel.this.form.get("note")).getInput());
			}
			@Override 
			public String getHistoryKey() {
				return "collaborator"; 
			}
			@Override 
			protected boolean isValid(IModel<User> model) {
				if (getEnabledGroups()!=null && !getEnabledGroups().isEmpty()) {
					for (Group group : getEnabledGroups()) {
						if (group.isMember(model.getObject())) {
							return true;
						}
					}
					return false;
				}
				else {
					return true;
				}
			}
		});
		
		form.add(new TextAreaField<String>("note", new PropertyModel<String>(this, "note")));

		add(form);
	}

 
	protected void assign(User user, String note) {
		getContent().getService(WorkflowService.class).reassign(user, getNote());
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}

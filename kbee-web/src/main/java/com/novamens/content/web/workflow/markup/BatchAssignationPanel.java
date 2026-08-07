package com.novamens.content.web.workflow.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.content.markup.ContentSelectionPanel;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.content.editor.ContentEditor;
import kbee.web.form.AutoCompleteFieldV5;

@SuppressWarnings("serial")
public class BatchAssignationPanel extends ContentEditor<Content> {
				
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchAssignationPanel.class.getName());
	
	private boolean done = false;
	private IModel<String> retlabel;
	private String note;
	private IModel<User> usermodel;
	
	/** 
	 * 
	 */
	public BatchAssignationPanel (String id, List<IModel<Content>> selection) {
		super(id);
		
		setOutputMarkupId(true);
		
		add(new ContentSelectionPanel(selection) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(BatchAssignationPanel.this);
			}
			@Override
			protected Page getPage(IModel<Content> model) {
				return BatchAssignationPanel.this.getPage(model);
			}
		});
		
		Model<String> feedbackmodel = new Model<String>() {
			public String getObject() {
				if (done)
					if (!BatchAssignationPanel.this.hasErrors())
						return getLabel("ok-message").getObject();
					else
						return getLabel("errors-message").getObject();
				return null;
			}
		};
		
		add(new Label("feedback", feedbackmodel) {
			public boolean isVisible() {
				return done;
			}
		});
		
		((Label)get("feedback")).setEscapeModelStrings(false);
		
		Form<Content> form = new Form<Content>("form", selection.get(0), Disposition.VERTICAL) {
			public boolean isVisible() {
				return !done;
			}
		};
		
		form.add(new AutoCompleteFieldV5<User>("user", new PropertyModel<User>(this, "user"), true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				if (getEnabledGroups()!=null && !getEnabledGroups().isEmpty()) {
					parameters.put("groups", getEnabledGroups());
				}
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern, parameters);
			}
			@Override 
			public String getHistoryKey() {
				return "collaborator"; 
			}
		});
		
		form.add(new TextAreaField<String>("note", new PropertyModel<String>(this, "note")));
		
		form.add(new WorkingIndicatorAjaxSubmitLink("button", "ReAssign", form) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				executeBatch(target);
				done = true;
			 	target.add(BatchAssignationPanel.this);
			 	if (!hasErrors())
			 		onReturn();
			}
			@Override
			public boolean isVisible() {
				return !done;
			}
			@Override
			public String getLabel() {
				return "ReAssign";
			}
			@Override
			public String getWorkingLabel() {
				return BatchAssignationPanel.this.getLabel("executing").getObject();
			}
			
			@Override
			public String getAjaxIndicatorMarkupId() {
				return getId();
			}
		});
		
		form.add(new AjaxLink<Void>("button-abort") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				done = true;
				onReturn();
			}
			public boolean isVisible() {
				return !done;
			}
		});
		
		setEditionEnabled(true);
		
		add(form);
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

	/** --------------------------------------------------------------------------------------------
	*/
	public IModel<String> getReturnLabel() {
		return retlabel;
	}
	
	
	@Override
	public Form<?> getForm() {
		return (Form<?>)get("form");
	}
	
	
	public IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsNew(boolean isnew) {
		// TODO Auto-generated method stub
		
	}

	
	
	protected void onReturn(){
	}
	
	
	protected Page getPage(IModel<Content> model) {
		return null;
	}
	
	
	protected String executeAction(IModel<Content> model){
		
		if (getTask(model)==null || !isMonitorable(model)) {
			return getLabel("error.denied").getObject();
		}
		
		if (!enabled(model)) {
			return getLabel("error.enabled").getObject();
		}

		model.getObject().getService(WorkflowService.class).reassign(getUser(), getNote());
		
		return "";
	}
	
	
	protected void executeBatch(AjaxRequestTarget target){
		for (IModel<Content> model : getSelection()) {
			String status = executeAction(model);
			((ContentSelectionPanel)get("selection")).setStatus(model.getObject(), status);
		}
	}
	
	/** --------------------------------------------------------------------------------------------
	 */
	protected boolean hasErrors() {
		return ((ContentSelectionPanel)get("selection")).hasErrors();
	}
	
	/** --------------------------------------------------------------------------------------------
	 */
	protected List<IModel<Content>> getSelection() {
		return ((ContentSelectionPanel)get("selection")).getSelection();
	}
	
	/** --------------------------------------------------------------------------
	*/
	protected Task getTask(IModel<Content> model) {
		if (model.getObject().getService(WorkflowService.class)==null)
			return null;
		return model.getObject().getService(WorkflowService.class).getTask();
	}
	
	
	protected boolean isMonitorable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
	}
	
	
	protected boolean enabled(IModel<Content> model) {
		KbeeTask task = (KbeeTask)getTask(model);
		if (task == null)
			return true;
		if (task.getEnabledGroups().isEmpty())
			return true;
		for (Group group : task.getEnabledGroups()) {
			if (group.isMember(getUser()))
				return true;
		}
		return false;
	}
	
	
	protected List<Group> getEnabledGroups() {
		List<Group> groups = new ArrayList<Group>();
		for (IModel<Content> model : getSelection()) {
			KbeeTask task = (KbeeTask)getTask(model);
			if (task == null) {
				return new ArrayList<Group>();
			}	
			if (task.getEnabledGroups().isEmpty()) {
				return new ArrayList<Group>();
			}
			groups.addAll(task.getEnabledGroups());
		}
		return groups;
	}
	


}

package kbee.web.model.procedure;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;

@SuppressWarnings("serial")
public class TaskRelatedQueriesEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;
	
 	private static Logger logger = Logger.getLogger(TaskRelatedQueriesEditor.class.getName());
 	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	
	public TaskRelatedQueriesEditor(String id, IModel<Task> model) {
		super(id, model);
		setOutputMarkupId(true);
		setEditionEnabled(false);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextAreaField<String>("knowledgeCriteria",5, 40) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "IQL Related Query"; }, 
						getRelatedQueriesHelp() );
			}
		});

		// ---
		// Ex: (property($attribute:household lastname$) and
		//     (property($attribute:household firstname$)
		//     workflowstatus(File Review) and isexternal(false)
		form.add(new TextAreaField<String>("relatedCriteria", 5, 40) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "IQL Related Query"; }, 
						getRelatedQueriesHelp() );
			}
		});

		add(form);
		
		add(new EditButtonsV5<Task>(this) {
			@Override
			public boolean isEnabled() {
				return role_admin | role_model;
			}
		});
		
		add(new InfoDialog("help-modal"));
	}
	
	public Task getTask() {
		return getModel().getObject();
	}
	
	public Procedure getProcedure() {
		return ((KbeeTask)getTask()).getProcedure();
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeTask task = (KbeeTask)getTask();
				KbeeProcedure procedure = (KbeeProcedure)(task.getProcedure());
				List<Task> tasks = procedure.getTasks();
				procedure.setTasks(tasks);
				onUpdate(target);
				procedure.getDomain().getService(WorkflowDomainService.class).update(procedure, getUpdateDescription());
				reset();
			}
		} 
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	private String getUpdateDescription() {
		String text = getLabel("task.auditname", getTask().getName()).getObject();
		String parts = "";
		for (String part : getUpdatedParts()) {
			parts += "".equals(parts) ? " " : ", ";
			parts += part; 
		}
		text += parts;
		return text;
	}
	
	protected IModel<String> getRelatedQueriesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getRelatedQueriesHelp());
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
}

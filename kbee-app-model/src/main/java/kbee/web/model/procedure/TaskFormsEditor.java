package kbee.web.model.procedure;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.workflow.Task;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class TaskFormsEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskFormsEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private boolean callerforms=false;
	//IModel<Procedure> proceduremodel;
	
	public TaskFormsEditor(String id, IModel<Task> taskmodel) {
		super(id, taskmodel);
		setEditionEnabled(false);
		//this.proceduremodel = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new BooleanField("includeCallerForms") {
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
			}	
		});
		
		form.add(new FormsEditor("forms", getModel()) {
			public boolean isEnabled() {
				return true;
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<Task>(this) {
			@Override
			public boolean isEnabled() {
				return role_admin | role_model;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				WebTask task = (WebTask)getModel().getObject();
				com.novamens.content.workflow.KbeeProcedure procedure = (com.novamens.content.workflow.KbeeProcedure)task.getProcedure();
				procedure.setTasks(procedure.getTasks());
				((com.novamens.content.workflow.KbeeProcedure)procedure).update();
				getWorkflowDao().update(procedure.getMaster());
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}	
	}
	
	public boolean getCallerForms() {
		return callerforms;
	}
	
	public void setCallerForms(boolean value) {
		this.callerforms = value;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
package kbee.web.model.procedure;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.content.workflow.KbeeForkJoinTask;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class TaskThreadsEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskThreadsEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	public TaskThreadsEditor(String id, IModel<Task> model) {
		super(id, model);
		setEditionEnabled(false);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ThreadsEditor("threads", getModel()) {
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
				KbeeForkJoinTask task = (KbeeForkJoinTask)getModel().getObject();
				KbeeProcedure procedure = (KbeeProcedure)task.getProcedure();
				procedure.setTasks(procedure.getTasks());
				getWorkflowDao().update(procedure);
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}	
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
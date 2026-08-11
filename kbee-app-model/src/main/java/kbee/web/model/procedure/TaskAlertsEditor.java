package kbee.web.model.procedure;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.email.EmailBuilderWorkflowAlertTimeOut;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class TaskAlertsEditor extends ObjectEditor<Task> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskAlertsEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	private String taskName; // nombre inicial para auditoria
	
	public TaskAlertsEditor(String id, IModel<Task> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setTaskName(getModelObject().getName());
		
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("dueDateAlerts"));
		form.add(new NumberField<Integer>("maxTimePending"));
		form.add(new NumberField<Integer>("maxTimeRunning"));

		
		 Link<Task> et=new  Link<Task>("email-template", getModel()) {
			@Override
			public void onClick() {
				try {
					setResponsePage( new RedirectPage(getNotificationEmailTemplateUrl()));
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			}
		};

		form.add(et);
		
		WorkingAjaxLink<Task> st=new  WorkingAjaxLink<Task>("send-test-email", getModel()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					EmailBuilderWorkflowAlertTimeOut builder =new EmailBuilderWorkflowAlertTimeOut( null, null, getPerson(getSessionUser()), "max-time-pending"); 
					ServiceLocator.getService(EmailService.class).send(builder);
					Thread.sleep(1200);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			}
		};

		form.add(st);
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
	
	
	protected String getNotificationEmailTemplateUrl() {
		String key= "workflow-notification-timeout"; 
		return "/emailtemplates/" + (getSessionUser().getLocale().getLanguage().equals("es")? "es":"en")+  "/"+key;
	}
	
	
	private void setTaskName(String name) {
		this.taskName = name;
	}
	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				KbeeTask task = (KbeeTask)getTask();
				
				KbeeProcedure procedure = (KbeeProcedure)(task.getProcedure());

				List<Task> tasks = procedure.getTasks();
				procedure.setTasks(tasks);
				setModel(new TaskModel(new ObjectModel<Procedure>(procedure), task));
				onUpdate(target);
				procedure.getDomain().getService(WorkflowDomainService.class).update(procedure, getUpdateDescription());
				reset();
			}
		} 
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	private String getUpdateDescription() {
		String text = getLabel("task.auditname", taskName).getObject();
		String parts = "";
		for (String part : getUpdatedParts()) {
			parts += "".equals(parts) ? " " : ", ";
			parts += part; 
		}
		text += parts;
		return text;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private Person getPerson(User user) {
		Person person = null;
		UserProfile userProfile = getContentDao().findUserProfileByUser(user);
		if (userProfile!=null) {
			person = (Person)userProfile.getEntity();
		}
		return person;
	}
}
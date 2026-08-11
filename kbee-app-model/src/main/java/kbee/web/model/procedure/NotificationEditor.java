package kbee.web.model.procedure;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;

import com.novamens.content.workflow.NotificationRule;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.workflow.KbeeNotificationRule;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.email.EmailBuilderWorkflowPostTerminationAlert;
import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public class NotificationEditor extends ObjectEditorPanel<ManualEndCondition> {
	private static final long serialVersionUID = 1L;
	
	private String text = null;
	private List<IModel<Role>> receivers; 

	IModel<Task> taskmodel;
	
	public NotificationEditor(IModel<Task> taskmodel) {
		super("notification");
		this.taskmodel=taskmodel;
		setOutputMarkupId(true);
	}

	
	
	public IModel<Task> getTaskModel() {
		return this.taskmodel;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setRule(getModelObject().getRule());
		
		
		WorkingAjaxLink<Task> st=new  WorkingAjaxLink<Task>("send-test-email", getTaskModel()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					EmailBuilderWorkflowPostTerminationAlert builder = new EmailBuilderWorkflowPostTerminationAlert(null, null, getPerson(getSessionUser()), getPerson(getSessionUser()), getText());
					ServiceLocator.getService(EmailService.class).send(builder);
					Thread.sleep(1000);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			}
		};

		add(st);
		
		add(new TextAreaField<String>("notificationText", new PropertyModel<String>(this, "text")) {
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>("The text above will replace the macro ${text} in the Email Template -> " + getNotificationEmailTemplateLink());
			}
		});
		
		add(new ReceiversEditor() {
			@Override
			protected IModel<Collection<Role>> getPropertyModel() {
				return new PropertyModel<Collection<Role>>(NotificationEditor.this, "receivers");
			}
			@Override
			public boolean isVisible() {
				return NotificationEditor.this.getText()!=null;
			}
		});
		
		add(new AjaxLink<Void>("add-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setText("");
				target.add(NotificationEditor.this);
			}
			@Override
			public boolean isVisible() {
				return getText()==null && getEditor().isEditionEnabled();
				
			}
		});
		
		add(new AjaxLink<Void>("delete-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setText(null);
				setReceivers(new ArrayList<Role>());
				target.add(NotificationEditor.this);
			}
			@Override
			public boolean isVisible() {
				return getText()!=null && getEditor().isEditionEnabled();
			}
		});
	}

	@Override
	public void updateModel() {
		WorkflowRule multiplerule = getModelObject().getRule();
		
		if (multiplerule!=null) {
			if (multiplerule instanceof MultipleRule) {
				List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
				for (WorkflowRule rule : rules) {
					if (rule instanceof NotificationRule) {
						rules.remove(rule);
						break;
					}
				}
			}
			else {
				multiplerule = null;
			}
		}
		
		if (multiplerule==null) {
			multiplerule = new MultipleRule();
			getModelObject().setRule(multiplerule);
		}
		
		if (getText()!=null && !getReceivers().isEmpty()) {
			KbeeNotificationRule notification = new KbeeNotificationRule();
			notification.setText(getText());
			notification.setReceivers(getReceivers());
			List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
			rules.add(notification);
		}
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setReceivers(List<Role> roles) {
		this.receivers = new ArrayList<IModel<Role>>();
		for (Role role : roles) {
			this.receivers.add(new ObjectModel<Role>(role));
		}
	}
	
	public List<Role> getReceivers() {
		List<Role> receivers = new ArrayList<Role>();
		if (this.receivers!=null)
			for (IModel<Role> model : this.receivers) {
				receivers.add(model.getObject());
			}
		return receivers;
	}
	
	public void setRule(WorkflowRule multiplerule) {
		if (multiplerule!=null) {
			if (multiplerule instanceof MultipleRule) {
				List<WorkflowRule> rules = ((MultipleRule)multiplerule).getRules();
				for (WorkflowRule rule : rules) {
					if (rule instanceof NotificationRule) {
						setText(((NotificationRule)rule).getText());
						setReceivers(((NotificationRule)rule).getReceivers());
						break;
					}
				}
			}
		}
	}
	
	public void onDetach() {
		super.onDetach();
		if (this.taskmodel!=null)
			this.taskmodel.detach();
	}
	
	protected String getNotificationEmailTemplateLink() {
		// workflow-notification
		String key= "workflow-notification"; 
		return "<a class=\"btn-link\"  target=\"_blank\" href=\"/emailtemplates/" + (getSessionUser().getLocale().getLanguage().equals("es")? "es":"en")+  "/"+key+"\">"+key+"</a>";
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
//	
//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}

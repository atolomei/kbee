package kbee.web.enoti;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.web.security.markup.RuleConditionWizardPanel;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.logging.CheckinEvent;
import com.novamens.logging.ProgressNoteEvent;
import com.novamens.logging.TaskStartEvent;
import com.novamens.security.Principal;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.email.EmailBuilderSendTestEmailRuleNotification;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;

/**
 * @author aferr
 *
 */
@SuppressWarnings("serial")
public class ENotiRuleEditor extends DomainObjectEditor<ENotiRule> {
			
	private static final long serialVersionUID = 1L;
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ENotiRuleEditor.class.getName());

	
	boolean publishEvent, progressNoteEvent, taskStartEvent, requiereConfirm;
	
	public ENotiRuleEditor() {
		super("editor");
	}

	public ENotiRuleEditor(IModel<ENotiRule> model) {
		this("editor", model, false);
	}

	public ENotiRuleEditor(String id, IModel<ENotiRule> model, boolean edition) {
		super(id, model);
		setEditionEnabled(edition);
		setOutputMarkupId(true);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		
				
		form.add(new TextField<String>("name", true));
		
		String rs = getLabelString("alert-msg");
		try {
			if (getRule().getOwner()!=null) {
				if (getRule().getOwner().getId().equals(getSessionUser().getId()))
					rs = rs.replace("{0}", getPerson().getEmail()!=null?getPerson().getEmail(): "N/A");
				else
					rs = rs.replace("{0}", getContentDao().findUserProfileByUserId(getRule().getOwner().getId()).getPerson().getEmail());
			}
			else if (getPerson().getEmail()!=null)
				rs = rs.replace("{0}", getPerson().getEmail()!=null?getPerson().getEmail(): "N/A");
		} 
		catch (Exception e) {
			logger.error(e);
		}
		
		//Label em=new Label("email", rs);
		//em.setEscapeModelStrings(false);
		//em.setVisible(!getRule().isSystem());
		
		AlertPanel<Void> pa=new AlertPanel<Void>("email",AlertPanel.INFO,  null, null, 
				new Model<String>(rs));
		pa.setIcon("fa-duotone fa-envelope");
		add(pa);

		
		
		form.add(new RuleConditionWizardPanel<ENotiRule>() {
			@Override
			protected IModel<String> getHelpText() {
				return getLabel("condition.help");
			}
		});

		setPublishEvent(getRule().includes(CheckinEvent.getClassEventType()));
		form.add(new CheckField("publishEvent", new PropertyModel<Boolean>(this, "publishEvent")));
		
		setProgressNoteEvent(getRule().includes(ProgressNoteEvent.getClassEventType()));
		form.add(new CheckField("progressNoteEvent", new PropertyModel<Boolean>(this, "progressNoteEvent")));
		
		setTaskStartEvent(getRule().includes(TaskStartEvent.getClassEventType()));
		form.add(new CheckField("taskStartEvent", new PropertyModel<Boolean>(this, "taskStartEvent")));

		
		//PublishEvent(getRule().includes(CheckinEvent.getClassEventType()));
		//form.add(new CheckField("requireConfirm", new PropertyModel<Boolean>(this, "requireConfirm")));

		
		
		WebMarkupContainer tc= new 	WebMarkupContainer("type-containers");
		tc.setVisible(false);
		form.add(tc);
		
		tc.add(new BooleanSwitchField("isemail", new PropertyModel<Boolean>(this, "isEmail")) {
			@Override
			public boolean isBorder() {
				return true;
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>("Send Email");
			}
			protected IModel<String> getText() {
				return getLabel("isemail.text", getPublishEmailTemplateLink(), getPendingEmailTemplateLink());
			}
		});

		tc.add(new BooleanSwitchField("isalert", new PropertyModel<Boolean>(this, "isAlert")) {
			@Override
			public boolean isBorder() {
				return true;
			}
		});

		WorkingAjaxLink<ENotiRule> st=new  WorkingAjaxLink<ENotiRule>("send-test-email", getModel()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					String key = null;
					if (getModel().getObject().getEventType()==ENotiRule.EVENT_PUBLISH_CONTENT)
						key= ENotiRule.PUBLISH_EMAIL_TEMPLATE + (getModelObject().isSystem()?"-domain":"-user");
					else
						key= ENotiRule.PENDING_EMAIL_TEMPLATE;
					EmailBuilderSendTestEmailRuleNotification builder = new EmailBuilderSendTestEmailRuleNotification(getModel().getObject(), key, getPerson());
					ServiceLocator.getService(EmailService.class).send(builder);
					Thread.sleep(1200);
				} 
				catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			}
		};
		tc.add(st);

		
		WebMarkupContainer rc= new 	WebMarkupContainer("receivers-containers");
		rc.setVisible(getRule().isSystem());
		form.add(rc);
		rc.add(getRule().isSystem() ? new ReceiversEditor<ENotiRule>("receivers") : new InvisiblePanel("receivers"));
		rc.add(getRule().isSystem() ? new RoleReceiversEditor<ENotiRule>() : new InvisiblePanel("roleReceivers")) ;
		
		add(form);
		
		
		add(new EditButtonsV5<ENotiRule>(this));
	}
	
	public KbeeENotiRule getRule() {
		return ((KbeeENotiRule) getModelObject());
	}
	
	public void setIsEmail(boolean b) {
		getRule().setEmail(b);
	}

	public void setEmail(boolean b) {
		getRule().setEmail(b);
	}
	
	public boolean isAlert() {
		return getRule().isAlert();
	}
	
	public void setAlert(boolean b) {
		getRule().setAlert(b);
	}
	
	public void setIsAlert(boolean b) {
		getRule().setAlert(b);
	}

	public boolean isEmail() {
		return getRule().isEmail();
	}
	
	public String getKey() {
		return getRule().getKey();
	}
	
	public void setKey(String key) {
		getRule().setKey(key);
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
	}

	public void addPrincipal(Principal p) {
		getRule().getReceivers().add(p);
	}
	
	public void setReceivers(List<Principal> list) {
		getRule().setReceivers(list);
	}
	
	public List<Principal> getReceivers() {
		return getRule().getReceivers();
	}
	
	public List<com.novamens.content.security.Role> getRoleReceivers() {
		return getRule().getRoleReceivers();
	}
	
	public boolean isPublishEvent() {
		return publishEvent;
	}

	public void setPublishEvent(boolean publishEvent) {
		this.publishEvent = publishEvent;
	}

	public boolean isProgressNoteEvent() {
		return progressNoteEvent;
	}

	public void setProgressNoteEvent(boolean progressNoteEvent) {
		this.progressNoteEvent = progressNoteEvent;
	}

	public boolean isTaskStartEvent() {
		return taskStartEvent;
	}
	
	
	public boolean isRequireConfirm() {
		return requiereConfirm;
	}
	public boolean getRequireConfirm() {
		return requiereConfirm;
	}
	
	public void setRequireConfirm(boolean v) {
		this.requiereConfirm =v;
	}
	

	public void setTaskStartEvent(boolean taskStartEvent) {
		this.taskStartEvent = taskStartEvent;
	}

	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				if ( (getReceivers()==null || getReceivers().isEmpty())  && (getRoleReceivers()==null || getRoleReceivers().isEmpty())) {
					logger.error("Receivers is null");
				}
				
				if (getRule().getKey()==null || getRule().getKey().length()==0 || isNew())
					getRule().setKey(prefixName());
					
				if (getRule().getName()==null || getRule().getName().length()==0)
					getRule().setName(getSessionUser().getFirstLastName()+" "+ String.valueOf(System.currentTimeMillis()%1000));
				
				getRule().setEventTypes(getEventTypes());
				
				logger.debug(getRule().toString());
				
				ServiceLocator.getService(ENotiRuleService.class).update(getRule(), getUpdatedParts());
				super.reset();
				getModel().detach();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public List<String> getEventTypes() {
		List<String> types = new ArrayList<String>();
		if (isPublishEvent()) types.add(CheckinEvent.getClassEventType());
		if (isProgressNoteEvent()) types.add(ProgressNoteEvent.getClassEventType());
		if (isTaskStartEvent()) types.add(TaskStartEvent.getClassEventType());
		return types;
	}

	protected String getPendingEmailTemplateLink() {
		String key= ENotiRule.PENDING_EMAIL_TEMPLATE; // there is only one template for pending tasks 	
		return "<a class=\"btn-link\"  target=\"_blank\" href=\"/emailtemplates/" + (getSessionUser().getLocale().getLanguage().equals("es")? "es":"en")+  "/"+key+"\">"+key+"</a>";
	}
	
	protected String getPublishEmailTemplateLink() {
		String key= ENotiRule.PUBLISH_EMAIL_TEMPLATE + (getModelObject().isSystem()?"-domain":"-user"); 
		return "<a class=\"btn-link\"  target=\"_blank\" href=\"/emailtemplates/" + (getSessionUser().getLocale().getLanguage().equals("es")? "es":"en")+  "/"+key+"\">"+key+"</a>";
	}
	
	private String prefixName() {
		if (getModelObject().getName()==null || getModelObject().getName().length()==0) {
			if (getModelObject().getId()!=null)
				return getModelObject().getId().toString();
			return String.valueOf(String.valueOf(Double.valueOf(Math.abs((Math.random()*10000))).intValue()));
		}
		if (getModelObject().getName().length()<=6)
			return getModelObject().getName().replaceAll("\\s", "-").toLowerCase();
		else
			return getModelObject().getName().substring(0,6).replaceAll("\\s", "-").toLowerCase();
	}
}

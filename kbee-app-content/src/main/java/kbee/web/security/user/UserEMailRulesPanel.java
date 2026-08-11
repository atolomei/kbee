package kbee.web.security.user;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.dashboard.SubscriptionsPanel;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.enoti.ENotiRulePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.security.user.UserMainPanel.NullUser;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class UserEMailRulesPanel extends DomainObjectEditor<UserProfile> {
				
	private static final long serialVersionUID = 1L;
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserEMailRulesPanel.class.getName());
	private IModel<UserProfile> model;
	private List<ENotiRule> rules = null;

	
	private Form<?> form;
	private boolean is_my_account = false;
	
	/**
	 * 
	 * 
	 * @param id
	 * @param model
	 * @param isMyAccount
	 */
	public UserEMailRulesPanel(String id, IModel<UserProfile> model, final boolean is_MyAccount) {
		super(id, model);
		
		setModel(model);
		
		setMyAccount(is_MyAccount);
		setOutputMarkupId(true);
		this.setEditionEnabled(false);
		
		final WebMarkupContainer rulescontainer = new WebMarkupContainer("rules-container");
		rulescontainer.setOutputMarkupId(true);
		
		rulescontainer.add(new ListView<ENotiRule>("rules", new PropertyModel<List<ENotiRule>>(this, "rules")) {
			
			public void populateItem(final ListItem<ENotiRule> item) {
				
				Link<Void> rulelink = new Link<Void>("rule-link") {
					public void onClick() {
						setResponsePage(new ENotiRulePage(new ObjectModel<ENotiRule>(item.getModelObject()), false, isMyAccount(), false));
					}
				};
				
				String rulelabel = item.getModelObject().getName();
				
				if (rulelabel==null) 
					rulelabel = "Email Notification " + item.getModelObject().getId().toString();
				
				rulelink.add(new AttributeModifier("target", "_blank"));
				
				rulelink.add(new Label("rule-label", rulelabel));

				item.add(rulelink);
				
				item.add(new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						((Dialog)UserEMailRulesPanel.this.get("remove-dialog")).open(target, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {

								if (button.equals(Dialog.Delete)) {
									try {
										ServiceLocator.getService(ENotiRuleService.class).delete(item.getModelObject());
										rules = null;
										target.add(rulescontainer);
									}
									catch (ContentMgmtException e) {
										logger.error(e);
									}
									catch (Exception e) {
										logger.error(e);
									}
								}
							}
						}, item.getModelObject().getName());
					}
				});
			}
		});
		
		add(rulescontainer);
		
		add(new Link<Void>("add-button") {
			@Override
			public void onClick() {
				try {
					
					/**
					 * key (Self Service rule)  = "user"
					 * key (Domain rule)> = "domain"
					 */
					ENotiRule rule = ServiceLocator.getService(ENotiRuleService.class).createEmailRule(UserEMailRulesPanel.this.getModel().getObject().getUser(), "user", false);
					rule.setName( new StringResourceModel("enoti", UserEMailRulesPanel.this, null).getString() + " " + String.valueOf(getRules().size()+1));
					setResponsePage(new ENotiRulePage(new ObjectModel<ENotiRule>(rule), true, isMyAccount(), false));
				}
				catch (ContentCreationException e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			}
			@Override
			public boolean isVisible() {
				if (getPerson(UserEMailRulesPanel.this.getModel().getObject()).getState()==ObjectState.DELETED)
					return false;
				
				// only root can edit root values
				if  (UserEMailRulesPanel.this.getModel().getObject().getUser().getUserName().startsWith("root@"))
					return getSessionUser().getUserName().startsWith("root@");
	
				
				return !(UserEMailRulesPanel.this.getModel().getObject() instanceof NullUser);
			}
		});
		
		add(new Dialog("remove-dialog", "dialog.delete.title", "dialog.delete.message", Dialog.Cancel, Dialog.Delete));
		
		
		add( new SubscriptionsPanel("subscriptions" , new ObjectModel<Person>(getModel().getObject().getPerson())) {
			protected void onClick(IModel<ContentSubscription> modelObject, int index) {
				IModel<Content> mod=new ObjectModel<Content>(modelObject.getObject().getContent());
				 UserEMailRulesPanel.this.onClick(mod, index);
			}
		});
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		BooleanSwitchField email_noti=new BooleanSwitchField("emailNotifications", new PropertyModel<Boolean>(this, "emailNotifications")) {
			protected IModel<String> getHelpText() {
				return getLabel("emailNotifications" + (isMyAccount()?"-myaccount": "") +  ".help", UserEMailRulesPanel.this.getPerson().getFirstLastName());
			}
		};
		email_noti.setBorder(false);
		
		BooleanSwitchField email_pending_noti=new BooleanSwitchField("emailPendingNotifications", new PropertyModel<Boolean>(this, "emailPendingNotifications")) {
			protected IModel<String> getHelpText() {
				return getLabel("emailPendingNotifications" + (isMyAccount()?"-myaccount": "") +  ".help", UserEMailRulesPanel.this.getPerson().getFirstLastName());
			}
		};
		email_pending_noti.setBorder(false);
				 
		BooleanSwitchField email_rule =new BooleanSwitchField("emailRuleNotifications", new PropertyModel<Boolean>(this, "emailRuleNotifications"));
		email_rule.setBorder(false);
		
		BooleanSwitchField alert_rule = new BooleanSwitchField("alertRuleNotification", new PropertyModel<Boolean>(this, "alertRuleNotifications"));
		alert_rule.setLabel(false);
		alert_rule.setBorder(false);
		
		BooleanSwitchField email_progress = new BooleanSwitchField("emailProgressNoteNotification", new PropertyModel<Boolean>(this, "emailProgressNoteNotifications"));
		email_progress.setBorder(false);
		
		BooleanSwitchField alert_progress = new BooleanSwitchField("alertProgressNoteNotification", new PropertyModel<Boolean>(this, "alertProgressNoteNotifications"));
		alert_progress.setLabel(false);
		alert_progress.setBorder(false);
		
		BooleanSwitchField whatsAppChannel = new BooleanSwitchField("whatsAppEnabled", new PropertyModel<Boolean>(this, "whatsAppEnabled"));
		whatsAppChannel.setBorder(false);											


		EditButtonsV5<UserProfile> buttons = new EditButtonsV5<UserProfile>(this) {
			protected String getEditClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			public boolean isEnabled() {
				// Only Root can Edit root user
				if (UserEMailRulesPanel.this.getModel().getObject().getUser().getUserName().startsWith("root@")
						&& !getSessionUser().getUserName().startsWith("root@"))
					return false;
				return true;
			}
		};
		
		form.add(email_noti);
		form.add(email_rule);
		form.add(alert_rule);
		form.add(email_progress);
		form.add(alert_progress);
		form.add(email_pending_noti);
		form.add(whatsAppChannel);
		add(form);
		
		form.add(buttons);
	}

	public Form<?> getForm() {
		return form;
	}

	public List<ENotiRule> getRules() {
		if (rules==null) 
			rules = ServiceLocator.getService(ENotiRuleService.class).getEmailRules(getModel().getObject().getUser());
		return rules;
	}
	

	public void setAlertProgressNoteNotifications(boolean b) {getModelObject().setAlertProgressNoteNotifications(b);}
	public boolean isAlertProgressNoteNotifications() {return getModelObject().isAlertProgressNoteNotifications();}

	public void setEmailProgressNoteNotifications(boolean b) {getModelObject().setEmailProgressNoteNotifications(b);}
	public boolean isEmailProgressNoteNotifications() {return getModelObject().isEmailProgressNoteNotifications();}
	
	public void setAlertRuleNotifications(boolean b) {getModelObject().setAlertRuleNotifications(b);}
	public boolean isAlertRuleNotifications() {return getModelObject().isAlertRuleNotifications();}
	
	public void setWhatsAppEnabled(boolean b) {((KbeeUserProfile)getModelObject()).setWhatsAppEnabled(b);}
	public boolean isWhatsAppEnabled() {return getModelObject().isWhatsAppEnabled();}

	
	public void setEmailPendingNotifications(boolean b) {getModelObject().setEmailPendingNotifications(b);}
	public boolean isEmailPendingNotifications() {return getModelObject().isEmailPendingNotifications();}


	public void setEmailRuleNotifications(boolean b) {getModelObject().setEmailRuleNotifications(b);}
	public boolean isEmailRuleNotifications() {	return getModelObject().isEmailRuleNotifications();	}
	
	public boolean isSendFilesEmail() {return getModelObject().isSendFilesEmail();}
		public void setIsSendFilesEmail(boolean value) {getModelObject().setSendFilesEmail(value);}
	public void setSendFilesEmail(boolean value) {getModelObject().setSendFilesEmail(value);}


	public void setEmailNotifications(boolean b) {getModelObject().setEmailNotifications(b);}
	public boolean isEmailNotifications() {return getModelObject().isEmailNotifications();}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				target.add(this);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public void setMyAccount( boolean b) {
		this.is_my_account=b;
	}
	
	public boolean isMyAccount() {
		return this.is_my_account;
	}

	@Override
	public void onDetach() {
		if (model!=null)
			model.detach();
		rules = null;
		super.onDetach();
	}
	
	protected Person getPerson() {
		return getModelObject().getPerson();
	}

	protected Person getPerson(UserProfile user) {
		return user.getPerson();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	@SuppressWarnings("unchecked")
	protected void onClick(IModel<Content> model, int index) {
		
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		
		if (model.getObject()==null) {
			setResponsePage(new ApplicationErrorPage<>( new Model<String>("Content no longer exists")));
			return;
		}
			
		
		
		try {
			
			TaskPage<Content> page = null;
			
			if (    workflowService.getTask()!=null &&
					workflowService.getContext().getProcess().isRunning()) {
				Task task = workflowService.getTask();
					page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
					if (model.getObject().getWorkspace()>0) {
						if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
							page.setEditionEnabled(true);
							page.setReadOnly(false);
						}
						else {
							page.setEditionEnabled(false);
							page.setReadOnly(true);
						}
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
			}
			
			if (page==null) {
				setResponsePage(new ApplicationErrorPage<>( new Model<String>("The Content is no longer executing a business process")));
				model.getObject().getService(ContentSubscriptionService.class).unsubscribe(getPerson());
				return;
			}
			setResponsePage(page);
			
			
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}



}

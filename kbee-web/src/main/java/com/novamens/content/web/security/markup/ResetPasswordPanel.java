package com.novamens.content.web.security.markup;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.entity.Person;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxButton;
import com.novamens.logging.SecurityEvent;
import com.novamens.logging.SecurityUpdateEvent;

				
public class ResetPasswordPanel extends Panel {
				
	private static final long serialVersionUID = 8083267356589449363L;
	
	static Logger logger = LogManager.getLogger(ResetPasswordPanel.class);

	private IModel<Person> model;

	private String title;

	public ResetPasswordPanel(String id, IModel<Person> model) {
		super(id);
		
		setModel(model);
		setOutputMarkupId(true);
		ResetPasswordForm form = new ResetPasswordForm("reset-password-form");
		add(form);
	}
	
	public void setTitle(String title) 					{this.title = title;}
	public String getTitle() 							{return this.title;}

	protected IModel<Person> getModel() 				{return model;}
	private void setModel(IModel<Person> model) 		{this.model=model;}
	
	public void close(AjaxRequestTarget target) {}
	
	
	public void onSubmit(AjaxRequestTarget target, String pwd) {

		 KbeeUser kuser = (KbeeUser) getProfile().getUser();

		 if (kuser.getPassword()==null || !kuser.getPassword().equals(kuser.encode(pwd))) {
			 	kuser.setPassword(pwd);
			 	DOMObjectService objectService = getModel().getObject().getService(DOMObjectService.class);
			 try {				
				 SecurityEvent logevent = new SecurityUpdateEvent(kuser, "Reset Password");
				 objectService.update(logevent);
				 
			} catch (Exception  e) {
				logger.error(e);
			}
		 }
		 close(target);
	}
	
	public void onCancel(AjaxRequestTarget target) {
		logger.debug("ResetPasswordPanel. onCancel");
		close(target);
	}
	
	public UserProfile getProfile() {
		return getModel().getObject().getProfile(UserProfile.class);
	}

	
	/**
	 *  
	 */
	public class ResetPasswordForm extends Form<Void> {
			
		private static final long serialVersionUID = 4561816856843561323L;
		
		FeedbackPanel feedback;
		private String pwd;
		private String retypePwd;
		
		public ResetPasswordForm(String id) {
			super(id);
			
			setOutputMarkupId(true);
			
			feedback = new FeedbackPanel("feedback") {
				private static final long serialVersionUID = -2166471612263570720L;
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (this.anyErrorMessage()) {
						tag.append("class", "error", " ");
					} 
					else if (anyMessage(FeedbackMessage.SUCCESS)) {
						tag.append("class", "ok", " ");
					}
				}
			};
			
		 	feedback.setEscapeModelStrings(false);
			feedback.setOutputMarkupId(true);
			add(feedback);
			
			add(new Behavior() {
				private static final long serialVersionUID = -1932043492052563579L;
				@Override
				public void renderHead(Component component, org.apache.wicket.markup.head.IHeaderResponse response) {
					StringBuffer script = new StringBuffer();
					script.append("function hidefeedback(feedbackid) {");
					script.append("var styleObj = document.getElementById(feedbackid).style;");
					script.append("styleObj.display = 'none'");
					script.append("}");
					response.render(new JavaScriptContentHeaderItem(script.toString(), "feedback"));
				}
			});

			
			
			PasswordTextField xpwd = new PasswordTextField("password", new PropertyModel<String>(this, "pwd"));
			add(xpwd);
			xpwd.setRequired(true);

			PasswordTextField xpwd_validate = new PasswordTextField("retype-password", new PropertyModel<String>(this, "retypePwd"));
			add(xpwd_validate);
			xpwd_validate.setRequired(true);

			add(new WorkingIndicatorAjaxButton("ok-link", ResetPasswordForm.this) {

				private static final long serialVersionUID = -2460231970138298598L;

				  	@Override
					protected void onSubmit(AjaxRequestTarget target) {

				  	if (pwd==null || pwd.length()==0 || retypePwd==null || retypePwd.length()==0) {
				  			// feedback.info(new StringResourceModel("notifications.notfound", NotificationsPanel.this, null).getString());
				  			feedback.error("Please enter password and password verfication.");
				  			onInfo(target);
				  			// target.add(ResetPasswordPanel.this);
				  		}
				  	else if (!pwd.equals(retypePwd)) {
				  			feedback.error("Password and verification does not match.");
				  			onInfo(target);
				  		}
				  		else
				  			ResetPasswordPanel.this.onSubmit(target, pwd);
				  	
					}
					
					 @Override
					 protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
					}
					
					@Override
					public boolean isEnabled() {
						return true;
					}
			});
			
			add(new AjaxLink<Void>("cancel-link") {
		 		private static final long serialVersionUID = 7601289761103719972L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					ResetPasswordPanel.this.close(target);
				}
			});
		}

		public void setPwd(String pwd) 				{this.pwd = pwd;}
		public String getPwd() 						{return this.pwd;}
												
		public void setRetypePwd(String retype_pwd) {this.retypePwd = retype_pwd;}
		public String getRetypePwd() 				{return this.retypePwd;}
		
		public void onInfo(AjaxRequestTarget target) {
			target.add(feedback);
			target.appendJavaScript("setTimeout(\"hidefeedback('"+feedback.getMarkupId()+"')\",2600);");
		}
						
		public void onError(AjaxRequestTarget target) {
			target.add(feedback);
			target.appendJavaScript("setTimeout(\"hidefeedback('"+feedback.getMarkupId()+"')\",2600);");
		}

		public void onDetach() {
			feedback.detach();	
			super.onDetach();
		}
		
	}
}

package kbee.web.security.user;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.service.PersonService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.web.content.markup.ContentPanel;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.kbee.security.KbeeAuthToken;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.sms.KbeeSmsMessage;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.security.AuthToken;
import com.novamens.security.TokenSubmission;
import com.novamens.service.ServiceLocator;
import com.novamens.sms.SmsService;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.PropertiesFactory;
import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.page.ErrorPageEvent;

public class PersonSMSEditor extends DomainObjectEditor<Person> {

	static Boolean ACCEPT_ALL_SIGNATURES = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("accept-all-signatures", "no").trim());
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PersonEditor.class.getName());
	
	private boolean ismyaccount;

	private WebMarkupContainer vc;
	// private WebMarkupContainer itc;
	private Form<?> form;	
	private String tokenvalue;
	private String tokenvalueValidation = "token";
	private boolean isTokenSent = false;

	
	public String geTtokenvalueValidation() {
	   return tokenvalueValidation;
	}
	
	public void seTtokenvalueValidation(String v) {
		   this.tokenvalueValidation=v;
		}
	
	
	org.apache.wicket.markup.html.form.TextField<String> tvv;
	
	public PersonSMSEditor(String id, IModel<Person> model, boolean ismyaccount) {
		super(id, model);
		this.ismyaccount = ismyaccount;
		setOutputMarkupId(true);
	}
	
	Label la;
	
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		TextField<String> phone = new TextField<String>("phone");
		
		
		phone.setRequired(true);
		form.add(phone);
		
		
		form.add(new EditButtonsV5<Person>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return ismyaccount || isRoot();
			}
		});
		
		
		// ----------------------------------------------------------------------------------------------------------------
		//
		//
		vc = new WebMarkupContainer("validate-container");
		vc.setOutputMarkupId(true);
		add(vc);
		add(form);
		
		la = new Label("result", " ");
		la.setEscapeModelStrings(true);
		vc.addOrReplace(la);
		
		
		AjaxLink<Void> al = new AjaxLink<Void>("test-sms") {
			private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					
					
						
					
					AuthToken token = new KbeeAuthToken(3600);
					tokenvalue = token.getTokenValue();
					//String message = "Código de Seguridad KBEE: " + tokenvalue;
					StringResourceModel s=new StringResourceModel("sec-token", PersonSMSEditor.this, null).setParameters(new Object []{tokenvalue});
					String message = s.getObject();
					
					try {
 

						
						@SuppressWarnings("unchecked")
						TextField<String> phone  = (TextField<String>) form.get("phone");
						String ph=phone.getValue();
						if (ph!=null) {
							
							if (ACCEPT_ALL_SIGNATURES) {
								logger.debug("Phone" + ph + " | Token -> " + message);	
							}
							else {
								ServiceLocator.getService(SmsService.class).sendMessage(new KbeeSmsMessage( ph , message));
							}

							isTokenSent = true;
							
							// itc.setVisible(isTokenSent );
							
							la = new Label("result", new Model<String>() {
								private static final long serialVersionUID = 1L;
								public String getObject() { 
									StringBuilder str = new StringBuilder();
									StringResourceModel s=new StringResourceModel("token-sent", PersonSMSEditor.this, null).setParameters(new Object []{tokenvalue});
									str.append(s.getObject());
									return str.toString();
									
								}
							});
							
							la.setEscapeModelStrings(false);
							vc.addOrReplace(la);
						}
						
					}
					
					catch (SecurityException e) {
						logger.error(e);
						StringBuilder str = new StringBuilder();

						str.append("<span class=\"danger\">");
						str.append( e.getClass().getName() + " | " + e.getMessage()  + "<br/>");
						str.append( (new StringResourceModel("remember-country-code", PersonSMSEditor.this, null).getObject()) );

						str.append("</span>");
						
						la = new Label("result", new Model<String>(str.toString()));
						la.setEscapeModelStrings(false);
						vc.addOrReplace(la);
				
					}
					catch (Exception e) {
							logger.error(e);
							la = new Label("result", new Model<String>("<span class=\"danger\">" +  e.getClass().getName() + " | " + e.getMessage() + "</span>"));
							la.setEscapeModelStrings(false);
							vc.addOrReplace(la);
					}
	 				target.add(PersonSMSEditor.this);
	 				
	 			}
		};
		vc.add(al);

		
		// ----------------------------------------------------------------------------------------------------------------
		//
		//
		
		/**
		itc = new WebMarkupContainer("input-token-container");
		itc.setOutputMarkupId(true);
		itc.setVisible(isTokenSent );
		add(itc);
		
		tvv = new org.apache.wicket.markup.html.form.TextField<String>("tokenValidation", new PropertyModel<String>(PersonSMSEditor.this, "tokenvalueValidation"));
		itc.add(tvv);
		
		AjaxLink<Void> check = new AjaxLink<Void>("check") {
			private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					la= new Label("result", new Model<String>() {
						private static final long serialVersionUID = 1L;
						public String getObject() { 
							
							StringBuilder str = new StringBuilder();
							String st=tvv.getInput();
							
							logger.debug(st);
							
							if (tokenvalue!=null && st!=null) {
								if (tokenvalue.equals(st))
									str.append("<span class=\"success\">OK </span>");
								else
									str.append("<span class=\"danger\">Error. El código enviado es -> <b>" + tokenvalue+ "</b> " + st + " </span>");
							}
							return str.toString();
							
						}
					});
					la.setEscapeModelStrings(false);
					itc.addOrReplace(la);
					target.add(PersonSMSEditor.this);
	 				
				}
		
		};
		
		check.setEnabled(true);
		itc.add(check);

		la = new Label("result", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() { 
				StringBuilder str = new StringBuilder();
				
				if (tokenvalue!=null && tokenvalueValidation!=null) {
					if (tokenvalue.equals(tokenvalueValidation))
						str.append("<span class=\"success\">OK </span>");
					else
						str.append("<span class=\"danger\">Error. El código enviado es -> <b>" + tokenvalue+ "</b></span>");
				}
				return str.toString();
				
			}
		});
		la.setEscapeModelStrings(true);
		itc.addOrReplace(la);
		*/

		
		
				
		
		
	}
	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
 			if (!getUpdatedParts().isEmpty()) {
				
 				
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				onUpdate(target);
			}
		}
		
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorPageEvent(target, e));
		}
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	

}

package com.novamens.content.web.security.markup;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.web.base.page.component.KbeeContentFooter;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class LoginPage extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage {
	 
	static final String xcss = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.kbee.css", "/css/kbee.css");
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	 
	private static final long serialVersionUID = 8691387363711079330L;

	public LoginPage() {
		this(null);
	}
	
	@SuppressWarnings("serial")
	public LoginPage(PageParameters parameters) {

		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);

		setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getProductTabTitle()));
		
		final String errorCode;
		
		if (parameters!=null)
			errorCode = parameters.get("login_error").toString();
		else
			errorCode = null;

		add(new Label("error", new Model<String>() {
			
			public String getObject() {

				if (errorCode==null)
					return "";
					
				if (errorCode.equals("1"))
					return "Error de autenticación.";

				return "";
			}
			
//			public boolean isVisible() {
//				return errorCode!=null;
//			}			
		}));
		
			
	//	err.setVisible(errorCode!=null);
	//	add(err);
	// add(new LoginForm("login-form", errorMsg));
		add((new KbeeContentFooter("footer")));
	}
					
	public class LoginForm extends Form<Void> {
		private static final long serialVersionUID = 1L;
		
		private FeedbackPanel feedback;
		
		private String username = "";
		private String password = "";
	
		
		
		public String getUsername() {return username;}
		public String getPassword() {return password;}
		
		public void setUsername(String username) {this.username=username;}
		public void setPassword(String password) {this.password=password;}
	

		
		public  LoginForm (String id) {
				this(id, null);
		}
		
	public  LoginForm (String id, String msgerror) {
			super(id);

		setOutputMarkupId(true);
		
		
			feedback = new FeedbackPanel("feedback") {
			
			private static final long serialVersionUID = 1L;
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
        
		RequiredTextField<String> user = new RequiredTextField<String>("username");
		user.setModel(new PropertyModel<String>(this, "username"));
		// add(user);
		
		PasswordTextField pwd = new PasswordTextField("password");
		pwd.setModel(new PropertyModel<String>(this, "password"));
		
		// add(pwd);
		
			if (msgerror!=null)
				feedback.info("error");
		

//			Button submit = new AjaxButton("submit", this) {
//			private static final long serialVersionUID = 1L;
//		
//			@Override 
//			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
//				
//				String msg = ((LoginForm) form).update();
//				
//				 if (msg!=null && msg.length()>0) {
//					 ((LoginForm) form).getFeedbackPanel().error(msg);
//					 onError(target, form);
//				} else {
//					
//					feedback.info(getUsername() + " " + getPassword());
//					target.add(feedback);
//					 // setResponsePage(QAHomePage.class);
//					 // verifica credenciales y pasa al Workspace
//				}
//			}
//			@Override
//			protected void onError(AjaxRequestTarget target, Form<?> form) {
//				target.add(feedback);
//			}
//		};
		
		// add(submit);
		
	}
	
//	private String update() {
//		
//		StringBuilder str= new StringBuilder();
//		
//		if (getUsername()==null) {
//			str.append("Completar: usuario");
//		}
//		
//		if (getPassword()==null){
//			if(str.length()==0)
//				str.append("Completar contraseña");
//			else
//				str.append(", contraseña");
//		}
//		
//		if(str.length()>0)
//			str.append(".");
//		
//
//		return str.toString();
//		
//	}
	
	
	public FeedbackPanel getFeedbackPanel() {
		return feedback;
	}

	//private User getUser() {
	//	return ServiceLocator.getService(SecurityService.class).getSessionUser();
	//}
}
	
	
}

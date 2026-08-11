package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.StaticField;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.FileUploadField;
import kbee.web.page.ErrorPageEvent;
import kbee.web.panel.AlertPanel;



@SuppressWarnings("serial")
public class PersonEditor extends DomainObjectEditor<Person> {
																									
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PersonEditor.class.getName());
	
	private boolean ismyaccount;


	
	public PersonEditor(String id, IModel<Person> model, boolean ismyaccount) {
		super(id, model);
		
		this.ismyaccount = ismyaccount;

		final boolean is_edit_person_enabled =
				  (!ismyaccount) 			||
			      isRoot() 					|| 
			      isAdminSessionUser() 		||
			      getModel().getObject().getProfile(UserProfile.class).isEditPersonEnabled(); // if it is my account and edit person is enabled

		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		WebMarkupContainer info = new WebMarkupContainer("account-disabled");
		info.setVisible(!is_edit_person_enabled);
		form.add(info);


		if ( getModel().getObject().getProfile(UserProfile.class).getUser().getUserName().startsWith("root@") ||
                getModel().getObject().getProfile(UserProfile.class).getUser().isCanonical())	 {

			StringResourceModel s_title=new StringResourceModel("system-user-title", this, null);
			StringResourceModel s_text=new StringResourceModel("system-user-text", this, null);
			AlertPanel<Person> a=new AlertPanel<Person>("system-user",  AlertPanel.INFO, getModel(), s_title, s_text);
			a.setIcon("fa-duotone fa-user-lock");
			form.add(a);
		}
		else {
			form.add( new InvisiblePanel("system-user"));	
		}
		

		

		
		
		
		form.add(new TextField<String>("firstName") { 
			@Override
			public boolean autofocus() {
				return true;
			};
		});
		
		TextField<String> lastname = new TextField<String>("lastName", true);
		TextField<String> email    = new TextField<String>("email", true);
				
		String sid = null;
		Person person=getModel().getObject();
		if (person instanceof PersonMember) {
			sid = ((PersonMember) person).getPerson().getId().toString(); 
		}
		else
			sid=getModel().getObject().getId().toString();
		
		StaticField<String> pid     = new StaticField<String> ("id", new Model<String>(sid));
		pid.setVisible(isRoot());
		form.add(pid);
												
		//BooleanField emailvalidated    = new BooleanField("emailValidated");
		//emailvalidated.setVisible(isAdminSessionUser());
		//emailvalidated.setEnabled(isAdminSessionUser());
		//form.add(emailvalidated);
		
		
		form.add(lastname);
		form.add(email);
		
		//TextField<String> phone = new TextField<String>("phone");
		//phone.setRequired(true);
		//form.add(phone);
		
		TextField<String> workposition = new TextField<String>("workPosition");
		workposition.setRequired(false);
		form.add(workposition);
		
		FileUploadField photo = new FileUploadField("photo") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getModelObject().setPhoto(getValue());
				getModelObject().setDefaultPhoto(false);
				setUpdatedPart("photo");
				EditEvent<Person> ev = new EditEvent<Person>(target, PersonEditor.this.getModel());
				fire(ev);
				PersonEditor.this.onUpdate(target);
			}
		};
		
		form.add(photo);
		
		/**
		form.add(new BooleanField("photoDomainLogo") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getModelObject().setPhotoDomainLogo(getValue().booleanValue());
				EditEvent<Person> ev = new EditEvent<Person>(target, PersonEditor.this.getModel());
				fire(ev);
			}
		});
		**/
		add(form);
		add(new EditButtonsV5<Person>(this) {
			@Override
			public boolean isVisible() {
				return isUserEnabled();
			}
		});
	}

	@Override
	public void update(AjaxRequestTarget target) {
		try {
 			if (!getUpdatedParts().isEmpty()) {
				
 				if (getModelObject().getEmail()!=null) {
					getModelObject().setEmail(getModelObject().getEmail().replace(",", ";"));
					getModelObject().setEmail( cleanupEmail(getModelObject().getEmail()));
					
 				}
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				onUpdate(target);
			}
		}
		
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorPageEvent(target, e));
		}
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		if (getModel().getObject().getState().equals(ObjectState.DRAFT)) {
			try {
				
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModelObject());
			}
			catch (Exception e) {
				logger.error(e);
				fire(new ErrorEvent<>(target, e));
			}
			onCancel(target);
		}
	}

	
private String cleanupEmail(String email) {
		
		if (email==null || email.length()==0)
			return email;
		
		email=email.trim().replace(";@","@").replace(",@","@");
		
		if (email.endsWith(".") || email.endsWith(";") ) {
			return email.substring(0, email.length()-1);
		}
		
		return email;
		
		
		// StringBuilder str = new StringBuilder();
		/**
		String arr[] = email.split("@");
		
		if (arr.length==1)
			return email;
		
		String name = arr[0].trim();
		String domain = arr[1].trim();
		
		if (name.endsWith(".") || name.endsWith(";") ) {
			name=name.substring(0, name.length()-1);
		}
	
		
		if (domain.endsWith(".") || domain.endsWith(";") ) {
			domain=domain.substring(0, domain.length()-1);
		}
		
		return name+"@"+domain;
		*/
	}
	public void setEmailValidated(boolean b) {
		((KbeePerson) getModelObject()).setEmailValidated(b);
	}
	
	public boolean isEmailValidated() {
		return getModelObject().isEmailValidated();
	}

	
	
	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public boolean isUserEnabled() {
		if (this.ismyaccount) {
			if (ServiceLocator.getService(UserService.class).getSessionUserProfile().isEditPersonEnabled())
				return true;
			else
				return isAdminSessionUser(); 
		}
		
		// it is not possible to edit root user unless you are root
		//
		try {
			if (getModel().getObject().getProfile(UserProfile.class).getUser().getName().startsWith("root@")) 
				return getSessionUser().getUserName().startsWith("root@");
		} catch (Exception e) {
				return false; 
		}
			
		// f the user being edited is not root, Domain Admin and Security can edit them.
		//
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
			   ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	}
	
	

}

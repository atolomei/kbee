package com.novamens.content.web.content.markup;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.email.EmailService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.email.EmailBuilderBase;
import kbee.email.EmailBuilderSendContent;
import kbee.web.content.panel.SendByEmailPanel2;



public class LabelsModal<T extends Content> extends Modal {
			
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(LabelsModal.class.getName());
	

	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	IModel<T> model;

	public LabelsModal(String id) {
		super(id);
		initialize();
		setBody(new SendByEmailPanel2<T>("body"));
	}

	/** 
	 * 
	 * 
	 */ 
	public LabelsModal(String id, IModel<T> model) {
		super(id);
		this.model = model;
		
		initialize();
		setBody(new SendByEmailPanel2<T>("body", model));
	}

	
	/**
	 * 
	 * 
	 */ 

	@SuppressWarnings({ "unchecked", "serial" })
	public void open(AjaxRequestTarget target, List<IModel<T>> selection_model) {

		setParameters(selection_model.get(0).getObject().getTitle());
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		
		title.setEscapeModelStrings(false);		
		
		modal_dialog.addOrReplace(title);
		
		((SendByEmailPanel2<T>)getBody()).setModel(selection_model.get(0));
		
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				if (button.isSubmit()) {
					SendByEmailPanel2<T> form = (SendByEmailPanel2<T>)getBody();
					String to = form.getReceiver();
					//String title = form.getModelObject().getTitle();
					String text = form.getText();
					if (isSupportUser() && !isRoot()) {
						sendByEmail(LabelsModal.this.getModel(), to,  text, null);
					}
					else {
						String[] attachment = attachmentEnabled() ? getAttachment(form.getModelObject()) : null;
						sendByEmail(LabelsModal.this.getModel(), to,  text, attachment);
					}
				}
			}
		});	
	}
	
 /**
  *  
  *  @param to
  * @param subject
  *  @param text
  * @param attachment
  */
	protected void sendByEmail(IModel<T> model, String to, String text, String[] attachment) {
		//Person sender = (Person)ServiceLocator.getService(UserService.class).getSessionUserProfile().getEntity();
		
		EmailBuilderSendContent builder = new EmailBuilderSendContent(model.getObject(), to, text);
		//EmailBuilderSendContent builder = new EmailBuilderSendContent(content, sender, to, text, attachment);
		builder.setLanguage(getUser().getLocale().getLanguage());
		builder.setSender(getUser());
		
		ServiceLocator.getService(EmailService.class).send(builder);
		
		//ServiceLocator.getService(EmailService.class).sendContentByEmail(content, sender, to,  text, attachment);
	}
	
	protected String[] getAttachment(T content) {
		if (!(content instanceof ResourceContainer))
			return null;
		int a = 0;
		List<KBFile> list = ((ResourceContainer) content).getFiles();
		
		if (list.isEmpty())
			return null;
		
		String attachment[] = new String[list.size()];
		
		for (KBFile file: list) {
			if (file.getUrl()!=null) 
				attachment[a++] = file.getUrl(); 
		}
		return attachment;
	}

	
	protected boolean attachmentEnabled() {
		return !(ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()) && 
			!ServiceLocator.getService(SecurityService.class).isRoot() &&
			!ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()));
	}
	
	
	 

	protected void initialize() {
		setTitle("modal.sendbyemail.title");
		setOutputMarkupId(true);
		setButtons(Modal.Cancel, Modal.Send);
		setModalType(Modal.MODAL_CENTER);
	}
	
	
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot( getUser() );
	}
	
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


}

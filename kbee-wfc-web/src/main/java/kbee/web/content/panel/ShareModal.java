package kbee.web.content.panel;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.email.EmailService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.wicket.markup.html.event.EmailSentEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Handler;

import kbee.email.EmailBuilderSendContent;
import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public class ShareModal<T extends Content> extends Modal {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ShareModal.class.getName());

	private IModel<T> model;
	
	public ShareModal(String id) {
		super(id);
		
		initialize();
		
		setBody(new SharePanel<T>("body", model) {
			public void onUpdate(AjaxRequestTarget target) {
				if (getPanel() instanceof SendByEmailPanel)
					replaceButtons(Modal.Cancel, Modal.Send);
				else
					replaceButtons(Modal.OK);
				target.add(ShareModal.this.get("modal-dialog"));
			}
		});
	}
	
	public ShareModal(String id, IModel<T> model) {
		super(id);
		this.model = model;
		initialize();
		setBody(new SharePanel<T>("body", model));
	}
	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}

	@SuppressWarnings({ "unchecked" })
	public void open(AjaxRequestTarget target, IModel<T> model) {

		this.model=model;
		
		setParameters(model.getObject().getTitle());
		
		if (get("modal-dialog")==null)
			super.addComponents();
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		
		title.setEscapeModelStrings(false);		
		
		modal_dialog.addOrReplace(title);
		
		((SharePanel<T>)getBody()).setModel(model);
		
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				
				if (button!=null && button.isSubmit()) {
					
					Panel panel = ((SharePanel<T>)getBody()).getPanel();
					
					if (panel instanceof SendByEmailPanel) {
						SendByEmailPanel<T> form = (SendByEmailPanel<T>)panel;
						String text = form.getText();
						List<PersonMember> receivers = form.getReceivers();
						List<String> emails = form.getToMails();
						for (PersonMember person : receivers) {
							sendByEmailTo(person, text);
						}
						for (String email : emails) {
							sendByEmailTo(email, text);
						}
						fire ( new EmailSentEvent<T>(ShareModal.this.model, target));
					}
				}
			}
		});	
	}
	
	@SuppressWarnings({ "unchecked" })
	public void open(AjaxRequestTarget target, IModel<T> model, Handler handler) {

		this.model=model;
		
		setParameters(model.getObject().getTitle());
		
		if (get("modal-dialog")==null)
			super.addComponents();
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		
		title.setEscapeModelStrings(false);		
		
		modal_dialog.addOrReplace(title);
		
		((SharePanel<T>)getBody()).setModel(model);

		super.open(target, handler);
	}
	
	private void sendByEmailTo(PersonMember person, String text) {
		
		try {
		
			String email = person.getEmail();
			
			if (email==null) { 
				logger.error("Person has email null -> " + person.getDisplayName());
				return;
			}
			
			EmailBuilderSendContent builder = new EmailBuilderSendContent();
			builder.setSender(getSessionUser());
			builder.setContent(getModel().getObject());
			builder.setTo(email);
			builder.setReceiver(person); 
			builder.setText(text);
			builder.setParameter("public-url", getPublicUrl(person));
			builder.setParameter("registration-url", getRegistrationUrl(person));
			ServiceLocator.getService(EmailService.class).send(builder);
			
		} catch (Exception e) {
			setResponsePage( new ApplicationErrorPage<T>(e));
			logger.error(e);
		}
		
	}
	
	private void sendByEmailTo(String email, String text) {

		try {

			if (email==null) {
				logger.error("Email is null");
				return;
			}
			
			EmailBuilderSendContent builder = new EmailBuilderSendContent();
			//Person sender = (Person)ServiceLocator.getService(UserService.class).getSessionUserProfile().getEntity();
			builder.setSender(getSessionUser());
			//builder.setSender(sender);
			builder.setContent(getModel().getObject());
			builder.setTo(email);
			builder.setText(text);
			builder.setParameter("public-url", getPublicUrl(null));
			ServiceLocator.getService(EmailService.class).send(builder);
		} catch (Exception e) {
			setResponsePage( new ApplicationErrorPage<T>(e));
			logger.error(e);
		}
	
	}
	

	protected void initialize() {
		setTitle("modal.share.title");
		setOutputMarkupId(true);
		setButtons(Modal.Cancel, Modal.Send);
		setModalType(Modal.MODAL_CENTER);
	}
	
	private String getRegistrationUrl(PersonMember person) {
		KbeeJson data = new KbeeJson();
		data.put("id", String.valueOf(person.getId()));
		data.put("date", person.getCreationOffsetDateTime().toString());
		data.put("domain", String.valueOf(person.getDomain().getId()));
		return person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	private String getPublicUrl(PersonMember person) {
		return getModel().getObject().getService(UrlService.class).getPublicUrl(person);
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
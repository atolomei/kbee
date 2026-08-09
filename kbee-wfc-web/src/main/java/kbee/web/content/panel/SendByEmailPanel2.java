package kbee.web.content.panel;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.StaticField;

/**
 * 
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class SendByEmailPanel2<T extends Content> extends ObjectEditor<T> {

	private static final long serialVersionUID = 1L;
	
	private String text, receiver;
	
	private boolean is_email_enabled = true;
	
	public SendByEmailPanel2(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public SendByEmailPanel2(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public void setReceiver(String to) {
		this.receiver = to;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public void setText(String note) {
		this.text = note;
	}
	
	public String getText() {
		return text;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		setText(getDefaultText());
		
		if (get("emailform")==null)
			addForm();
		else {
			((TextAreaField<String>) get("emailform:text")).setValue(getDefaultText());
		}
		
		
		String email=getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_STATUS);
		if (email!=null && (email.equals("true") || email.equals("yes") || email.equals("enabled") )) 
			this.is_email_enabled = true;
		else
			this.is_email_enabled=false;

		
		
		
		
	}	
	
	@Override
	public Form<?> getForm() {
		return (Form<?>)get("emailform");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected void addForm() {
		Form<?> form = new Form<Void>("emailform", Disposition.VERTICAL);
		
		WebMarkupContainer email = new WebMarkupContainer("service-disabled") {
			@Override
			public boolean isVisible() {
				return !is_email_enabled;
			}
		};
		
		Label disabledlabel = new Label("disabled", getLabel("service-disabled", getDomain().getId().toString()));
		disabledlabel.setEscapeModelStrings(false);
		email.add(disabledlabel);
		form.add(email);
		
		StaticField<String> contenttitle = new StaticField<String>("content", new Model<String>() {
			@Override
			public String getObject() {
				if (SendByEmailPanel2.this.getModel().getObject()==null)
					return "";
				return SendByEmailPanel2.this.getModel().getObject().getTitle();
			}
		});
		form.add(contenttitle);
		
		TextField<String> dest = new TextField<String>("destinatary", new PropertyModel<String>(this, "receiver"));
		dest.setRequired(true);
		
		form.add(dest);
		
		TextAreaField<String> t= new TextAreaField<String>("text", new PropertyModel<String>(this, "text"), 8, 10);
		t.setEscapeModelStrings(false);
		
		form.add(t);
		add(form);
	}
	
	protected String getDefaultText() {
		StringBuilder msg = new StringBuilder();
		if (getModel()==null)
			return null;
		
		List<String> description = getModel().getObject().getDescriptionAsList(getLocale().getLanguage());
		
		
		for (String str: description) 
			 msg.append(str+"\n");
		
		IModel<String> rsm = getLabel("sendbyemailpanel.text", 
			ServiceLocator.getService(UserService.class).getSessionUserProfile().getPersonFirstLastName(), 
			SendByEmailPanel2.this.getModel().getObject().getTitle(), 
			msg.toString());
		return rsm.getObject();					
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
}

package kbee.web.domain;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.service.DomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.CertificateParser;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;


@SuppressWarnings("serial")
public class DomainCertificateEditor extends DomainObjectEditor<Domain> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainCertificateEditor.class.getName());
	
	Form<?> form;
	
	
	class PEMCertificateValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			try {
				CertificateParser.Get().read(validatable.getValue());
			}
			catch (IOException | CertificateException e) {
				validatable.error(new ValidationError(e.getMessage()));
				logger.error(e);
			}
		}
	}
	
	class PEMKeyValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			try {
				CertificateParser.Get().readPlainKey(validatable.getValue());
			}
			catch (IOException | CertificateException e) {
				validatable.error(new ValidationError(e.getMessage()));
				logger.error(e);
			}
		}
	}
	
	
	public DomainCertificateEditor(String id, IModel<Domain> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
	
		AlertPanel<Void> pa=new AlertPanel<Void>("disclaimer",AlertPanel.INFO,  null, 
				null, 
				getLabel("certificate-info"));
		pa.setIcon(AlertPanel.HELP_INFO);
	
		
		form.addOrReplace(pa);
		
		TextAreaField<String> pk = new TextAreaField<String>("privateKey", 
			new PropertyModel<String>(this, "privateKey"), 
			true,
			Width.W12,
			new PEMKeyValidator(),
			20, 40);
		pk.setVisible(isRoot());
		form.add(pk);
		
		
		TextAreaField<String> cert = new TextAreaField<String>("certificate", 
			new PropertyModel<String>(this, "certificate"), 
			true,
			Width.W12,
			new PEMCertificateValidator(),
			20, 40);
		cert.setVisible(isRoot());
		form.add(cert);
		
		add(new EditButtonsV5<Domain>(this) {
			@Override
			public boolean isEnabled()  {
				if (isSupportUser() && !isRoot())
					return false;
				return isAdminSessionUser() || isFactoryAdminSessionUser() ||  isServiceAdminSessionUser();
			}
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});

	}
	
	
	
	public void update(AjaxRequestTarget target) {
		
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getService(DomainService.class).update(getUpdatedParts());
				reset();
			}

		} catch (Exception e) {
			logger.error(e);
			fire (new ErrorEvent<Domain>(target, getModel(), e));
		}
	}
	
	public String getCertificate() {
		String plaintext = null; 
		try {
			Certificate certificate = getModelObject().getCertificate();
			if (certificate!=null) {
				plaintext = CertificateParser.Get().write(certificate);
			}
		}
		catch (IOException e) {
			logger.error(e);
		}
		return plaintext;
	}
	
	public void setCertificate(String plaintext) {
		try {
			((KbeeDomain)getModelObject()).setCertificate(CertificateParser.Get().read(plaintext));
		}
		catch (IOException | CertificateException e) {
			logger.error(e);
		}
	}
	
	public String getPrivateKey() {
		String plainkey = null; 
		try {
			PrivateKey key = getModelObject().getPrivateKey();
			if (key!=null) {
				plainkey = CertificateParser.Get().writePlainKey(key);
			}
		}
		catch (IOException e) {
			logger.error(e);
		}
		return plainkey;
	}
	
	public void setPrivateKey(String plainkey) {
		try {
			((KbeeDomain)getModelObject()).setPrivateKey(CertificateParser.Get().readPlainKey(plainkey));
		}
		catch (IOException | CertificateException e) {
			logger.error(e);
		}
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
}

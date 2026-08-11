package kbee.web.domain;



import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;

public class DomainEmailSettingsEditor extends DomainObjectEditor<Domain> {

	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(DomainEmailSettingsEditor.class.getName());

	private Json json;
	
	public class DomainPropertyModel implements IModel<String> {
		private static final long serialVersionUID = 1L;
		private String key; 
		public DomainPropertyModel(String key) {
			this.key = key;
		}
		public String getObject() {
			return (String)getSettings().get(key);
		}
		public void setObject(String value) {
			getSettings().put(key, value.toString());
		}
		public void detach() {
		}
	}
	 
	public class BooleanModel implements IModel<Boolean> {
		private static final long serialVersionUID = 1L;
		private String key; 
		public BooleanModel(String key) {
			this.key = key;
		}
		public Boolean getObject() {
			return Boolean.TRUE.toString().equals(getSettings().get(key)) ? Boolean.TRUE : Boolean.FALSE;
		}
		public void setObject(Boolean value) {
			getSettings().put(key, value.toString());
		}
		public void detach() {
		}
	}
	
	public class StatusModel extends DomainPropertyModel {
		private static final long serialVersionUID = 1L;
		public StatusModel(String key) {
			super(key);
		}
		public String getObject() {
			if ("enabled".equals(super.getObject()))
				return getStringResource("service.enabled");
			else
				return getStringResource("service.disabled");
		}
		public void setObject(String value) {
			if (value.equals(getStringResource("service.enabled"))) 
				super.setObject("enabled");
			else
				super.setObject("disabled");
		}
	};

	
	/** 
	   It is important to use getModelObject() for the Domain instead of getDomain because
	   sometimes the Domain being edited is different from the user's domain.
	  
	   @param id
	   @param model
	 */
	public DomainEmailSettingsEditor(String id, IModel<Domain> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		
		/**
		WebMarkupContainer disclaimer = new WebMarkupContainer("disclaimer-basic") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return DomainEmailSettingsEditor.this.getModel().getObject().getDomainType()==DomainType.EXPRESS;
			}
		};
		add(disclaimer);
				

		WebMarkupContainer disclaimer_premium = new WebMarkupContainer("disclaimer-premium") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return DomainEmailSettingsEditor.this.getModel().getObject().getDomainType()!=DomainType.EXPRESS;
			}
		};
		add(disclaimer_premium);
		**/

		
		AlertPanel<Void> pa=new AlertPanel<Void>("disclaimer",AlertPanel.INFO,  null, 
				null, 
				getModel().getObject().getDomainType()==DomainType.EXPRESS ?
						getLabel("disclaimer-basic")
						: 
				getLabel("disclaimer-premium"));
		pa.setIcon(AlertPanel.HELP_INFO);
		addOrReplace(pa);
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<String>("eMailServiceStatus", 
				 new StatusModel(DomainSettingsService.EMAIL_SERVICE_STATUS), 
				 new PropertyModel<List<String>>(this, "status")));

		
		TextField<String> email = new TextField<String>("eMailServiceNoReply", 
				new DomainPropertyModel(DomainSettingsService.EMAIL_SERVICE_NO_REPLY))   {
					private static final long serialVersionUID = 1L;
					@Override
					protected String getAutoComplete() {
						return "email";
					}
			
					@Override
					protected String getInputType() {
						return "text";
					}
			};
		
		 
		email.setVisible(false);
		
		form.add(email);
		add(form);
		
		add(new EditButtonsV5<Domain>(this) {
			private static final long serialVersionUID = 1L;
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
	
	/**
	 * 
	 */
	public void update(AjaxRequestTarget target) {
		if (!getUpdatedParts().isEmpty()) {
			List<String> list=new ArrayList<String>();
			list.add("Email Service");
			getModelObject().getService(DomainSettingsService.class).SetValues(getSettings(), list);
			ServiceLocator.getService(UserService.class).evict();
			reset();
		}
	}
	
	public List<String> getStatus() {
		List<String> status = new ArrayList<String>();
		status.add(getStringResource("service.enabled"));
		status.add(getStringResource("service.disabled"));
		return status;
	}
	
	public Json getSettings() {
		
		if (this.json==null) {
			this.json = getModelObject().getService(DomainSettingsService.class).getValues();

			if (this.json==null) {
				this.json = new KbeeJson();
				this.json.put(DomainSettingsService.EMAIL_SERVICE_NO_REPLY, "-");
				this.json.put(DomainSettingsService.CONSOLES_PERSISTS_LABELS, "yes");
				this.json.put(DomainSettingsService.TIP_OF_THE_DAY, "no");
			}
		}
		
		if (this.json.get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY) == null) {
			this.json.put(DomainSettingsService.EMAIL_SERVICE_NO_REPLY, "-");
		}
		return this.json;
	}
	
	@Override
	public void onDetach() {
		this.json = null;
		super.onDetach();
	}
	
	protected String getStringResource(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	/**
	 *  User that edits the Form. 
	 */
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}


}

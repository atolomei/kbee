package kbee.web.domain;


import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.wicket.markup.html.form.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.UrlService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;


import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.FileUploadField;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.ResourceThumbnailImage;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

@SuppressWarnings("serial")
public class DomainSettingsEditor extends DomainObjectEditor<Domain> {
	private static final long serialVersionUID = 1L;
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainSettingsEditor.class.getName());
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_settings = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SETTINGS.getId());
 
	private Json json;

	private Boolean cabinet_template;
	private Boolean cabinet_kbase;
	private Boolean cabinet_external;
	private Boolean encryptFiles = false;
	private Boolean integrationService = false;
	private Boolean oauth = false;
	
	private IModel<String> m_description;
	private IModel<String> m_website;
	private IModel<String> m_organization;
							
	private IModel<String> m_defaultpassword;
	
	private  String logo_id;
	
	private IModel<KBFSStorageType> storage_type_model;
	private IModel<KBFile> m_kbfile = null;
	
	private  boolean logo_changed = false; 
	
	private com.novamens.service.SecurityService service = null;

	private List<ZoneAux> zlist = null;

	private static final int MINIMUM_LENGTH = 8;

	
	private class PasswordsValidator implements IFormValidator {

		public String SPECIALS = "[!@#\\$%\\^-_]";

		public boolean hasNumber(String pwd) {
			return pwd.matches(".*[0-9].*");
		}
		public boolean hasSpecials(String pwd) {
			return pwd.matches(SPECIALS);
		}
		public boolean hasCapitalLetter(String pwd) {
			return pwd.matches(".*[A-Z].*");
		}

		@Override
		public void validate(org.apache.wicket.markup.html.form.Form<?> form) {

			if (getPasswordField().getInput().getDefaultModelObject()==null) 
				return;
			
			
			String pwd1 = (String) getPasswordField().getInput().getDefaultModelObject();
			
			if (pwd1==null || pwd1.length()==0)
				return;

			if (((String) getPasswordField().getInput().getDefaultModelObject()) == null) {
				ValidationError error = new ValidationError();
				error.addKey(getClass().getSimpleName());
				getPasswordField().setError(error);
				return;
			}

			else if (((String) getPasswordField().getInput().getDefaultModelObject()).length() < MINIMUM_LENGTH) {
				ValidationError error = new ValidationError();
				error.addKey("minimunlength");
				getPasswordField().setError(error);
				return;
			}

			else if (!hasNumber(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavedigit");
				getPasswordField().setError(error);
				return;
			}

			else if (!hasCapitalLetter(pwd1)) {
				ValidationError error = new ValidationError();
				error.addKey("musthavecapitalletter");
				getPasswordField().setError(error);
				return;
			}
		}

		@Override
		public FormComponent<?>[] getDependentFormComponents() {
			return new FormComponent<?>[0];
		}
	}

	
	
	/**
	 * 
	 * 
	 *
	 */
	public class ZoneAux implements Serializable {
		private static final long serialVersionUID = 1L;
		private String key;  // US/Central
		private String zid;  // -05:00
		public ZoneAux (String key, String zid) {
			this.zid=zid;
			this.key=key;
		}
		public String getKey() {
			return key;
		}
		public String getZid() {
			return zid;
		}
		public String getLabel() {
			return String.format( "UTC%s  -  %35s" , this.zid ,this.key);
		}
	}
	
	/**
	 * 
	 * 
	 *
	 */
	public class KBFSStorageModel implements IModel<KBFSStorageType> {
		private int key;
		private KBFSStorageType object;
				
		public KBFSStorageModel(KBFSStorageType object) {
			this.object=object;	
			this.key = object.getId();
		}
		public KBFSStorageType getObject() {
			if (object==null) {
				object=KBFSStorageType.getById(key);
			}
			return object;
		}
		public void setObject(KBFSStorageType object) {
			this.key=object.getId();
			this.object=object;
		}
		public void detach() {
			this.object=null;
		}
	}
	
	/**
	 * 
	 *
	 */
	public class DomainPropertyModel implements IModel<String> {
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
	 *  IS_DOMAIN_KBEE: if the Domain where I am editing is kbee.
		It is NOT that the Domain under edition is kbee
	 */			
	
	public DomainSettingsEditor(String id, IModel<Domain> model) {
		super(id, model);
		
		
		final boolean IS_DOMAIN_KBEE = isDomainKbee();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<Domain> form = new Form<Domain>("form", getModel(), Disposition.VERTICAL);
		
		IModel<ZoneAux> zonemodel = new IModel<ZoneAux>() { 
			private static final long serialVersionUID = 1L;
			public ZoneAux getObject() {
				String tz  = getModelObject().getTimeZone();
				ZoneId localzid= ZoneId.systemDefault();
				ZoneAux back  = null;
				for (ZoneAux zone: getZones()) {
						
					if (zone.getKey().equals(tz))
							return zone;
						
					if (zone.getKey().equals(localzid.getId()))
							back=zone;
				}
				if (back!=null)
					return back;
				
				return getZones().get(0);
				
				
			}
			public void setObject(ZoneAux du) {
				getModelObject().setTimeZone(du.getKey());
			}
			public void detach() {
			} 
		};
											
		form.add(new ChoiceField<ZoneAux> ("timezone", zonemodel, new PropertyModel<List<ZoneAux>>(this,"zones"), true) {
			protected IModel<String> getHelpText() {
				return null;
			}
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			
		});
		
		form.add(new StaticField<String>("name", new Model<String>(getModelObject().getName())));
		
		
		this.oauth			 	= Boolean.valueOf(model.getObject().isOAuthAuthentication());
		this.cabinet_template 	= Boolean.valueOf(model.getObject().isCabinetTemplate());
		this.cabinet_kbase 		= Boolean.valueOf(model.getObject().isCabinetKnowledgeBase());
		this.cabinet_external 	= Boolean.valueOf(model.getObject().isCabinetExternal());
		this.m_defaultpassword  = new Model<String>(model.getObject().getDefaultPassword()); 

		
		this.logo_id = model.getObject().getLogoUrl();
		
		form.add(new ChoiceField<Locale>("locale", new PropertyModel<List<Locale>>(this, "locales"), true) {
			protected String getDisplayValue(Locale value) {
				return value.getDisplayLanguage(getSessionUser().getLocale());
			}
			protected String getIdValue(Locale value) {
				return value.getLanguage();
			}
		});
		
		WebMarkupContainer eidc = new WebMarkupContainer("eidcontainer") {
			@Override
			public boolean isVisible() {
				return (isDomainKbee() || isRoot());
			}
		};
		
		add(eidc);
		
		WebMarkupContainer pc = new WebMarkupContainer("portals-container") {
			@Override
			public boolean isVisible() {
				
				if (isExpressVersion())
					return false;
				
				
				return true;
			}
		};
		pc.add(new BooleanSwitchField("portalLibrary") {
			@Override
			public boolean isBorder() {
				return true;
			}
		});
		form.add(pc);
		
		
		TextField<String> eid = new TextField<String>("externalId") {
			@Override
			public boolean isEnabled() {
				return isDomainKbee() || isRoot();
			}
  			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "External Id"; }, getText("externalid.helptext"));
			}
		};
 		eidc.add(eid);
		
		storage_type_model = new KBFSStorageModel(model.getObject().getStorageType());
		
		eidc.add(new ChoiceField<KBFSStorageType> ("storageType", storage_type_model,  new PropertyModel<List<KBFSStorageType>>(this, "storageTypes"), true) {
			@Override
			protected String getDisplayValue(KBFSStorageType value) {
				return value.getDisplayName();
			}
			@Override
			protected String getIdValue(KBFSStorageType value) {
				return String.valueOf(value.getId());
			}
			
			@Override
			public boolean isEnabled() {
				return isDomainKbee() && role_admin;
			}
		});
		form.add(eidc);

		this.encryptFiles= model.getObject().isEncryptFiles();
		
		eidc.add(new BooleanField("encryptFiles",  new PropertyModel<>(this, "encryptFiles")) {
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return isDomainKbee() && role_admin;
			}
		});
		
		this.integrationService = model.getObject().hasIntegrationService();
		
		eidc.add(new BooleanField("integrationService",  new PropertyModel<>(this, "integrationService")) {
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return role_admin;
			}
		});

		form.add(pc);

		m_description = new Model<String>(model.getObject().getDescription());
		m_website = new Model<String>(model.getObject().getWebsite());
		m_organization = new Model<String>(model.getObject().getOrganization());
				
		TextField<String> org = new TextField<String>("organization", m_organization);
		TextAreaField<String> des = new TextAreaField<String>("description", m_description);
		des.setRows(10);
		TextField<String> website = new TextField<String>("website", m_website);
		org.setRequired(true);
	
		form.add(org);
		form.add(des);
		form.add(website);
		
		form.add(new TextField<String>("vanityUrl") {
			@Override
			protected IModel<String> getHelpText() {
				return new StringResourceModel("vanityUrl.help", DomainSettingsEditor.this).setParameters( 
						DomainSettingsEditor.this.getModelObject().getService(UrlService.class).getServerUrl() 
				);
			}
			@Override
			public boolean isEnabled() {
				return isDomainKbee() || isRoot();
			}
		});
				
		form.add(new TextField<String>("defaultpassword", m_defaultpassword));
		form.add(new PasswordsValidator());
		
		form.add(new BooleanField("oauth",  new PropertyModel<>(this, "oauth")) {
			@Override
			public boolean isEnabled() {
				return isDomainKbee() || isRoot();
			}
		});
		
		// Type: Express | Enterprise
		//
		// IS_DOMAIN_KBEE: if the Domain where I am is kbee.
		// It is NOT that the Domain under edition is kbee, the Domain under edition is getModelObject()
		//
		if (!IS_DOMAIN_KBEE || model.getObject().getDomainType()==DomainType.SYSTEM)
			form.add(new StaticField<String>("type", new Model<String>(getModelObject().getDomainType().getLabel(getUser().getLocale()))));
		else {
			// Selector Dropdown [ Express | Enterprise ] 
			form.add(new StaticField<String>("type", new Model<String>(getModelObject().getDomainType().getLabel(getUser().getLocale()))));
		}
		
		Image img = null;

		if (this.logo_id!=null) 
			m_kbfile = new ObjectModel<KBFile>((KBFile) getContentDao().findResourceById(KBFile.class, this.logo_id));
		
		if (m_kbfile!=null) 
			img = new ResourceThumbnailImage<>("imglogo", new ObjectModel<Resource>( (Resource) m_kbfile.getObject()), ThumbnailSize.MINI);
		else
		 	img = new InvisibleImage("imglogo");
 		

		FileUploadField photo = null;
		
		if (m_kbfile!=null && m_kbfile.getObject()!=null) {
			photo = new FileUploadField("logo", m_kbfile) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					KBFile file = getValue();
					if (file!=null) {
						((KbeeDomain) getModelObject()).setLogoUrl(file.getId().toString());
						DomainSettingsEditor.this.logo_id=file.getId().toString();
					}
					else {
						((KbeeDomain) getModelObject()).setLogoUrl(null);
						DomainSettingsEditor.this.logo_id=null;
					}
					DomainSettingsEditor.this.logo_changed = true;
					DomainSettingsEditor.this.setUpdatedPart("Logo");
					addImage(file);
					target.add(DomainSettingsEditor.this);
				}
			};
		}
		else {
			photo = new FileUploadField("logo") {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					KBFile file = getValue();
					if (file!=null) {
						((KbeeDomain) getModelObject()).setLogoUrl(file.getId().toString());
						DomainSettingsEditor.this.logo_id=file.getId().toString();
					}
					else {
						((KbeeDomain) getModelObject()).setLogoUrl(null);
						DomainSettingsEditor.this.logo_id=null;
					}
					DomainSettingsEditor.this.logo_changed = true;
					DomainSettingsEditor.this.setUpdatedPart("Logo");
					addImage(file);
					target.add(DomainSettingsEditor.this);
				}
			};
		}

		WebMarkupContainer logoc=new WebMarkupContainer( "logoc") {
			public boolean isVisible() {
				return true;
			}
		};
			
		WebMarkupContainer imagec=new WebMarkupContainer( "image-container") {
			public boolean isVisible() {
				return ((KbeeDomain) DomainSettingsEditor.this.getModelObject()).getLogo()!=null;
			}
		};
		
		logoc.add(photo);
		logoc.add(imagec);
		imagec.add(img);

		form.add(logoc);
		
		add(form);
		
		add(new EditButtonsV5<Domain>(this) {
			@Override
			public boolean isEnabled()  {
				if (isSupportSessionUser() && !isRoot())
						return false;
				return isAdminSessionUser() || isFactoryAdminSessionUser() ||  isServiceAdminSessionUser() || role_settings;
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
		
		add(new InfoDialog("help-modal"));

	}
	
	public Boolean getCabinetTemplate() {
		return this.cabinet_template;
	}
	
	public Boolean getCabinetKnowledgeBase()  {
		return this.cabinet_kbase;
	}
	
	public Boolean getCabinetExternal() {
		return this.cabinet_external;
	}
	

	public void  setCabinetTemplate(Boolean b) {
		this.cabinet_template=b;
	}
	
	public void  setCabinetKnowledgeBase(Boolean b)  {
		this.cabinet_kbase=b;
	}
	
	public void  setCabinetExternal(Boolean b) {
		this.cabinet_external=b;
	}
	
	
	/**
	 *  <p> {@code getModelObject() (DomainSettingsEditor.this.getModel().getObject()) }
	 *  is used because we must update the values of this Domain, which is normally the 
	 *  same as getDomain(), except when the Domain parameters are
	 *  edited from the kbee Domain.</p>
	 *  
	 */			
	@Override
	public void update(AjaxRequestTarget target) {

		if (!getUpdatedParts().isEmpty()) {
			
			try {
				
				getModelObject().getService(DomainSettingsService.class).SetValues(getSettings());
				getModelObject().setCabinetExternal(this.getCabinetExternal().booleanValue());
				getModelObject().setCabinetKnowledgeBase(this.getCabinetKnowledgeBase().booleanValue());
				getModelObject().setCabinetTemplate(this.getCabinetTemplate().booleanValue());
				getModelObject().setOAuthAuthentication(this.getOauth().booleanValue());
				
				//((KbeeDomain) getModelObject()).setLanguage(getLocale().getLanguage());
				
				if (this.storage_type_model.getObject()!=null && getModelObject().getStorageType().getId()!=this.storage_type_model.getObject().getId())
					getModelObject().setStorageType(this.storage_type_model.getObject());


				// We dont support encryption on KBFS1
				//
				if (this.storage_type_model.getObject()!=null && this.storage_type_model.getObject()==KBFSStorageType.KBFS1)
					getModelObject().setEncryptFiles(false);
				else
					getModelObject().setEncryptFiles(this.getEncryptFiles());
				
				if (this.logo_changed) {  
					if (this.logo_id!=null) {
						((KbeeDomain) getModelObject()).setLogoUrl(this.logo_id);
					}
					else {
						((KbeeDomain) getModelObject()).setLogoUrl(null);
						((KbeeDomain) getModelObject()).setLogoUrl(null);
					}
				}
							
				getModelObject().setDescription(this.m_description.getObject());
				getModelObject().setOrganization(this.m_organization.getObject());
				getModelObject().setWebsite(this.m_website.getObject());
				
				((KbeeDomain) getModelObject()).setIntegrationService(getIntegrationService());
				
				if (this.m_defaultpassword.getObject()!=null)
					((KbeeDomain)getModelObject()).setDefaultPassword(this.m_defaultpassword.getObject().trim());
				
				getModelObject().getService(DomainService.class).update(getUpdatedParts());
				ServiceLocator.getService(UserService.class).evict();
				reset();
				
				
			} catch (ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
			}
		}
	}

	public void cancel() {
		this.logo_id = getModelObject().getLogoUrl();
		if (this.logo_id!=null) { 
			KBFile kbfile = (KBFile) getContentDao().findResourceById(KBFile.class, this.logo_id);
			Image img = new ResourceThumbnailImage<>("imglogo", new ObjectModel<Resource>((Resource) kbfile), ThumbnailSize.MINI);
			((Form<?>) get("form:logoc")).replace(img);
		}
	}
    
	
	public List<String> getStatus() {
		List<String> status = new ArrayList<String>();
		status.add(getStringResource("service.enabled"));
		status.add(getStringResource("service.disabled"));
		return status;
	}
	
	/**
	 * 
	 * If the Json settings was not created for some reason when the Domain was created.
	 * an empty Json should be enough.
	 * 
	 * @return
	 */
	public Json getSettings() {
		if (json==null) {
			json = getModelObject().getService(DomainSettingsService.class).getValues();
			if  (json==null)
				json = new KbeeJson();
		}
		return json;
	}
	
	@Override
	public void onDetach() {
		json = null;
		service=null;
		if (m_kbfile!=null)
			m_kbfile.detach();
		super.onDetach();
	}

	
	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
	}

//	public Locale getLocale() {
//		return getModelObject().getLocale();
//	}
//
//	public void setLocale(Locale locale) {
//		((KbeeDomain) getModelObject()).setLocale(locale);
//	}
	
	public List<Locale> getLocales() {
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		return locales;
	}

	
	
	public List<ZoneAux> getZones() {
		if (zlist!=null)
			return zlist;
		zlist = new ArrayList<ZoneAux>();
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			Map<String, String> map = service.getOrderedZoneIds();
			map.forEach((k, v) -> {zlist.add(new ZoneAux(k, v));});
			return zlist;
	}
	
	

	protected String getStringResource(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}

	
	/**
	 *  This is the Domain of the User that edits the form. If the form is being edited
	 *  by root @ kbee , then this refers to Domain kbee and not 
	 *  to the Domain under edition.
	 */

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	/**
	 *  User that edits the Form
	 */
	protected KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	public Boolean getEncryptFiles() {
		return encryptFiles;
	}

	public void setEncryptFiles(Boolean encryptFiles) {
		this.encryptFiles = encryptFiles;
	}
	
	public Boolean getIntegrationService() {
		return integrationService;
	}

	public void setIntegrationService(Boolean value) {
		this.integrationService = value;
	}

	public Boolean getOauth() {
		return oauth;
	}

	public void setOauth(Boolean b) {
		this.oauth = b;
	}
	
	
	public List<KBFSStorageType> getStorageTypes() {
		
		List<KBFSStorageType> list = new ArrayList<KBFSStorageType>();
		
		//list.add(KBFSStorageType.KBFS1);	

		list.add(KBFSStorageType.Minio);
		list.add(KBFSStorageType.Odilon);
		
		if (ServiceLocator.getService(BrandingService.class).isKbee())
			list.add(KBFSStorageType.AmazonS3);
		
		return list;
	}

	
	public TextField<?> getPasswordField() {
		return (TextField<?>) get("form:defaultpassword");
	}
	
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	@SuppressWarnings("unused")
	private com.novamens.service.SecurityService getSecurityService() {
		if (this.service!=null)
			return this.service;	
		this.service = ServiceLocator.getService(com.novamens.service.SecurityService.class);
		return this.service;
	}
		

	private void addImage(KBFile kbfile) {
		Image img = null;
		if (kbfile!=null) {
		 img = new ResourceThumbnailImage("imglogo", new ObjectModel<Resource>((Resource) kbfile), ThumbnailSize.MINI);
		}
		else {
			img = new InvisibleImage("imglogo");
		}
		((WebMarkupContainer) get("form:logoc:image-container")).replace(img);
	}

	public IModel<String> getDefaultPassword() {
		return new Model<String> (getModel().getObject().getDefaultPassword());
	}
	
	public void setDefaultPassword(IModel<String> password) {
		((KbeeDomain) getModel().getObject()).setDefaultPassword(password.getObject());
	}


	
}

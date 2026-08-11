package kbee.web.security.user;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.entity.Person;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DomService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSignature;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.FileUploadField;
import kbee.web.panel.AlertPanel;
import kbee.web.resource.WebResourceReference;


/**
 * STATUS -> 
 * 
 * DEVICE_STATUS
 *  
 *  Initial 
	Registering
	Registered
 *
 */
@SuppressWarnings("serial")
public class UserSignaturesEditor extends DomainObjectEditor<UserProfile> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserSignaturesEditor .class.getName());
	static Boolean SIMULATE_SIGNATURE = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("simulate-hand-written-signature", "no").trim());
	
	private static final long serialVersionUID = 1L;
				
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_external = !is_root && !is_domain_admin && ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.EXTERNAL_USER.getId());
	
	/** --------------------------------------------------
	 * 
	 *
	 */
	
	static final int   DEVICE_NONE_REGISTERED 	= 1;
	static final int   DEVICE_REGISTERING 		= 2;
	static final int   DEVICE_REGISTERED		= 3;
	
	
	//private int DEVICE_STATUS = DEVICE_NONE_REGISTERED;
	
	private WebMarkupContainer  	signaturePanel;
	private DeviceRegisterPanel 	deviceRegisterPanel;
	private DeviceArchivePanel  	deviceArchivePanel;

	//private SignaturesArchivePanel  signatureArchivePanel;
	//private SignatureUploadPanel    signatureUploadPanel;
	//private WebMarkupContainer  	signatureTypePanel;
	
	
	private Boolean hasHWSignature;
	
	private List<IModel<UserDevice>> enabled_devices;

	
	
	/***
	 * @param id
	 * @param model
	 */
	public UserSignaturesEditor(String id, IModel<UserProfile> model) {
		super(id, model);

		setOutputMarkupId(true);
		setEditionEnabled(false);

		// GENERAL
		//
		AlertPanel<UserProfile>  no_s= new AlertPanel<UserProfile>("alert-info", AlertPanel.INFO, getModel(), null, getLabel("to-sign"));
		no_s.setIcon(AlertPanel.HELP_INFO);
		addOrReplace(no_s);
		
		add(new NoDevicePanel());
		add(new DevicesPanel()); 
		//addDevicePanel(); 
		
		// DEVICE REGISTER 	----------------------------------------------
		//
		deviceRegisterPanel =new DeviceRegisterPanel();
		deviceRegisterPanel.setVisible(false);
		add(deviceRegisterPanel);
		
		
		// DEVICE ARCHIVE 	----------------------------------------------
		//
		deviceArchivePanel = new DeviceArchivePanel();
		deviceArchivePanel.setVisible(false);
		add(deviceArchivePanel);
		
		
		// SIGNATURE TYPE ----------------------------------------------
		add( new InvisiblePanel("signature-type"));
		
		
		// SIGNATURE PANEL ----------------------------------------------
		
		signaturePanel = new SignaturePanel();
		add(signaturePanel);
		
		
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (enabled_devices!=null)
			enabled_devices.forEach(item -> item.detach());
	}
	
	public Person getPerson() {
		return getModelObject().getPerson();
	}
	
	
	public List<IModel<UserDevice>>  getEnabledDevices() {
		
		if (enabled_devices!=null)
				return enabled_devices; 
		
		enabled_devices = new ArrayList<IModel<UserDevice>>();
		
		for (IModel<UserDevice> model : getDevices()) {
			if (model.getObject().getState().equals(ObjectState.ENABLED)) {
				enabled_devices.add(model);
			}
		}
		return enabled_devices;
	}
	
	
	public KbeeUserDevice getDevice() {
		for (IModel<UserDevice> model : getDevices()) {
			if (model.getObject().getState().equals(ObjectState.ENABLED)) {
				return (KbeeUserDevice)model.getObject();
			}
		}
		return null;
	}
	
	public List<IModel<UserDevice>> getArchivedDevices() {
		List<IModel<UserDevice>> devices = new ArrayList<IModel<UserDevice>>();
		for (IModel<UserDevice> model : getDevices()) {
			if (model.getObject().getState().equals(ObjectState.ARCHIVED)) {
				devices.add(model);
			}
		}
		return devices;
	}
	
	public List<IModel<UserDevice>> getDevices() {
		List<IModel<UserDevice>> devices = new ArrayList<IModel<UserDevice>>();
		for (UserDevice device : getModelObject().getDevices()) {
			devices.add(new ObjectModel<UserDevice>(device));
		}
		return devices;
	}

	// La ultima firma registrada (subida)
	public KbeeUserSignature getSignature() {
		KbeeUserSignature signature = null;
		for (IModel<UserSignature> model : getSignatures()) {
			if (model.getObject().getState().equals(ObjectState.ENABLED)) {
				KbeeUserSignature s = (KbeeUserSignature)model.getObject();
				if (signature==null || s.getLastModifiedOffsetDateTime().isAfter(signature.getLastModifiedOffsetDateTime()))
					signature = s; 
			}
		}
		return signature;
	}
	
	public List<IModel<UserSignature>> getArchivedSignatures() {
		List<IModel<UserSignature>> signatures = new ArrayList<IModel<UserSignature>>();
		for (IModel<UserSignature> model : getSignatures()) {
			if (model.getObject().getState().equals(ObjectState.ARCHIVED)) {
				signatures.add(model);
			}
		}
		return signatures;
	}
	
	public List<IModel<UserSignature>> getSignatures() {
		List<IModel<UserSignature>> signatures = new ArrayList<IModel<UserSignature>>();
		for (UserSignature signature : getModelObject().getSignatures()) {
			signatures.add(new ObjectModel<UserSignature>(signature));
		}
		return signatures;
	}
	
	protected void delete(UserSignature signature) {
		getModelObject().getPerson().getService(PersonService.class).delete(signature);
	}
	
	protected void delete(UserDevice device) {
		getModelObject().getPerson().getService(PersonService.class).delete(device);
	}
	
	protected void restore(UserDevice device) {
		getModelObject().getPerson().getService(PersonService.class).updateDevice(device);
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}
	
	protected String format(OffsetDateTime date) {
		return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(date);
	}
	
	protected Boolean isHWSignature() {
		if (hasHWSignature==null)
			hasHWSignature = Boolean.valueOf(getSignatureImage() !=null);
		return hasHWSignature;
	}
	
	protected KBFile getSignatureImage() {
		if (getSignature()==null) return null;
		KBFile imagefile = getSignature().getHandWriteImage();
		return imagefile;
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	
	/**
	 * 
	 * DEVICE
	 * 
	 * 
	 */
//	private void addDevicesPanel() {
//		add(new DevicesPanel());
//		
//	}
//	private void addDevicePanel() {
//		
//		
//		// DEVICE -----------------------------------------------------------
//		//
//		WebMarkupContainer devicepanel = new WebMarkupContainer("device") {
//			public boolean isVisible() {
//				return getDevice()!=null;
//			}
//		};
//		
//		add(devicepanel);
//
//		WebMarkupContainer icon = new WebMarkupContainer("phone-icon");
//		devicepanel.add(icon);
//				
//		if (getDevice()!=null && getDevice().isAndroid())
//			icon.add( new AttributeModifier("class" , "fa-brands fa-android android-color"));
//		else if (getDevice()!=null && getDevice().isIOS())
//			icon.add( new AttributeModifier("class" , "fa-brands fa-apple apple-color"));
//		else
//			icon.add( new AttributeModifier("class" , "fa-regular fa-mobile generic-phone-color"));
//		
//		// ------------------------------------
//		//
//		// Alta Manual -> nro
//		// Alta Android -> modelo y otros datos
//		//
//		devicepanel.add(new Label("device", getDevice() !=null ? getDevice().getDescription() : "" ));
//		devicepanel.add( (new Label("number", getDevice() !=null ? getDevice().getNumber() : "" )).setVisible(getDevice()!=null && getDevice().getNumber()!=null));
//		
//		
//		devicepanel.add(new Label("date", getDevice() !=null ? getDevice().getCreationOffsetDateTimeColloquial() : "" ));	
//		
//		Label sms=new Label("web-registered",	new StringResourceModel( getDevice()!=null && getDevice().isWebRegistered() ? "webapp-registered" : "android-registered", this, null));
//		devicepanel.add(sms);
//
//		devicepanel.add(new AjaxLink<Void>("removedevice-link") {
//
//			@Override
//			public boolean isVisible() {
//				
//				if (is_root || is_domain_admin)
//					return true;
//				
//				return getSessionUser().getId().equals(UserSignaturesEditor.this.getModel().getObject().getUser().getId());
//			}
//			
//			public void onClick(AjaxRequestTarget target) {
//				getConfirmationDialog().open(target, 
//					null,
//					getLabel("removedevice.confirmation.message"), 
//					getLabel("removedevice.confirmation.text"),
//					
//					Dialog.Delete, 
//					new Dialog.Handler() {
//						@Override
//						public void onClick(AjaxRequestTarget target, Button button) {
//							if (button.key().equals(Dialog.Delete.key())) {
//								if (getDevice()!=null)
//									delete(getDevice());
//								target.add(UserSignaturesEditor.this);
//							}
//						}
//				});
//			}
//		});
//		
//		devicepanel.add(new AjaxLink<Void>("devicearchive-link") {
//			public void onClick(AjaxRequestTarget target) {
//				deviceArchivePanel.setVisible(true);
//				deviceArchivePanel.show(target);  // Fragment
//			}
//			
//			@Override
//			public boolean isVisible() {
//				return !getArchivedDevices().isEmpty();
//			}
//		});
//
//		
//		
//	}
	
	/**
	 * 
	 * 
	 *  Signature Upload
	 *  
	 *
	 */
	public class SignatureUploadPanel extends Fragment implements Editor<KbeeUserSignature> {

//		private boolean isvisible = false;
		IModel<KBFile> signaturePhoto;
		IModel<KbeeUserSignature> model;
		
		Form<?> form;
		
		
		public IModel<KbeeUserSignature> getModel() {
			return model;
		}
				
		public void  setModel(IModel<KbeeUserSignature> model) {
			this.model=model;
		}
		
		/**
		 * 
		 * 
		 */
		public SignatureUploadPanel() {
			super("signature-upload", "signature-upload-fragment", UserSignaturesEditor.this);
			
			setOutputMarkupId(true);
			
			KbeeUserSignature sd=getSignature();
			if (sd!=null) {
				setModel(new ObjectModel<KbeeUserSignature>(sd));
			
				KBFile f=getSignatureImage();
				
				if (f!=null)
					 setSignaturePhoto(new ObjectModel<KBFile>(f));
				 
			}
			form = new Form<Void>("form", Disposition.VERTICAL);
			
			
			FileUploadField photo = new FileUploadField("handWriteImage", getSignaturePhoto()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {

					if (getValue()!=null) {
						setSignaturePhoto(new ObjectModel<KBFile>(getValue()));
						//getModelObject().setPhoto(getValue());
						//getModelObject().setDefaultPhoto(false);
						setUpdatedPart("signature image");
					}
					//EditEvent<Person> ev = new EditEvent<Person>(target, PersonEditor.this.getModel());
					//fire(ev);
					//PersonEditor.this.onUpdate(target);
				}
			};
			
			form.add(photo);
			add(form);
			
			add(new AjaxLink<Void>("save") {
				public void onClick(AjaxRequestTarget target) {
					
					if (getSignaturePhoto()!=null && getSignaturePhoto().getObject()!=null) { 
						KbeeUserSignature s= getSignature();
						if (s!=null) {
							s.setHandWriteImage(getSignaturePhoto().getObject());
							s.setLastModifiedUser( getSessionUser());
							s.setLastModifiedOffsetDateTime(OffsetDateTime.now());
							s.getService(DomService.class).update(getUpdatedParts());
						}
					}
					SignatureUploadPanel.this.setVisible(false);
					target.add(UserSignaturesEditor.this);
				}
			});
			
			add(new AjaxLink<Void>("cancel") {
				public void onClick(AjaxRequestTarget target) {
					SignatureUploadPanel.this.setVisible(false);
					target.add(UserSignaturesEditor.this);
				}
			});
			
			
		}

		public IModel<KBFile> getSignaturePhoto() {
			return this.signaturePhoto;
		}
		

		public void setSignaturePhoto( IModel<KBFile> photo) {
			this.signaturePhoto = photo;
		}

		
		
		public void show(AjaxRequestTarget target) {
			//isvisible = true;
			setVisible(true);
			target.add(this);
		}


		@Override
		public void update(AjaxRequestTarget target) {
			// TODO Auto-generated method stub
			logger.debug("update");
		}


		@Override
		public void edit(AjaxRequestTarget target) {
			// TODO Auto-generated method stub
			logger.debug("edit");
		}

		@Override
		public org.apache.wicket.markup.html.form.Form<?> getForm() {
			return form;
		}


		@Override
		public KbeeUserSignature getModelObject() {
			return getModel().getObject();
		}

		@Override
		public void update(KbeeUserSignature object) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean isEditionEnabled() {
			return true;
		}


		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public void setIsNew(boolean isnew) {
		}

		@Override
		public boolean isNew() {
			return false;
		}


		@Override
		public boolean isFullWidth() {
			return false;
		}


		@Override
		public List<UpdatedField> getUpdatedFields() {
			return new ArrayList<UpdatedField>();
		}


		@Override
		public void setUpdatedField(UpdatedField updatedField) {
		}

		@Override
		public List<String> getUpdatedParts() {
			return new ArrayList<String>();
		}


		@Override
		public void setUpdatedPart(String updatedPart) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/** ----------------------------------------------
	 *	SIGNATURE ARCHIVE
	 */
	public class SignaturesArchivePanel extends Fragment {
		
		private boolean archivevisible = false;
		
		WebMarkupContainer archivetable;
		
		public void onDetach() {
			super.onDetach();
			if (archivetable!=null && archivetable.get("signature")!=null)
				archivetable.get("signature").detach();
		}
		
		public SignaturesArchivePanel() {
			super("signatures-archive", "signatures-archive-fragment", UserSignaturesEditor.this);
			
			setOutputMarkupId(true);
			
			archivetable = new WebMarkupContainer("archive-table") {
				@Override
				public boolean isVisible() {
					return archivevisible;
				}
			};
			
			archivetable.add(new ListView<IModel<UserSignature>>("signature", () -> getArchivedSignatures()) {
				@Override
				public void populateItem(ListItem<IModel<UserSignature>> item) {
					KbeeUserSignature signature = (KbeeUserSignature)item.getModelObject().getObject();
					item.add(new Image("image", signature.getHandWriteImage()!=null ?  new WebResourceReference(signature.getHandWriteImage()) : null) {
						public boolean isVisible() {
							return ((KbeeUserSignature)item.getModelObject().getObject()).getHandWriteImage()!=null;
						}
					});
					item.add(new WebMarkupContainer("icon") {
						public boolean isVisible() {
							return ((KbeeUserSignature)item.getModelObject().getObject()).getHandWriteImage()==null;
						}
					});	
					item.add(new Label("date", signature.getCreationOffsetDateTimeColloquial()));	
					item.add(new Label("archived", format(signature.getLastModifiedOffsetDateTime())));	
					item.add(new Label("device", signature.getDevice().getDescription()));	
					item.add(new Label("state", signature.getState().getLabel( getSessionUser().getLocale())));	
				}
			});
			
			add(archivetable);
			
			archivetable.add(new AjaxLink<Void>("close-link") {
				public void onClick(AjaxRequestTarget target) {
					archivevisible = false;
					target.add(SignaturesArchivePanel.this);
				}
			});
		}
		
		public void show(AjaxRequestTarget target) {
			archivevisible = true;
			target.add(this);
		}
	}	

	

	
	/** --------------------------------------------------
	 * 
	 * NO DEVICE
	 * 
	 */
	public class NoDevicePanel extends Fragment {

		WebMarkupContainer  nodevicePanel;
		WebMarkupContainer  actionsPanel;
		
		public NoDevicePanel() {
			super("no-device-panel", "no-device-fragment", UserSignaturesEditor.this);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
		
			// NO DEVICE ----------------------------------------------
			
			nodevicePanel = new WebMarkupContainer("nodevice") {
				public boolean isVisible() {
					return getDevice()==null && !deviceRegisterPanel.isVisible();
				}
			};
			
			
			AlertPanel<UserProfile>  no_device_s = new AlertPanel<UserProfile>("nodevice-alert", AlertPanel.DANGER, getModel(), 
					new StringResourceModel("register", this, null),			
					new StringResourceModel("nodevice", this, null));
			no_device_s.setIcon(AlertPanel.ATTENTION);
			nodevicePanel.add(no_device_s);
			add(nodevicePanel);
			
//			 actionsPanel = new WebMarkupContainer("actions");
//			 actionsPanel.setVisible( is_root || is_domain_admin || (getSessionUser()==getModel().getObject().getUser()));
//			 				
//			 nodevicePanel.add(actionsPanel);
//
//			 actionsPanel.add(new AjaxLink<Void>("devicearchive-link") {
//				public void onClick(AjaxRequestTarget target) {
//					deviceArchivePanel.setVisible(true);
//					deviceArchivePanel.show(target);
//				}
//				
//				public boolean isVisible() {
//					return !getArchivedDevices().isEmpty();
//				}
//			});
			
			
		}
	}

		
	
	/** --------------------------------------------------
	 * DEVICES PANEL
	 */
	public class DevicesPanel extends Fragment {
		
		public DevicesPanel() {
			super("devices", "devices-fragment", UserSignaturesEditor.this);
			
			setOutputMarkupId(true);
			
			add(new ListView<IModel<UserDevice>>("device", () -> getDevices()) {

				public void populateItem(ListItem<IModel<UserDevice>> item) {
					
					KbeeUserDevice device = (KbeeUserDevice)item.getModelObject().getObject();
					
					item.add(new Label("description", device.getDescription()));	
					item.add(new Label("registered", format(device.getRegistrationTime())));	

					//item.add(new Label("archived", 
					//	ObjectState.ARCHIVED.equals(device.getState()) ?
					//	format(device.getLastModifiedOffsetDateTime()) :
					//	""));	
					
					item.add(new Label("state", getStateLabel(device)));
					item.add(new AjaxLink<Void>("delete-link") {
						@Override
						public void onClick(AjaxRequestTarget target) {
							getConfirmationDialog().open(target, 
									null,
									getLabel("removedevice.confirmation.message"), 
									getLabel("removedevice.confirmation.text"), 
									Dialog.Delete, 
									new Dialog.Handler() {
										@Override
										public void onClick(AjaxRequestTarget target, Button button) {
											if (button.key().equals(Dialog.Delete.key())) {
												delete(item.getModelObject().getObject());
												target.add(DevicesPanel.this);
											}
										}
									});
						}
						@Override
						public boolean isVisible() {
							return ObjectState.ENABLED.equals(device.getState());
						}
					});
					item.add(new AjaxLink<Void>("restore-link") {
						public void onClick(AjaxRequestTarget target) {
							getConfirmationDialog().open(target, 
									null,
									getLabel("restoredevice.confirmation.message"), 
									getLabel("restoredevice.confirmation.text"), 
									Dialog.Ok, 
									new Dialog.Handler() {
										@Override
										public void onClick(AjaxRequestTarget target, Button button) {
											if (button.key().equals(Dialog.Ok.key())) {
												restore(item.getModelObject().getObject());
												target.add(DevicesPanel.this);
											}
										}
									});
						}
						@Override
						public boolean isVisible() {
							return ObjectState.ARCHIVED.equals(device.getState());
						}
					});
				}
			});
		}
		public String getStateLabel(UserDevice device) {
			if (ObjectState.ENABLED.equals(device.getState())) {
				return getLabelString("enabled.label");
			}
			else {
				String label = getLabelString("archived.label");
				label += " (" + format(device.getLastModifiedOffsetDateTime()) + ")";
				return label;
			}
		}

	}
	
	/** --------------------------------------------------
	 * DEVICE ARCHIVE
	 */
	public class DeviceArchivePanel extends Fragment {
		
		private boolean archivevisible = false;
		
		public DeviceArchivePanel() {
			super("device-archive", "device-archive-fragment", UserSignaturesEditor.this);
			
			setOutputMarkupId(true);
			
			WebMarkupContainer devicearchivetable = new WebMarkupContainer("device-archive-table") {
				@Override
				public boolean isVisible() {
					return archivevisible;
				}
			};
			
			devicearchivetable.add(new ListView<IModel<UserDevice>>("device", () -> getArchivedDevices()) {
				public void populateItem(ListItem<IModel<UserDevice>> item) {
					KbeeUserDevice device = (KbeeUserDevice)item.getModelObject().getObject();
					item.add(new Label("description", device.getDescription()));	
					item.add(new Label("registered", format(device.getRegistrationTime())));
					item.add(new Label("archived", format(device.getLastModifiedOffsetDateTime())));	
					item.add(new Label("state", device.getState().getLabel( getSessionUser().getLocale())));
				}
			});
			
			devicearchivetable.add(new AjaxLink<Void>("close-link") {
				public void onClick(AjaxRequestTarget target) {
					archivevisible = false;
					target.add(DeviceArchivePanel.this);
				}
			});
			
			add(devicearchivetable);
		}
		
		public void show(AjaxRequestTarget target) {
			archivevisible = true;
			target.add(this);
		}

	}

	
	/** --------------------------------------------------
	 *	SIGNATURE TYPE
	 */
	public class SignatureTypePanel extends Fragment {
				
		public SignatureTypePanel() {
			super("signature-type", "signature-type-fragment", UserSignaturesEditor.this);
			setOutputMarkupId(true);
			String s_t;
			if (getSignature()!=null) {
				s_t=getSignature().getType().getLabel( getSessionUser().getLocale());
			}
			else
				s_t="N/A";
			Label type = new Label("type", s_t);
			add(type);
		}
		
		
	}
	/** --------------------------------------------------
	 *	DEVICE REGISTER
	 */
	public class DeviceRegisterPanel extends Fragment {
	
		static final int STEP_DEVICE_INITIAL 		= 0; 
		static final int STEP_DEVICE_REGISTERING 	= 1;
		static final int STEP_DEVICE_REGISTERED 	= 2;
		
		private int deviceRegisterPanel_state = STEP_DEVICE_INITIAL;
		
		
		private String number;
		private TextField<String> number_f;

		
		public class NumberValidator implements IValidator<String> {
			public NumberValidator() {
			}
			@Override
			public void validate(final IValidatable<String> validatable) {
				String number = validatable.getValue();
				if (!number.matches("[\\-|0-9]+") || number.length()>25) {
					validatable.error(new ValidationError(this, "number.error"));
					return;
				}
			}
		}
		
		
		public DeviceRegisterPanel() {
			super("device-register", "device-register-fragment", UserSignaturesEditor.this);
			
			setOutputMarkupId(true);
			
			setNumber(getPerson().getPhone());
			
			WebMarkupContainer wizard = new WebMarkupContainer("wizard") {
				public boolean isVisible() {
					return  deviceRegisterPanel_state > STEP_DEVICE_INITIAL;
				}
			};
			
			WebMarkupContainer step1 = new WebMarkupContainer("step1") {
				public boolean isVisible() {
					return  deviceRegisterPanel_state == STEP_DEVICE_REGISTERING;
				}
			};
			
			number_f = new TextField<String>("number", new PropertyModel<String>(this, "number"), false, new NumberValidator()) {
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					validate();
					updateModel();
				}
				protected boolean isInputEnabled() {
					return true;
				}
			};
		
			step1.add(number_f);
			
			step1.add(new AjaxLink<Void>("register") {
				public void onClick(AjaxRequestTarget target) {
					if (validNumber() ) {
						registerDevice();
						 deviceRegisterPanel_state = STEP_DEVICE_REGISTERED;
					}
					target.add(UserSignaturesEditor.this);
				}
			});
			
			step1.add(new AjaxLink<Void>("cancel") {
				public void onClick(AjaxRequestTarget target) {
					deviceRegisterPanel_state = STEP_DEVICE_INITIAL;
					deviceRegisterPanel.setVisible(false);
					target.add(UserSignaturesEditor.this);
				}
			});
			
			wizard.add(step1);
			
			WebMarkupContainer step2 = new WebMarkupContainer("step2") {
				public boolean isVisible() {
					return deviceRegisterPanel_state == STEP_DEVICE_REGISTERED;
				}
			};
			
			Label email_sent = new Label("registered-message",	new StringResourceModel("registered.message", UserSignaturesEditor.this, null).setParameters(new Object[] {UserSignaturesEditor.this.getModel().getObject().getEntity().getEmail()}));
			email_sent.setEscapeModelStrings(false);
			step2.add(email_sent);
					
			step2.add(new AjaxLink<Void>("ok") {
				public void onClick(AjaxRequestTarget target) {
					deviceRegisterPanel_state = STEP_DEVICE_INITIAL;
					target.add(UserSignaturesEditor.this);
				}
			});
			
			wizard.add(step2);
			
			add(wizard);
		}
		
		public boolean validNumber() {
			number_f.validateModel();
			return !number_f.hasErrorMessage();
		}
		
		public void show(AjaxRequestTarget target) {
			deviceRegisterPanel_state = STEP_DEVICE_REGISTERING; 
			number_f.onBeforeRender();
			target.focusComponent(number_f.getInput());
			target.add(UserSignaturesEditor.this);
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
		}
		
		private void registerDevice() {
			getPerson().getService(PersonService.class).registerDevice(getDevice());
		}
		
		private UserDevice getDevice() {
			KbeeUserDevice device = new KbeeUserDevice();
			device.setDeviceId(getNumber());
			//device.setDescription();
			device.setNumber(getNumber());
			device.setUserProfile(getModelObject());
			device.setDomain(getDomain());
			//device.setWebResgi
			return device;
		}
	}
	
	
	/** --------------------------------------------------
	 * SIGNATURE PANEL
	 */
	public class SignaturePanel extends Fragment {
		
		
		public SignaturePanel() {
			super("signature-panel", "signature-fragment", UserSignaturesEditor.this);
			
			WebMarkupContainer signature = new WebMarkupContainer("signature") {
				public boolean isVisible() {
					return getSignature()!=null;
				}
			};
			add(signature);
			
			signature.add(new Image("image", () -> new WebResourceReference(getSignatureImage())) {
				public boolean isVisible() {
					return isHWSignature();
				}
			});
			
			signature.add(new WebMarkupContainer("icon") {
				public boolean isVisible() {
					return !isHWSignature();
				}
			});
			
			signature.add(new Label("date",  (getSignature() != null ? format(getSignature().getCreationOffsetDateTime()) : "")));
			
			UserDevice device = getSignature()!=null ? getSignature().getDevice() : null;
			
			if (device==null)
				signature.add(new Label("device", ""));
			else
				signature.add(new Label("device",
					device.isWebRegistered() ?
					getLabelString("generated-by-webapp") :
					device.getDescription()));

			add(new WebMarkupContainer("nosignature") {
				public boolean isVisible() {
					return getSignature()==null;
				}
			});
		}
		
		public boolean isVisible() {
			return getSignature()!=null;
		}
	}	
	
}
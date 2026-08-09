package kbee.web.eform;


import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.SignedData;

import com.novamens.content.entity.Person;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.sms.KbeeSmsMessage;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.markup.html.event.SignEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.PanelCapture;
import com.novamens.security.AuthToken;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.sms.SmsService;
import com.novamens.whatsapp.HsmComponent;
import com.novamens.whatsapp.HsmParameter;
import com.novamens.whatsapp.WhatsAppService;
import com.novamens.whatsapp.HsmComponent.Section;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;

import kbee.util.PropertiesFactory;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.panel.AlertPanel;
import kbee.web.resource.WebResourceReference;
import kbee.web.workflow.FeedbackPanel;
import kbee.web.workflow.task.KbeeWebWorkflowEvent;



/**
 * 
 * 
 * 
 * 
 *
 */

@SuppressWarnings("serial")
public class ESignaturePanel extends ModelPanel<EFormData>  {
	private static final long serialVersionUID = 1L;
	
	static Boolean ACCEPT_ALL_SIGNATURES = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("accept-all-signatures", "no").trim());
	static Boolean SIMULATE_SIGNATURE = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("simulate-hand-written-signature", "no").trim());	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ESignaturePanel.class.getName());
	
	private boolean inError = false;
	
	/** -------------------------------------------------
	 *  NOT SIGNED -> SIGN BUTTON 
	 */

	public class NotSignedState extends Fragment {
		public NotSignedState() {
			super("state", "not-signed-state-fragment", ESignaturePanel.this);
			add(new AjaxLink<Void>("sign-button") {
				public void onClick(AjaxRequestTarget target) {
					try {
					onSign(target);
					} catch (Exception e) {
						logger.error(e);
						fire (new ErrorEvent<>(target, e));
					}
				}
				public boolean isEnabled() {
					return isEditionEnabled();
				}
				public boolean isVisible() {
					return  ((com.novamens.kbee.content.workflow.KbeeTaskForm) ESignaturePanel.this.getModel().getObject().getForm()).isSignatureRequired();
				}
			});
		}
		
		public void onSign(AjaxRequestTarget target) {
		}
		
	}

	
	
	/** -------------------------------------------------
	 * 
	 * 
	 * 
	 */
	public class SmsSignatureState extends Fragment {
		
		final int STATE_METHOD_UNDEFINED=0;
		final int STATE_APP_TOKEN=200;
		
		final int STATE_SMS_NOT_SENT=10;
		final int STATE_SMS_SENT=20;
		
		private int step = STATE_METHOD_UNDEFINED;
		
		private String token, tokensended, token_feedback=null;
		
		private boolean sms=false, whatsApp=false;
		
		int alert_type = AlertPanel.INFO;
		
		private TextField<String> smsToken;
		
		WebMarkupContainer sign_app_token;
		WebMarkupContainer sign_sms;
		TextField<String> f_token;
		WebMarkupContainer sign_token_container;
		
		/**
		 * 
		 */
		public SmsSignatureState() {
			super("state", "smssignature-state-fragment", ESignaturePanel.this);
			
			setOutputMarkupId(true);
			
					
			
			sign_token_container = new WebMarkupContainer("sign-selection-container") {
				public boolean isVisible() {
					return step==STATE_METHOD_UNDEFINED;
				}
			};
			addOrReplace(sign_token_container);

			StringResourceModel str = new StringResourceModel("sign-explanation", this, null).setParameters( new Object[] {getPerson().getPhone()!=null?getPerson().getPhone():"[n/a]"});
			Label l_str = new Label("sign-explanation", str);
			l_str.setEscapeModelStrings(false);
			
			sign_token_container.add(l_str);
			

			
			
			sign_token_container.add(new WorkingAjaxLink<Void>("app-token-selector") {
				@Override
				public boolean isEnabled() {
					if (getSessionUser()==null) 
						return false;
					return true;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					token_feedback=null;
					step=STATE_APP_TOKEN;
					target.add(SmsSignatureState.this);
				}
			});

			sign_token_container.add(new WorkingAjaxLink<Void>("sms-token-selector") {
				@Override
				public boolean isEnabled() {
					if (getSessionUser()==null)	return false;
					return true;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					token_feedback=null;
					sendToken();
					sms = true;
					whatsApp = false;
					step = STATE_SMS_SENT;
					smsToken.onBeforeRender();
					smsToken.setHelpVisible();
					target.focusComponent(smsToken.getInput());
					target.add(SmsSignatureState.this);
				}
			});
			
			sign_token_container.add(new WorkingAjaxLink<Void>("whatsapp-token-selector") {
				@Override
				public boolean isEnabled() {
					if (getSessionUser()==null)	return false;
					//if (!getUserProfile().isWhatsAppEnabled()) return false;
					return true;
				}
				@Override
				public boolean isVisible() {
					//if (!getUserProfile().isWhatsAppEnabled()) return false;
					return true;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					token_feedback=null;
					sendWhatsAppToken();
					whatsApp = true;
					sms = false;
					step = STATE_SMS_SENT;
					smsToken.onBeforeRender();
					smsToken.setHelpVisible();
					target.focusComponent(smsToken.getInput());
					target.add(SmsSignatureState.this);
				}
			});

			
			/*----------------------- APP ---------------------------------*/
			
			sign_app_token = new WebMarkupContainer("sign-with-app") {
				public boolean isVisible() {
					return step==STATE_APP_TOKEN;
				}
			};
			addOrReplace(sign_app_token);
			sign_app_token.setVisible(true);
			
			f_token = new TextField<String>("token", new PropertyModel<String>(this, "token")) {
				public void onUpdate(AjaxRequestTarget target) {
					updateModel();
				}
				protected boolean isInputEnabled() {
					return true;
				}
			};
			
			sign_app_token.add(f_token);
					
			sign_app_token.add(new WorkingAjaxLink<Void>("sign-button") {
				@Override
				public boolean isEnabled() {
					if (getSessionUser()==null) 
							return false;
					return true;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (ACCEPT_ALL_SIGNATURES || validToken()) {
						onSigned(target);
					}
					else {
						setDanger(getLabelString("token.invalid"));
					}
					target.add(SmsSignatureState.this);
				}
				@Override
				public String getIndicatingLabel() {
					return getLabelString("signing");
				}
			});
			
			sign_app_token.add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
			});
			
			
			/*----------------------- SMS ---------------------------------*/
			
			sign_sms = new WebMarkupContainer("sign-with-sms") {
				public boolean isVisible() {
					return step==STATE_SMS_SENT || step==STATE_SMS_NOT_SENT;
				};
			};
				
			addOrReplace(sign_sms);
			
			WebMarkupContainer step1 = new WebMarkupContainer("step1") {
				public boolean isVisible() {
					return step==STATE_SMS_NOT_SENT;
				}
			};
			
			
			step1.add(new AjaxLink<Void>("sendtoken-button") {
				public void onClick(AjaxRequestTarget target) {
					sendToken();
					step = STATE_SMS_SENT;
					smsToken.onBeforeRender();
					smsToken.setHelpVisible();
					target.focusComponent(smsToken.getInput());
					target.add(SmsSignatureState.this);
				}
			});
			
			step1.add(new AjaxLink<Void>("cancel1-button") {
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
			});
			
			sign_sms.add(step1);
			
			WebMarkupContainer step2 = new WebMarkupContainer("step2") {
				public boolean isVisible() {
					return step==STATE_SMS_SENT;
				}
			};
			
			Label securityLabel = new Label("security-token-info", new Model<String>() {
				public String getObject() {
					return sms ? getLabelString("sms-token-info") : getLabelString("whatsapp-token-info");
				}
			});
			securityLabel.setEscapeModelStrings(false);
			sign_sms.add(securityLabel);
			
			smsToken = new TextField<String>("smstoken", new PropertyModel<String>(this, "token")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updateModel();
				}
				@Override
				protected boolean isInputEnabled() {
					return true;
				}
			};
			
			step2.add(smsToken);
					
			step2.add(new AjaxLink<Void>("resendtoken-button") {
				public void onClick(AjaxRequestTarget target) {
					if (sms) 
						sendToken();
					else
						sendWhatsAppToken();
					if (!hasErrors()) {
						SmsSignatureState.this.smsToken.clearInput();
						setInfo(getLabelString("token.sentsms"));
					}
					target.add(SmsSignatureState.this);
				}
			});
			step2.add(new WorkingAjaxLink<Void>("sign-button") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					
					if (validToken()) {
						onSigned(target);
					}
					else {
						setDanger(getLabelString("token.invalid"));
					}
					target.add(SmsSignatureState.this);
				}
				@Override
				public String getIndicatingLabel() {
					return getLabelString("signing");
				}
			});
			step2.add(new AjaxLink<Void>("cancel2-button") {
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
			});
			sign_sms.add(step2);
						
			add(new AlertPanel<EFormData>("feedback", AlertPanel.INFO,  getModel(), null, () -> getFeedbackMessage()) {
				public boolean isVisible() {
					return getFeedbackMessage()!=null;
				}
				protected int getType() {
					return alert_type;
				}
			});
		}
		public Component getTokenInput() {
			f_token.onBeforeRender();
			return f_token.getInput();
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public void onSigned(AjaxRequestTarget target) {
		}
		public void onCancel(AjaxRequestTarget target) {
		}
		
		public String getFeedbackMessage() {
			return token_feedback;
		}
		public boolean hasErrors() {
			return AlertPanel.DANGER==alert_type;
		}
		protected void setInfo(String message) {
			alert_type = AlertPanel.INFO;
			token_feedback = message;
		}
		protected void setDanger(String message) {
			alert_type = AlertPanel.DANGER;
			token_feedback = message;
		}
		protected void sendToken() {
			token_feedback = null; 
			if (ACCEPT_ALL_SIGNATURES) {
				logger.debug("No Sms sent because ACCEPT_ALL_SIGNATURES = 'yes'");
				return;
			}
			try {
				String phoneNumber = getPhoneNumber();
				if (phoneNumber!=null) {
					AuthToken token = ServiceLocator.getService(UserService.class).getAuthToken();
					tokensended = token.getTokenValue();
					String message = getLabelString("send-token",tokensended); 
					ServiceLocator.getService(SmsService.class).sendMessage(new KbeeSmsMessage(phoneNumber, message));
				}
				else {
					setDanger(getLabelString("nophone.error"));
				}
			}
			catch (Exception e) {
				logger.error(e);
				setDanger(e.getClass().getName() + (e.getMessage()!=null?(" | " + e.getMessage()):"") );
			}
		}
		protected void sendWhatsAppToken() {
			token_feedback = null; 
			if (ACCEPT_ALL_SIGNATURES) {
				logger.debug("No WhatsApp sent because ACCEPT_ALL_SIGNATURES = 'yes'");
				return;
			}
			try {
				String phoneNumber = getPhoneNumber();
				if (phoneNumber!=null) {
					AuthToken token = ServiceLocator.getService(UserService.class).getAuthToken();
					tokensended = token.getTokenValue();
			   		List<HsmComponent> components = new ArrayList<>();
		    		
		    		HsmComponent component = new HsmComponent(Section.Header);
		    		component.setParameters(new HsmParameter("text", getDomain().getDisplayName()));
		    		components.add(component);
		    		
		    		component = new HsmComponent(Section.Body);
		    		component.setParameters(new HsmParameter("text", tokensended));
		    		components.add(component);
		    		
		    		ServiceLocator.getService(WhatsAppService.class).startConversation("security_token", phoneNumber, components);
				}
				else {
					setDanger(getLabelString("nophone.error"));
				}
			}
			catch (Exception e) {
				logger.error(e);
				setDanger(e.getClass().getName() + (e.getMessage()!=null?(" | " + e.getMessage()):"") );
			}
		}
		protected String getPhoneNumber() {
			String number = getPerson().getPhone();
			if (number!=null && "".equals(number.trim())) number = null;
			return number;
		}
		protected boolean validToken() {
			if (ACCEPT_ALL_SIGNATURES) {
				logger.debug("No Validation because ACCEPT_ALL_SIGNATURES = 'yes'");
				return true;
			}
			try {
				String token = tokensended==null ? 
					ServiceLocator.getService(UserService.class).getAuthToken().getTokenValue() :
					tokensended;	
				String tokenInput = getToken()!=null ? getToken().trim() : null;
				return tokenInput!=null && token!=null && tokenInput.equals(token);
			}
			catch (Exception e) {
				return false;
			}
		}
	}
	
	
	
	
	
	
	/** ----------------------------------------
	 * 
	 * SIGNATURE INFO 
	 * 
	 * If the document is Signed
	 * 
	 *
	 */
	public class SignedState extends Fragment {
		
	
		public SignedState() {
			super("state", "signed-state-fragment", ESignaturePanel.this);
			add(new Image("image", getSignatureImage()!=null?new WebResourceReference(getSignatureImage()):null) {
				public boolean isVisible() {
					return getSignatureImage()!=null;
				}
			});
			add(new WebMarkupContainer("icon") {
				public boolean isVisible() {
					return getSignatureImage()==null;
				}
			});	
			
			add(new Label("user", () -> getUserName(getSignature())));
			
																	
			WebMarkupContainer dni_container = new WebMarkupContainer("dni-container");
			add(dni_container);
			String idt=getIdType(getSignature());
			String idn=getUserId(getSignature());
			
			dni_container.setVisible((idt!=null && idt.length()>0) || (idn!=null && idn.length()>0));
			dni_container.add(new Label("tipodni", () -> idt));
			dni_container.add(new Label("userid", () -> idn ));
			
			add(new Label("device", () -> getDate(ESignaturePanel.this.getModelObject().getSignatures().get(0))));
			
			
			add(new AjaxLink<Void>("remove-link") {
				
				@Override
				public boolean isEnabled() {
					return isEditionEnabled() || userSignature();
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					getConfirmationDialog().open(target, 
							getLabel("removesignature.confirmation.message", getUserName(getSignature())), 
							new Button("button.remove","btn btn-sm btn-danger"), 
							new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals("button.remove")) {
										unsign();
										onUnsign(target);
									}
								}
							});
				}
			});
			
			
			add(new ConfirmationDialog("confirmation-dialog"));
		}
		
		Boolean hasHWSignature;
		protected Boolean isHWSignature() {
			if (hasHWSignature==null)
				hasHWSignature = Boolean.valueOf(getSignatureImage() !=null);
			return hasHWSignature;
		}
		
		protected KBFile getSignatureImage() {
		
			KBFile imagefile = getSignature().getHandWriteImage();

			return imagefile;
		}
		
		protected Index getQueryIndex() {
			return getDomain().getService(JavaIndexerService.class).getIndex();
		}
		protected Domain getDomain() {
			return (Domain)ServiceLocator.getService(UserService.class).getDomain();
		}
		
		protected KbeeUserSignature getSignature() {
			UserSignature signature = getModelObject().getSignatures().get(0).getSignature();
			return (KbeeUserSignature)signature;
			
		}
		public String getUserName(KbeeUserSignature signature) {
			return signature.getUserProfile().getUser().getFirstLastName();
		}
		public String getUserId(KbeeUserSignature signature) {
			Person pe=signature.getUserProfile().getPerson();
			String s=pe.getService(PersonService.class).getIdentityDocument();
			return s!=null?s:"";
		}
									
		public String getIdType(KbeeUserSignature signature) {
			Person pe=signature.getUserProfile().getPerson();
			String s=pe.getService(PersonService.class).getIdentityType();
			return s!=null?s:"";
		}
		
		
		public boolean userSignature() {
			return getSignature().getUser().equals(getSessionUser());
		}
		protected void onUnsigned(AjaxRequestTarget target) {
		}
		protected String getDate(SignedData data) {
			User user = getSessionUser();
			Locale locale = user != null ? getSessionUser().getLocale()  : Locale.getDefault();
			String zid = user != null ? getSessionUser().getTimeZone()  : ZoneId.systemDefault().getId();
					
			String date = ServiceLocator.getService(DateTimeService.class).format(
					data.getDate(), 
					zid, 
					locale,
					DateTimeService.Day_Month_Year_hh_mm_ss_zzz );
			return date;
		}
		protected ConfirmationDialog getConfirmationDialog() {
			return (ConfirmationDialog) get("confirmation-dialog");
		}
	}


	

	/** -------------------------------------
	 * 
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	
	public ESignaturePanel(String id, IModel<EFormData> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public Content getContent() {
		return getModelObject() instanceof EFormContentData ? ((EFormContentData)getModelObject()).getContent() : null;
	}
	
	public boolean validate() {
		return true;
	}
	
	public boolean inError() {
		return inError;
	}
	
	public boolean isEditionEnabled() {
		return false;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getModelObject().isSigned())
			setSignedState(null);
		else
			setNotSignedState(null);
		
		
	}
	
	public void refresh(AjaxRequestTarget target) {
		if (inError()) {
			if (validate()) {
				setNotSignedState(target);
			}
			else {
				setErrorState(target);
			}
		}
	}

	@Override
	public void onAfterRender() {
		super.onAfterRender();
	}
	
	
	/**
	 * @param target
	 */
	protected void setSignatureState(AjaxRequestTarget target) {
		
		inError = false;
		
		/** -------------------
		 * SMS
		 */
		//if (isSignatureTypeSMS()) {
			addOrReplace(new SmsSignatureState() {
				public void onSigned(AjaxRequestTarget target) {
					try {
						
						logger.debug(" onSigned( AjaxRequestTarget target ) ");
						sign();
						setSignedState(target);
						
						logger.debug("Si el proceso tiene accion default -> entonces se ejecuta ");
						EndCondition action = getDefaultAction();
						if (action!=null) {
							fire(new KbeeWebWorkflowEvent(action.getEvent(), action.getLabel(), target));
						}
					}
					catch (Exception e) {
						
						logger.error(e);
						
						StringBuilder str = new StringBuilder();
						str.append(e.getClass().getName());
						str.append(e.getMessage()!=null? (" | " + e.getMessage()):"");
						

						ESignaturePanel.this.error(str.toString());
						setErrorState(target);
					}
				}
				@Override
				public void onCancel(AjaxRequestTarget target) {
					setNotSignedState(target);
				}
			});
		//}

		target.add(this);
	}
	
	/**
	 * 
	 * 
	 * @throws SignatureException
	 */
	protected void sign() throws SignatureException {
		
		
		if (getContent()!=null) {
			
			UserSignature signature = getSignature(SignatureType.SMS);
			
			if (signature==null || signature.getCertificate()==null) {
				signature = getPerson().getService(PersonService.class).updateSignature(getSmsDevice() );
		 	}
			
			getContent().getService(ContentService.class).sign(getModelObject(),  getSnapshot(getModelObject()), signature, getSignatureHtmlStream(signature));
			
		}
	}
	
	protected void unsign() {
		if (getContent()!=null) {
			getContent().getService(ContentService.class).unsign(getModelObject());
		}
	}
	
	public String getSignatureHtmlStream(UserSignature signature) {
		ResourceReference css  = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
		String url = String.valueOf((RequestCycle.get().urlFor(css, null)));
		String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		String prefix = "<html><head>";
		prefix += "<link rel=\"stylesheet\" type=\"text/css\" href=\""+absoluteUrl+"\">";
		// prefix += "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,400;0,900;1,400&amp;display=swap\">";
		prefix += "</head><body>";
        PanelCapture capture = new PanelCapture(new EPdfSignaturePanel(signature));
        String sufix="</body></html>";		
        String capturestring = capture.getString();											
        String stream = prefix + capturestring + sufix;
        return stream;
 	}
	
	protected void onUnsign(AjaxRequestTarget target) {
		setNotSignedState(target);
	}
	
	protected void setSignedState(AjaxRequestTarget target) {
		inError = false;
		addOrReplace(new SignedState() {
			protected void onUnsigned(AjaxRequestTarget target) {
				setNotSignedState(target);
			}
		});
		
		if (target!=null) {
			target.add(this);
			fireScanAll (new SignEvent(target, true));		
		}
	}
	
	
	/**
	 * @param target
	 */
	protected void setNotSignedState(AjaxRequestTarget target) {
		inError = false;
		addOrReplace(new NotSignedState() {
			@Override
			public void onSign(AjaxRequestTarget target) {
				setSignatureState(target);
			}
		});
		
		if (target!=null) {
			target.add(this);
			
			fireScanAll (new SignEvent(target, false));
		}
	}
	
	protected void setErrorState(AjaxRequestTarget target) {
		inError = true;
		addOrReplace(new FeedbackPanel("state"));
		if (target!=null)
			target.add(this);
	}
	
	protected UserSignature getSignature(SignatureType type) {
		for (UserSignature signature : getUserProfile().getSignatures()) {
			if (type.equals(signature.getType()) && ObjectState.ENABLED.equals(signature.getState())) {
				return signature;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * - Firma en Dispositivo móvil 
	 * 
	 * - Firma Web -> SMS
	 * - Firma Web -> Token
	 * 
	 */
	
	
	protected boolean isSignatureWebSupported() {
		return true;
		
	}
	
	protected UserDevice getSmsDevice() {
		for (UserDevice device : getUserProfile().getDevices()) {
			if (ObjectState.ENABLED.equals(device.getState()) && device.getNumber()!=null) {
				return device;
			}
		}
		return null;
	}
	
	protected boolean hasDevice() {
		for (UserDevice device : getUserProfile().getDevices()) {
			if (ObjectState.ENABLED.equals(device.getState()) && device.getNumber()==null) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean hasSignature() {
		return getSignature(SignatureType.PHONE_APP)!=null;
	}
	
	protected String getSnapshot(EFormData data) {
		return (new EFormCapture(data)).getString(); 
	}
	
	protected Person getPerson() {
		return getUserProfile().getPerson();
	}
	
	protected UserProfile getUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
	
	/**
	 * 
	 * @return
	 */
	protected EndCondition getDefaultAction() {

		UserTask task = (UserTask)getContent().getService(WorkflowService.class).getTask();
		
		for (EndCondition action : task.getEndConditions()) {
			if (action.isDefault()) {
				return action;
			}
		}
		
		
		return null;
	}
	
	protected User getSessionUser() {
        try {
            return ServiceLocator.getService(SecurityService.class).getSessionUser();
        } 
        catch (Exception e) {
        	logger.error(e);
            return null;
        }
    }


	/** -------------------------------------------------
	 * Sign with APP token
	 * 
	 * 
	 */
	public class SignatureState extends Fragment {
		
		private String token, sign_feedback=null;
		
		private Label signatureLabel = null;
		
		public SignatureState() {
			super("state", "signature-state-fragment", ESignaturePanel.this);
			
			setOutputMarkupId(true);
			
			add(new TextField<String>("token", new PropertyModel<String>(this, "token")) {
				public void onUpdate(AjaxRequestTarget target) {
					updateModel();
				}
				protected boolean isInputEnabled() {
					return true;
				}
			});
			
			add(new WorkingAjaxLink<Void>("sign-button") {
				@Override
				public boolean isEnabled() {
					if (getSessionUser()==null) 
							return false;
					return true;
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (ACCEPT_ALL_SIGNATURES || validToken()) {
						onSigned(target);
					}
					else {
						sign_feedback = getLabel("token-invalid").getObject();
						signatureLabel.add(new AttributeModifier("class", "alert alert-danger"));
						
					}
					target.add(SignatureState.this);
				}
				@Override
				public String getIndicatingLabel() {
					return getLabelString("signing");
				}
			});
			
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
			});
			
			
			signatureLabel = new Label("feedback", ()->getFeedbackMessage()) {
				public boolean isVisible() {
					return getFeedbackMessage()!=null;
				}
			};
			signatureLabel.setOutputMarkupId(true);
			add(signatureLabel);
		}
		
		public void onSigned(AjaxRequestTarget target) {
		}
		public void onCancel(AjaxRequestTarget target) {
		}
		public String getFeedbackMessage() {
			return sign_feedback;
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
			((Field<?>)get("token")).onBeforeRender();
			((TextField<?>)get("token")).setHelpVisible();
		}
		
		public Component getTokenInput() {
			((Field<?>)get("token")).onBeforeRender();
			return ((Field<?>)get("token")).getInput();
		}
		
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		
		
		protected boolean validToken() {
			if (logger.isDebugEnabled() && ACCEPT_ALL_SIGNATURES)
				return true;
			try {
				AuthToken token = ServiceLocator.getService(UserService.class).getAuthToken();
				String tokenInput = getToken()!=null ? getToken().trim() : null;
				return tokenInput!=null && Long.valueOf(token.getTokenValue()).equals(Long.valueOf(tokenInput));
			}
			catch (Exception e) {
				logger.error(e);
				return false;
			}
		}
	}

}
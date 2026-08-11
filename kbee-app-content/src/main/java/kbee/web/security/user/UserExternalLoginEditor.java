package kbee.web.security.user;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.kbee.content.user.KbeeUserExternalLoginPlatform;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

@SuppressWarnings("serial")
public class UserExternalLoginEditor extends DomainObjectEditor<UserProfile> {

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private static Logger logger = Logger.getLogger(UserEditor.class.getName());

	private Boolean googleAuth = Boolean.valueOf(false);
	private Boolean facebookAuth = Boolean.valueOf(false);

	private String googleUserPlatformId;
	private String facebookUserPlatformId;
	
    public UserExternalLoginEditor(String id, IModel<UserProfile> model) {
        super(id, model);

        setOutputMarkupId(true);
        setEditionEnabled(false);
    }
    
    
    
    public void onInitialize() {
    	super.onInitialize(); 
        
    	setUp();
        
		setGoogleAuth(hasAuthPlatform(ExternalPlatformId.GOOGLE));
		setFacebookAuth(hasAuthPlatform(ExternalPlatformId.FACEBOOK));

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		
		
		form.add(new TextField<String>("googleUserPlatformId", new PropertyModel<String>(UserExternalLoginEditor.this, "googleUserPlatformId")));
		form.add(new BooleanSwitchField("googleAuth", new PropertyModel<Boolean>(this, "googleAuth")));
		
		form.add(new BooleanSwitchField("facebookAuth", new PropertyModel<Boolean>(this, "facebookAuth")));
		form.add(new TextField<String>("facebookUserPlatformId", new PropertyModel<String>(UserExternalLoginEditor.this, "facebookUserPlatformId")));
		
		
		add(form);
		
		add(new EditButtonsV5<UserProfile>(this) {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return role_admin;
			}
		});    	
    		
    }
    
    private void setUp() {

    	UserProfile profile = getProfile();
		
    	for (UserExternalLoginPlatform p : profile.getUserExternalLoginPlatforms()) {
    		if (p.getPlatformId()==ExternalPlatformId.GOOGLE.getId() ) {
    			googleUserPlatformId = p.getUserPlatformId();
			}
    		if (p.getPlatformId()==ExternalPlatformId.FACEBOOK.getId() ) {
    			facebookUserPlatformId = p.getUserPlatformId();
			}
    	}
    	
    	/**
    	if (googleUserPlatformId==null && hasAuthPlatform(ExternalPlatformId.GOOGLE)) {
			googleUserPlatformId = getProfile().getPerson().getEmail();
			return;
		}

    	if (facebookUserPlatformId==null && hasAuthPlatform(ExternalPlatformId.FACEBOOK)) {
    		facebookUserPlatformId = getProfile().getPerson().getEmail();
			return;
		}
		**/
    	
    }
    
    public String getGoogleUserPlatformId() {
    	return googleUserPlatformId;
    }
    
    public void setGoogleUserPlatformId( String s) {
    	googleUserPlatformId=s;
    }
    
    				
    public String getFacebookUserPlatformId() {
    	return facebookUserPlatformId;
    }
    
    public void setFacebookUserPlatformId( String s) {
    	facebookUserPlatformId=s;
    }

    
    public Boolean getGoogleAuth() {
		return googleAuth;
	}

	public void setGoogleAuth(Boolean googleAuth) {
		this.googleAuth = googleAuth;
	}

	public Boolean getFacebookAuth() {
		return facebookAuth;
	}

	public void setFacebookAuth(Boolean facebookAuth) {
		this.facebookAuth = facebookAuth;
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().setUserExternalLoginPlatforms(getAuthPlatforms());
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				fire(new EditorEvent(target));
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	private List<UserExternalLoginPlatform> getAuthPlatforms() {
		
		List<UserExternalLoginPlatform> platforms = new ArrayList<UserExternalLoginPlatform>();
		
		List<ExternalPlatformId> ids = new ArrayList<ExternalPlatformId>();
		
		if (getGoogleAuth()) 
			ids.add(ExternalPlatformId.GOOGLE);
		
		if (getFacebookAuth()) 
			ids.add(ExternalPlatformId.FACEBOOK);
		
	
		for (ExternalPlatformId platformId : ids) {
			
			UserExternalLoginPlatform platform = new KbeeUserExternalLoginPlatform();
			
			platform.setEnabled(true);
			platform.setPlatformId(platformId.getId());
			platform.setUserPlatformIdType(UserExternalPlatformIdType.EMAIL.getId());
			
			if (platformId.getId()==ExternalPlatformId.GOOGLE.getId() ) {
				platform.setUserPlatformId(this.getGoogleUserPlatformId()!=null ? this.getGoogleUserPlatformId() : getProfile().getPerson().getEmail() );
			}
			else if (platformId.getId()==ExternalPlatformId.FACEBOOK.getId() ) {
				platform.setUserPlatformId(this.getFacebookUserPlatformId()!=null ? getProfile().getPerson().getEmail() : getProfile().getPerson().getEmail() );
			}
			else
				platform.setUserPlatformId(getProfile().getPerson().getEmail());
			
			
			platform.setUserProfile(getModelObject());
			platforms.add(platform);
		}
		return platforms;
	}
	
	private Boolean hasAuthPlatform(ExternalPlatformId platform) {
		UserProfile profile = getProfile();
		if (profile==null) return Boolean.valueOf(false);
		for (UserExternalLoginPlatform p : profile.getUserExternalLoginPlatforms()) {
			if (p.getPlatformId() == platform.getId()) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}
	
	private UserProfile getProfile() {
		return getModelObject();
	}
}

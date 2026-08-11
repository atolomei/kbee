package kbee.web.idoc;

import org.apache.wicket.markup.head.CssUrlReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSignature;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.eform.EFormViewer;
import kbee.web.eform.EPdfSignaturePanel;
import kbee.web.page.KbeeWebPage;

public class SharedSignaturePage extends KbeeWebPage<UserSignature> {
	private static final long serialVersionUID = 1L;
				
	private static Logger logger = Logger.getLogger(SharedSignaturePage.class.getName());
	
	private static final ResourceReference CSS_EFORM = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");

	
	public SharedSignaturePage() {
	}

	public SharedSignaturePage(PageParameters parameters) {
		
		UserSignature signature = getSignature(parameters);
	
		if (signature!=null) {
			setModel(new ObjectModel<UserSignature>(signature));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
//		response.render(CssHeaderItem.forReference(KBEE_SEARCHER_CSS));
//		response.render(CssHeaderItem.forReference(ICONS_CSS));
//		
//		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
//		
//		response.render(CssHeaderItem.forReference(CORE_CSS));
//		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
//
//		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
//
//		response.render(JavaScriptHeaderItem.forReference(KBEE_JS));
//		
//		response.render(CssHeaderItem.forReference(AW));
//		
//		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
//		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
//		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
//		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
//		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
//		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		String url = String.valueOf((RequestCycle.get().urlFor(CSS_EFORM, null)));
		String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		response.render(CssUrlReferenceHeaderItem.forUrl(absoluteUrl));
	}

	public UserSignature getSignature() {
		return getModel()!=null ? getModelObject() : null;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getSignature()==null) {
			addOrReplace(new ErrorPanel("signature", new Model<String>("Signature"), new Model<String>("user not found or access denied.")));
			return;
		}
		
		add(new EPdfSignaturePanel("signature", getSignature()));
	}
	
	protected UserSignature getSignature(PageParameters parameters) {
		try {
			StringValue userid = parameters.get("user");
			if (userid.isNull() || userid.isEmpty()) return null;
			
			User user = getSecurityDao().findUserById(Long.valueOf(userid.toString()));
			if (user==null) return null;
			UserProfile profile = getContentDao().findUserProfileByUser(user);
			
			StringValue deviceIdParameter = parameters.get("device");
			if (deviceIdParameter.isNull() || deviceIdParameter.isEmpty()) return null;
			String deviceId = deviceIdParameter.toString();
			
			if (profile==null) return null;
			for (UserSignature signature :  profile.getSignatures()) {
				if (ObjectState.ENABLED.equals(signature.getState()) &&
					SignatureType.PHONE_APP.equals(signature.getType()) && 
					deviceId.equals(signature.getDevice().getDeviceId())) {
					return signature;
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	protected SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
}
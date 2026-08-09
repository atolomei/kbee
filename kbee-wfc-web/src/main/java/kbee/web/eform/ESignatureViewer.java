package kbee.web.eform;

import java.time.ZoneId;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.SignedData;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormData;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.PropertiesFactory;

import kbee.web.console.MyBoxQuery;
import kbee.web.resource.WebResourceReference;

public class ESignatureViewer extends ModelPanel<EFormData>  {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ESignatureViewer.class.getName());
	
	static Boolean ACCEPT_ALL_SIGNATURES = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("accept-all-signatures", "no").trim());
	
	static Boolean SIMULATE_SIGNATURE = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("simulate-hand-written-signature", "no").trim());
	
	
	
	
	
	
	
	
	public ESignatureViewer(String id, IModel<EFormData> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	public void onInitialize() {
		super.onInitialize();
		
		if (getSignature()!=null) {
			if (getSignature().getType().equals(SignatureType.PHONE_APP)) {
				add(new Image("image", new WebResourceReference( getSignatureImage() )));
			}
			else {
				add(new InvisiblePanel("image"));
			}
			add(new Label("user", () -> getUserName(getSignature())));	
			add(new Label("device", () -> getDate(getModelObject().getSignatures().get(0))));
		} 
		else {
			logger.debug("signature is null");
			add(new GenericPhoto("image"));
			add(new Label("user", ""));	
			add(new Label("device", ""));
		}
	}
	
	
	

	
	protected KbeeUserSignature getSignature() {
		
		if (getModelObject()==null)
			return null;
		
		if (!getModelObject().isSigned())
			return null;
		

		UserSignature signature = getModelObject().getSignatures().get(0).getSignature();
		return (KbeeUserSignature)signature;
		
	}
	
	public String getUserName(KbeeUserSignature signature) {
		return signature.getUserProfile().getUser().getLastFirstName();
	}
	

	
	
	protected KBFile getSignatureImage() {
		KBFile imagefile = getSignature().getHandWriteImage();
		
		/**if (imagefile==null)  {
			if (SIMULATE_SIGNATURE) {
				MyBoxQuery query  = new MyBoxQuery( ServiceLocator.getService(SecurityService.class).getSessionUser(), getQueryIndex());
				
				ResultSet kbfiles = query.execute();
				int index=0;
				while (kbfiles.hasNext() && index++<1000) {
					IDoc content = (IDoc) kbfiles.next().getObject();
					if (content!=null && content.getResources()!=null && content.getResources().size()>0) {
						KBFile kb= (KBFile) content.getResources().get(0);
						if (	kb.getFileName()!=null && (kb.getFileName().toLowerCase().contains("signature") ||
								kb.getFileName().toLowerCase().contains("firma"))) {
							imagefile=kb;
							break;
						}
					}
				}
			}
		}**/
		
		return imagefile;
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
	
	protected User getSessionUser() {
        try {
            return ServiceLocator.getService(SecurityService.class).getSessionUser();
        } 
        catch (Exception e) {
        	logger.error(e);
            return null;
        }
    }
	
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	
	
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}	
	
}

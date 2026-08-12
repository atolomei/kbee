package com.novamens.content.web.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.form.EFormData;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.ObjectState;
import com.novamens.file.PdfInfo;
import com.novamens.file.PdfService;
import com.novamens.kbee.wicket.util.PanelCapture;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;

import kbee.web.eform.EFormViewer;
import kbee.web.eform.EPdfSignaturePanel;
import kbee.web.error.ErrorPanel;
import kbee.web.page.ApplicationPage;
import software.amazon.awssdk.utils.StringInputStream;

public class Test3 extends ApplicationPage<Void> {
				
	
	private static final long serialVersionUID = 1L;


	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Test3.class.getName()); 

	
	ResourceReference css  = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
	//ResourceReference css2  = new CssResourceReference(Form.class, "fontawesome-pro-6.1.1-web/css/duotone.css");

	WebMarkupContainer mc;
	
	public Test3()	{
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		
		
		
		UserSignature signature = getUserSignature();
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		if (profile.getSignatures()==null || profile.getSignatures().size()==0) {
			logger.error("User has no Signature");
			add( new ErrorPanel("container", new Model<String>("No signature")));
			return;
		}
		
		add(new EPdfSignaturePanel(signature));
		
//		KBFile file = (KBFile)getContentDao().findResourceById(KBFile.class, 346117229);
//		add(new KbeePdfViewer("pdf", new ObjectModel<KBFile>(file)));
//		
//		add(new  AjaxLink<>("sign") {
//			public void onClick(AjaxRequestTarget taregt) {
//				sign();
//			}
//		});
//		
		add(new  DonwloadMenuItemPanelV5<EFormData>("link") {
			@Override 
			public String getLabel() {
				return "BAJAR";
			}
			@Override
			public boolean isDeleteFileAfterDownload()  {
				return true;
			}
			public String getFileName() {
				return "test.pdf";
			}
			@Override
			protected File getFile() {
				try {
					String filename = getFileName();
					File file = File.createTempFile(filename, ".pdf");
					//OutputStream output = new FileOutputStream(file);
					PdfInfo info = new PdfInfo();
					info.setFileName(filename);
			        file = ServiceLocator.getService(PdfService.class).convertHtml(getHtmlStream(signature), info);
					//output.close();
					return file;
				}
				catch (IOException e) {
					return null;
				}
			}
		});
	}
	
	
	
	public InputStream getHtmlStream(UserSignature signature) {
		String prefix = "<html><head>";
		prefix += "<link rel=\"stylesheet\" type=\"text/css\" href=\""+getCssUrl()+"\">";
		prefix += "</head><body>";
        PanelCapture capture = new PanelCapture(new EPdfSignaturePanel(signature));
        String sufix="</body></html>";		
        String capturestring = capture.getString();	
        capturestring = capturestring.replace("&amp;", "&");
        InputStream stream = new StringInputStream( prefix + capturestring + sufix );
        return stream;
 	}
	
	public void sign() {
		try {
			File input = new File("c:\\temp\\in.pdf");
			UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			UserSignature signature = profile.getSignatures().get(2);
			
			File temp = new File("c:\\temp\\out.pdf");
			
		    OutputStream signedoutput = new FileOutputStream(temp);
			Certificate certificate = signature.getCertificate();
			PrivateKey privateKey = signature.getPrivateKey();
			Certificate caCertificate = profile.getDomain().getCertificate();
			InputStream stream = getHtmlStream(signature);
			String string = ((StringInputStream)stream).getString();    
			ServiceLocator.getService(PdfService.class).sign(input, caCertificate, certificate, privateKey, signedoutput, string);
			signedoutput.close();
		}
		catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	public UserSignature getUserSignature() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		UserSignature signature = null;
		for (UserSignature s : profile.getSignatures()) {
			if (ObjectState.ENABLED.equals(s.getState())) {
				signature = s;
				break;
				
			}
		}
		return signature;
	}
	
	public String getCssUrl() {
		ResourceReference css  = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
		String url = String.valueOf((RequestCycle.get().urlFor(css, null)));
		String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		return absoluteUrl;
	} 
	
	public void renderHead(IHeaderResponse response) {
		// super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(css));
		//response.render(CssHeaderItem.forReference(css2));
	}	
}

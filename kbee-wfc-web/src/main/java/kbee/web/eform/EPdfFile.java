package kbee.web.eform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.SignedData;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.service.ContentService;
import com.novamens.file.PdfInfo;
import com.novamens.file.PdfService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;

import software.amazon.awssdk.utils.StringInputStream;

public class EPdfFile {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EPdfFile.class.getName());

	
	EFormData data;
	
	public EPdfFile(EFormData data) {
		setData(data);
	}

	public EFormData getData() {
		return data;
	}

	public void setData(EFormData data) {
		this.data = data;
	}
	
	public File getFile() throws IOException, SignatureException  {
		String filename = getFileName();
		
		//File file = File.createTempFile(filename, ".pdf");
		//OutputStream output = new FileOutputStream(file);
        //ServiceLocator.getService(PdfService.class).getStream(getHtmlStream(), output);
		//output.close();
		
		PdfInfo info = new PdfInfo();
		info.setFileName(filename);
		info.setTitle(getTitle());
		info.setSubject(getSubject());
		info.setAuthor(getAuthor());
		info.setCreator("Kbee");
		
        File file = ServiceLocator.getService(PdfService.class).convertHtml(getHtmlStream(), info);
		
		
		if (isSignedData()) {
			file = sign(file);
		}
		return file;
	}
	
	
	public InputStream getHtmlStream() {
		String prefix = "<html><head>";
		prefix+="<link rel=\"stylesheet\" type=\"text/css\" href=\""+getCssUrl()+"\">";
		prefix+="<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,400;0,900;1,400&amp;display=swap\">";
		prefix+="</head><body>";
        EFormCapture eform_capture = new EFormCapture(getData());
        String sufix="</body></html>";		
        String capture = eform_capture.getString();											
        InputStream stream = new StringInputStream( prefix + capture + sufix );
        return stream;
 	}
	
	
		
	public File sign(File file) throws IOException, SignatureException {
		
		String signedname = getFilenameWithoutExtension()+"-signed.pdf";
		
        File signedtemp = File.createTempFile(signedname, ".pdf");
        OutputStream signedoutput = new FileOutputStream(signedtemp);
        InputStream input = new FileInputStream(file);
		SignedData signeddata = data.getSignatures().get(0);
		Certificate certificate = signeddata.getSignature().getCertificate();
		PrivateKey privateKey = signeddata.getSignature().getPrivateKey();
		User user = getSessionUser();
		Certificate caCertificate = ((KbeeUser)user).getDomain().getCertificate(); 
		ServiceLocator.getService(PdfService.class).sign(input, caCertificate, certificate, privateKey, signedoutput);
		signedoutput.close();
		file.delete();
		return signedtemp;
	}
	
	public boolean isSignedData() {
		return !getData().getSignatures().isEmpty();
	}
	
	public String getCssUrl() {
		ResourceReference css  = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
		String url = String.valueOf((RequestCycle.get().urlFor(css, null)));
		String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		return absoluteUrl;
	}
	
	
	private String getFilenameWithoutExtension() {
		
		try {
			String title = getData().getObjectTitle();
			if (title==null) 
				title = getData().getForm().getDisplayName();
			
			title= title.toLowerCase().replace(" ", "-");
			title= title.replace("\r", "");
			title= title.replace("\n", "");
			
			DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd");
			String filename = df.format(OffsetDateTime.now()) + "-" + title.replace(" ", "-");
			return filename;
		} 
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
			
		}
	}
	
	public String getFileName() {
		return getFilenameWithoutExtension()+".pdf";
	}
	
	public String getTitle() {
		return getContent()!=null ? getContent().getTitle() : getData().getForm().getDisplayName();
	}
	
	public String getSubject() {
		String subject = "-";
		Content content = getContent();
		if (content!=null) {
			subject =content.getService(ContentService.class).getConsoleSubtitle();
		}
		return subject;
	}
	
	public String getAuthor() {
		String author = "-";
		Content content = getContent();
		if (content!=null) {
			author =content.getLastModifiedUser().getDisplayName();
		}
		else {
			author = getSessionUser().getDisplayName();
		}
		return author;
	}
	
	public Content getContent() {
		return getData() instanceof EFormContentData ? ((EFormContentData)getData()).getContent() : null;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}

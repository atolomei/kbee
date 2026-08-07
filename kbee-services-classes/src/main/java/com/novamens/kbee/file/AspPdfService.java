package com.novamens.kbee.file;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.apache.commons.io.FileUtils;

import com.aspose.pdf.Document;
import com.aspose.pdf.HtmlLoadOptions;
import com.aspose.pdf.HtmlMediaType;
import com.aspose.pdf.HtmlSaveOptions;
import com.aspose.pdf.HtmlSaveOptions.CssSavingInfo;
import com.aspose.pdf.HtmlSaveOptions.CssUrlRequestInfo;
import com.aspose.pdf.LoadOptions.ResourceLoadingResult;
import com.aspose.pdf.MarginInfo;
import com.aspose.pdf.PKCS7;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageInfo;
import com.aspose.pdf.SaveOptions.ResourceSavingInfo;
import com.aspose.pdf.facades.PdfFileInfo;
import com.aspose.pdf.facades.PdfFileSignature;
import com.novamens.file.PdfInfo;
import com.novamens.file.PdfService;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.CertificateParser;
import com.novamens.signature.SignatureException;

/**
 * 
 * 
 * 
 *
 */
public class AspPdfService implements PdfService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AspPdfService.class.getName());
	
	public AspPdfService () {
		try {
		com.aspose.pdf.License license = new com.aspose.pdf.License();
		//InputStream stream2 = resource.getInputStream();
		InputStream in = this.getClass().getResourceAsStream("Aspose.PDF.Java.lic");
		license.setLicense(in);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getStream(InputStream input, OutputStream output) throws IOException {
		
        HtmlLoadOptions options = new HtmlLoadOptions();
        
        options.setHtmlMediaType(HtmlMediaType.Print);
        options.setEmbedFonts(true);
        
        PageInfo page= new PageInfo();
        
        MarginInfo mi = new MarginInfo(0,0,0,0);
        page.setMargin(mi);
        
        page.setLandscape(false);
        
        
        
        options.setPageInfo(page);
        
        options.setCustomLoaderOfExternalResources(new HtmlLoadOptions.ResourceLoadingStrategy() {
            public ResourceLoadingResult invoke(String resourceURI) {
                ResourceLoadingResult res = new ResourceLoadingResult(new byte[] {});
                res.setLoadingCancelled(true);
                return res;
            }
        });
        Document document = new Document(input, options);
        document.save(output);
        document.close();
	}
	
	public File convertHtml(InputStream input, PdfInfo info) throws IOException {
		
		File file = File.createTempFile(info.getFileName(), ".pdf");
		
		OutputStream output = new FileOutputStream(file);
		  
        HtmlLoadOptions options = new HtmlLoadOptions();
        
        options.setHtmlMediaType(HtmlMediaType.Print);
        options.setEmbedFonts(true);
        
        PageInfo page= new PageInfo();
        
        MarginInfo mi = new MarginInfo(0,0,0,0);
        page.setMargin(mi);
        
        page.setLandscape(false);
        
        options.setPageInfo(page);
        
        options.setCustomLoaderOfExternalResources(new HtmlLoadOptions.ResourceLoadingStrategy() {
            public ResourceLoadingResult invoke(String resourceURI) {
                ResourceLoadingResult res = new ResourceLoadingResult(new byte[] {});
                res.setLoadingCancelled(true);
                return res;
            }
        });
        Document document = new Document(input, options);
        document.save(output);
        document.close();
        
        PdfFileInfo fileInfo = new PdfFileInfo(file.getPath());
        
        // Set PDF information
        fileInfo.setAuthor(info.getAuthor());
        fileInfo.setTitle (info.getTitle());
        fileInfo.setSubject (info.getSubject());
        //fileInfo.setKeywords("Peace and Development");
        fileInfo.setCreator (info.getCreator());
        
        fileInfo.saveNewInfo(file.getPath());
        
        return file;
 	}

	
	public File getHtml(String id, InputStream input) throws IOException {
		
	        
		String rootFolder = ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "pdfserver" + File.separator + "db";
		File htmlFileFolder = new File(rootFolder+File.separator+id+File.separator);	
		File htmlFile = new File(htmlFileFolder, "pdf.html");
		
		if (htmlFile.exists()) {
			return htmlFile;
		}
		
		Document doc = new Document(input);

		HtmlSaveOptions options = new HtmlSaveOptions();
		doc.setTitle(rootFolder);

	   
		options.setCustomCssSavingStrategy(new HtmlSaveOptions.CssSavingStrategy() {
			@Override
			public void invoke(CssSavingInfo savingInfo) {
				try {
					File outputFile = new File (htmlFileFolder, "styles.css");
					FileUtils.copyInputStreamToFile(savingInfo.getContentStream(), outputFile);
				}
				catch (IOException e) {
					logger.error(e);
				}
			}
		});
		
		options.setCustomResourceSavingStrategy(new HtmlSaveOptions.ResourceSavingStrategy() {
			@Override
			public String invoke(ResourceSavingInfo savingInfo) {
				String resourceName  =savingInfo.getSupposedFileName();
				try {
					File outputFile = new File (htmlFileFolder, resourceName);
					FileOutputStream outputStream = new FileOutputStream(outputFile);
	        		outputStream.write(savingInfo.getContentStream());
	        		outputStream.close();
				}
				catch (IOException e) {
					logger.error(e);
				}
        		return "/pdfserver/"+id+"/"+resourceName;
			}			
		});
		
		options.setCustomStrategyOfCssUrlCreation(new HtmlSaveOptions.CssUrlMakingStrategy() {
			@Override
			public String invoke(CssUrlRequestInfo info) {
				return "/pdfserver/"+id+"/styles.css";
			}
		});
	        
		doc.save(htmlFile.getPath(), options);
	        
		doc.close();
	        
		return htmlFile;
	}

	
	/** ------------------------------------------
	 * 
	 * 
	 */
	public void sign(File input, Certificate caCertificate, Certificate certificate, PrivateKey key, OutputStream output, String signaturestream) throws SignatureException {
		try {
	        
			Document originalDocument = new Document(input.getAbsolutePath());
			Page originalDocumentPage  = originalDocument.getPages().get_Item(1);
	        PageInfo originalDocumentPageInfo = originalDocumentPage.getPageInfo();

	        HtmlLoadOptions htmloptions = new HtmlLoadOptions();
	        htmloptions.setHtmlMediaType(HtmlMediaType.Print);
	        htmloptions.setEmbedFonts(false);
	        htmloptions.setPageInfo(originalDocumentPageInfo);
	        htmloptions.getPageInfo().setWidth(originalDocumentPage.getMediaBox().getWidth());
	        htmloptions.getPageInfo().setHeight(originalDocumentPage.getMediaBox().getHeight());
	        
	        Document signaturedoc = new Document(new ByteArrayInputStream(signaturestream.getBytes()), htmloptions);
			
			Page signaturePage = signaturedoc.getPages().get_Item(1);
			//originalDocumentPage.getMediaBox().getWidth();
			//signaturePage.setMediaBox(originalDocumentPage.getMediaBox());
			
			signaturePage.setPageInfo(originalDocumentPageInfo);
			
			originalDocument.getPages().add(signaturePage);
			originalDocument.save();
			originalDocument.close();
			
			originalDocument = new Document(input.getAbsolutePath());
			
			// Create a field
			//((Page p = originalDocument.getPages().get_Item(originalDocument.getPages().size());
			//SignatureField signatureField = new SignatureField(p, new com.aspose.pdf.Rectangle(100, 200, 300, 300));
			//signatureField.setName("signature");
			//signatureField.setPartialName("psignature");

			//Add field to the document
			//originalDocument.getForm().add(signatureField, originalDocument.getPages().size());
			
			PdfFileSignature signature = new PdfFileSignature(originalDocument);
			String password = "Pa$$w0rd2020";
			byte pfx[] = CertificateParser.Get().writePfx(caCertificate, certificate, key, password);
			ByteArrayInputStream stream = new ByteArrayInputStream(pfx);
	        PKCS7 pkcs = new PKCS7(stream, password); // Use PKCS7/PKCS7Detached
	        
	        //SignatureCustomAppearance signatureCustomAppearance = new SignatureCustomAppearance();
	        //signatureCustomAppearance.setFontSize(6);
	        //signatureCustomAppearance.setFontFamilyName("Helvetica");
	        //signatureCustomAppearance.setDigitalSignedLabel("SIGNED BY:");
	        //pkcs.setCustomAppearance(signatureCustomAppearance);
	        
	        signature.sign(originalDocument.getPages().size(), false, new java.awt.Rectangle(100, 10, 4, 2), pkcs);
	        // Save output PDF f ile
	        signature.getDocument().optimize();
	        signature.save(output);
	        signature.close();
		}
		catch (Exception e) {
			logger.error(e);
			throw new SignatureException(e);
		}
	}
	
	/** ------------------------------------------
	 * 
	 * 
	 */
	public void getSigned(File input, OutputStream output, String signaturestream) throws IOException {
		try {
	        
			Document originalDocument = new Document(input.getAbsolutePath());
			Page originalDocumentPage  = originalDocument.getPages().get_Item(1);
	        PageInfo originalDocumentPageInfo = originalDocumentPage.getPageInfo();

	        HtmlLoadOptions htmloptions = new HtmlLoadOptions();
	        htmloptions.setHtmlMediaType(HtmlMediaType.Print);
	        htmloptions.setEmbedFonts(false);
	        htmloptions.setPageInfo(originalDocumentPageInfo);
	        htmloptions.getPageInfo().setWidth(originalDocumentPage.getMediaBox().getWidth());
	        htmloptions.getPageInfo().setHeight(originalDocumentPage.getMediaBox().getHeight());
	        
	        Document signaturedoc = new Document(new ByteArrayInputStream(signaturestream.getBytes()), htmloptions);
			
			Page signaturePage = signaturedoc.getPages().get_Item(1);
			
			signaturePage.setPageInfo(originalDocumentPageInfo);
			
			originalDocument.getPages().add(signaturePage);
			originalDocument.save(output);
			originalDocument.close();
		}
		catch (Exception e) {
			logger.error(e);
			throw new IOException(e);
		}
	}

	
	public void sign(InputStream input, Certificate caCertificate, Certificate certificate, PrivateKey key, OutputStream output) throws SignatureException {
		try {
			//List<String> fonts = FontRepository.getLocalFontPaths();
			//fonts.add("/usr/share/fonts/msttcore");
			//FontRepository.setLocalFontPaths(fonts);
			
			Document document = new Document(input);
	
			PdfFileSignature signature = new PdfFileSignature(document);
			String password = "Pa$$w0rd2020";
			byte pfx[] = CertificateParser.Get().writePfx(caCertificate, certificate, key, password);
			ByteArrayInputStream stream = new ByteArrayInputStream(pfx);  
	        PKCS7 pkcs = new PKCS7(stream, password); // Use PKCS7/PKCS7Detached
	        signature.sign(1, false, new java.awt.Rectangle(300, 100, 400, 200), pkcs);
	        // Save output PDF f ile
	        signature.save(output);
		        signature.close();
		}
		catch (Exception e) {
			logger.error(e);
			throw new SignatureException(e);
		}
	}

}

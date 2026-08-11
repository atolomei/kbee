package test.com.novamens.kbee.idoc.webapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DealRoomSoapTest {

	@Test
	public void test0() {
		try {
			
			
		String url="http://int2016b.pct.realpage.com/WebServices/DocumentManagement/Document.asmx";
		
		TrustManager[] trustAllCerts = new TrustManager[]{
		        new X509TrustManager() {

		            public java.security.cert.X509Certificate[] getAcceptedIssuers()
		            {
		                return null;
		            }
		            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
		            {
		                //No need to implement.
		            }
		            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
		            {
		                //No need to implement.
		            }
		        }
		};

		// Install the all-trusting trust manager
		try 
		{
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} 
		catch (Exception e) 
		{
		    // System.out.println(e);
		}
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		//HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Apache-HttpClient/4.5.2");
		con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		con.setRequestProperty("Content-Length", "720");
		con.setRequestProperty("SOAPAction", "http://tempuri.org/GetRPDDDealRoomFile");
				
		String siteId="8000095";
		String pmcId ="8000094";
		String fileId ="F2B7AC9D-DB28-40CD-AA26-CC3C7EC70F87";
		
//		String uri = "https://idoc7.realpage.com/api/file/onesitedm/cwsapartments/5cc8acb0-3da5-479c-8105-6247ca832dd4";

		String xmlRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"+
				  "<soapenv:Header>"+
				      "<tem:UserAuthentication>"+
				         "<tem:PMCID>"+pmcId+"</tem:PMCID>"+
	 			         "<tem:SiteID>"+siteId+"</tem:SiteID>"+
				         "<tem:UserID>1</tem:UserID>"+
				    
				         "<tem:Password>?</tem:Password>"+
				    
				         "<tem:SessionGUID>4554DFA3-BA7C-498F-A3E1-C47D01518791</tem:SessionGUID>"+
				   
				         "<tem:UserLogin>?</tem:UserLogin>"+
				      "</tem:UserAuthentication>"+
				   "</soapenv:Header>"+
				   "<soapenv:Body>"+
				      "<tem:GetRPDDDealRoomFile>"+
		               "<tem:rpddDealRoomID>"+fileId+"</tem:rpddDealRoomID>"+
				      "</tem:GetRPDDDealRoomFile>"+
				   "</soapenv:Body>"+
				"</soapenv:Envelope>";
		
		con.setRequestProperty("Content-Length", String.valueOf(xmlRequest.length()));

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(xmlRequest);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + url);
		//// System.out.println("Post parameters : " + urlParameters);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in;
		if (responseCode==200) {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}
		else {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		StringBuilder xmlStringBuilder = new StringBuilder();
		ByteArrayInputStream input =  new ByteArrayInputStream(
			response.toString().getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element element = doc.getDocumentElement();
		
		Node elements1 = element.getFirstChild();
		
		Node elements2 = elements1.getFirstChild();
		
		elements2.getTextContent();
		
		NodeList elements3 = element.getElementsByTagName("Body");
		
		
		FileOutputStream fos = new FileOutputStream("c:\\temp\\SOAP.pdf");
		fos.write(Base64.decodeBase64(elements2.getTextContent()));
		fos.close();

		//print result
		// System.out.println(elements2.getTextContent());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		
	}
	
//	@Test
//	public void test1() {
//		
//		DOMResult response	= new DOMResult();
//		SaajSoapMessageFactory factory = new SaajSoapMessageFactory();
//		factory.setSoapVersion(SoapVersion.SOAP_11);
//		factory.afterPropertiesSet();
//		WebServiceTemplate template = new WebServiceTemplate(factory);
//		template.setDefaultUri("http://sat2012c.sat.realpage.com/webservices/documentmanagement/document.asmx");
//		template.setMessageSender(new HttpUrlConnectionMessageSender());
//		
//		
//		
//		String xmlRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"+
//		  "<soapenv:Header>"+
//		      "<tem:UserAuthentication>"+
//		         "<tem:PMCID>7041446</tem:PMCID>"+
//		         "<tem:SiteID>7041453</tem:SiteID>"+
//		         "<tem:UserID>1</tem:UserID>"+
//		    
//		         "<tem:Password>?</tem:Password>"+
//		    
//		         "<tem:SessionGUID>4554DFA3-BA7C-498F-A3E1-C47D01518791</tem:SessionGUID>"+
//		   
//		         "<tem:UserLogin>?</tem:UserLogin>"+
//		      "</tem:UserAuthentication>"+
//		   "</soapenv:Header>"+
//		   "<soapenv:Body>"+
//		      "<tem:GetDocumentInByteArray>"+
//		               "<tem:DocumentID>BAFDD205-C14E-469D-AB15-9B5D75DCFCFA</tem:DocumentID>"+
//		      "</tem:GetDocumentInByteArray>"+
//		   "</soapenv:Body>"+
//		"</soapenv:Envelope>";
//		
//		StreamSource request  = new StreamSource(new StringReader(xmlRequest));
//		
//		WebServiceMessageCallback callback = new WebServiceMessageCallback() {
//	        public void doWithMessage(WebServiceMessage message) {
//	            ((SoapMessage)message).setSoapAction("http://tempuri.org/GetDocumentInByteArray");
//	            ((SaajSoapMessage)message).getSaajMessage().getMimeHeaders().setHeader(TransportConstants.HEADER_CONTENT_TYPE, "text/xml;charset=UTF-8");
//	            ((SaajSoapMessage)message).getSaajMessage().getMimeHeaders().setHeader(TransportConstants.HEADER_CONTENT_LENGTH, "720");
//	            ((SaajSoapMessage)message).getSaajMessage().getMimeHeaders().setHeader(TransportConstants.HEADER_ACCEPT_ENCODING, "gzip,deflate");
//	        }
//		};
//	        
//		boolean haveResponse = template.sendSourceAndReceiveToResult(request, callback,  response);
//		if (haveResponse) {
//			// System.out.println(response);
//		}
//	}
}

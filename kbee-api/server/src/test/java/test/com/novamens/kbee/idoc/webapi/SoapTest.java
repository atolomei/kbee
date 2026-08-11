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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
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


public class SoapTest {

	@Test
	public void test0() {
		try {
			
			
		String url="https://lm-10-22-161-166.onesitedev.realpage.com/WebServices/DocumentManagement/Document.asmx";
		
		URL obj = new URL(url);
		//HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Apache-HttpClient/4.5.2");
		con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		con.setRequestProperty("Content-Length", "720");
		con.setRequestProperty("SOAPAction", "http://tempuri.org/GetDocumentInByteArray");
				
		String siteId="1193451";
		String pmcId ="1192422";
		String fileId ="1f2607b1-ea29-4218-9bd7-315d16f3ef54";
		
//		String uri = "https://idoc7.realpage.com/api/file/onesitedm/cwsapartments/5cc8acb0-3da5-479c-8105-6247ca832dd4";

		String xmlRequest = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
				  "<soapenv:Header>"+
				      "<UserAuthentication>"+
				         "<PMCID>"+pmcId+"</PMCID>"+
				         "<SiteID>"+siteId+"</SiteID>"+
				         "<UserID>3530457</UserID>"+
				    
				         "<Password>?</Password>"+
				    
				         "<SessionGUID>4554DFA3-BA7C-498F-A3E1-C47D01518791</SessionGUID>"+
				   
				         "<UserLogin>?</UserLogin>"+
				      "</UserAuthentication>"+
				   "</soapenv:Header>"+
				   "<soapenv:Body>"+
				      "<GetRPDDDealRoomFile>"+
		               "<rpddDealRoomID>"+fileId+"</rpddDealRoomID>"+
				      "</<GetRPDDDealRoomFile>"+
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
}

package test.com.novamens.kbee.idoc.webapi;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class SamlTest {
	
	@Test
	public void test() {
		try {
			String saml = "<samlp:Response ID=\"_491a56b7-cbe7-4ccd-918d-5dd3e50d23bd\" Version=\"2.0\" IssueInstant=\"2017-06-23T17:13:35.190Z\" Destination=\"http://testing.idoc.realpage.com/sso/login\" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">GreenBook</saml:Issuer><samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\" /></samlp:Status><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" /><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\" /><Reference URI=\"#_491a56b7-cbe7-4ccd-918d-5dd3e50d23bd\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" /><Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"><InclusiveNamespaces PrefixList=\"#default samlp saml ds xs xsi\" xmlns=\"http://www.w3.org/2001/10/xml-exc-c14n#\" /></Transform></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /><DigestValue>FfkX+EBQBQILkOeUqx8vcn/8B98=</DigestValue></Reference></SignedInfo><SignatureValue>C2TEgCCoJfeoxb5LBArjnfDzn9U8RTlaxLoLZcQHrm88QN5i8TMTQfYBTWx28P3VoYuaMQPVWeV5LJ78maLZ0v4wvnu0LEJ9G2ycPMOodBDcZFil6NUSbiWWP5q5Rno8Q+B6nm1DgWYZI9ox+0MzK0Ydlsk8XM3ILWsocTfxWObQ1b884NfmxOqR/dlCav4GaZWewa0ClW5K5ZsnjwkEqqeQN3akU5NLB91vxtIgF12Yt/XPmreLY89bfyckJ3aaS30nn26RqlV6GI90KWviAlJLpA97yfeiuy1DnrwTJroQ1wU0DT4Cna+R6aCDu44Go1BfkKalBmnS89EdGBI2Ww==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate></X509Data></KeyInfo></Signature><saml:Assertion ID=\"_0babcdc0-9f42-49f8-ac3a-7188c821b10c\" IssueInstant=\"2017-06-23T17:13:35.190Z\" Version=\"2.0\" xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"><saml:Issuer>GreenBook</saml:Issuer><saml:Subject><saml:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">root@demo</saml:NameID></saml:Subject><saml:Conditions NotBefore=\"2017-06-23T16:13:35.190Z\" NotOnOrAfter=\"2017-06-23T18:13:35.190Z\" /><saml:AuthnStatement AuthnInstant=\"2017-06-23T17:13:35.190Z\" SessionIndex=\"560ed22b-ff5d-4a07-8996-7f62842e5389\"><saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement><saml:AttributeStatement><saml:Attribute Name=\"productUsername\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\" FriendlyName=\"productUsername\"><saml:AttributeValue>root@demo</saml:AttributeValue></saml:Attribute><saml:Attribute Name=\"EnterpriseUserId\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\" FriendlyName=\"EnterpriseUserId\"><saml:AttributeValue>83e7c931-97b8-4bfc-9bcc-71da8618dbb7</saml:AttributeValue></saml:Attribute><saml:Attribute Name=\"EnterpriseLogin\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\" FriendlyName=\"EnterpriseLogin\"><saml:AttributeValue>atolomei@realpage.com</saml:AttributeValue></saml:Attribute></saml:AttributeStatement></saml:Assertion></samlp:Response>";
			String r = new String(Base64.encodeBase64(saml.getBytes()));
			String uri = "http://testing.idoc.realpage.com/sso/login?SAMLResponse="+r;
			//String uri = "http://localhost:8080/sso/login?SAMLResponse="+r;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<Boolean> response = restTemplate.exchange(uri, HttpMethod.POST, getCredentials(), Boolean.class);
			// System.out.println(response.getBody());
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getMessage());
		}
	}

	
	private HttpEntity<String> getCredentials(){
		String plainCredentials="root@windsor:w1nR00tw";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		//headers.add("Authorization", "Basic " + base64Credentials);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> credentials = new HttpEntity<String>(headers);
		return credentials;
	}
	
}

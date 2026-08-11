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

import com.novamens.content.model.Classifier;


public class GetTest {

	@Test
	public void test1() {
		//String uri = "http://10.34.216.182:8089/api/classifiers";
		String uri = "http://testing.idoc.realpage.com/api/classifiers";
		//String uri = "http://localhost:8080/api/classifiers";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, getCredentials(), String.class);
		// System.out.println(response);
	}
	
	@Test
	public void test2() {
		try {
			String uri = "http://localhost:8080/api/classifiers/100";
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<Classifier> response = restTemplate.exchange(uri, HttpMethod.GET, getCredentials(), Classifier.class);
			// System.out.println(response.getBody().getDisplayName());
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getMessage());
		}
	}

	
	private HttpEntity<String> getCredentials(){
        String plainCredentials="root@rum:1Aqqqqqq";
      //String plainCredentials="root@edgewood:root";
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<String> credentials = new HttpEntity<String>(headers);
        return credentials;
	}
	
}

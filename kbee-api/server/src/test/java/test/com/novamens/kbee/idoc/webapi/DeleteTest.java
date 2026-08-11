package test.com.novamens.kbee.idoc.webapi;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


public class DeleteTest {

	@Test
	public void test00() {
		try {
			
			String uri = "https://testing-rpdm.realpage.com/api/file/onesitedm/demo/3291124-3521587-f412b250-ebc8-4bea-af64-bb57e36c6fff?permanentDelete=true";
			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}
	
	//@Test
	public void test01() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/cwsapartments/B1F7A18C-9B31-40FF-ADF5-30F0942B5402";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}

	//@Test
	public void test03() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/windsor/F2725F95-6EA5-45B1-A495-C72FAB23DED3";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}

	//@Test
	public void test04() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/windsor/F2725F95-6EA5-45B1-A495-C72FAB23DED4";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}

	//@Test
	public void test05() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/windsor/F2725F95-6EA5-45B1-A495-C72FAB23DED5";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}

	//@Test
	public void test06() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/windsor/F2725F95-6EA5-45B1-A495-C72FAB23DED6";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}

	//@Test
	public void test07() {
		try {

			String uri = "http://localhost:8080/api/file/onesite/windsor/F2725F95-6EA5-45B1-A495-C72FAB23DED7";

			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), String.class);
			
			// System.out.println(response.getBody());
			// System.out.println(response);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}	
	}
	
	private HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + getCredentials());
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}
	
	private String getCredentials(){
		//String plainCredentials="root@demoapi:1Aqqqqqq";
		//String plainCredentials="root@cwsapartments:1Aqqqqqq";
		String plainCredentials="root@kbee:1Aqqqqqq";
		//String plainCredentials="root@windsor:w1nds0rR00t";
		//String plainCredentials="root@kbee:id0cB4sic";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
}

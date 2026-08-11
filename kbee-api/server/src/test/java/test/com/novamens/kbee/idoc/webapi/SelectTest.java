package test.com.novamens.kbee.idoc.webapi;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import kbee.api.model.ApiResource;
import kbee.api.model.IResponse;


 
public class SelectTest {

	@Test
	public void test() {

		String uri = "http://localhost:8080/api/select?s=status(Active)";
		
		
//        RestTemplate restTemplate = new RestTemplate();
//        IDocFile file  = new IDocFile("Tomy","TomyUIri");
//        restTemplate.put(REST_SERVICE_URI+"/user/1", user);
		
//        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
//        headers.add("Authorization", "Basic " + base64Credentials);
//        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        String plainCredentials="root@edgewood:root";
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        
	    //HttpEntity<IFile> file = new HttpEntity<IFile>(new IFile(), headers);

  
	    RestTemplate restTemplate = new RestTemplate();
	    
      //  ResponseEntity<IFile> response = restTemplate.exchange(uri, HttpMethod.PUT, file, IFile.class);
	    ResponseEntity<IResponse> response = restTemplate.exchange(uri, HttpMethod.GET, getCredentials(), IResponse.class);
     
        // System.out.println(response.getBody());
	    // System.out.println(response);

	}
	
    private HttpEntity<String> getCredentials(){
        String plainCredentials="root@edgewood:root";
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<String> credentials = new HttpEntity<String>(headers);
        return credentials;
    }
	
}

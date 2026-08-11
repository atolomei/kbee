package test.com.novamens.kbee.idoc.webapi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.idoc.webapi.client.JacksonHttpMessageConverter;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;

public class PostTest {
	
	
	@Test
	public void test01() {
		try {
			
			//String uri = \"https://testing-rpdm.realpage.com/api/file/onesitedm/demo/3291124-3521587-f412b250-ebc8-4bea-af64-bb57e36c6fff\";
			//String uri = "https://idoc7.realpage.com/api/file/onesitedm/aimco/1188641-2397844-276643fc-5628-43a1-a884-381858e322ab-12058";
			
			String uri = "http://localhost:8080/api/file/onesite/demo4/3707013-3973932-c2140c5f-b021-42db-9859-e31aefed93ff";
				
			ApiFile file = new ApiFile();
			
			file.setTitle("DEMO UNIT TEST 01");
			
			file.setDomain("demo4");
			file.setClassName("Compliance File");
			
			file.setApplication("onesitedm");
			
			file.setExternalId("20209976022");
			
			file.setAttribute("File Type", "Test");
			file.setAttribute("Property", "Test");
			file.setAttribute("Unit", "1A");
			file.setAttribute("Status", "Final");
			
			
			
			RestTemplate restTemplate = new RestTemplate();
			
			List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
			converters.add(new JacksonHttpMessageConverter());
			restTemplate.setMessageConverters(converters);
			
			ResponseEntity<ITransaction> response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<ApiFile>(file, getHeaders()), ITransaction.class);
			
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
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
	
	
	private HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic" + getCredentials());
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}
	
	private String getCredentials(){
		String plainCredentials="root@kbee:root2";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
}
